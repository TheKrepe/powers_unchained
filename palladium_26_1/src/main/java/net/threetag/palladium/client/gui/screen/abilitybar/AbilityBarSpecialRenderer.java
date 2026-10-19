package net.threetag.palladium.client.gui.screen.abilitybar;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.threetag.palladium.power.ability.AbilityInstance;
import net.threetag.palladium.power.ability.AbilitySerializer;
import net.threetag.palladium.power.ability.AbilitySerializers;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public interface AbilityBarSpecialRenderer {

    Map<AbilitySerializer<?>, AbilityBarSpecialRenderer> REGISTRY = new HashMap<>();

    static void register(AbilitySerializer<?> serializer, AbilityBarSpecialRenderer renderer) {
        REGISTRY.put(serializer, renderer);
    }

    @Nullable
    static AbilityBarSpecialRenderer get(AbilitySerializer<?> serializer) {
        return REGISTRY.get(serializer);
    }

    static void registerRenderers() {
        register(AbilitySerializers.MODE_SWITCH.get(), new ModeSwitchAbilityBarSpecial());
    }

    void render(Minecraft minecraft, GuiGraphicsExtractor gui, AbilityInstance<?> ability, int x, int y, int width, int height, AbilityBarAlignment alignment);
}
