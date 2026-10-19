package net.threetag.palladium.item.suitset;


import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;
import net.minecraft.world.item.Item;
import net.threetag.palladium.Palladium;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public class SuitSets {

    private static final BiMap<Identifier, SuitSet> SUIT_SETS = HashBiMap.create();
    private static final Map<Item, SuitSet> ITEM_SUIT_SETS = new HashMap<>();

    public static final Codec<SuitSet> BY_NAME_CODEC = Identifier.CODEC.flatXmap(identifier -> {
        SuitSet suitSet = SUIT_SETS.get(identifier);
        return suitSet != null ? DataResult.success(suitSet) : DataResult.error(() -> "Unknown suit set " + identifier);
    }, suitSet -> {
        Identifier identifier = SUIT_SETS.inverse().get(suitSet);
        return suitSet != null ? DataResult.success(identifier) : DataResult.error(() -> "Unknown suit set " + identifier);
    });

    public static void register(Identifier id, SuitSet suitSet) {
        if (SUIT_SETS.containsKey(id)) {
            throw new IllegalStateException("Suit set " + id + " is already registered");
        }

        SUIT_SETS.put(id, suitSet);
    }

    @Nullable
    public static SuitSet get(Identifier id) {
        return SUIT_SETS.get(id);
    }

    public static SuitSet getFromItem(Item item) {
        return ITEM_SUIT_SETS.get(item);
    }

    @Nullable
    public static Identifier getKey(SuitSet suitSet) {
        return SUIT_SETS.inverse().get(suitSet);
    }

    public static String makeDescriptionId(Identifier id) {
        return Util.makeDescriptionId(Palladium.MOD_ID + ".suit_set", id);
    }

    public static Component getTitle(Identifier id) {
        return Component.translatable(makeDescriptionId(id));
    }

    public static void initItemLookup() {
        ITEM_SUIT_SETS.clear();

        for (SuitSet suitSet : SUIT_SETS.values()) {
            suitSet.helmet().ifPresent(item -> ITEM_SUIT_SETS.put(item, suitSet));
            suitSet.chestplate().ifPresent(item -> ITEM_SUIT_SETS.put(item, suitSet));
            suitSet.leggings().ifPresent(item -> ITEM_SUIT_SETS.put(item, suitSet));
            suitSet.boots().ifPresent(item -> ITEM_SUIT_SETS.put(item, suitSet));
        }
    }

}
