package net.threetag.palladium.logic.trigger;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.criterion.ContextAwarePredicate;
import net.minecraft.advancements.criterion.EntityPredicate;
import net.minecraft.advancements.criterion.NbtPredicate;
import net.minecraft.advancements.criterion.SimpleCriterionTrigger;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.threetag.palladium.logic.automation.AutomationContextEnricher;
import net.threetag.palladium.logic.context.DataContext;
import net.threetag.palladium.logic.context.DataContextKeys;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class CustomEventTrigger extends SimpleCriterionTrigger<CustomEventTrigger.TriggerInstance> implements AutomationContextEnricher {

    @Nullable
    private CurrentEvent currentEvent;

    @Override
    public Codec<CustomEventTrigger.TriggerInstance> codec() {
        return CustomEventTrigger.TriggerInstance.CODEC;
    }

    public void trigger(ServerPlayer player, Identifier eventId, CompoundTag data) {
        this.currentEvent = new CurrentEvent(eventId, data);

        try {
            this.trigger(player, (triggerInstance) -> triggerInstance.matches(eventId, data));
        } finally {
            this.currentEvent = null;
        }
    }

    @Override
    public void enrich(DataContext context) {
        if (this.currentEvent != null) {
            context.with(DataContextKeys.EVENT_ID, this.currentEvent.id())
                    .with(DataContextKeys.EVENT_DATA, this.currentEvent.data());
        }
    }

    private record CurrentEvent(Identifier id, CompoundTag data) {
    }

    public record TriggerInstance(Optional<ContextAwarePredicate> player,
                                  Optional<IdentifierPredicate> id,
                                  Optional<NbtPredicate> data) implements SimpleCriterionTrigger.SimpleInstance {

        public static final Codec<CustomEventTrigger.TriggerInstance> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(CustomEventTrigger.TriggerInstance::player),
                IdentifierPredicate.CODEC.optionalFieldOf("id").forGetter(CustomEventTrigger.TriggerInstance::id),
                NbtPredicate.CODEC.optionalFieldOf("data").forGetter(CustomEventTrigger.TriggerInstance::data)
        ).apply(instance, CustomEventTrigger.TriggerInstance::new));

        public boolean matches(Identifier eventId, CompoundTag data) {
            if (this.id.isPresent() && !this.id.get().test(eventId)) {
                return false;
            }

            return this.data.isEmpty() || this.data.get().matches(data);
        }
    }
}
