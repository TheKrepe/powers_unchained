package lucraft.mods.lucraftcore.superpowers.abilities;

import lucraft.mods.lucraftcore.LucraftCore;
import lucraft.mods.lucraftcore.superpowers.ModuleSuperpowers;
import lucraft.mods.lucraftcore.util.helper.LCRenderHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.entity.EntityLivingBase;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class AbilityInvisibility extends AbilityToggle {

    public AbilityInvisibility(EntityLivingBase entity) {
        super(entity);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void drawIcon(Minecraft mc, Gui gui, int x, int y) {
        LCRenderHelper.drawIcon(mc, gui, x, y, 1, 6);
    }

    @Override
    public void updateTick() {

    }

    @Mod.EventBusSubscriber(modid = LucraftCore.MODID)
    public static class Renderer {

        @SideOnly(Side.CLIENT)
        @SubscribeEvent
        public static void onRenderLiving(RenderLivingEvent.Pre e) {
            if (!ModuleSuperpowers.INSTANCE.isEnabled())
                return;

            for (AbilityInvisibility ab : Ability.getAbilitiesFromClass(Ability.getAbilities(e.getEntity()), AbilityInvisibility.class)) {
                if (ab != null && ab.isUnlocked() && ab.isEnabled()) {
                    e.setCanceled(true);
                    return;
                }
            }
        }

        @SubscribeEvent
        public static void onVisibility(PlayerEvent.Visibility e) {
            if (!ModuleSuperpowers.INSTANCE.isEnabled())
                return;

            for (AbilityInvisibility ab : Ability.getAbilitiesFromClass(Ability.getAbilities(e.getEntityPlayer()), AbilityInvisibility.class)) {
                if (ab != null && ab.isUnlocked() && ab.isEnabled()) {
                    e.modifyVisibility(0D);
                    return;
                }
            }
        }

    }

}
