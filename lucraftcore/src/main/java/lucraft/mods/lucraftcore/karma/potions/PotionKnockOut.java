package lucraft.mods.lucraftcore.karma.potions;

import lucraft.mods.lucraftcore.LucraftCore;
import lucraft.mods.lucraftcore.karma.ModuleKarma;
import lucraft.mods.lucraftcore.util.helper.LCRenderHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

@EventBusSubscriber(modid = LucraftCore.MODID)
public class PotionKnockOut extends Potion {

    @SubscribeEvent
    public static void onRegisterPotions(RegistryEvent.Register<Potion> e) {
        if (ModuleKarma.INSTANCE.isEnabled()) {
            e.getRegistry().register(POTION_KNOCK_OUT);
        }
    }

    public static PotionKnockOut POTION_KNOCK_OUT = new PotionKnockOut();

    public PotionKnockOut() {
        super(false, 0x4d4d4d);
        this.setPotionName("potion.knockOut");
        this.setRegistryName(new ResourceLocation(LucraftCore.MODID, "knock_out"));
    }

    @Override
    public boolean shouldRender(PotionEffect effect) {
        return true;
    }

    @Override
    public boolean hasStatusIcon() {
        return false;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void renderInventoryEffect(int x, int y, PotionEffect effect, Minecraft mc) {
        if (effect.getPotion() == this) {
            GlStateManager.pushMatrix();
            GlStateManager.enableBlend();
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

            LCRenderHelper.drawIcon(mc, mc.currentScreen, x + 8, y + 8, 0, 9);

            GlStateManager.disableBlend();
            GlStateManager.popMatrix();
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void renderHUDEffect(int x, int y, PotionEffect effect, Minecraft mc, float alpha) {
        if (effect.getPotion() == this) {
            GlStateManager.pushMatrix();
            GlStateManager.enableBlend();
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

            LCRenderHelper.drawIcon(mc, x + 4, y + 4, 0, 9);

            GlStateManager.disableBlend();
            GlStateManager.popMatrix();
        }
    }

}