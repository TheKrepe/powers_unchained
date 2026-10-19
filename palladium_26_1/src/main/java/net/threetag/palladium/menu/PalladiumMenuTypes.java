package net.threetag.palladium.menu;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.network.IContainerFactory;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.threetag.palladium.Palladium;

public class PalladiumMenuTypes {

    public static final DeferredRegister<MenuType<?>> MENU_TYPES = DeferredRegister.create(Registries.MENU, Palladium.MOD_ID);

    public static final DeferredHolder<MenuType<?>, MenuType<TailoringMenu>> TAILORING = MENU_TYPES.register("tailoring", () -> new MenuType<>(TailoringMenu::new, FeatureFlags.VANILLA_SET));
    public static final DeferredHolder<MenuType<?>, MenuType<MultiversalIteratorMenu>> MULTIVERSAL_ITERATOR = MENU_TYPES.register("multiversal_iterator", () -> new MenuType<>(MultiversalIteratorMenu::new, FeatureFlags.VANILLA_SET));
    public static final DeferredHolder<MenuType<?>, MenuType<MultiversalIteratorSuitStandMenu>> MULTIVERSAL_ITERATOR_SUIT_STAND = MENU_TYPES.register("multiversal_iterator_suit_stand", () -> new MenuType<>((IContainerFactory<MultiversalIteratorSuitStandMenu>) MultiversalIteratorSuitStandMenu::new, FeatureFlags.VANILLA_SET));

}
