package lol.magix.breakingbedrock.objects.game;

import lol.magix.breakingbedrock.translators.pack.ResourcePackInfo;
import lombok.Data;
import org.cloudburstmc.protocol.bedrock.data.AuthoritativeMovementMode;
import org.cloudburstmc.protocol.bedrock.data.PlayerPermission;
import org.cloudburstmc.protocol.bedrock.packet.AddPlayerPacket;

import java.util.*;

@Data
public final class SessionData {
    /* Server data. */
    private final Map<String, Integer> id2Runtime = new HashMap<>();
    private final List<ResourcePackInfo> activePacks = new ArrayList<>();
    private final List<AddPlayerPacket> pendingPlayers = new ArrayList<>();

    private String serverBrand = "Bedrock";
    private boolean packsDownloaded = true;

    /* Entity data. */
    private int runtimeId = -1;
    private boolean spawned = false;
    private boolean jumping = false;

    /* Player data. */
    private List<String> chain = new ArrayList<>();
    private PlayerPermission permission = PlayerPermission.MEMBER;

    private String displayName = "";
    private String xuid = "";
    private UUID identity = null;

    private int viewDistance = 4;

    /* Player flags. */
    private boolean isInitialized = false;
    private boolean isReady = false;
    private AuthoritativeMovementMode movementMode = AuthoritativeMovementMode.CLIENT;

    private boolean logPackets = false;
}
