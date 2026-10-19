package net.threetag.palladium.power.ability;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.network.PacketDistributor;
import net.threetag.palladium.Palladium;
import net.threetag.palladium.component.PalladiumDataComponents;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.icon.Icon;
import net.threetag.palladium.logic.condition.Condition;
import net.threetag.palladium.logic.condition.TrueCondition;
import net.threetag.palladium.logic.context.DataContext;
import net.threetag.palladium.network.DisplayModeSwitchPacket;
import net.threetag.palladium.power.energybar.EnergyBarUsage;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ModeSwitchAbility extends Ability {

    public static final MapCodec<ModeSwitchAbility> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    Codec.unboundedMap(Codec.STRING, Mode.CODEC).validate(map -> map.isEmpty()
                            ? com.mojang.serialization.DataResult.error(() -> "Mode switch ability must define at least one mode")
                            : com.mojang.serialization.DataResult.success(map)).fieldOf("modes").forGetter(a -> a.modes),
                    propertiesCodec(), stateCodec(), energyBarUsagesCodec()
            ).apply(instance, ModeSwitchAbility::new));

    private final Map<String, Mode> modes;
    private final List<String> keys;

    public ModeSwitchAbility(Map<String, Mode> modes, AbilityProperties properties, AbilityStateManager stateManager, List<EnergyBarUsage> energyBarUsages) {
        super(properties, stateManager, energyBarUsages);
        this.modes = new LinkedHashMap<>(modes);
        this.keys = List.copyOf(this.modes.keySet());
    }

    @Override
    public void registerDataComponents(DataComponentMap.Builder components) {
        super.registerDataComponents(components);
        components.set(PalladiumDataComponents.Abilities.MODE_SWITCH_ACTIVE_KEY.get(), this.keys.getFirst());
    }

    @Override
    public void firstTick(LivingEntity entity, AbilityInstance<?> abilityInstance) {
        String next = this.nextAvailableKey(getActiveKey(abilityInstance), DataContext.forAbility(entity, abilityInstance));

        if (!entity.level().isClientSide()) {
            abilityInstance.set(PalladiumDataComponents.Abilities.MODE_SWITCH_ACTIVE_KEY.get(), next);
        }

        Palladium.PROXY.displayModeSwitch(entity, this.modes.get(next));
    }

    private String nextAvailableKey(String currentKey, DataContext context) {
        int current = Math.max(0, this.keys.indexOf(currentKey));
        int count = this.keys.size();

        for (int step = 1; step <= count; step++) {
            String key = this.keys.get((current + step) % count);

            if (this.modes.get(key).conditions().test(context)) {
                return key;
            }
        }

        return currentKey;
    }

    public static String getActiveKey(AbilityInstance<?> instance) {
        if (!(instance.getAbility() instanceof ModeSwitchAbility ability)) {
            return "";
        }

        String key = instance.getOrDefault(PalladiumDataComponents.Abilities.MODE_SWITCH_ACTIVE_KEY.get(), "");
        return ability.modes.containsKey(key) ? key : ability.keys.getFirst();
    }

    public Mode getActiveMode(AbilityInstance<?> instance) {
        return this.modes.get(getActiveKey(instance));
    }

    @Nullable
    public Mode getMode(String key) {
        return this.modes.get(key);
    }

    public Map<String, Mode> getModes() {
        return this.modes;
    }

    public static boolean setMode(LivingEntity entity, AbilityInstance<?> instance, String key) {
        if (!(instance.getAbility() instanceof ModeSwitchAbility ability)) {
            return false;
        }

        Mode mode = ability.modes.get(key);

        if (mode == null) {
            return false;
        }

        if (!entity.level().isClientSide()) {
            instance.set(PalladiumDataComponents.Abilities.MODE_SWITCH_ACTIVE_KEY.get(), key);

            if (entity instanceof ServerPlayer serverPlayer) {
                PacketDistributor.sendToPlayer(serverPlayer, new DisplayModeSwitchPacket(instance.getReference(), key));
            }
        }

        return true;
    }

    @Override
    public AbilitySerializer<?> getSerializer() {
        return AbilitySerializers.MODE_SWITCH.get();
    }

    public record Mode(Component title, Optional<Icon> icon, Condition conditions) {

        public static final Codec<Mode> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                ComponentSerialization.CODEC.fieldOf("title").forGetter(Mode::title),
                Icon.CODEC.optionalFieldOf("icon").forGetter(Mode::icon),
                Condition.CODEC.optionalFieldOf("conditions", TrueCondition.INSTANCE).forGetter(Mode::conditions)
        ).apply(instance, Mode::new));
    }

    public static class Serializer extends AbilitySerializer<ModeSwitchAbility> {

        @Override
        public MapCodec<ModeSwitchAbility> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<Ability, ModeSwitchAbility> builder, HolderLookup.Provider provider) {
            builder.setDescription("Cycles through a map of modes each time it is activated, switching to the next one whose conditions are met and briefly showing it on screen.")
                    .add("modes", TYPE_ANY, "A map of mode key -> mode (at least one). Each mode has a required \"title\", an optional \"icon\" texture, and optional \"conditions\" (a mode is only switchable-to while its conditions are met). The key is the unique id used by the mode condition and value.")
                    .addExampleObject(new ModeSwitchAbility(
                            new LinkedHashMap<>(Map.of(
                                    "mode_a", new Mode(Component.literal("Mode A"), Optional.empty(), TrueCondition.INSTANCE)
                            )),
                            AbilityProperties.BASIC, AbilityStateManager.EMPTY, List.of()));
        }
    }
}
