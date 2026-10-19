package net.threetag.palladium.item.suitset;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.ArmorType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingEquipmentChangeEvent;
import net.threetag.palladium.Palladium;
import net.threetag.palladium.entity.data.PalladiumEntityData;
import net.threetag.palladium.entity.data.PalladiumEntityDataTypes;

import java.util.ArrayList;
import java.util.List;

@EventBusSubscriber(modid = Palladium.MOD_ID)
public class EntitySuitSetHandler extends PalladiumEntityData<LivingEntity, EntitySuitSetHandler> {

    public static final Codec<EntitySuitSetHandler> CODEC = MapCodec.unit(EntitySuitSetHandler::new).codec();

    private final List<SuitSet> suitSets = new ArrayList<>();
    private final ItemStack[] lastEquipment = new ItemStack[ArmorType.values().length];

    @Override
    protected void init() {
        this.updateSuitSets();
    }

    @Override
    public void tick() {
        var entity = this.getEntity();

        if (entity.level().isClientSide() && this.equipmentChanged(entity)) {
            this.updateSuitSets();
        }
    }

    private boolean equipmentChanged(LivingEntity entity) {
        for (ArmorType slot : ArmorType.values()) {
            if (entity.getItemBySlot(slot.getSlot()) != this.lastEquipment[slot.ordinal()]) {
                return true;
            }
        }

        return false;
    }

    private void updateSuitSets() {
        var entity = this.getEntity();

        this.suitSets.clear();
        for (ArmorType slot : ArmorType.values()) {
            var stack = entity.getItemBySlot(slot.getSlot());
            this.lastEquipment[slot.ordinal()] = stack;
            SuitSet candidate = SuitSets.getFromItem(stack.getItem());
            if (candidate != null && !this.suitSets.contains(candidate) && candidate.isWearing(entity)) {
                this.suitSets.add(candidate);
            }
        }

        if (!entity.level().isClientSide()) {
            var powerHandler = PalladiumEntityData.get(entity, PalladiumEntityDataTypes.POWER_HANDLER.get());
            if (powerHandler != null) {
                powerHandler.markDirty();
            }
        }
    }

    @SubscribeEvent
    static void onEquipmentChange(LivingEquipmentChangeEvent e) {
        if (e.getSlot().getType() != EquipmentSlot.Type.HUMANOID_ARMOR) {
            return;
        }

        var handler = PalladiumEntityData.get(e.getEntity(), PalladiumEntityDataTypes.SUIT_SETS.get());
        if (handler != null) {
            handler.updateSuitSets();
        }
    }

    public List<SuitSet> getSuitSets() {
        return this.suitSets;
    }


    public boolean isWearing(SuitSet suitSet) {
        return this.suitSets.contains(suitSet);
    }

    @Override
    public Codec<EntitySuitSetHandler> codec() {
        return CODEC;
    }

    public static EntitySuitSetHandler get(LivingEntity entity) {
        return PalladiumEntityData.get(entity, PalladiumEntityDataTypes.SUIT_SETS.get());
    }
}
