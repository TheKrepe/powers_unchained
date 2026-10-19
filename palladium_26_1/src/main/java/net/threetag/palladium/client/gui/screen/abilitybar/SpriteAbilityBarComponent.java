package net.threetag.palladium.client.gui.screen.abilitybar;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.threetag.palladium.logic.context.DataContext;

public record SpriteAbilityBarComponent(AbilityBarSprites sprites, String sprite, DataContext context, int width, int height) implements AbilityBarComponent {

    @Override
    public int getWidth() {
        return this.width;
    }

    @Override
    public int getHeight() {
        return this.height;
    }

    @Override
    public void extractContent(Minecraft minecraft, GuiGraphicsExtractor gui, int x, int y, AbilityBarAlignment alignment) {
        this.sprites.blit(gui, this.sprite, this.context, x, y, this.width, this.height);
    }
}
