package net.threetag.palladium.client.trail;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.world.entity.Entity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;
import net.threetag.palladium.entity.data.PalladiumEntityData;

public class EntityTrailHandler extends PalladiumEntityData<Entity, EntityTrailHandler> {

    public static final Codec<EntityTrailHandler> CODEC = MapCodec.unit(EntityTrailHandler::create).codec();

    public static EntityTrailHandler create() {
        if (FMLEnvironment.getDist() == Dist.CLIENT) {
            return new ClientEntityTrailHandler();
        } else {
            return new EntityTrailHandler();
        }
    }

    @Override
    public Codec<EntityTrailHandler> codec() {
        return CODEC;
    }
}
