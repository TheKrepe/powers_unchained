package net.threetag.palladium.client.gui.ui.layout;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.core.HolderLookup;
import net.minecraft.util.ExtraCodecs;
import net.threetag.palladium.client.gui.ui.UiAlignment;
import net.threetag.palladium.client.gui.ui.screen.UiScreen;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;

import java.util.List;

public class MultiColumnLayout extends UiLayout {

    public static final MapCodec<MultiColumnLayout> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codecs.CODEC.listOf(1, 10).fieldOf("layouts").forGetter(l -> l.layouts),
            ExtraCodecs.NON_NEGATIVE_INT.optionalFieldOf("gap", 0).forGetter(l -> l.gap),
            UiAlignment.CODEC.optionalFieldOf("alignment", UiAlignment.CENTER).forGetter(MultiColumnLayout::getAlignment)
    ).apply(instance, MultiColumnLayout::new));

    private final List<UiLayout> layouts;
    private final int gap;
    private final UiAlignment alignment;
    private final int width, height;

    public MultiColumnLayout(List<UiLayout> layouts, int gap) {
        this(layouts, gap, UiAlignment.CENTER);
    }

    public MultiColumnLayout(List<UiLayout> layouts, int gap, UiAlignment alignment) {
        this.layouts = layouts;
        this.gap = gap;
        this.alignment = alignment;
        int w = 0;
        int h = 0;

        for (UiLayout layout : layouts) {
            w += layout.getWidth();
            h = Math.max(h, layout.getHeight());
        }

        w += (layouts.size() - 1) * gap;

        this.width = w;
        this.height = h;
    }

    @Override
    public UiAlignment getAlignment() {
        return this.alignment;
    }

    public List<UiLayout> getLayouts() {
        return this.layouts;
    }

    public int getGap() {
        return this.gap;
    }

    @Override
    public UiLayoutSerializer<?> getSerializer() {
        return UiLayoutSerializers.MULTI_COLUMN;
    }

    @Override
    public int getWidth() {
        return this.width;
    }

    @Override
    public int getHeight() {
        return this.height;
    }

    @Override
    public void renderBackground(GuiGraphicsExtractor guiGraphics, int x, int y, int width, int height) {
        for (UiLayout layout : this.layouts) {
            layout.renderBackground(guiGraphics, x, y, layout.getWidth(), layout.resolveHeight(height));
            x += layout.getWidth() + this.gap;
        }
    }

    @Override
    public void addWidgets(UiScreen screen, int x, int y, int width, int height, WidgetConsumer consumer) {
        for (UiLayout layout : this.layouts) {
            layout.addWidgets(screen, x, y, layout.getWidth(), layout.resolveHeight(height), consumer);
            x += layout.getWidth() + this.gap;
        }
    }

    public static class Serializer extends UiLayoutSerializer<MultiColumnLayout> {

        @Override
        public MapCodec<MultiColumnLayout> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<UiLayout, MultiColumnLayout> builder, HolderLookup.Provider provider) {
            builder.setName("Multi Column Layout")
                    .setDescription("Used for using multiple layouts next to each other horizontally.")
                    .add("layouts", TYPE_UI_LAYOUTS, "List of UI layouts.")
                    .addOptional("gap", TYPE_NON_NEGATIVE_INT, "Gap between each layout.", 0)
                    .addOptional("alignment", TYPE_UI_ALIGNMENT, "Where on the screen the layout is placed.", "center");
        }
    }
}
