package net.threetag.palladium.client.animation;

import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.threetag.palladium.Palladium;
import net.threetag.palladium.addonpack.log.AddonPackLog;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;

public class PackAnimationControllerManager extends SimpleJsonResourceReloadListener<PackAnimationController> {

    public static final Identifier ID = Palladium.id("animation_controllers");
    public static final PackAnimationControllerManager INSTANCE = new PackAnimationControllerManager();

    private final Map<Identifier, PackAnimationController> byName = new HashMap<>();

    protected PackAnimationControllerManager() {
        super(PackAnimationController.Codecs.SIMPLE_CODEC, FileToIdConverter.json("palladium/animation_controllers"));
    }

    @Override
    protected void apply(Map<Identifier, PackAnimationController> objects, ResourceManager resourceManager, ProfilerFiller profiler) {
        this.byName.clear();
        for (Map.Entry<Identifier, PackAnimationController> entry : objects.entrySet()) {
            PackAnimationController merged = mergeParents(entry.getKey(), objects, new LinkedHashSet<>());
            if (merged != null) {
                this.byName.put(entry.getKey(), merged.resolve());
            }
        }
        AddonPackLog.info("Loaded " + this.byName.size() + " animation controllers");
    }

    private PackAnimationController mergeParents(Identifier id, Map<Identifier, PackAnimationController> raw, LinkedHashSet<Identifier> visited) {
        PackAnimationController controller = raw.get(id);
        if (controller == null) {
            AddonPackLog.error("Animation controller not found: " + id);
            return null;
        }

        if (controller.parent().isEmpty()) {
            return controller;
        }

        Identifier parentId = controller.parent().get();
        if (visited.contains(parentId)) {
            AddonPackLog.error("Circular parent reference in animation controller: " + id + " -> " + parentId);
            return controller;
        }

        visited.add(id);
        PackAnimationController parent = mergeParents(parentId, raw, visited);
        if (parent == null) {
            AddonPackLog.error("Parent animation controller not found for: " + id);
            return controller;
        }

        if (parent.getSerializer() != controller.getSerializer()) {
            AddonPackLog.error("Animation controller " + id + " cannot inherit from " + parentId + ": controller types differ");
            return controller;
        }

        return controller.merge(parent);
    }

    public PackAnimationController get(Identifier id) {
        return this.byName.get(id);
    }
}
