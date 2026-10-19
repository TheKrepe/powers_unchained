package lucraft.mods.lucraftcore.superpowers;

import lucraft.mods.lucraftcore.superpowers.abilities.Ability;
import lucraft.mods.lucraftcore.superpowers.abilities.AbilityEntry;
import lucraft.mods.lucraftcore.superpowers.events.AbilityKeyEvent;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.server.permission.DefaultPermissionLevel;
import net.minecraftforge.server.permission.PermissionAPI;

import java.util.HashMap;
import java.util.Map;

public class PermissionAbilityHandler {

    public static Map<ResourceLocation, String> PERMS = new HashMap<>();

    public static void init() {
        for (AbilityEntry entry : Ability.ABILITY_REGISTRY.getValuesCollection()) {
            String perm = "ability." + entry.getRegistryName().getNamespace() + "." + entry.getRegistryName().getPath();
            PermissionAPI.registerNode(perm, DefaultPermissionLevel.ALL, "Ability '" + entry.getRegistryName().toString() + "'");
            PERMS.put(entry.getRegistryName(), perm);
        }
    }

    @SubscribeEvent
    public void onAbility(AbilityKeyEvent.Server e) {
        String perm = PERMS.get(e.ability.getAbilityEntry().getRegistryName());

        if (e.entity instanceof EntityPlayerMP) {
            if (!PermissionAPI.hasPermission((EntityPlayer) e.entity, perm)) {
                e.setCanceled(true);
                ((EntityPlayerMP) e.entity).sendStatusMessage(new TextComponentTranslation("lucraftcore.info.no_perm_ability"), true);
            }
        }
    }

}
