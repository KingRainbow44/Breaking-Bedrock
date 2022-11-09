package lol.magix.breakingbedrock.objects.game;

import lombok.Data;

import java.util.UUID;

@Data
public final class SessionData {
    /* Entity data. */
    private int runtimeId = -1;

    /* Player data. */
    private String displayName = "";
    private String xuid = "";
    private UUID identity = null;

    /* Login flags. */
    private boolean isInitialized = false;
}
