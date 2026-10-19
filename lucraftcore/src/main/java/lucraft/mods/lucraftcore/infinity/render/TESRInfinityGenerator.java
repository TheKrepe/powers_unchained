package lucraft.mods.lucraftcore.infinity.render;

import lucraft.mods.lucraftcore.LCConfig;
import lucraft.mods.lucraftcore.LucraftCore;
import lucraft.mods.lucraftcore.infinity.EnumInfinityStone;
import lucraft.mods.lucraftcore.infinity.blocks.TileEntityInfinityGenerator;
import lucraft.mods.lucraftcore.infinity.items.ItemInfinityStone;
import lucraft.mods.lucraftcore.util.helper.LCRenderHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.tileentity.TileEntityBeaconRenderer;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.tileentity.TileEntityBeacon;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TESRInfinityGenerator extends TileEntitySpecialRenderer<TileEntityInfinityGenerator> {

    public static ResourceLocation SPACE = new ResourceLocation(LucraftCore.MODID, "textures/blocks/infinity_generator_overlay_space.png");
    public static ResourceLocation TIME = new ResourceLocation(LucraftCore.MODID, "textures/blocks/infinity_generator_overlay_time.png");
    public static ResourceLocation SOUL = new ResourceLocation(LucraftCore.MODID, "textures/blocks/infinity_generator_overlay_soul.png");
    public static ResourceLocation REALITY = new ResourceLocation(LucraftCore.MODID, "textures/blocks/infinity_generator_overlay_reality.png");
    public static ResourceLocation POWER = new ResourceLocation(LucraftCore.MODID, "textures/blocks/infinity_generator_overlay_power.png");
    public static ResourceLocation MIND = new ResourceLocation(LucraftCore.MODID, "textures/blocks/infinity_generator_overlay_mind.png");

    @Override
    public boolean isGlobalRenderer(TileEntityInfinityGenerator te)
    {
        return true;
    }

    @Override
    public void render(TileEntityInfinityGenerator te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        IItemHandler itemHandler = te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
        if (!itemHandler.getStackInSlot(0).isEmpty() && itemHandler.getStackInSlot(0).getItem() instanceof ItemInfinityStone) {
            EnumInfinityStone type = ((ItemInfinityStone) itemHandler.getStackInSlot(0).getItem()).getType();

            if(LCConfig.infinity.renderInfinityGeneratorBeam)
                this.renderBeacon(x, y, z, (double) partialTicks, 1D, getBeamSegments(type), (double) te.getWorld().getTotalWorldTime());

            GlStateManager.pushMatrix();
            GlStateManager.translate(x, y, z);
            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder buffer = tessellator.getBuffer();
            GlStateManager.disableLighting();
            LCRenderHelper.setLightmapTextureCoords(240, 240);
            Minecraft.getMinecraft().renderEngine.bindTexture(getOverlayTexture(type));
            GlStateManager.disableCull();
            float f = 0.001F;
            float f1 = 1F / 16F;

            buffer.begin(GL11.GL_POLYGON, DefaultVertexFormats.POSITION_TEX);
            buffer.pos(0, 0, -f).tex(0, 1).endVertex();
            buffer.pos(1, 0, -f).tex(1, 1).endVertex();
            buffer.pos(1, 1, -f).tex(1, 0).endVertex();
            buffer.pos(0, 1, -f).tex(0, 0).endVertex();
            tessellator.draw();

            buffer.begin(GL11.GL_POLYGON, DefaultVertexFormats.POSITION_TEX);
            buffer.pos(0, 0, 1 + f).tex(0, 1).endVertex();
            buffer.pos(1, 0, 1 + f).tex(1, 1).endVertex();
            buffer.pos(1, 1, 1 + f).tex(1, 0).endVertex();
            buffer.pos(0, 1, 1 + f).tex(0, 0).endVertex();
            tessellator.draw();

            buffer.begin(GL11.GL_POLYGON, DefaultVertexFormats.POSITION_TEX);
            buffer.pos(-f, 0, 0).tex(0, 1).endVertex();
            buffer.pos(-f, 0, 1).tex(1, 1).endVertex();
            buffer.pos(-f, 1, 1).tex(1, 0).endVertex();
            buffer.pos(-f, 1, 0).tex(0, 0).endVertex();
            tessellator.draw();

            buffer.begin(GL11.GL_POLYGON, DefaultVertexFormats.POSITION_TEX);
            buffer.pos(1 + f, 0, 0).tex(0, 1).endVertex();
            buffer.pos(1 + f, 0, 1).tex(1, 1).endVertex();
            buffer.pos(1 + f, 1, 1).tex(1, 0).endVertex();
            buffer.pos(1 + f, 1, 0).tex(0, 0).endVertex();
            tessellator.draw();

            buffer.begin(GL11.GL_POLYGON, DefaultVertexFormats.POSITION_TEX);
            buffer.pos(0, 1 + f, 0).tex(0, 1).endVertex();
            buffer.pos(0, 1 + f, 1).tex(1, 1).endVertex();
            buffer.pos(1, 1 + f, 1).tex(1, 0).endVertex();
            buffer.pos(1, 1 + f, 0).tex(0, 0).endVertex();
            tessellator.draw();

            buffer.begin(GL11.GL_POLYGON, DefaultVertexFormats.POSITION_TEX);
            buffer.pos(0, -f, 0).tex(0, 1).endVertex();
            buffer.pos(0, -f, 1).tex(1, 1).endVertex();
            buffer.pos(1, -f, 1).tex(1, 0).endVertex();
            buffer.pos(1, -f, 0).tex(0, 0).endVertex();
            tessellator.draw();

            LCRenderHelper.setupRenderLightning();
            AxisAlignedBB box = new AxisAlignedBB(f1 * 4, f1 * 4, f1 * 4, f1 * 12, f1 * 12, f1 * 12);
            float thickness = 0.00001F;
            LCRenderHelper.drawRandomLightningCoordsInAABB(thickness, getLightningColor(type), box, new Random(te.getWorld().getTotalWorldTime() / 2));
            LCRenderHelper.finishRenderLightning();

            GlStateManager.translate(0.5F, 0.5F, 0.5F);
            GlStateManager.scale(0.5F, 0.5F, 0.5F);
            GlStateManager.rotate(Minecraft.getMinecraft().player.ticksExisted + partialTicks, 0, 1, 0);
            GlStateManager.rotate(MathHelper.sin((Minecraft.getMinecraft().player.ticksExisted + partialTicks) / 10F) * 10F, 1, 0, 0);
            Minecraft.getMinecraft().getRenderItem().renderItem(itemHandler.getStackInSlot(0), ItemCameraTransforms.TransformType.FIXED);

            LCRenderHelper.restoreLightmapTextureCoords();
            GlStateManager.enableLighting();
            GlStateManager.popMatrix();
        }
    }

    public static ResourceLocation getOverlayTexture(EnumInfinityStone type) {
        switch (type) {
            case SPACE:
                return SPACE;
            case MIND:
                return MIND;
            case POWER:
                return POWER;
            case REALITY:
                return REALITY;
            case SOUL:
                return SOUL;
            case TIME:
                return TIME;
            default:
                return null;
        }
    }

    public static Color getLightningColor(EnumInfinityStone type) {
        switch (type) {
            case SPACE:
                return new Color(2, 85, 255);
            case MIND:
                return Color.YELLOW;
            case POWER:
                return new Color(1F, 0F, 1F);
            case REALITY:
                return Color.RED;
            case SOUL:
                return new Color(1F, 0.6F, 0F);
            case TIME:
                return new Color(0.6F, 1F, 0F);
            default:
                return null;
        }
    }

    public void renderBeacon(double x, double y, double z, double partialTicks, double textureScale, List<TileEntityBeacon.BeamSegment> beamSegments, double totalWorldTime) {
        GlStateManager.alphaFunc(516, 0.1F);
        this.bindTexture(TileEntityBeaconRenderer.TEXTURE_BEACON_BEAM);

        if (textureScale > 0.0D) {
            GlStateManager.disableFog();
            int i = 0;

            for (int j = 0; j < beamSegments.size(); ++j) {
                TileEntityBeacon.BeamSegment tileentitybeacon$beamsegment = beamSegments.get(j);
                renderBeamSegment(x, y, z, partialTicks, textureScale, totalWorldTime, i, tileentitybeacon$beamsegment.getHeight(), tileentitybeacon$beamsegment.getColors());
                i += tileentitybeacon$beamsegment.getHeight();
            }

            GlStateManager.enableFog();
        }
    }

    public static void renderBeamSegment(double x, double y, double z, double partialTicks, double textureScale, double totalWorldTime, int yOffset, int height, float[] colors) {
        renderBeamSegment(x, y, z, partialTicks, textureScale, totalWorldTime, yOffset, height, colors, 0.2D, 0.25D);
    }

    public static void renderBeamSegment(double x, double y, double z, double partialTicks, double textureScale, double totalWorldTime, int yOffset, int height, float[] colors, double beamRadius, double glowRadius) {
        int i = yOffset + height;
        GlStateManager.glTexParameteri(3553, 10242, 10497);
        GlStateManager.glTexParameteri(3553, 10243, 10497);
        GlStateManager.disableLighting();
        GlStateManager.disableCull();
        GlStateManager.disableBlend();
        GlStateManager.depthMask(true);
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        double d0 = totalWorldTime + partialTicks;
        double d1 = height < 0 ? d0 : -d0;
        double d2 = MathHelper.frac(d1 * 0.2D - (double) MathHelper.floor(d1 * 0.1D));
        float f = colors[0];
        float f1 = colors[1];
        float f2 = colors[2];
        double d3 = d0 * 0.025D * -1.5D;
        double d4 = 0.5D + Math.cos(d3 + 2.356194490192345D) * beamRadius;
        double d5 = 0.5D + Math.sin(d3 + 2.356194490192345D) * beamRadius;
        double d6 = 0.5D + Math.cos(d3 + (Math.PI / 4D)) * beamRadius;
        double d7 = 0.5D + Math.sin(d3 + (Math.PI / 4D)) * beamRadius;
        double d8 = 0.5D + Math.cos(d3 + 3.9269908169872414D) * beamRadius;
        double d9 = 0.5D + Math.sin(d3 + 3.9269908169872414D) * beamRadius;
        double d10 = 0.5D + Math.cos(d3 + 5.497787143782138D) * beamRadius;
        double d11 = 0.5D + Math.sin(d3 + 5.497787143782138D) * beamRadius;
        double d12 = 0.0D;
        double d13 = 1.0D;
        double d14 = -1.0D + d2;
        double d15 = (double) height * textureScale * (0.5D / beamRadius) + d14;
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
        bufferbuilder.pos(x + d4, y + (double) i, z + d5).tex(1.0D, d15).color(f, f1, f2, 1.0F).endVertex();
        bufferbuilder.pos(x + d4, y + (double) yOffset, z + d5).tex(1.0D, d14).color(f, f1, f2, 1.0F).endVertex();
        bufferbuilder.pos(x + d6, y + (double) yOffset, z + d7).tex(0.0D, d14).color(f, f1, f2, 1.0F).endVertex();
        bufferbuilder.pos(x + d6, y + (double) i, z + d7).tex(0.0D, d15).color(f, f1, f2, 1.0F).endVertex();
        bufferbuilder.pos(x + d10, y + (double) i, z + d11).tex(1.0D, d15).color(f, f1, f2, 1.0F).endVertex();
        bufferbuilder.pos(x + d10, y + (double) yOffset, z + d11).tex(1.0D, d14).color(f, f1, f2, 1.0F).endVertex();
        bufferbuilder.pos(x + d8, y + (double) yOffset, z + d9).tex(0.0D, d14).color(f, f1, f2, 1.0F).endVertex();
        bufferbuilder.pos(x + d8, y + (double) i, z + d9).tex(0.0D, d15).color(f, f1, f2, 1.0F).endVertex();
        bufferbuilder.pos(x + d6, y + (double) i, z + d7).tex(1.0D, d15).color(f, f1, f2, 1.0F).endVertex();
        bufferbuilder.pos(x + d6, y + (double) yOffset, z + d7).tex(1.0D, d14).color(f, f1, f2, 1.0F).endVertex();
        bufferbuilder.pos(x + d10, y + (double) yOffset, z + d11).tex(0.0D, d14).color(f, f1, f2, 1.0F).endVertex();
        bufferbuilder.pos(x + d10, y + (double) i, z + d11).tex(0.0D, d15).color(f, f1, f2, 1.0F).endVertex();
        bufferbuilder.pos(x + d8, y + (double) i, z + d9).tex(1.0D, d15).color(f, f1, f2, 1.0F).endVertex();
        bufferbuilder.pos(x + d8, y + (double) yOffset, z + d9).tex(1.0D, d14).color(f, f1, f2, 1.0F).endVertex();
        bufferbuilder.pos(x + d4, y + (double) yOffset, z + d5).tex(0.0D, d14).color(f, f1, f2, 1.0F).endVertex();
        bufferbuilder.pos(x + d4, y + (double) i, z + d5).tex(0.0D, d15).color(f, f1, f2, 1.0F).endVertex();
        tessellator.draw();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.depthMask(false);
        d3 = 0.5D - glowRadius;
        d4 = 0.5D - glowRadius;
        d5 = 0.5D + glowRadius;
        d6 = 0.5D - glowRadius;
        d7 = 0.5D - glowRadius;
        d8 = 0.5D + glowRadius;
        d9 = 0.5D + glowRadius;
        d10 = 0.5D + glowRadius;
        d11 = 0.0D;
        d12 = 1.0D;
        d13 = -1.0D + d2;
        d14 = (double) height * textureScale + d13;
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
        bufferbuilder.pos(x + d3, y + (double) i, z + d4).tex(1.0D, d14).color(f, f1, f2, 0.125F).endVertex();
        bufferbuilder.pos(x + d3, y + (double) yOffset, z + d4).tex(1.0D, d13).color(f, f1, f2, 0.125F).endVertex();
        bufferbuilder.pos(x + d5, y + (double) yOffset, z + d6).tex(0.0D, d13).color(f, f1, f2, 0.125F).endVertex();
        bufferbuilder.pos(x + d5, y + (double) i, z + d6).tex(0.0D, d14).color(f, f1, f2, 0.125F).endVertex();
        bufferbuilder.pos(x + d9, y + (double) i, z + d10).tex(1.0D, d14).color(f, f1, f2, 0.125F).endVertex();
        bufferbuilder.pos(x + d9, y + (double) yOffset, z + d10).tex(1.0D, d13).color(f, f1, f2, 0.125F).endVertex();
        bufferbuilder.pos(x + d7, y + (double) yOffset, z + d8).tex(0.0D, d13).color(f, f1, f2, 0.125F).endVertex();
        bufferbuilder.pos(x + d7, y + (double) i, z + d8).tex(0.0D, d14).color(f, f1, f2, 0.125F).endVertex();
        bufferbuilder.pos(x + d5, y + (double) i, z + d6).tex(1.0D, d14).color(f, f1, f2, 0.125F).endVertex();
        bufferbuilder.pos(x + d5, y + (double) yOffset, z + d6).tex(1.0D, d13).color(f, f1, f2, 0.125F).endVertex();
        bufferbuilder.pos(x + d9, y + (double) yOffset, z + d10).tex(0.0D, d13).color(f, f1, f2, 0.125F).endVertex();
        bufferbuilder.pos(x + d9, y + (double) i, z + d10).tex(0.0D, d14).color(f, f1, f2, 0.125F).endVertex();
        bufferbuilder.pos(x + d7, y + (double) i, z + d8).tex(1.0D, d14).color(f, f1, f2, 0.125F).endVertex();
        bufferbuilder.pos(x + d7, y + (double) yOffset, z + d8).tex(1.0D, d13).color(f, f1, f2, 0.125F).endVertex();
        bufferbuilder.pos(x + d3, y + (double) yOffset, z + d4).tex(0.0D, d13).color(f, f1, f2, 0.125F).endVertex();
        bufferbuilder.pos(x + d3, y + (double) i, z + d4).tex(0.0D, d14).color(f, f1, f2, 0.125F).endVertex();
        tessellator.draw();
        GlStateManager.enableLighting();
        GlStateManager.enableTexture2D();
        GlStateManager.depthMask(true);
    }

    public List<TileEntityBeacon.BeamSegment> getBeamSegments(EnumInfinityStone stone) {
        List<TileEntityBeacon.BeamSegment> list = new ArrayList<>();
        for (int i = 0; i < 256; i++) {
            list.add(new TileEntityBeacon.BeamSegment(getLightningColor(stone).getRGBComponents(null)));
        }
        return list;
    }

}
