package net.threetag.palladium.icon;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;

public record ItemIcon(ItemStackTemplate itemStackTemplate) implements Icon {

    public static final MapCodec<ItemIcon> CODEC = RecordCodecBuilder.mapCodec((instance) -> instance
            .group(ItemStackTemplate.CODEC.fieldOf("item").forGetter(ItemIcon::getItem))
            .apply(instance, ItemIcon::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, ItemIcon> STREAM_CODEC = StreamCodec.composite(
            ItemStackTemplate.STREAM_CODEC, ItemIcon::getItem, ItemIcon::new
    );

    public ItemIcon(ItemLike itemLike) {
        this(new ItemStackTemplate(itemLike.asItem()));
    }

    @Override
    public IconSerializer<ItemIcon> getSerializer() {
        return IconSerializers.ITEM.get();
    }

    public ItemStackTemplate getItem() {
        return this.itemStackTemplate;
    }

    @Override
    public String toString() {
        return "ItemIcon{" + "itemStackTemplate=" + itemStackTemplate + '}';
    }

    public static class Serializer extends IconSerializer<ItemIcon> {

        @Override
        public MapCodec<ItemIcon> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, ItemIcon> streamCodec() {
            return STREAM_CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<Icon, ItemIcon> builder, HolderLookup.Provider provider) {
            builder.setName("Item").setDescription("Displays an item")
                    .add("item", TYPE_ITEM_STACK, "The item that will be displayed")
                    .addExampleObject(new ItemIcon(new ItemStackTemplate(Items.APPLE)));
        }
    }

}
