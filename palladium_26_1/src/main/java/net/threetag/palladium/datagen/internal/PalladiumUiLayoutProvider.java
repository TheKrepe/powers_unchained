package net.threetag.palladium.datagen.internal;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.threetag.palladium.Palladium;
import net.threetag.palladium.client.gui.ui.background.RepeatingTextureBackground;
import net.threetag.palladium.client.gui.ui.layout.DefaultPowerLayout;
import net.threetag.palladium.client.renderer.power.PowerRenderer;
import net.threetag.palladium.datagen.UiLayoutProvider;

import java.util.concurrent.CompletableFuture;

public class PalladiumUiLayoutProvider extends UiLayoutProvider {

    public PalladiumUiLayoutProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, lookupProvider, Palladium.MOD_ID);
    }

    @Override
    protected void gather(HolderLookup.Provider provider) {
        this.unconditional(PowerRenderer.DEFAULT_SCREEN_LAYOUT, new DefaultPowerLayout(
                256,
                196,
                RepeatingTextureBackground.RED_WOOL,
                DefaultPowerLayout.DisplayType.AUTO));
    }
}
