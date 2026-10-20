package com.thekrepe.powersunchained.compat.curios;

import com.mojang.blaze3d.vertex.PoseStack;
import com.thekrepe.powersunchained.client.gadget.WebShootersModel;
import com.thekrepe.powersunchained.registry.PUItems;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.PlayerModelType;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.client.ICurioRenderer;

/**
 * Curios client integration. Only class-loaded when Curios is installed (see {@code PowersUnchainedNeoForgeClient}).
 */
public final class CuriosClientCompat {

    private CuriosClientCompat() {
    }

    /** Call after items are registered (client setup). */
    public static void init() {
        ICurioRenderer.register(PUItems.WEB_SHOOTERS.get(), WebShootersRenderer::new);
    }

    /** Draws the web-shooters on the wearer's wrists, in third and first person. */
    private static final class WebShootersRenderer implements ICurioRenderer {

        @Override
        public <S extends LivingEntityRenderState, M extends EntityModel<? super S>> void render(ItemStack stack,
                SlotContext slotContext, PoseStack poseStack, SubmitNodeCollector collector, int light, S state,
                RenderLayerParent<S, M> parent, EntityRendererProvider.Context context, float yRot, float xRot) {
            if (!(state instanceof HumanoidRenderState humanoid)) {
                return;
            }
            WebShootersModel model = WebShootersModel.get(isSlim(state), null);
            if (model == null) {
                return;
            }
            model.setupAnim(humanoid);
            collector.submitModel(model, humanoid, poseStack, RenderTypes.armorCutoutNoCull(WebShootersModel.TEXTURE),
                    light, OverlayTexture.NO_OVERLAY, humanoid.outlineColor);
        }

        @Override
        public void renderFirstPersonHand(ItemStack stack, SlotContext slotContext, HumanoidArm arm, PoseStack poseStack,
                SubmitNodeCollector collector, AvatarRenderState state, AbstractClientPlayer player, int light) {
            WebShootersModel model = WebShootersModel.get(isSlim(state), arm);
            if (model == null) {
                return;
            }
            model.setupAnim(state);
            model.resetPose();
            collector.submitModel(model, state, poseStack, RenderTypes.armorCutoutNoCull(WebShootersModel.TEXTURE),
                    light, OverlayTexture.NO_OVERLAY, state.outlineColor);
        }

        private static boolean isSlim(LivingEntityRenderState state) {
            return state instanceof AvatarRenderState avatar && avatar.skin.model() == PlayerModelType.SLIM;
        }
    }
}
