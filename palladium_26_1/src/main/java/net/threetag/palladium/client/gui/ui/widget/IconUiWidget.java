package net.threetag.palladium.client.gui.ui.widget;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.core.HolderLookup;
import net.threetag.palladium.client.gui.ui.UiAlignment;
import net.threetag.palladium.client.renderer.icon.IconRenderer;
import net.minecraft.world.item.Items;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.icon.Icon;
import net.threetag.palladium.icon.ItemIcon;
import net.threetag.palladium.logic.context.DataContext;
import net.minecraft.world.item.ItemStackTemplate;

public final class IconUiWidget extends RenderableUiWidget {

    public static final MapCodec<IconUiWidget> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Icon.CODEC.fieldOf("icon").forGetter(IconUiWidget::icon),
            propertiesCodec16X16()
    ).apply(instance, IconUiWidget::new));

    private final Icon icon;

    public IconUiWidget(Icon icon, UiWidgetProperties properties) {
        super(properties);
        this.icon = icon;
    }

    @Override
    public void render(Minecraft minecraft, GuiGraphicsExtractor gui, DataContext context, int x, int y, int width, int height, int mouseX, int mouseY, UiAlignment alignment) {
        IconRenderer.drawIcon(this.icon, minecraft, gui, DataContext.forEntity(minecraft.player), x, y, 1F);
    }

    @Override
    public UiWidgetSerializer<?> getSerializer() {
        return UiWidgetSerializers.ICON;
    }

    public Icon icon() {
        return icon;
    }

    public static class Serializer extends UiWidgetSerializer<IconUiWidget> {

        @Override
        public MapCodec<IconUiWidget> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<UiWidget, IconUiWidget> builder, HolderLookup.Provider provider) {
            builder.setName("Icon")
                    .setDescription("Renders an icon")
                    .add("icon", TYPE_ICON, "The icon to be rendered")
                    .addExampleObject(new IconUiWidget(new ItemIcon(new ItemStackTemplate(Items.APPLE)), UiWidgetProperties.DEFAULT_16X16));
        }
    }
}
