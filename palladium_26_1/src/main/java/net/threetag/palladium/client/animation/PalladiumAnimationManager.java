package net.threetag.palladium.client.animation;

import com.zigythebird.playeranim.api.PlayerAnimationFactory;
import com.zigythebird.playeranimcore.animation.AnimationController;
import com.zigythebird.playeranimcore.api.firstPerson.FirstPersonConfiguration;
import com.zigythebird.playeranimcore.api.firstPerson.FirstPersonMode;
import com.zigythebird.playeranimcore.enums.State;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Avatar;
import net.minecraft.world.entity.HumanoidArm;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.threetag.palladium.Palladium;

public class PalladiumAnimationManager {

    public static final Identifier EMPTY_ANIMATION = Palladium.id("empty");

    public static final Identifier CONTROLLER_IDLE = Palladium.id("idle");
    public static final Identifier CONTROLLER_MOVEMENT = Palladium.id("movement");
    public static final Identifier CONTROLLER_FLIGHT = Palladium.id("flight");
    public static final Identifier CONTROLLER_SPECIAL = Palladium.id("special");
    public static final Identifier CONTROLLER_OPENABLE = Palladium.id("openable");
    public static final Identifier CONTROLLER_TRANSITION = Palladium.id("transition");

    private static final FirstPersonConfiguration FIRST_PERSON_BOTH_ARMS = new FirstPersonConfiguration(true, true, true, true, true);
    private static final FirstPersonConfiguration FIRST_PERSON_RIGHT_ARM = new FirstPersonConfiguration(true, false, true, true, true);
    private static final FirstPersonConfiguration FIRST_PERSON_LEFT_ARM = new FirstPersonConfiguration(false, true, true, true, true);

    public static void registerLayers(FMLClientSetupEvent e) {
        e.enqueueWork(() -> PlayerAnimationFactory.ANIMATION_DATA_FACTORY.registerFactory(CONTROLLER_MOVEMENT, 500,
                player -> new RenderLayerAwareAnimationController(player, new PowerAnimationContainer(PalladiumAnimationLayer.MOVEMENT, player))
        ));

        e.enqueueWork(() -> PlayerAnimationFactory.ANIMATION_DATA_FACTORY.registerFactory(CONTROLLER_IDLE, 1000,
                player -> new RenderLayerAwareAnimationController(player, new PowerAnimationContainer(PalladiumAnimationLayer.IDLE, player))
        ));

        e.enqueueWork(() -> PlayerAnimationFactory.ANIMATION_DATA_FACTORY.registerFactory(CONTROLLER_FLIGHT, 1500,
                player -> new RenderLayerAwareAnimationController(player, new FlightAnimationContainer(PalladiumAnimationLayer.FLIGHT, player))
        ));

        e.enqueueWork(() -> PlayerAnimationFactory.ANIMATION_DATA_FACTORY.registerFactory(CONTROLLER_SPECIAL, 2000,
                player -> new RenderLayerAwareAnimationController(player, new PowerAnimationContainer(PalladiumAnimationLayer.SPECIAL, player))
                        .setFirstPersonModeHandler(PalladiumAnimationManager::thirdPersonModelWhilePlaying)
                        .setFirstPersonConfigurationHandler(animationController -> visibleArms(player))
        ));

        e.enqueueWork(() -> PlayerAnimationFactory.ANIMATION_DATA_FACTORY.registerFactory(CONTROLLER_OPENABLE, 2200,
                player -> new RenderLayerAwareAnimationController(player, new OpenableAnimationContainer(player))
                        .setFirstPersonModeHandler(PalladiumAnimationManager::thirdPersonModelWhilePlaying)
                        .setFirstPersonConfigurationHandler(animationController -> visibleArms(player))
        ));

        e.enqueueWork(() -> PlayerAnimationFactory.ANIMATION_DATA_FACTORY.registerFactory(CONTROLLER_TRANSITION, 2500,
                player -> new TransitionAwareAnimationController(player, new PowerTransitionAnimationContainer(player))
        ));
    }

    private static FirstPersonConfiguration visibleArms(Avatar player) {
        if (!player.getOffhandItem().isEmpty()) {
            return FIRST_PERSON_BOTH_ARMS;
        }

        return player.getMainArm() == HumanoidArm.RIGHT ? FIRST_PERSON_RIGHT_ARM : FIRST_PERSON_LEFT_ARM;
    }

    private static FirstPersonMode thirdPersonModelWhilePlaying(AnimationController animationController) {
        var animationInstance = animationController.getCurrentAnimationInstance();
        var animationId = animationInstance != null ? animationInstance.getNameOrId() : null;
        return animationController.getAnimationState() == State.STOPPED || EMPTY_ANIMATION.getPath().equals(animationId) ? FirstPersonMode.NONE : FirstPersonMode.THIRD_PERSON_MODEL;
    }
}
