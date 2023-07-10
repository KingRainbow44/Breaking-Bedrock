package lol.magix.breakingbedrock.objects.absolute;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Getter
@RequiredArgsConstructor
public enum FormType {
    /* Form Types */
    SIMPLE_FORM("form"),
    CUSTOM_FORM("custom_form"),
    MODAL_FORM("modal"),

    /* Element Types */
    /* These are for custom forms. */
    UNKNOWN("unknown"),
    LABEL("label"),
    TOGGLE("toggle"),
    SLIDER("slider"),
    STEP_SLIDER("step_slider"),
    DROPDOWN("dropdown"),
    INPUT("input"),

    ;

    final String serialized;

    private static final Map<String, FormType> SERIALIZED = new HashMap<>();

    static {
        for (var value : FormType.values())
            SERIALIZED.put(value.getSerialized(), value);
    }

    /**
     * Fetches a form element from a serialized string.
     *
     * @param serialized The serialized string.
     * @return The form element.
     */
    public static FormType from(String serialized) {
        return SERIALIZED.get(serialized);
    }

    public static final class Adapter extends TypeAdapter<FormType> {
        @Override
        public void write(JsonWriter out, FormType value) throws IOException {
            out.value(value.getSerialized());
        }

        @Override
        public FormType read(JsonReader in) throws IOException {
            return FormType.from(in.nextString());
        }
    }
}
