package net.threetag.palladium.client.gui.ui.widget;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.gui.TextAlignment;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.threetag.palladium.client.gui.screen.power.PowerUiScreen;
import net.threetag.palladium.client.gui.ui.screen.UiScreen;
import net.threetag.palladium.client.util.RenderUtil;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.logic.value.holder.TextComponentValueHolder;
import net.threetag.palladium.power.Power;
import net.threetag.palladium.registry.PalladiumRegistryKeys;
import net.threetag.palladium.util.PalladiumCodecs;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.Optional;

public class PowerNameUiWidget extends AbstractStringUiWidget {

    public static final MapCodec<PowerNameUiWidget> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ResourceKey.codec(PalladiumRegistryKeys.POWER).optionalFieldOf("power").forGetter(c -> Optional.ofNullable(c.power)),
            PalladiumCodecs.COLOR_INT_CODEC.optionalFieldOf("color", RenderUtil.DEFAULT_GRAY).forGetter(PowerNameUiWidget::getColor),
            Codec.BOOL.optionalFieldOf("shadow", false).forGetter(PowerNameUiWidget::hasShadow),
            TEXT_ALIGNMENT_CODEC.optionalFieldOf("alignment", TextAlignment.LEFT).forGetter(PowerNameUiWidget::getTextAlignment),
            TEXT_OVERFLOW_CODEC.optionalFieldOf("overflow", StringWidget.TextOverflow.CLAMPED).forGetter(PowerNameUiWidget::getTextOverflow),
            propertiesCodec()
    ).apply(instance, (p, c, s, a, o, props) -> new PowerNameUiWidget(p.orElse(null), c, s, a, o, props)));

    @Nullable
    private final ResourceKey<Power> power;

    public PowerNameUiWidget(@Nullable ResourceKey<Power> power, int color, boolean shadow, TextAlignment alignment, StringWidget.TextOverflow textOverflow, UiWidgetProperties properties) {
        super(color, shadow, alignment, textOverflow, properties);
        this.power = power;
    }

    public PowerNameUiWidget(@Nullable ResourceKey<Power> power, UiWidgetProperties properties) {
        super(RenderUtil.DEFAULT_GRAY, false, TextAlignment.LEFT, StringWidget.TextOverflow.CLAMPED, properties);
        this.power = power;
    }

    @Override
    public UiWidgetSerializer<?> getSerializer() {
        return UiWidgetSerializers.POWER_NAME;
    }

    @Override
    public TextComponentValueHolder getText(UiScreen screen) {
        if (this.power != null) {
            return Objects.requireNonNull(screen.getMinecraft().level).registryAccess()
                    .lookupOrThrow(PalladiumRegistryKeys.POWER).get(this.power)
                    .map(holder -> holder.value().getName())
                    .orElse(TextComponentValueHolder.EMPTY);
        } else if (screen instanceof PowerUiScreen powerUiScreen) {
            return powerUiScreen.getPowerInstance().getPower().value().getName();
        } else {
            return TextComponentValueHolder.EMPTY;
        }
    }

    public static class Serializer extends AbstractStringUiWidgetSerializer<PowerNameUiWidget> {

        @Override
        public MapCodec<PowerNameUiWidget> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<UiWidget, PowerNameUiWidget> builder, HolderLookup.Provider provider) {
            builder.setName("Power Name")
                    .setDescription("Renders the name of a power.")
                    .addOptional("power", TYPE_POWER, "The power that will be displayed. If none is specified, it will use the from the current power screen (if this widget is used in one).")
                    .addExampleObject(new PowerNameUiWidget(null, UiWidgetProperties.DEFAULT));
        }
    }
}
