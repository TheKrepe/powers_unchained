package lucraft.mods.heroesexpansion.client.render.item;

import lucraft.mods.heroesexpansion.HeroesExpansion;
import lucraft.mods.heroesexpansion.client.models.ModelKreeBattleAxe;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntityItemStackRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public class ItemRendererKreeBattleAxe extends TileEntityItemStackRenderer {

    public static final ResourceLocation TEXTURE = new ResourceLocation(HeroesExpansion.MODID, "textures/models/kree_battle_axe.png");
    public static final ModelKreeBattleAxe MODEL = new ModelKreeBattleAxe();

    @Override
    public void renderByItem(ItemStack stack, float partialTicks) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(0.5F, 0.5F, 0.5F);
        GlStateManager.rotate(180, 1, 0, 0);
        Minecraft.getMinecraft().renderEngine.bindTexture(TEXTURE);
        MODEL.renderModel(0.0625F);
        GlStateManager.popMatrix();
    }
}
