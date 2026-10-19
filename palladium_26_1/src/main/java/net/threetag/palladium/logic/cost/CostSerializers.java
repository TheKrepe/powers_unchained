package net.threetag.palladium.logic.cost;

import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.threetag.palladium.Palladium;
import net.threetag.palladium.registry.PalladiumRegistryKeys;

public class CostSerializers {

    public static final DeferredRegister<CostSerializer<?>> COST_SERIALIZERS = DeferredRegister.create(PalladiumRegistryKeys.COST_SERIALIZER, Palladium.MOD_ID);

    public static final DeferredHolder<CostSerializer<?>, ItemCost.Serializer> ITEM = COST_SERIALIZERS.register("item", ItemCost.Serializer::new);
    public static final DeferredHolder<CostSerializer<?>, ExperienceLevelCost.Serializer> EXPERIENCE_LEVEL = COST_SERIALIZERS.register("experience_level", ExperienceLevelCost.Serializer::new);
    public static final DeferredHolder<CostSerializer<?>, ScoreCost.Serializer> SCORE = COST_SERIALIZERS.register("score", ScoreCost.Serializer::new);
    public static final DeferredHolder<CostSerializer<?>, NumberDataAttachmentCost.Serializer> NUMBER_DATA_ATTACHMENT = COST_SERIALIZERS.register("number_data_attachment", NumberDataAttachmentCost.Serializer::new);
    public static final DeferredHolder<CostSerializer<?>, MultiversalDesignationCost.Serializer> MULTIVERSAL_DESIGNATION = COST_SERIALIZERS.register("multiversal_designation", MultiversalDesignationCost.Serializer::new);

}
