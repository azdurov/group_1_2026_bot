package group2026.joke;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class JokeService {

    private final JokeClient jokeClient;

    public String getJoke() {
        try {
            Optional<JokeResponse> jokeOpt = jokeClient.getJoke();
            if (jokeOpt.isPresent()) {
                JokeResponse joke = jokeOpt.get();
                if (joke.error() != null && joke.error()) {
                    throw new RuntimeException("Joke API returned error");
                }
                return joke.getJokeText();
            }
        } catch (Exception e) {
            log.error("Error getting joke", e);
            throw new RuntimeException(e.getMessage(), e);
        }
        throw new RuntimeException("Joke is empty");
    }
}
