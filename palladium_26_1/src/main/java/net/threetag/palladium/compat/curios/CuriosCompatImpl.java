package net.threetag.palladium.compat.curios;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.NeoForge;
import net.threetag.palladium.entity.data.PalladiumEntityData;
import net.threetag.palladium.entity.data.PalladiumEntityDataTypes;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.CuriosSlotTypes;
import top.theillusivec4.curios.api.event.CurioChangeEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CuriosCompatImpl extends CuriosCompat {

    public static void register() {
        CuriosCompat.INSTANCE = new CuriosCompatImpl();
        NeoForge.EVENT_BUS.addListener(CurioChangeEvent.Item.class, CuriosCompatImpl::onCurioChange);
        NeoForge.EVENT_BUS.addListener(CurioChangeEvent.State.class, CuriosCompatImpl::onCurioChange);
    }

    private static void onCurioChange(CurioChangeEvent e) {
        var handler = PalladiumEntityData.get(e.getEntity(), PalladiumEntityDataTypes.POWER_HANDLER.get());

        if (handler != null) {
            handler.markDirty();
        }
    }

    @Override
    public List<String> getSlots(Level level) {
        return CuriosSlotTypes.getSlotTypes(level.isClientSide()).keySet().stream().toList();
    }

    @Override
    public List<ItemStack> getFromSlot(LivingEntity entity, String slot) {
        return CuriosApi.getCuriosInventory(entity)
                .flatMap(handler -> handler.getStacksHandler(slot))
                .map(stacksHandler -> {
                    var stacks = stacksHandler.getStacks();
                    List<ItemStack> result = new ArrayList<>();
                    for (int i = 0; i < stacks.getSlots(); i++) {
                        result.add(stacks.getStackInSlot(i));
                    }
                    return result;
                })
                .orElse(Collections.emptyList());
    }

    @Override
    public ItemStack getFromSlot(LivingEntity entity, String slot, int index) {
        return CuriosApi.getCuriosInventory(entity)
                .flatMap(handler -> handler.getStacksHandler(slot))
                .map(stacksHandler -> stacksHandler.getStacks().getStackInSlot(index))
                .orElse(ItemStack.EMPTY);
    }

    @Override
    public void setInSlot(LivingEntity entity, String slot, int index, ItemStack stack) {
        CuriosApi.getCuriosInventory(entity)
                .flatMap(handler -> handler.getStacksHandler(slot))
                .ifPresent(stacksHandler -> stacksHandler.getStacks().setStackInSlot(index, stack));
    }

    @Override
    public int getSlotSize(LivingEntity entity, String slot) {
        return CuriosApi.getCuriosInventory(entity)
                .flatMap(handler -> handler.getStacksHandler(slot))
                .map(stacksHandler -> stacksHandler.getStacks().getSlots())
                .orElse(0);
    }

    @Override
    public void clearSlot(LivingEntity entity, String slot) {
        CuriosApi.getCuriosInventory(entity)
                .flatMap(handler -> handler.getStacksHandler(slot))
                .ifPresent(stacksHandler -> {
                    var stacks = stacksHandler.getStacks();
                    for (int i = 0; i < stacks.getSlots(); i++) {
                        stacks.setStackInSlot(i, ItemStack.EMPTY);
                    }
                });
    }
}
