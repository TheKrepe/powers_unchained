package com.star_1mp.powersunleashed.power;

import com.star_1mp.powersunleashed.Constants;
import com.star_1mp.powersunleashed.power.skill.SkillTrees;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

/** A named set of reusable abilities granted to a player together. */
public final class Superpower {

    private static volatile Map<String, Superpower> registry = Map.of();

    public static final Superpower SPIDERMAN = register("spiderman", Constants.id("suit_creator/group/spider_man"), List.of(
            new AbilityGrant(PowerAbility.WALL_CLIMBING, null),
            new AbilityGrant(PowerAbility.NIGHT_VISION, SkillTrees.NIGHT_VISION),
            new AbilityGrant(PowerAbility.SPIDER_SENSE, null),
            new AbilityGrant(PowerAbility.ROLL, SkillTrees.ROLL)));

    /** An optional skill in this power's own tree unlocks the ability. */
    public record AbilityGrant(PowerAbility ability, @Nullable String requiredSkill) {
    }

    private final String id;
    private final Identifier emblem;
    private final Map<PowerAbility, AbilityGrant> abilities;

    private Superpower(String id, Identifier emblem, Map<PowerAbility, AbilityGrant> abilities) {
        this.id = id;
        this.emblem = emblem;
        this.abilities = abilities;
    }

    public static synchronized Superpower register(String id, Identifier emblem, List<AbilityGrant> grants) {
        if (registry.containsKey(id)) {
            throw new IllegalArgumentException("Duplicate power ID: " + id);
        }
        Map<PowerAbility, AbilityGrant> abilities = new LinkedHashMap<>();
        for (AbilityGrant grant : grants) {
            if (abilities.putIfAbsent(grant.ability(), grant) != null) {
                throw new IllegalArgumentException("Duplicate ability " + grant.ability().id() + " in power " + id);
            }
        }
        Superpower power = new Superpower(id, emblem, Collections.unmodifiableMap(abilities));
        Map<String, Superpower> powers = new LinkedHashMap<>(registry);
        powers.put(id, power);
        registry = Collections.unmodifiableMap(powers);
        return power;
    }

    public String id() {
        return this.id;
    }

    public Identifier emblem() {
        return this.emblem;
    }

    public Collection<AbilityGrant> abilities() {
        return this.abilities.values();
    }

    public @Nullable AbilityGrant grant(PowerAbility ability) {
        return this.abilities.get(ability);
    }

    public Component title() {
        return Component.translatable("superpower.powers_unleashed." + this.id);
    }

    public static Collection<Superpower> all() {
        return registry.values();
    }

    public static @Nullable Superpower byId(String id) {
        return registry.get(id);
    }
}
