package net.threetag.palladium.condition;

import com.google.gson.JsonObject;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.threetag.palladium.util.context.DataContext;
import net.threetag.palladium.util.context.DataContextType;
import net.threetag.palladium.util.property.ConditionArrayProperty;
import net.threetag.palladium.util.property.PalladiumProperty;
import net.threetag.palladiumcore.util.Platform;

public class AsClientPlayerCondition extends Condition {

    public final Condition[] conditions;

    public AsClientPlayerCondition(Condition[] conditions) {
        this.conditions = conditions;
    }

    @Override
    public boolean active(DataContext context) {
        if (Platform.isClient()) {
            return this.activeForClientPlayer(context);
        } else {
            return false;
        }
    }

    @Environment(EnvType.CLIENT)
    private boolean activeForClientPlayer(DataContext context) {
        Player clientPlayer = Minecraft.getInstance().player;

        if (clientPlayer == null) {
            return false;
        }

        DataContext clientContext = context.copy()
                .with(DataContextType.ENTITY, clientPlayer)
                .with(DataContextType.LEVEL, clientPlayer.level());

        for (Condition condition : this.conditions) {
            if (!condition.active(clientContext)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public ConditionSerializer getSerializer() {
        return ConditionSerializers.AS_CLIENT_PLAYER.get();
    }

    public static class Serializer extends ConditionSerializer {

        public static final PalladiumProperty<Condition[]> CONDITIONS = new ConditionArrayProperty("conditions").configurable("List of conditions to evaluate against the client player (the player viewing the game)");

        public Serializer() {
            this.withProperty(CONDITIONS, new Condition[0]);
        }

        @Override
        public Condition make(JsonObject json) {
            return new AsClientPlayerCondition(this.getProperty(json, CONDITIONS));
        }

        @Override
        public ConditionEnvironment getContextEnvironment() {
            return ConditionEnvironment.ASSETS;
        }

        @Override
        public String getDocumentationDescription() {
            return "Evaluates the given conditions against the client player (the player viewing the game) instead of the context entity. Useful for render layers that should only appear based on the viewing player's state. Returns false if this condition is being checked server-side.";
        }
    }
}
