package net.threetag.palladium.client.gui.screen.abilitybar;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.threetag.palladium.Palladium;
import net.threetag.palladium.client.texture.TextureReference;
import net.threetag.palladium.logic.context.DataContext;
import net.threetag.palladium.power.ability.AbilityColor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class AbilityBarSprites {

    private static final List<String> NAMES = new ArrayList<>();
    private static final Identifier DEFAULT_FOLDER = Palladium.id("ability_bar");

    public static final String POWER_INDICATOR_TOP_LEFT = register("power_indicator_top_left");
    public static final String POWER_INDICATOR_TOP_RIGHT = register("power_indicator_top_right");
    public static final String POWER_INDICATOR_BOTTOM_LEFT = register("power_indicator_bottom_left");
    public static final String POWER_INDICATOR_BOTTOM_RIGHT = register("power_indicator_bottom_right");
    public static final String ABILITY_LIST = register("ability_list");
    public static final String SINGLE_ABILITY = register("single_ability");
    public static final String SLOT_EMPTY = register("slot_empty");
    public static final String SLOT_DISABLED = register("slot_disabled");
    public static final String SLOT_ENABLED = register("slot_enabled");
    public static final String SLOT_LOCKED = register("slot_locked");
    public static final String SLOT_LOCK = register("slot_lock");
    public static final String SLOT_COOLDOWN = register("slot_cooldown");
    public static final String SWITCH_KEY_FORWARD = register("switch_key_forward");
    public static final String SWITCH_KEY_BACKWARD = register("switch_key_backward");
    public static final String KEY_JUMP = register("key_jump");
    public static final String KEY_MOUSE_LEFT = register("key_mouse_left");
    public static final String KEY_MOUSE_RIGHT = register("key_mouse_right");
    public static final String KEY_MOUSE_MIDDLE = register("key_mouse_middle");
    public static final String ENERGY_BAR = register("energy_bar");
    public static final String ENERGY_BAR_FILL = register("energy_bar_fill");

    static {
        for (AbilityColor color : AbilityColor.values()) {
            register(slotName(color));
        }
    }

    public static final AbilityBarSprites DEFAULT = new AbilityBarSprites(DEFAULT_FOLDER, Collections.emptyMap());

    public static final Codec<AbilityBarSprites> CODEC = Codec.either(Identifier.CODEC, Codec.unboundedMap(Codec.STRING, TextureReference.CODEC)).comapFlatMap(
            either -> either.map(
                    folder -> DataResult.success(new AbilityBarSprites(folder, Collections.emptyMap())),
                    AbilityBarSprites::fromMap
            ),
            sprites -> sprites.overrides.isEmpty() ? Either.left(sprites.folder) : Either.right(sprites.overrides)
    );

    private final Identifier folder;
    private final Map<String, TextureReference> overrides;

    private AbilityBarSprites(Identifier folder, Map<String, TextureReference> overrides) {
        this.folder = folder;
        this.overrides = overrides;
    }

    private static DataResult<AbilityBarSprites> fromMap(Map<String, TextureReference> map) {
        for (String key : map.keySet()) {
            if (!NAMES.contains(key)) {
                return DataResult.error(() -> "Unknown ability bar texture '" + key + "', must be one of " + NAMES);
            }
        }

        return DataResult.success(new AbilityBarSprites(DEFAULT_FOLDER, Map.copyOf(map)));
    }

    private static String register(String name) {
        NAMES.add(name);
        return name;
    }

    public static String slotName(AbilityColor color) {
        return "slot_" + color.getSerializedName();
    }

    public static String powerIndicatorName(AbilityBarAlignment alignment) {
        return switch (alignment) {
            case TOP_LEFT -> POWER_INDICATOR_TOP_LEFT;
            case TOP_RIGHT -> POWER_INDICATOR_TOP_RIGHT;
            case BOTTOM_LEFT -> POWER_INDICATOR_BOTTOM_LEFT;
            case BOTTOM_RIGHT -> POWER_INDICATOR_BOTTOM_RIGHT;
        };
    }

    public TextureReference get(String name) {
        var override = this.overrides.get(name);
        return override != null ? override : TextureReference.normal(this.folder.withSuffix("/" + name));
    }

    public void blit(GuiGraphicsExtractor gui, String name, DataContext context, int x, int y, int width, int height) {
        this.blit(gui, name, context, width, height, 0, 0, x, y, width, height, -1);
    }

    public void blit(GuiGraphicsExtractor gui, String name, DataContext context, int spriteWidth, int spriteHeight, int textureX, int textureY, int x, int y, int width, int height) {
        this.blit(gui, name, context, spriteWidth, spriteHeight, textureX, textureY, x, y, width, height, -1);
    }

    public void blit(GuiGraphicsExtractor gui, String name, DataContext context, int spriteWidth, int spriteHeight, int textureX, int textureY, int x, int y, int width, int height, int color) {
        var reference = this.get(name);

        if (reference.isDynamic()) {
            var texture = reference.getTexture(context);

            if (texture != null) {
                gui.blit(RenderPipelines.GUI_TEXTURED, texture, x, y, textureX, textureY, width, height, spriteWidth, spriteHeight, color);
            }
        } else {
            gui.blitSprite(RenderPipelines.GUI_TEXTURED, reference.getPath(), spriteWidth, spriteHeight, textureX, textureY, x, y, width, height, color);
        }
    }
}
