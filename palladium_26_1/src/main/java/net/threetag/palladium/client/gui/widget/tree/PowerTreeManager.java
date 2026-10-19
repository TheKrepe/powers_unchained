package net.threetag.palladium.client.gui.widget.tree;

import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.threetag.palladium.Palladium;
import net.threetag.palladium.addonpack.log.AddonPackLog;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class PowerTreeManager extends SimpleJsonResourceReloadListener<PowerTree> {

    public static final Identifier ID = Palladium.id("power_trees");
    public static final PowerTreeManager INSTANCE = new PowerTreeManager();

    private final Map<Identifier, PowerTree> byName = new HashMap<>();

    protected PowerTreeManager() {
        super(PowerTree.CODEC, FileToIdConverter.json("palladium/power_trees"));
    }

    @Override
    protected void apply(Map<Identifier, PowerTree> objects, ResourceManager resourceManager, ProfilerFiller profiler) {
        this.byName.clear();
        objects.forEach((id, tree) -> {
            tree.validate(problem -> AddonPackLog.warning("Power tree {}: {}", id, problem));
            this.byName.put(id, tree);
        });
        AddonPackLog.info("Loaded " + this.byName.size() + " power trees");
    }

    @Nullable
    public PowerTree get(Identifier id) {
        return this.byName.get(id);
    }
}
