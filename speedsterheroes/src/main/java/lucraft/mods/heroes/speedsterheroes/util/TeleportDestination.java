package lucraft.mods.heroes.speedsterheroes.util;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.INBTSerializable;

public class TeleportDestination implements INBTSerializable<NBTTagCompound> {

	private String name;
	private int dimension;
	private BlockPos pos = BlockPos.ORIGIN;
	
	public TeleportDestination(String name, BlockPos pos, int dimension) {
		this.name = name;
		this.pos = pos;
		this.dimension = dimension;
	}
	
	public TeleportDestination(String name, double x, double y, double z, int dimension) {
		this(name, new BlockPos(x, y, z), dimension);
	}
	
	@Override
	public NBTTagCompound serializeNBT() {
		NBTTagCompound nbt = new NBTTagCompound();
		nbt.setString("Name", name);
		nbt.setInteger("Dimension", dimension);
		nbt.setDouble("X", pos.getX());
		nbt.setDouble("Y", pos.getY());
		nbt.setDouble("Z", pos.getZ());
		return nbt;
	}

	@Override
	public void deserializeNBT(NBTTagCompound nbt) {
		if(nbt == null) {
			System.out.println("KACKE");
			return;
		}
		this.name = nbt.getString("Name");
		this.dimension = nbt.getInteger("Dimension");
		this.pos = new BlockPos(nbt.getDouble("X"), nbt.getDouble("Y"), nbt.getDouble("Z"));
	}
	
	public String getName() {
		return name;
	}

	public int getDimensionId() {
		return dimension;
	}
	
	public BlockPos getPos() {
		return pos;
	}
	
	public double getX() {
		return getPos().getX();
	}
	
	public double getY() {
		return getPos().getY();
	}
	
	public double getZ() {
		return getPos().getZ();
	}
	
	public TeleportDestination copy() {
		return new TeleportDestination(getName(), getPos(), getDimensionId());
	}
	
	public static TeleportDestination DEFAULT = new TeleportDestination("_", BlockPos.ORIGIN, 0);
	
	public static TeleportDestination fromNBT(NBTTagCompound nbt) {
		TeleportDestination td = DEFAULT.copy();
		td.deserializeNBT(nbt);
		return td;
	}
	
}
