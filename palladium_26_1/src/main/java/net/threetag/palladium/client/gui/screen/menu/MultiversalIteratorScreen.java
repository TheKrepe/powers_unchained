package net.threetag.palladium.client.gui.screen.menu;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.ItemCombinerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.threetag.palladium.Palladium;
import net.threetag.palladium.item.MultiversalExtrapolatorItem;
import net.threetag.palladium.item.multiverse.Universe;
import net.threetag.palladium.menu.MultiversalIteratorMenu;
import net.threetag.palladium.sound.PalladiumSoundEvents;

public class MultiversalIteratorScreen extends ItemCombinerScreen<MultiversalIteratorMenu> {

    public static final Identifier TEXTURE = Palladium.id("textures/gui/container/multiversal_iterator.png");

    private int tickCount = 0;
    private int resultAnimationTimer = 0;
    private CycleButton cycleButton;

    public MultiversalIteratorScreen(MultiversalIteratorMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title, TEXTURE);
    }

    @Override
    protected void subInit() {
        super.subInit();
        this.addRenderableWidget(this.cycleButton = new CycleButton(this.leftPos + 133, this.topPos + 53,
                button -> this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, MultiversalIteratorMenu.CYCLE_BUTTON_ID)));
        this.cycleButton.active = false;
    }

    @Override
    protected void extractErrorIcon(GuiGraphicsExtractor guiGraphics, int xo, int yo) {
        var universe = MultiversalExtrapolatorItem.getUniverse(this.menu.getSlot(MultiversalIteratorMenu.EXTRAPOLATOR_SLOT).getItem());

        if (universe == null) {
            RandomSource random = RandomSource.create(this.tickCount / 2);
            String number = "" + random.nextInt(10) + random.nextInt(10) + random.nextInt(10);
            guiGraphics.text(this.font, Universe.getGenericUniverseComponent(number), xo + 61, yo + 24, 0xffffffff);
        } else {
            guiGraphics.text(this.font, Universe.getTitle(universe.identifier()), xo + 61, yo + 24, 0xffffffff);
        }

        if (!this.menu.getSlot(this.menu.getResultSlot()).getItem().isEmpty() && this.resultAnimationTimer > 0) {
            guiGraphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, xo + 74, yo + 49, 176, 0, this.resultAnimationTimer, 17, 256, 256);
        }
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        this.tickCount++;

        if (this.menu.getSlot(this.menu.getResultSlot()).getItem().isEmpty() || this.resultAnimationTimer >= 20) {
            this.resultAnimationTimer = 0;
        } else {
            this.resultAnimationTimer++;
        }

        if (this.cycleButton != null) {
            this.cycleButton.active = this.menu.getResultCount() > 1;
        }
    }

    @Override
    public void slotChanged(AbstractContainerMenu container, int slotIndex, ItemStack stack) {
        if (slotIndex == MultiversalIteratorMenu.EXTRAPOLATOR_SLOT && !stack.isEmpty() && MultiversalExtrapolatorItem.getUniverse(stack) != null) {
            this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(PalladiumSoundEvents.MULTIVERSE_SEARCH.get(), 1.0F));
        }
    }

    private static class CycleButton extends Button {

        protected CycleButton(int x, int y, OnPress onPress) {
            super(x, y, 8, 8, Component.empty(), onPress, DEFAULT_NARRATION);
        }

        @Override
        public void extractContents(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
            if (this.active) {
                guiGraphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, this.getX(), this.getY(), this.isHoveredOrFocused() ? 184 : 176, 17, this.getWidth(), this.getHeight() + 1, 256, 256);
            }
        }
    }
}
