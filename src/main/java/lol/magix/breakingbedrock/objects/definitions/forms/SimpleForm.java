package lol.magix.breakingbedrock.objects.definitions.forms;

import lol.magix.breakingbedrock.objects.absolute.FormType;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

public final class SimpleForm implements IForm {
    @Getter private String title, content;

    @Getter private List<String> buttons;

    @Setter private transient int selected = -1;

    @Override
    public FormType getType() {
        return FormType.SIMPLE_FORM;
    }

    @Override
    public String serializeResponse() {
        return String.valueOf(this.selected);
    }
}
