package net.threetag.palladium.client.hub;

import com.google.gson.JsonObject;
import net.minecraft.util.GsonHelper;
import org.jetbrains.annotations.Nullable;

public record HubPlatform(String projectId, @Nullable String url, int downloadCount) {

    @Nullable
    public static HubPlatform fromJson(@Nullable JsonObject json) {
        if (json == null) {
            return null;
        }

        return new HubPlatform(
                GsonHelper.getAsString(json, "project_id", ""),
                HubAddon.getString(json, "url"),
                GsonHelper.getAsInt(json, "download_count", 0)
        );
    }
}
