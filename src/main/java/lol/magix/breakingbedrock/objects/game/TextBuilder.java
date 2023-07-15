package lol.magix.breakingbedrock.objects.game;

import lombok.RequiredArgsConstructor;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Style;
import net.minecraft.text.Text;

@RequiredArgsConstructor
public final class TextBuilder {
    /**
     * Creates generic text.
     *
     * @param text The text.
     * @return The text.
     */
    public static TextBuilder text(String text) {
        return new TextBuilder(text, Mode.TEXT);
    }

    /**
     * Creates translatable text.
     *
     * @param key The key.
     * @return The text.
     */
    public static TextBuilder translate(String key) {
        return new TextBuilder(key, Mode.TRANSLATABLE);
    }

    private Style style = Style.EMPTY;
    private final String text;
    private final Mode mode;

    /**
     * Applies color to the text.
     *
     * @param color The color.
     * @return The text builder.
     */
    public TextBuilder color(int color) {
        this.style = this.style.withColor(color);
        return this;
    }

    /**
     * Applies a click event handler to the text.
     *
     * @param action The action.
     * @return The text builder.
     */
    public TextBuilder action(ClickEvent action) {
        this.style = this.style.withClickEvent(action);
        return this;
    }

    /**
     * Applies a hover event handler to the text.
     *
     * @param action The action.
     * @return The text builder.
     */
    public TextBuilder action(HoverEvent action) {
        this.style = this.style.withHoverEvent(action);
        return this;
    }

    /**
     * @return The built text instance.
     */
    public Text get() {
        // Get the text instance.
        var text = switch (this.mode) {
            case TEXT -> Text.literal(this.text);
            case TRANSLATABLE -> Text.translatable(this.text);
        };

        // Apply the style.
        text = text.setStyle(this.style);

        // Return the text.
        return text;
    }

    enum Mode {
        TEXT,
        TRANSLATABLE
    }
}
