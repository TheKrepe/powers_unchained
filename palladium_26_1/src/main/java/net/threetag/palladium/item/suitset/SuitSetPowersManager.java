package net.threetag.palladium.item.suitset;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.neoforged.neoforge.registries.holdersets.OrHolderSet;
import net.threetag.palladium.Palladium;
import net.threetag.palladium.power.Power;
import net.threetag.palladium.registry.PalladiumRegistryKeys;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SuitSetPowersManager extends SimpleJsonResourceReloadListener<SuitSetPowersManager.SuitSetPower> {

    public static SuitSetPowersManager INSTANCE;
    private final Map<SuitSet, HolderSet<Power>> suitSetPowers = new HashMap<>();

    public SuitSetPowersManager() {
        super(SuitSetPower.CODEC, FileToIdConverter.json("palladium/suit_set_power"));
    }

    @Override
    protected void apply(Map<Identifier, SuitSetPower> objects, ResourceManager resourceManager, ProfilerFiller profilerFiller) {
        this.suitSetPowers.clear();

        Map<SuitSet, List<HolderSet<Power>>> powers = new HashMap<>();
        for (SuitSetPower suitSetPower : objects.values()) {
            powers.computeIfAbsent(suitSetPower.suitSet, _ -> new ArrayList<>()).add(suitSetPower.powers);
        }

        powers.forEach((suitSet, holderSets) -> {
            if (holderSets.size() == 1) {
                this.suitSetPowers.put(suitSet, holderSets.getFirst());
            } else {
                this.suitSetPowers.put(suitSet, new OrHolderSet<>(holderSets));
            }
        });
    }

    public HolderSet<Power> getPowers(SuitSet suitSet) {
        return this.suitSetPowers.get(suitSet);
    }

    public record SuitSetPower(SuitSet suitSet, HolderSet<Power> powers) {

        public static final Codec<SuitSetPower> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                SuitSets.BY_NAME_CODEC.fieldOf("suit_set").forGetter(SuitSetPower::suitSet),
                RegistryCodecs.homogeneousList(PalladiumRegistryKeys.POWER).fieldOf("power").forGetter(SuitSetPower::powers)
        ).apply(instance, SuitSetPower::new));

    }

}
