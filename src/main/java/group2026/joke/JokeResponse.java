package group2026.joke;

import com.fasterxml.jackson.annotation.JsonProperty;

public record JokeResponse(

        @JsonProperty("content")
        String content

) {
}