package tanalista.adaptadores.http;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Clock;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.Map;
import java.util.concurrent.Semaphore;

/** Bounded single-instance limiter; peer IP only, never untrusted forwarded headers. */
@ApplicationScoped
public class LimiteAutenticacao {
    private record Janela(long fim, int usadas) { }
    private final Map<String, Janela> janelas = new HashMap<>();
    private final Semaphore processamento = new Semaphore(2);
    private final Clock relogio;

    public LimiteAutenticacao() { this(Clock.systemUTC()); }
    LimiteAutenticacao(Clock relogio) { this.relogio = relogio; }

    public synchronized void conferir(String ip, String email, boolean cadastro) {
        long agora = relogio.millis();
        janelas.values().removeIf(j -> j.fim <= agora);
        if (janelas.size() >= 10000) throw excesso();
        String operacao = cadastro ? "cadastro:" : "login:";
        consumir(operacao + "ip:" + resumo(ip), cadastro ? 10 : 30, agora);
        consumir(operacao + "conta:" + resumo(email == null ? "" : email.strip().toLowerCase(java.util.Locale.ROOT)),
                cadastro ? 5 : 10, agora);
    }

    private void consumir(String chave, int maximo, long agora) {
        var janela = janelas.getOrDefault(chave, new Janela(agora + 60000, 0));
        if (janela.usadas >= maximo) throw excesso();
        janelas.put(chave, new Janela(janela.fim, janela.usadas + 1));
    }

    public void adquirir() { if (!processamento.tryAcquire()) throw excesso(); }
    public void liberar() { processamento.release(); }

    private WebApplicationException excesso() {
        return new WebApplicationException(Response.status(429).header("Retry-After", "60").build());
    }

    private String resumo(String valor) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(valor.getBytes(StandardCharsets.UTF_8)));
        } catch (java.security.NoSuchAlgorithmException impossivel) { throw new IllegalStateException(impossivel); }
    }
}
