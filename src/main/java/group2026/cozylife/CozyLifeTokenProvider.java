package group2026.cozylife;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;

@Slf4j
@Component
@RequiredArgsConstructor
public class CozyLifeTokenProvider {

    private static final Duration TTL = Duration.ofMinutes(55);
    private static final String LOGIN_PATH = "/app/user/login";
    private static final String SUCCESS_RET = "1";

    private final CozyLifeHttpClient httpClient;
    private final CozyLifeProperties properties;

    private volatile String token;
    private volatile Instant expiresAt;

    public String getToken() {
        if (isValid()) {
            return token;
        }
        return refreshToken();
    }

    public void invalidate() {
        token = null;
        expiresAt = null;
    }

    private synchronized String refreshToken() {
        if (isValid()) {
            return token;
        }

        CozyLifeResponse.Login login = httpClient.post(
                LOGIN_PATH,
                CozyLifeLoginRequest.of(properties.email(), properties.password()),
                CozyLifeResponse.Login.class);

        if (!SUCCESS_RET.equals(login.ret())
                || login.info() == null
                || login.info().token() == null
                || login.info().token().isBlank()) {
            throw new RuntimeException("Не удалось авторизоваться в CozyLife. " + login.desc());
        }

        token = login.info().token();
        expiresAt = Instant.now().plus(TTL);
        return token;
    }

    private boolean isValid() {
        return token != null && expiresAt != null && Instant.now().isBefore(expiresAt);
    }
}