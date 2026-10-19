package lucraft.mods.lucraftcore.superpowers.abilities;

import lucraft.mods.lucraftcore.superpowers.abilities.data.AbilityData;
import lucraft.mods.lucraftcore.superpowers.abilities.data.AbilityDataInteger;
import lucraft.mods.lucraftcore.superpowers.abilities.data.AbilityDataPotion;
import lucraft.mods.lucraftcore.superpowers.abilities.supplier.EnumSync;
import lucraft.mods.lucraftcore.util.helper.StringHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.potion.PotionUtils;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Arrays;
import java.util.Random;

public class AbilityPotionPunch extends AbilityToggle {

    public static AbilityData<Potion> POTION = new AbilityDataPotion("potion").disableSaving().setSyncType(EnumSync.SELF).enableSetting("potion", "Sets the actual potion");
    public static AbilityData<Integer> AMPLIFIER = new AbilityDataInteger("amplifier").disableSaving().setSyncType(EnumSync.NONE).enableSetting("amplifier", "Sets the amplifier for the potion effect");
    public static AbilityData<Integer> DURATION = new AbilityDataInteger("duration").disableSaving().setSyncType(EnumSync.NONE).enableSetting("duration", "Sets the duration for the potion effect");

    public AbilityPotionPunch(EntityLivingBase entity) {
        super(entity);
    }

    @Override
    public void registerData() {
        super.registerData();
        this.dataManager.register(POTION, MobEffects.POISON);
        this.dataManager.register(AMPLIFIER, 0);
        this.dataManager.register(DURATION, 20);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void drawIcon(Minecraft mc, Gui gui, int x, int y) {
        ItemStack stack = new ItemStack(Items.POTIONITEM);
        stack = PotionUtils.appendEffects(stack, Arrays.asList(new PotionEffect(this.dataManager.get(POTION), 0, 20)));
        float zLevel = Minecraft.getMinecraft().getRenderItem().zLevel;
        Minecraft.getMinecraft().getRenderItem().zLevel = -100.5F;
        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, 0);
        Minecraft.getMinecraft().getRenderItem().renderItemIntoGUI(stack, 0, 0);
        GlStateManager.popMatrix();
        Minecraft.getMinecraft().getRenderItem().zLevel = zLevel;
    }

    @Override
    public String getDisplayDescription() {
        return super.getDisplayDescription() + "\n \n" + TextFormatting.BLUE + StringHelper.translateToLocal(this.dataManager.get(POTION).getName());
    }

    @Override
    public void updateTick() {
        spawnPotionParticles(1);
    }

    private void spawnPotionParticles(int particleCount) {
        int i = this.dataManager.get(POTION).getLiquidColor();
        Random rand = new Random();
        if (i != -1 && particleCount > 0) {
            double d0 = (double) (i >> 16 & 255) / 255.0D;
            double d1 = (double) (i >> 8 & 255) / 255.0D;
            double d2 = (double) (i >> 0 & 255) / 255.0D;

            for (int j = 0; j < particleCount; ++j) {
                this.entity.world.spawnParticle(EnumParticleTypes.SPELL_MOB, entity.posX + (rand.nextDouble() - 0.5D) * (double) entity.width,
                        entity.posY + rand.nextDouble() * (double) entity.height, entity.posZ + (rand.nextDouble() - 0.5D) * (double) entity.width, d0, d1, d2);
            }
        }
    }

    @Override
    public void onHurt(LivingHurtEvent e) {
        if (isEnabled()) {
            e.getEntityLiving().addPotionEffect(new PotionEffect(this.dataManager.get(POTION), this.dataManager.get(DURATION), this.dataManager.get(AMPLIFIER)));
        }
    }

}
