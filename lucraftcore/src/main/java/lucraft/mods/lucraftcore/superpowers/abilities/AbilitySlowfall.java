package lucraft.mods.lucraftcore.superpowers.abilities;

import lucraft.mods.lucraftcore.util.helper.PlayerHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumParticleTypes;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class AbilitySlowfall extends AbilityToggle {

    public AbilitySlowfall(EntityLivingBase entity) {
        super(entity);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void drawIcon(Minecraft mc, Gui gui, int x, int y) {
        float zLevel = Minecraft.getMinecraft().getRenderItem().zLevel;
        Minecraft.getMinecraft().getRenderItem().zLevel = -100.5F;
        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, 0);
        Minecraft.getMinecraft().getRenderItem().renderItemIntoGUI(new ItemStack(Items.FEATHER), 0, 0);
        GlStateManager.popMatrix();
        Minecraft.getMinecraft().getRenderItem().zLevel = zLevel;
    }

    @Override
    public void updateTick() {
        if (!entity.onGround && entity.motionY < 0.0D) {
            entity.motionY *= 0.6D;
            entity.fallDistance = 0F;
            PlayerHelper.spawnParticleForAll(
                    entity.world, 50, EnumParticleTypes.CLOUD, true, (float) entity.posX, (float) entity.posY, (float) entity.posZ,
                    (entity.world.rand.nextFloat() - 0.5F), -entity.world.rand.nextFloat(), (
                            entity.world.rand.nextFloat() - 0.5F), 0.1F, 1);
        }
    }

}
