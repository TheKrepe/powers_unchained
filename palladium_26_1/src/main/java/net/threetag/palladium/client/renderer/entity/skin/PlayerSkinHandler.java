package net.threetag.palladium.client.renderer.entity.skin;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.client.entity.ClientAvatarEntity;
import net.minecraft.world.entity.Avatar;
import net.minecraft.world.entity.player.PlayerSkin;
import net.threetag.palladium.entity.data.PalladiumEntityData;
import net.threetag.palladium.entity.data.PalladiumEntityDataTypes;

public class PlayerSkinHandler extends PalladiumEntityData<Avatar, PlayerSkinHandler> {

    public static final Codec<PlayerSkinHandler> CODEC = MapCodec.unit(PlayerSkinHandler::new).codec();

    private PlayerSkin original;
    private PlayerSkinInfo overriddenSkin;

    @Override
    public void tick() {
        if (this.original != null && this.getEntity().level().isClientSide() && this.getEntity() instanceof ClientAvatarEntity av) {
            var defaultSkin = new PlayerSkinInfo(this.original);
            var loop = defaultSkin;

            for (Pair<Integer, PlayerSkinChangeManager.SkinProvider> pair : PlayerSkinChangeManager.PROVIDER) {
                loop = pair.getSecond().getSkin(this.getEntity(), loop, defaultSkin);
            }

            if (loop == defaultSkin) {
                this.overriddenSkin = null;
            } else {
                this.overriddenSkin = loop;
            }
        }
    }

    public PlayerSkinInfo getOverriddenSkin(PlayerSkin original) {
        this.original = original;
        return this.overriddenSkin;
    }

    @Override
    public Codec<PlayerSkinHandler> codec() {
        return CODEC;
    }

    public static PlayerSkinHandler get(Avatar avatar) {
        return PalladiumEntityData.get(avatar, PalladiumEntityDataTypes.SKIN_OVERRIDE.get());
    }
}
