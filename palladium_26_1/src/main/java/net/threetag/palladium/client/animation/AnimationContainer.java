package net.threetag.palladium.client.animation;

import com.zigythebird.playeranim.animation.PlayerAnimResources;
import com.zigythebird.playeranimcore.animation.AnimationController;
import com.zigythebird.playeranimcore.animation.RawAnimation;
import com.zigythebird.playeranimcore.animation.layered.AnimationSnapshot;
import com.zigythebird.playeranimcore.animation.layered.modifier.AbstractFadeModifier;
import com.zigythebird.playeranimcore.bones.PlayerAnimBone;
import com.zigythebird.playeranimcore.bones.ToggleablePlayerAnimBone;
import com.zigythebird.playeranimcore.easing.EasingType;
import com.zigythebird.playeranimcore.enums.PlayState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Avatar;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public abstract class AnimationContainer implements AnimationController.AnimationStateHandler {

    private final Avatar player;
    private Identifier currentAnimation;

    protected AnimationContainer(Avatar player) {
        this.player = player;
    }

    public Avatar getPlayer() {
        return player;
    }

    public PlayState setAnimation(Identifier animationId, int blendTransition, AnimationController animationController, AnimationController.AnimationSetter animationSetter) {
        return this.setAnimation(animationId, blendTransition, false, animationController, animationSetter);
    }

    public PlayState setAnimation(Identifier animationId, int blendTransition, boolean holdOnLastFrame, AnimationController animationController, AnimationController.AnimationSetter animationSetter) {
        boolean stopping = animationId == null;
        if (animationId == null) {
            animationId = PalladiumAnimationManager.EMPTY_ANIMATION;
        }

        boolean fade = !(stopping && !this.fadeOnStop());

        if (!Objects.equals(animationId, this.currentAnimation)) {
            if (fade && animationController instanceof RenderLayerAwareAnimationController controller) {
                animationController.removeModifierIf(modifier -> modifier instanceof AbstractFadeModifier);

                if (blendTransition > 0) {
                    AbstractFadeModifier fadeModifier = AbstractFadeModifier.standardFadeIn(blendTransition, EasingType.EASE_OUT_SINE);
                    Map<String, ToggleablePlayerAnimBone> snapshots = new HashMap<>();

                    for (PlayerAnimBone bone : controller.getActiveBones().values()) {
                        snapshots.put(bone.getName(), new ToggleablePlayerAnimBone(bone));
                    }

                    fadeModifier.setTransitionAnimation(new AnimationSnapshot(snapshots));
                    animationController.addModifierLast(fadeModifier);
                }
            }
            this.currentAnimation = animationId;
        }

        var anim = PlayerAnimResources.getAnimation(this.currentAnimation);
        boolean loops = anim != null && anim.loopType().shouldPlayAgain(null, anim);
        var raw = holdOnLastFrame && !loops && !PalladiumAnimationManager.EMPTY_ANIMATION.equals(this.currentAnimation)
                ? RawAnimation.begin().thenPlayAndHold(anim)
                : RawAnimation.begin().thenPlay(anim);
        return animationSetter.setAnimation(raw);
    }

    protected boolean fadeOnStop() {
        return true;
    }
}
