package group2026.joke;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record JokeResponse(
        Boolean error,
        String type,
        @JsonProperty("joke") String singleJoke,
        @JsonProperty("setup") String setup,
        @JsonProperty("delivery") String delivery,
        String lang
) {
    public String getJokeText() {
        if (singleJoke != null && !singleJoke.isEmpty()) {
            return singleJoke;
        }
        if (setup != null && delivery != null) {
            return setup + "\n" + delivery;
        }
        return "";
    }
}
