package lucraft.mods.lucraftcore.infinity.render;

import lucraft.mods.lucraftcore.LCConfig;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

/**
 * ModelInfinityGauntlet - Lucraft Created using Tabula 6.0.0
 */
public class ModelInfinityGauntlet extends ModelBase {

    public ModelRenderer shape1;
    public ModelRenderer hand;
    public ModelRenderer index_0;
    public ModelRenderer middle_0;
    public ModelRenderer ring_0;
    public ModelRenderer pinkie_0;
    public ModelRenderer thumb_0;
    public ModelRenderer stone_0;
    public ModelRenderer stone_1;
    public ModelRenderer stone_2;
    public ModelRenderer stone_3;
    public ModelRenderer stone_4;
    public ModelRenderer stone_5;
    public ModelRenderer index_1;
    public ModelRenderer middle_1;
    public ModelRenderer ring_1;
    public ModelRenderer pinkie_1;
    public ModelRenderer thumb_1;

    public ModelInfinityGauntlet() {
        this.textureWidth = 64;
        this.textureHeight = 16;
        this.thumb_0 = new ModelRenderer(this, 43, 0);
        this.thumb_0.setRotationPoint(-2.0F, 16.0F, 1.0F);
        this.thumb_0.addBox(-1.0F, -1.0F, -0.5F, 1, 1, 1, 0.0F);
        this.setRotateAngle(thumb_0, 0.0F, 0.0F, -0.17453292519943295F);
        this.stone_5 = new ModelRenderer(this, 39, 2);
        this.stone_5.setRotationPoint(-2.5999999046325684F, 16.600000381469727F, 0.5F);
        this.stone_5.addBox(0.0F, 0.0F, 0.0F, 1, 1, 1, 0.0F);
        this.pinkie_0 = new ModelRenderer(this, 39, 0);
        this.pinkie_0.setRotationPoint(2.0F, 16.0F, -2.0F);
        this.pinkie_0.addBox(-0.5F, -1.0F, -0.5F, 1, 1, 1, 0.0F);
        this.hand = new ModelRenderer(this, 24, 0);
        this.hand.setRotationPoint(0.0F, 16.0F, 0.0F);
        this.hand.addBox(-2.5F, 0.0F, -2.5F, 5, 4, 5, 0.0F);
        this.middle_1 = new ModelRenderer(this, 47, 2);
        this.middle_1.setRotationPoint(0.0F, -2.0F, 0.0F);
        this.middle_1.addBox(-0.5F, -3.0F, -0.5F, 1, 3, 1, 0.0F);
        this.setRotateAngle(middle_1, -0.3490658503988659F, 0.0F, 0.0F);
        this.ring_1 = new ModelRenderer(this, 51, 2);
        this.ring_1.setRotationPoint(0.0F, -2.0F, 0.0F);
        this.ring_1.addBox(-0.5F, -2.0F, -0.5F, 1, 2, 1, 0.0F);
        this.setRotateAngle(ring_1, -0.3490658503988659F, 0.0F, 0.0F);
        this.stone_4 = new ModelRenderer(this, 25, 2);
        this.stone_4.setRotationPoint(-0.4000000059604645F, 17.700000762939453F, -2.5999999046325684F);
        this.stone_4.addBox(0.0F, 0.0F, 0.0F, 1, 1, 1, 0.0F);
        this.middle_0 = new ModelRenderer(this, 18, 0);
        this.middle_0.setRotationPoint(-0.6000000238418579F, 16.0F, -2.0F);
        this.middle_0.addBox(-0.5F, -2.0F, -0.5F, 1, 2, 1, 0.0F);
        this.stone_2 = new ModelRenderer(this, 55, 0);
        this.stone_2.setRotationPoint(-1.100000023841858F, 16.200000762939453F, -2.5999999046325684F);
        this.stone_2.addBox(0.0F, 0.0F, 0.0F, 1, 1, 1, 0.0F);
        this.shape1 = new ModelRenderer(this, 0, 0);
        this.shape1.setRotationPoint(0.0F, 20.0F, 0.0F);
        this.shape1.addBox(-3.0F, 0.0F, -3.0F, 6, 4, 6, 0.0F);
        this.ring_0 = new ModelRenderer(this, 22, 0);
        this.ring_0.setRotationPoint(0.699999988079071F, 16.0F, -2.0F);
        this.ring_0.addBox(-0.5F, -2.0F, -0.5F, 1, 2, 1, 0.0F);
        this.stone_1 = new ModelRenderer(this, 51, 0);
        this.stone_1.setRotationPoint(0.20000000298023224F, 16.200000762939453F, -2.5999999046325684F);
        this.stone_1.addBox(0.0F, 0.0F, 0.0F, 1, 1, 1, 0.0F);
        this.pinkie_1 = new ModelRenderer(this, 55, 2);
        this.pinkie_1.setRotationPoint(0.0F, -1.0F, 0.0F);
        this.pinkie_1.addBox(-0.5F, -2.0F, -0.5F, 1, 2, 1, 0.0F);
        this.setRotateAngle(pinkie_1, -0.3490658503988659F, 0.0F, 0.0F);
        this.thumb_1 = new ModelRenderer(this, 59, 2);
        this.thumb_1.setRotationPoint(-0.5F, -1.0F, 0.0F);
        this.thumb_1.addBox(-0.5F, -2.0F, -0.5F, 1, 2, 1, 0.0F);
        this.setRotateAngle(thumb_1, -0.3490658503988659F, 0.0F, 0.0F);
        this.index_0 = new ModelRenderer(this, 0, 0);
        this.index_0.setRotationPoint(-2.0F, 16.0F, -2.0F);
        this.index_0.addBox(-0.5F, -2.0F, -0.5F, 1, 2, 1, 0.0F);
        this.stone_0 = new ModelRenderer(this, 47, 0);
        this.stone_0.setRotationPoint(1.399999976158142F, 16.200000762939453F, -2.5999999046325684F);
        this.stone_0.addBox(0.0F, 0.0F, 0.0F, 1, 1, 1, 0.0F);
        this.index_1 = new ModelRenderer(this, 43, 2);
        this.index_1.setRotationPoint(0.0F, -2.0F, 0.0F);
        this.index_1.addBox(-0.5F, -2.0F, -0.5F, 1, 2, 1, 0.0F);
        this.setRotateAngle(index_1, -0.3490658503988659F, 0.0F, 0.0F);
        this.stone_3 = new ModelRenderer(this, 59, 0);
        this.stone_3.setRotationPoint(-2.4000000953674316F, 16.200000762939453F, -2.5999999046325684F);
        this.stone_3.addBox(0.0F, 0.0F, 0.0F, 1, 1, 1, 0.0F);
        this.middle_0.addChild(this.middle_1);
        this.ring_0.addChild(this.ring_1);
        this.pinkie_0.addChild(this.pinkie_1);
        this.thumb_0.addChild(this.thumb_1);
        this.index_0.addChild(this.index_1);
    }

    @Override
    public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5) {
        this.thumb_0.render(f5);
        this.stone_5.render(f5);
        this.pinkie_0.render(f5);
        this.hand.render(f5);
        this.stone_4.render(f5);
        this.middle_0.render(f5);
        this.stone_2.render(f5);
        this.shape1.render(f5);
        this.ring_0.render(f5);
        this.stone_1.render(f5);
        this.index_0.render(f5);
        this.stone_0.render(f5);
        this.stone_3.render(f5);
    }

    public void renderModel(float f) {
        this.fingerVisibility(LCConfig.infinity.fingersOnGauntlet);
        this.thumb_0.render(f);
        this.stone_5.render(f);
        this.pinkie_0.render(f);
        this.hand.render(f);
        this.stone_4.render(f);
        this.middle_0.render(f);
        this.stone_2.render(f);
        this.shape1.render(f);
        this.ring_0.render(f);
        this.stone_1.render(f);
        this.index_0.render(f);
        this.stone_0.render(f);
        this.stone_3.render(f);
    }

    public void fingerVisibility(boolean visible) {
        this.index_0.showModel = visible;
        this.middle_0.showModel = visible;
        this.ring_0.showModel = visible;
        this.pinkie_0.showModel = visible;
        this.thumb_0.showModel = visible;
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
