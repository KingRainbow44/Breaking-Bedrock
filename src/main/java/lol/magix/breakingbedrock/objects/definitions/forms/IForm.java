package lol.magix.breakingbedrock.objects.definitions.forms;

import lol.magix.breakingbedrock.objects.absolute.FormType;

public interface IForm {
    /**
     * @return The form title.
     */
    String getTitle();

    /**
     * @return The form type.
     */
    FormType getType();

    /**
     * Serializes all the response data in the form.
     *
     * @return The serialized response.
     */
    String serializeResponse();
}
