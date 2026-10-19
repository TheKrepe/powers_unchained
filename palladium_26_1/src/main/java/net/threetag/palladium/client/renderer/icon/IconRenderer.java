package net.threetag.palladium.client.renderer.icon;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.neoforged.fml.loading.FMLEnvironment;
import net.threetag.palladium.Palladium;
import net.threetag.palladium.icon.Icon;
import net.threetag.palladium.icon.IconSerializer;
import net.threetag.palladium.icon.IconSerializers;
import net.threetag.palladium.logic.context.DataContext;
import net.threetag.palladium.registry.PalladiumRegistries;

import java.util.HashMap;
import java.util.Map;

public interface IconRenderer<T extends Icon> {

    Map<IconSerializer<?>, IconRenderer<?>> RENDERER_MAP = new HashMap<>();

    static void registerRenderers() {
        registerRenderer(IconSerializers.COMPOUND.get(), new CompoundIconRenderer());
        registerRenderer(IconSerializers.ITEM.get(), new ItemIconRenderer());
        registerRenderer(IconSerializers.ITEM_IN_SLOT.get(), new ItemInSlotIconRenderer());
        registerRenderer(IconSerializers.INGREDIENT.get(), new IngredientIconRenderer());
        registerRenderer(IconSerializers.TEXTURE.get(), new TexturedIconRenderer());
        registerRenderer(IconSerializers.SPRITE.get(), new SpriteIconRenderer());
        registerRenderer(IconSerializers.EXPERIENCE.get(), new ExperienceIconRenderer());
    }

    static <T extends Icon> void registerRenderer(IconSerializer<T> serializer, IconRenderer<T> renderer) {
        if (!FMLEnvironment.isProduction()) {
            Palladium.LOGGER.info("Registering icon renderer for {}: {}", PalladiumRegistries.ICON_SERIALIZER.getKey(serializer), renderer.getClass().getName());
        }
        RENDERER_MAP.put(serializer, renderer);
    }

    @SuppressWarnings("unchecked")
    static <T extends Icon> IconRenderer<T> getRenderer(T icon) {
        IconRenderer<T> renderer = (IconRenderer<T>) RENDERER_MAP.get(icon.getSerializer());

        if (renderer == null) {
            throw new IllegalStateException(String.format("No icon renderer registered for %s", PalladiumRegistries.ICON_SERIALIZER.getKey(icon.getSerializer()).toString()));
        }

        return renderer;
    }

    static void drawIcon(Icon icon, Minecraft mc, GuiGraphicsExtractor guiGraphics, DataContext context, int x, int y, float alpha) {
        getRenderer(icon).draw(icon, mc, guiGraphics, context, x, y, 16, 16, alpha);
    }

    static void drawIcon(Icon icon, Minecraft mc, GuiGraphicsExtractor guiGraphics, DataContext context, int x, int y, int width, int height, float alpha) {
        getRenderer(icon).draw(icon, mc, guiGraphics, context, x, y, width, height, alpha);
    }

    default void draw(T icon, Minecraft mc, GuiGraphicsExtractor guiGraphics, DataContext context, int x, int y, float alpha) {
        this.draw(icon, mc, guiGraphics, context, x, y, 16, 16, alpha);
    }

    void draw(T icon, Minecraft mc, GuiGraphicsExtractor guiGraphics, DataContext context, int x, int y, int width, int height, float alpha);

}
