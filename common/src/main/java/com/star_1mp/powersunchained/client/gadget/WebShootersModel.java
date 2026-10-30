package com.star_1mp.powersunchained.client.gadget;

import com.mojang.blaze3d.vertex.PoseStack;
import com.star_1mp.powersunchained.Constants;
import com.star_1mp.powersunchained.client.suit.SuitModelLayers;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.PlayerModelType;
import org.jspecify.annotations.Nullable;

/**
 * Web-shooters drawn like a suit piece: a player-skin texture (64x64, only the arm regions used) on the player model,
 * inflated just above skin sleeves and suit sleeves ({@link SuitModelLayers#WEB_SHOOTERS_INFLATE}), so they stay
 * visible over any skin and a worn chestplate.
 * Only the arms (and sleeves) are shown.
 */
public class WebShootersModel extends PlayerModel {

    public static final Identifier TEXTURE = Constants.id("textures/gadget/web_shooters.png");
    public static final Identifier TEXTURE_SLIM = Constants.id("textures/gadget/web_shooters_slim.png");

    private static @Nullable WebShootersModel wide;
    private static @Nullable WebShootersModel slim;
    private static @Nullable WebShootersModel firstPersonWide;
    private static @Nullable WebShootersModel firstPersonSlim;

    public WebShootersModel(ModelPart root, boolean slim) {
        super(root, slim);
        this.applyVisibility();
    }

    /** Called when entity models are (re)baked; bakeLayer builds a new part tree per call. */
    public static void bake(EntityModelSet modelSet) {
        wide = new WebShootersModel(modelSet.bakeLayer(SuitModelLayers.WEB_SHOOTERS), false);
        slim = new WebShootersModel(modelSet.bakeLayer(SuitModelLayers.WEB_SHOOTERS_SLIM), true);
        firstPersonWide = new WebShootersModel(modelSet.bakeLayer(SuitModelLayers.WEB_SHOOTERS), false);
        firstPersonSlim = new WebShootersModel(modelSet.bakeLayer(SuitModelLayers.WEB_SHOOTERS_SLIM), true);
    }

    @Override
    public void setupAnim(AvatarRenderState state) {
        super.setupAnim(state);
        // PlayerModel hides sleeves from the skin flags (which suits turn off), so set our own visibility again
        this.applyVisibility();
    }

    private void applyVisibility() {
        this.head.visible = false;
        this.hat.visible = false;
        this.body.visible = false;
        this.jacket.visible = false;
        this.rightLeg.visible = false;
        this.leftLeg.visible = false;
        this.rightPants.visible = false;
        this.leftPants.visible = false;
        this.rightArm.visible = true;
        this.leftArm.visible = true;
        this.rightSleeve.visible = true;
        this.leftSleeve.visible = true;
    }

    private static final java.util.Map<String, RenderType> TYPES = new java.util.HashMap<>();

    /** Texture per look (Tech Table customization): default = the owner's textures, else web_shooters_<look>(_slim). */
    private static RenderType renderType(boolean slimArms, String look) {
        String key = look + (slimArms ? "_slim" : "");
        return TYPES.computeIfAbsent(key, k -> RenderTypes.armorCutoutNoCull(com.star_1mp.powersunchained.tech.GadgetSkins.DEFAULT.equals(look)
                ? (slimArms ? TEXTURE_SLIM : TEXTURE)
                : Constants.id("textures/gadget/web_shooters_" + look + (slimArms ? "_slim" : "") + ".png")));
    }

    /** Third person, on a player or mannequin. */
    public static void render(PoseStack poseStack, SubmitNodeCollector collector, int light, AvatarRenderState state, String look) {
        boolean slimArms = state.skin.model() == PlayerModelType.SLIM;
        WebShootersModel model = slimArms ? slim : wide;
        if (model != null) {
            collector.submitModel(model, state, poseStack, renderType(slimArms, look), light, OverlayTexture.NO_OVERLAY, state.outlineColor);
        }
    }

    /** First person: the arm currently being drawn (pose stack already set up for the arm). */
    public static void renderArm(PoseStack poseStack, SubmitNodeCollector collector, int light, HumanoidArm side, boolean slimArms, String look) {
        WebShootersModel model = slimArms ? firstPersonSlim : firstPersonWide;
        if (model == null) {
            return;
        }
        boolean right = side == HumanoidArm.RIGHT;
        ModelPart arm = right ? model.rightArm : model.leftArm;
        arm.resetPose();
        arm.visible = true;
        (right ? model.rightSleeve : model.leftSleeve).visible = true;
        arm.zRot = right ? 0.1F : -0.1F;
        collector.submitModelPart(arm, poseStack, renderType(slimArms, look), light, OverlayTexture.NO_OVERLAY, null);
    }
}
