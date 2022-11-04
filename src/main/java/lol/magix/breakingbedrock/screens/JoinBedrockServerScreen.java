package lol.magix.breakingbedrock.screens;

import lol.magix.breakingbedrock.network.BedrockNetworkClient;
import lol.magix.breakingbedrock.objects.ConnectionDetails;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.CheckboxWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

@Environment(EnvType.CLIENT)
public final class JoinBedrockServerScreen extends Screen {
    private final Screen parent;

    private ButtonWidget joinServerButton;
    private TextFieldWidget addressField;
    private TextFieldWidget portField;
    private CheckboxWidget onlineCheckbox;

    public JoinBedrockServerScreen(Screen parent) {
        super(Text.of("Join Bedrock Server"));
        this.parent = parent;
    }

    /**
     * Called when the join server button is pressed.
     * @param button The button.
     */
    private void joinServer(ButtonWidget button) {
        // Get the address, port, and authentication method.
        var address = this.addressField.getText();
        if (address.isEmpty()) return;

        var portField = this.portField.getText();
        int port; try {
            port = Integer.parseInt(portField);
            if (port < 0 || port > 65535) throw new NumberFormatException("Port must be between 0 and 65535.");
        } catch (NumberFormatException exception) {
            port = 19132;
        }

        var online = this.onlineCheckbox.isChecked();

        // Connect to the server.
        var connectTo = new ConnectionDetails(address, port, online);
        BedrockNetworkClient.getInstance().connect(connectTo);
    }

    /**
     * Called when the address input text changes.
     */
    private void modifyAddress() {
        var addressText = this.addressField.getText();
        var portText = this.portField.getText();

        var active = !addressText.isEmpty();
        if (portText.isEmpty()) active = false;
        if (portText.length() > 5) active = false; else try {
            var port = Integer.parseInt(portText);
            if (port < 0 || port > 65535) active = false;
        } catch (NumberFormatException exception) {
            active = false;
        }

        this.joinServerButton.active = active;
    }

    @Override
    protected void init() {
        // Check if running on client.
        if (this.client == null)
            return;

        // Configure the screen.
        this.client.keyboard.setRepeatEvents(true);
        this.joinServerButton = new ButtonWidget(this.width / 2 - 102, this.height / 4 + 100 + 12,
                204, 20, Text.translatable("selectServer.select"), this::joinServer);
        this.addressField = new TextFieldWidget(this.textRenderer, this.width / 2 - 100, (this.height / 4) + 16,
                200, 20, Text.of("Enter Address"));
        this.portField = new TextFieldWidget(this.textRenderer, this.width / 2 - 100, (this.height / 4) + 52,
                200, 20, Text.of("Enter Port"));
        this.onlineCheckbox = new CheckboxWidget(this.width / 2 - 100, (this.height / 4) + 80,
                200, 20, Text.of("Use XBox Authentication?"), true);

        var cancelButton = new ButtonWidget(this.width / 2 - 102, this.height / 4 + 100 + 36,
                204, 20, ScreenTexts.CANCEL, button -> this.close());

        // Modify data for each widget.
        this.addressField.setMaxLength(128);
        this.addressField.setTextFieldFocused(true);
        this.addressField.setText("127.0.0.1");
        this.addressField.setChangedListener(text -> this.modifyAddress());

        this.portField.setMaxLength(6);
        this.portField.setTextFieldFocused(false);
        this.portField.setText("19132");
        this.portField.setChangedListener(text -> this.modifyAddress());

        // Draw the widgets to the screen.
        this.addDrawableChild(this.joinServerButton);
        this.addDrawableChild(this.addressField);
        this.addDrawableChild(this.portField);
        this.addDrawableChild(cancelButton);
        // Focus on the address field.
        this.setInitialFocus(this.addressField);
        this.modifyAddress();
    }

    @Override
    public void tick() {
        // Tick input fields.
        this.addressField.tick();
        this.portField.tick();
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBackground(matrices); // Render the background.

        // Draw text with shadows.
        JoinBedrockServerScreen.drawCenteredText(matrices, this.textRenderer, this.title,
                this.width / 2, 20, 16777215);
        JoinBedrockServerScreen.drawTextWithShadow(matrices, this.textRenderer, Text.of("Server Address and Port"),
                this.width / 2 - 100, this.height / 4, 10526880);

        // Draw widgets to the screen.
        this.addressField.render(matrices, mouseX, mouseY, delta);
        this.portField.render(matrices, mouseX, mouseY, delta);
        this.onlineCheckbox.render(matrices, mouseX, mouseY, delta);
        super.render(matrices, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        this.addressField.mouseClicked(mouseX, mouseY, button);
        this.portField.mouseClicked(mouseX, mouseY, button);
        this.onlineCheckbox.mouseClicked(mouseX, mouseY, button);
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (this.client == null)
            return false;

        // Check if the Enter key was pressed.
        if ((this.addressField.isFocused() || this.portField.isFocused())
                && (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER)) {
            // Press and reply with audio feedback.
            this.joinServerButton.onPress();
            this.joinServerButton.playDownSound(this.client.getSoundManager());
            return true;
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void resize(MinecraftClient client, int width, int height) {
        // Save the existing values.
        var addressText = this.addressField.getText();
        var portText = this.portField.getText();
        // Re-create each element.
        this.init(client, width, height);
        // Restore the values.
        this.addressField.setText(addressText);
        this.portField.setText(portText);
    }

    @Override
    public void close() {
        if (this.client == null)
            return;

        this.client.setScreen(this.parent);
    }

    @Override
    public void removed() {
        if (this.client == null)
            return;

        this.client.keyboard.setRepeatEvents(false);
        this.client.options.write();
    }
}
