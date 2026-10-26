package com.star_1mp.powersunchained.client.ability;

import com.star_1mp.powersunchained.client.input.PUKeyMappings;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.Nullable;

/**
 * One key system for every ability (like Palladium's): each ability the player has right now (web-shooter gadgets,
 * superpowers...) gets the next free ability key, in the player's order ({@link AbilityHudConfig#order}, changed in
 * the HUD settings). The default order puts the web-shooters first (keys 1-3) and Spider-Man's powers after them, so
 * without web-shooters the powers move up to keys 1-2.
 */
public final class AbilitySlots {

    public static final String WEB_SHOT = "web_shot";
    public static final String WEB_ZIP = "web_zip";
    public static final String WEB_SWING = "web_swing";
    public static final String ROLL = "roll";
    public static final String ABILITY_WHEEL = "ability_wheel";
    /** Default order of all abilities. */
    public static final List<String> DEFAULT_ORDER = List.of(WEB_SHOT, WEB_ZIP, WEB_SWING, ROLL, ABILITY_WHEEL);

    /**
     * An ability that can sit on a key.
     *
     * @param available whether the player has it right now
     * @param entry     its line in the HUD
     */
    public record Ability(String id, Predicate<LocalPlayer> available, Function<LocalPlayer, AbilityHud.Entry> entry,
            Component previewName) {
    }

    private static final Map<String, Ability> ABILITIES = new LinkedHashMap<>();
    /** Ability ids on the keys (slot -> id, null = free), refreshed every tick. */
    private static final String[] ASSIGNED = new String[PUKeyMappings.ALL.size()];

    private AbilitySlots() {
    }

    public static void register(String id, Predicate<LocalPlayer> available, Function<LocalPlayer, AbilityHud.Entry> entry,
            Component previewName) {
        ABILITIES.put(id, new Ability(id, available, entry, previewName));
    }

    public static @Nullable Ability get(String id) {
        return ABILITIES.get(id);
    }

    /** All registered ability ids in the player's order. */
    public static List<String> ordered() {
        List<String> order = new ArrayList<>(AbilityHudConfig.get().order());
        for (String id : DEFAULT_ORDER) {
            if (!order.contains(id)) {
                order.add(id);
            }
        }
        for (String id : ABILITIES.keySet()) {
            if (!order.contains(id)) {
                order.add(id);
            }
        }
        order.removeIf(id -> !ABILITIES.containsKey(id));
        return order;
    }

    /** The abilities the player has right now, in key order (at most one per key). */
    public static List<String> available(LocalPlayer player) {
        List<String> list = new ArrayList<>();
        for (String id : ordered()) {
            if (list.size() < ASSIGNED.length && ABILITIES.get(id).available().test(player)) {
                list.add(id);
            }
        }
        return list;
    }

    /** Client tick (before the abilities read their keys): which ability is on which key now. */
    public static void tick() {
        LocalPlayer player = Minecraft.getInstance().player;
        List<String> list = player != null ? available(player) : List.of();
        for (int i = 0; i < ASSIGNED.length; i++) {
            ASSIGNED[i] = i < list.size() ? list.get(i) : null;
            if (ASSIGNED[i] == null) {
                // a key without an ability: drop its presses, so they don't fire once something lands on it
                KeyMapping key = PUKeyMappings.ALL.get(i);
                while (key.consumeClick()) {
                    // nothing
                }
            }
        }
    }

    /** The key of an ability, or {@code null} if it has none right now. */
    public static @Nullable KeyMapping key(String id) {
        for (int i = 0; i < ASSIGNED.length; i++) {
            if (id.equals(ASSIGNED[i])) {
                return PUKeyMappings.ALL.get(i);
            }
        }
        return null;
    }

    /** Whether the ability's key is held. */
    public static boolean isDown(String id) {
        KeyMapping key = key(id);
        return key != null && key.isDown();
    }

    /** Presses of the ability's key since last asked (all of them consumed). */
    public static int clicks(String id) {
        KeyMapping key = key(id);
        int n = 0;
        if (key != null) {
            while (key.consumeClick()) {
                n++;
            }
        }
        return n;
    }

    /** The HUD lines per key for the player (null = free key), or {@code null} if it has no ability at all. */
    public static AbilityHud.Entry @Nullable [] entries(LocalPlayer player) {
        List<String> list = available(player);
        if (list.isEmpty()) {
            return null;
        }
        AbilityHud.Entry[] entries = new AbilityHud.Entry[ASSIGNED.length];
        for (int i = 0; i < list.size(); i++) {
            entries[i] = ABILITIES.get(list.get(i)).entry().apply(player);
        }
        return entries;
    }

    /** Moves an ability one place up ({@code -1}) or down ({@code +1}) among {@code shown}, swapping it with the neighbour. */
    public static void move(List<String> shown, String id, int dir) {
        int i = shown.indexOf(id);
        int j = i + dir;
        if (i < 0 || j < 0 || j >= shown.size()) {
            return;
        }
        List<String> order = ordered();
        int a = order.indexOf(id);
        int b = order.indexOf(shown.get(j));
        order.set(a, shown.get(j));
        order.set(b, id);
        AbilityHudConfig.get().abilityOrder = new ArrayList<>(order);
    }
}
