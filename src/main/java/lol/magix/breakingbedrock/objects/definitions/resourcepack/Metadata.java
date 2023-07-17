package lol.magix.breakingbedrock.objects.definitions.resourcepack;

import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;
import lombok.Builder;

import java.util.List;

@Builder
public final class Metadata {
    private final Pack pack;

    @Builder
    public static class Pack {
        @SerializedName("pack_format")
        private final int packFormat;
        private final List<JsonObject> description;
    }
}
