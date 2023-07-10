package lol.magix.breakingbedrock.objects.definitions.forms.elements;

import com.google.gson.JsonArray;
import com.google.gson.annotations.SerializedName;
import lol.magix.breakingbedrock.objects.absolute.FormType;
import lol.magix.breakingbedrock.objects.definitions.forms.FormElement;
import lombok.Getter;
import lombok.Setter;

public final class FormSlider extends FormElement {
    @Getter private String text;
    @Getter private int min, max;

    @SerializedName("default")
    @Getter private int defaultValue = -1;

    @Setter private transient float value = this.min;

    @Override
    public FormType getType() {
        return FormType.SLIDER;
    }

    @Override
    public void serializeResponse(JsonArray object) {
        object.add(this.value);
    }
}
