package lol.magix.breakingbedrock.translators;

import com.google.gson.JsonObject;
import lol.magix.breakingbedrock.network.BedrockNetworkClient;
import lol.magix.breakingbedrock.objects.absolute.FormType;
import lol.magix.breakingbedrock.objects.definitions.forms.*;
import lol.magix.breakingbedrock.objects.definitions.forms.elements.FormDropdown;
import lol.magix.breakingbedrock.objects.definitions.forms.elements.FormLabel;
import lol.magix.breakingbedrock.utils.EncodingUtils;
import lol.magix.breakingbedrock.utils.TextUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import org.cloudburstmc.protocol.bedrock.packet.ModalFormRequestPacket;
import org.cloudburstmc.protocol.bedrock.packet.ModalFormResponsePacket;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

public final class FormTranslator {
    private static final Map<Integer, IForm> FORMS = new HashMap<>();
    private static final Map<Integer, Consumer<Integer>> OPTIONS = new HashMap<>();

    /**
     * Submits the form.
     *
     * @param formId The form ID.
     */
    public static void submitForm(int formId) {
        var form = FORMS.get(formId);
        if (form == null) return;

        // Serialize the form.
        var responsePacket = new ModalFormResponsePacket();
        responsePacket.setFormId(formId);
        responsePacket.setFormData(form.serializeResponse());
        responsePacket.setCancelReason(Optional.empty());

        // Send the packet.
        var client = BedrockNetworkClient.getInstance();
        if (client != null && client.isConnected())
            client.sendPacket(responsePacket);

        // Unregister the form.
        FORMS.remove(formId);
        OPTIONS.remove(formId);
    }

    /**
     * Runs a registered form option.
     *
     * @param formId The ID of the form.
     * @param option The form option.
     */
    public static void runOption(int formId, int option) {
        var consumer = OPTIONS.get(formId);
        if (consumer == null) return;

        consumer.accept(option);
    }

    /**
     * Translates a form to Java.
     *
     * @param packet The packet to translate.
     */
    public static void translate(ModalFormRequestPacket packet) {
        var formData = EncodingUtils.jsonDecode(
                packet.getFormData(), JsonObject.class);
        var formType = FormType.from(formData.get("type").getAsString());

        FormTranslator.translate(switch (formType) {
            default -> throw new RuntimeException("Invalid form type " + formType + "!");
            case SIMPLE_FORM -> EncodingUtils.jsonDecode(packet.getFormData(), SimpleForm.class);
            case MODAL_FORM -> EncodingUtils.jsonDecode(packet.getFormData(), ModalForm.class);
            case CUSTOM_FORM -> EncodingUtils.jsonDecode(packet.getFormData(), CustomForm.class);
        }, packet.getFormId());
    }

    /**
     * Translates a form to Java.
     *
     * @param form  The form to translate.
     * @param formId The form ID.
     */
    public static void translate(IForm form, int formId) {
        var client = MinecraftClient.getInstance();
        var player = client.player;
        if (player == null) return;

        // Register the form.
        FORMS.put(formId, form);

        switch (form.getType()) {
            default -> throw new RuntimeException("Invalid form type " + form.getType() + "!");
            case SIMPLE_FORM -> {
                var simpleForm = (SimpleForm) form;

                // This is a form with: title, description, and buttons.
                player.sendMessage(TextUtils.translate(form.getTitle()));
                player.sendMessage(TextUtils.translate(simpleForm.getContent()));

                // Send the buttons.
                for (var i = 0; i < simpleForm.getButtons().size(); i++) {
                    var button = simpleForm.getButtons().get(i);
                    var text = button.getText();

                    player.sendMessage(TextUtils.translate(text).setStyle(Style.EMPTY
                            .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                                    "/formoption " + formId + " " + i))
                            .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                    Text.of("Click to select this option."))))
                    );
                }

                // Register the options.
                OPTIONS.put(formId, option -> {
                    simpleForm.setSelected(option);
                    FormTranslator.submitForm(formId);
                });
            }
            case MODAL_FORM -> {
                var modalForm = (ModalForm) form;

                // This is a Yes or No form.
                player.sendMessage(TextUtils.translate(form.getTitle()));
                player.sendMessage(TextUtils.translate(modalForm.getContent()));
                // Send the two buttons.
                player.sendMessage(TextUtils.translate(modalForm.getButton1()).setStyle(Style.EMPTY
                        .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                                "/formoption " + formId + " 0"))));
                player.sendMessage(TextUtils.translate(modalForm.getButton2()).setStyle(Style.EMPTY
                        .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                                "/formoption " + formId + " 1"))));

                // Register the options.
                OPTIONS.put(formId, option -> {
                    modalForm.setResponse(option == 0);
                    FormTranslator.submitForm(formId);
                });
            }
            case CUSTOM_FORM -> {
                var customForm = (CustomForm) form;

                // This is a form with a variety of elements.
                player.sendMessage(TextUtils.translate(form.getTitle()));
                // Translate each element into a chat message.
                for (var element : customForm.getContent()) {
                    FormTranslator.translate(formId, player, element);
                }
            }
        }

        // Send the submit button.
        player.sendMessage(Text.literal("Submit").setStyle(Style.EMPTY
                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                        "/formsubmit " + formId))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                        Text.of("Click to submit the form.\n" +
                                "You can only submit once.")))
                )
        );
    }

    /**
     * Translates a form element to Java.
     *
     * @param formId The form ID.
     * @param player The player receiving the form.
     * @param element The form element to translate.
     */
    public static void translate(
            int formId, ClientPlayerEntity player, FormElement element
    ) {
        switch (element.getType()) {
            case LABEL -> {
                var label = (FormLabel) element;
                player.sendMessage(TextUtils.translate(label.getText()));
            }
            case DROPDOWN -> {
                var dropdown = (FormDropdown) element;

                player.sendMessage(TextUtils.translate(dropdown.getText()));
                for (var i = 0; i < dropdown.getOptions().size(); i++) {
                    var option = dropdown.getOptions().get(i);

                    // Send the option to the player.
                    player.sendMessage(TextUtils.translate(option)
                            .copy().setStyle(Style.EMPTY
                                    .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                                            String.format("/formoption %d %d", formId, i)))
                            )
                    );
                }

                // Register the option.
                OPTIONS.put(formId, selected -> {
                    var option = dropdown.getOptions().get(selected);
                    dropdown.setOption(option);
                });
            }
        }
    }
}
