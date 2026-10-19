package net.threetag.palladium.logic.automation;

import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.advancements.criterion.SimpleCriterionTrigger;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.threetag.palladium.addonpack.log.AddonPackLog;
import net.threetag.palladium.logic.context.DataContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

public class AutomationManager extends SimpleJsonResourceReloadListener<Automation> {

    public static AutomationManager INSTANCE;

    private final Map<CriterionTrigger<?>, List<Automation>> byTrigger = new HashMap<>();

    public AutomationManager() {
        super(Automation.CODEC, FileToIdConverter.json("palladium/automation"));
    }

    @Override
    protected void apply(Map<Identifier, Automation> automationMap, ResourceManager resourceManager, ProfilerFiller profilerFiller) {
        this.byTrigger.clear();
        automationMap.forEach((_, automation) -> {
            this.byTrigger.computeIfAbsent(automation.trigger().trigger(), _ -> new ArrayList<>()).add(automation);
        });
        AddonPackLog.info(automationMap.size() + " automations loaded");
    }

    @SuppressWarnings("unchecked")
    public void run(ServerPlayer player, SimpleCriterionTrigger<?> criterion, Predicate<?> matcher) {
        List<Automation> automations = this.byTrigger.get(criterion);

        if (automations != null && !automations.isEmpty()) {
            DataContext context = DataContext.forEntity(player);

            if (criterion instanceof AutomationContextEnricher enricher) {
                enricher.enrich(context);
            }

            for (Automation automation : automations) {
                Criterion<?> automationCriterion = automation.trigger();
                if (((Predicate<Object>) matcher).test(automationCriterion.triggerInstance())) {
                    if (automation.condition().test(context)) {
                        for (var action : automation.actions()) {
                            action.run(context);
                        }
                    }
                }
            }
        }
    }
}
