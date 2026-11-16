package com.star_1mp.powersunleashed.client.suitcreator;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.star_1mp.powersunleashed.Constants;
import com.star_1mp.powersunleashed.platform.Services;
import com.star_1mp.powersunleashed.suit.SuitTag;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Per-player Suit Creator view settings, kept in {@code config/powers_unleashed-suit_creator.json}: enabled tags,
 * favorite suits, collapsed groups. The selected suit / tab only live for the session.
 */
public final class SuitCreatorPrefs {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static final Set<SuitTag> TAGS = EnumSet.allOf(SuitTag.class);
    public static final Set<String> FAVORITES = new LinkedHashSet<>();
    public static final Set<String> COLLAPSED = new LinkedHashSet<>();

    /** Session state. */
    public static String selected = "";
    public static int tab = 0;
    public static boolean slim = false;

    private static boolean loaded;

    private SuitCreatorPrefs() {
    }

    private static Path file() {
        return Services.PLATFORM.getConfigDir().resolve("powers_unleashed-suit_creator.json");
    }

    public static void load() {
        if (loaded) {
            return;
        }
        loaded = true;
        Path file = file();
        if (!Files.exists(file)) {
            return;
        }
        try (Reader reader = Files.newBufferedReader(file)) {
            Data data = GSON.fromJson(reader, Data.class);
            if (data == null) {
                return;
            }
            if (data.tags != null) {
                java.util.Set<SuitTag> loadedTags = EnumSet.noneOf(SuitTag.class);
                for (String name : data.tags) {
                    for (SuitTag tag : SuitTag.values()) {
                        if (tag.name().equalsIgnoreCase(name)) {
                            loadedTags.add(tag);
                        }
                    }
                }
                // a saved list of tags that no longer exist (tags were renamed): keep all on
                if (!loadedTags.isEmpty() || data.tags.isEmpty()) {
                    TAGS.clear();
                    TAGS.addAll(loadedTags);
                }
            }
            if (data.favorites != null) {
                FAVORITES.addAll(data.favorites);
            }
            if (data.collapsed != null) {
                COLLAPSED.addAll(data.collapsed);
            }
        } catch (IOException | RuntimeException e) {
            Constants.LOG.warn("Could not read {}: {}", file, e.toString());
        }
    }

    public static void save() {
        Data data = new Data();
        data.tags = TAGS.stream().map(t -> t.name().toLowerCase(java.util.Locale.ROOT)).toList();
        data.favorites = new ArrayList<>(FAVORITES);
        data.collapsed = new ArrayList<>(COLLAPSED);
        Path file = file();
        try {
            Files.createDirectories(file.getParent());
            try (Writer writer = Files.newBufferedWriter(file)) {
                GSON.toJson(data, writer);
            }
        } catch (IOException e) {
            Constants.LOG.warn("Could not write {}: {}", file, e.toString());
        }
    }

    private static final class Data {
        List<String> tags;
        List<String> favorites;
        List<String> collapsed;
    }
}
