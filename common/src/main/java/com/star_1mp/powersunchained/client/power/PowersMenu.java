package com.star_1mp.powersunchained.client.power;

import com.star_1mp.powersunchained.Constants;
import com.star_1mp.powersunchained.client.ability.AbilityHudConfig;
import com.star_1mp.powersunchained.client.input.PUKeyMappings;
import java.util.Locale;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.Nullable;

/**
 * The powers menu (abilities + progression, for every hero of the mod): opened with its key or the button in the
 * inventory. The menu itself is not made yet ({@link PowersMenuScreen} is a placeholder); concepts in
 * {@code docs/concepts/powers_menu}.
 */
public final class PowersMenu {

    /** Where the inventory button sits (Interface Settings). */
    public enum ButtonPlace {
        /** Next to the recipe book (survival); right of the armour on the creative inventory tab. */
        RECIPE_BOOK,
        /** Outside the inventory, at its top right. */
        RIGHT,
        /** Outside the inventory, at its top left. */
        LEFT,
        /** No button, key only. */
        HIDDEN;

        public String id() {
            return this.name().toLowerCase(Locale.ROOT);
        }

        public Component label() {
            return Component.translatable("screen.powers_unchained.hud_settings.menu_button." + this.id());
        }

        public static ButtonPlace byId(@Nullable String id) {
            for (ButtonPlace p : values()) {
                if (p.id().equals(id)) {
                    return p;
                }
            }
            return RECIPE_BOOK;
        }
    }

    private static final WidgetSprites SPRITES = new WidgetSprites(Constants.id("powers_menu/button"), Constants.id("powers_menu/button_highlighted"));

    private PowersMenu() {
    }

    public static ButtonPlace place() {
        return ButtonPlace.byId(AbilityHudConfig.get().menuButton);
    }

    public static void open(@Nullable Screen parent) {
        Minecraft.getInstance().gui.setScreen(new PowersMenuScreen(parent));
    }

    /** The button for the player's inventory screens (survival / creative), or null for other screens. */
    public static @Nullable MenuButton createButton(Screen screen) {
        if (screen instanceof InventoryScreen || screen instanceof CreativeModeInventoryScreen) {
            return new MenuButton((AbstractContainerScreen<?>) screen);
        }
        return null;
    }

    /** Inventory button with the powers emblem (20x18, like the recipe book button). */
    public static final class MenuButton extends net.minecraft.client.gui.components.AbstractButton {

        private final AbstractContainerScreen<?> screen;

        public AbstractContainerScreen<?> screen() {
            return this.screen;
        }

        MenuButton(AbstractContainerScreen<?> screen) {
            super(0, 0, 20, 18, Component.translatable("key.powers_unchained.powers_menu"));
            this.screen = screen;
            this.setTooltip(Tooltip.create(Component.translatable("key.powers_unchained.powers_menu").append(" (")
                    .append(PUKeyMappings.POWERS_MENU.getTranslatedKeyMessage()).append(")")));
        }

        @Override
        public void onPress(net.minecraft.client.input.InputWithModifiers input) {
            open(this.screen);
        }

        @Override
        protected void extractContents(net.minecraft.client.gui.GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
            graphics.fill(this.getX() - 1, this.getY() - 1, this.getX() + this.width + 1, this.getY() + this.height + 1, 0xFFFF0000);  // TEMP debug
            graphics.blitSprite(net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED, SPRITES.get(this.active, this.isHoveredOrFocused()),
                    this.getX(), this.getY(), this.width, this.height);
        }

        @Override
        protected void updateWidgetNarration(net.minecraft.client.gui.narration.NarrationElementOutput output) {
            this.defaultButtonNarrationText(output);
        }

        /**
         * Every frame (loader glue, which knows the screen's position): the inventory moves when the recipe book opens,
         * the creative screen changes tabs.
         */
        public void place(int left, int top, int imageWidth) {
            ButtonPlace place = PowersMenu.place();
            boolean creative = this.screen instanceof CreativeModeInventoryScreen;
            if (place == ButtonPlace.HIDDEN
                    || (creative && place == ButtonPlace.RECIPE_BOOK && !((CreativeModeInventoryScreen) this.screen).isInventoryOpen())) {
                this.visible = false;
                return;
            }
            this.visible = true;
            switch (place) {
                case RIGHT -> this.setPosition(left + imageWidth + 2, top + 4);
                case LEFT -> this.setPosition(left - 22, top + 4);
                default -> {
                    if (creative) {
                        this.setPosition(left + 151, top + 20);  // right of the armour slots, inventory tab only
                    } else {
                        this.setPosition(left + 126, this.screen.height / 2 - 22);  // the recipe book button is at +104
                    }
                }
            }
        }
    }
}
