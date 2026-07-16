package group2026.joke;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class JokeService {

    private final JokeClient jokeClient;

    public String getRandomJoke() {

        return jokeClient
                .getRandomJoke()
                .content();
    }
}
