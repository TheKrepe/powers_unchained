package net.threetag.palladium.client.gui.ui.widget;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.ItemDisplayWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.threetag.palladium.client.gui.ui.screen.UiScreen;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.logic.context.DataContext;

public class ItemUiWidget extends UiWidget {

    public static final MapCodec<ItemUiWidget> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ItemStackTemplate.CODEC.fieldOf("item").forGetter(c -> c.itemStack),
            Codec.BOOL.optionalFieldOf("decorations", true).forGetter(c -> c.decorations),
            Codec.BOOL.optionalFieldOf("tooltip", true).forGetter(c -> c.tooltip),
            propertiesCodec16X16()
    ).apply(instance, ItemUiWidget::new));

    private final ItemStackTemplate itemStack;
    private final boolean decorations;
    private final boolean tooltip;

    public ItemUiWidget(ItemStackTemplate itemStack, boolean decorations, boolean tooltip, UiWidgetProperties properties) {
        super(properties);
        this.itemStack = itemStack;
        this.decorations = decorations;
        this.tooltip = tooltip;
    }

    @Override
    public UiWidgetSerializer<?> getSerializer() {
        return UiWidgetSerializers.ITEM;
    }

    @Override
    public AbstractWidget buildWidget(UiScreen screen, ScreenRectangle rectangle, DataContext context) {
        var stack = this.itemStack.create();
        var widget = new ItemDisplayWidget(
                screen.getMinecraft(),
                0, 0,
                this.getWidth(context), this.getHeight(context),
                stack.getHoverName(),
                stack,
                this.decorations,
                this.tooltip
        );

        widget.setPosition(this.getX(rectangle, context), this.getY(rectangle, context));

        if (!this.tooltip) {
            this.getProperties().tooltip().ifPresent(component -> widget.setTooltip(Tooltip.create(component)));
        }

        return widget;
    }

    public static class Serializer extends UiWidgetSerializer<ItemUiWidget> {

        @Override
        public MapCodec<ItemUiWidget> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<UiWidget, ItemUiWidget> builder, HolderLookup.Provider provider) {
            builder.setName("Item")
                    .setDescription("Renders an item")
                    .add("item", TYPE_ITEM_STACK, "The item stack to be rendered")
                    .addOptional("decorations", TYPE_BOOLEAN, "Whether or not item stack \"decorations\" should be drawn (count, damage, etc.).", true)
                    .addOptional("tooltip", TYPE_BOOLEAN, "Whether or not the default tooltip for the item information should be rendered. If enabled, this will override the tooltip of the properties.", true)
                    .addExampleObject(new ItemUiWidget(new ItemStackTemplate(Items.APPLE), true, true, UiWidgetProperties.DEFAULT_16X16));
        }
    }
}
