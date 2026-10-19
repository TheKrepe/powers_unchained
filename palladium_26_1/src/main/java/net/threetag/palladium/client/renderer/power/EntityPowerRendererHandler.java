package net.threetag.palladium.client.renderer.power;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;
import net.threetag.palladium.entity.data.PalladiumEntityData;

public class EntityPowerRendererHandler extends PalladiumEntityData<LivingEntity, EntityPowerRendererHandler> {

    public static final Codec<EntityPowerRendererHandler> CODEC = MapCodec.unit(EntityPowerRendererHandler::create).codec();

    public static EntityPowerRendererHandler create() {
        if (FMLEnvironment.getDist() == Dist.CLIENT) {
            return new ClientPowerRendererHandler();
        }
        return new EntityPowerRendererHandler();
    }

    @Override
    public Codec<EntityPowerRendererHandler> codec() {
        return CODEC;
    }
}
