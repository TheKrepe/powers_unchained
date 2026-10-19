package net.threetag.palladium.client.gui.screen.abilitybar;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.ChatScreen;
import net.threetag.palladium.client.PalladiumKeyMappings;
import net.threetag.palladium.client.renderer.icon.IconRenderer;
import net.threetag.palladium.config.PalladiumClientConfig;
import net.threetag.palladium.logic.context.DataContext;
import net.threetag.palladium.power.ability.AbilityInstance;
import net.threetag.palladium.power.ability.enabling.KeyBindEnablingHandler;
import net.threetag.palladium.power.ability.keybind.AbilityKeyBind;
import net.threetag.palladium.power.ability.keybind.JumpKeyBind;
import net.threetag.palladium.power.ability.keybind.KeyBindType;
import net.threetag.palladium.power.ability.keybind.MouseClickKeyBind;

public record AbilityListComponent(AbilityBar.AbilityList abilityList) implements AbilityBarComponent {

    @Override
    public int getWidth() {
        return 24;
    }

    @Override
    public int getHeight() {
        return 112;
    }

    @Override
    public void extractContent(Minecraft minecraft, GuiGraphicsExtractor gui, int x, int y, AbilityBarAlignment alignment) {
        var sprites = this.abilityList.getSprites();
        sprites.blit(gui, AbilityBarSprites.ABILITY_LIST, DataContext.forPower(minecraft.player, this.abilityList.getPowerInstance()), x, y, this.getWidth(), this.getHeight());

        for (int i = 0; i < AbilityBar.AbilityList.MAX_ABILITIES; i++) {
            var ability = this.abilityList.getAbility(i);
            int abilityX = x + 3;
            int abilityY = y + 3 + (i * 22);
            renderAbility(minecraft, sprites, gui, abilityX, abilityY, alignment, ability, i);
        }
    }

    public static void renderAbility(Minecraft minecraft, AbilityBarSprites sprites, GuiGraphicsExtractor gui, int x, int y, AbilityBarAlignment alignment, AbilityInstance<?> ability, int index) {
        if (ability != null) {
            var context = DataContext.forAbility(minecraft.player, ability);

            if (ability.isUnlocked()) {
                if (ability.getAbility().getStateManager().getEnablingHandler() instanceof KeyBindEnablingHandler handler
                        && handler.getBehaviour() == KeyBindEnablingHandler.Behaviour.ACTIVATION) {
                    int height = (int) (handler.getActivationPercentage(ability, context) * 18);
                    sprites.blit(gui, AbilityBarSprites.SLOT_DISABLED, context, x, y, 18, 18);
                    sprites.blit(gui, AbilityBarSprites.SLOT_ENABLED, context, 18, 18, 0, 18 - height, x, y + (18 - height), 18, height);
                } else {
                    sprites.blit(gui, ability.isEnabled() ? AbilityBarSprites.SLOT_ENABLED : AbilityBarSprites.SLOT_DISABLED, context, x, y, 18, 18);
                }

                // Ability Icon
                IconRenderer.drawIcon(ability.getAbility().getProperties().getIcon(), minecraft, gui, context, x + 1, y + 1, 1F);

                var barIcon = AbilityBarSpecialRenderer.get(ability.getAbility().getSerializer());
                if (barIcon != null) {
                    barIcon.render(minecraft, gui, ability, x, y, 18, 18, alignment);
                }

                // Cooldown
                if (ability.getAbility().getStateManager().getEnablingHandler() instanceof KeyBindEnablingHandler handler
                        && handler.displayCooldown(ability)) {
                    float percentage = handler.getCooldownPercentage(ability, context);

                    sprites.blit(gui, AbilityBarSprites.SLOT_COOLDOWN, context, 18, 18, 0, 0, x, y, (int) (18 * percentage), 18);
                }

                // Key Bind (inside)
                if (PalladiumClientConfig.ABILITY_BAR_KEY_BIND_DISPLAY.get() == AbilityKeyBindDisplay.INSIDE &&
                        ability.getAbility().getStateManager().getEnablingHandler() instanceof KeyBindEnablingHandler handler) {
                    var key = getComponentForKeyBind(handler.getKeyBindType(), sprites, context, true, index);
                    gui.pose().pushMatrix();
                    key.extractContent(minecraft, gui, x + 19 - key.getWidth(), y + 19 - key.getHeight(), alignment);
                    gui.pose().popMatrix();
                }

                // Name / Key Bind (outside)
                boolean chatOpen = minecraft.screen instanceof ChatScreen;
                AbilityBarComponent displayedText = null;

                if (PalladiumClientConfig.ABILITY_BAR_KEY_BIND_DISPLAY.get() == AbilityKeyBindDisplay.OUTSIDE) {
                    if (chatOpen) {
                        displayedText = new TextAbilityBarComponent(ability.getAbility().getDisplayName().get(context));
                    } else if (ability.getAbility().getStateManager().getEnablingHandler() instanceof KeyBindEnablingHandler handler) {
                        displayedText = getComponentForKeyBind(handler.getKeyBindType(), sprites, context, false, index);
                    }
                } else if (chatOpen) {
                    displayedText = new TextAbilityBarComponent(ability.getAbility().getDisplayName().get(context));
                }

                if (displayedText != null) {
                    int width = displayedText.getWidth();
                    gui.fill(
                            x + (alignment.isLeft() ? 21 : -width - 10),
                            y + 3,
                            x + (alignment.isLeft() ? 28 : -width - 3) + width,
                            y + 5 + 10,
                            0x80000000
                    );
                    displayedText.extractContent(minecraft, gui, x + (alignment.isLeft() ? 26 : -width - 8), y + 3 + ((12 - displayedText.getHeight()) / 2), alignment);
                }
            } else {
                sprites.blit(gui, AbilityBarSprites.SLOT_LOCKED, context, x, y, 18, 18);
                sprites.blit(gui, AbilityBarSprites.SLOT_LOCK, context, x, y, 18, 18);
            }

            // Ability Color
            sprites.blit(gui, AbilityBarSprites.slotName(ability.getAbility().getProperties().getColor()), context, x - 3, y - 3, 24, 24);
        } else {
            sprites.blit(gui, AbilityBarSprites.SLOT_EMPTY, DataContext.forEntity(minecraft.player), x, y, 18, 18);
        }
    }

    public static AbilityBarComponent getComponentForKeyBind(KeyBindType type, AbilityBarSprites sprites, DataContext context, boolean inside, int index) {
        return switch (type) {
            case JumpKeyBind jump -> new SpriteAbilityBarComponent(sprites, AbilityBarSprites.KEY_JUMP, context, 10, 5);
            case AbilityKeyBind ability ->
                    new TextAbilityBarComponent(PalladiumKeyMappings.ABILITY_KEYS[index].getTranslatedKeyMessage(), inside);
            case MouseClickKeyBind mouse -> new SpriteAbilityBarComponent(sprites,
                    mouse.clickType == MouseClickKeyBind.ClickType.LEFT_CLICK ? AbilityBarSprites.KEY_MOUSE_LEFT :
                            (mouse.clickType == MouseClickKeyBind.ClickType.RIGHT_CLICK ? AbilityBarSprites.KEY_MOUSE_RIGHT : AbilityBarSprites.KEY_MOUSE_MIDDLE),
                    context, 5, 7);
            default ->
                    throw new IllegalStateException(String.format("No UiComponent known for key bind type %s", type.getClass().getSimpleName()));
        };
    }
}
