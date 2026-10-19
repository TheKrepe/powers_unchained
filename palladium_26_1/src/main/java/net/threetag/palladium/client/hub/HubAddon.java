package net.threetag.palladium.client.hub;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.util.GsonHelper;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public record HubAddon(
        int id,
        @Nullable String name,
        @Nullable String author,
        @Nullable String description,
        @Nullable String previewImage,
        @Nullable String iconUrl,
        List<String> versions,
        int totalDownloadCount,
        List<HubTag> tags,
        @Nullable HubPlatform modrinth,
        @Nullable HubPlatform curseforge
) {

    public static HubAddon fromJson(JsonObject json) {
        List<String> versions = new ArrayList<>();
        JsonArray versionArray = GsonHelper.getAsJsonArray(json, "versions", new JsonArray());
        for (JsonElement element : versionArray) {
            versions.add(element.getAsString());
        }

        List<HubTag> tags = new ArrayList<>();
        JsonArray tagArray = GsonHelper.getAsJsonArray(json, "tags", new JsonArray());
        for (JsonElement element : tagArray) {
            if (element.isJsonObject()) {
                tags.add(HubTag.fromJson(element.getAsJsonObject()));
            }
        }

        HubPlatform modrinth = null;
        HubPlatform curseforge = null;
        if (json.has("platforms") && json.get("platforms").isJsonObject()) {
            JsonObject platforms = json.getAsJsonObject("platforms");
            modrinth = HubPlatform.fromJson(asObjectOrNull(platforms.get("modrinth")));
            curseforge = HubPlatform.fromJson(asObjectOrNull(platforms.get("curseforge")));
        }

        return new HubAddon(
                GsonHelper.getAsInt(json, "id", 0),
                getString(json, "name"),
                getString(json, "author"),
                getString(json, "description"),
                getString(json, "preview_image"),
                getString(json, "icon_url"),
                versions,
                GsonHelper.getAsInt(json, "total_download_count", 0),
                tags,
                modrinth,
                curseforge
        );
    }

    @Nullable
    private static JsonObject asObjectOrNull(@Nullable JsonElement element) {
        return element != null && element.isJsonObject() ? element.getAsJsonObject() : null;
    }

    @Nullable
    static String getString(JsonObject json, String key) {
        JsonElement element = json.get(key);
        return element != null && element.isJsonPrimitive() ? element.getAsString() : null;
    }

    public String displayName() {
        return this.name != null && !this.name.isBlank() ? this.name : "Addon #" + this.id;
    }
}
