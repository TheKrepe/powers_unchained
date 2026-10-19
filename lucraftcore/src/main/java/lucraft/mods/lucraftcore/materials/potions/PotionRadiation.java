package lucraft.mods.lucraftcore.materials.potions;

import lucraft.mods.lucraftcore.LucraftCore;
import lucraft.mods.lucraftcore.materials.Material;
import lucraft.mods.lucraftcore.materials.Material.MaterialComponent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

public class PotionRadiation extends Potion {

    public static DamageSource RADIATION = new DamageSource("radiation").setDamageBypassesArmor();

    public PotionRadiation() {
        super(false, 0x4d4d4d);
        this.setPotionName("potion.radiation");
        this.setRegistryName(new ResourceLocation(LucraftCore.MODID, "radiation"));
    }

    @Override
    public boolean isBadEffect() {
        return true;
    }

    @Override
    public boolean shouldRender(PotionEffect effect) {
        return true;
    }

    @Override
    public boolean hasStatusIcon() {
        return false;
    }

    @Override
    public void performEffect(EntityLivingBase entityLivingBaseIn, int p_76394_2_) {
        entityLivingBaseIn.attackEntityFrom(RADIATION, 3.0F);
    }

    @Override
    public boolean isReady(int p_76397_1_, int p_76397_2_) {
        int j = 25 >> p_76397_2_;
        return j > 0 ? p_76397_1_ % j == 0 : true;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void renderInventoryEffect(int x, int y, PotionEffect effect, Minecraft mc) {
        if (effect.getPotion() == this) {
            GlStateManager.pushMatrix();
            GlStateManager.enableBlend();
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

            mc.getRenderItem().renderItemIntoGUI(Material.URANIUM.getItemStack(MaterialComponent.INGOT), x + 8, y + 8);

            GlStateManager.disableBlend();
            GlStateManager.popMatrix();
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void renderHUDEffect(int x, int y, PotionEffect effect, Minecraft mc, float alpha) {
        if (effect.getPotion() == this) {
            GlStateManager.pushMatrix();
            GlStateManager.enableBlend();
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

            mc.getRenderItem().renderItemIntoGUI(Material.URANIUM.getItemStack(MaterialComponent.INGOT), x + 4, y + 4);

            GlStateManager.disableBlend();
            GlStateManager.popMatrix();
        }
    }

}
