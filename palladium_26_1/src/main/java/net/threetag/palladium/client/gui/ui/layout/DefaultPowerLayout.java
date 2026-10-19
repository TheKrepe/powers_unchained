package net.threetag.palladium.client.gui.ui.layout;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.core.HolderLookup;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.LivingEntity;
import net.threetag.palladium.client.gui.screen.ModalScreen;
import net.threetag.palladium.client.gui.ui.UiAlignment;
import net.threetag.palladium.client.gui.ui.background.RepeatingTextureBackground;
import net.threetag.palladium.client.gui.ui.background.SpriteBackground;
import net.threetag.palladium.client.gui.ui.background.UiBackground;
import net.threetag.palladium.client.gui.screen.power.PowerUiScreen;
import net.threetag.palladium.client.gui.ui.widget.PowerListUiWidget;
import net.threetag.palladium.client.gui.ui.widget.PowerNameUiWidget;
import net.threetag.palladium.client.gui.ui.widget.PowerTreeUiWidget;
import net.threetag.palladium.client.gui.ui.widget.UiWidget;
import net.threetag.palladium.client.gui.ui.widget.UiWidgetProperties;
import net.threetag.palladium.client.gui.widget.PowerTreePopulator;
import net.threetag.palladium.client.gui.ui.screen.UiPadding;
import net.threetag.palladium.client.gui.ui.screen.UiScreen;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.documentation.SettingType;
import net.threetag.palladium.power.PowerInstance;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

public class DefaultPowerLayout extends UiLayout {

    public static final MapCodec<DefaultPowerLayout> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ExtraCodecs.POSITIVE_INT.optionalFieldOf("width", 256).forGetter(DefaultPowerLayout::getWidth),
            ExtraCodecs.POSITIVE_INT.optionalFieldOf("height", 196).forGetter(DefaultPowerLayout::getHeight),
            UiBackground.Codecs.CODEC.optionalFieldOf("background", RepeatingTextureBackground.RED_WOOL).forGetter(l -> l.treeBackground),
            DisplayType.CODEC.optionalFieldOf("display_type", DisplayType.AUTO).forGetter(l -> l.displayType)
    ).apply(instance, DefaultPowerLayout::new));

    private final int width;
    private final int height;
    private final UiBackground background;
    private final UiBackground treeBackground;
    private final DisplayType displayType;

    public DefaultPowerLayout(int width, int height, UiBackground treeBackground, DisplayType displayType) {
        this.width = width;
        this.height = height;
        this.background = new SpriteBackground(ModalScreen.BACKGROUND_MODAL_HEADER);
        this.treeBackground = treeBackground;
        this.displayType = displayType;
    }

    @Override
    public UiLayoutSerializer<?> getSerializer() {
        return UiLayoutSerializers.POWER_DEFAULT;
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
        this.background.render(guiGraphics, x, y, width, height, 1F);
    }

    @Override
    public void addWidgets(UiScreen screen, int x, int y, int width, int height, WidgetConsumer consumer) {
        var padding = UiPadding.SEVEN;
        var rectangle = new ScreenRectangle(
                x + padding.left(),
                y + padding.top(),
                width - padding.horizontal(),
                height - padding.vertical());

        var context = screen.getContext();

        var title = new PowerNameUiWidget(null, UiWidgetProperties.withSize(rectangle.width(), 10));
        consumer.accept(title, title.buildWidget(screen, rectangle, context), rectangle);

        var powerInstance = screen instanceof PowerUiScreen powerUiScreen ? powerUiScreen.getPowerInstance() : null;
        var properties = UiWidgetProperties.alignWithSize(
                UiAlignment.BOTTOM_CENTER, width - padding.vertical(), height - 20 - padding.bottom());

        UiWidget abilities = this.displayType.isTree(screen.getMinecraft().player, powerInstance)
                ? new PowerTreeUiWidget(null, null, this.treeBackground, properties)
                : new PowerListUiWidget(null, this.treeBackground, properties);
        consumer.accept(abilities, abilities.buildWidget(screen, rectangle, context), rectangle);
    }

    public enum DisplayType implements StringRepresentable {

        AUTO("auto"),
        TREE("tree"),
        LIST("list");

        public static final Codec<DisplayType> CODEC = StringRepresentable.fromEnum(DisplayType::values);

        private final String name;

        DisplayType(String name) {
            this.name = name;
        }

        public boolean isTree(@Nullable LivingEntity entity, @Nullable PowerInstance powerInstance) {
            return switch (this) {
                case AUTO -> PowerTreePopulator.canBeTree(entity, powerInstance);
                case TREE -> true;
                case LIST -> false;
            };
        }

        @Override
        public @NonNull String getSerializedName() {
            return this.name;
        }
    }

    public static class Serializer extends UiLayoutSerializer<DefaultPowerLayout> {

        @Override
        public MapCodec<DefaultPowerLayout> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<UiLayout, DefaultPowerLayout> builder, HolderLookup.Provider provider) {
            builder.setName("Default Power Layout")
                    .setDescription("The default layout for power screens")
                    .addOptional("width", TYPE_POSITIVE_INT, "Width of this layout", 256)
                    .addOptional("height", TYPE_POSITIVE_INT, "Height of this layout", 196)
                    .addOptional("background", TYPE_UI_BACKGROUND, "The background that is drawn for the power tree.", RepeatingTextureBackground.RED_WOOL)
                    .addOptional("display_type", SettingType.enumList(DisplayType.values()), "How the abilities are displayed. \"auto\" uses a tree if the power has a power tree file or the abilities are connected to each other, otherwise a list.", DisplayType.AUTO.getSerializedName());
        }
    }
}
