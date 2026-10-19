package lucraft.mods.heroesexpansion.potions;

import lucraft.mods.heroesexpansion.HeroesExpansion;
import lucraft.mods.heroesexpansion.items.HEItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

@EventBusSubscriber(modid = HeroesExpansion.MODID)
public class PotionKryptonitePoison extends Potion {

    public static PotionKryptonitePoison KRYPTONITE_POISON = new PotionKryptonitePoison();

    @SubscribeEvent
    public static void onRegisterPotions(RegistryEvent.Register<Potion> e) {
        e.getRegistry().register(KRYPTONITE_POISON);
    }

    //------------------------------------------------------------------------------

    protected PotionKryptonitePoison() {
        super(true, 0x00cc00);
        this.setPotionName("potion.kryptonite_poison");
        this.setRegistryName(HeroesExpansion.MODID, "kryptonite_poison");
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void renderInventoryEffect(int x, int y, PotionEffect effect, Minecraft mc) {
        if (effect.getPotion() == this) {
            GlStateManager.pushMatrix();
            GlStateManager.enableBlend();
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

            mc.getRenderItem().renderItemIntoGUI(new ItemStack(HEItems.KRYPTONITE), x + 8, y + 8);

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

            mc.getRenderItem().renderItemIntoGUI(new ItemStack(HEItems.KRYPTONITE), x + 4, y + 4);

            GlStateManager.disableBlend();
            GlStateManager.popMatrix();
        }
    }

}
