package lucraft.mods.lucraftcore.util.render;

import com.google.common.collect.ImmutableMap;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraftforge.common.model.IModelPart;
import net.minecraftforge.common.model.IModelState;
import net.minecraftforge.common.model.TRSRTransformation;

import java.util.Map;
import java.util.Optional;

public class LCModelState implements IModelState {

    private final ImmutableMap<TransformType, TRSRTransformation> map;
    private final Optional<TRSRTransformation> defaultTransform;

    public LCModelState(Map<TransformType, TRSRTransformation> map) {
        this(map, Optional.empty());
    }

    public LCModelState(Map<TransformType, TRSRTransformation> map, Optional<TRSRTransformation> defaultTransform) {
        this.map = ImmutableMap.copyOf(map);
        this.defaultTransform = defaultTransform;
    }

    @Override
    public Optional<TRSRTransformation> apply(Optional<? extends IModelPart> part) {
        if (!part.isPresent() || !(part.get() instanceof TransformType) || !map.containsKey(part.get())) {
            return defaultTransform;
        }
        return Optional.ofNullable(map.get(part.get()));
    }
}
