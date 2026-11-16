package com.star_1mp.powersunleashed.client;

import com.star_1mp.powersunleashed.Constants;
import com.star_1mp.powersunleashed.platform.Services;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

/**
 * The mod was renamed Powers Unchained -> Powers Unleashed (id {@code powers_unchained} -> {@code powers_unleashed}).
 * Keeps the player's client settings: {@code config/powers_unchained-*} files are renamed once, unless a new file
 * with that name already exists.
 */
public final class ConfigMigration {
    private static final String OLD_PREFIX = "powers_unchained";

    private ConfigMigration() {
    }

    public static void run() {
        Path dir = Services.PLATFORM.getConfigDir();
        if (!Files.isDirectory(dir)) {
            return;
        }
        try (Stream<Path> files = Files.list(dir)) {
            files.filter(p -> p.getFileName().toString().startsWith(OLD_PREFIX + "-")).forEach(old -> {
                Path renamed = old.resolveSibling(Constants.MOD_ID + old.getFileName().toString().substring(OLD_PREFIX.length()));
                if (Files.exists(renamed)) {
                    return;
                }
                try {
                    Files.move(old, renamed);
                    Constants.LOG.info("Renamed old config {} -> {}", old.getFileName(), renamed.getFileName());
                } catch (IOException e) {
                    Constants.LOG.warn("Could not rename old config {}", old, e);
                }
            });
        } catch (IOException e) {
            Constants.LOG.warn("Could not look for old configs", e);
        }
    }
}
