package net.threetag.palladium.client.renderer.entity.skin;

import com.google.common.collect.ImmutableMultimap;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.Util;
import net.minecraft.world.entity.player.PlayerModelType;
import net.threetag.palladium.Palladium;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

public class PlayerSkinFetcher {

    private static final Map<String, GameProfile> CACHED_NAME_PROFILES = new ConcurrentHashMap<>();
    private static final Map<String, PlayerSkinInfo> CACHED_NAME_SKINS = new ConcurrentHashMap<>();
    private static final Pattern UUID_PATTERN = Pattern.compile("(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})");

    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_2)
            .build();

    @Nullable
    public static Identifier getOrLoadPlayerSkin(String username) {
        var skinInfo = getPlayerSkinInfo(username);

        if (skinInfo.hasFailed() || skinInfo.isLoading()) {
            return null;
        } else {
            return skinInfo.getSkin().body().id();
        }
    }

    @Nullable
    public static PlayerModelType getOrLoadModelType(String username) {
        var skinInfo = getPlayerSkinInfo(username);

        if (skinInfo.hasFailed() || skinInfo.isLoading()) {
            return null;
        } else {
            return skinInfo.getSkin().model();
        }
    }

    @Nullable
    public static PlayerSkinInfo getLoadedPlayerSkinInfo(final @NotNull String username) {
        var skinInfo = getPlayerSkinInfo(username);

        if (skinInfo.hasFailed() || skinInfo.isLoading()) {
            return null;
        } else {
            return skinInfo;
        }
    }

    public static PlayerSkinInfo getPlayerSkinInfo(final @NotNull String username) {
        var mcPlayer = Minecraft.getInstance().player;

        if (mcPlayer != null && username.equalsIgnoreCase(mcPlayer.getGameProfile().name())) {
            return CACHED_NAME_SKINS.computeIfAbsent(username.toLowerCase(Locale.ROOT), s -> {
                var info = new PlayerSkinInfo();
                info.set(mcPlayer.getSkin());
                return info;
            });
        }

        return CACHED_NAME_SKINS.computeIfAbsent(username.toLowerCase(Locale.ROOT), (key) -> {
            Util.backgroundExecutor().execute(() -> {
                var profile = getGameProfile(username);

                if (profile != null) {
                    Minecraft.getInstance().getSkinManager().get(profile).thenAccept(playerSkin -> {
                        if (playerSkin.isPresent()) {
                            CACHED_NAME_SKINS.computeIfAbsent(profile.name().toLowerCase(Locale.ROOT), (_) -> new PlayerSkinInfo()).set(playerSkin.get());
                        } else {
                            CACHED_NAME_SKINS.computeIfAbsent(profile.name().toLowerCase(Locale.ROOT), (_) -> new PlayerSkinInfo()).setFailed();
                        }
                    });
                } else {
                    CACHED_NAME_SKINS.computeIfAbsent(username.toLowerCase(Locale.ROOT), (_) -> new PlayerSkinInfo()).setFailed();
                }
            });

            return new PlayerSkinInfo();
        });
    }

    public static @Nullable GameProfile getGameProfile(final @NotNull String name) {
        return CACHED_NAME_PROFILES.computeIfAbsent(name, (key) -> {
            JsonObject object = parseURL("https://api.mojang.com/users/profiles/minecraft/" + key);
            if (object == null) return null;
            UUID uuid = fromStringWithoutDashes(GsonHelper.getAsString(object, "id"));
            String realName = GsonHelper.getAsString(object, "name");

            JsonObject sessionObject = parseURL("https://sessionserver.mojang.com/session/minecraft/profile/" + uuid + "?unsigned=false");
            if (sessionObject != null && sessionObject.has("properties")) {
                var propsBuilder = ImmutableMultimap.<String, Property>builder();
                for (var element : sessionObject.getAsJsonArray("properties")) {
                    var prop = element.getAsJsonObject();
                    String propName = GsonHelper.getAsString(prop, "name");
                    String propValue = GsonHelper.getAsString(prop, "value");
                    String propSignature = prop.has("signature") ? GsonHelper.getAsString(prop, "signature") : null;
                    propsBuilder.put(propName, new Property(propName, propValue, propSignature));
                }
                return new GameProfile(uuid, realName, new PropertyMap(propsBuilder.build()));
            }

            return new GameProfile(uuid, realName);
        });
    }

    private static @Nullable JsonObject parseURL(final @NotNull String purl) {
        try {
            URI uri = new URI(purl);
            HttpRequest request = HttpRequest.newBuilder(uri)
                    .GET()
                    .build();

            HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new IllegalStateException("Failed to do an HTTP request to: " + purl + " Response code: " + response.statusCode());
            }

            String responseBody = response.body();
            return JsonParser.parseString(responseBody).getAsJsonObject();
        } catch (Exception e) {
            Palladium.LOGGER.warn("Failed to fetch URL: {}", purl);
        }
        return null;
    }

    private static UUID fromStringWithoutDashes(final @NotNull String uuid) {
        String correctedUUID = UUID_PATTERN.matcher(uuid).replaceAll("$1-$2-$3-$4-$5");
        return UUID.fromString(correctedUUID);
    }

}
