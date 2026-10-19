package lucraft.mods.heroes.speedsterheroes.client.models;

import lucraft.mods.lucraftcore.suitset.SuitSet;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.inventory.EntityEquipmentSlot;

/**
 * ModelJayGarrickHelm - Lucraft
 * Created using Tabula 5.1.0
 */
public class ModelJayGarrickHelm extends ModelSpeedsterAdvancedBiped {
    public ModelRenderer plate;
    public ModelRenderer helm;
    public ModelRenderer left1;
    public ModelRenderer left2;
    public ModelRenderer left3;
    public ModelRenderer right1;
    public ModelRenderer right2;
    public ModelRenderer right3;

    public ModelJayGarrickHelm(float f, String normalTex, String lightTex, SuitSet hero, EntityEquipmentSlot slot) {
    	super(f, normalTex, lightTex, hero, slot, false);

    	this.lightsTex = null;
    	
        this.left1 = new ModelRenderer(this, 0, 16);
        this.left1.setRotationPoint(0.0F, 0.0F, 0.0F);
        this.left1.addBox(-6.6F, -7.0F, -0.8F, 1, 1, 1, 0.0F);
        this.helm = new ModelRenderer(this, 0, 31);
        this.helm.setRotationPoint(0.0F, 0.0F, 0.0F);
        this.helm.addBox(-4.0F, -8.3F, -4.0F, 8, 2, 8, 0.6F);
        this.right2 = new ModelRenderer(this, 0, 18);
        this.right2.setRotationPoint(0.0F, 0.0F, 0.0F);
        this.right2.addBox(5.9F, -7.8F, -1.5F, 1, 1, 1, 0.0F);
        this.left2 = new ModelRenderer(this, 0, 18);
        this.left2.setRotationPoint(0.0F, 0.0F, 0.0F);
        this.left2.addBox(-6.9F, -7.8F, -1.5F, 1, 1, 1, 0.0F);
        this.left3 = new ModelRenderer(this, 0, 20);
        this.left3.setRotationPoint(0.0F, 0.0F, 0.0F);
        this.left3.addBox(-6.6F, -8.6F, -0.7F, 1, 1, 4, 0.0F);
        this.right3 = new ModelRenderer(this, 0, 20);
        this.right3.setRotationPoint(0.0F, 0.0F, 0.0F);
        this.right3.addBox(5.6F, -8.6F, -0.7F, 1, 1, 4, 0.0F);
        this.plate = new ModelRenderer(this, 0, 16);
        this.plate.setRotationPoint(0.0F, 0.0F, 0.0F);
        this.plate.addBox(-6.5F, -6.0F, -6.5F, 13, 1, 13, 0.0F);
        this.right1 = new ModelRenderer(this, 0, 16);
        this.right1.setRotationPoint(0.0F, 0.0F, 0.0F);
        this.right1.addBox(5.6F, -7.0F, -0.8F, 1, 1, 1, 0.0F);
        
        this.bipedHead.addChild(helm);
        this.bipedHead.addChild(plate);
        this.bipedHead.addChild(left1);
        this.bipedHead.addChild(left2);
        this.bipedHead.addChild(left3);
        this.bipedHead.addChild(right1);
        this.bipedHead.addChild(right2);
        this.bipedHead.addChild(right3);
    }

    @Override
    public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5) {
    	super.render(entity, f, f1, f2, f3, f4, f5);
    }

    /**
     * This is a helper function from Tabula to set the rotation of model parts
     */
    public void setRotateAngle(ModelRenderer modelRenderer, float x, float y, float z) {
        modelRenderer.rotateAngleX = x;
        modelRenderer.rotateAngleY = y;
        modelRenderer.rotateAngleZ = z;
    }
    
    @Override
    public void setRotationAngles(float p_78087_1_, float p_78087_2_, float p_78087_3_, float p_78087_4_, float p_78087_5_, float p_78087_6_, Entity entityIn) {
    	super.setRotationAngles(p_78087_1_, p_78087_2_, p_78087_3_, p_78087_4_, p_78087_5_, p_78087_6_, entityIn);
    }
    
    @Override
    public void setModelVisibility() {
    }
    
}