package net.threetag.palladium.client.gui.ui.widget;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceKey;
import net.threetag.palladium.client.gui.screen.power.PowerUiScreen;
import net.threetag.palladium.client.gui.ui.background.RepeatingTextureBackground;
import net.threetag.palladium.client.gui.ui.background.UiBackground;
import net.threetag.palladium.client.gui.ui.screen.UiScreen;
import net.threetag.palladium.client.gui.widget.PowerListWidget;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.logic.context.DataContext;
import net.threetag.palladium.power.Power;
import net.threetag.palladium.power.PowerInstance;
import net.threetag.palladium.power.PowerUtil;
import net.threetag.palladium.registry.PalladiumRegistryKeys;

import javax.annotation.Nullable;
import java.util.Optional;

public class PowerListUiWidget extends UiWidget {

    public static final MapCodec<PowerListUiWidget> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ResourceKey.codec(PalladiumRegistryKeys.POWER).optionalFieldOf("power").forGetter(c -> Optional.ofNullable(c.power)),
            UiBackground.Codecs.CODEC.optionalFieldOf("background", RepeatingTextureBackground.RED_WOOL).forGetter(c -> c.background),
            propertiesCodec()
    ).apply(instance, (p, b, props) -> new PowerListUiWidget(p.orElse(null), b, props)));

    @Nullable
    private final ResourceKey<Power> power;
    private final UiBackground background;

    public PowerListUiWidget(@Nullable ResourceKey<Power> power, UiBackground background, UiWidgetProperties properties) {
        super(properties);
        this.power = power;
        this.background = background;
    }

    @Override
    public UiWidgetSerializer<?> getSerializer() {
        return UiWidgetSerializers.POWER_LIST;
    }

    @Override
    public AbstractWidget buildWidget(UiScreen screen, ScreenRectangle rectangle, DataContext context) {
        PowerInstance powerInstance = null;

        if (this.power != null) {
            powerInstance = PowerUtil.getPowerHandler(screen.getMinecraft().player).getPowerInstance(this.power.identifier());
        } else if (screen instanceof PowerUiScreen powerUiScreen) {
            powerInstance = powerUiScreen.getPowerInstance();
        }

        return new PowerListWidget(
                screen,
                powerInstance,
                this.background,
                this.getX(rectangle, context),
                this.getY(rectangle, context),
                this.getWidth(context),
                this.getHeight(context));
    }

    public static class Serializer extends UiWidgetSerializer<PowerListUiWidget> {

        @Override
        public MapCodec<PowerListUiWidget> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<UiWidget, PowerListUiWidget> builder, HolderLookup.Provider provider) {
            builder.setName("Power List")
                    .setDescription("Renders a scrollable list of all abilities of a power")
                    .addOptional("power", TYPE_POWER, "The power that will be displayed. If none is specified, it will use the from the current power screen (if this widget is used in one).")
                    .addOptional("background", TYPE_UI_BACKGROUND, "The background that is drawn behind the list.", RepeatingTextureBackground.RED_WOOL.toString())
                    .addExampleObject(new PowerListUiWidget(null, RepeatingTextureBackground.RED_WOOL, UiWidgetProperties.DEFAULT));
        }
    }
}
