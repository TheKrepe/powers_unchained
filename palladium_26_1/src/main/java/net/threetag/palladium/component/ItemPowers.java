package net.threetag.palladium.component;

import com.mojang.serialization.Codec;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.threetag.palladium.entity.PlayerSlot;
import net.threetag.palladium.power.Power;
import net.threetag.palladium.registry.PalladiumRegistryKeys;
import net.threetag.palladium.util.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public record ItemPowers(Map<PlayerSlot, HolderSet<Power>> powers) {

    public static final Codec<ItemPowers> CODEC = Codec.unboundedMap(
            PlayerSlot.CODEC,
            RegistryCodecs.homogeneousList(PalladiumRegistryKeys.POWER)
    ).xmap(ItemPowers::new, ItemPowers::powers);
    public static final StreamCodec<RegistryFriendlyByteBuf, ItemPowers> STREAM_CODEC = ByteBufCodecs.map(
            Utils::newMap,
            PlayerSlot.STREAM_CODEC,
            ByteBufCodecs.holderSet(PalladiumRegistryKeys.POWER)
    ).map(ItemPowers::new, ItemPowers::powers);

    public List<Holder<Power>> forSlot(PlayerSlot slot) {
        List<Holder<Power>> list = new ArrayList<>();

        if (this.powers.containsKey(PlayerSlot.AnySlot.INSTANCE)) {
            list.addAll(this.powers.get(PlayerSlot.AnySlot.INSTANCE).stream().toList());
        }

        if (this.powers.containsKey(slot)) {
            list.addAll(this.powers.get(slot).stream().toList());
        }

        return list;
    }

    public boolean contains(PlayerSlot slot, Holder<Power> holder) {
        if (this.powers.containsKey(PlayerSlot.AnySlot.INSTANCE) && this.powers.get(PlayerSlot.AnySlot.INSTANCE).contains(holder)) {
            return true;
        }

        return this.powers.containsKey(slot) && this.powers.get(slot).contains(holder);
    }
}
