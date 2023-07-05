package lol.magix.breakingbedrock.mixin.serverlist;

import com.google.common.net.HostAndPort;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.socket.nio.NioDatagramChannel;
import lol.magix.breakingbedrock.BreakingBedrock;
import lol.magix.breakingbedrock.objects.ThreadFactoryBuilder;
import lol.magix.breakingbedrock.objects.absolute.NetworkConstants;
import lol.magix.breakingbedrock.objects.game.BedrockServerInfo;
import net.minecraft.GameVersion;
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

import java.net.InetSocketAddress;
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
        var socketAddress = new InetSocketAddress(address.getHost(), address.getPort());
        SERVER_PING_POOL.submit(() -> {
            this.server.label = Text.translatable("multiplayer.status.pinging");
            this.server.ping = -1L;
            this.server.playerListSummary = null;

            var currTime = System.currentTimeMillis();

            this.server.online = true;
            this.server.label = Text.of("motd");
            this.server.ping = System.currentTimeMillis() - currTime;
            this.server.playerCountLabel = createPlayerCountText(1, 3);
            this.server.version = Text.of("1.20.1");
            // I want to show the player count
            this.server.protocolVersion = NetworkConstants.PACKET_CODEC.getProtocolVersion();
        });
    }

    @Inject(method = "protocolVersionMatches", at = @At("HEAD"), cancellable = true)
    public void protocolVersionMatches(CallbackInfoReturnable<Boolean> cir) {
        if (!(this.server instanceof BedrockServerInfo)) {
            return;
        }

        cir.setReturnValue(true);
    }

    private static Text createPlayerCountText(int current, int max) {
        if(current >= 1000) {
            return Text.literal(Integer.toString(current)).formatted(Formatting.GRAY); // TODO: make this a toggleable setting, but still keep it a fun nod.
        }

        return Text.literal(Integer.toString(current)).append(Text.literal("/").formatted(Formatting.DARK_GRAY)).append(Integer.toString(max)).formatted(Formatting.GRAY);
    }
}
