package com.thekrepe.powersunchained.loot;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.thekrepe.powersunchained.item.SuitFragmentItem;
import com.thekrepe.powersunchained.suit.Suit;
import com.thekrepe.powersunchained.suit.SuitUnlocks;
import com.thekrepe.powersunchained.suit.Suits;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.loot.LootModifier;

/**
 * Adds a suit fragment to chest loot with {@code chance}. The suit is one the opening player hasn't unlocked yet
 * (any suit if there's no player or they have them all).
 */
public class SuitFragmentLootModifier extends LootModifier {

    public static final MapCodec<SuitFragmentLootModifier> CODEC = RecordCodecBuilder.mapCodec(i -> codecStart(i)
            .and(Codec.FLOAT.fieldOf("chance").forGetter(m -> m.chance))
            .apply(i, SuitFragmentLootModifier::new));

    private final float chance;

    public SuitFragmentLootModifier(Optional<Holder<LootItemCondition>> condition, int priority, float chance) {
        super(condition, priority);
        this.chance = chance;
    }

    @Override
    protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> loot, LootContext context) {
        if (context.getRandom().nextFloat() >= this.chance) {
            return loot;
        }
        List<Suit> pool = Suits.ALL;
        if (context.getOptional(LootContextParams.THIS_ENTITY) instanceof Player player) {
            List<Suit> locked = Suits.ALL.stream().filter(s -> !SuitUnlocks.isUnlocked(player, s)).toList();
            if (!locked.isEmpty()) {
                pool = locked;
            }
        }
        loot.add(SuitFragmentItem.of(pool.get(context.getRandom().nextInt(pool.size()))));
        return loot;
    }

    @Override
    public MapCodec<? extends net.neoforged.neoforge.common.loot.IGlobalLootModifier> codec() {
        return CODEC;
    }
}
