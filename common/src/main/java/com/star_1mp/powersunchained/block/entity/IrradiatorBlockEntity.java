package com.star_1mp.powersunchained.block.entity;

import com.star_1mp.powersunchained.block.IrradiatorBlock;
import com.star_1mp.powersunchained.entity.spider.RadioactiveSpider;
import com.star_1mp.powersunchained.irradiation.Irradiation;
import com.star_1mp.powersunchained.irradiation.IrradiationRecipes;
import com.star_1mp.powersunchained.irradiation.IrradiatorShake;
import com.star_1mp.powersunchained.menu.IrradiatorMenu;
import com.star_1mp.powersunchained.registry.PUAdvancements;
import com.star_1mp.powersunchained.registry.PUBlockEntities;
import com.star_1mp.powersunchained.registry.PUDamageTypes;
import com.star_1mp.powersunchained.registry.PUEntities;
import com.star_1mp.powersunchained.registry.PUItems;
import com.star_1mp.powersunchained.web.WebSlowness;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ConversionParams;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.spider.CaveSpider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import org.jspecify.annotations.Nullable;

/**
 * The Irradiator (owner's spec). Fuel = Isotope Capsule (one per irradiation, replaced by an empty cartridge when it is
 * done); 150 s; started by the screen's button or a redstone pulse. What it irradiates:
 * <ul>
 *   <li>a living creature standing ON the block (has priority): frozen in place (shakes like a curing zombie villager),
 *   darkness + nausea until the end. A cave spider becomes a Radioactive Spider (70 %) or dies for real (vanilla loot);
 *   a player survives with 5 min of poison (75 %) or dies; any other mob the same with 70 %;</li>
 *   <li>else the item in the sample slot ({@link IrradiationRecipes}: spider eye -> radioactive spider eye 70 %, else
 *   it burns up).</li>
 * </ul>
 * Leaving the block / dying / taking the capsule out stops it (nothing is used up). Geiger-like sculk clicks, green
 * sparks, the core glows brighter while it runs (block state {@code active}).
 */
public class IrradiatorBlockEntity extends BlockEntity implements Container, MenuProvider {

    public static final int SLOT_FUEL = 0;
    public static final int SLOT_INPUT = 1;
    public static final int SLOT_OUTPUT = 2;
    public static final int SLOTS = 3;

    public static final int MODE_NONE = 0;
    public static final int MODE_ENTITY = 1;
    public static final int MODE_ITEM = 2;

    // container data (shorts over the network: the entity id is split in two)
    public static final int DATA_PROGRESS = 0;
    public static final int DATA_RUNNING = 1;
    public static final int DATA_MODE = 2;
    public static final int DATA_TARGET_LO = 3;
    public static final int DATA_TARGET_HI = 4;
    public static final int DATA_KIND = 5;
    public static final int DATA_SIZE = 6;

    private final NonNullList<ItemStack> items = NonNullList.withSize(SLOTS, ItemStack.EMPTY);
    private boolean running;
    private int progress;
    private int mode = MODE_NONE;
    private @Nullable UUID targetUuid;
    /** Entity id of the creature on the block (running: the irradiated one), -1 none; synced for the screen / shaking. */
    private int targetId = -1;
    private int targetKind = Irradiation.KIND_NONE;
    private boolean wasPowered;
    /** Client: id to shake (from the update packet). */
    private int shakeId = -1;

    private final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            IrradiatorBlockEntity be = IrradiatorBlockEntity.this;
            return switch (index) {
                case DATA_PROGRESS -> be.progress;
                case DATA_RUNNING -> be.running ? 1 : 0;
                case DATA_MODE -> be.running ? be.mode : MODE_NONE;
                case DATA_TARGET_LO -> be.targetId & 0xFFFF;
                case DATA_TARGET_HI -> (be.targetId >> 16) & 0xFFFF;
                case DATA_KIND -> be.targetKind;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
        }

        @Override
        public int getCount() {
            return DATA_SIZE;
        }
    };

    public IrradiatorBlockEntity(BlockPos pos, BlockState state) {
        super(PUBlockEntities.IRRADIATOR.get(), pos, state);
    }

    // =====================================================================================================
    // ticking
    // =====================================================================================================

    public static void serverTick(Level level, BlockPos pos, BlockState state, IrradiatorBlockEntity be) {
        if (level instanceof ServerLevel server) {
            be.tick(server, pos);
        }
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, IrradiatorBlockEntity be) {
        if (be.shakeId >= 0) {
            IrradiatorShake.mark(be.shakeId, level.getGameTime());
            if (level.getRandom().nextFloat() < 0.35F) {
                level.addParticle(RadioactiveSpider.SPARK, pos.getX() + level.getRandom().nextDouble(), pos.getY() + 1.0 + level.getRandom().nextDouble() * 0.8,
                        pos.getZ() + level.getRandom().nextDouble(), 0.0, 0.03, 0.0);
            }
        }
    }

    private void tick(ServerLevel level, BlockPos pos) {
        if (!this.running) {
            if (level.getGameTime() % 5 == 0) {
                LivingEntity on = this.creatureOnTop(level, pos);
                int id = on == null ? -1 : on.getId();
                this.targetKind = on == null ? Irradiation.KIND_NONE : Irradiation.kind(on);
                this.targetId = id;
                if (on != null && this.targetKind == Irradiation.KIND_CAVE_SPIDER && level.getGameTime() % 20 == 0) {
                    for (Player p : level.players()) {
                        if (p instanceof ServerPlayer sp && p.distanceToSqr(pos.getCenter()) < 16 * 16) {
                            PUAdvancements.award(sp, PUAdvancements.SAMPLE);
                        }
                    }
                }
            }
            return;
        }
        if (!this.hasFuel()) {
            this.stop(level, pos);
            return;
        }
        LivingEntity target = null;
        if (this.mode == MODE_ENTITY) {
            Entity e = this.targetUuid == null ? null : level.getEntity(this.targetUuid);
            if (!(e instanceof LivingEntity living) || !living.isAlive() || !this.isOnTop(living, pos)) {
                this.stop(level, pos);
                return;
            }
            target = living;
            this.hold(level, living, pos);
        } else if (IrradiationRecipes.find(this.items.get(SLOT_INPUT)) == null) {
            this.stop(level, pos);
            return;
        }
        // geiger-like clicks, more often the higher the dose
        float dose = this.progress / (float) Irradiation.DURATION;
        if (level.getRandom().nextFloat() < 0.25F + 0.45F * dose) {
            level.playSound(null, pos, SoundEvents.SCULK_CLICKING, SoundSource.BLOCKS, 0.45F, 0.9F + level.getRandom().nextFloat() * 0.9F);
        }
        if (this.progress % 4 == 0) {
            double y = target != null ? target.getY() + target.getBbHeight() * 0.5 : pos.getY() + 1.1;
            level.sendParticles(RadioactiveSpider.SPARK, pos.getX() + 0.5, y, pos.getZ() + 0.5, 2, 0.3, 0.25, 0.3, 0.01);
        }
        if (++this.progress >= Irradiation.DURATION) {
            this.finish(level, pos, target);
        }
    }

    /** Frozen in the middle of the block, darkness + nausea until it is over. */
    private void hold(ServerLevel level, LivingEntity entity, BlockPos pos) {
        WebSlowness.hold(entity);
        if (entity instanceof Mob mob) {
            mob.getNavigation().stop();
        }
        double cx = pos.getX() + 0.5;
        double cz = pos.getZ() + 0.5;
        if (Math.abs(entity.getX() - cx) > 0.2 || Math.abs(entity.getZ() - cz) > 0.2) {
            entity.teleportTo(cx, entity.getY(), cz);
        }
        if (this.progress % 20 == 0) {
            entity.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 60, 0, false, false, true));
            entity.addEffect(new MobEffectInstance(MobEffects.NAUSEA, 100, 0, false, false, true));
        }
    }

    // =====================================================================================================
    // start / finish
    // =====================================================================================================

    /**
     * Button or redstone pulse. @param starter who pressed the button (null: redstone)
     * @return whether an irradiation started
     */
    public boolean start(ServerLevel level, @Nullable ServerPlayer starter) {
        BlockPos pos = this.worldPosition;
        if (this.running || !this.hasFuel()) {
            return false;
        }
        LivingEntity on = this.creatureOnTop(level, pos);
        if (on != null) {
            this.mode = MODE_ENTITY;
            this.targetUuid = on.getUUID();
            this.targetId = on.getId();
            this.targetKind = Irradiation.kind(on);
            if (this.targetKind == Irradiation.KIND_CAVE_SPIDER) {
                // like a zombie villager starting to cure
                level.playSound(null, on.getX(), on.getEyeY(), on.getZ(), SoundEvents.ZOMBIE_VILLAGER_CURE, on.getSoundSource(),
                        1.0F + level.getRandom().nextFloat(), level.getRandom().nextFloat() * 0.7F + 0.3F);
            }
        } else if (this.canIrradiateItem()) {
            this.mode = MODE_ITEM;
            this.targetUuid = null;
            this.targetId = -1;
            this.targetKind = Irradiation.KIND_NONE;
        } else {
            return false;
        }
        this.running = true;
        this.progress = 0;
        level.playSound(null, pos, SoundEvents.BEACON_ACTIVATE, SoundSource.BLOCKS, 0.8F, 1.4F);
        this.setActive(level, pos, true);
        if (starter != null) {
            PUAdvancements.award(starter, PUAdvancements.EXPERIMENT);
        } else {
            for (Player p : level.players()) {
                if (p instanceof ServerPlayer sp && p.distanceToSqr(pos.getCenter()) < 16 * 16) {
                    PUAdvancements.award(sp, PUAdvancements.EXPERIMENT);
                }
            }
        }
        this.changed(level, pos);
        return true;
    }

    private void finish(ServerLevel level, BlockPos pos, @Nullable LivingEntity target) {
        this.useFuel(level, pos);
        float roll = level.getRandom().nextFloat();
        if (this.mode == MODE_ENTITY && target != null) {
            int kind = Irradiation.kind(target);
            if (kind == Irradiation.KIND_CAVE_SPIDER && target instanceof CaveSpider spider) {
                if (roll < Irradiation.SPIDER_CHANCE) {
                    spider.convertTo(PUEntities.RADIOACTIVE_SPIDER.get(), ConversionParams.single(spider, false, false), radioactive -> {
                    });
                    level.levelEvent(null, 1027, spider.blockPosition(), 0);  // the zombie villager "converted" sound
                    level.sendParticles(RadioactiveSpider.SPARK, spider.getX(), spider.getY() + 0.3, spider.getZ(), 40, 0.4, 0.3, 0.4, 0.05);
                } else {
                    kill(level, spider);
                }
            } else if (roll < (kind == Irradiation.KIND_PLAYER ? Irradiation.PLAYER_SURVIVE : Irradiation.MOB_SURVIVE)) {
                target.addEffect(new MobEffectInstance(MobEffects.POISON, Irradiation.SURVIVOR_POISON_TICKS, 0));
            } else {
                kill(level, target);
            }
        } else if (this.mode == MODE_ITEM) {
            IrradiationRecipes.Recipe recipe = IrradiationRecipes.find(this.items.get(SLOT_INPUT));
            if (recipe != null) {
                this.items.get(SLOT_INPUT).shrink(1);
                if (roll < recipe.chance()) {
                    this.addToOutput(level, pos, new ItemStack(recipe.result().get()));
                    level.playSound(null, pos, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, 1.0F, 1.6F);
                } else {
                    level.sendParticles(ParticleTypes.LARGE_SMOKE, pos.getX() + 0.5, pos.getY() + 1.1, pos.getZ() + 0.5, 8, 0.2, 0.1, 0.2, 0.01);
                    level.playSound(null, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.8F, 1.2F);
                }
            }
        }
        this.stop(level, pos);
    }

    private static void kill(ServerLevel level, LivingEntity entity) {
        level.sendParticles(ParticleTypes.LARGE_SMOKE, entity.getX(), entity.getY() + entity.getBbHeight() * 0.5, entity.getZ(), 10, 0.25, 0.25, 0.25, 0.02);
        entity.hurtServer(level, PUDamageTypes.radiation(level), Float.MAX_VALUE);
    }

    private void stop(ServerLevel level, BlockPos pos) {
        this.running = false;
        this.progress = 0;
        this.mode = MODE_NONE;
        this.targetUuid = null;
        this.setActive(level, pos, false);
        this.changed(level, pos);
    }

    /** The capsule turns into an empty cartridge (owner): in its slot when it was the last one, else the output. */
    private void useFuel(ServerLevel level, BlockPos pos) {
        ItemStack fuel = this.items.get(SLOT_FUEL);
        fuel.shrink(1);
        ItemStack cartridge = new ItemStack(PUItems.EMPTY_WEB_CARTRIDGE.get());
        if (fuel.isEmpty()) {
            this.items.set(SLOT_FUEL, cartridge);
        } else {
            this.addToOutput(level, pos, cartridge);
        }
    }

    private void addToOutput(ServerLevel level, BlockPos pos, ItemStack stack) {
        ItemStack out = this.items.get(SLOT_OUTPUT);
        if (out.isEmpty()) {
            this.items.set(SLOT_OUTPUT, stack);
        } else if (ItemStack.isSameItemSameComponents(out, stack) && out.getCount() + stack.getCount() <= out.getMaxStackSize()) {
            out.grow(stack.getCount());
        } else {
            Containers.dropItemStack(level, pos.getX() + 0.5, pos.getY() + 1.1, pos.getZ() + 0.5, stack);
        }
    }

    // =====================================================================================================
    // helpers
    // =====================================================================================================

    public boolean hasFuel() {
        return this.items.get(SLOT_FUEL).is(PUItems.ISOTOPE_CAPSULE.get());
    }

    private boolean canIrradiateItem() {
        IrradiationRecipes.Recipe recipe = IrradiationRecipes.find(this.items.get(SLOT_INPUT));
        if (recipe == null) {
            return false;
        }
        ItemStack out = this.items.get(SLOT_OUTPUT);
        return out.isEmpty() || (out.is(recipe.result().get()) && out.getCount() < out.getMaxStackSize());
    }

    private AABB top(BlockPos pos) {
        return new AABB(pos.getX() + 0.05, pos.getY() + 0.9, pos.getZ() + 0.05, pos.getX() + 0.95, pos.getY() + 1.4, pos.getZ() + 0.95);
    }

    private boolean isOnTop(LivingEntity entity, BlockPos pos) {
        return entity.getBoundingBox().intersects(this.top(pos)) && entity.getY() >= pos.getY() + 0.85 && entity.getY() < pos.getY() + 1.4;
    }

    /** A mob or player standing on the block (the one nearest the middle). */
    private @Nullable LivingEntity creatureOnTop(ServerLevel level, BlockPos pos) {
        List<LivingEntity> list = level.getEntitiesOfClass(LivingEntity.class, this.top(pos),
                e -> e.isAlive() && !e.isSpectator() && (e instanceof Mob || e instanceof Player) && this.isOnTop(e, pos));
        return list.stream().min(Comparator.comparingDouble(e -> e.distanceToSqr(pos.getX() + 0.5, e.getY(), pos.getZ() + 0.5))).orElse(null);
    }

    private void setActive(ServerLevel level, BlockPos pos, boolean active) {
        BlockState state = level.getBlockState(pos);
        if (state.hasProperty(IrradiatorBlock.ACTIVE) && state.getValue(IrradiatorBlock.ACTIVE) != active) {
            level.setBlock(pos, state.setValue(IrradiatorBlock.ACTIVE, active), 3);
        }
    }

    private void changed(ServerLevel level, BlockPos pos) {
        this.setChanged();
        BlockState state = level.getBlockState(pos);
        level.sendBlockUpdated(pos, state, state, 3);
    }

    /** Loader / block hook: neighbour changed — a rising redstone edge starts an irradiation. */
    public void redstone(ServerLevel level, boolean powered) {
        if (powered && !this.wasPowered) {
            this.start(level, null);
        }
        this.wasPowered = powered;
    }

    public ContainerData data() {
        return this.data;
    }

    // =====================================================================================================
    // sync / save
    // =====================================================================================================

    @Override
    public @Nullable Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        tag.putInt("Shake", this.running && this.mode == MODE_ENTITY ? this.targetId : -1);
        return tag;
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.shakeId = input.getIntOr("Shake", -1);
        if (this.level == null || !this.level.isClientSide()) {
            this.items.clear();
            ContainerHelper.loadAllItems(input, this.items);
            this.wasPowered = input.getBooleanOr("Powered", false);
        }
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        ContainerHelper.saveAllItems(output, this.items);
        output.putBoolean("Powered", this.wasPowered);
    }

    // =====================================================================================================
    // Container / MenuProvider
    // =====================================================================================================

    @Override
    public int getContainerSize() {
        return SLOTS;
    }

    @Override
    public boolean isEmpty() {
        return this.items.stream().allMatch(ItemStack::isEmpty);
    }

    @Override
    public ItemStack getItem(int slot) {
        return this.items.get(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        ItemStack result = ContainerHelper.removeItem(this.items, slot, amount);
        if (!result.isEmpty()) {
            this.setChanged();
        }
        return result;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return ContainerHelper.takeItem(this.items, slot);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        this.items.set(slot, stack);
        stack.limitSize(this.getMaxStackSize(stack));
        this.setChanged();
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return switch (slot) {
            case SLOT_FUEL -> stack.is(PUItems.ISOTOPE_CAPSULE.get());
            case SLOT_INPUT -> IrradiationRecipes.find(stack) != null;
            default -> false;
        };
    }

    @Override
    public boolean stillValid(Player player) {
        return Container.stillValidBlockEntity(this, player);
    }

    @Override
    public void clearContent() {
        this.items.clear();
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("container.powers_unchained.irradiator");
    }

    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new IrradiatorMenu(containerId, inventory, this, this.data, ContainerLevelAccess.create(this.level, this.worldPosition));
    }
}
