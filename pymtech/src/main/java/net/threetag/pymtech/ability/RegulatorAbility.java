package net.threetag.pymtech.ability;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fml.network.NetworkHooks;
import net.threetag.pymtech.item.PTItems;
import net.threetag.threecore.ability.Ability;
import net.threetag.threecore.ability.AbilityType;
import net.threetag.threecore.item.ThreeDataItemHandler;
import net.threetag.threecore.util.icon.ItemIcon;
import net.threetag.threecore.util.threedata.EnumSync;
import net.threetag.threecore.util.threedata.FloatThreeData;
import net.threetag.threecore.util.threedata.ItemStackThreeData;
import net.threetag.threecore.util.threedata.ThreeData;

public abstract class RegulatorAbility extends Ability implements INamedContainerProvider {

    public static final ThreeData<ItemStack> REGULATOR = new ItemStackThreeData("regulator");
    public static final ThreeData<ItemStack>[] VIAL_SLOTS = new ThreeData[]{new ItemStackThreeData("vial_0"),
            new ItemStackThreeData("vial_1"), new ItemStackThreeData("vial_2"),
            new ItemStackThreeData("vial_3"), new ItemStackThreeData("vial_4"),
            new ItemStackThreeData("vial_5"), new ItemStackThreeData("vial_6"),
            new ItemStackThreeData("vial_7"), new ItemStackThreeData("vial_8"),
            new ItemStackThreeData("vial_9"), new ItemStackThreeData("vial_10"),
            new ItemStackThreeData("vial_11")};
    public static final ThreeData<Float> REGULATED_SIZE = new FloatThreeData("regulated_size").setSyncType(EnumSync.SELF);
    public final ThreeDataItemHandler itemHandler;

    public RegulatorAbility(AbilityType type) {
        super(type);
        ThreeData<ItemStack>[] items = new ThreeData[this.getVialSlots() + 1];
        items[0] = REGULATOR;
        for (int i = 1; i <= this.getVialSlots(); i++) {
            items[i] = VIAL_SLOTS[i - 1];
        }
        this.itemHandler = new ThreeDataItemHandler(this, items);
    }

    public abstract int getVialSlots();

    @Override
    public void registerData() {
        super.registerData();
        this.dataManager.register(ICON, new ItemIcon(PTItems.REGULATOR.get()));
        this.dataManager.register(REGULATOR, ItemStack.EMPTY);
        for (int i = 0; i < this.getVialSlots(); i++) {
            this.dataManager.register(VIAL_SLOTS[i], ItemStack.EMPTY);
        }
    }

    @Override
    public void action(LivingEntity entity) {
        if (entity instanceof ServerPlayerEntity)
            NetworkHooks.openGui((ServerPlayerEntity) entity, this, buf -> buf.writeString(this.container.getId().toString() + "#" + this.getId()));
    }

    @Override
    public ITextComponent getDisplayName() {
        return this.dataManager.get(TITLE);
    }

}
