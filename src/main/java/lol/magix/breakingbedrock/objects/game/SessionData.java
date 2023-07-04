package lol.magix.breakingbedrock.objects.game;

import lombok.Data;
import org.cloudburstmc.protocol.bedrock.data.AuthoritativeMovementMode;

import java.util.UUID;

@Data
public final class SessionData {
    /* Entity data. */
    private int runtimeId = -1;
    private boolean spawned = false;
    private boolean jumping = false;

    /* Player data. */
    private String displayName = "";
    private String xuid = "";
    private UUID identity = null;

    /* Player flags. */
    private boolean isInitialized = false;
    private AuthoritativeMovementMode movementMode = AuthoritativeMovementMode.CLIENT;
}
