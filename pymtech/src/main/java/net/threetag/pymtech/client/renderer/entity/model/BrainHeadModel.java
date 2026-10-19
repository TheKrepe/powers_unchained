package net.threetag.pymtech.client.renderer.entity.model;// Made with Blockbench 3.6.6
// Exported for Minecraft version 1.15
// Paste this class into your mod and generate all required imports


import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.Model;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.util.ResourceLocation;
import net.threetag.pymtech.PymTech;

import java.util.function.Function;

public class BrainHeadModel extends Model {

    public static final BrainHeadModel INSTANCE = new BrainHeadModel(RenderType::getEntityTranslucent);
    public static final ResourceLocation TEXTURE = new ResourceLocation(PymTech.MODID, "textures/entity/brain_head.png");

    private final ModelRenderer bipedHead;

    public BrainHeadModel(Function<ResourceLocation, RenderType> renderTypeIn) {
        super(renderTypeIn);
        textureWidth = 32;
        textureHeight = 16;

        bipedHead = new ModelRenderer(this);
        bipedHead.setRotationPoint(0.0F, 0.0F, 0.0F);
        bipedHead.setTextureOffset(4, 7).addBox(-3.5F, -7.5F, -3.5F, 7.0F, 2.0F, 7.0F, 0.0F, false);
        bipedHead.setTextureOffset(-8, 0).addBox(-4.0F, -6.0F, -4.0F, 8.0F, 0.0F, 8.0F, 0.0F, false);
    }

    @Override
    public void render(MatrixStack matrixStack, IVertexBuilder buffer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        bipedHead.render(matrixStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
    }
}