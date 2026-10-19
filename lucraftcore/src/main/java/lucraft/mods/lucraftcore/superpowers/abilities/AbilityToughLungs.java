package lucraft.mods.lucraftcore.superpowers.abilities;

import lucraft.mods.lucraftcore.LucraftCore;
import micdoodle8.mods.galacticraft.api.event.oxygen.GCCoreOxygenSuffocationEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class AbilityToughLungs extends AbilityConstant {

    public AbilityToughLungs(EntityLivingBase entity) {
        super(entity);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void drawIcon(Minecraft mc, Gui gui, int x, int y) {
        Item item = Item.REGISTRY.containsKey(new ResourceLocation("galacticraftcore:oxygen_mask")) ?
                Item.REGISTRY.getObject(new ResourceLocation("galacticraftcore:oxygen_mask")) :
                Item.getItemFromBlock(Blocks.BARRIER);
        float zLevel = Minecraft.getMinecraft().getRenderItem().zLevel;
        Minecraft.getMinecraft().getRenderItem().zLevel = -100.5F;
        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, 0);
        Minecraft.getMinecraft().getRenderItem().renderItemIntoGUI(new ItemStack(item), 0, 0);
        GlStateManager.popMatrix();
        Minecraft.getMinecraft().getRenderItem().zLevel = zLevel;
    }

    @Override
    public void updateTick() {

    }

    @Mod.EventBusSubscriber(modid = LucraftCore.MODID)
    public static class EventHandler {

        @SubscribeEvent
        @Optional.Method(modid = "galacticraftcore")
        public static void onSuffocate(GCCoreOxygenSuffocationEvent.Pre e) {
            for (AbilityToughLungs ability : Ability.getAbilitiesFromClass(Ability.getAbilities(e.getEntityLiving()), AbilityToughLungs.class)) {
                if (ability != null && ability.isUnlocked()) {
                    e.setCanceled(true);
                }
            }
        }

    }

}
