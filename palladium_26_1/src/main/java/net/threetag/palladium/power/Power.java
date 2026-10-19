package net.threetag.palladium.power;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryFixedCodec;
import net.threetag.palladium.icon.Icon;
import net.threetag.palladium.logic.value.holder.TextComponentValueHolder;
import net.threetag.palladium.power.ability.Ability;
import net.threetag.palladium.power.energybar.EnergyBarConfiguration;
import net.threetag.palladium.registry.PalladiumRegistryKeys;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

public class Power {

    public static final Codec<Power> CODEC = RecordCodecBuilder.create((instance) -> instance
            .group(
                    Identifier.CODEC.optionalFieldOf("parent").forGetter(p -> Optional.ofNullable(p.parentId)),
                    TextComponentValueHolder.CODEC.fieldOf("name").forGetter(Power::getName),
                    Icon.CODEC.fieldOf("icon").forGetter(Power::getIcon),
                    Codec.BOOL.optionalFieldOf("persistent_data", false).forGetter(Power::hasPersistentData),
                    Codec.BOOL.optionalFieldOf("hidden", false).forGetter(Power::isHidden),
                    Codec.unboundedMap(Codec.STRING, Ability.CODEC).optionalFieldOf("abilities", Collections.emptyMap()).forGetter(Power::getAbilities),
                    Codec.unboundedMap(Codec.STRING, EnergyBarConfiguration.CODEC).optionalFieldOf("energy_bars", Collections.emptyMap()).forGetter(Power::getEnergyBars)
            )
            .apply(instance, (parent, name, icon, persistentData, hidden, abilities, energyBars) ->
                    new Power(parent.orElse(null), name, icon, persistentData, hidden, abilities, energyBars)));

    public static final Codec<Holder<Power>> HOLDER_CODEC = RegistryFixedCodec.create(PalladiumRegistryKeys.POWER);

    @Nullable
    private final Identifier parentId;
    private final TextComponentValueHolder name;
    private final Icon icon;
    private final Map<String, Ability> abilities;
    private final Map<String, EnergyBarConfiguration> energyBars;
    private final boolean persistentData;
    private final boolean hidden;

    @SuppressWarnings("deprecation")
    public Power(@Nullable Identifier parentId, TextComponentValueHolder name, Icon icon, boolean persistentData, boolean hidden, Map<String, Ability> abilities, Map<String, EnergyBarConfiguration> energyBars) {
        this.parentId = parentId;
        this.name = name;
        this.icon = icon;
        this.persistentData = persistentData;
        this.hidden = hidden;
        this.abilities = abilities;
        this.energyBars = energyBars;

        for (Map.Entry<String, Ability> e : this.abilities.entrySet()) {
            e.getValue().setKey(e.getKey());
        }

        for (Map.Entry<String, EnergyBarConfiguration> e : this.energyBars.entrySet()) {
            e.getValue().setKey(e.getKey());
        }
    }

    public @Nullable Identifier getParentId() {
        return parentId;
    }

    public TextComponentValueHolder getName() {
        return this.name;
    }

    public Icon getIcon() {
        return this.icon;
    }

    public Map<String, Ability> getAbilities() {
        return this.abilities;
    }

    public Map<String, EnergyBarConfiguration> getEnergyBars() {
        return this.energyBars;
    }

    public boolean hasPersistentData() {
        return this.persistentData;
    }

    public boolean isHidden() {
        return this.hidden;
    }

}
