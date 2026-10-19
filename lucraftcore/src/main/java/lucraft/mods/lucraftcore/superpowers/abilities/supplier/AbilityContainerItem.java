package lucraft.mods.lucraftcore.superpowers.abilities.supplier;

import lucraft.mods.lucraftcore.superpowers.abilities.Ability;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;

import java.util.UUID;

public class AbilityContainerItem extends AbilityContainer {

    public final EntityEquipmentSlot slot;
    protected ItemStack stack = ItemStack.EMPTY.copy();
    private ItemStack cacheStack = ItemStack.EMPTY.copy();

    public AbilityContainerItem(EntityLivingBase entity, Ability.EnumAbilityContext context, EntityEquipmentSlot slot) {
        super(entity, context);
        this.slot = slot;
    }

    @Override
    public void onUpdate() {
        ItemStack currentStack = getItemStackIfProvider();

        if (!currentStack.isEmpty()) {
            if (!currentStack.hasTagCompound())
                currentStack.setTagCompound(new NBTTagCompound());

            if (!currentStack.getTagCompound().hasKey("AbilityUUID"))
                currentStack.getTagCompound().setTag("AbilityUUID", NBTUtil.createUUIDTag(UUID.randomUUID()));
        }

        if ((this.stack.isEmpty() != currentStack.isEmpty()) || (!currentStack.isEmpty() && this.stack.hasTagCompound() && !NBTUtil.getUUIDFromTag(currentStack.getTagCompound().getCompoundTag("AbilityUUID")).equals(NBTUtil.getUUIDFromTag(this.stack.getTagCompound().getCompoundTag("AbilityUUID"))))) {
            this.cacheStack = currentStack;
            this.switchProvider(currentStack.isEmpty() ? null : (IAbilityProvider) currentStack.getItem());
        }

        boolean dirty = false;

        for (Ability ab : getAbilities()) {
            ab.onUpdate();

            if (ab.sync != null) {
                sync = sync.add(ab.sync);
                ab.sync = EnumSync.NONE;
            }

            if (ab.dirty) {
                dirty = dirty || ab.dirty;
                ab.dirty = false;
            }
        }

        if (dirty) {
            this.save();
        }

        if (sync != EnumSync.NONE) {
            sync();
            this.sync = EnumSync.NONE;
        }
    }

    @Override
    public void switchProvider(IAbilityProvider provider) {
        for (Ability ab : getAbilities())
            if (ab.isUnlocked())
                ab.lastTick();

        if (this.provider != null && !this.stack.isEmpty())
            save();

        this.provider = provider;
        this.stack = cacheStack;
        this.cacheStack = ItemStack.EMPTY;

        if (this.provider != null && !this.stack.isEmpty()) {
            this.abilities = filterAbilities(provider.addDefaultAbilities(this.entity, new Ability.AbilityMap(), this.context));
            load();
        } else {
            this.abilities.clear();
        }

        this.sync = this.sync.add(EnumSync.EVERYONE);
    }

    public ItemStack getItemStackIfProvider() {
        ItemStack stack = this.entity.getItemStackFromSlot(this.slot);
        return stack.getItem() instanceof IAbilityProvider ? stack : ItemStack.EMPTY;
    }

    @Override
    public void save() {
        if (!this.stack.isEmpty() && !entity.world.isRemote) {
            if (!this.stack.hasTagCompound())
                this.stack.setTagCompound(new NBTTagCompound());
            this.stack.getTagCompound().setTag("ItemAbilities", this.serializeNBT());
        }
    }

    @Override
    public void load() {
        if (!this.stack.isEmpty()) {
            NBTTagCompound nbt = this.stack.hasTagCompound() ? this.stack.getTagCompound() : new NBTTagCompound();
            this.deserializeNBT(nbt.getCompoundTag("ItemAbilities"));
        }
    }
}
