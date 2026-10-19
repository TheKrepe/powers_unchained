package lucraft.mods.lucraftcore.superpowers.abilitybar;

import lucraft.mods.lucraftcore.superpowers.abilities.Ability;
import lucraft.mods.lucraftcore.superpowers.abilities.supplier.AbilityContainer;
import lucraft.mods.lucraftcore.util.abilitybar.IAbilityBarEntry;
import lucraft.mods.lucraftcore.util.abilitybar.IAbilityBarProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;

import java.util.ArrayList;
import java.util.List;

public class SuperpowerAbilityBarProvider implements IAbilityBarProvider {

    @Override
    public List<IAbilityBarEntry> getEntries() {
        EntityPlayer player = Minecraft.getMinecraft().player;
        List<IAbilityBarEntry> list = new ArrayList<>();

        for (Ability.EnumAbilityContext context : Ability.EnumAbilityContext.values()) {
            AbilityContainer container = Ability.getAbilityContainer(context, player);

            if (container != null) {
                for (Ability ab : container.getAbilities()) {
                    if (ab.showInAbilityBar() && !ab.isHidden() && ab.isUnlocked())
                        list.add(new SuperpowerAbilityBarEntry(ab));
                }
            }
        }

        return list;
    }

}