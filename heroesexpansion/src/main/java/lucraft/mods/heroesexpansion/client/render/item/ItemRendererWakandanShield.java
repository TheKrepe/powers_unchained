package lucraft.mods.heroesexpansion.client.render.item;

import lucraft.mods.heroesexpansion.HeroesExpansion;
import lucraft.mods.heroesexpansion.client.models.ModelWakandanShieldClosed;
import lucraft.mods.heroesexpansion.client.models.ModelWakandanShieldOpened;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntityItemStackRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public class ItemRendererWakandanShield extends TileEntityItemStackRenderer {

    public static final ModelWakandanShieldClosed MODEL_CLOSED = new ModelWakandanShieldClosed();
    public static final ModelWakandanShieldOpened MODEL_OPENED = new ModelWakandanShieldOpened();
    public static final ResourceLocation TEXTURE = new ResourceLocation(HeroesExpansion.MODID, "textures/models/wakandan_shield.png");

    @Override
    public void renderByItem(ItemStack itemStackIn, float partialTicks) {
        GlStateManager.translate(0.5F, 0.35F, 0.3F);
        GlStateManager.rotate(180F, 1, 0, 0);
        Minecraft.getMinecraft().getTextureManager().bindTexture(new ResourceLocation(HeroesExpansion.MODID, "textures/models/wakandan_shield.png"));

        if (itemStackIn.hasTagCompound() && itemStackIn.getTagCompound().getBoolean("Open"))
            MODEL_OPENED.render(null, 0, 0, 0, 0, 0, 0.0625F);
        else
            MODEL_CLOSED.render(null, 0, 0, 0, 0, 0, 0.0625F);
    }
}
