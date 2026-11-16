package com.star_1mp.powersunleashed.client.ability;

import com.star_1mp.powersunleashed.client.input.PUKeyMappings;
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
 * One key system for every ability (like Palladium's). By default every ability the player has right now (web-shooter
 * gadgets, superpowers...) gets the next free ability key: the web-shooters first (keys 1-3) and Spider-Man's powers
 * after them, so without web-shooters the powers move up to keys 1-2. In the Ability Settings the player can pick their
 * own set instead ({@link AbilityHudConfig#abilitySlots}): one ability (or none) per key; a key whose ability the player
 * doesn't have right now stays empty, and empty keys are not shown in the HUD.
 */
public final class AbilitySlots {

    public static final String WEB_SHOT = "web_shot";
    public static final String WEB_ZIP = "web_zip";
    public static final String WEB_SWING = "web_swing";
    public static final String ROLL = "roll";
    public static final String ABILITY_WHEEL = "ability_wheel";
    /** Default order of all abilities. */
    /** No ability on the key. */
    public static final String NONE = "none";
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

    /** The abilities the player has right now, in the default order (all of them, the choices of the Ability Settings). */
    public static List<String> owned(LocalPlayer player) {
        List<String> list = new ArrayList<>();
        for (String id : ordered()) {
            if (ABILITIES.get(id).available().test(player)) {
                list.add(id);
            }
        }
        return list;
    }

    /** Whether the player picked their own ability set. */
    public static boolean custom() {
        return AbilityHudConfig.get().abilitySlots != null;
    }

    /** The ability chosen for a key in the player's own set ({@link #NONE} if none), or the automatic one. */
    public static String chosen(@Nullable LocalPlayer player, int slot) {
        List<String> slots = AbilityHudConfig.get().abilitySlots;
        if (slots != null) {
            return slot < slots.size() && slots.get(slot) != null ? slots.get(slot) : NONE;
        }
        String[] auto = player != null ? assignment(player) : autoAll();
        return auto[slot] != null ? auto[slot] : NONE;
    }

    /** Puts an ability (or {@link #NONE}) on a key of the player's own set; it leaves any other key it was on. */
    public static void choose(@Nullable LocalPlayer player, int slot, String id) {
        AbilityHudConfig config = AbilityHudConfig.get();
        List<String> slots = new ArrayList<>();
        for (int i = 0; i < ASSIGNED.length; i++) {
            slots.add(chosen(player, i));
        }
        if (!NONE.equals(id)) {
            for (int i = 0; i < slots.size(); i++) {
                if (id.equals(slots.get(i))) {
                    slots.set(i, NONE);
                }
            }
        }
        slots.set(slot, id);
        config.abilitySlots = slots;
        config.save();
    }

    /** Back to the automatic set. */
    public static void resetChoices() {
        AbilityHudConfig config = AbilityHudConfig.get();
        config.abilitySlots = null;
        config.abilityOrder = null;
        config.save();
    }

    /** Every ability in the default order on the keys (no player to ask). */
    private static String[] autoAll() {
        String[] out = new String[ASSIGNED.length];
        List<String> all = ordered();
        for (int i = 0; i < out.length && i < all.size(); i++) {
            out[i] = all.get(i);
        }
        return out;
    }

    /** Which ability is on which key for the player right now (null = none). */
    public static String[] assignment(LocalPlayer player) {
        String[] out = new String[ASSIGNED.length];
        List<String> slots = AbilityHudConfig.get().abilitySlots;
        if (slots != null) {
            for (int i = 0; i < out.length && i < slots.size(); i++) {
                Ability ability = slots.get(i) != null ? ABILITIES.get(slots.get(i)) : null;
                out[i] = ability != null && ability.available().test(player) ? ability.id() : null;
            }
            return out;
        }
        List<String> owned = owned(player);
        for (int i = 0; i < out.length && i < owned.size(); i++) {
            out[i] = owned.get(i);
        }
        return out;
    }

    /** Client tick (before the abilities read their keys): which ability is on which key now. */
    public static void tick() {
        LocalPlayer player = Minecraft.getInstance().player;
        String[] now = player != null ? assignment(player) : new String[ASSIGNED.length];
        for (int i = 0; i < ASSIGNED.length; i++) {
            ASSIGNED[i] = now[i];
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

    /** The HUD lines per key for the player (null = no ability on that key), or {@code null} if it has none at all. */
    public static AbilityHud.Entry @Nullable [] entries(LocalPlayer player) {
        String[] now = assignment(player);
        AbilityHud.Entry[] entries = new AbilityHud.Entry[ASSIGNED.length];
        boolean any = false;
        for (int i = 0; i < now.length; i++) {
            if (now[i] != null) {
                entries[i] = ABILITIES.get(now[i]).entry().apply(player);
                any = true;
            }
        }
        return any ? entries : null;
    }
}
