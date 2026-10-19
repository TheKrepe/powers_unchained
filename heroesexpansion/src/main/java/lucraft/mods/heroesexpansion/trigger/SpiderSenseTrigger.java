package lucraft.mods.heroesexpansion.trigger;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import lucraft.mods.heroesexpansion.HeroesExpansion;
import net.minecraft.advancements.ICriterionTrigger;
import net.minecraft.advancements.PlayerAdvancements;
import net.minecraft.advancements.critereon.AbstractCriterionInstance;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ResourceLocation;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class SpiderSenseTrigger implements ICriterionTrigger<SpiderSenseTrigger.Instance> {

    private static final ResourceLocation ID = new ResourceLocation(HeroesExpansion.MODID, "spider_sense");
    private final Map<PlayerAdvancements, Listeners> listeners = Maps.<PlayerAdvancements, SpiderSenseTrigger.Listeners>newHashMap();

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @Override
    public void addListener(PlayerAdvancements playerAdvancementsIn, net.minecraft.advancements.ICriterionTrigger.Listener<Instance> listener) {
        SpiderSenseTrigger.Listeners recipeunlockedtrigger$listeners = this.listeners.get(playerAdvancementsIn);

        if (recipeunlockedtrigger$listeners == null) {
            recipeunlockedtrigger$listeners = new SpiderSenseTrigger.Listeners(playerAdvancementsIn);
            this.listeners.put(playerAdvancementsIn, recipeunlockedtrigger$listeners);
        }

        recipeunlockedtrigger$listeners.add(listener);
    }

    @Override
    public void removeListener(PlayerAdvancements playerAdvancementsIn, net.minecraft.advancements.ICriterionTrigger.Listener<Instance> listener) {
        SpiderSenseTrigger.Listeners recipeunlockedtrigger$listeners = this.listeners.get(playerAdvancementsIn);

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

    public void trigger(EntityPlayerMP player) {
        SpiderSenseTrigger.Listeners recipeunlockedtrigger$listeners = this.listeners.get(player.getAdvancements());

        if (recipeunlockedtrigger$listeners != null) {
            recipeunlockedtrigger$listeners.trigger();
        }
    }

    @Override
    public Instance deserializeInstance(JsonObject json, JsonDeserializationContext context) {
        return new SpiderSenseTrigger.Instance();
    }

    public static class Instance extends AbstractCriterionInstance {

        public Instance() {
            super(SpiderSenseTrigger.ID);
        }

    }

    static class Listeners {

        private final PlayerAdvancements playerAdvancements;
        private final Set<Listener<Instance>> listeners = Sets.<SpiderSenseTrigger.Listener<SpiderSenseTrigger.Instance>>newHashSet();

        public Listeners(PlayerAdvancements playerAdvancementsIn) {
            this.playerAdvancements = playerAdvancementsIn;
        }

        public boolean isEmpty() {
            return this.listeners.isEmpty();
        }

        public void add(ICriterionTrigger.Listener<SpiderSenseTrigger.Instance> listener) {
            this.listeners.add(listener);
        }

        public void remove(ICriterionTrigger.Listener<SpiderSenseTrigger.Instance> listener) {
            this.listeners.remove(listener);
        }

        public void trigger() {
            List<Listener<Instance>> list = null;

            for (ICriterionTrigger.Listener<SpiderSenseTrigger.Instance> listener : this.listeners) {
                if (list == null) {
                    list = Lists.<ICriterionTrigger.Listener<SpiderSenseTrigger.Instance>>newArrayList();
                }

                list.add(listener);
            }

            if (list != null) {
                for (ICriterionTrigger.Listener<SpiderSenseTrigger.Instance> listener1 : list) {
                    listener1.grantCriterion(this.playerAdvancements);
                }
            }
        }
    }

}

