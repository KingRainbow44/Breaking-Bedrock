package lol.magix.breakingbedrock.objects.definitions.forms;

import lol.magix.breakingbedrock.objects.absolute.FormType;
import lombok.Getter;
import lombok.Setter;

public final class ModalForm implements IForm {
    @Getter private String title, content;

    @Getter private String button1, button2;

    @Setter private transient boolean response = false;

    @Override
    public FormType getType() {
        return FormType.MODAL_FORM;
    }

    @Override
    public String serializeResponse() {
        return String.valueOf(this.response);
    }
}
