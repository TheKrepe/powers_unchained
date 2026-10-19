package net.threetag.palladium.client.hub;

import com.google.gson.JsonObject;
import net.minecraft.util.GsonHelper;
import org.jetbrains.annotations.Nullable;

public record HubPlatformProject(String projectId, @Nullable String url, int downloadCount) {

    @Nullable
    public static HubPlatformProject fromJson(@Nullable JsonObject json) {
        if (json == null) {
            return null;
        }

        return new HubPlatformProject(
                GsonHelper.getAsString(json, "project_id", ""),
                HubAddon.getString(json, "url"),
                GsonHelper.getAsInt(json, "download_count", 0)
        );
    }
}
