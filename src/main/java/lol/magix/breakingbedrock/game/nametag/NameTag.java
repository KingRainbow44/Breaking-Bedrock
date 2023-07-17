package lol.magix.breakingbedrock.game.nametag;

import lol.magix.breakingbedrock.utils.TextUtils;
import lombok.Getter;
import lombok.ToString;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;

@ToString(exclude = "entity")
public final class NameTag {
    private static final float OFFSET = 0.125f;

    @Getter private final Entity entity;
    @Getter private Vec3d position;

    @Getter private String rawNameTag;

    private List<NameTagLine> lines = new ArrayList<>();
    @Getter private boolean displayed = false;

    public NameTag(Entity entity) {
        this.entity = entity;
        this.position = entity.getPos();

        this.rawNameTag = entity.getName().getString();
    }

    /**
     * Sets the name tag of the entity.
     *
     * @param nameTag The name tag to set.
     */
    public void setNameTag(String nameTag) {
        this.rawNameTag = nameTag;

        // Check if content exists.
        if (!this.lines.isEmpty()) {
            this.lines.forEach(NameTagLine::hide);
            this.lines.clear();
        }

        // Format the name tag.
        var split = nameTag.split("\n");
        for (var line : split) {
            var tagLine = new NameTagLine(
                    this, TextUtils.translate(line));
            tagLine.setPosition(this.getPosition());

            this.lines.add(tagLine);
        }

        this.show();
    }

    /**
     * Updates the position of the name tag.
     * This is called when the entity moves.
     *
     * @param source The source to update the position from.
     */
    public void setPosition(Vec3d source) {
        this.position = source;
        this.reorganize();
    }

    /**
     * @return The position of the name tag with the offset applied.
     */
    public Vec3d getOffsetPosition() {
        return this.getPosition().add(0, OFFSET, 0);
    }

    /**
     * Sorts and positions the name tag's content.
     */
    public void reorganize() {
        var position = this.getOffsetPosition();
        var yLevel = position.getY();

        var endLine = this.lines.size() - 1;
        for (var i = endLine; i > 0; i--) {
            var line = this.lines.get(i);
            yLevel += 0.23f; // This is the height of a line.
            yLevel += 0.02f; // This is the spacing between lines.

            line.setPosition(new Vec3d(
                    position.getX(), yLevel, position.getZ()));
        }
    }

    /**
     * Shows this name tag.
     */
    public void show() {
        // Check if the name tag is being shown.
        if (this.displayed) this.hide();

        // Sort the name tag's content.
        this.reorganize();

        // Show each line.
        var world = MinecraftClient.getInstance().world;
        this.lines.forEach(line -> line.show(world));

        this.displayed = true;
    }

    /**
     * Hides this name tag.
     */
    public void hide() {
        // Check if the name tag is being shown.
        if (!this.displayed) return;

        // Hide each line.
        this.lines.forEach(NameTagLine::hide);

        this.displayed = false;
    }
}
