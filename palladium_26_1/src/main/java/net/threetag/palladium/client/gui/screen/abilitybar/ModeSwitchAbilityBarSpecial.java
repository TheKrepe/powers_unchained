package net.threetag.palladium.client.gui.screen.abilitybar;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.threetag.palladium.client.renderer.icon.IconRenderer;
import net.threetag.palladium.logic.context.DataContext;
import net.threetag.palladium.power.ability.AbilityInstance;
import net.threetag.palladium.power.ability.ModeSwitchAbility;

public class ModeSwitchAbilityBarSpecial implements AbilityBarSpecialRenderer {

    @Override
    public void render(Minecraft minecraft, GuiGraphicsExtractor gui, AbilityInstance<?> ability, int x, int y, int width, int height, AbilityBarAlignment alignment) {
        if (!(ability.getAbility() instanceof ModeSwitchAbility modeSwitch)) {
            return;
        }

        ModeSwitchAbility.Mode mode = modeSwitch.getActiveMode(ability);

        if (mode == null) {
            return;
        }

        mode.icon().ifPresent(icon -> {
            gui.pose().pushMatrix();
            gui.pose().translate(x + width / 4F, y + height * 3 / 4F);
            gui.pose().scale(0.5F, 0.5F);
            IconRenderer.drawIcon(icon, minecraft, gui, DataContext.forAbility(minecraft.player, ability), -8, -8, 1F);
            gui.pose().popMatrix();
        });
    }
}
