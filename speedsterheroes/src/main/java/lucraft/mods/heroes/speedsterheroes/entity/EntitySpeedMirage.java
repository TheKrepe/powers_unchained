package lucraft.mods.heroes.speedsterheroes.entity;

import java.util.LinkedList;
import java.util.Random;

import lucraft.mods.heroes.speedsterheroes.SpeedsterHeroes;
import lucraft.mods.heroes.speedsterheroes.client.render.SHRenderer;
import lucraft.mods.heroes.speedsterheroes.speedstertypes.SpeedsterType;
import lucraft.mods.heroes.speedsterheroes.superpower.SpeedforcePlayerHandler;
import lucraft.mods.heroes.speedsterheroes.trailtypes.TrailType;
import lucraft.mods.heroes.speedsterheroes.util.SpeedsterHeroesUtil;
import lucraft.mods.lucraftcore.superpower.SuperpowerHandler;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumHandSide;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class EntitySpeedMirage extends EntityLivingBase {

	public EntityLivingBase acquired;
	public TrailType trail;
	public float scale;
	public float[] lightningFactor;
	public float alpha = 1F;
	
	public int progress;

	Random rand = new Random();
	
	public EntitySpeedMirage(World par1World) {
		super(par1World);
		setSize(0.1F, 0.1F);
		noClip = true;
		ignoreFrustumCheck = true;
	}

	public EntitySpeedMirage(World par1World, EntityLivingBase ac, Vec3d pos, boolean shouldRender) {
		super(par1World);
		this.acquired = ac;
		this.progress = 0;
		setSize(ac.width, ac.height);
		noClip = true;
//		renderDistanceWeight = 10D;
		ignoreFrustumCheck = true;
		
		setLocationAndAngles(acquired.posX, acquired.posY, acquired.posZ, acquired.rotationYaw, acquired.rotationPitch);
		
		SpeedsterType type = SpeedsterHeroesUtil.getSpeedsterType(acquired);
		this.trail = type != null ? type.getTrailType() : (ac instanceof EntityPlayer ? (SuperpowerHandler.hasSuperpower((EntityPlayer) ac, SpeedsterHeroes.speedforce) ? SuperpowerHandler.getSpecificSuperpowerPlayerHandler((EntityPlayer)ac, SpeedforcePlayerHandler.class).customTrailType : TrailType.lightnings_gold) : TrailType.lightnings_gold);
		if(trail == null)
			trail = (ac instanceof EntityPlayer ? (SuperpowerHandler.hasSuperpower((EntityPlayer) ac, SpeedsterHeroes.speedforce) ? SuperpowerHandler.getSpecificSuperpowerPlayerHandler((EntityPlayer)ac, SpeedforcePlayerHandler.class).customTrailType : TrailType.lightnings_gold) : TrailType.lightnings_gold);
		
		this.lightningFactor = new float[20];
		for(int i = 0; i < 20; i++) {
			this.lightningFactor[i] = rand.nextFloat();
		}
		
		//----------------------------------------------------------------------------
		
//		this.swingProgress = acquired.swingProgress;
//		this.prevSwingProgress = acquired.prevSwingProgress;
//		this.prevRenderYawOffset = acquired.prevRenderYawOffset;
//		this.renderYawOffset = acquired.renderYawOffset;
//		this.prevRotationYawHead = acquired.prevRotationYawHead;
//		this.rotationYawHead = acquired.rotationYawHead;
//		this.prevRotationPitch = acquired.prevRotationPitch;
//		this.rotationPitch = acquired.rotationPitch;
//		this.limbSwingAmount = acquired.limbSwingAmount;
//		this.prevLimbSwingAmount = acquired.prevLimbSwingAmount;
//		this.limbSwing = acquired.limbSwing;
		
		this.swingProgress = acquired.swingProgress;
		this.prevSwingProgress = acquired.swingProgress;
		this.prevRenderYawOffset = acquired.renderYawOffset;
		this.renderYawOffset = acquired.renderYawOffset;
		this.prevRotationYawHead = acquired.rotationYawHead;
		this.rotationYawHead = acquired.rotationYawHead;
		this.prevRotationPitch = acquired.rotationPitch;
		this.rotationPitch = acquired.rotationPitch;
		this.limbSwingAmount = acquired.limbSwingAmount;
		this.prevLimbSwingAmount = acquired.limbSwingAmount;
		this.limbSwing = acquired.limbSwing;
	}
	
	public Vec3d getLightningPosVector(int i) {
		float halfWidth = width / 2;
		return new Vec3d(posX - halfWidth + (lightningFactor[i] * width), posY, posZ - halfWidth + (lightningFactor[10 + i] * width));
	}
	
	@Override
	public void onUpdate() {

		progress++;
		
		if (progress >= 11) {
			setDead();
			LinkedList<EntitySpeedMirage> list = SHRenderer.getSpeedMiragesFromPlayer(acquired);
			if(list.contains(this)) {
				list.remove(this);
				SHRenderer.trailDataMap.put(acquired, list);
			}
			return;
		}
	}

	@Override
	public boolean shouldRenderInPass(int pass) {
		return pass == 1;
	}

	@Override
	public boolean canBeCollidedWith() {
		return false;
	}

	@Override
	public boolean canBePushed() {
		return false;
	}

	@Override
	public boolean isEntityAlive() {
		return !this.isDead;
	}

	@Override
	public void setHealth(float par1) {
	}

	@Override
	public boolean writeToNBTOptional(NBTTagCompound par1NBTTagCompound) {
		return false;
	}

	@Override
	protected void entityInit() {
		super.entityInit();
	}

	@Override
	public void readEntityFromNBT(NBTTagCompound nbttagcompound) {
	}

	@Override
	public void writeEntityToNBT(NBTTagCompound nbttagcompound) {
	}

	@Override
	public Iterable<ItemStack> getArmorInventoryList() {
		return this.acquired.getArmorInventoryList();
	}

	@Override
	public ItemStack getItemStackFromSlot(EntityEquipmentSlot slotIn) {
		return this.acquired.getItemStackFromSlot(slotIn);
	}

	@Override
	public void setItemStackToSlot(EntityEquipmentSlot slotIn, ItemStack stack) {
		
	}

	@Override
	public EnumHandSide getPrimaryHand() {
		return acquired.getPrimaryHand();
	}
}