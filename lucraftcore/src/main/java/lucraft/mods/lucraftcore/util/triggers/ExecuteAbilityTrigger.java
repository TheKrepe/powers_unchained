package lucraft.mods.lucraftcore.util.triggers;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import lucraft.mods.lucraftcore.LucraftCore;
import lucraft.mods.lucraftcore.superpowers.abilities.Ability;
import lucraft.mods.lucraftcore.superpowers.abilities.AbilityEntry;
import net.minecraft.advancements.ICriterionTrigger;
import net.minecraft.advancements.PlayerAdvancements;
import net.minecraft.advancements.critereon.AbstractCriterionInstance;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ExecuteAbilityTrigger implements ICriterionTrigger<ExecuteAbilityTrigger.Instance> {

    private static final ResourceLocation ID = new ResourceLocation(LucraftCore.MODID, "execute_ability");
    private final Map<PlayerAdvancements, ExecuteAbilityTrigger.Listeners> listeners = Maps.<PlayerAdvancements, ExecuteAbilityTrigger.Listeners>newHashMap();

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @Override
    public void addListener(PlayerAdvancements playerAdvancementsIn, net.minecraft.advancements.ICriterionTrigger.Listener<Instance> listener) {
        ExecuteAbilityTrigger.Listeners recipeunlockedtrigger$listeners = this.listeners.get(playerAdvancementsIn);

        if (recipeunlockedtrigger$listeners == null) {
            recipeunlockedtrigger$listeners = new ExecuteAbilityTrigger.Listeners(playerAdvancementsIn);
            this.listeners.put(playerAdvancementsIn, recipeunlockedtrigger$listeners);
        }

        recipeunlockedtrigger$listeners.add(listener);
    }

    @Override
    public void removeListener(PlayerAdvancements playerAdvancementsIn, net.minecraft.advancements.ICriterionTrigger.Listener<Instance> listener) {
        ExecuteAbilityTrigger.Listeners recipeunlockedtrigger$listeners = this.listeners.get(playerAdvancementsIn);

        if (recipeunlockedtrigger$listeners != null) {
            recipeunlockedtrigger$listeners.remove(listener);

            if (recipeunlockedtrigger$listeners.isEmpty()) {
                this.listeners.remove(playerAdvancementsIn);
            }
        }
    }

    @Override
    public void removeAllListeners(PlayerAdvancements playerAdvancementsIn) {
        this.listeners.remove(playerAdvancementsIn);
    }

    public void trigger(EntityPlayerMP player, AbilityEntry ability) {
        ExecuteAbilityTrigger.Listeners recipeunlockedtrigger$listeners = this.listeners.get(player.getAdvancements());

        if (recipeunlockedtrigger$listeners != null) {
            recipeunlockedtrigger$listeners.trigger(ability);
        }
    }

    @Override
    public Instance deserializeInstance(JsonObject json, JsonDeserializationContext context) {
        AbilityEntry ability = json.has("ability") ? Ability.ABILITY_REGISTRY.getValue(new ResourceLocation(JsonUtils.getString(json, "ability"))) : null;
        return new ExecuteAbilityTrigger.Instance(ability);
    }

    public static class Instance extends AbstractCriterionInstance {

        @Nullable
        private final AbilityEntry ability;

        public Instance(@Nullable AbilityEntry ability) {
            super(ExecuteAbilityTrigger.ID);
            this.ability = ability;
        }

        public boolean test(AbilityEntry ability) {
            return this.ability == ability;
        }

    }

    static class Listeners {

        private final PlayerAdvancements playerAdvancements;
        private final Set<ICriterionTrigger.Listener<ExecuteAbilityTrigger.Instance>> listeners = Sets.<ExecuteAbilityTrigger.Listener<ExecuteAbilityTrigger.Instance>>newHashSet();

        public Listeners(PlayerAdvancements playerAdvancementsIn) {
            this.playerAdvancements = playerAdvancementsIn;
        }

        public boolean isEmpty() {
            return this.listeners.isEmpty();
        }

        public void add(ICriterionTrigger.Listener<ExecuteAbilityTrigger.Instance> listener) {
            this.listeners.add(listener);
        }

        public void remove(ICriterionTrigger.Listener<ExecuteAbilityTrigger.Instance> listener) {
            this.listeners.remove(listener);
        }

        public void trigger(AbilityEntry ability) {
            List<ICriterionTrigger.Listener<ExecuteAbilityTrigger.Instance>> list = null;

            for (ICriterionTrigger.Listener<ExecuteAbilityTrigger.Instance> listener : this.listeners) {
                if (((ExecuteAbilityTrigger.Instance) listener.getCriterionInstance()).test(ability)) {
                    if (list == null) {
                        list = Lists.<ICriterionTrigger.Listener<ExecuteAbilityTrigger.Instance>>newArrayList();
                    }

                    list.add(listener);
                }
            }

            if (list != null) {
                for (ICriterionTrigger.Listener<ExecuteAbilityTrigger.Instance> listener1 : list) {
                    listener1.grantCriterion(this.playerAdvancements);
                }
            }
        }
    }

}