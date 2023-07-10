package lol.magix.breakingbedrock.objects.definitions.forms;

import com.google.gson.JsonArray;
import lol.magix.breakingbedrock.objects.absolute.FormType;
import lombok.Data;

@Data
public abstract class FormElement {
    /**
     * @return The form type.
     */
    public abstract FormType getType();

    /**
     * Serializes the response data for this element.
     *
     * @param object The response object.
     */
    public abstract void serializeResponse(JsonArray object);
}
