package lol.magix.breakingbedrock.utils;

import net.minecraft.client.resource.language.TranslationStorage;
import net.minecraft.text.Text;
import net.minecraft.text.Texts;
import net.minecraft.util.Formatting;

import java.awt.*;
import java.util.ArrayList;

public interface TextUtils {
    /**
     * Removes Minecraft's formatting codes from the text.
     *
     * @param text The text to strip.
     * @return The stripped text.
     */
    static String strip(String text) {
        return text.replaceAll("§[0-9a-fk-or]", "");
    }

    /**
     * Converts a color code to a {@link Color} object.
     *
     * @param code The color code to convert.
     * @return The converted color.
     */
    static Formatting convert(String code) {
        // Check if the code starts with the formatting character.
        if (code.startsWith("§"))
            code = code.substring(1);

        return Formatting.byCode(code.charAt(0));
    }

    /**
     * Translates Bedrock text into Java text.
     *
     * @param text The text to translate.
     * @param parameters The parameters to use.
     * @return The translated text.
     */
    static Text translate(String text, Object... parameters) {
        Text value = null;
        var stripped = TextUtils.strip(text);
        var raw = stripped.replaceAll("%", "");

        System.out.println("Original: " + text);
        System.out.println("Stripped: " + stripped);
        System.out.println("Contains key: " + TranslationStorage
                .getInstance().hasTranslation(raw));

        // Check if the text is translatable.
        if (stripped.startsWith("%") && TranslationStorage
                .getInstance().hasTranslation(raw)) {
            // The text is translatable.
            value = Text.translatable(raw, parameters);
            // We assume that there's only one color code at the beginning of the text.
            if (text.startsWith("§")) {
                var code = text.substring(0, 2);
                value = value.copy().fillStyle(value.getStyle()
                        .withColor(TextUtils.convert(code)));
            }
        } else {
            // Divide each part of the text by the color code.
            var parts = stripped.split("§");
            // Create a new text container.
            var texts = new ArrayList<Text>();
            // Iterate through each part of the text.
            for (var part : parts) {
                // Check if the part is empty.
                if (part.isEmpty()) continue;
                // Create a new text object.
                var textObject = Text.of(TextUtils.strip(part));
                // Check if the text starts with a color code.
                if (part.startsWith("§")) {
                    // Get the color code.
                    var code = part.substring(0, 1);
                    // Set the color of the text.
                    textObject = textObject.copy().fillStyle(textObject.getStyle()
                            .withColor(TextUtils.convert(code)));
                }
                // Append the text to the container.
                texts.add(textObject);
            }

            // Create a new text container.
            value = Texts.join(texts, Text.of(""));
        }

        return value;
    }
}
