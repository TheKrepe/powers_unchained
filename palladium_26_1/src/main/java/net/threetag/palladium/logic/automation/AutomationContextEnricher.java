package net.threetag.palladium.logic.automation;

import net.minecraft.advancements.CriterionTrigger;
import net.threetag.palladium.logic.context.DataContext;

public interface AutomationContextEnricher {

    void enrich(DataContext context);

}
