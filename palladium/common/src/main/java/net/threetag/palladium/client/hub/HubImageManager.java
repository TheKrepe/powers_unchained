package net.threetag.palladium.client.hub;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import net.threetag.palladium.Palladium;
import org.jetbrains.annotations.Nullable;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class HubImageManager {

    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_2)
            .build();

    private final Minecraft minecraft = Minecraft.getInstance();
    private final Map<String, LoadedImage> loaded = new ConcurrentHashMap<>();
    private final Set<String> requested = ConcurrentHashMap.newKeySet();

    @Nullable
    public LoadedImage get(@Nullable String url) {
        if (url == null || url.isBlank()) {
            return null;
        }

        LoadedImage existing = this.loaded.get(url);
        if (existing != null) {
            return existing;
        }

        if (this.requested.add(url)) {
            this.download(url);
        }

        return null;
    }

    private void download(String url) {
        HttpRequest request;
        try {
            request = HttpRequest.newBuilder(URI.create(url)).GET().build();
        } catch (Exception e) {
            Palladium.LOGGER.warn("Invalid PalladiumHub image url {}: {}", url, e.getMessage());
            return;
        }

        HTTP_CLIENT.sendAsync(request, HttpResponse.BodyHandlers.ofByteArray())
                .thenAccept(response -> {
                    if (response.statusCode() != 200) {
                        Palladium.LOGGER.warn("Failed to download addon image {} (status {})", url, response.statusCode());
                        return;
                    }

                    NativeImage image;
                    try {
                        image = decode(response.body());
                    } catch (Exception e) {
                        Palladium.LOGGER.warn("Failed to decode addon image {}: {}", url, e.getMessage());
                        return;
                    }

                    if (image == null) {
                        Palladium.LOGGER.warn("Unsupported image format for addon image {}", url);
                        return;
                    }

                    this.minecraft.execute(() -> this.register(url, image));
                })
                .exceptionally(throwable -> {
                    Palladium.LOGGER.warn("Failed to download addon image {}: {}", url, throwable.getMessage());
                    return null;
                });
    }

    @Nullable
    private static NativeImage decode(byte[] bytes) throws IOException {
        BufferedImage buffered = ImageIO.read(new ByteArrayInputStream(bytes));
        if (buffered == null) {
            return null;
        }

        int width = buffered.getWidth();
        int height = buffered.getHeight();
        int[] argb = buffered.getRGB(0, 0, width, height, null, 0, width);

        NativeImage image = new NativeImage(NativeImage.Format.RGBA, width, height, false);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int color = argb[y * width + x];
                int abgr = (color & 0xFF00FF00) | ((color >> 16) & 0xFF) | ((color & 0xFF) << 16);
                image.setPixelRGBA(x, y, abgr);
            }
        }

        return image;
    }

    private void register(String url, NativeImage image) {
        ResourceLocation id = Palladium.id("hub/image/" + Integer.toHexString(url.hashCode()));

        try {
            int width = image.getWidth();
            int height = image.getHeight();
            this.minecraft.getTextureManager().register(id, new DynamicTexture(image));
            this.loaded.put(url, new LoadedImage(id, width, height));
        } catch (Exception e) {
            image.close();
            Palladium.LOGGER.warn("Failed to register addon image texture {}: {}", url, e.getMessage());
        }
    }

    public void releaseAll() {
        for (LoadedImage image : this.loaded.values()) {
            this.minecraft.getTextureManager().release(image.id());
        }

        this.loaded.clear();
        this.requested.clear();
    }

    public record LoadedImage(ResourceLocation id, int width, int height) {
    }
}
