package tanalista.adaptadores.http;

import jakarta.ws.rs.core.Application;
import org.eclipse.microprofile.openapi.annotations.enums.SecuritySchemeType;
import org.eclipse.microprofile.openapi.annotations.security.SecurityScheme;

@SecurityScheme(securitySchemeName = "bearerAuth", type = SecuritySchemeType.HTTP,
        scheme = "bearer", bearerFormat = "JWT",
        description = "Access token RS256 do provedor configurado. Obrigatório com AUTH_ENABLED=true; não usar ID token.")
public class SegurancaOpenApi extends Application { }
