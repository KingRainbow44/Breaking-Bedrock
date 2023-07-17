package lol.magix.breakingbedrock;

import com.google.gson.Gson;
import lol.magix.breakingbedrock.objects.definitions.resourcepack.JavaFontDefinition;

public final class FontTest {
    public static void main(String[] args) {
        var definition = JavaFontDefinition.b()
                .file("test.png")
                .height(16)
                .ascent(16)
                .simple("E0");

        System.out.println(new Gson().toJson(definition));
    }
}
