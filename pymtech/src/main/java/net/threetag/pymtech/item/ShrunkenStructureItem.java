package net.threetag.pymtech.item;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntitySize;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;
import net.threetag.pymtech.entity.ShrunkenStructureEntity;
import net.threetag.pymtech.sizechangetype.PTSizeChangeTypes;
import net.threetag.pymtech.storage.ShrunkenStructureData;
import net.threetag.threecore.capability.CapabilitySizeChanging;
import net.threetag.threecore.entity.SolidItemEntity;
import net.threetag.threecore.item.SolidItem;

import javax.annotation.Nullable;
import java.util.function.Supplier;

public class ShrunkenStructureItem extends SolidItem {

    public ShrunkenStructureItem(Properties properties) {
        super(properties);
    }

    @Nullable
    @Override
    public EntitySize getEntitySize(SolidItemEntity entity, ItemStack stack) {
        ShrunkenStructureData data = get(entity.world, stack);
        if (data == null)
            return super.getEntitySize(entity, stack);
        float width = Math.max(data.getSize().getX(), data.getSize().getZ());
        return new EntitySize(width, data.getSize().getY(), false);
    }

    @Nullable
    @Override
    public Entity createEntity(World world, Entity location, ItemStack itemstack) {
        ShrunkenStructureEntity entity = new ShrunkenStructureEntity(world, location.getPosX(), location.getPosY(), location.getPosZ(), itemstack);
        entity.setMotion(location.getMotion());
        entity.getCapability(CapabilitySizeChanging.SIZE_CHANGING).ifPresent(sizeChanging -> sizeChanging.setSizeDirectly(PTSizeChangeTypes.PYM_PARTICLES, 0.1F));

        if (location instanceof ItemEntity) {
            entity.setOwnerId(((ItemEntity) location).getOwnerId());
            entity.setThrowerId(((ItemEntity) location).getThrowerId());
        }

        return entity;
    }

    @Override
    public void fillItemGroup(ItemGroup itemGroup, NonNullList<ItemStack> items) {

    }

    public static ItemStack create(ShrunkenStructureData data) {
        ItemStack stack = new ItemStack(PTItems.SHRUNKEN_STRUCTURE.get());
        stack.getOrCreateTag().putInt("ShrunkenStructure", data.getId());
        return stack;
    }

    public static ShrunkenStructureData get(World world, ItemStack stack) {
        return stack.isEmpty() ? null : ShrunkenStructureData.get(world, stack.getOrCreateTag().getInt("ShrunkenStructure"));
    }

    public static ShrunkenStructureData getOrCreate(World world, ItemStack stack, Supplier<ShrunkenStructureData> supplier) {
        ShrunkenStructureData data = get(world, stack);
        if (data == null || data.isEmpty()) {
            ShrunkenStructureData data1 = supplier.get();
            ShrunkenStructureData.register(world, data1);
            return data1;
        } else {
            return data;
        }
    }

}
