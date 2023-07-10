package lol.magix.breakingbedrock.objects.definitions.forms;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;
import lol.magix.breakingbedrock.objects.absolute.FormType;
import lol.magix.breakingbedrock.objects.definitions.forms.elements.*;
import lol.magix.breakingbedrock.utils.EncodingUtils;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public final class CustomForm implements IForm {
    @SerializedName("type")
    private String rawType;

    private String title = "";
    private List<JsonObject> content = new ArrayList<>();

    private List<FormElement> formElements = null;

    /**
     * @return The contents of the form.
     */
    public List<FormElement> getContent() {
        if (this.formElements != null)
            return this.formElements;

        this.formElements = new ArrayList<>();
        for (var element : this.content) {
            System.out.println(element);
            this.formElements.add(switch (element.get("type").getAsString()) {
                default -> new FormElement() {
                    @Override
                    public FormType getType() {
                        return FormType.UNKNOWN;
                    }

                    @Override
                    public void serializeResponse(JsonArray object) {
                        object.add("");
                    }
                };
                case "label" -> EncodingUtils.jsonDecode(element.toString(), FormLabel.class);
                case "dropdown" -> EncodingUtils.jsonDecode(element.toString(), FormDropdown.class);
                case "slider" -> EncodingUtils.jsonDecode(element.toString(), FormSlider.class);
                case "input" -> EncodingUtils.jsonDecode(element.toString(), FormInput.class);
                case "step_slider" -> EncodingUtils.jsonDecode(element.toString(), FormStepSlider.class);
                case "toggle" -> EncodingUtils.jsonDecode(element.toString(), FormToggle.class);
            });
        }

        return this.formElements;
    }

    /**
     * @return The form type.
     */
    public FormType getType() {
        return FormType.from(rawType);
    }

    /**
     * Serializes all the response data in the form.
     *
     * @return The serialized response.
     */
    public String serializeResponse() {
        var responses = new JsonArray();
        for (var element : this.getContent())
            element.serializeResponse(responses);

        return EncodingUtils.jsonEncode(responses);
    }
}
