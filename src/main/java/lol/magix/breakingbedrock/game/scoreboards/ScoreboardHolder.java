package lol.magix.breakingbedrock.game.scoreboards;

import lombok.Data;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Data
public final class ScoreboardHolder {
    private final Map<String, ScoreboardContainer> scoreboards
            = new ConcurrentHashMap<>();

    /* These are references to existing scoreboards. */
    private ScoreboardContainer sidebar = null;
    private ScoreboardContainer belowName = null;
    private ScoreboardContainer playerList = null;

    /**
     * Gets a scoreboard from the holder.
     *
     * @param objectiveId The ID of the scoreboard.
     * @return The scoreboard.
     */
    @Nullable
    public ScoreboardContainer getScoreboard(String objectiveId) {
        return this.scoreboards.get(objectiveId);
    }

    /**
     * Adds a scoreboard to the holder.
     *
     * @param scoreboard The scoreboard to add.
     */
    public void addScoreboard(ScoreboardContainer scoreboard) {
        this.scoreboards.put(scoreboard.getScoreboardId(), scoreboard);
    }

    /**
     * Removes a scoreboard from the holder.
     *
     * @param scoreboard The scoreboard to remove.
     */
    public void removeScoreboard(String scoreboard) {
        this.scoreboards.remove(scoreboard);
    }
}
