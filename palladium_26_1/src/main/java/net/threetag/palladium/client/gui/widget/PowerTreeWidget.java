package net.threetag.palladium.client.gui.widget;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.ARGB;
import net.minecraft.util.EasingType;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec2;
import net.neoforged.neoforge.client.ClientHooks;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.threetag.palladium.Palladium;
import net.threetag.palladium.client.gui.screen.DelayedRenderCallReceiver;
import net.threetag.palladium.client.gui.screen.ModalScreen;
import net.threetag.palladium.client.gui.ui.background.UiBackground;
import net.threetag.palladium.client.gui.widget.tree.*;
import net.threetag.palladium.client.util.GuiUtil;
import net.threetag.palladium.client.util.RenderUtil;
import net.threetag.palladium.config.PalladiumClientConfig;
import net.threetag.palladium.icon.Icon;
import net.threetag.palladium.logic.context.DataContext;
import net.threetag.palladium.network.BuyAbilityPacket;
import net.threetag.palladium.power.PowerInstance;
import net.threetag.palladium.power.ability.AbilityInstance;
import net.threetag.palladium.power.ability.AbilityReference;
import net.threetag.palladium.logic.cost.CostDisplay;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class PowerTreeWidget extends AbstractWidget implements TickableWidget {

    public static final String TRANS_TITLE = "gui.palladium.powers";
    public static final String TRANS_CUSTOMIZE = "gui.palladium.powers.customize";
    public static final String TRANS_KEY_UNLOCK = "gui.palladium.powers.unlock";
    public static final String TRANS_KEY_VERY_SAD_LABEL = "gui.palladium.powers.sad_label";
    public static final String TRANS_KEY_NO_ABILITIES_LABEL = "gui.palladium.powers.empty";
    public static final Identifier SPRITE_TITLE_BOX = Palladium.id("powers/title_box");
    public static final Identifier SPRITE_VIGNETTE = Palladium.id("powers/vignette");
    public static final Identifier SPRITE_LOCK = Palladium.id("powers/lock");
    private final Screen parent;
    private final UiBackground background;
    @Nullable
    private final Identifier treeId;
    @Nullable
    private PowerTree tree;
    private int gridSize = PowerTree.DEFAULT_GRID_SIZE;
    private float shiftX, shiftY;
    private int innerWidth, innerHeight;
    private boolean isDragging = false;
    private int offsetX = 0, offsetY = 0;
    private int fade, fade0;
    private boolean enableFade = false;
    public final List<AbilityElement> abilities = new ArrayList<>();
    private final List<CustomElement> customElements = new ArrayList<>();
    private final List<Element> elements = new ArrayList<>();
    private final List<PowerTreeConnection> connections = new ArrayList<>();

    public PowerTreeWidget(Screen parent, PowerInstance powerInstance, @Nullable Identifier treeId, UiBackground background,
                           int x, int y, int width, int height) {
        super(x, y, width, height, Component.empty());
        this.parent = parent;
        this.background = background;
        this.treeId = treeId;
        this.innerWidth = Math.max(500, this.getWidth());
        this.innerHeight = Math.max(500, this.getHeight());
        this.populate(powerInstance);
    }

    public void populate(PowerInstance powerInstance) {
        this.abilities.clear();
        this.customElements.clear();
        this.elements.clear();
        this.connections.clear();

        if (powerInstance != null) {
            this.tree = PowerTreeManager.INSTANCE.get(this.treeId != null ? this.treeId : powerInstance.getPowerId());
            this.gridSize = this.tree != null ? this.tree.gridSize() : PowerTree.DEFAULT_GRID_SIZE;
            PowerTreePopulator.generateTree(powerInstance, this.abilities, this.customElements, this.tree);
            this.elements.addAll(this.abilities);
            this.elements.addAll(this.customElements);

            float minX = Float.MAX_VALUE, minY = Float.MAX_VALUE, maxX = -Float.MAX_VALUE, maxY = -Float.MAX_VALUE;

            for (Element element : this.elements) {
                element.gridSize = this.gridSize;
                minX = Math.min(minX, element.gridX);
                minY = Math.min(minY, element.gridY);
                maxX = Math.max(maxX, element.gridX + 1);
                maxY = Math.max(maxY, element.gridY + 1);
            }

            if (this.tree != null) {
                for (PowerTree.Region region : this.tree.regions()) {
                    minX = Math.min(minX, region.pos().x);
                    minY = Math.min(minY, region.pos().y);
                    maxX = Math.max(maxX, region.pos().x + region.size().x);
                    maxY = Math.max(maxY, region.pos().y + region.size().y);
                }

                for (PowerTree.Connection connection : this.tree.connections()) {
                    for (Vec2 point : connection.via()) {
                        minX = Math.min(minX, point.x);
                        minY = Math.min(minY, point.y);
                        maxX = Math.max(maxX, point.x + 1);
                        maxY = Math.max(maxY, point.y + 1);
                    }
                }
            }

            if (this.elements.isEmpty()) {
                return;
            }

            float contentWidth = (maxX - minX) * this.gridSize;
            float contentHeight = (maxY - minY) * this.gridSize;
            this.innerWidth = (int) Math.max(contentWidth, this.getWidth());
            this.innerHeight = (int) Math.max(contentHeight, this.getHeight());
            this.shiftX = -minX + (this.innerWidth - contentWidth) / this.gridSize / 2F;
            this.shiftY = -minY + (this.innerHeight - contentHeight) / this.gridSize / 2F;

            for (Element element : this.elements) {
                element.gridX += this.shiftX;
                element.gridY += this.shiftY;
            }

            this.connections.addAll(PowerTreePopulator.generateConnections(powerInstance, this.abilities, this.customElements, this.tree,
                    point -> new int[]{this.toPixelX(point.x), this.toPixelY(point.y)}));
            this.connections.sort(Comparator.comparing(PowerTreeConnection::getState));
            this.offsetX = Mth.clamp(this.offsetX, 0, this.innerWidth - this.getWidth());
            this.offsetY = Mth.clamp(this.offsetY, 0, this.innerHeight - this.getHeight());
        }
    }

    private int toPixelX(float gridX) {
        return Math.round((gridX + this.shiftX) * this.gridSize + this.gridSize / 2F);
    }

    private int toPixelY(float gridY) {
        return Math.round((gridY + this.shiftY) * this.gridSize + this.gridSize / 2F);
    }

    @Override
    public void tick() {
        this.fade0 = this.fade;

        if (this.enableFade && this.fade < 10) {
            this.fade++;
        } else if (!this.enableFade && this.fade > 0) {
            this.fade--;
        }

        this.enableFade = false;

        if (!this.abilities.isEmpty() && this.abilities.stream().anyMatch(AbilityElement::needsRefresh)) {
            this.populate(this.abilities.getFirst().abilityInstance.getPowerInstance());
        }
    }

    @Override
    protected void extractWidgetRenderState(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (this.elements.isEmpty()) {
            int x = this.getX() + this.getWidth() / 2;
            int y = this.getY() + this.getHeight() / 2;
            guiGraphics.fill(this.getX(), this.getY(), this.getRight(), this.getBottom(), RenderUtil.FULL_BLACK);
            GuiUtil.drawCenteredWordWrap(guiGraphics, Component.translatable(TRANS_KEY_NO_ABILITIES_LABEL), x, y - 20, this.getWidth() - 15, -1);
            GuiUtil.drawCenteredWordWrap(guiGraphics, Component.translatable(TRANS_KEY_VERY_SAD_LABEL), x, y + 20, this.getWidth() - 15, -1);
            return;
        }

        guiGraphics.enableScissor(this.getX(), this.getY(), this.getRight(), this.getBottom());
        this.extractInner(guiGraphics, this.getX() - this.offsetX, this.getY() - this.offsetY, this.innerWidth,
                this.innerHeight, mouseX, mouseY);
        guiGraphics.disableScissor();
        guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, SPRITE_VIGNETTE, this.getX(), this.getY(), this.getWidth(), this.getHeight());

        float fade = EasingType.IN_OUT_CUBIC.apply(Mth.lerp(partialTick, this.fade0, this.fade) / 10F) / 3F;
        if (fade > 0F) {
            guiGraphics.fill(this.getX(), this.getY(), this.getRight(), this.getBottom(), ARGB.colorFromFloat(fade, 0, 0, 0));
        }
    }

    private void extractInner(GuiGraphicsExtractor guiGraphics, int x, int y, int width, int height, int mouseX, int mouseY) {
        this.background.render(guiGraphics, x, y, width, height, this.alpha);

        if (this.tree != null) {
            for (PowerTree.Region region : this.tree.regions()) {
                this.extractRegion(guiGraphics, region, x, y);
            }
        }

        for (PowerTreeConnection connection : this.connections) {
            connection.draw(guiGraphics, x, y, true);
        }

        for (PowerTreeConnection connection : this.connections) {
            connection.draw(guiGraphics, x, y, false);
        }

        for (Element element : this.elements) {
            element.extractRenderState(guiGraphics, x + element.getX(), y + element.getY());

            if (this.parent instanceof DelayedRenderCallReceiver receiver && this.isMouseOverElement(element, mouseX, mouseY)) {
                receiver.renderDelayed(gui -> element.extractHovered(gui, x + element.getX(), y + element.getY()));
                this.enableFade = true;
            }
        }
    }

    private void extractRegion(GuiGraphicsExtractor guiGraphics, PowerTree.Region region, int x, int y) {
        int x0 = x + Math.round((region.pos().x + this.shiftX) * this.gridSize);
        int y0 = y + Math.round((region.pos().y + this.shiftY) * this.gridSize);
        int width = Math.round(region.size().x * this.gridSize);
        int height = Math.round(region.size().y * this.gridSize);

        region.color().ifPresent(color -> guiGraphics.fill(x0, y0, x0 + width, y0 + height, color));
        region.background().ifPresent(background -> PowerTreeNodeStyle.blit(guiGraphics, background,
                DataContext.forEntity(this.parent.getMinecraft().player), x0, y0, width, height));
        region.label().ifPresent(label -> guiGraphics.text(this.parent.getMinecraft().font, label,
                x0 + Math.round(region.labelOffset().x), y0 + Math.round(region.labelOffset().y), region.labelColor(), region.labelShadow()));
    }

    private boolean isMouseOverElement(Element element, int mouseX, int mouseY) {
        if (!this.isMouseOver(mouseX, mouseY)) {
            return false;
        }

        int x = this.getX() - this.offsetX + element.getX();
        int y = this.getY() - this.offsetY + element.getY();
        int half = element.style.getSize() / 2;

        return mouseX >= x - half && mouseX <= x + half && mouseY >= y - half && mouseY <= y + half;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {

    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean isDoubleClick) {
        if (this.isActive()) {
            if (this.isValidClickButton(event.buttonInfo())) {
                if (this.isMouseOver(event.x(), event.y())) {
                    for (Element element : this.elements) {
                        if (this.isMouseOverElement(element, (int) event.x(), (int) event.y())) {
                            this.playDownSound(Minecraft.getInstance().getSoundManager());
                            return element.onClicked(this.parent);
                        }
                    }

                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double mouseX, double mouseY) {
        if (event.button() != InputConstants.MOUSE_BUTTON_LEFT) {
            this.isDragging = false;
            return false;
        } else {
            if (!this.isDragging) {
                this.isDragging = true;
            } else {
                this.drag(mouseX, mouseY);
            }

            return true;
        }
    }

    public void drag(double dragX, double dragY) {
        this.offsetX = (int) Mth.clamp(this.offsetX - dragX, 0, this.innerWidth - this.getWidth());
        this.offsetY = (int) Mth.clamp(this.offsetY - dragY, 0, this.innerHeight - this.getHeight());
    }

    public abstract static class Element {

        protected final Minecraft minecraft = Minecraft.getInstance();
        float gridX = 0, gridY = 0;
        int gridSize = PowerTree.DEFAULT_GRID_SIZE;
        PowerTreeNodeStyle.Resolved style = PowerTreeNodeStyle.Resolved.DEFAULT;

        public abstract String getKey();

        public abstract PowerTreeState getState();

        public abstract Component getTitle();

        @Nullable
        public abstract Icon getIcon();

        protected abstract DataContext getContext();

        @Nullable
        protected Component getSubtitle() {
            return null;
        }

        public abstract boolean onClicked(Screen parent);

        public Element setStyle(PowerTreeNodeStyle.Resolved style) {
            this.style = style;
            return this;
        }

        public int getX() {
            return Math.round(this.gridX * this.gridSize + (this.gridSize / 2F));
        }

        public int getY() {
            return Math.round(this.gridY * this.gridSize + (this.gridSize / 2F));
        }

        public void extractRenderState(GuiGraphicsExtractor guiGraphics, int x, int y) {
            this.style.render(guiGraphics, x, y, this.getState(), this.getIcon(), this.getContext());
        }

        public void extractHovered(GuiGraphicsExtractor guiGraphics, int x, int y) {
            var font = this.minecraft.font;
            var title = this.getTitle();
            var subtitle = this.getSubtitle();

            if (!title.getString().isBlank() || subtitle != null) {
                int textWidth = font.width(title);
                int padding = 3;
                int half = this.style.getSize() / 2;

                if (subtitle != null) {
                    int subtitleWidth = font.width(subtitle);
                    guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, SPRITE_TITLE_BOX, x, y - 13,
                            Math.max(subtitleWidth, textWidth) + half + (padding * 3), 26 + 10);
                    guiGraphics.text(font, title, x + half + padding, y - 4, RenderUtil.FULL_WHITE);
                    guiGraphics.text(font, subtitle, x + half + padding, y - 4 + 11, RenderUtil.DEFAULT_GRAY, false);
                } else {
                    guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, SPRITE_TITLE_BOX, x, y - 13,
                            textWidth + half + (padding * 3), 26);
                    guiGraphics.text(font, title, x + half + padding, y - 4, RenderUtil.FULL_WHITE);
                }
            }

            this.extractRenderState(guiGraphics, x, y);
        }

        protected ModalScreen createModal(Component description) {
            return new ModalScreen(description)
                    .setHeader(this.getTitle())
                    .disableBackgroundRendering();
        }
    }

    public static class AbilityElement extends Element {

        private final AbilityInstance<?> abilityInstance;
        private Component title;
        private Component description;
        private Icon icon;
        private final boolean unlocked;
        boolean fixedPosition = false;
        List<AbilityElement> parents = new LinkedList<>();
        List<AbilityElement> children = new LinkedList<>();
        public boolean parentsUnlocked = true;

        public AbilityElement(AbilityInstance<?> abilityInstance) {
            this.abilityInstance = abilityInstance;
            this.title = abilityInstance.getAbility().getDisplayName().get(DataContext.forAbility(minecraft.player, abilityInstance));
            this.icon = abilityInstance.getAbility().getProperties().getIcon();
            this.unlocked = abilityInstance.isUnlocked();
            var desc = abilityInstance.getAbility().getProperties().getDescription();
            this.description = desc == null ? Component.empty() : desc.get(this.unlocked);
        }

        public AbilityElement applyNode(PowerTree.Node node) {
            node.title().ifPresent(title -> this.title = title);
            node.icon().ifPresent(icon -> this.icon = icon);
            node.description().ifPresent(description -> this.description = description);
            return this;
        }

        @Override
        public boolean onClicked(Screen parent) {
            var handled = this.parentsUnlocked && !this.unlocked
                    && this.abilityInstance.getAbility().getStateManager().getUnlockingHandler()
                    .onClicked(parent.getMinecraft().player, this.abilityInstance);

            if (!handled) {
                this.openModal(parent);
            }

            return true;
        }

        public boolean openModal(Screen parent) {
            return this.openModal(parent, null, false);
        }

        public boolean openModal(Screen parent, @Nullable CostDisplay display, boolean available) {
            if (display == null && this.description.getString().isBlank()) {
                return false;
            }

            var modal = this.createModal(this.description);

            if (display != null) {
                var button = TextWithIconButton.textWithIconBuilder(Component.translatable(TRANS_KEY_UNLOCK).append(": " + display.amount() + "x "), display.icon(), s -> {
                    ClientPacketDistributor.sendToServer(new BuyAbilityPacket(this.getReference()));
                    modal.onClose();
                    Objects.requireNonNull(Objects.requireNonNull(this.minecraft).player).playSound(SoundEvents.PLAYER_LEVELUP, 1F, 1F);
                }).bounds(0, 0, 20, 20).tooltip(Tooltip.create(display.description())).build();
                button.active = available;
                modal.addButton(button);
            }

            ClientHooks.pushGuiLayer(parent.getMinecraft(), modal);
            return true;
        }

        public boolean needsRefresh() {
            return this.unlocked != this.abilityInstance.isUnlocked();
        }

        public AbilityElement setGridPos(float x, float y) {
            this.gridX = x;
            this.gridY = y;
            return this;
        }

        public AbilityElement setGridPosFixed(float x, float y) {
            this.fixedPosition = true;
            return this.setGridPos(x, y);
        }

        public void updatePosition(float x, float y) {
            if (!this.fixedPosition) {
                this.gridX = x;
                this.gridY = y;
            }

            for (AbilityElement child : this.children) {
                child.updatePosition(this.gridX + 1, y);
            }
        }

        @Override
        public PowerTreeState getState() {
            return PowerTreeState.of(this.unlocked, this.parentsUnlocked);
        }

        @Override
        protected DataContext getContext() {
            return DataContext.forAbility(this.minecraft.player, this.abilityInstance);
        }

        @Override
        protected @Nullable Component getSubtitle() {
            return PalladiumClientConfig.DEV_MODE.getAsBoolean() ? Component.literal(this.abilityInstance.getReference().toString()) : null;
        }

        public AbilityReference getReference() {
            return this.abilityInstance.getReference();
        }

        public AbilityInstance<?> getAbilityInstance() {
            return this.abilityInstance;
        }

        @Override
        public Component getTitle() {
            return this.title;
        }

        @Override
        public Icon getIcon() {
            return this.icon;
        }

        @Override
        public String getKey() {
            return this.abilityInstance.getAbility().getKey();
        }

        public boolean isUnlocked() {
            return this.unlocked;
        }

        public void updateRelatives(Collection<AbilityElement> list) {
            List<? extends AbilityInstance<?>> parents = this.abilityInstance.getAbility().getStateManager()
                    .getUnlockingHandler().getParentAbilities().stream()
                    .map(reference -> reference.getInstance(this.minecraft.player,
                            this.abilityInstance.getPowerInstance()))
                    .toList();

            if (!parents.isEmpty()) {
                for (AbilityElement element : list) {
                    if (parents.contains(element.abilityInstance)) {
                        this.parents.add(element);
                        element.children.add(this);

                        this.parentsUnlocked &= element.abilityInstance.isUnlocked();
                    }
                }
            }
        }
    }

    public static class CustomElement extends Element {

        private final String key;
        private final Component title;
        @Nullable
        private final Icon icon;
        private final Component description;
        private PowerTreeState state = PowerTreeState.UNLOCKED;

        public CustomElement(String key, PowerTree.Node node) {
            this.key = key;
            this.title = node.title().orElse(Component.empty());
            this.icon = node.icon().orElse(null);
            this.description = node.description().orElse(Component.empty());
            this.gridX = node.pos().x;
            this.gridY = node.pos().y;
        }

        @Override
        public String getKey() {
            return this.key;
        }

        @Override
        public PowerTreeState getState() {
            return this.state;
        }

        public void setState(PowerTreeState state) {
            this.state = state;
        }

        @Override
        public Component getTitle() {
            return this.title;
        }

        @Override
        public @Nullable Icon getIcon() {
            return this.icon;
        }

        @Override
        protected DataContext getContext() {
            return DataContext.forEntity(this.minecraft.player);
        }

        @Override
        public boolean onClicked(Screen parent) {
            if (!this.description.getString().isBlank()) {
                ClientHooks.pushGuiLayer(parent.getMinecraft(), this.createModal(this.description));
            }

            return true;
        }
    }

}
