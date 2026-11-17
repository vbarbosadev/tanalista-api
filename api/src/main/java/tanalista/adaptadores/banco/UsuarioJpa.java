package tanalista.adaptadores.banco;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import tanalista.dominio.Usuario;

@Entity
@Table(name = "usuario")
public class UsuarioJpa {
    @Id public UUID id;
    @Column(name = "email_normalizado", nullable = false, length = 254) public String email;
    @Column(name = "senha_hash", nullable = false, length = 512) public String senhaHash;
    @Column(name = "criado_em", nullable = false) public Instant criadoEm;

    Usuario dominio() { return new Usuario(id, email, senhaHash, criadoEm); }
}
