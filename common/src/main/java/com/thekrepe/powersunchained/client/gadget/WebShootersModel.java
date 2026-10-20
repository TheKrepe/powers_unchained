package com.thekrepe.powersunchained.client.gadget;

import com.thekrepe.powersunchained.Constants;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.HumanoidArm;
import org.jspecify.annotations.Nullable;

/**
 * Web-Shooters worn on both wrists: a metal cuff, a cartridge housing on the outer side and a small nozzle at the
 * front. Humanoid model with empty head/body/legs, so it follows the wearer's arm poses via {@code setupAnim}.
 */
public class WebShootersModel extends HumanoidModel<HumanoidRenderState> {

    public static final Identifier TEXTURE = Constants.id("textures/models/web_shooters.png");
    public static final ModelLayerLocation WIDE = new ModelLayerLocation(Constants.id("web_shooters"), "main");
    public static final ModelLayerLocation SLIM = new ModelLayerLocation(Constants.id("web_shooters_slim"), "main");

    private static Models wide;
    private static Models slim;

    public WebShootersModel(ModelPart root, @Nullable HumanoidArm onlyArm) {
        super(root);
        if (onlyArm != null) {
            // first-person models show a single arm; fixed, because model submission is deferred
            this.rightArm.visible = onlyArm == HumanoidArm.RIGHT;
            this.leftArm.visible = onlyArm == HumanoidArm.LEFT;
        }
    }

    public static void registerLayers(BiConsumer<ModelLayerLocation, Supplier<LayerDefinition>> registrar) {
        registrar.accept(WIDE, () -> LayerDefinition.create(createMesh(4), 32, 32));
        registrar.accept(SLIM, () -> LayerDefinition.create(createMesh(3), 32, 32));
    }

    /** Called when entity models are (re)baked. */
    public static void bake(EntityModelSet modelSet) {
        wide = new Models(modelSet, WIDE);
        slim = new Models(modelSet, SLIM);
    }

    /** @param arm {@code null} for both arms (third person) */
    public static @Nullable WebShootersModel get(boolean slimArms, @Nullable HumanoidArm arm) {
        Models set = slimArms ? slim : wide;
        if (set == null) {
            return null;
        }
        return arm == null ? set.both : arm == HumanoidArm.RIGHT ? set.right : set.left;
    }

    private static MeshDefinition createMesh(int armWidth) {
        MeshDefinition mesh = HumanoidModel.createMesh(CubeDeformation.NONE, 0.0F);
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("head", CubeListBuilder.create(), PartPose.ZERO)
                .addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.ZERO);
        root.addOrReplaceChild("body", CubeListBuilder.create(), PartPose.ZERO);
        root.addOrReplaceChild("right_leg", CubeListBuilder.create(), PartPose.offset(-1.9F, 12.0F, 0.0F));
        root.addOrReplaceChild("left_leg", CubeListBuilder.create(), PartPose.offset(1.9F, 12.0F, 0.0F));

        // arm-local x ranges (same as the player model): right [1 - w, 1], left [-1, w - 1]
        float rMin = 1 - armWidth;
        float lMax = armWidth - 1;
        root.addOrReplaceChild("right_arm", CubeListBuilder.create()
                        .texOffs(0, 0).addBox(rMin - 0.5F, 6.5F, -2.5F, armWidth + 1, 3, 5)
                        .texOffs(0, 10).addBox(rMin - 1.25F, 7.0F, -1.5F, 1, 2, 3)
                        .texOffs(0, 16).addBox(0.0F, 7.5F, -3.25F, 1, 1, 1),
                PartPose.offset(-5.0F, 2.0F, 0.0F));
        root.addOrReplaceChild("left_arm", CubeListBuilder.create().mirror()
                        .texOffs(0, 0).addBox(-1.5F, 6.5F, -2.5F, armWidth + 1, 3, 5)
                        .texOffs(0, 10).addBox(lMax + 0.25F, 7.0F, -1.5F, 1, 2, 3)
                        .texOffs(0, 16).addBox(-1.0F, 7.5F, -3.25F, 1, 1, 1),
                PartPose.offset(5.0F, 2.0F, 0.0F));
        return mesh;
    }

    private static final class Models {
        final WebShootersModel both;
        final WebShootersModel right;
        final WebShootersModel left;

        Models(EntityModelSet modelSet, ModelLayerLocation layer) {
            // bakeLayer builds a new part tree each call
            this.both = new WebShootersModel(modelSet.bakeLayer(layer), null);
            this.right = new WebShootersModel(modelSet.bakeLayer(layer), HumanoidArm.RIGHT);
            this.left = new WebShootersModel(modelSet.bakeLayer(layer), HumanoidArm.LEFT);
        }
    }
}
