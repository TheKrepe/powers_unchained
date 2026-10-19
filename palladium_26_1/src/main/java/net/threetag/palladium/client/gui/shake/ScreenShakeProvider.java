package net.threetag.palladium.client.gui.shake;

import net.minecraft.client.player.LocalPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiConsumer;
import java.util.function.Function;

@FunctionalInterface
public interface ScreenShakeProvider {

    void provide(LocalPlayer player, BiConsumer<Object, ScreenShakeSource> consumer);

    default void reset() {

    }

    static ScreenShakeProvider timed(Function<LocalPlayer, @Nullable ScreenShake> shake) {
        return new TimedScreenShakeProvider(shake);
    }
}
