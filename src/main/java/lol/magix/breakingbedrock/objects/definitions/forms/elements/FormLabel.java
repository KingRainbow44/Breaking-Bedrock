package lol.magix.breakingbedrock.objects.definitions.forms.elements;

import com.google.gson.JsonArray;
import com.google.gson.JsonNull;
import lol.magix.breakingbedrock.objects.absolute.FormType;
import lol.magix.breakingbedrock.objects.definitions.forms.FormElement;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public final class FormLabel extends FormElement {
    private String text = "";

    @Override
    public FormType getType() {
        return FormType.LABEL;
    }

    @Override
    public void serializeResponse(JsonArray object) {
        object.add(JsonNull.INSTANCE);
    }
}
