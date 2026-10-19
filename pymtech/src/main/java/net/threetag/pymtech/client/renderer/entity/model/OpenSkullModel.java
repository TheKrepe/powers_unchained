package net.threetag.pymtech.client.renderer.entity.model;// Made with Blockbench 3.6.6

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.Model;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.util.ResourceLocation;

import java.util.function.Function;

public class OpenSkullModel extends Model {

    public static final OpenSkullModel INSTANCE = new OpenSkullModel(RenderType::getEntityTranslucent);

    private final ModelRenderer head;
    private final ModelRenderer headWear;

    public OpenSkullModel(Function<ResourceLocation, RenderType> renderTypeIn) {
        super(renderTypeIn);
        textureWidth = 64;
        textureHeight = 64;

        head = new ModelRenderer(this);
        head.setRotationPoint(0.0F, -2.4F, 0.0F);
        head.setTextureOffset(0, 0).addBox(-4.0F, 2.4F, -4.0F, 8.0F, 0.0F, 8.0F, 0.0F, false);
        head.setTextureOffset(8, 10).addBox(-4.0F, -3.6F, -4.0F, 8.0F, 6.0F, 0.0F, 0.0F, false);
        head.setTextureOffset(16, 10).addBox(-4.0F, -3.6F, 4.0F, 8.0F, 6.0F, 0.0F, 0.0F, false);
        head.setTextureOffset(0, 2).addBox(-4.0F, -3.6F, -4.0F, 0.0F, 6.0F, 8.0F, 0.0F, false);
        head.setTextureOffset(24, 2).addBox(4.0F, -3.6F, -4.0F, 0.0F, 6.0F, 8.0F, 0.0F, false);

        headWear = new ModelRenderer(this);
        headWear.setRotationPoint(0.0F, -2.4F, 0.0F);
        headWear.setTextureOffset(32, 0).addBox(-4.0F, 2.4F, -4.0F, 8.0F, 0.0F, 8.0F, 0.5F, false);
        headWear.setTextureOffset(40, 10).addBox(-4.0F, -3.6F, -4.0F, 8.0F, 6.0F, 0.0F, 0.5F, false);
        headWear.setTextureOffset(48, 10).addBox(-4.0F, -3.6F, 4.0F, 8.0F, 6.0F, 0.0F, 0.5F, false);
        headWear.setTextureOffset(32, 2).addBox(-4.0F, -3.6F, -4.0F, 0.0F, 6.0F, 8.0F, 0.5F, false);
        headWear.setTextureOffset(48, 2).addBox(4.0F, -3.6F, -4.0F, 0.0F, 6.0F, 8.0F, 0.5F, false);
    }

    @Override
    public void render(MatrixStack matrixStack, IVertexBuilder buffer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        head.render(matrixStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
//        headWear.render(matrixStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);

    }
}