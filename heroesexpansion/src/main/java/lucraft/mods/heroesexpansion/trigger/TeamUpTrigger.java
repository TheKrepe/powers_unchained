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

public class TeamUpTrigger implements ICriterionTrigger<TeamUpTrigger.Instance> {

    private static final ResourceLocation ID = new ResourceLocation(HeroesExpansion.MODID, "team_up");
    private final Map<PlayerAdvancements, Listeners> listeners = Maps.<PlayerAdvancements, TeamUpTrigger.Listeners>newHashMap();

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @Override
    public void addListener(PlayerAdvancements playerAdvancementsIn, Listener<Instance> listener) {
        TeamUpTrigger.Listeners recipeunlockedtrigger$listeners = this.listeners.get(playerAdvancementsIn);

        if (recipeunlockedtrigger$listeners == null) {
            recipeunlockedtrigger$listeners = new TeamUpTrigger.Listeners(playerAdvancementsIn);
            this.listeners.put(playerAdvancementsIn, recipeunlockedtrigger$listeners);
        }

        recipeunlockedtrigger$listeners.add(listener);
    }

    @Override
    public void removeListener(PlayerAdvancements playerAdvancementsIn, Listener<Instance> listener) {
        TeamUpTrigger.Listeners recipeunlockedtrigger$listeners = this.listeners.get(playerAdvancementsIn);

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
        TeamUpTrigger.Listeners recipeunlockedtrigger$listeners = this.listeners.get(player.getAdvancements());

        if (recipeunlockedtrigger$listeners != null) {
            recipeunlockedtrigger$listeners.trigger();
        }
    }

    @Override
    public Instance deserializeInstance(JsonObject json, JsonDeserializationContext context) {
        return new TeamUpTrigger.Instance();
    }

    public static class Instance extends AbstractCriterionInstance {

        public Instance() {
            super(TeamUpTrigger.ID);
        }

    }

    static class Listeners {

        private final PlayerAdvancements playerAdvancements;
        private final Set<Listener<Instance>> listeners = Sets.<Listener<TeamUpTrigger.Instance>>newHashSet();

        public Listeners(PlayerAdvancements playerAdvancementsIn) {
            this.playerAdvancements = playerAdvancementsIn;
        }

        public boolean isEmpty() {
            return this.listeners.isEmpty();
        }

        public void add(Listener<TeamUpTrigger.Instance> listener) {
            this.listeners.add(listener);
        }

        public void remove(Listener<TeamUpTrigger.Instance> listener) {
            this.listeners.remove(listener);
        }

        public void trigger() {
            List<Listener<Instance>> list = null;

            for (Listener<TeamUpTrigger.Instance> listener : this.listeners) {
                if (list == null) {
                    list = Lists.<Listener<TeamUpTrigger.Instance>>newArrayList();
                }

                list.add(listener);
            }

            if (list != null) {
                for (Listener<TeamUpTrigger.Instance> listener1 : list) {
                    listener1.grantCriterion(this.playerAdvancements);
                }
            }
        }
    }

}

