package lucraft.mods.heroesexpansion.integration;

import lucraft.mods.heroesexpansion.HEConfig;
import lucraft.mods.heroesexpansion.items.HEItems;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import slimeknights.tconstruct.library.TinkerRegistry;

public class HETiConIntegration {

    public static void init() {
        Fluid uru = FluidRegistry.getFluid("uru");

        TinkerRegistry.registerMelting(HEItems.MJOLNIR, uru, 144 * 6);
        TinkerRegistry.registerMelting(HEItems.MJOLNIR_HEAD, uru, 144 * 6);
        TinkerRegistry.registerMelting(HEItems.ULTIMATE_MJOLNIR, uru, 144 * 9);
        TinkerRegistry.registerMelting(HEItems.ULTIMATE_MJOLNIR_HEAD, uru, 144 * 9);
        TinkerRegistry.registerMelting(HEItems.STORMBREAKER, uru, 144 * 9);
        TinkerRegistry.registerMelting(HEItems.STORMBREAKER_HEAD, uru, 144 * 9);

        if (HEConfig.MOD_CRAFTING) {
            TinkerRegistry.registerTableCasting(new ItemStack(HEItems.MJOLNIR_HEAD), new ItemStack(HEItems.MJOLNIR_HEAD_CAST), uru, 144 * 6);
            TinkerRegistry.registerTableCasting(new ItemStack(HEItems.ULTIMATE_MJOLNIR_HEAD), new ItemStack(HEItems.ULTIMATE_MJOLNIR_HEAD_CAST), uru, 144 * 9);
            TinkerRegistry.registerTableCasting(new ItemStack(HEItems.STORMBREAKER_HEAD), new ItemStack(HEItems.STORMBREAKER_HEAD_CAST), uru, 144 * 9);
        }
    }

}
