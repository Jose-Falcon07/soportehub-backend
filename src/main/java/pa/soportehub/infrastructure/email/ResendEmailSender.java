package pa.soportehub.infrastructure.email;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import pa.soportehub.domain.notification.EmailSender;

/** Adaptador: traduce nuestro puerto EmailSender a la API REST de Resend. */
@Component
public class ResendEmailSender implements EmailSender {

    private static final Logger log = LoggerFactory.getLogger(ResendEmailSender.class);

    @Value("${app.resend.api-key:}")
    private String apiKey;

    @Value("${app.resend.from}")
    private String from;

    private final HttpClient http = HttpClient.newHttpClient();
    private final ObjectMapper mapper;

    public ResendEmailSender(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public void send(String to, String subject, String html) {
        if (apiKey.isBlank()) {
            // Solo desarrollo: sin clave no se envía; se muestra el contenido para poder copiar el enlace
            log.warn("RESEND_API_KEY vacía. Correo NO enviado a {}. Contenido: {}", to, html);
            return;
        }
        try {
            String body = mapper.writeValueAsString(
                    Map.of("from", from, "to", List.of(to), "subject", subject, "html", html));
            HttpRequest request = HttpRequest.newBuilder(URI.create("https://api.resend.com/emails"))
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();
            HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 300) {
                log.error("Resend respondió {}: {}", response.statusCode(), response.body());
            }
        } catch (Exception e) {
            log.error("No se pudo enviar el correo a {}", to, e);
        }
    }
}
