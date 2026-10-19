package lucraft.mods.heroesexpansion.client.render.item;

import lucraft.mods.heroesexpansion.HeroesExpansion;
import lucraft.mods.heroesexpansion.client.models.ModelCompoundBow;
import lucraft.mods.heroesexpansion.items.ItemCompoundBow;
import lucraft.mods.heroesexpansion.items.ItemHEArrow;
import lucraft.mods.lucraftcore.util.helper.LCRenderHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.tileentity.TileEntityItemStackRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemArrow;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.opengl.GL11;

import java.util.UUID;

public class ItemRendererCompoundBow extends TileEntityItemStackRenderer {

    public static final ResourceLocation BOW_TEX = new ResourceLocation(HeroesExpansion.MODID, "textures/models/compound_bow.png");
    public static final ResourceLocation ENCHANTED_ITEM_GLINT_RES = new ResourceLocation("textures/misc/enchanted_item_glint.png");
    public static final ModelCompoundBow BOW_MODEL = new ModelCompoundBow();

    @Override
    public void renderByItem(ItemStack stack, float partialTicks) {
        Minecraft.getMinecraft().renderEngine.bindTexture(BOW_TEX);
        GlStateManager.translate(0.5F, 0, 0.5F);
        BOW_MODEL.renderModel(0.0625F);
        if (stack.isItemEnchanted())
            renderEnchantedGlint(Minecraft.getMinecraft().player, BOW_MODEL);

        GlStateManager.pushMatrix();
        EntityPlayer entity = stack.hasTagCompound() ? Minecraft.getMinecraft().world.getPlayerEntityByUUID(UUID.fromString(stack.getTagCompound().getString("Owner"))) : null;
        if (entity != null && entity.getActiveItemStack() != stack)
            entity = null;

        float span = entity == null ? 0F : MathHelper.clamp((float) (!entity.getActiveItemStack().isEmpty() ? entity.getActiveItemStack().getMaxItemUseDuration() - entity.getItemInUseCount() : 0) / 20F, 0F, 1F);

        if (entity != null && entity.getItemInUseCount() == 0)
            span = 0;

        Tessellator tes = Tessellator.getInstance();
        BufferBuilder vb = tes.getBuffer();
        GL11.glLineWidth(3);
        GlStateManager.color(0, 0, 0);

        vb.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION);

        vb.pos(0.06D, 1.6D, 0.4D).endVertex();
        vb.pos(0.06D, 0.01D, 0.4D + span / 1.8F).endVertex();

        vb.pos(0.06D, 0.01D, 0.4D + span / 1.8F).endVertex();
        vb.pos(0.06D, -1.35D, 0.4D).endVertex();

        tes.draw();

        if (span > 0F && entity != null) {
            GlStateManager.scale(1.5F, 1.5F, 1.5F);
            GlStateManager.translate(0.05D, 0, -0.45D + span * 0.36D);
            GlStateManager.rotate(180, 0, 1, 0);
            RenderManager rendermanager = Minecraft.getMinecraft().getRenderManager();
            ItemStack arrow = ((ItemCompoundBow) entity.getActiveItemStack().getItem()).findAmmo((EntityPlayer) entity).first();
//		        ItemStack arrow = new ItemStack(HEItems.EXPLOSIVE_ARROW);
            if (arrow.isEmpty())
                arrow = new ItemStack(Items.ARROW);
            if (arrow.getItem() instanceof ItemHEArrow) {
                ((ItemHEArrow) arrow.getItem()).type.renderArrow(null, false, true, ((ItemHEArrow) arrow.getItem()).getColor(arrow), Minecraft.getMinecraft());
            } else {
                GlStateManager.translate(0, 0.02D, -0.1D);
                Entity arrowEntity = ((ItemArrow) Items.ARROW).createArrow(entity.world, arrow, entity);
                rendermanager.renderEntity(arrowEntity, 0.0D, 0.0D, 0.0D, 0.0F, 1.0F, false);
            }
        }

        GlStateManager.popMatrix();
    }

    public static void renderEnchantedGlint(EntityLivingBase entity, ModelCompoundBow model) {
        float f = (float) entity.ticksExisted + LCRenderHelper.renderTick;
        Minecraft.getMinecraft().renderEngine.bindTexture(ENCHANTED_ITEM_GLINT_RES);
        GlStateManager.enableBlend();
        GlStateManager.depthFunc(514);
        GlStateManager.depthMask(false);
        GlStateManager.color(0.5F, 0.5F, 0.5F, 1.0F);

        for (int i = 0; i < 2; ++i) {
            GlStateManager.disableLighting();
            GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_COLOR, GlStateManager.DestFactor.ONE);
            GlStateManager.color(0.38F, 0.19F, 0.608F, 1.0F);
            GlStateManager.matrixMode(5890);
            GlStateManager.loadIdentity();
            GlStateManager.scale(0.33333334F, 0.33333334F, 0.33333334F);
            GlStateManager.rotate(30.0F - (float) i * 60.0F, 0.0F, 0.0F, 1.0F);
            GlStateManager.translate(0.0F, f * (0.001F + (float) i * 0.003F) * 20.0F, 0.0F);
            GlStateManager.matrixMode(5888);
            model.renderModel(0.0625F);
        }

        GlStateManager.matrixMode(5890);
        GlStateManager.loadIdentity();
        GlStateManager.matrixMode(5888);
        GlStateManager.enableLighting();
        GlStateManager.depthMask(true);
        GlStateManager.depthFunc(515);
        GlStateManager.disableBlend();
    }

}
