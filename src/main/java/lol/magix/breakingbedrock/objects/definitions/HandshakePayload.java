package lol.magix.breakingbedrock.objects.definitions;

import lombok.Getter;

@Getter
public final class HandshakePayload {
    private String salt; // Base64-encoded salt.
}
