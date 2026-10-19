package net.threetag.palladium.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.threetag.palladium.component.PalladiumDataComponents;
import net.threetag.palladium.entity.Bottleable;
import org.jspecify.annotations.Nullable;

import java.util.function.Supplier;

public class MobBottleItem extends Item {

    private final Supplier<EntityType<? extends Mob>> type;
    private final SoundEvent emptySound;

    public MobBottleItem(EntityType<? extends Mob> type, SoundEvent emptySound, Item.Properties properties) {
        super(properties);
        this.type = () -> type;
        this.emptySound = emptySound;
    }

    public MobBottleItem(Supplier<EntityType<? extends Mob>> type, SoundEvent emptySound, Properties properties) {
        super(properties);
        this.type = type;
        this.emptySound = emptySound;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);
        BlockHitResult hitResult = getPlayerPOVHitResult(level, player, ClipContext.Fluid.ANY);
        if (hitResult.getType() == HitResult.Type.MISS) {
            return InteractionResult.PASS;
        } else if (hitResult.getType() != HitResult.Type.BLOCK) {
            return InteractionResult.PASS;
        } else {
            BlockPos pos = hitResult.getBlockPos();
            Direction direction = hitResult.getDirection();
            BlockPos directionOffsetPos = pos.relative(direction);
            if (level.mayInteract(player, pos) && player.mayUseItemAt(directionOffsetPos, direction, itemStack)) {
                this.playEmptySound(player, level, directionOffsetPos);
                if (level instanceof ServerLevel serverLevel) {
                    this.spawn(serverLevel, itemStack, directionOffsetPos);
                }

                player.awardStat(Stats.ITEM_USED.get(this));
                return InteractionResult.SUCCESS.heldItemTransformedTo(Items.GLASS_BOTTLE.getDefaultInstance());
            } else {
                return InteractionResult.FAIL;
            }
        }
    }

    protected void playEmptySound(@Nullable LivingEntity user, LevelAccessor level, BlockPos pos) {
        level.playSound(user, pos, this.emptySound, SoundSource.NEUTRAL, 1.0F, 1.0F);
    }

    private void spawn(ServerLevel level, ItemStack itemStack, BlockPos spawnPos) {
        Mob mob = this.type.get().create(level, EntityType.createDefaultStackConfig(level, itemStack, null), spawnPos, EntitySpawnReason.BUCKET, true, false);
        if (mob instanceof Bottleable bottleable) {
            CustomData entityData = itemStack.getOrDefault(PalladiumDataComponents.Items.BOTTLE_ENTITY_DATA, CustomData.EMPTY);
            bottleable.loadFromBottleTag(entityData.copyTag());
            bottleable.setFromBottle(true);
        }

        if (mob != null) {
            level.addFreshEntityWithPassengers(mob);
            mob.playAmbientSound();
        }

    }

}
