package net.threetag.pymtech.storage;

import com.google.common.collect.Maps;
import com.mojang.authlib.GameProfile;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.network.PacketDistributor;
import net.threetag.pymtech.PymTech;
import net.threetag.pymtech.client.renderer.item.ShrunkenStructureItemRenderer;
import net.threetag.pymtech.network.NotifyShrunkenStructureUpdate;
import net.threetag.pymtech.util.BlockUtil;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;

public class ShrunkenStructureData extends WorldSavedData {

    public static final Map<Integer, ShrunkenStructureData> shrunkenStructures = Maps.newHashMap();

    protected BlockData[][][] blockData;
    protected BlockPos size;
    protected int count;
    protected int id;
    @OnlyIn(Dist.CLIENT)
    public ShrunkenStructureItemRenderer.CachedRender renderer;

    public ShrunkenStructureData(String name) {
        super(name);
    }

    public ShrunkenStructureData(int id, BlockData[][][] blockData, BlockPos size) {
        super("shrunkenstructure_" + id);
        this.blockData = blockData;
        this.size = size;
        this.count = countEntries(blockData, size);
        this.id = id;
    }

    public boolean isEmpty() {
        return this.blockData == null || this.size == null || this.size.getX() <= 0 || this.size.getY() <= 0 || this.size.getZ() <= 0;
    }

    public BlockData[][][] getBlockData() {
        return blockData;
    }

    public BlockPos getSize() {
        return size;
    }

    public int getId() {
        return id;
    }

    public void placeInWorld(World world, BlockPos center, @Nullable PlayerEntity player) {
        if (world.isRemote || !(world instanceof ServerWorld))
            return;

        Vector3d origin = Vector3d.copy(center);

        for (int x = 0; x < this.size.getX(); x++) {
            for (int y = 0; y < this.size.getY(); y++) {
                for (int z = 0; z < this.size.getZ(); z++) {
                    BlockData data = this.blockData[x][y][z];

                    if (data != null && !data.getBlockState().isAir()) {
                        BlockState state = data.getBlockState();
                        CompoundNBT tileEntity = data.getTileEntityData();
                        Vector3d v = new Vector3d(x - (this.size.getX() / 2F), y, z - (this.size.getZ() / 2F));
                        BlockPos pos = new BlockPos(origin.x + v.x, origin.y + v.y, origin.z + v.z);
                        BlockState current = world.getBlockState(pos);
                        PlayerEntity p = player == null ? new FakePlayer((ServerWorld) world, new GameProfile(null, PymTech.MODID)) : player;

                        if (current.getBlockHardness(world, pos) == -1F || MinecraftForge.EVENT_BUS.post(new BlockEvent.BreakEvent(world, pos, current, p))) {
                            Block.spawnDrops(state, world, pos);
                        } else if (current.getBlockHardness(world, pos) > state.getBlockHardness(world, pos)) {
                            Block.spawnDrops(state, world, pos);
                        } else {
                            world.destroyBlock(pos, true);
                            world.setBlockState(pos, state, 2);

                            if (tileEntity != null && world.getTileEntity(pos) != null) {
                                tileEntity.putInt("x", pos.getX());
                                tileEntity.putInt("y", pos.getY());
                                tileEntity.putInt("z", pos.getZ());
                                world.getTileEntity(pos).read(state, tileEntity);
                            }
                        }
                    }
                }
            }
        }
    }

    public ShrunkenStructureData rotateCounterclockwise() {
        BlockPos newSize = new BlockPos(this.size.getZ(), this.size.getY(), this.size.getX());
        BlockData[][][] newData = new BlockData[newSize.getX()][newSize.getY()][newSize.getZ()];

        for (int x = 0; x < this.size.getX(); x++) {
            for (int y = 0; y < this.size.getY(); y++) {
                for (int z = 0; z < this.size.getZ(); z++) {
                    BlockData data = this.blockData[x][y][z];

                    if (data != null) {
                        newData[z][y][newSize.getZ() - 1 - x] = new BlockData(data.getBlockState().rotate(Rotation.COUNTERCLOCKWISE_90), new BlockPos(z, y, newSize.getZ() - 1 - x), data.getTileEntityData());
                    }
                }
            }
        }

        this.size = newSize;
        this.blockData = newData;
        this.markDirty();
        PymTech.NETWORK_CHANNEL.send(PacketDistributor.ALL.noArg(), new NotifyShrunkenStructureUpdate(this.id));
        return this;
    }

    @Override
    public void read(CompoundNBT nbt) {
        this.size = NBTUtil.readBlockPos(nbt.getCompound("Size"));
        this.count = nbt.getInt("Count");
        this.id = nbt.getInt("Id");
        this.blockData = new BlockData[this.size.getX()][this.size.getY()][this.size.getZ()];
        ListNBT list = nbt.getList("Blocks", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            BlockData data = new BlockData(list.getCompound(i));
            this.blockData[data.getPosition().getX()][data.getPosition().getY()][data.getPosition().getZ()] = data;
        }
        if (this.count <= 0)
            countEntries(this.blockData, this.size);
    }

    @Override
    public CompoundNBT write(CompoundNBT compound) {
        compound.put("Size", NBTUtil.writeBlockPos(this.size));
        compound.putInt("Count", this.count);
        compound.putInt("Id", this.id);
        ListNBT list = new ListNBT();
        for (int x = 0; x < this.size.getX(); x++) {
            for (int y = 0; y < this.size.getY(); y++) {
                for (int z = 0; z < this.size.getZ(); z++) {
                    BlockData data = this.blockData[x][y][z];
                    if (data != null && !data.getBlockState().isAir())
                        list.add(data.serializeNBT());
                }
            }
        }
        compound.put("Blocks", list);
        return compound;
    }

    public void writeToBuffer(PacketBuffer buffer) {
        buffer.writeBlockPos(this.size);
        buffer.writeInt(this.count);
        buffer.writeInt(this.id);
        for (int x = 0; x < this.size.getX(); x++) {
            for (int y = 0; y < this.size.getY(); y++) {
                for (int z = 0; z < this.size.getZ(); z++) {
                    BlockData data = blockData[x][y][z];
                    if (data != null) {
                        buffer.writeCompoundTag(data.serializeNBT());
                    }
                }
            }
        }
    }

    public void readFromBuffer(PacketBuffer buffer) {
        this.size = buffer.readBlockPos();
        this.blockData = new BlockData[this.size.getX()][this.size.getY()][this.size.getZ()];
        this.count = buffer.readInt();
        this.id = buffer.readInt();
        for (int i = 0; i < this.count; i++) {
            CompoundNBT nbt = buffer.readCompoundTag();
            BlockData blockData = new BlockData(null, null, null);
            blockData.deserializeNBT(nbt);
            this.blockData[blockData.getPosition().getX()][blockData.getPosition().getY()][blockData.getPosition().getZ()] = blockData;
        }
    }

    public static ShrunkenStructureData get(World world, int id) {
        String name = "shrunkenstructure_" + id;
        if (world instanceof ServerWorld) {
            return world.getServer().getWorld(World.OVERWORLD).getSavedData().get(() -> new ShrunkenStructureData(name), name);
        } else {
            final ShrunkenStructureData[] data = {null};
            DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> data[0] = shrunkenStructures.get(id));
            return data[0];
        }
    }

    public static boolean register(World world, ShrunkenStructureData data) {
        if (world instanceof ServerWorld) {
            world.getServer().getWorld(World.OVERWORLD).getSavedData().set(data);
            data.markDirty();
            return true;
        } else {
            AtomicBoolean result = new AtomicBoolean(false);
            DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> shrunkenStructures.put(data.getId(), data));
            return result.get();
        }
    }

    public static int countEntries(BlockData[][][] blockData, BlockPos size) {
        int count = 0;
        for (int x = 0; x < size.getX(); x++) {
            for (int y = 0; y < size.getY(); y++) {
                for (int z = 0; z < size.getZ(); z++) {
                    BlockData data = blockData[x][y][z];
                    if (data != null)
                        count++;
                }
            }
        }
        return count;
    }

    public static ShrunkenStructureData create(int id, World world, AxisAlignedBB axisAlignedBB, Predicate<BlockPos> predicate, boolean destroy) {
        axisAlignedBB = BlockUtil.eraseEmptyPlanes(world, axisAlignedBB);
        BlockPos size = new BlockPos((int) (axisAlignedBB.maxX - axisAlignedBB.minX), (int) (axisAlignedBB.maxY - axisAlignedBB.minY), (int) (axisAlignedBB.maxZ - axisAlignedBB.minZ));
        BlockData[][][] blockData = new BlockData[size.getX()][size.getY()][size.getZ()];

        // TODO Clear inventories of items with inventories

        for (int x = 0; x < size.getX(); x++) {
            for (int y = 0; y < size.getY(); y++) {
                for (int z = 0; z < size.getZ(); z++) {
                    BlockPos pos = new BlockPos(axisAlignedBB.minX + x, axisAlignedBB.minY + y, axisAlignedBB.minZ + z);

                    if (!world.isAirBlock(pos) && predicate.test(pos)) {
                        if (world.getTileEntity(pos) != null) {
                            blockData[x][y][z] = new BlockData(world.getBlockState(pos), new BlockPos(x, y, z), world.getTileEntity(pos).write(new CompoundNBT()));
                        } else {
                            blockData[x][y][z] = new BlockData(world.getBlockState(pos), new BlockPos(x, y, z), null);
                        }
                        if (destroy) {
                            if (world.getTileEntity(pos) != null)
                                world.removeTileEntity(pos);
                            world.setBlockState(pos, Blocks.AIR.getDefaultState(), 2);
                        }
                    }
                }
            }
        }

        return new ShrunkenStructureData(id, blockData, size);
    }

    @Mod.EventBusSubscriber(modid = PymTech.MODID, value = Dist.CLIENT)
    public static class ClientEventHandler {

        @SubscribeEvent
        public static void onLogin(ClientPlayerNetworkEvent.LoggedInEvent e) {
            shrunkenStructures.clear();
        }

        @SubscribeEvent
        public static void onLogout(ClientPlayerNetworkEvent.LoggedOutEvent e) {
            shrunkenStructures.clear();
        }

    }

    public static class BlockData implements INBTSerializable<CompoundNBT> {

        BlockState blockState;
        BlockPos pos;
        CompoundNBT tileEntityData;

        public BlockData(BlockState blockState, BlockPos position, CompoundNBT tileEntityData) {
            this.blockState = blockState;
            this.pos = position;
            this.tileEntityData = tileEntityData;
        }

        public BlockData(CompoundNBT nbt) {
            this.deserializeNBT(nbt);
        }

        public BlockState getBlockState() {
            return blockState;
        }

        public BlockPos getPosition() {
            return pos;
        }

        public CompoundNBT getTileEntityData() {
            return tileEntityData;
        }

        public boolean hasTileEntity() {
            return this.tileEntityData != null;
        }

        @Override
        public CompoundNBT serializeNBT() {
            CompoundNBT nbt = new CompoundNBT();
            // TODO Do mappings like template nbt files
            nbt.put("Block", NBTUtil.writeBlockState(this.blockState));
            nbt.put("Position", NBTUtil.writeBlockPos(this.pos));
            if (this.hasTileEntity())
                nbt.put("TileEntityData", this.tileEntityData);
            return nbt;
        }

        @Override
        public void deserializeNBT(CompoundNBT nbt) {
            this.blockState = NBTUtil.readBlockState(nbt.getCompound("Block"));
            this.pos = NBTUtil.readBlockPos(nbt.getCompound("Position"));
            if (nbt.contains("TileEntityData"))
                this.tileEntityData = nbt.getCompound("TileEntityData");
        }

    }

}
