package net.threetag.palladium.power.ability.unlocking;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.PacketDistributor;
import net.threetag.palladium.Palladium;
import net.threetag.palladium.component.PalladiumDataComponents;
import net.threetag.palladium.logic.condition.AbilityUnlockedCondition;
import net.threetag.palladium.logic.condition.AndCondition;
import net.threetag.palladium.logic.condition.Condition;
import net.threetag.palladium.logic.condition.TrueCondition;
import net.threetag.palladium.logic.context.DataContext;
import net.threetag.palladium.logic.cost.Cost;
import net.threetag.palladium.logic.cost.CostDisplay;
import net.threetag.palladium.network.AbilityClickedPacket;
import net.threetag.palladium.network.OpenAbilityBuyScreenPacket;
import net.threetag.palladium.power.ability.AbilityInstance;
import net.threetag.palladium.power.ability.AbilityReference;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class BuyableUnlockingHandler extends UnlockingHandler {

    public static final MapCodec<BuyableUnlockingHandler> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Cost.CODEC.fieldOf("cost").forGetter(h -> h.cost),
            Condition.CODEC.optionalFieldOf("requires", TrueCondition.INSTANCE).forGetter(h -> h.condition)
    ).apply(instance, BuyableUnlockingHandler::new));

    public final Cost cost;
    public final Condition condition;
    private final List<AbilityReference> parentAbilities;

    public BuyableUnlockingHandler(Cost cost, Condition condition) {
        this.cost = cost;
        this.condition = condition;
        this.parentAbilities = new ArrayList<>();

        if (condition instanceof AndCondition(List<Condition> conditions)) {
            for (Condition c : conditions) {
                if (c instanceof AbilityUnlockedCondition(AbilityReference ability)) {
                    this.parentAbilities.add(ability);
                }
            }
        } else if (condition instanceof AbilityUnlockedCondition(AbilityReference ability)) {
            this.parentAbilities.add(ability);
        }
    }

    @Override
    public boolean requiresContext() {
        return !(this.condition instanceof TrueCondition);
    }

    @Override
    public boolean check(LivingEntity entity, AbilityInstance<?> abilityInstance, @Nullable DataContext context) {
        return abilityInstance.getOrDefault(PalladiumDataComponents.Abilities.BOUGHT.get(), false) && this.condition.test(context);
    }

    @Override
    public void registerDataComponents(DataComponentMap.Builder builder) {
        builder.set(PalladiumDataComponents.Abilities.BOUGHT.get(), false);
    }

    public boolean hasEnoughCurrency(LivingEntity entity) {
        return this.cost.hasEnoughCurrency(entity);
    }

    public void consumeCurrency(LivingEntity entity) {
        this.cost.consumeCurrency(entity);
    }

    public void unlock(LivingEntity entity, AbilityInstance<?> abilityInstance) {
        abilityInstance.set(PalladiumDataComponents.Abilities.BOUGHT.get(), true);
    }

    public CostDisplay getDisplay() {
        return this.cost.createDisplay();
    }

    @Override
    public boolean onClicked(Player player, AbilityInstance<?> abilityInstance) {
        if (abilityInstance.getOrDefault(PalladiumDataComponents.Abilities.BOUGHT.get(), false)) {
            return false;
        }

        if (player.level().isClientSide()) {
            Palladium.PROXY.sendPacketToServer(new AbilityClickedPacket(abilityInstance.getReference()));
        } else if (player instanceof ServerPlayer serverPlayer) {
            PacketDistributor.sendToPlayer(serverPlayer, new OpenAbilityBuyScreenPacket(abilityInstance.getReference(), this.hasEnoughCurrency(player)));
        }

        return true;
    }

    @Override
    public List<AbilityReference> getParentAbilities() {
        return this.parentAbilities;
    }

    @Override
    public MapCodec<? extends UnlockingHandler> codec() {
        return CODEC;
    }
}
