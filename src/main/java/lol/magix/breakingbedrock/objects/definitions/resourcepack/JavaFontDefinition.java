package lol.magix.breakingbedrock.objects.definitions.resourcepack;

import lombok.Data;
import lombok.experimental.Accessors;
import org.apache.commons.text.StringEscapeUtils;

import java.util.ArrayList;
import java.util.List;

@Data @Accessors(chain = true, fluent = true)
public final class JavaFontDefinition {
    /**
     * @return A new instance of {@link JavaFontDefinition}.
     */
    public static JavaFontDefinition b() {
        return new JavaFontDefinition();
    }

    private String type = "bitmap";
    private String file = "custom-font/";
    private int height = 16;
    private int ascent = 16;
    private List<String> chars = new ArrayList<>();

    private JavaFontDefinition() {

    }

    /**
     * Sets the path to the font file.
     * This should be a PNG.
     *
     * @param name The path to the font file.
     * @return The current instance of {@link JavaFontDefinition}.
     */
    public JavaFontDefinition name(String name) {
        // Check for the file extension.
        if (!name.endsWith(".png")) name += ".png";

        this.file += name;
        return this;
    }

    /**
     * Creates a simple character map.
     * This generates a grid of 16x16 unicode characters.
     *
     * @param unicodePrefix The unicode prefix.
     * @return The current instance of {@link JavaFontDefinition}.
     */
    public JavaFontDefinition simple(String unicodePrefix) {
        var format = "\\u" + unicodePrefix;

        for (var y = 0; y < 16; y++) {
            var row = new StringBuilder();
            for (var x = 0; x < 16; x++) {
                var unicode = format + Integer.toHexString(x + y * 16);
                // Check if we are missing a character.
                if (unicode.length() < 6) unicode = format +
                        "0" + Integer.toHexString(x + y * 16);

                row.append(StringEscapeUtils.unescapeJava(unicode));
            }

            this.chars.add(row.toString());
        }

        return this;
    }

    /**
     * Creates a simple character map.
     * This generates one row of 16 unicode characters.
     *
     * @param unicodePrefix The unicode prefix.
     * @param offset The offset.
     * @return The current instance of {@link JavaFontDefinition}.
     */
    public JavaFontDefinition offset(String unicodePrefix, int offset) {
        var format = "\\u" + unicodePrefix;

        var row = new StringBuilder();
        for (var i = 0; i < 16; i++) {
            var unicode = format + Integer.toHexString(i + offset * 16);
            // Check if we are missing a character.
            if (unicode.length() < 6) unicode = format +
                    "0" + Integer.toHexString(i + offset);

            row.append(StringEscapeUtils.unescapeJava(unicode));
        }

        this.chars.add(row.toString());
        return this;
    }
}
