package lucraft.mods.heroesexpansion.client.models;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

public class ModelMjolnir extends ModelBase {

    public ModelRenderer headMain;
    public ModelRenderer backBottom;
    public ModelRenderer backUp;
    public ModelRenderer backLeft;
    public ModelRenderer backRight;
    public ModelRenderer backMain;
    public ModelRenderer upMain;
    public ModelRenderer upRight;
    public ModelRenderer upLeft;
    public ModelRenderer bottomMain;
    public ModelRenderer upDetail;
    public ModelRenderer upDetail2;
    public ModelRenderer frontLeft;
    public ModelRenderer frontRight;
    public ModelRenderer frontUp;
    public ModelRenderer frontBottom;
    public ModelRenderer frontMain;
    public ModelRenderer bottomRight;
    public ModelRenderer bottomLeft;
    public ModelRenderer handle;
    public ModelRenderer rope;
    public ModelRenderer shape22;

    public ModelMjolnir() {
        this(0F);
    }

    public ModelMjolnir(float scale) {
        this.textureWidth = 64;
        this.textureHeight = 32;
        this.upMain = new ModelRenderer(this, 0, 14);
        this.upMain.setRotationPoint(-1.0F, -4.0F, -3.0F);
        this.upMain.addBox(0.0F, 0.0F, 0.0F, 2, 1, 6, scale);
        this.frontMain = new ModelRenderer(this, 0, 24);
        this.frontMain.setRotationPoint(-1.5F, -3.5F, -2.86F);
        this.frontMain.addBox(0.0F, 0.0F, -1.0F, 3, 4, 1, scale);
        this.frontBottom = new ModelRenderer(this, 0, 0);
        this.frontBottom.setRotationPoint(-1.0F, 1.0F, -3.0F);
        this.frontBottom.addBox(0.0F, -1.0F, -1.0F, 2, 1, 1, scale);
        this.setRotateAngle(frontBottom, -0.5375614096142535F, 0.0F, 0.0F);
        this.backMain = new ModelRenderer(this, 0, 24);
        this.backMain.setRotationPoint(-1.5F, -3.5F, 2.86F);
        this.backMain.addBox(0.0F, 0.0F, 0.0F, 3, 4, 1, scale);
        this.headMain = new ModelRenderer(this, 0, 0);
        this.headMain.setRotationPoint(0.0F, 0.5F, 0.0F);
        this.headMain.addBox(-2.0F, -4.0F, -3.0F, 4, 4, 6, scale);
        this.bottomMain = new ModelRenderer(this, 0, 14);
        this.bottomMain.setRotationPoint(-1.0F, 0.0F, -3.0F);
        this.bottomMain.addBox(0.0F, 0.0F, 0.0F, 2, 1, 6, scale);
        this.rope = new ModelRenderer(this, 56, 25);
        this.rope.setRotationPoint(0.0F, 7.5F, 0.0F);
        this.rope.addBox(0.0F, 0.0F, 0.0F, 0, 3, 4, scale);
        this.upRight = new ModelRenderer(this, 30, 0);
        this.upRight.mirror = true;
        this.upRight.setRotationPoint(1.0F, -4.0F, -3.0F);
        this.upRight.addBox(0.0F, 0.0F, 0.0F, 1, 1, 6, scale);
        this.setRotateAngle(upRight, 0.0F, 0.0F, 0.5201081170943103F);
        this.upDetail2 = new ModelRenderer(this, 35, 22);
        this.upDetail2.setRotationPoint(0.0F, -4.1F, 0.0F);
        this.upDetail2.addBox(-0.5F, 0.0F, -0.5F, 1, 6, 1, scale + 0.1F);
        this.backLeft = new ModelRenderer(this, 23, 0);
        this.backLeft.mirror = true;
        this.backLeft.setRotationPoint(2.0F, -3.5F, 3.0F);
        this.backLeft.addBox(-1.0F, 0.0F, 0.0F, 1, 4, 1, scale);
        this.setRotateAngle(backLeft, 0.0F, -0.5375614096142535F, 0.0F);
        this.frontRight = new ModelRenderer(this, 23, 0);
        this.frontRight.mirror = true;
        this.frontRight.setRotationPoint(-2.0F, -3.5F, -3.0F);
        this.frontRight.addBox(0.0F, 0.0F, -1.0F, 1, 4, 1, scale);
        this.setRotateAngle(frontRight, 0.0F, -0.5375614096142535F, 0.0F);
        this.backUp = new ModelRenderer(this, 0, 0);
        this.backUp.setRotationPoint(-1.0F, -4.0F, 3.0F);
        this.backUp.addBox(0.0F, 0.0F, 0.0F, 2, 1, 1, scale);
        this.setRotateAngle(backUp, -0.5375614096142535F, 0.0F, 0.0F);
        this.frontUp = new ModelRenderer(this, 0, 0);
        this.frontUp.setRotationPoint(-1.0F, -4.0F, -3.0F);
        this.frontUp.addBox(0.0F, 0.0F, -1.0F, 2, 1, 1, scale);
        this.setRotateAngle(frontUp, 0.5375614096142535F, 0.0F, 0.0F);
        this.bottomRight = new ModelRenderer(this, 30, 0);
        this.bottomRight.mirror = true;
        this.bottomRight.setRotationPoint(-1.0F, 1.0F, 0.0F);
        this.bottomRight.addBox(-1.0F, -1.0F, -3.0F, 1, 1, 6, scale);
        this.setRotateAngle(bottomRight, 0.0F, 0.0F, 0.5201081170943103F);
        this.bottomLeft = new ModelRenderer(this, 30, 0);
        this.bottomLeft.setRotationPoint(1.0F, 1.0F, 0.0F);
        this.bottomLeft.addBox(0.0F, -1.0F, -3.0F, 1, 1, 6, scale);
        this.setRotateAngle(bottomLeft, 0.0F, 0.0F, -0.5201081170943103F);
        this.shape22 = new ModelRenderer(this, 24, 12);
        this.shape22.setRotationPoint(0.0F, 6.3F, 0.0F);
        this.shape22.addBox(-0.5F, 0.0F, -0.5F, 1, 1, 1, scale + 0.1F);
        this.upDetail = new ModelRenderer(this, 35, 22);
        this.upDetail.setRotationPoint(0.0F, -4.2F, 0.0F);
        this.upDetail.addBox(-0.5F, 0.0F, -0.5F, 1, 1, 1, scale + 0.05F);
        this.setRotateAngle(upDetail, 0.0F, 0.7853981633974483F, 0.0F);
        this.backBottom = new ModelRenderer(this, 0, 0);
        this.backBottom.setRotationPoint(-1.0F, 1.0F, 3.0F);
        this.backBottom.addBox(0.0F, -1.0F, 0.0F, 2, 1, 1, scale);
        this.setRotateAngle(backBottom, 0.5375614096142535F, 0.0F, 0.0F);
        this.handle = new ModelRenderer(this, 24, 12);
        this.handle.setRotationPoint(0.0F, 0.0F, 0.0F);
        this.handle.addBox(-0.5F, 0.0F, -0.5F, 1, 8, 1, scale);
        this.backRight = new ModelRenderer(this, 23, 0);
        this.backRight.setRotationPoint(-2.0F, -3.5F, 3.0F);
        this.backRight.addBox(0.0F, 0.0F, 0.0F, 1, 4, 1, scale);
        this.setRotateAngle(backRight, 0.0F, 0.5375614096142535F, 0.0F);
        this.upLeft = new ModelRenderer(this, 30, 0);
        this.upLeft.setRotationPoint(-1.0F, -4.0F, -3.0F);
        this.upLeft.addBox(-1.0F, 0.0F, 0.0F, 1, 1, 6, scale);
        this.setRotateAngle(upLeft, 0.0F, 0.0F, -0.5201081170943103F);
        this.frontLeft = new ModelRenderer(this, 23, 0);
        this.frontLeft.setRotationPoint(2.0F, -3.5F, -3.0F);
        this.frontLeft.addBox(-1.0F, 0.0F, -1.0F, 1, 4, 1, scale);
        this.setRotateAngle(frontLeft, 0.0F, 0.5375614096142535F, 0.0F);
    }

    public void disableHandle() {
        this.rope.showModel = false;
        this.handle.showModel = false;
        this.shape22.showModel = false;
    }

    @Override
    public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5) {
        this.upMain.render(f5);
        this.frontMain.render(f5);
        this.frontBottom.render(f5);
        this.backMain.render(f5);
        this.headMain.render(f5);
        this.bottomMain.render(f5);
        this.rope.render(f5);
        this.upRight.render(f5);
        this.upDetail2.render(f5);
        this.backLeft.render(f5);
        this.frontRight.render(f5);
        this.backUp.render(f5);
        this.frontUp.render(f5);
        this.bottomRight.render(f5);
        this.bottomLeft.render(f5);
        this.shape22.render(f5);
        this.upDetail.render(f5);
        this.backBottom.render(f5);
        this.handle.render(f5);
        this.backRight.render(f5);
        this.upLeft.render(f5);
        this.frontLeft.render(f5);
    }

    public void renderModel(float f) {
        this.upMain.render(f);
        this.frontMain.render(f);
        this.frontBottom.render(f);
        this.backMain.render(f);
        this.headMain.render(f);
        this.bottomMain.render(f);
        this.rope.render(f);
        this.upRight.render(f);
        this.upDetail2.render(f);
        this.backLeft.render(f);
        this.frontRight.render(f);
        this.backUp.render(f);
        this.frontUp.render(f);
        this.bottomRight.render(f);
        this.bottomLeft.render(f);
        this.shape22.render(f);
        this.upDetail.render(f);
        this.backBottom.render(f);
        this.handle.render(f);
        this.backRight.render(f);
        this.upLeft.render(f);
        this.frontLeft.render(f);
    }

    /**
     * This is a helper function from Tabula to set the rotation of model parts
     */
    public void setRotateAngle(ModelRenderer modelRenderer, float x, float y, float z) {
        modelRenderer.rotateAngleX = x;
        modelRenderer.rotateAngleY = y;
        modelRenderer.rotateAngleZ = z;
    }
}
