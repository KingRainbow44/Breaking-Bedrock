package lol.magix.breakingbedrock.objects.definitions.forms.elements;

import com.google.gson.JsonArray;
import com.google.gson.annotations.SerializedName;
import lol.magix.breakingbedrock.objects.absolute.FormType;
import lol.magix.breakingbedrock.objects.definitions.forms.FormElement;
import lombok.Getter;
import lombok.Setter;

public final class FormInput extends FormElement {
    @Getter private String text;

    @Getter private String placeholder;

    @SerializedName("default")
    @Getter private String defaultValue = "";

    @Setter private transient String value = "";

    @Override
    public FormType getType() {
        return FormType.INPUT;
    }

    @Override
    public void serializeResponse(JsonArray object) {
        object.add(this.value);
    }
}
