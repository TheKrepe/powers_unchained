package com.thekrepe.powersunchained.registry;

import com.thekrepe.powersunchained.menu.SuitCreatorMenu;
import com.thekrepe.powersunchained.platform.Services;
import java.util.function.Supplier;
import net.minecraft.world.inventory.MenuType;

/** Menu types of the mod. */
public final class PUMenus {

    public static final Supplier<MenuType<SuitCreatorMenu>> SUIT_CREATOR = Services.REGISTRY.registerMenu("suit_creator", SuitCreatorMenu::new);

    private PUMenus() {
    }

    public static void init() {
    }
}
