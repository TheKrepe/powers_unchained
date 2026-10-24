package com.star_1mp.powersunchained.client.suit;

import java.util.EnumMap;
import java.util.Map;
import net.minecraft.client.model.geom.EntityModelSet;

/** The four suit piece models for one skin type (wide or slim). */
public final class SuitModelSet {

    private final Map<SuitModel.Part, SuitModel> models = new EnumMap<>(SuitModel.Part.class);

    private SuitModelSet() {
    }

    public static SuitModelSet bake(EntityModelSet modelSet, boolean slim) {
        SuitModelSet set = new SuitModelSet();
        for (SuitModel.Part part : SuitModel.Part.values()) {
            var layer = part == SuitModel.Part.FEET
                    ? (slim ? SuitModelLayers.BOOTS_SLIM : SuitModelLayers.BOOTS)
                    : (slim ? SuitModelLayers.SUIT_SLIM : SuitModelLayers.SUIT);
            // bakeLayer creates a new ModelPart tree every call, so every piece gets its own instance.
            SuitModel model = new SuitModel(modelSet.bakeLayer(layer), slim, part);
            model.applyVisibility();
            set.models.put(part, model);
        }
        return set;
    }

    public SuitModel get(SuitModel.Part part) {
        return this.models.get(part);
    }
}
