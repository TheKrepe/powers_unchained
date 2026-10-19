package net.threetag.palladium.logic.cost;

import com.mojang.serialization.Codec;
import net.minecraft.world.entity.LivingEntity;
import net.threetag.palladium.registry.PalladiumRegistries;

public abstract class Cost {

    public static final Codec<Cost> CODEC = PalladiumRegistries.COST_SERIALIZER.byNameCodec().dispatch(Cost::getSerializer, CostSerializer::codec);

    public abstract boolean hasEnoughCurrency(LivingEntity entity);

    public abstract void consumeCurrency(LivingEntity entity);

    public abstract CostDisplay createDisplay();

    public abstract CostSerializer<?> getSerializer();

}
