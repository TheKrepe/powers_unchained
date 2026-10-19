package net.threetag.palladium.client.animation;

import net.threetag.palladium.Palladium;

public class PackAnimationControllerSerializers {

    public static final PackAnimationControllerSerializer<?> DEFAULT = register("default", new DefaultPackAnimationController.Serializer());
    public static final PackAnimationControllerSerializer<?> TRIGGERED = register("triggered", new TriggeredPackAnimationController.Serializer());

    private static <T extends PackAnimationController> PackAnimationControllerSerializer<T> register(String id, PackAnimationControllerSerializer<T> serializer) {
        return PackAnimationControllerSerializer.register(Palladium.id(id), serializer);
    }

    public static void init() {
        // nothing
    }

}
