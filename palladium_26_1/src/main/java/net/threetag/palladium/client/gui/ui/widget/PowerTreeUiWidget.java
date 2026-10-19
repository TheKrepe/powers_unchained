package net.threetag.palladium.client.gui.ui.widget;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.threetag.palladium.client.gui.screen.power.PowerUiScreen;
import net.threetag.palladium.client.gui.ui.background.RepeatingTextureBackground;
import net.threetag.palladium.client.gui.ui.screen.UiScreen;
import net.threetag.palladium.client.gui.ui.background.UiBackground;
import net.threetag.palladium.client.gui.widget.PowerTreeWidget;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.logic.context.DataContext;
import net.threetag.palladium.power.Power;
import net.threetag.palladium.power.PowerInstance;
import net.threetag.palladium.power.PowerUtil;
import net.threetag.palladium.registry.PalladiumRegistryKeys;

import javax.annotation.Nullable;
import java.util.Optional;

public class PowerTreeUiWidget extends UiWidget {

    public static final MapCodec<PowerTreeUiWidget> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ResourceKey.codec(PalladiumRegistryKeys.POWER).optionalFieldOf("power").forGetter(c -> Optional.ofNullable(c.power)),
            Identifier.CODEC.optionalFieldOf("tree").forGetter(c -> Optional.ofNullable(c.tree)),
            UiBackground.Codecs.CODEC.optionalFieldOf("background", RepeatingTextureBackground.RED_WOOL).forGetter(c -> c.background),
            propertiesCodec()
    ).apply(instance, (p, t, b, props) -> new PowerTreeUiWidget(p.orElse(null), t.orElse(null), b, props)));

    @Nullable
    private final ResourceKey<Power> power;
    @Nullable
    private final Identifier tree;
    private final UiBackground background;

    public PowerTreeUiWidget(@Nullable ResourceKey<Power> power, @Nullable Identifier tree, UiBackground background, UiWidgetProperties properties) {
        super(properties);
        this.power = power;
        this.tree = tree;
        this.background = background;
    }

    @Override
    public UiWidgetSerializer<?> getSerializer() {
        return UiWidgetSerializers.POWER_TREE;
    }

    @Override
    public AbstractWidget buildWidget(UiScreen screen, ScreenRectangle rectangle, DataContext context) {
        PowerInstance powerInstance = null;

        if (this.power != null) {
            powerInstance = PowerUtil.getPowerHandler(screen.getMinecraft().player).getPowerInstance(this.power.identifier());
        } else if (screen instanceof PowerUiScreen powerUiScreen) {
            powerInstance = powerUiScreen.getPowerInstance();
        }

        return new PowerTreeWidget(
                screen,
                powerInstance,
                this.tree,
                this.background,
                this.getX(rectangle, context),
                this.getY(rectangle, context),
                this.getWidth(context),
                this.getHeight(context));
    }

    public static class Serializer extends UiWidgetSerializer<PowerTreeUiWidget> {

        @Override
        public MapCodec<PowerTreeUiWidget> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<UiWidget, PowerTreeUiWidget> builder, HolderLookup.Provider provider) {
            builder.setName("Power Tree")
                    .setDescription("Renders an interactable tree for a power of a power")
                    .addOptional("power", TYPE_POWER, "The power that will be displayed. If none is specified, it will use the from the current power screen (if this widget is used in one).")
                    .addOptional("tree", TYPE_IDENTIFIER, "The power tree file that defines the layout, from 'assets/<namespace>/palladium/power_trees/'. If none is specified, the tree with the same ID as the power is used. Without any tree file, the layout is generated automatically.")
                    .addOptional("background", TYPE_UI_BACKGROUND, "The background that is drawn for the power tree.", RepeatingTextureBackground.RED_WOOL.toString())
                    .addExampleObject(new PowerTreeUiWidget(null, null, RepeatingTextureBackground.RED_WOOL, UiWidgetProperties.DEFAULT));
        }
    }
}
