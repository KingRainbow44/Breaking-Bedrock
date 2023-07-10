package lol.magix.breakingbedrock.objects.definitions.forms.elements;

import com.google.gson.JsonArray;
import com.google.gson.annotations.SerializedName;
import lol.magix.breakingbedrock.objects.absolute.FormType;
import lol.magix.breakingbedrock.objects.definitions.forms.FormElement;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

public final class FormStepSlider extends FormElement {
    @Getter private String text;
    @Getter private List<String> steps;

    @SerializedName("default")
    @Getter private int defaultValue = 0;

    @Setter private transient String step = "";

    @Override
    public FormType getType() {
        return FormType.STEP_SLIDER;
    }

    @Override
    public void serializeResponse(JsonArray object) {
        object.add(this.steps.indexOf(
                !this.step.isEmpty() ? this.step :
                        this.steps.get(this.defaultValue)
        ));
    }
}
