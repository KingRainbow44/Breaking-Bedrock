package lol.magix.breakingbedrock.objects.definitions.forms.elements;

import com.google.gson.JsonArray;
import com.google.gson.annotations.SerializedName;
import lol.magix.breakingbedrock.objects.absolute.FormType;
import lol.magix.breakingbedrock.objects.definitions.forms.FormElement;
import lombok.Getter;
import lombok.Setter;

public final class FormToggle extends FormElement {
    @Getter private String text;

    @SerializedName("default")
    @Getter private boolean defaultValue = false;

    @Setter private transient boolean value = false;

    @Override
    public FormType getType() {
        return FormType.TOGGLE;
    }

    @Override
    public void serializeResponse(JsonArray object) {
        object.add(this.value);
    }
}
