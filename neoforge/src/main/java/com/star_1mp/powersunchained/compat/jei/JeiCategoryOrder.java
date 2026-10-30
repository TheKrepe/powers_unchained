package com.star_1mp.powersunchained.compat.jei;

import com.star_1mp.powersunchained.Constants;
import com.star_1mp.powersunchained.platform.Services;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * JEI has no API for the order of recipe categories: it keeps it in {@code config/jei/recipe-category-sort-order.ini}
 * (crafting first, then minecraft's, then the rest A-Z; new ones are appended). So the Suit Creator would come after
 * the anvil for suit pieces. Once per game folder (marker file) we put our category right after crafting in that file;
 * after that the player's own order in JEI is never touched again.
 */
final class JeiCategoryOrder {

    private static final String VISIBLE = "[visible]";
    private static final String HIDDEN = "[hidden]";
    private static final String CRAFTING = "minecraft:crafting";

    private JeiCategoryOrder() {
    }

    static void suitCreatorFirst(String id) {
        Path config = Services.PLATFORM.getConfigDir();
        Path marker = config.resolve(Constants.MOD_ID + "-jei-order.done");
        if (Files.exists(marker)) {
            return;
        }
        Path file = config.resolve("jei").resolve("recipe-category-sort-order.ini");
        try {
            List<String> lines;
            if (Files.exists(file)) {
                lines = new ArrayList<>(Files.readAllLines(file, StandardCharsets.UTF_8));
                int hidden = lines.indexOf(HIDDEN);
                if (hidden >= 0 && lines.subList(hidden, lines.size()).stream().anyMatch(l -> l.trim().equals(id))) {
                    // the player hid it on purpose
                    Files.writeString(marker, "");
                    return;
                }
                lines.removeIf(l -> l.trim().equals(id));
                int visible = lines.indexOf(VISIBLE);
                if (visible < 0) {
                    lines.add(0, VISIBLE);
                    visible = 0;
                }
                int end = lines.indexOf(HIDDEN);
                int crafting = -1;
                for (int i = visible + 1; i < (end < 0 ? lines.size() : end); i++) {
                    if (lines.get(i).trim().equals(CRAFTING)) {
                        crafting = i;
                    }
                }
                lines.add(crafting >= 0 ? crafting + 1 : visible + 1, id);
                if (!lines.contains(HIDDEN)) {
                    lines.add(HIDDEN);
                }
            } else {
                // first start with JEI: JEI appends every other category after these two
                lines = List.of(VISIBLE, CRAFTING, id, HIDDEN);
            }
            Files.createDirectories(file.getParent());
            Files.write(file, lines, StandardCharsets.UTF_8);
            Files.writeString(marker, "");
            Constants.LOG.info("Put {} right after crafting in JEI's recipe category order", id);
        } catch (IOException | RuntimeException e) {
            Constants.LOG.warn("Could not adjust JEI's recipe category order in {}", file, e);
        }
    }
}
