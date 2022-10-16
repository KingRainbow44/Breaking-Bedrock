package lol.magix.breakingbedrock.objects.absolute;

import com.nukkitx.protocol.bedrock.BedrockPacket;
import net.minecraft.network.Packet;

/**
 * Types of translatable packets.
 */
public enum PacketType {
    BEDROCK, JAVA;

    /**
     * Checks if the packet type matches the packet.
     * @param type The packet type.
     * @param packet The packet.
     * @return True if the packet type matches the packet.
     */
    public static boolean matches(PacketType type, Class<?> packet) {
        return switch (type) {
            case BEDROCK -> BedrockPacket.class.isAssignableFrom(packet);
            case JAVA -> Packet.class.isAssignableFrom(packet);
        };
    }
}
