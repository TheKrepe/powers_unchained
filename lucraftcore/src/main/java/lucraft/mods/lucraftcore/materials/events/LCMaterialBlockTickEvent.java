package lucraft.mods.lucraftcore.materials.events;

import lucraft.mods.lucraftcore.materials.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.eventhandler.Event;

import java.util.Random;

public class LCMaterialBlockTickEvent extends Event {

    private Material material;
    private Material.MaterialComponent component;
    private World world;
    private BlockPos pos;
    private IBlockState state;
    private Random rand;

    public LCMaterialBlockTickEvent(Material material, Material.MaterialComponent component, World worldIn, BlockPos pos, IBlockState state, Random rand) {
        this.material = material;
        this.component = component;
        this.world = worldIn;
        this.pos = pos;
        this.state = state;
        this.rand = rand;
    }

    public Material getMaterial() {
        return material;
    }

    public Material.MaterialComponent getComponent() {
        return component;
    }

    public World getWorld() {
        return world;
    }

    public BlockPos getPos() {
        return pos;
    }

    public IBlockState getState() {
        return state;
    }

    public Random getRand() {
        return rand;
    }
}
