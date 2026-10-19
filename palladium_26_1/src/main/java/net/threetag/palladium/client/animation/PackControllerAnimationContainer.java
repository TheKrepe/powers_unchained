package net.threetag.palladium.client.animation;

import com.zigythebird.playeranimcore.animation.AnimationController;
import com.zigythebird.playeranimcore.animation.AnimationData;
import com.zigythebird.playeranimcore.enums.PlayState;
import com.zigythebird.playeranimcore.enums.State;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Avatar;
import net.threetag.palladium.logic.context.DataContext;
import net.threetag.palladium.logic.context.DataContextKeys;

public abstract class PackControllerAnimationContainer extends AnimationContainer {

    protected final PalladiumAnimationLayer layer;
    private PackAnimationController currentAnimationController;
    private PackAnimationController.State currentState;
    private String currentStateAnimation;
    private int lastTransitionTick = -1;

    public PackControllerAnimationContainer(PalladiumAnimationLayer layer, Avatar player) {
        super(player);
        this.layer = layer;
    }

    private int getBlendTransition() {
        if (this.currentState != null && this.currentState.blendTransition().isPresent()) {
            return this.currentState.blendTransition().get();
        }
        return this.currentAnimationController != null ? this.currentAnimationController.blendTransition().orElse(5) : 5;
    }

    public abstract PackAnimationController getAvailableAnimationController(Avatar player);

    @Override
    public PlayState handle(AnimationController animationController, AnimationData animationData, AnimationController.AnimationSetter animationSetter) {
        var current = getAvailableAnimationController(this.getPlayer());

        if (current != this.currentAnimationController) {
            this.currentAnimationController = current;
            this.enterState(null);
        }

        if (this.currentAnimationController != null) {
            if (this.currentState == null) {
                this.enterState(this.currentAnimationController.initialState()
                        .map(s -> this.currentAnimationController.states().get(s))
                        .orElse(null));
            }

            if (this.currentState != null) {
                int currentTick = this.getPlayer().tickCount;
                if (currentTick != this.lastTransitionTick) {
                    this.lastTransitionTick = currentTick;
                    boolean animationFinished = animationController.hasAnimationFinished()
                            || animationController.getAnimationState() == State.PAUSED;
                    var context = DataContext.forEntity(this.getPlayer()).with(DataContextKeys.ANY_ANIMATION_FINISHED, animationFinished);

                    for (PackAnimationController.Transition transition : this.currentState.transitions()) {
                        if (transition.condition().test(context)) {
                            this.enterState(this.currentAnimationController.states().get(transition.state()));
                            break;
                        }
                    }
                }
                Identifier animId = this.currentStateAnimation != null ? Identifier.tryParse(this.currentStateAnimation) : null;
                return this.setAnimation(animId, this.getBlendTransition(), true, animationController, animationSetter);
            } else {
                return this.setAnimation(null, this.getBlendTransition(), animationController, animationSetter);
            }
        } else {
            return this.setAnimation(null, this.getBlendTransition(), animationController, animationSetter);
        }
    }

    private void enterState(PackAnimationController.State state) {
        this.currentState = state;
        this.currentStateAnimation = state != null ? state.pickAnimation(this.getPlayer().getRandom()) : null;
    }
}
