package net.threetag.palladium.item;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.transfer.ItemAccessResourceHandler;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.threetag.palladium.Palladium;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@EventBusSubscriber(modid = Palladium.MOD_ID)
public class VialItem extends Item {

    public static final int VOLUME = 500;

    public static final MapCodec<VialItem> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            BuiltInRegistries.FLUID.byNameCodec().optionalFieldOf("fluid", Fluids.EMPTY).forGetter(v -> v.content),
            ItemTypes.propertiesCodec()
    ).apply(instance, VialItem::new));

    private static final Map<Fluid, VialItem> FILLED_VIALS = new HashMap<>();

    public final Fluid content;

    public VialItem(Fluid content, Properties properties) {
        super(properties);
        this.content = content;

        if (content != Fluids.EMPTY) {
            FILLED_VIALS.put(content, this);
        }
    }

    public boolean isEmpty() {
        return this.content == Fluids.EMPTY;
    }

    public static Item getEmptyVial() {
        return PalladiumItems.VIAL.get();
    }

    @Nullable
    public static VialItem getFilledVial(Fluid fluid) {
        return FILLED_VIALS.get(fluid);
    }

    public static Collection<VialItem> getFilledVials() {
        return Collections.unmodifiableCollection(FILLED_VIALS.values());
    }

    @SubscribeEvent
    static void registerCapabilities(RegisterCapabilitiesEvent e) {
        for (Item item : BuiltInRegistries.ITEM) {
            if (item instanceof VialItem) {
                e.registerItem(Capabilities.Fluid.ITEM, (stack, access) -> new ResourceHandler(access), item);
            }
        }
    }

    public static class ResourceHandler extends ItemAccessResourceHandler<FluidResource> {

        public ResourceHandler(ItemAccess itemAccess) {
            super(itemAccess, 1);
        }

        @Override
        protected FluidResource getResourceFrom(ItemResource accessResource, int index) {
            return accessResource.getItem() instanceof VialItem vial && !vial.isEmpty() ? FluidResource.of(vial.content) : FluidResource.EMPTY;
        }

        @Override
        protected int getAmountFrom(ItemResource accessResource, int index) {
            return this.getResourceFrom(accessResource, index).isEmpty() ? 0 : VOLUME;
        }

        @Override
        protected ItemResource update(ItemResource accessResource, int index, FluidResource newResource, int newAmount) {
            if (newAmount == 0) {
                return ItemResource.of(getEmptyVial());
            }

            if (newAmount != VOLUME) {
                return ItemResource.EMPTY;
            }

            var filled = getFilledVial(newResource.getFluid());
            return filled != null ? ItemResource.of(filled) : ItemResource.EMPTY;
        }

        @Override
        protected int getCapacity(int index, FluidResource resource) {
            Objects.checkIndex(index, this.size());
            return VOLUME;
        }
    }
}
