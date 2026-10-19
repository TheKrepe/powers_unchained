package net.threetag.pymtech.storage;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.WorldSavedData;

import java.util.Iterator;

public class ShrunkenStructureIdTracker extends WorldSavedData {

    private final Object2IntMap<String> map = new Object2IntOpenHashMap();

    public ShrunkenStructureIdTracker() {
        super("shrunkenstructureidcounts");
        this.map.defaultReturnValue(-1);
    }

    @Override
    public void read(CompoundNBT nbt) {
        this.map.clear();
        Iterator var2 = nbt.keySet().iterator();

        while (var2.hasNext()) {
            String key = (String) var2.next();
            if (nbt.contains(key, 99)) {
                this.map.put(key, nbt.getInt(key));
            }
        }
    }

    @Override
    public CompoundNBT write(CompoundNBT nbt) {
        ObjectIterator var2 = this.map.object2IntEntrySet().iterator();

        while (var2.hasNext()) {
            Object2IntMap.Entry<String> entry = (Object2IntMap.Entry) var2.next();
            nbt.putInt(entry.getKey(), entry.getIntValue());
        }

        return nbt;
    }

    public int getNextId() {
        int id = this.map.getInt("shrunkenstructure") + 1;
        this.map.put("shrunkenstructure", id);
        this.markDirty();
        return id;
    }

    public static int getNextId(World world) {
        int i = 0;
        if (world instanceof ServerWorld) {
            i = world.getServer().getWorld(World.OVERWORLD).getSavedData().getOrCreate(ShrunkenStructureIdTracker::new, "shrunkenstructureidcounts").getNextId();
        }
        return i;
    }

}
