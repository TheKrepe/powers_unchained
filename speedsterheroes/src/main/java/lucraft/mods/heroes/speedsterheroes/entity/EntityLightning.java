package lucraft.mods.heroes.speedsterheroes.entity;

import java.util.List;
import java.util.UUID;

import io.netty.buffer.ByteBuf;
import lucraft.mods.heroes.speedsterheroes.SpeedsterHeroes;
import lucraft.mods.heroes.speedsterheroes.proxies.SpeedsterHeroesProxy;
import lucraft.mods.heroes.speedsterheroes.speedstertypes.SpeedsterType;
import lucraft.mods.heroes.speedsterheroes.superpower.SpeedforcePlayerHandler;
import lucraft.mods.lucraftcore.superpower.SuperpowerHandler;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IProjectile;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class EntityLightning extends Entity implements IProjectile, IEntityAdditionalSpawnData {

	public SpeedsterType type;
	public float velocity;
	public int progress;

	private int xTile = -1;
	private int yTile = -1;
	private int zTile = -1;
	private Block inTile;
	protected boolean inGround;
	public int throwableShake;
	/** The entity that threw this throwable item. */
	private EntityLivingBase thrower;
	private String throwerName;
	private int ticksInGround;
	private int ticksInAir;

	public EntityLightning(World world, EntityLivingBase entity, SpeedsterType type, double x, double y, double z) {
		super(world);
		progress = 20 * 20;

		this.type = type;

		if (entity instanceof EntityPlayer) {
			this.velocity = MathHelper.clamp((SuperpowerHandler.hasSuperpower((EntityPlayer) entity, SpeedsterHeroes.speedforce) ? SuperpowerHandler.getSpecificSuperpowerPlayerHandler((EntityPlayer)entity, SpeedforcePlayerHandler.class).speedLevel : 1) / 5F, 1, 10);
		}

		this.thrower = entity;
		this.setSize(0.25F, 0.25F);
		this.setLocationAndAngles(entity.posX, entity.posY + (double) entity.getEyeHeight(), entity.posZ, entity.rotationYaw, entity.rotationPitch);
		this.posX -= (double) (MathHelper.cos(this.rotationYaw / 180.0F * (float) Math.PI) * 0.16F);
		this.posY -= 0.10000000149011612D;
		this.posZ -= (double) (MathHelper.sin(this.rotationYaw / 180.0F * (float) Math.PI) * 0.16F);
		this.setPosition(this.posX, this.posY, this.posZ);
		float f = 0.4F;
		this.motionX = (double) (-MathHelper.sin(this.rotationYaw / 180.0F * (float) Math.PI) * MathHelper.cos(this.rotationPitch / 180.0F * (float) Math.PI) * f);
		this.motionZ = (double) (MathHelper.cos(this.rotationYaw / 180.0F * (float) Math.PI) * MathHelper.cos(this.rotationPitch / 180.0F * (float) Math.PI) * f);
		this.motionY = (double) (-MathHelper.sin((this.rotationPitch + this.getInaccuracy()) / 180.0F * (float) Math.PI) * f);
		this.setThrowableHeading(this.motionX, this.motionY, this.motionZ, this.velocity, 1.0F);
	}

	public SpeedsterType getSpeedsterType() {
		return SpeedsterType.flashS2;
	}

	protected float getVelocity() {
		return this.velocity;
	}

	protected float getInaccuracy() {
		return 0.0F;
	}

	public void setThrowableHeading(double x, double y, double z, float velocity, float inaccuracy) {
		float f = MathHelper.sqrt(x * x + y * y + z * z);
		x = x / (double) f;
		y = y / (double) f;
		z = z / (double) f;
		x = x + this.rand.nextGaussian() * 0.007499999832361937D * (double) inaccuracy;
		y = y + this.rand.nextGaussian() * 0.007499999832361937D * (double) inaccuracy;
		z = z + this.rand.nextGaussian() * 0.007499999832361937D * (double) inaccuracy;
		x = x * (double) velocity;
		y = y * (double) velocity;
		z = z * (double) velocity;
		this.motionX = x;
		this.motionY = y;
		this.motionZ = z;
		float f1 = MathHelper.sqrt(x * x + z * z);
		this.prevRotationYaw = this.rotationYaw = (float) (MathHelper.atan2(x, z) * 180.0D / Math.PI);
		this.prevRotationPitch = this.rotationPitch = (float) (MathHelper.atan2(y, (double) f1) * 180.0D / Math.PI);
		this.ticksInGround = 0;
	}

	@SideOnly(Side.CLIENT)
	public void setVelocity(double x, double y, double z) {
		this.motionX = x;
		this.motionY = y;
		this.motionZ = z;

		if (this.prevRotationPitch == 0.0F && this.prevRotationYaw == 0.0F) {
			float f = MathHelper.sqrt(x * x + z * z);
			this.prevRotationYaw = this.rotationYaw = (float) (MathHelper.atan2(x, z) * 180.0D / Math.PI);
			this.prevRotationPitch = this.rotationPitch = (float) (MathHelper.atan2(y, (double) f) * 180.0D / Math.PI);
		}
	}

	public EntityLightning(World world) {
		super(world);
		this.setSize(0.25F, 0.25F);
	}

	@Override
	public void onUpdate() {
		this.lastTickPosX = this.posX;
		this.lastTickPosY = this.posY;
		this.lastTickPosZ = this.posZ;
		super.onUpdate();

		if (this.throwableShake > 0) {
			--this.throwableShake;
		}

		if (this.inGround) {
			if (this.world.getBlockState(new BlockPos(this.xTile, this.yTile, this.zTile)).getBlock() == this.inTile) {
				++this.ticksInGround;

				if (this.ticksInGround == 1200) {
					this.setDead();
				}

				return;
			}

			this.inGround = false;
			this.motionX *= (double) (this.rand.nextFloat() * 0.2F);
			this.motionY *= (double) (this.rand.nextFloat() * 0.2F);
			this.motionZ *= (double) (this.rand.nextFloat() * 0.2F);
			this.ticksInGround = 0;
			this.ticksInAir = 0;
		} else {
			++this.ticksInAir;
		}

		Vec3d Vec3d = new Vec3d(this.posX, this.posY, this.posZ);
		Vec3d Vec3d1 = new Vec3d(this.posX + this.motionX, this.posY + this.motionY, this.posZ + this.motionZ);
		RayTraceResult movingobjectposition = this.world.rayTraceBlocks(Vec3d, Vec3d1);
		Vec3d = new Vec3d(this.posX, this.posY, this.posZ);
		Vec3d1 = new Vec3d(this.posX + this.motionX, this.posY + this.motionY, this.posZ + this.motionZ);

		if (movingobjectposition != null) {
			Vec3d1 = new Vec3d(movingobjectposition.hitVec.xCoord, movingobjectposition.hitVec.yCoord, movingobjectposition.hitVec.zCoord);
		}

		if (!this.world.isRemote) {
			Entity entity = null;
			List<Entity> list = this.world.getEntitiesWithinAABBExcludingEntity(this, this.getEntityBoundingBox().addCoord(this.motionX, this.motionY, this.motionZ).expand(1.0D, 1.0D, 1.0D));
			double d0 = 0.0D;
			EntityLivingBase entitylivingbase = this.getThrower();

			for (int j = 0; j < list.size(); ++j) {
				Entity entity1 = (Entity) list.get(j);

				if (entity1.canBeCollidedWith() && (entity1 != entitylivingbase || this.ticksInAir >= 5)) {
					float f = 0.3F;
					AxisAlignedBB axisalignedbb = entity1.getEntityBoundingBox().expand((double) f, (double) f, (double) f);
					RayTraceResult movingobjectposition1 = axisalignedbb.calculateIntercept(Vec3d, Vec3d1);

					if (movingobjectposition1 != null) {
						double d1 = Vec3d.squareDistanceTo(movingobjectposition1.hitVec);

						if (d1 < d0 || d0 == 0.0D) {
							entity = entity1;
							d0 = d1;
						}
					}
				}
			}

			if (entity != null) {
				movingobjectposition = new RayTraceResult(entity);
			}
		}

		if (movingobjectposition != null) {
			if (movingobjectposition.typeOfHit == RayTraceResult.Type.BLOCK && this.world.getBlockState(movingobjectposition.getBlockPos()).getBlock() == Blocks.PORTAL) {
				this.setPortal(movingobjectposition.getBlockPos());
			} else {
				this.onImpact(movingobjectposition);
			}
		}

		this.posX += this.motionX / getVelocity();
		this.posY += this.motionY / getVelocity();
		this.posZ += this.motionZ / getVelocity();
		float f1 = MathHelper.sqrt(this.motionX * this.motionX + this.motionZ * this.motionZ);
//		this.rotationYaw = (float) (MathHelper.atan2(this.motionX, this.motionZ) * 180.0D / Math.PI);

		for (this.rotationPitch = (float) (MathHelper.atan2(this.motionY, (double) f1) * 180.0D / Math.PI); this.rotationPitch - this.prevRotationPitch < -180.0F; this.prevRotationPitch -= 360.0F) {
			;
		}

		while (this.rotationPitch - this.prevRotationPitch >= 180.0F) {
			this.prevRotationPitch += 360.0F;
		}

//		while (this.rotationYaw - this.prevRotationYaw < -180.0F) {
//			this.prevRotationYaw -= 360.0F;
//		}

//		while (this.rotationYaw - this.prevRotationYaw >= 180.0F) {
//			this.prevRotationYaw += 360.0F;
//		}

		this.rotationPitch = this.prevRotationPitch + (this.rotationPitch - this.prevRotationPitch) * 0.2F;
//		this.rotationYaw = this.prevRotationYaw + (this.rotationYaw - this.prevRotationYaw) * 0.2F;
		
		float f2 = getVelocity();

		if (this.isInWater()) {
			this.setDead();
		}

		this.motionX *= (double) f2;
		this.motionY *= (double) f2;
		this.motionZ *= (double) f2;
		this.setPosition(this.posX, this.posY, this.posZ);

		progress--;

		if (progress <= 0)
			this.setDead();
	}

	public void writeEntityToNBT(NBTTagCompound tagCompound) {
		tagCompound.setShort("xTile", (short) this.xTile);
		tagCompound.setShort("yTile", (short) this.yTile);
		tagCompound.setShort("zTile", (short) this.zTile);
		ResourceLocation resourcelocation = (ResourceLocation) Block.REGISTRY.getNameForObject(this.inTile);
		tagCompound.setString("inTile", resourcelocation == null ? "" : resourcelocation.toString());
		tagCompound.setByte("shake", (byte) this.throwableShake);
		tagCompound.setByte("inGround", (byte) (this.inGround ? 1 : 0));

		if ((this.throwerName == null || this.throwerName.length() == 0) && this.thrower instanceof EntityPlayer) {
			this.throwerName = this.thrower.getName();
		}

		tagCompound.setString("ownerName", this.throwerName == null ? "" : this.throwerName);
	}

	public void readEntityFromNBT(NBTTagCompound tagCompund) {
		this.xTile = tagCompund.getShort("xTile");
		this.yTile = tagCompund.getShort("yTile");
		this.zTile = tagCompund.getShort("zTile");

		if (tagCompund.hasKey("inTile", 8)) {
			this.inTile = Block.getBlockFromName(tagCompund.getString("inTile"));
		} else {
			this.inTile = Block.getBlockById(tagCompund.getByte("inTile") & 255);
		}

		this.throwableShake = tagCompund.getByte("shake") & 255;
		this.inGround = tagCompund.getByte("inGround") == 1;
		this.thrower = null;
		this.throwerName = tagCompund.getString("ownerName");

		if (this.throwerName != null && this.throwerName.length() == 0) {
			this.throwerName = null;
		}

		this.thrower = this.getThrower();
	}

	public EntityLivingBase getThrower() {
		if (this.thrower == null && this.throwerName != null && this.throwerName.length() > 0) {
			this.thrower = this.world.getPlayerEntityByName(this.throwerName);

			if (this.thrower == null && this.world instanceof WorldServer) {
				try {
					Entity entity = ((WorldServer) this.world).getEntityFromUuid(UUID.fromString(this.throwerName));

					if (entity instanceof EntityLivingBase) {
						this.thrower = (EntityLivingBase) entity;
					}
				} catch (Throwable var2) {
					this.thrower = null;
				}
			}
		}

		return this.thrower;
	}

	@SideOnly(Side.CLIENT)
	public boolean isInRangeToRenderDist(double distance) {
		double d0 = this.getEntityBoundingBox().getAverageEdgeLength() * 4.0D;

		if (Double.isNaN(d0)) {
			d0 = 4.0D;
		}

		d0 = d0 * 64.0D;
		return distance < d0 * d0;
	}

	protected void onImpact(RayTraceResult mop) {
		if (mop.entityHit != null) {
			mop.entityHit.attackEntityFrom(DamageSource.causeThrownDamage(this, this.getThrower()), 9);
			
			if(mop.entityHit instanceof EntityLivingBase)
				((EntityLivingBase)mop.entityHit).addPotionEffect(new PotionEffect(SpeedsterHeroesProxy.speedShock, 200));
		}

		this.world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, posX, posY, posZ, 0.0D, 0.0D, 0.0D);
		this.world.spawnParticle(EnumParticleTypes.FLAME, posX, posY, posZ, 0.0D, 0.0D, 0.0D);

		this.setDead();

	}

	@Override
	public void readFromNBT(NBTTagCompound tagCompund) {
		super.readFromNBT(tagCompund);
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound tagCompund) {
		return super.writeToNBT(tagCompund);
	}

	protected float getGravityVelocity() {
		return 0.00001F;
	}

	@Override
	public void writeSpawnData(ByteBuf buffer) {
		buffer.writeInt(SpeedsterType.getSpeedsterTypeId(this.type));
		buffer.writeInt(progress);
	}

	@Override
	public void readSpawnData(ByteBuf additionalData) {
		this.type = SpeedsterType.getSpeedsterTypeFromId(additionalData.readInt());
		this.progress = additionalData.readInt();
	}

	@Override
	protected void entityInit() {

	}

}
