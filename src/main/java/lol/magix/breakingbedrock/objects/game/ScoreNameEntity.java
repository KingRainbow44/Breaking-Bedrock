package lol.magix.breakingbedrock.objects.game;

import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

public interface ScoreNameEntity {
    /**
     * Sets the name which takes the place of a scoreboard score.
     *
     * @param name The name to set.
     */
    void setScoreName(Text name);

    /**
     * @return The name which takes the place of a scoreboard score.
     */
    @Nullable
    Text getScoreName();

    /**
     * @return True if the entity has a score name.
     */
    default boolean hasScoreName() {
        return this.getScoreName() != null;
    }
}
