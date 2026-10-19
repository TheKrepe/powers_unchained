package com.thekrepe.powersunchained.client.suit;

import com.thekrepe.powersunchained.Constants;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.player.PlayerModel;

/**
 * Model layers for suits. The geometry is the player model itself, slightly inflated, so the suit
 * sits right on top of the skin like a second skin (the way Palladium draws suits), not like bulky armor.
 */
public final class SuitModelLayers {

    /** How far the suit's base layer sits above the skin (skin is 0.0, vanilla armor is 1.0). */
    public static final float SUIT_INFLATE = 0.05F;
    /** Boots are drawn a bit above the leggings so both can be worn at once. */
    public static final float BOOTS_INFLATE = 0.1F;

    public static final ModelLayerLocation SUIT = location("suit");
    public static final ModelLayerLocation SUIT_SLIM = location("suit_slim");
    public static final ModelLayerLocation BOOTS = location("suit_boots");
    public static final ModelLayerLocation BOOTS_SLIM = location("suit_boots_slim");

    private SuitModelLayers() {
    }

    public static void register(BiConsumer<ModelLayerLocation, Supplier<LayerDefinition>> registrar) {
        registrar.accept(SUIT, () -> create(SUIT_INFLATE, false));
        registrar.accept(SUIT_SLIM, () -> create(SUIT_INFLATE, true));
        registrar.accept(BOOTS, () -> create(BOOTS_INFLATE, false));
        registrar.accept(BOOTS_SLIM, () -> create(BOOTS_INFLATE, true));
    }

    private static LayerDefinition create(float inflate, boolean slim) {
        return LayerDefinition.create(PlayerModel.createMesh(new CubeDeformation(inflate), slim), 64, 64);
    }

    private static ModelLayerLocation location(String name) {
        return new ModelLayerLocation(Constants.id(name), "main");
    }
}
