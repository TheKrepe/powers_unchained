package net.threetag.palladium.logic.context;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.threetag.palladium.Palladium;
import net.threetag.palladium.entity.PlayerSlot;
import net.threetag.palladium.power.Power;
import net.threetag.palladium.power.PowerInstance;
import net.threetag.palladium.power.ability.AbilityInstance;

public class DataContextKeys {

    public static ContextKey<Entity> ENTITY = create("entity");
    public static ContextKey<BlockPos> BLOCK_POS = create("block_pos");
    public static ContextKey<Level> LEVEL = create("level");
    public static ContextKey<ItemStack> ITEM = create("item");
    public static ContextKey<PlayerSlot> SLOT = create("slot");
    public static ContextKey<AbilityInstance<?>> ABILITY_INSTANCE = create("ability_instance");
    public static ContextKey<Holder<Power>> POWER = create("power");
    public static ContextKey<PowerInstance> POWER_INSTANCE = create("power_instance");
    public static ContextKey<Boolean> ANY_ANIMATION_FINISHED = create("any_animation_finished");
    public static ContextKey<Long> EYE_SELECTION = create("eye_selection");
    public static ContextKey<Boolean> ITEM_OPENED = create("item_opened");
    public static ContextKey<Integer> ITEM_OPEN_TIMER = create("item_open_timer");
    public static ContextKey<Identifier> EVENT_ID = create("event_id");
    public static ContextKey<CompoundTag> EVENT_DATA = create("event_data");
    public static ContextKey<Entity> TARGET = create("target");

    private static <T> ContextKey<T> create(String name) {
        return new ContextKey<>(Palladium.id(name));
    }

}
