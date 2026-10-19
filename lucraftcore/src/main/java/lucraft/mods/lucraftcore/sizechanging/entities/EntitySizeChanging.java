package lucraft.mods.lucraftcore.sizechanging.entities;

import io.netty.buffer.ByteBuf;
import lucraft.mods.lucraftcore.sizechanging.sizechanger.SizeChanger;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHandSide;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Arrays;

public class EntitySizeChanging extends EntityLivingBase implements IEntityAdditionalSpawnData {

    public EntityLivingBase parent;
    public float size;
    public int lifeTime;
    public float limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch, scale;
    public SizeChanger sizeChanger;
    @SideOnly(Side.CLIENT)
    public RenderLivingBase renderer;

    public EntitySizeChanging(World worldIn) {
        super(worldIn);
        setSize(0.1F, 0.1F);
        noClip = true;
        ignoreFrustumCheck = true;
    }

    public EntitySizeChanging(World worldIn, EntityLivingBase entity, SizeChanger sizeChanger, float size, int lifeTime) {
        this(worldIn);
        this.parent = entity;
        this.setLocationAndAngles(entity.prevPosX, entity.prevPosY, entity.prevPosZ, entity.rotationYaw, entity.rotationPitch);
        this.sizeChanger = sizeChanger;
        this.size = size;
        this.lifeTime = lifeTime;
        this.renderYawOffset = entity.renderYawOffset;
    }

    @Override
    public void onUpdate() {

        if (this.ticksExisted >= lifeTime) {
            this.setDead();
        }
    }

    @Override
    public Iterable<ItemStack> getArmorInventoryList() {
        return Arrays.asList();
    }

    @Override
    public ItemStack getItemStackFromSlot(EntityEquipmentSlot slotIn) {
        return ItemStack.EMPTY;
    }

    @Override
    public void setItemStackToSlot(EntityEquipmentSlot slotIn, ItemStack stack) {

    }

    @Override
    public EnumHandSide getPrimaryHand() {
        return this.parent.getPrimaryHand();
    }

    @Override
    public void writeSpawnData(ByteBuf buffer) {
        buffer.writeInt(this.lifeTime);
        buffer.writeFloat(this.size);
    }

    @Override
    public void readSpawnData(ByteBuf additionalData) {
        this.lifeTime = additionalData.readInt();
        this.size = additionalData.readFloat();
    }
}
