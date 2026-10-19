package net.threetag.palladium.item;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.Position;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TridentItem;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.threetag.palladium.entity.PalladiumThrownTrident;
import org.jspecify.annotations.NonNull;

public class PalladiumTridentItem extends TridentItem {

    public static final MapCodec<PalladiumTridentItem> TRIDENT_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ItemPropertiesCodec.CODEC.fieldOf("properties").forGetter(item -> ((PalladiumItemExtension) item).palladium$getProperties())
    ).apply(instance, PalladiumTridentItem::new));

    public PalladiumTridentItem(Item.Properties properties) {
        super(properties);
    }

    @Override
    public @NonNull Projectile asProjectile(@NonNull Level level, Position pos, ItemStack stack, @NonNull Direction direction) {
        return new PalladiumThrownTrident(level, pos.x(), pos.y(), pos.z(), stack.copyWithCount(1));
    }

    @Override
    public boolean releaseUsing(@NonNull ItemStack itemStack, @NonNull Level level, @NonNull LivingEntity entity, int remainingTime) {
        if (entity instanceof Player player) {
            int timeHeld = this.getUseDuration(itemStack, entity) - remainingTime;
            if (timeHeld < 10) {
                return false;
            } else {
                float riptideStrength = EnchantmentHelper.getTridentSpinAttackStrength(itemStack, player);
                if (!(riptideStrength > 0.0F) || player.isInWaterOrRain() && !player.isPassenger()) {
                    if (itemStack.nextDamageWillBreak()) {
                        return false;
                    } else {
                        Holder<SoundEvent> sound = EnchantmentHelper.pickHighestLevel(itemStack, EnchantmentEffectComponents.TRIDENT_SOUND)
                                .orElse(SoundEvents.TRIDENT_THROW);
                        player.awardStat(Stats.ITEM_USED.get(this));
                        if (level instanceof ServerLevel serverLevel) {
                            itemStack.hurtWithoutBreaking(1, player);
                            if (riptideStrength == 0.0F) {
                                ItemStack thrownItemStack = itemStack.consumeAndReturn(1, player);
                                PalladiumThrownTrident trident = Projectile.spawnProjectileFromRotation(
                                        PalladiumThrownTrident::new, serverLevel, thrownItemStack, player, 0.0F, 2.5F, 1.0F
                                );
                                if (player.hasInfiniteMaterials()) {
                                    trident.pickup = AbstractArrow.Pickup.CREATIVE_ONLY;
                                }

                                level.playSound(null, trident, sound.value(), SoundSource.PLAYERS, 1.0F, 1.0F);
                                return true;
                            }
                        }

                        if (riptideStrength > 0.0F) {
                            float yRot = player.getYRot();
                            float xRot = player.getXRot();
                            float xd = -Mth.sin(yRot * (float) (Math.PI / 180.0)) * Mth.cos(xRot * (float) (Math.PI / 180.0));
                            float yd = -Mth.sin(xRot * (float) (Math.PI / 180.0));
                            float zd = Mth.cos(yRot * (float) (Math.PI / 180.0)) * Mth.cos(xRot * (float) (Math.PI / 180.0));
                            float dist = Mth.sqrt(xd * xd + yd * yd + zd * zd);
                            xd *= riptideStrength / dist;
                            yd *= riptideStrength / dist;
                            zd *= riptideStrength / dist;
                            player.push(xd, yd, zd);
                            player.startAutoSpinAttack(20, 8.0F, itemStack);
                            if (player.onGround()) {
                                float heightDifference = 1.1999999F;
                                player.move(MoverType.SELF, new Vec3(0.0, heightDifference, 0.0));
                            }

                            level.playSound(null, player, sound.value(), SoundSource.PLAYERS, 1.0F, 1.0F);
                            return true;
                        } else {
                            return false;
                        }
                    }
                } else {
                    return false;
                }
            }
        } else {
            return false;
        }
    }
}
