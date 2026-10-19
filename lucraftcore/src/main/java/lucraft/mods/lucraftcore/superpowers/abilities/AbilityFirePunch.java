package lucraft.mods.lucraftcore.superpowers.abilities;

import lucraft.mods.lucraftcore.superpowers.abilities.data.AbilityData;
import lucraft.mods.lucraftcore.superpowers.abilities.data.AbilityDataInteger;
import lucraft.mods.lucraftcore.superpowers.abilities.supplier.EnumSync;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumParticleTypes;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Random;

public class AbilityFirePunch extends AbilityToggle {

    public static final AbilityData<Integer> DURATION = new AbilityDataInteger("duration").disableSaving().setSyncType(EnumSync.NONE).enableSetting("duration", "Sets for how long a hurt entity will be on fire");

    public AbilityFirePunch(EntityLivingBase entity) {
        super(entity);
    }

    @Override
    public void registerData() {
        super.registerData();
        this.dataManager.register(DURATION, 20);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void drawIcon(Minecraft mc, Gui gui, int x, int y) {
        ItemStack stack = new ItemStack(Items.BLAZE_POWDER);
        float zLevel = Minecraft.getMinecraft().getRenderItem().zLevel;
        Minecraft.getMinecraft().getRenderItem().zLevel = -100.5F;
        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, 0);
        Minecraft.getMinecraft().getRenderItem().renderItemIntoGUI(stack, 0, 0);
        GlStateManager.popMatrix();
        Minecraft.getMinecraft().getRenderItem().zLevel = zLevel;
    }

    @Override
    public void updateTick() {
        Random rand = new Random();
        for (int j = 0; j < 1; ++j) {
            this.entity.world.spawnParticle(EnumParticleTypes.FLAME, entity.posX + (rand.nextDouble() - 0.5D) * (double) entity.width,
                    entity.posY + rand.nextDouble() * (double) entity.height, entity.posZ + (rand.nextDouble() - 0.5D) * (double) entity.width, 0, 0, 0);
        }
    }

    @Override
    public void onHurt(LivingHurtEvent e) {
        if (isEnabled()) {
            e.getEntityLiving().setFire(this.dataManager.get(DURATION));
        }
    }

}
