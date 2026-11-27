package com.star_1mp.powersunleashed.client.ability;

import com.star_1mp.powersunleashed.client.input.PUKeyMappings;
import com.star_1mp.powersunleashed.power.PowerAbility;
import com.star_1mp.powersunleashed.power.Superpowers;
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
 * doesn't have right now stays empty, and empty keys are not shown in the HUD. The ultimate has its own key.
 */
public final class AbilitySlots {

    public static final String WEB_SHOT = "web_shot";
    public static final String WEB_ZIP = "web_zip";
    public static final String WEB_SWING = "web_swing";
    public static final String ROLL = "roll";
    public static final String ABILITY_WHEEL = "ability_wheel";
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
            Component previewName, @Nullable PowerAbility ultimate) {
    }

    private static final Map<String, Ability> ABILITIES = new LinkedHashMap<>();
    /** Ability ids on the keys (slot -> id, null = free), refreshed every tick. */
    private static final String[] ASSIGNED = new String[PUKeyMappings.ALL.size()];

    private AbilitySlots() {
    }

    public static void register(String id, Predicate<LocalPlayer> available, Function<LocalPlayer, AbilityHud.Entry> entry,
            Component previewName) {
        ABILITIES.put(id, new Ability(id, available, entry, previewName, null));
    }

    public static void registerUltimate(PowerAbility ability) {
        if (!ability.ultimate()) {
            throw new IllegalArgumentException("Expected an ultimate: " + ability.id());
        }
        Component name = ability.title();
        Ability entry = new Ability(ability.id(), player -> Superpowers.has(player, ability),
                player -> new AbilityHud.Entry(name, null, true), name, ability);
        if (ABILITIES.putIfAbsent(ability.id(), entry) != null) {
            throw new IllegalArgumentException("Duplicate ability key entry: " + ability.id());
        }
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
    public static List<String> owned(@Nullable LocalPlayer player) {
        List<String> list = new ArrayList<>();
        if (player == null) {
            return list;
        }
        for (String id : ordered()) {
            if (ABILITIES.get(id).available().test(player)) {
                list.add(id);
            }
        }
        return list;
    }

    /** Whether the player picked their own ability set. */
    public static boolean custom() {
        AbilityHudConfig config = AbilityHudConfig.get();
        return config.abilitySlots != null || config.ultimateAbility != null;
    }

    public static boolean accepts(int slot, @Nullable String id) {
        if (slot < 0 || slot >= ASSIGNED.length) {
            return false;
        }
        Ability ability = ABILITIES.get(id);
        return NONE.equals(id) || (ability != null && (ability.ultimate() != null) == (slot == PUKeyMappings.ULTIMATE_SLOT));
    }

    /** The configured or automatic choice for each slot, including choices the player doesn't currently own. */
    public static String[] choices(@Nullable LocalPlayer player) {
        AbilityHudConfig config = AbilityHudConfig.get();
        String[] choices = automatic(player);
        for (int slot = 0; slot < choices.length; slot++) {
            if (slot == PUKeyMappings.ULTIMATE_SLOT) {
                if (config.ultimateAbility != null) {
                    choices[slot] = config.ultimateAbility;
                }
            } else if (config.abilitySlots != null) {
                choices[slot] = slot < config.abilitySlots.size() ? config.abilitySlots.get(slot) : null;
            }
            if (!accepts(slot, choices[slot])) {
                choices[slot] = NONE;
            }
        }
        return choices;
    }

    /** Puts an ability (or {@link #NONE}) on a compatible key, clearing any duplicate key. */
    public static boolean choose(@Nullable LocalPlayer player, int slot, String id) {
        if (!accepts(slot, id)) {
            return false;
        }
        AbilityHudConfig config = AbilityHudConfig.get();
        if (slot == PUKeyMappings.ULTIMATE_SLOT) {
            config.ultimateAbility = id;
            config.save();
            return true;
        }
        List<String> slots = new ArrayList<>();
        String[] choices = choices(player);
        for (int i = 0; i < PUKeyMappings.ULTIMATE_SLOT; i++) {
            String previous = choices[i];
            slots.add(id.equals(previous) ? NONE : previous);
        }
        slots.set(slot, id);
        config.abilitySlots = slots;
        config.save();
        return true;
    }

    /** Back to the automatic set. */
    public static void resetChoices() {
        AbilityHudConfig config = AbilityHudConfig.get();
        config.abilitySlots = null;
        config.abilityOrder = null;
        config.ultimateAbility = null;
        config.save();
    }

    private static String[] automatic(@Nullable LocalPlayer player) {
        String[] out = new String[ASSIGNED.length];
        int next = 0;
        for (String id : player == null ? ordered() : owned(player)) {
            if (ABILITIES.get(id).ultimate() != null) {
                if (out[PUKeyMappings.ULTIMATE_SLOT] == null) {
                    out[PUKeyMappings.ULTIMATE_SLOT] = id;
                }
            } else if (next < PUKeyMappings.ULTIMATE_SLOT) {
                out[next++] = id;
            }
        }
        return out;
    }

    /** Which ability is on which key for the player right now (null = none). */
    public static String[] assignment(LocalPlayer player) {
        AbilityHudConfig config = AbilityHudConfig.get();
        String[] out = automatic(player);
        List<String> slots = config.abilitySlots;
        if (slots != null) {
            for (int i = 0; i < PUKeyMappings.ULTIMATE_SLOT; i++) {
                out[i] = assigned(player, i, i < slots.size() ? slots.get(i) : null);
            }
        }
        if (config.ultimateAbility != null) {
            out[PUKeyMappings.ULTIMATE_SLOT] = assigned(player, PUKeyMappings.ULTIMATE_SLOT, config.ultimateAbility);
        }
        return out;
    }

    private static @Nullable String assigned(LocalPlayer player, int slot, @Nullable String id) {
        Ability ability = ABILITIES.get(id);
        return ability != null && accepts(slot, id) && ability.available().test(player) ? id : null;
    }

    /** Consumes the ultimate key and returns the assigned ultimate for this press. */
    public static @Nullable PowerAbility ultimatePressed() {
        String id = ASSIGNED[PUKeyMappings.ULTIMATE_SLOT];
        if (id != null && clicks(id) > 0) {
            return ABILITIES.get(id).ultimate();
        }
        return null;
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
