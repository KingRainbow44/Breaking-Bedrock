package lol.magix.breakingbedrock.objects.definitions.forms.elements;

import com.google.gson.JsonArray;
import com.google.gson.annotations.SerializedName;
import lol.magix.breakingbedrock.objects.absolute.FormType;
import lol.magix.breakingbedrock.objects.definitions.forms.FormElement;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter @Setter
public final class FormDropdown extends FormElement {
    private String text = "";
    private List<String> options = new ArrayList<>();

    @SerializedName("default")
    private Object defaultOption = null;

    @Setter private transient String option = "";

    @Override
    public FormType getType() {
        return FormType.DROPDOWN;
    }

    @Override
    public void serializeResponse(JsonArray object) {
        object.add(this.options.indexOf(this.option));
    }
}
