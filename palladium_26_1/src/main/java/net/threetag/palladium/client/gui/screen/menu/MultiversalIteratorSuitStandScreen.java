package net.threetag.palladium.client.gui.screen.menu;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerListener;
import net.minecraft.world.item.ItemStack;
import net.threetag.palladium.Palladium;
import net.threetag.palladium.client.gui.widget.ArrowButton;
import net.threetag.palladium.client.gui.widget.FlatButton;
import net.threetag.palladium.item.MultiversalExtrapolatorItem;
import net.threetag.palladium.item.multiverse.Universe;
import net.threetag.palladium.menu.MultiversalIteratorSuitStandMenu;
import net.threetag.palladium.sound.PalladiumSoundEvents;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class MultiversalIteratorSuitStandScreen extends AbstractContainerScreen<MultiversalIteratorSuitStandMenu> implements ContainerListener {

    public static final Identifier TEXTURE = Palladium.id("textures/gui/container/multiversal_iterator_suit_stand.png");
    public static final Identifier SPRITE_CONFIRM = Palladium.id("widget/confirm");
    public static final Identifier SPRITE_DENY = Palladium.id("widget/deny");
    private static final Quaternionf SUIT_STAND_ANGLE = new Quaternionf().rotationXYZ(0.43633232F, 0.0F, (float) Math.PI);

    private int tickCount = 0;

    public MultiversalIteratorSuitStandScreen(MultiversalIteratorSuitStandMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title, 176, 214);
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    protected void init() {
        super.init();
        this.menu.addSlotListener(this);

        this.addRenderableWidget(new CycleButton(this.leftPos + 51, this.topPos + 71, false, button -> this.menu.switchPage(false)));
        this.addRenderableWidget(new CycleButton(this.leftPos + 114, this.topPos + 71, true, button -> this.menu.switchPage(true)));
        this.addRenderableWidget(new ConfirmButton(this.leftPos + 136, this.topPos + 69,
                button -> this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, this.menu.getSelectedPage().id())));
    }

    @Override
    public void removed() {
        super.removed();
        this.menu.removeSlotListener(this);
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        this.tickCount++;
        this.menu.updatePagesIfEquipmentChanged();
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(guiGraphics, mouseX, mouseY, partialTick);
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, this.leftPos, this.topPos, 0.0F, 0.0F, this.imageWidth, this.imageHeight, 256, 256);

        var universe = MultiversalExtrapolatorItem.getUniverse(this.menu.getSlot(MultiversalIteratorSuitStandMenu.EXTRAPOLATOR_SLOT).getItem());

        if (universe == null) {
            RandomSource random = RandomSource.create(this.tickCount / 2);
            String number = "" + random.nextInt(10) + random.nextInt(10) + random.nextInt(10);
            guiGraphics.text(this.font, Universe.getGenericUniverseComponent(number), this.leftPos + 61, this.topPos + 24, 0xffffffff);
        } else {
            guiGraphics.text(this.font, Universe.getTitle(universe.identifier()), this.leftPos + 61, this.topPos + 24, 0xffffffff);
        }

        this.renderSuitStandPreview(guiGraphics);
    }

    private void renderSuitStandPreview(GuiGraphicsExtractor guiGraphics) {
        var suitStand = this.menu.suitStand;

        if (suitStand == null) {
            return;
        }

        this.menu.getSelectedPage().pretend(suitStand, stand -> {
            var renderState = extractRenderState(stand);

            if (renderState instanceof LivingEntityRenderState livingState) {
                livingState.bodyRot = 210.0F;
                livingState.yRot = 0.0F;
                livingState.xRot = 25.0F;
                livingState.boundingBoxWidth = livingState.boundingBoxWidth / livingState.scale;
                livingState.boundingBoxHeight = livingState.boundingBoxHeight / livingState.scale;
                livingState.scale = 1.0F;
            }

            guiGraphics.entity(renderState, 30F, new Vector3f(0.0F, renderState.boundingBoxHeight / 2.0F, 0.0F),
                    SUIT_STAND_ANGLE, null, this.leftPos + 51, this.topPos + 44, this.leftPos + 125, this.topPos + 117);
        });
    }

    private static EntityRenderState extractRenderState(ArmorStand suitStand) {
        var dispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
        EntityRenderer<? super ArmorStand, ?> renderer = dispatcher.getRenderer(suitStand);
        EntityRenderState renderState = renderer.createRenderState(suitStand, 1.0F);
        renderState.shadowPieces.clear();
        renderState.outlineColor = 0;
        return renderState;
    }

    @Override
    public void slotChanged(AbstractContainerMenu container, int slotIndex, ItemStack stack) {
        if (slotIndex == MultiversalIteratorSuitStandMenu.EXTRAPOLATOR_SLOT && !stack.isEmpty() && MultiversalExtrapolatorItem.getUniverse(stack) != null) {
            this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(PalladiumSoundEvents.MULTIVERSE_SEARCH.get(), 1.0F));
        }
    }

    @Override
    public void dataChanged(AbstractContainerMenu container, int dataSlotIndex, int value) {
    }

    private class CycleButton extends ArrowButton {

        protected CycleButton(int x, int y, boolean next, OnPress onPress) {
            super(x, y, next, onPress);
        }

        @Override
        public void extractContents(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
            this.active = MultiversalIteratorSuitStandScreen.this.menu.getPages().size() > 1;
            super.extractContents(guiGraphics, mouseX, mouseY, partialTick);
        }
    }

    private class ConfirmButton extends FlatButton {

        protected ConfirmButton(int x, int y, OnPress onPress) {
            super(x, y, 22, 22, onPress);
        }

        @Override
        public void extractContents(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
            this.active = MultiversalIteratorSuitStandScreen.this.menu.getSelectedPage().id() != 0;
            super.extractContents(guiGraphics, mouseX, mouseY, partialTick);
            guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, this.active ? SPRITE_CONFIRM : SPRITE_DENY, this.getX(), this.getY(),
                    this.getWidth(), this.getHeight());
        }
    }
}
