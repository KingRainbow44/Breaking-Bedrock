package lol.magix.breakingbedrock.game.scoreboards;

import lombok.Data;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Data
public final class ScoreboardContainer {
    /**
     * Creates a new scoreboard container.
     *
     * @param scoreboardId The ID of the scoreboard.
     * @return The new scoreboard container.
     */
    public static ScoreboardContainer of(String scoreboardId) {
        return new ScoreboardContainer(scoreboardId);
    }

    private final String scoreboardId;
    private final Map<Long, String> id2Name
            = new ConcurrentHashMap<>();
}
