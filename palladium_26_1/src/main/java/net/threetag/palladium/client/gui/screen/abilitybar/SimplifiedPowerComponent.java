package net.threetag.palladium.client.gui.screen.abilitybar;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.threetag.palladium.logic.context.DataContext;
import net.threetag.palladium.power.ability.AbilityInstance;

public class SimplifiedPowerComponent implements AbilityBarComponent {

    private final AbilityBar.AbilityList abilityList;
    private final AbilityInstance<?> abilityInstance;

    public SimplifiedPowerComponent(AbilityBar.AbilityList abilityList, AbilityInstance<?> abilityInstance) {
        this.abilityList = abilityList;
        this.abilityInstance = abilityInstance;
    }

    @Override
    public int getWidth() {
        return 24;
    }

    @Override
    public int getHeight() {
        return 24;
    }

    @Override
    public void extractContent(Minecraft minecraft, GuiGraphicsExtractor gui, int x, int y, AbilityBarAlignment alignment) {
        var sprites = this.abilityList.getSprites();
        sprites.blit(gui, AbilityBarSprites.SINGLE_ABILITY, DataContext.forAbility(minecraft.player, this.abilityInstance), x, y, 24, 24);
        AbilityListComponent.renderAbility(minecraft, sprites, gui, x + 3, y + 3, alignment, this.abilityInstance, 0);
    }
}
