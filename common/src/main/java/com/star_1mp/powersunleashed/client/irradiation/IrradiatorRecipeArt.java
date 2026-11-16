package com.star_1mp.powersunleashed.client.irradiation;

import com.star_1mp.powersunleashed.client.gui.GuiFx;
import com.star_1mp.powersunleashed.irradiation.IrradiatorViewRecipe;
import com.star_1mp.powersunleashed.registry.PUItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

/** JEI / REI page of the Irradiator: a dark green monitor, sample -> glowing beam -> result, chance, time, capsule. */
public final class IrradiatorRecipeArt {

    public static final int WIDTH = 150;
    public static final int HEIGHT = 62;
    public static final int[] INPUT = {14, 14};
    public static final int[] OUTPUT = {120, 14};

    private IrradiatorRecipeArt() {
    }

    public static void draw(GuiGraphicsExtractor g, IrradiatorViewRecipe recipe, int x, int y) {
        Font font = Minecraft.getInstance().font;
        g.fill(x, y, x + WIDTH, y + HEIGHT, 0xFF0A140D);
        GuiFx.scanlines(g, x, y, WIDTH, HEIGHT);
        g.fill(x, y, x + WIDTH, y + 1, 0x8058F05A);
        g.fill(x, y + HEIGHT - 1, x + WIDTH, y + HEIGHT, 0xFF16391E);
        frame(g, x + INPUT[0], y + INPUT[1]);
        frame(g, x + OUTPUT[0], y + OUTPUT[1]);
        // the beam with a capsule in the middle
        int by = y + INPUT[1] + 7;
        g.fillGradient(x + 36, by, x + 114, by + 3, 0xFF2C8A3A, 0xFF58F05A);
        g.fill(x + 110, by - 2, x + 112, by + 5, 0xFF58F05A);
        g.fill(x + 112, by - 1, x + 114, by + 4, 0xFF58F05A);
        g.item(new ItemStack(PUItems.ISOTOPE_CAPSULE.get()), x + 67, y + 6);
        Component chance = Component.translatable("jei.powers_unleashed.irradiator.chance", recipe.chancePercent());
        g.centeredText(font, chance, x + WIDTH / 2, y + 26, 0xFF58F05A);
        g.text(font, Component.translatable(recipe.creature() ? "jei.powers_unleashed.irradiator.on_block" : "jei.powers_unleashed.irradiator.time"),
                x + 4, y + 40, 0xFF9AA3AD, false);
        g.text(font, Component.translatable(recipe.creature() ? "jei.powers_unleashed.irradiator.fail_spider" : "jei.powers_unleashed.irradiator.fail_item"),
                x + 4, y + 51, 0xFFFF8A7A, false);
    }

    private static void frame(GuiGraphicsExtractor g, int x, int y) {
        g.fill(x - 1, y - 1, x + 17, y + 17, 0xFF2C8A3A);
        g.fill(x, y, x + 16, y + 16, 0xFF0E1A12);
    }
}
