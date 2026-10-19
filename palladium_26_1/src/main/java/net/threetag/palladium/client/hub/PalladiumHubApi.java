package net.threetag.palladium.client.hub;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.SharedConstants;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.Util;
import net.neoforged.fml.loading.FMLEnvironment;
import net.threetag.palladium.Palladium;
import org.jetbrains.annotations.Nullable;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class PalladiumHubApi {

    private static final String BASE_URL = "https://palladium.threetag.net/api";
    private static final int PER_PAGE = 50;
    private static final int MAX_PAGES = 20;

    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_2)
            .build();

    private PalladiumHubApi() {
    }

    public static CompletableFuture<List<HubAddon>> fetchAddons(@Nullable String version, AddonSort sort) {
        return CompletableFuture.supplyAsync(() -> {
            List<HubAddon> addons = new ArrayList<>();
            int page = 1;
            int lastPage = 1;

            do {
                JsonObject response = requestJson(buildAddonUrl(version, sort, page));
                if (response == null) {
                    break;
                }

                JsonArray data = GsonHelper.getAsJsonArray(response, "data", new JsonArray());
                for (JsonElement element : data) {
                    if (element.isJsonObject()) {
                        addons.add(HubAddon.fromJson(element.getAsJsonObject()));
                    }
                }

                if (response.has("meta") && response.get("meta").isJsonObject()) {
                    lastPage = GsonHelper.getAsInt(response.getAsJsonObject("meta"), "last_page", page);
                }

                page++;
            } while (page <= lastPage && page <= MAX_PAGES);

            return addons;
        }, Util.backgroundExecutor());
    }

    private static String buildAddonUrl(@Nullable String version, AddonSort sort, int page) {
        StringBuilder url = new StringBuilder(BASE_URL);
        url.append("/v1/addons?sort=").append(sort.getQuery());
        url.append("&per_page=").append(PER_PAGE);
        url.append("&page=").append(page);

        if (version != null && !version.isBlank()) {
            url.append("&version=").append(URLEncoder.encode(version, StandardCharsets.UTF_8));
        }

        return url.toString();
    }

    @Nullable
    private static JsonObject requestJson(String url) {
        try {
            HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                    .header("Accept", "application/json")
                    .GET()
                    .build();

            HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                Palladium.LOGGER.warn("PalladiumHub request to {} failed with status {}", url, response.statusCode());
                return null;
            }

            return JsonParser.parseString(response.body()).getAsJsonObject();
        } catch (Exception e) {
            Palladium.LOGGER.warn("Failed to reach PalladiumHub at {}: {}", url, e.getMessage());
            return null;
        }
    }

    public static String currentVersionLabel() {
        if (!FMLEnvironment.isProduction()) {
            return null;
        }

        String name = SharedConstants.getCurrentVersion().name();
        String[] parts = name.split("\\.");
        return parts.length >= 2 ? parts[0] + "." + parts[1] : name;
    }
}
