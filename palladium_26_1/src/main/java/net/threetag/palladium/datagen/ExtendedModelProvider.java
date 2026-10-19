package net.threetag.palladium.datagen;

import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.ModelProvider;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.threetag.palladium.client.model.item.MultiversalVariantItemModel;
import net.threetag.palladium.client.model.item.MultiversalVariantModels;

import java.util.List;
import java.util.Optional;

public class ExtendedModelProvider extends ModelProvider {

    public ExtendedModelProvider(PackOutput output, String modId) {
        super(output, modId);
    }

    protected Identifier multiversalVariantModel(ItemModelGenerators itemModels, String name, Material texture) {
        Identifier modelLocation = Identifier.fromNamespaceAndPath(this.modId, MultiversalVariantModels.MODEL_PREFIX + name);
        return ModelTemplates.FLAT_ITEM.create(modelLocation, TextureMapping.layer0(texture), itemModels.modelOutput);
    }

    protected Identifier multiversalVariantModel(ItemModelGenerators itemModels, String name, Identifier texture) {
        return this.multiversalVariantModel(itemModels, name, new Material(texture));
    }

    protected Identifier multiversalVariantModel(ItemModelGenerators itemModels, String name) {
        return this.multiversalVariantModel(itemModels, name, new Material(Identifier.fromNamespaceAndPath(this.modId, MultiversalVariantModels.MODEL_PREFIX + name)));
    }

    protected void multiversalVariantItem(ItemModelGenerators itemModels, Item item) {
        itemModels.itemModelOutput.accept(item, new MultiversalVariantItemModel.Unbaked(Optional.empty(), List.of()));
    }
}
