package lol.magix.breakingbedrock.mixin.serverlist;

import com.google.common.net.HostAndPort;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.socket.nio.NioDatagramChannel;
import lol.magix.breakingbedrock.BreakingBedrock;
import lol.magix.breakingbedrock.objects.ThreadFactoryBuilder;
import lol.magix.breakingbedrock.objects.absolute.NetworkConstants;
import lol.magix.breakingbedrock.objects.game.BedrockServerInfo;
import lol.magix.breakingbedrock.utils.NetworkUtils;
import net.minecraft.GameVersion;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerServerListWidget;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.logging.UncaughtExceptionLogger;
import org.cloudburstmc.netty.channel.raknet.RakChannelFactory;
import org.cloudburstmc.netty.channel.raknet.config.RakChannelOption;
import org.cloudburstmc.protocol.bedrock.BedrockClientSession;
import org.cloudburstmc.protocol.bedrock.BedrockPeer;
import org.cloudburstmc.protocol.bedrock.BedrockPong;
import org.cloudburstmc.protocol.bedrock.netty.initializer.BedrockChannelInitializer;
import org.cloudburstmc.protocol.bedrock.netty.initializer.BedrockClientInitializer;
import org.cloudburstmc.protocol.bedrock.packet.BedrockPacketHandler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;

@Mixin(MultiplayerServerListWidget.ServerEntry.class)
public abstract class MixinMultiplayerServerListWidget {
    private static final ThreadPoolExecutor SERVER_PING_POOL =
            new ScheduledThreadPoolExecutor(5, ThreadFactoryBuilder.base());
    private static final Text CANNOT_RESOLVE_TEXT = Text.translatable("multiplayer.status.cannot_resolve")
            .formatted(Formatting.DARK_RED);
    private static final Text CANNOT_CONNECT_TEXT = Text.translatable("multiplayer.status.cannot_connect")
            .formatted(Formatting.DARK_RED);

    @Shadow @Final private ServerInfo server;
    @Shadow public abstract void saveFile();

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Ljava/util/concurrent/ThreadPoolExecutor;submit(Ljava/lang/Runnable;)Ljava/util/concurrent/Future;"), cancellable = true)
    public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta, CallbackInfo ci) {
        if (!(this.server instanceof BedrockServerInfo)) {
            return;
        }
        ci.cancel();

        var address = HostAndPort.fromString(this.server.address).withDefaultPort(19132);
        SERVER_PING_POOL.submit(() -> {
            this.server.label = Text.translatable("multiplayer.status.pinging");
            this.server.ping = -1L;
            this.server.playerListSummary = null;

            // Ping the server.
            var pingStart = System.currentTimeMillis();
            BedrockPong pong; try {
                pong = NetworkUtils.pingServer(address);
            } catch (UnknownHostException ignored) {
                this.server.label = CANNOT_RESOLVE_TEXT;
                this.server.online = false;
                return;
            } catch (IOException exception) {
                this.server.label = CANNOT_CONNECT_TEXT;
                this.server.online = false;
                return;
            }

            // Update the server info.
            this.server.online = true;
            this.server.label = Text.of(pong.motd() + "\n" + pong.subMotd());
            this.server.ping = System.currentTimeMillis() - pingStart;
            this.server.playerCountLabel = createPlayerCountText(
                    pong.playerCount(), pong.maximumPlayerCount());
            this.server.version = Text.of(pong.version());
            this.server.protocolVersion = NetworkConstants.PACKET_CODEC.getProtocolVersion();

            MinecraftClient.getInstance().execute(this::saveFile);
        });
    }

    @Inject(method = "protocolVersionMatches", at = @At("HEAD"), cancellable = true)
    public void protocolVersionMatches(CallbackInfoReturnable<Boolean> cir) {
        if (!(this.server instanceof BedrockServerInfo)) {
            return;
        }

        cir.setReturnValue(true);
    }

    /**
     * Creates a player count text.
     *
     * @param current The current player count.
     * @param max The maximum player count.
     * @return The player count text.
     */
    private static Text createPlayerCountText(int current, int max) {
        if (current >= 1000) {
            return Text.literal(Integer.toString(current)).formatted(Formatting.GRAY);
        }

        return Text.literal(Integer.toString(current)).append(Text.literal("/").formatted(Formatting.DARK_GRAY)).append(Integer.toString(max)).formatted(Formatting.GRAY);
    }
}
