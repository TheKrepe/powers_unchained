package lucraft.mods.heroes.speedsterheroes.client.models;

import lucraft.mods.lucraftcore.suitset.SuitSet;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ModelTheRivalEars extends ModelSpeedsterAdvancedBiped {

	public ModelRenderer leftEar;
    public ModelRenderer rightEar;

    public ModelTheRivalEars(float f, String normalTex, String lightTex, SuitSet hero, EntityEquipmentSlot slot) {
    	super(f, normalTex, lightTex, hero, slot, false);
        this.textureWidth = 64;
        this.textureHeight = 64;

        this.leftEar = new ModelRenderer(this, 10, 17);
        this.leftEar.setRotationPoint(0.0F, 0.0F, 0.0F);
        this.leftEar.addBox(3.3999999F, -6.0F, 1.6F, 1, 4, 5);
        this.setRotationAngles(this.leftEar, 0.22689280275926282F, 0.4075643510626834F, 0.0F);
        this.rightEar = new ModelRenderer(this, 0, 17);
        this.rightEar.setRotationPoint(0.0F, 0.0F, 0.0F);
        this.rightEar.addBox(-4.4F, -6.0F, 1.6000001F, 1, 4, 5);
        this.setRotationAngles(this.rightEar, 0.22689280275926282F, -0.4068583470577035F, 0.0F);
        
        this.bipedHead.addChild(leftEar);
        this.bipedHead.addChild(rightEar);
    }

    public void setRotationAngles(ModelRenderer modelRenderer, float x, float y, float z) {
        modelRenderer.rotateAngleX = x;
        modelRenderer.rotateAngleY = y;
        modelRenderer.rotateAngleZ = z;
    }
    
    @Override
    public void setModelVisibility() {
    }
}
