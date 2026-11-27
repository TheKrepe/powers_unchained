package com.star_1mp.powersunleashed.power;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import org.jspecify.annotations.Nullable;

/** A reusable ability definition. A superpower decides how its player earns access to it. */
public final class PowerAbility {

    private static volatile Map<String, PowerAbility> registry = Map.of();

    /** Crawl over walls and ceilings like Marvel's Spider-Man. */
    public static final PowerAbility WALL_CLIMBING = registerToggle("wall_climbing", true);
    /** Night vision (off until switched on in the wheel). */
    public static final PowerAbility NIGHT_VISION = registerToggle("night_vision", false);
    /** Warns about danger nearby. */
    public static final PowerAbility SPIDER_SENSE = registerToggle("spider_sense", true);
    /** Dodge roll on the ground, front flip in the air. */
    public static final PowerAbility ROLL = registerTriggered("roll");

    private enum Activation {
        TOGGLE, TRIGGER, ULTIMATE
    }

    private final String id;
    private final Activation activation;
    private final boolean onByDefault;
    private final @Nullable Consumer<ServerPlayer> ultimateAction;

    private PowerAbility(String id, Activation activation, boolean onByDefault, @Nullable Consumer<ServerPlayer> ultimateAction) {
        this.id = id;
        this.activation = activation;
        this.onByDefault = onByDefault;
        this.ultimateAction = ultimateAction;
    }

    public static PowerAbility registerToggle(String id, boolean onByDefault) {
        return register(new PowerAbility(id, Activation.TOGGLE, onByDefault, null));
    }

    public static PowerAbility registerTriggered(String id) {
        return register(new PowerAbility(id, Activation.TRIGGER, false, null));
    }

    /** Registers an ultimate during mod initialization. The action runs on the server. */
    public static PowerAbility registerUltimate(String id, Consumer<ServerPlayer> action) {
        return register(new PowerAbility(id, Activation.ULTIMATE, false, Objects.requireNonNull(action)));
    }

    private static synchronized PowerAbility register(PowerAbility ability) {
        String id = ability.id();
        Identifier key = Identifier.tryParse(id);
        if (key == null || key.getPath().isEmpty() || id.length() > 128 || "none".equals(id)) {
            throw new IllegalArgumentException("Invalid ability ID: " + id);
        }
        if (registry.containsKey(id)) {
            throw new IllegalArgumentException("Duplicate ability ID: " + id);
        }
        Map<String, PowerAbility> abilities = new LinkedHashMap<>(registry);
        abilities.put(id, ability);
        registry = Collections.unmodifiableMap(abilities);
        return ability;
    }

    public String id() {
        return this.id;
    }

    public boolean toggleable() {
        return this.activation == Activation.TOGGLE;
    }

    public boolean ultimate() {
        return this.activation == Activation.ULTIMATE;
    }

    public void activateUltimate(ServerPlayer player) {
        if (this.ultimateAction != null && player.isAlive() && !player.isSpectator() && Superpowers.has(player, this)) {
            this.ultimateAction.accept(player);
        }
    }

    public boolean onByDefault() {
        return this.onByDefault;
    }

    public Component title() {
        return Component.translatable("ability.powers_unleashed." + this.id);
    }

    public static Collection<PowerAbility> all() {
        return registry.values();
    }

    public static @Nullable PowerAbility byId(String id) {
        return registry.get(id);
    }
}
