package lucraft.mods.lucraftcore.util.triggers;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import lucraft.mods.lucraftcore.LucraftCore;
import lucraft.mods.lucraftcore.superpowers.Superpower;
import lucraft.mods.lucraftcore.superpowers.SuperpowerHandler;
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

public class LoseSuperpowerTrigger implements ICriterionTrigger<LoseSuperpowerTrigger.Instance> {

    private static final ResourceLocation ID = new ResourceLocation(LucraftCore.MODID, "lose_superpower");
    private final Map<PlayerAdvancements, LoseSuperpowerTrigger.Listeners> listeners = Maps.<PlayerAdvancements, LoseSuperpowerTrigger.Listeners>newHashMap();

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @Override
    public void addListener(PlayerAdvancements playerAdvancementsIn, net.minecraft.advancements.ICriterionTrigger.Listener<Instance> listener) {
        LoseSuperpowerTrigger.Listeners recipeunlockedtrigger$listeners = this.listeners.get(playerAdvancementsIn);

        if (recipeunlockedtrigger$listeners == null) {
            recipeunlockedtrigger$listeners = new LoseSuperpowerTrigger.Listeners(playerAdvancementsIn);
            this.listeners.put(playerAdvancementsIn, recipeunlockedtrigger$listeners);
        }

        recipeunlockedtrigger$listeners.add(listener);
    }

    @Override
    public void removeListener(PlayerAdvancements playerAdvancementsIn, net.minecraft.advancements.ICriterionTrigger.Listener<Instance> listener) {
        LoseSuperpowerTrigger.Listeners recipeunlockedtrigger$listeners = this.listeners.get(playerAdvancementsIn);

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

    public void trigger(EntityPlayerMP player, Superpower superpower) {
        LoseSuperpowerTrigger.Listeners recipeunlockedtrigger$listeners = this.listeners.get(player.getAdvancements());

        if (recipeunlockedtrigger$listeners != null) {
            recipeunlockedtrigger$listeners.trigger(superpower);
        }
    }

    @Override
    public Instance deserializeInstance(JsonObject json, JsonDeserializationContext context) {
        Superpower superpower = json.has("superpower") ? SuperpowerHandler.SUPERPOWER_REGISTRY.getValue(new ResourceLocation(JsonUtils.getString(json, "superpower"))) : null;
        return new LoseSuperpowerTrigger.Instance(superpower);
    }

    public static class Instance extends AbstractCriterionInstance {

        @Nullable
        private final Superpower superpower;

        public Instance(@Nullable Superpower superpower) {
            super(LoseSuperpowerTrigger.ID);
            this.superpower = superpower;
        }

        public boolean test(Superpower superpower) {
            return this.superpower == superpower;
        }

    }

    static class Listeners {

        private final PlayerAdvancements playerAdvancements;
        private final Set<ICriterionTrigger.Listener<LoseSuperpowerTrigger.Instance>> listeners = Sets.<LoseSuperpowerTrigger.Listener<LoseSuperpowerTrigger.Instance>>newHashSet();

        public Listeners(PlayerAdvancements playerAdvancementsIn) {
            this.playerAdvancements = playerAdvancementsIn;
        }

        public boolean isEmpty() {
            return this.listeners.isEmpty();
        }

        public void add(ICriterionTrigger.Listener<LoseSuperpowerTrigger.Instance> listener) {
            this.listeners.add(listener);
        }

        public void remove(ICriterionTrigger.Listener<LoseSuperpowerTrigger.Instance> listener) {
            this.listeners.remove(listener);
        }

        public void trigger(Superpower superpower) {
            List<ICriterionTrigger.Listener<LoseSuperpowerTrigger.Instance>> list = null;

            for (ICriterionTrigger.Listener<LoseSuperpowerTrigger.Instance> listener : this.listeners) {
                if (((LoseSuperpowerTrigger.Instance) listener.getCriterionInstance()).test(superpower)) {
                    if (list == null) {
                        list = Lists.<ICriterionTrigger.Listener<LoseSuperpowerTrigger.Instance>>newArrayList();
                    }

                    list.add(listener);
                }
            }

            if (list != null) {
                for (ICriterionTrigger.Listener<LoseSuperpowerTrigger.Instance> listener1 : list) {
                    listener1.grantCriterion(this.playerAdvancements);
                }
            }
        }
    }

}
