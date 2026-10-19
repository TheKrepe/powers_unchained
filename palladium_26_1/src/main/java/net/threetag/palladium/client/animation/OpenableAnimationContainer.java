package net.threetag.palladium.client.animation;

import com.zigythebird.playeranimcore.animation.AnimationController;
import com.zigythebird.playeranimcore.animation.AnimationData;
import com.zigythebird.playeranimcore.enums.PlayState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Avatar;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class OpenableAnimationContainer extends AnimationContainer {

    private static final Map<UUID, Identifier> PENDING = new HashMap<>();

    private Identifier animation;
    private int startTick;

    public OpenableAnimationContainer(Avatar player) {
        super(player);
    }

    public static void play(Avatar player, Identifier animation) {
        PENDING.put(player.getUUID(), animation);
    }

    @Override
    public PlayState handle(AnimationController animationController, AnimationData animationData, AnimationController.AnimationSetter animationSetter) {
        int tick = this.getPlayer().tickCount;

        if (this.animation != null && tick - this.startTick > 1 && animationController.hasAnimationFinished()) {
            this.animation = null;
        }

        var pending = PENDING.remove(this.getPlayer().getUUID());

        if (pending != null) {
            this.animation = pending;
            this.startTick = tick;
            animationController.forceAnimationReset();
        }

        return this.setAnimation(this.animation, 5, animationController, animationSetter);
    }

    public boolean isPlaying() {
        return this.animation != null;
    }
}
