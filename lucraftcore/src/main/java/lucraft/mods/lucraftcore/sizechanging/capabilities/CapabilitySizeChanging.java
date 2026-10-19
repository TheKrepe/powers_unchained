package lucraft.mods.lucraftcore.sizechanging.capabilities;

import lucraft.mods.lucraftcore.LCConfig;
import lucraft.mods.lucraftcore.LucraftCore;
import lucraft.mods.lucraftcore.network.LCPacketDispatcher;
import lucraft.mods.lucraftcore.sizechanging.entities.EntitySizeChanging;
import lucraft.mods.lucraftcore.sizechanging.events.EntityCanChangeInSizeEvent;
import lucraft.mods.lucraftcore.sizechanging.events.SizeChangeEvent;
import lucraft.mods.lucraftcore.sizechanging.network.MessageSyncSizeChanging;
import lucraft.mods.lucraftcore.sizechanging.sizechanger.SizeChanger;
import lucraft.mods.lucraftcore.superpowers.effects.EffectTrail;
import lucraft.mods.lucraftcore.util.helper.LCRenderHelper;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.client.event.PlayerSPPushOutOfBlocksEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.List;

public class CapabilitySizeChanging implements ISizeChanging {

    @CapabilityInject(ISizeChanging.class)
    public static final Capability<ISizeChanging> SIZE_CHANGING_CAP = null;

    public static final double MIN_SIZE = 0.10D;
    public static final double MAX_SIZE = 16D;

    public EntityLivingBase entity;

    protected float size;
    protected float sizePerTick;
    protected float prevSize;
    protected float estimatedSize;
    protected float origWidth;
    protected float origHeight;
    protected SizeChanger sizeChanger;
    protected List<EntitySizeChanging> entities;

    public CapabilitySizeChanging(EntityLivingBase entity) {
        this.entity = entity;
        this.entities = new ArrayList<>();
    }

    @Override
    public void tick() {
        if (origWidth == 0F)
            origWidth = entity.width;
        if (origHeight == 0F)
            origHeight = entity.height;
        if (size == 0F)
            size = 1F;

        this.prevSize = size;

        if (sizePerTick != 0F) {
            this.size += sizePerTick;

            if (Math.abs(size - estimatedSize) < Math.abs(sizePerTick)) {
                this.sizePerTick = 0F;
                this.size = this.estimatedSize;
                this.getSizeChanger().end(this.entity, this, this.size);
                MinecraftForge.EVENT_BUS.post(new SizeChangeEvent.Post(this.entity, this.size, sizeChanger));
            }

            this.getSizeChanger().onSizeChanged(entity, this, this.size);
        }

        this.size = (float) MathHelper.clamp(size, MIN_SIZE, MAX_SIZE);
        updateHitbox();
        this.getSizeChanger().onUpdate(entity, this, this.size);

        if (this.entities.size() > 0) {
            List<EntitySizeChanging> copy = new ArrayList<EntitySizeChanging>(this.entities);
            for (EntitySizeChanging entity : copy) {
                if (entity == null || entity.isDead)
                    this.entities.remove(entity);
            }
        }
    }

    public void updateHitbox() {
        float size = getRenderSize(FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT ? LCRenderHelper.renderTick : 1F);
        Vec3d pos = entity.getPositionVector();
        float w = size == 0.1F && origWidth == 0.6F ? 0.0625F : origWidth * size;
        float width = w / 2F;
        float height = origHeight * size;
        entity.width = w;
        entity.height = height;
        entity.setEntityBoundingBox(new AxisAlignedBB(pos.x - width, pos.y, pos.z - width, pos.x + width, pos.y + height, pos.z + width));

        if (entity instanceof EntityPlayer) {
            ((EntityPlayer) entity).eyeHeight = ((EntityPlayer) entity).getDefaultEyeHeight() * size;
        }
    }

    @Override
    public float getSize() {
        return size;
    }

    @Override
    public float getRenderSize(float partialTick) {
        return prevSize + (size - prevSize) * partialTick;
    }

    @Override
    public boolean setSize(float size) {
        return this.setSize(size, getSizeChanger());
    }

    @Override
    public boolean setSize(float size, SizeChanger sizeChanger) {
        if (size != this.estimatedSize) {
            if (MinecraftForge.EVENT_BUS.post(new SizeChangeEvent.Pre(this.entity, this.size, size, sizeChanger)))
                return false;
            if (!sizeChanger.start(this.entity, this, this.size, size))
                return false;
            this.sizeChanger = sizeChanger;
            this.estimatedSize = size;
            this.sizePerTick = (float) (estimatedSize - this.size) / this.getSizeChanger().getSizeChangingTime(this.entity, this, estimatedSize);
            this.syncToAll();
            return true;
        }

        return false;
    }

    @Override
    public void setSizeDirectly(float size) {
        if (this.size != size) {
            this.size = (float) MathHelper.clamp(size, MIN_SIZE, MAX_SIZE);
            this.estimatedSize = size;
            this.sizePerTick = 0F;
            this.syncToAll();
            updateHitbox();
        }
    }

    @Override
    public void changeSizeChanger(SizeChanger sizeChanger) {
        if (this.sizeChanger != sizeChanger && sizeChanger != null) {
            this.sizeChanger = sizeChanger;
            this.syncToAll();
        }
    }

    @Override
    public SizeChanger getSizeChanger() {
        return this.sizeChanger == null ? SizeChanger.DEFAULT_SIZE_CHANGER : this.sizeChanger;
    }

    @Override
    public void setOriginalSize(float width, float height) {
        this.origWidth = width;
        this.origHeight = height;
        this.syncToAll();
    }

    @Override
    public float getOriginalWidth() {
        return this.origHeight;
    }

    @Override
    public float getOriginalHeight() {
        return this.origWidth;
    }

    @Override
    public EntitySizeChanging spawnEntity(SizeChanger sizeChanger, int lifeTime, float size) {
        EntitySizeChanging entity = new EntitySizeChanging(this.entity.world, this.entity, sizeChanger, size, lifeTime);
        this.entity.world.spawnEntity(entity);
        this.entities.add(entity);
        return entity;
    }

    @Override
    public List<EntitySizeChanging> getEntities() {
        return this.entities;
    }

    @Override
    public NBTTagCompound writeNBT() {
        NBTTagCompound nbt = new NBTTagCompound();

        nbt.setFloat("Size", size);
        nbt.setFloat("SizePerTick", sizePerTick);
        nbt.setFloat("EstimatedSize", estimatedSize);
        nbt.setFloat("OrigWidth", origWidth);
        nbt.setFloat("OrigHeight", origHeight);
        nbt.setString("SizeChanger", getSizeChanger().getRegistryName().toString());

        return nbt;
    }

    @Override
    public void readNBT(NBTTagCompound nbt) {
        this.size = nbt.getFloat("Size");
        this.sizePerTick = nbt.getFloat("SizePerTick");
        this.estimatedSize = nbt.getFloat("EstimatedSize");
        this.origWidth = nbt.getFloat("OrigWidth");
        this.origHeight = nbt.getFloat("OrigHeight");
        this.sizeChanger = SizeChanger.SIZE_CHANGER_REGISTRY.getValue(new ResourceLocation(nbt.getString("SizeChanger")));
    }

    @Override
    public void syncToPlayer(EntityPlayer receiver) {
        if (receiver instanceof EntityPlayerMP)
            LCPacketDispatcher.sendTo(new MessageSyncSizeChanging(this.entity), (EntityPlayerMP) receiver);
    }

    @Override
    public void syncToAll() {
        if (entity instanceof EntityPlayerMP)
            this.syncToPlayer((EntityPlayerMP) entity);
        if (entity.world instanceof WorldServer) {
            for (EntityPlayer players : ((WorldServer) entity.world).getEntityTracker().getTrackingPlayers(entity)) {
                if (players instanceof EntityPlayerMP) {
                    LCPacketDispatcher.sendTo(new MessageSyncSizeChanging(entity), (EntityPlayerMP) players);
                }
            }
        }
    }

    public static class EventHandler {

        public static boolean canChangeInSize(Entity entity) {
            if (!(entity instanceof EntityLivingBase) || entity instanceof EntitySizeChanging || entity instanceof EffectTrail.EntityTrail || MinecraftForge.EVENT_BUS.post(new EntityCanChangeInSizeEvent((EntityLivingBase) entity)))
                return false;
            return !LCConfig.SizeChanging.isEntityBlacklisted((EntityLivingBase) entity);
        }

        @Optional.Method(modid = "customnpcs")
        @SubscribeEvent
        public void onCanChangeInSize(EntityCanChangeInSizeEvent e) {
            if (e.getEntity().getClass().getCanonicalName().startsWith("noppes.npcs.entity"))
                e.setCanceled(true);
        }

        @SubscribeEvent
        public void onAttachCapabilities(AttachCapabilitiesEvent<Entity> evt) {
            if (evt.getObject().hasCapability(SIZE_CHANGING_CAP, null) || !(evt.getObject() instanceof EntityLivingBase) || !canChangeInSize(evt.getObject()))
                return;

            evt.addCapability(new ResourceLocation(LucraftCore.MODID, "size_changing"), new CapabilitySizeChangingProvider(new CapabilitySizeChanging((EntityLivingBase) evt.getObject())));
        }

        @SubscribeEvent
        public void onPlayerStartTracking(PlayerEvent.StartTracking e) {
            if (e.getTarget().hasCapability(SIZE_CHANGING_CAP, null)) {
                e.getTarget().getCapability(SIZE_CHANGING_CAP, null).syncToPlayer(e.getEntityPlayer());
            }
        }

        @SubscribeEvent
        public void onPlayerClone(PlayerEvent.Clone e) {
            NBTTagCompound compound = (NBTTagCompound) SIZE_CHANGING_CAP.getStorage().writeNBT(SIZE_CHANGING_CAP, e.getOriginal().getCapability(SIZE_CHANGING_CAP, null), null);
            SIZE_CHANGING_CAP.getStorage().readNBT(SIZE_CHANGING_CAP, e.getEntityPlayer().getCapability(SIZE_CHANGING_CAP, null), null, compound);
        }

        @SubscribeEvent
        public void onLivingUpdate(LivingEvent.LivingUpdateEvent e) {
            if (!(e.getEntityLiving() instanceof EntityPlayer) && e.getEntityLiving().hasCapability(SIZE_CHANGING_CAP, null))
                e.getEntityLiving().getCapability(SIZE_CHANGING_CAP, null).tick();
        }

        @SubscribeEvent
        public void onPlayerTick(TickEvent.PlayerTickEvent e) {
            if (e.player.hasCapability(SIZE_CHANGING_CAP, null) && e.phase == TickEvent.Phase.END)
                e.player.getCapability(SIZE_CHANGING_CAP, null).tick();
        }

        @SubscribeEvent
        public void onVisibility(PlayerEvent.Visibility e) {
            if (e.getEntityPlayer().hasCapability(SIZE_CHANGING_CAP, null))
                e.modifyVisibility(e.getEntityPlayer().getCapability(CapabilitySizeChanging.SIZE_CHANGING_CAP, null).getSize());
        }

        @SubscribeEvent
        public void onRespawn(net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerRespawnEvent e) {
            if (e.player.hasCapability(CapabilitySizeChanging.SIZE_CHANGING_CAP, null)) {
                e.player.getCapability(CapabilitySizeChanging.SIZE_CHANGING_CAP, null).setSizeDirectly(1F);
                e.player.getCapability(CapabilitySizeChanging.SIZE_CHANGING_CAP, null).syncToAll();
            }
        }

        @SubscribeEvent
        @SideOnly(Side.CLIENT)
        public void pushOutOfBlock(PlayerSPPushOutOfBlocksEvent e) {
            if (e.getEntityPlayer().getCapability(SIZE_CHANGING_CAP, null).getSize() < 1F) {
                e.setCanceled(true);
                EntityPlayer player = e.getEntityPlayer();
                AxisAlignedBB axisalignedbb = e.getEntityBoundingBox();
                float size = e.getEntityPlayer().getCapability(SIZE_CHANGING_CAP, null).getSize();

                pushPlayerSPOutOfBlocks(player, player.posX - (double) player.width * 0.35D, axisalignedbb.minY + Math.max(0.125D, (0.5D * Math.min(size, 1F))), player.posZ + (double) player.width * 0.35D);
                pushPlayerSPOutOfBlocks(player, player.posX - (double) player.width * 0.35D, axisalignedbb.minY + Math.max(0.125D, (0.5D * Math.min(size, 1F))), player.posZ - (double) player.width * 0.35D);
                pushPlayerSPOutOfBlocks(player, player.posX + (double) player.width * 0.35D, axisalignedbb.minY + Math.max(0.125D, (0.5D * Math.min(size, 1F))), player.posZ - (double) player.width * 0.35D);
                pushPlayerSPOutOfBlocks(player, player.posX + (double) player.width * 0.35D, axisalignedbb.minY + Math.max(0.125D, (0.5D * Math.min(size, 1F))), player.posZ + (double) player.width * 0.35D);
            }
        }

        private static void pushPlayerSPOutOfBlocks(EntityPlayer player, double x, double y, double z) {
            if (player.noClip) {
                return;
            } else {
                BlockPos blockpos = new BlockPos(x, y, z);
                double d0 = x - (double) blockpos.getX();
                double d1 = z - (double) blockpos.getZ();

                int entHeight = Math.max((int) Math.ceil(player.height), 1);

                boolean inTranslucentBlock = !isHeadspaceFree(player.world, blockpos, entHeight);

                if (inTranslucentBlock) {
                    int i = -1;
                    double d2 = 9999.0D;

                    if (isHeadspaceFree(player.world, blockpos.west(), entHeight) && d0 < d2) {
                        d2 = d0;
                        i = 0;
                    }

                    if (isHeadspaceFree(player.world, blockpos.east(), entHeight) && 1.0D - d0 < d2) {
                        d2 = 1.0D - d0;
                        i = 1;
                    }

                    if (isHeadspaceFree(player.world, blockpos.north(), entHeight) && d1 < d2) {
                        d2 = d1;
                        i = 4;
                    }

                    if (isHeadspaceFree(player.world, blockpos.south(), entHeight) && 1.0D - d1 < d2) {
                        d2 = 1.0D - d1;
                        i = 5;
                    }

                    float f = 0.1F;

                    if (i == 0) {
                        player.motionX = -0.10000000149011612D;
                    }

                    if (i == 1) {
                        player.motionX = 0.10000000149011612D;
                    }

                    if (i == 4) {
                        player.motionZ = -0.10000000149011612D;
                    }

                    if (i == 5) {
                        player.motionZ = 0.10000000149011612D;
                    }
                }
            }
        }

        private static boolean isHeadspaceFree(World world, BlockPos pos, int height) {
            for (int y = 0; y < height; y++) {
                if (!isOpenBlockSpace(world, pos.add(0, y, 0))) {
                    return false;
                }
            }
            return true;
        }

        private static boolean isOpenBlockSpace(World world, BlockPos pos) {
            IBlockState iblockstate = world.getBlockState(pos);
            return !iblockstate.getBlock().isNormalCube(iblockstate, world, pos);
        }

    }

    public static class Storage implements Capability.IStorage<ISizeChanging> {

        @Override
        public NBTBase writeNBT(Capability<ISizeChanging> capability, ISizeChanging instance, EnumFacing side) {
            return instance.writeNBT();
        }

        @Override
        public void readNBT(Capability<ISizeChanging> capability, ISizeChanging instance, EnumFacing side, NBTBase nbt) {
            instance.readNBT((NBTTagCompound) nbt);
        }

    }

}
