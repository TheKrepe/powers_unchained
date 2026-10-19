package net.threetag.palladium.client.gui.screen.abilitybar;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.threetag.palladium.logic.context.DataContext;
import net.threetag.palladium.power.energybar.EnergyBarInstance;

public class EnergyBarComponent implements AbilityBarComponent {

    private final AbilityBar.AbilityList abilityList;
    private final EnergyBarInstance energyBarInstance;

    public EnergyBarComponent(AbilityBar.AbilityList abilityList, EnergyBarInstance energyBarInstance) {
        this.abilityList = abilityList;
        this.energyBarInstance = energyBarInstance;
    }

    @Override
    public int getWidth() {
        return 10;
    }

    @Override
    public int getHeight() {
        return 112;
    }

    @Override
    public void extractContent(Minecraft minecraft, GuiGraphicsExtractor gui, int x, int y, AbilityBarAlignment alignment) {
        int fullHeight = this.getHeight() - 6;
        int height = (int) ((energyBarInstance.get() / (float) energyBarInstance.getMax()) * fullHeight);

        var sprites = this.abilityList.getSprites();
        var context = DataContext.forPower(minecraft.player, this.abilityList.getPowerInstance());
        sprites.blit(gui, AbilityBarSprites.ENERGY_BAR, context, x, y, this.getWidth(), this.getHeight());
        sprites.blit(gui, AbilityBarSprites.ENERGY_BAR_FILL, context, this.getWidth() - 6, fullHeight, 0, fullHeight - height, x + 3, y + 3 + fullHeight - height, this.getWidth() - 6, height, this.energyBarInstance.getConfiguration().color().getRGB());
    }
}
