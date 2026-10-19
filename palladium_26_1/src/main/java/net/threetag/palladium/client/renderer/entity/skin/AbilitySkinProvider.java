package net.threetag.palladium.client.renderer.entity.skin;

import net.minecraft.world.entity.Avatar;
import net.threetag.palladium.logic.context.DataContext;
import net.threetag.palladium.power.ability.AbilitySerializers;
import net.threetag.palladium.power.ability.AbilityUtil;

public class AbilitySkinProvider implements PlayerSkinChangeManager.SkinProvider {

    @Override
    public PlayerSkinInfo getSkin(Avatar avatar, PlayerSkinInfo previousSkin, PlayerSkinInfo defaultSkin) {
        var abilities = AbilityUtil.getEnabledInstances(avatar, AbilitySerializers.SKIN_CHANGE.get()).stream().sorted((a1, a2) -> a2.getAbility().priority - a1.getAbility().priority).toList();

        if (!abilities.isEmpty()) {
            var ability = abilities.getFirst();
            var model = ability.getAbility().modelChangeType.orElse(previousSkin.getSkin().model());

            return new PlayerSkinInfo(
                    ability.getAbility().texture.get(avatar).getTexture(DataContext.forAbility(avatar, ability)),
                    model
            );
        }

        return previousSkin;
    }
}
