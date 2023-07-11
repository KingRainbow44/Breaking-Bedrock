package lol.magix.breakingbedrock.objects.game;

import lombok.Data;
import org.cloudburstmc.protocol.bedrock.data.AuthoritativeMovementMode;

import java.util.*;

@Data
public final class SessionData {
    /* Server data. */
    private final Map<String, Integer> id2Runtime = new HashMap<>();

    /* Entity data. */
    private int runtimeId = -1;
    private boolean spawned = false;
    private boolean jumping = false;

    /* Player data. */
    private List<String> chain = new ArrayList<>();

    private String displayName = "";
    private String xuid = "";
    private UUID identity = null;

    private int viewDistance = 4;

    /* Player flags. */
    private boolean isInitialized = false;
    private boolean isReady = false;
    private AuthoritativeMovementMode movementMode = AuthoritativeMovementMode.CLIENT;
}
