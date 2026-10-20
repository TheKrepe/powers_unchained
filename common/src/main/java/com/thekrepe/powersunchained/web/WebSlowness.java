package com.thekrepe.powersunchained.web;

import com.thekrepe.powersunchained.Constants;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

/**
 * Sticky webs on an entity's legs slow it down: every web stuck to it adds to this tick's count, at the end of the
 * server tick the movement speed modifier is set from the count (like Fisk's {@code applyWebSlowness}):
 * 1 web -> -40%, 2 -> -53%, 3 -> -60% ... and removed once no web is left.
 */
public final class WebSlowness {

    private static final Identifier MODIFIER = Constants.id("web_slowness");
    private static final Map<LivingEntity, Float> COUNTS = new WeakHashMap<>();

    private WebSlowness() {
    }

    /** Server side, called by every sticky web on the entity's legs, every tick. */
    public static void add(LivingEntity entity) {
        COUNTS.merge(entity, 1.0F, Float::sum);
    }

    /** Server side, at the end of every server tick. */
    public static void endTick() {
        COUNTS.replaceAll((entity, count) -> {
            apply(entity, count);
            return 0.0F;
        });
        // entities without webs got their modifier removed above -> stop tracking them
        COUNTS.entrySet().removeIf(e -> !hasModifier(e.getKey()));
    }

    private static boolean hasModifier(LivingEntity entity) {
        AttributeInstance speed = entity.getAttribute(Attributes.MOVEMENT_SPEED);
        return speed != null && speed.getModifier(MODIFIER) != null;
    }

    private static void apply(LivingEntity entity, float count) {
        AttributeInstance speed = entity.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speed == null) {
            return;
        }
        double amount = count > 0 ? (1.0 / (1.0 + count) - 1.0) / 1.25 : 0.0;
        AttributeModifier current = speed.getModifier(MODIFIER);
        if (amount == 0.0) {
            if (current != null) {
                speed.removeModifier(MODIFIER);
            }
        } else if (current == null || current.amount() != amount) {
            if (current != null) {
                speed.removeModifier(MODIFIER);
            }
            speed.addTransientModifier(new AttributeModifier(MODIFIER, amount, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
        }
    }
}
