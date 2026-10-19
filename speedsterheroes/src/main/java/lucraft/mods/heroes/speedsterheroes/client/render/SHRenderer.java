package lucraft.mods.heroes.speedsterheroes.client.render;

import java.util.HashMap;
import java.util.LinkedList;

import org.lwjgl.opengl.GL11;

import lucraft.mods.heroes.speedsterheroes.SpeedsterHeroes;
import lucraft.mods.heroes.speedsterheroes.abilities.AbilityBlade;
import lucraft.mods.heroes.speedsterheroes.client.models.ModelTachyonDevice;
import lucraft.mods.heroes.speedsterheroes.client.models.ModelTachyonPrototype;
import lucraft.mods.heroes.speedsterheroes.client.render.speedtrail.SpeedTrailRenderer;
import lucraft.mods.heroes.speedsterheroes.config.SHConfig;
import lucraft.mods.heroes.speedsterheroes.entity.EntityBlackFlash;
import lucraft.mods.heroes.speedsterheroes.entity.EntityDimensionBreach;
import lucraft.mods.heroes.speedsterheroes.entity.EntityDimensionBreach.BreachActionTypes;
import lucraft.mods.heroes.speedsterheroes.entity.EntityParticleAcceleratorSit;
import lucraft.mods.heroes.speedsterheroes.entity.EntityRingDummy;
import lucraft.mods.heroes.speedsterheroes.entity.EntitySpeedMirage;
import lucraft.mods.heroes.speedsterheroes.entity.EntityTimeRemnant;
import lucraft.mods.heroes.speedsterheroes.entity.EntityTimeWraith;
import lucraft.mods.heroes.speedsterheroes.items.SHItems;
import lucraft.mods.heroes.speedsterheroes.proxies.SpeedsterHeroesProxy;
import lucraft.mods.heroes.speedsterheroes.speedstertypes.SpeedsterType;
import lucraft.mods.heroes.speedsterheroes.superpower.SpeedforcePlayerHandler;
import lucraft.mods.heroes.speedsterheroes.trailtypes.TrailType;
import lucraft.mods.heroes.speedsterheroes.util.SpeedsterHeroesUtil;
import lucraft.mods.lucraftcore.abilities.Ability;
import lucraft.mods.lucraftcore.entity.EntityMountableBlock;
import lucraft.mods.lucraftcore.events.RenderModelEvent;
import lucraft.mods.lucraftcore.events.RenderOnPlayerHandEvent;
import lucraft.mods.lucraftcore.superpower.SuperpowerHandler;
import lucraft.mods.lucraftcore.util.IFakePlayerEntity;
import lucraft.mods.lucraftcore.util.IOpenableHelmet;
import lucraft.mods.lucraftcore.util.LucraftCoreClientUtil;
import net.minecraft.block.BlockLiquid;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelPlayer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHandSide;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.event.entity.EntityEvent.EntityConstructing;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerChangedDimensionEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedOutEvent;

public class SHRenderer {

	public static final ResourceLocation ICON_TEX = new ResourceLocation(SpeedsterHeroes.MODID, "textures/gui/icons.png");
	
	public static final ModelTachyonPrototype TACHYON_PROTOTYPE_MODEL = new ModelTachyonPrototype();
	public static final ResourceLocation TACHYON_PROTOTYPE_TEX = new ResourceLocation(SpeedsterHeroes.MODID, "textures/models/tachyonPrototype.png");
	
	public static final ModelTachyonDevice TACHYON_DEVICE_MODEL = new ModelTachyonDevice();
	public static final ResourceLocation TACHYON_DEVICE_TEX = new ResourceLocation(SpeedsterHeroes.MODID, "textures/models/tachyonDevice.png");
	
	public static void drawIcon(Minecraft mc, Gui gui, int x, int y, int row, int column) {
		mc.renderEngine.bindTexture(ICON_TEX);
		gui.drawTexturedModalRect(x, y, column * 16, row * 16, 16, 16);
	}

	public static void drawIcon(Minecraft mc, int x, int y, int row, int column) {
		drawIcon(mc, mc.ingameGUI, x, y, row, column);
	}

	public static HashMap<EntityLivingBase, Float> traveledBlocksMap = new HashMap<EntityLivingBase, Float>();
	public static HashMap<EntityLivingBase, Vec3d> lastPosMap = new HashMap<EntityLivingBase, Vec3d>();
	public static HashMap<EntityLivingBase, LinkedList<EntitySpeedMirage>> trailDataMap = new HashMap<EntityLivingBase, LinkedList<EntitySpeedMirage>>();

	@SubscribeEvent
	public void onLeave(PlayerLoggedOutEvent e) {
		if (canHaveTrail(e.player)) {
			traveledBlocksMap.remove(e.player);
			lastPosMap.remove(e.player);
			trailDataMap.remove(e.player);
		}
	}

	@SubscribeEvent
	public void onLogin(PlayerLoggedInEvent e) {
		if (canHaveTrail(e.player)) {
			traveledBlocksMap.put(e.player, 0F);
			lastPosMap.put(e.player, e.player.getPositionVector());
			trailDataMap.put(e.player, new LinkedList<EntitySpeedMirage>());
		}

	}

	@SubscribeEvent
	public void onLogin(EntityConstructing e) {
		if (e.getEntity() instanceof EntityTimeRemnant) {
			traveledBlocksMap.put((EntityLivingBase) e.getEntity(), 0F);
			lastPosMap.put((EntityLivingBase) e.getEntity(), e.getEntity().getPositionVector());
			trailDataMap.put((EntityLivingBase) e.getEntity(), new LinkedList<EntitySpeedMirage>());
		}

	}

	@SubscribeEvent
	public void onDimChange(PlayerChangedDimensionEvent e) {
		for (EntitySpeedMirage esm : trailDataMap.get(e.player)) {
			esm.setDead();
		}
		trailDataMap.put(e.player, new LinkedList<EntitySpeedMirage>());
	}

	@SubscribeEvent
	public void playerTick(LivingUpdateEvent e) {
		if (SHConfig.trailRender && canHaveTrail(e.getEntityLiving()) && e.getEntityLiving().world.isRemote) {

			if (e.getEntity() instanceof EntityPlayer && !canSeePlayer((EntityPlayer) e.getEntity(), Minecraft.getMinecraft().player)) {
				return;
			}

			if (isInSpeed(e.getEntityLiving())) {
				if (isPlayerMovingClientSide(e.getEntityLiving())) {
					EntityLivingBase player = e.getEntityLiving();

					if (!traveledBlocksMap.containsKey(player))
						traveledBlocksMap.put(player, 0F);
					if (!lastPosMap.containsKey(player))
						lastPosMap.put(player, player.getPositionVector());
					if (!trailDataMap.containsKey(player))
						trailDataMap.put(player, new LinkedList<EntitySpeedMirage>());

					Vec3d lastPos = lastPosMap.get(player);
					float distancedWalked = traveledBlocksMap.get(player) + (float) player.getDistance(lastPos.xCoord, lastPos.yCoord, lastPos.zCoord);

					if (distancedWalked >= player.width + (player.width / 10)) {
						EntitySpeedMirage esm = new EntitySpeedMirage(player.world, player, player.getPositionVector(), false);
						player.world.spawnEntity(esm);
						addSpeedMirageToList(player, esm);
						distancedWalked = 0F;
					}

					traveledBlocksMap.put(player, distancedWalked);
				}

				lastPosMap.put(e.getEntityLiving(), e.getEntityLiving().getPositionVector());
			}
		}
	}

	public static boolean isPlayerMovingClientSide(EntityLivingBase entity) {
		return areVec3dEqual(entity.getPositionVector(), lastPosMap.get(entity));
	}

	public static boolean areVec3dEqual(Vec3d vec1, Vec3d vec2) {
		if (vec1 == null || vec2 == null)
			return false;
		return vec1.xCoord != vec2.xCoord || vec1.yCoord != vec2.yCoord || vec1.zCoord != vec2.zCoord;
	}

	public void addSpeedMirageToList(EntityLivingBase player, EntitySpeedMirage e) {
		LinkedList<EntitySpeedMirage> list = trailDataMap.get(player);
		if (list.size() >= 10) {
			list.remove(0);
			list.add(e);
		} else {
			list.add(e);
		}
		trailDataMap.put(player, list);
	}

	public static LinkedList<EntitySpeedMirage> getSpeedMiragesFromPlayer(EntityLivingBase player) {
		if (trailDataMap.get(player) == null)
			trailDataMap.put(player, new LinkedList<EntitySpeedMirage>());
		return trailDataMap.get(player);
	}

	// ------- Render Stuff -------------------------------------

	@SubscribeEvent
	public void renderModel(RenderModelEvent e) {
		if (e.getEntityLiving() instanceof EntityPlayer) {
			EntityPlayer player = (EntityPlayer) e.getEntity();

			if (player.getRidingEntity() != null && player.getRidingEntity() instanceof EntityParticleAcceleratorSit && ((EntityParticleAcceleratorSit) player.getRidingEntity()).info.equalsIgnoreCase("particleAccelerator")) {
				EntityParticleAcceleratorSit sit = (EntityParticleAcceleratorSit) player.getRidingEntity();
				float f = 0;

				if (sit.facing == EnumFacing.EAST)
					f = 90F;
				else if (sit.facing == EnumFacing.NORTH)
					f = 180F;
				else if (sit.facing == EnumFacing.WEST)
					f = -90F;

				GlStateManager.rotate(-player.renderYawOffset - f, 0, 1, 0);
			}
		}

		if (SpeedsterHeroesUtil.isEntityAvailableForSpeedforce(e.getEntityLiving()) && SuperpowerHandler.getSpecificSuperpowerPlayerHandler((EntityPlayer) e.getEntityLiving(), SpeedforcePlayerHandler.class) != null && SuperpowerHandler.getSpecificSuperpowerPlayerHandler((EntityPlayer) e.getEntityLiving(), SpeedforcePlayerHandler.class).isWallRunning) {
			GlStateManager.translate(0, -e.getEntity().height, 0);
			GlStateManager.rotate(-90, 1, 0, 0);
			// GlStateManager.rotate(-26.1F, 0, 0, 1);
			// GlStateManager.rotate(-100 - e.entityLiving.rotationYaw / 10F, 0,
			// 0, 1);
			// System.out.println("" + e.entityLiving.rotationYaw);
		}

		else if (e.getEntity() instanceof EntityTimeWraith) {
			EntityTimeWraith entity = (EntityTimeWraith) e.getEntityLiving();
			double d = Math.min(Math.sqrt((entity.prevPosX - entity.posX) * (entity.prevPosX - entity.posX) + (entity.prevPosZ - entity.posZ) * (entity.prevPosZ - entity.posZ)), 1.0D) * (entity.moveForward == 0.0F ? 1.0F : entity.moveForward);
			GL11.glRotated(80.0D * d, 1.0D, 0.0D, 0.0D);
			GlStateManager.translate(0, 0, 30 * d * 4 / 100);
			GlStateManager.translate(0, Math.cos((entity.ticksExisted + LucraftCoreClientUtil.renderTick) * 0.1F) / 10F, 0);

			if (entity.getDespawnTimer() > 0) {
				GL11.glBlendFunc(770, 771);
				GL11.glAlphaFunc(516, 0.003921569F);

				GlStateManager.color(1, 1, 1, 1F - (entity.getDespawnTimer() + LucraftCoreClientUtil.renderTick) / 100F);
			}
		}

		else if (e.getEntity() instanceof EntityBlackFlash) {
			EntityBlackFlash entity = (EntityBlackFlash) e.getEntityLiving();

			if (entity.getDespawnTimer() > 0) {
				GL11.glBlendFunc(770, 771);
				GL11.glAlphaFunc(516, 0.003921569F);

				GlStateManager.color(1, 1, 1, 1F - (entity.getDespawnTimer() + LucraftCoreClientUtil.renderTick) / 100F);
			}
		}

		else if (e.getEntity() instanceof EntityRingDummy) {
			EntityRingDummy entity = (EntityRingDummy) e.getEntity();
			double d = Math.min(Math.sqrt((entity.prevPosX - entity.posX) * (entity.prevPosX - entity.posX) + (entity.prevPosZ - entity.posZ) * (entity.prevPosZ - entity.posZ)), 1.0D) * (entity.moveForward == 0.0F ? 1.0F : entity.moveForward);
			float scale = MathHelper.clamp((entity.ticksExisted + LucraftCoreClientUtil.renderTick) / 20F, 0, 0.9F);
			GL11.glRotated(80.0D * d, 1.0D, 0.0D, 0.0D);
			GlStateManager.scale(scale, scale, scale);
			GlStateManager.translate(0, -0.2F + Math.cos((entity.ticksExisted + LucraftCoreClientUtil.renderTick) * 0.1F) / 10F, 0);
		}
	}

	@SubscribeEvent
	public void setupModel(RenderModelEvent.SetRotationAngels e) {
		if (SpeedsterHeroesUtil.isEntityAvailableForSpeedforce(e.getEntity())) {
			EntityPlayer player = (EntityPlayer) e.getEntity();
			SpeedforcePlayerHandler data = SuperpowerHandler.getSpecificSuperpowerPlayerHandler(player, SpeedforcePlayerHandler.class);

			if (data != null && data.isInSpeed && (e.getEntity().onGround || player.world.getBlockState(player.getPosition().add(0, -1, 0)).getBlock() instanceof BlockLiquid)) {
				e.limbSwing *= data.speedLevel;
			}

			if (data != null && data.isWallRunning) {
				e.limbSwing = (float) player.posY;
				e.limbSwingAmount = MathHelper.cos((float) player.posY * data.speedLevel * 4F);
			}

			if (player.getRidingEntity() != null && player.getRidingEntity() instanceof EntityMountableBlock && ((EntityMountableBlock) player.getRidingEntity()).info.equalsIgnoreCase("particleAccelerator")) {
				e.setCanceled(true);

				ModelBiped model = e.model;

				model.bipedLeftArm.rotateAngleZ = -2.3F;
				model.bipedRightArm.rotateAngleZ = 2.3F;

				if (model instanceof ModelPlayer) {
					ModelPlayer playerModel = (ModelPlayer) model;
					playerModel.bipedLeftArmwear.rotateAngleZ = -2.3F;
					playerModel.bipedRightArmwear.rotateAngleZ = 2.3F;
				}
			}
		}

		else if (e.getEntity() instanceof EntityTimeWraith) {
			e.limbSwing = 0;
			e.limbSwingAmount = 0;
		}

		else if (e.getEntity() instanceof EntityRingDummy) {
			e.setCanceled(true);

			EntityRingDummy entity = (EntityRingDummy) e.getEntity();
			float d = (float) (Math.min(Math.sqrt((entity.prevPosX - entity.posX) * (entity.prevPosX - entity.posX) + (entity.prevPosZ - entity.posZ) * (entity.prevPosZ - entity.posZ)), 1.0D) * (entity.moveForward == 0.0F ? 1.0F : entity.moveForward));

			e.model.bipedRightArm.rotateAngleX = 0.7F * d;
			e.model.bipedRightArm.rotateAngleZ = 0.2F * d;

			e.model.bipedLeftArm.rotateAngleX = 0.7F * d;
			e.model.bipedLeftArm.rotateAngleZ = -0.2F * d;

			e.model.bipedRightLeg.rotateAngleX = 0.7F * d;
			e.model.bipedRightLeg.rotateAngleZ = 0.2F * d;

			e.model.bipedLeftLeg.rotateAngleX = 0.7F * d;
			e.model.bipedLeftLeg.rotateAngleZ = -0.2F * d;
		}
	}

	@SubscribeEvent
	public void renderEntityPre(RenderLivingEvent.Pre<EntityLivingBase> e) {
		if (e.getEntity() instanceof EntityPlayer) {
			if (!canSeePlayer((EntityPlayer) e.getEntity(), Minecraft.getMinecraft().player)) {
				e.setCanceled(true);
			}
			
			if (e.getEntity().getRidingEntity() instanceof EntityDimensionBreach) {
				EntityDimensionBreach breach = (EntityDimensionBreach) e.getEntity().getRidingEntity();

				if (breach.type == BreachActionTypes.PORTAL)
					e.setCanceled(true);
			}
		}

		if (e.getEntity() instanceof EntitySpeedMirage) {
			EntitySpeedMirage entity = (EntitySpeedMirage) e.getEntity();
			SpeedTrailRenderer renderer = entity.trail.getSpeedTrailRenderer();

			if (!renderer.shouldRenderSpeedMirage(entity, entity.trail)) {
				e.setCanceled(true);
				return;
			}

			renderer.preRenderSpeedMirage(entity, entity.trail);
		}

		if (e.getEntity().isPotionActive(SpeedsterHeroesProxy.speedShock)) {
			TrailType.renderer_lightnings.renderFlickering(e.getEntity(), TrailType.lightnings_orange);
		}
	}

	@SubscribeEvent
	public void renderEntityPost(RenderLivingEvent.Post<EntityLivingBase> e) {
		if (canHaveTrail(e.getEntity()) && isInSpeed(e.getEntity())) {
			EntityLivingBase player = e.getEntity();
			SpeedsterType type = SpeedsterHeroesUtil.getSpeedsterType(player);
			TrailType trail = null;
			if (type == null) {
				trail = (SpeedsterHeroesUtil.isVelocity9Active(player) ? TrailType.lightnings_orange : (player instanceof EntityPlayer ? (SuperpowerHandler.hasSuperpower((EntityPlayer) player) ? SuperpowerHandler.getSpecificSuperpowerPlayerHandler((EntityPlayer) player, SpeedforcePlayerHandler.class).customTrailType : TrailType.normal) : TrailType.normal));
			} else if (type.getTrailType() != null)
				trail = type.getTrailType();
			else
				trail = player instanceof EntityPlayer && SuperpowerHandler.hasSuperpower((EntityPlayer) player) ? SuperpowerHandler.getSpecificSuperpowerPlayerHandler((EntityPlayer) player, SpeedforcePlayerHandler.class).customTrailType : TrailType.normal;

			trail.getSpeedTrailRenderer().renderTrail(player, trail);

			if (SHConfig.flickerRender) {
				if (isPlayerMovingClientSide(player) && !(Minecraft.getMinecraft().gameSettings.thirdPersonView == 0 && player == Minecraft.getMinecraft().player))
					trail.getSpeedTrailRenderer().renderFlickering(player, trail);
				if (((type != null && type.doesFlicker()) || SpeedsterHeroesUtil.isVelocity9Active(player)) && !(Minecraft.getMinecraft().gameSettings.thirdPersonView == 0 && player == Minecraft.getMinecraft().player))
					trail.getSpeedTrailRenderer().renderFlickering(player, trail);
			}
		}
		
		// if(e.getEntity() == Minecraft.getMinecraft().thePlayer) {
		// Tessellator tes = Tessellator.getInstance();
		// VertexBuffer buf = tes.getBuffer();
		//
		// GlStateManager.pushMatrix();
		// GlStateManager.disableTexture2D();
		// GlStateManager.disableLighting();
		// GlStateManager.enableBlend();
		// GlStateManager.blendFunc(770, 1);
		// GL11.glLineWidth(2);
		// GlStateManager.color(0, 1, 0, 1);
		// GlStateManager.translate(-0.25F, 0.01F, -0.25F);
		//
		// GlStateManager.pushMatrix();
		// GlStateManager.translate(0.25F, 0, 0.25F);
		// GlStateManager.rotate(-e.getEntity().ticksExisted -
		// LucraftCoreClientUtil.renderTick, 0, 1, 0);
		// buf.begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION);
		// buf.pos(-0.25F, 0, -0.25F).endVertex();
		// buf.pos(0.25F, 0, -0.25F).endVertex();
		// buf.pos(0.25F, 0, 0.25F).endVertex();
		// buf.pos(-0.25F, 0, 0.25F).endVertex();
		// buf.pos(-0.25F, 0, -0.25F).endVertex();
		// tes.draw();
		// GlStateManager.popMatrix();
		//
		// GlStateManager.pushMatrix();
		// GlStateManager.translate(0.25F, 0, 0.25F);
		// GlStateManager.rotate(45F + e.getEntity().ticksExisted +
		// LucraftCoreClientUtil.renderTick, 0, 1, 0);
		// buf.begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION);
		// buf.pos(-0.25F, 0, -0.25F).endVertex();
		// buf.pos(0.25F, 0, -0.25F).endVertex();
		// buf.pos(0.25F, 0, 0.25F).endVertex();
		// buf.pos(-0.25F, 0, 0.25F).endVertex();
		// buf.pos(-0.25F, 0, -0.25F).endVertex();
		// tes.draw();
		// GlStateManager.popMatrix();
		//
		// GlStateManager.pushMatrix();
		// GlStateManager.translate(0.25F, 0, 0.25F);
		// buf.begin(GL11.GL_LINE_LOOP, DefaultVertexFormats.POSITION);
		// for(int i = 0; i <= 300; i++) {
		// double angle = 2F * Math.PI * i / 300;
		// double x = Math.cos(angle);
		// double y = Math.sin(angle);
		// float size = 2.85F;
		// buf.pos(x / size, 0, y / size).endVertex();
		// }
		// tes.draw();
		// GlStateManager.popMatrix();
		//
		// GlStateManager.pushMatrix();
		// GlStateManager.translate(0.25F, 0, 0.25F);
		// buf.begin(GL11.GL_LINE_LOOP, DefaultVertexFormats.POSITION);
		// for(int i = 0; i <= 300; i++) {
		// double angle = 2F * Math.PI * i / 300;
		// double x = Math.cos(angle);
		// double y = Math.sin(angle);
		// float size = 2.65F;
		// buf.pos(x / size, 0, y / size).endVertex();
		// }
		// tes.draw();
		// GlStateManager.popMatrix();
		//
		// GlStateManager.pushMatrix();
		// GlStateManager.translate(0.25F, 0, 0.25F);
		// buf.begin(GL11.GL_LINE_LOOP, DefaultVertexFormats.POSITION);
		// for(int i = 0; i <= 300; i++) {
		// double angle = 2F * Math.PI * i / 300;
		// double x = Math.cos(angle);
		// double y = Math.sin(angle);
		// float size = 2.15F;
		// buf.pos(x / size, 0, y / size).endVertex();
		// }
		// tes.draw();
		// GlStateManager.popMatrix();
		//
		// GlStateManager.pushMatrix();
		// GlStateManager.translate(0.25F, 0, 0.25F);
		// buf.begin(GL11.GL_LINE_LOOP, DefaultVertexFormats.POSITION);
		// for(int i = 0; i <= 300; i++) {
		// double angle = 2F * Math.PI * i / 300;
		// double x = Math.cos(angle);
		// double y = Math.sin(angle);
		// float size = 2.05F;
		// buf.pos(x / size, 0, y / size).endVertex();
		// }
		// tes.draw();
		// GlStateManager.popMatrix();
		//
		// GlStateManager.pushMatrix();
		// GlStateManager.translate(0.25F, 0, 0.25F);
		// GlStateManager.rotate(e.getEntity().ticksExisted +
		// LucraftCoreClientUtil.renderTick, 0, 1, 0);
		// buf.begin(GL11.GL_LINE_LOOP, DefaultVertexFormats.POSITION);
		// for(int i = 0; i <= 300; i++) {
		// double angle = 2F * Math.PI * i / 300;
		// float amount = 0.03F;
		// double x = Math.cos(angle);
		// double y = Math.sin(angle);
		//
		// if(x < 0)
		// x += i%5 == 0 ? amount : 0;
		// else
		// x -= i%5 == 0 ? amount : 0;
		//
		// if(y < 0)
		// y += i%5 == 0 ? amount : 0;
		// else
		// y -= i%5 == 0 ? amount : 0;
		//
		// float size = 2.36F;
		// buf.pos(x / size, 0, y / size).endVertex();
		// }
		// tes.draw();
		// GlStateManager.popMatrix();
		//
		// GlStateManager.pushMatrix();
		// GlStateManager.translate(0.25F, 0, 0.25F);
		// buf.begin(GL11.GL_LINE_LOOP, DefaultVertexFormats.POSITION);
		// for(int i = 0; i <= 300; i++) {
		// double angle = 2F * Math.PI * i / 300;
		// double x = Math.cos(angle);
		// double y = Math.sin(angle);
		// buf.pos(x / 4F, 0, y / 4F).endVertex();
		// }
		// tes.draw();
		// GlStateManager.popMatrix();
		//
		// GlStateManager.disableBlend();
		// GlStateManager.enableLighting();
		// GlStateManager.enableTexture2D();
		// GlStateManager.popMatrix();
		// }

	}

//	@SubscribeEvent
//	public void onRenderWorldLastEvent(RenderWorldLastEvent e) {
//		if (SuperpowerHandler.getSpecificSuperpowerPlayerHandler(Minecraft.getMinecraft().player, SpeedforcePlayerHandler.class) != null && SuperpowerHandler.getSpecificSuperpowerPlayerHandler(Minecraft.getMinecraft().player, SpeedforcePlayerHandler.class).inSpeedforcePortal) {
//			EntityPlayer player = Minecraft.getMinecraft().player;
//			float height = player.height * 1.7F;
//			float diff = height - player.height;
//			GlStateManager.pushMatrix();
//			GlStateManager.glTexParameteri(3553, 10242, 10497);
//			GlStateManager.glTexParameteri(3553, 10243, 10497);
//			GlStateManager.disableLighting();
//			GlStateManager.disableCull();
//			GlStateManager.disableBlend();
//			GlStateManager.depthMask(true);
//			GlStateManager.color(1, 1, 1);
//			GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
//			Minecraft.getMinecraft().renderEngine.bindTexture(TileEntityBeaconRenderer.TEXTURE_BEACON_BEAM);
//			Tessellator tessellator = Tessellator.getInstance();
//			VertexBuffer vertexbuffer = tessellator.getBuffer();
//
//			float yawOffset = player.prevRenderYawOffset + (player.renderYawOffset - player.prevRenderYawOffset) * e.getPartialTicks();
//			GlStateManager.rotate(yawOffset, 0, -1, 0);
//			GlStateManager.translate(0, 1, 0);
//			GlStateManager.rotate(player.ticksExisted + e.getPartialTicks(), 0, 0, 1);
//			GlStateManager.translate(-player.width / 2, -player.height / 2, 0);
//			GlStateManager.color(0.5F, 0.5F, 1F);
//			GlStateManager.translate(0, 0, -20 * height);
//
//			for (int i = 0; i < 40; i++) {
//				vertexbuffer.begin(7, DefaultVertexFormats.POSITION_TEX);
//
//				vertexbuffer.pos(-height / 2, -diff / 2, height / 2).tex(0, 0).endVertex();
//				vertexbuffer.pos(height / 2, -diff / 2, height / 2).tex(1, 0).endVertex();
//				vertexbuffer.pos(height / 2, -diff / 2, -height / 2).tex(1, 1).endVertex();
//				vertexbuffer.pos(-height / 2, -diff / 2, -height / 2).tex(0, 1).endVertex();
//
//				vertexbuffer.pos(-height / 2, player.height + diff / 2, height / 2).tex(0, 0).endVertex();
//				vertexbuffer.pos(height / 2, player.height + diff / 2, height / 2).tex(1, 0).endVertex();
//				vertexbuffer.pos(height / 2, player.height + diff / 2, -height / 2).tex(1, 1).endVertex();
//				vertexbuffer.pos(-height / 2, player.height + diff / 2, -height / 2).tex(0, 1).endVertex();
//
//				vertexbuffer.pos(-height / 2, player.height + diff / 2, -height / 2).tex(1, 0).endVertex();
//				vertexbuffer.pos(-height / 2, player.height + diff / 2, height / 2).tex(1, 1).endVertex();
//				vertexbuffer.pos(-height / 2, -diff / 2, height / 2).tex(0, 1).endVertex();
//				vertexbuffer.pos(-height / 2, -diff / 2, -height / 2).tex(0, 0).endVertex();
//
//				vertexbuffer.pos(height / 2, player.height + diff / 2, -height / 2).tex(1, 0).endVertex();
//				vertexbuffer.pos(height / 2, player.height + diff / 2, height / 2).tex(1, 1).endVertex();
//				vertexbuffer.pos(height / 2, -diff / 2, height / 2).tex(0, 1).endVertex();
//				vertexbuffer.pos(height / 2, -diff / 2, -height / 2).tex(0, 0).endVertex();
//
//				tessellator.draw();
//
//				GlStateManager.translate(0, 0, height);
//			}
//
//			GlStateManager.enableLighting();
//			GlStateManager.enableTexture2D();
//			GlStateManager.popMatrix();
//		}
//	}

	@SuppressWarnings("rawtypes")
	@SubscribeEvent
	public void renderNameTag(RenderLivingEvent.Specials.Pre e) {
		if (canHaveTrail(e.getEntity())) {
			EntityLivingBase player = e.getEntity();
			SpeedsterType type = SpeedsterHeroesUtil.getSpeedsterType(player);
			if (type != null && SpeedsterHeroesUtil.hasHelmetOn(player)) {
				boolean maskOpen = !player.getItemStackFromSlot(EntityEquipmentSlot.HEAD).isEmpty() && player.getItemStackFromSlot(EntityEquipmentSlot.HEAD).getItem() instanceof IOpenableHelmet && ((IOpenableHelmet)player.getItemStackFromSlot(EntityEquipmentSlot.HEAD).getItem()).isHelmetOpen(player, player.getItemStackFromSlot(EntityEquipmentSlot.HEAD));
				if (player.getItemStackFromSlot(EntityEquipmentSlot.HEAD).hasTagCompound() && type.shouldHideNameTag(player, maskOpen)) {
					e.setCanceled(true);
				}
			}
		}
	}
	
	@SubscribeEvent
	public void onRenderOnHand(RenderOnPlayerHandEvent e) {
		AbilityBlade blade = Ability.getAbilityFromClass(Ability.getCurrentPlayerAbilities(e.getEntityPlayer()), AbilityBlade.class);
		
		if(blade != null && blade.isUnlocked() && blade.isEnabled()) {
            GlStateManager.rotate(-90.0F, 1.0F, 0.0F, 0.0F);
            GlStateManager.rotate(180.0F, 0.0F, 1.0F, 0.0F);
            boolean flag = e.getSide() == EnumHandSide.LEFT;
            ItemCameraTransforms.TransformType transform = flag ? TransformType.THIRD_PERSON_LEFT_HAND : TransformType.THIRD_PERSON_RIGHT_HAND;
            GlStateManager.translate((float)(flag ? -1 : 1) / 16.0F, 0.125F, -0.625F);
            Minecraft.getMinecraft().getItemRenderer().renderItemSide(e.getEntityLiving(), new ItemStack(SHItems.savitarBlade), transform, flag);
		}
	}

	public static boolean canHaveTrail(Entity entity) {
		// if(entity != null &&
		// entity.getClass().toGenericString().contains("EntityCustomNpc")) {
		//// ICustomNpc<?> npc = (ICustomNpc<?>)entity;
		// return true;
		// }
		return entity != null && entity instanceof EntityLivingBase && (entity instanceof EntityPlayer || entity instanceof EntityTimeRemnant || entity instanceof EntityBlackFlash) && !(entity instanceof IFakePlayerEntity);
	}

	public static boolean isInSpeed(EntityLivingBase entity) {
		if (entity instanceof EntityPlayer) {
			return entity.isPotionActive(SpeedsterHeroesProxy.velocity9) ? true : (SuperpowerHandler.hasSuperpower((EntityPlayer) entity, SpeedsterHeroes.speedforce) && SuperpowerHandler.getSpecificSuperpowerPlayerHandler((EntityPlayer) entity, SpeedforcePlayerHandler.class).isInSpeed);
		} else if (entity instanceof EntityTimeRemnant || entity instanceof EntityBlackFlash) {
			return true;
		}
		// else
		// if(entity.getClass().toGenericString().contains("EntityCustomNpc")) {
		// return true;
		// }
		return false;
	}

	public static boolean canSeePlayer(EntityPlayer seenPlayer, EntityPlayer seeingPlayer) {
//		Superpower s1 = SuperpowerHandler.getSuperpower(seenPlayer);
//		Superpower s2 = SuperpowerHandler.getSuperpower(seeingPlayer);
//
//		if (s1 == SpeedsterHeroes.speedforce) {
//			SpeedforcePlayerHandler data = SuperpowerHandler.getSpecificSuperpowerPlayerHandler(seenPlayer, SpeedforcePlayerHandler.class);
//		}

		return true;
	}

//	@SubscribeEvent
//	public void fogRenderEvent(EntityViewRenderEvent.RenderFogEvent event) {
//		Entity entity = event.getEntity();
////		int amount = 8;
//		double radius = 10;
//		 GlStateManager.setFogStart((float) (radius / 2F));
//		 GlStateManager.setFogEnd((float) (radius + 2));
////		
////		if (event.getEntity().ticksExisted % 10 == 0) {
////			
////			double increment = (2 * Math.PI) / amount;
////			for (int i = 0; i < amount; i++) {
////				double angle = i * increment;
////				double x = radius * Math.cos(angle);
////				double z = radius * Math.sin(angle);
////
////				for(int height = -2; height < 5; height++)
////					entity.getEntityWorld().spawnParticle(EnumParticleTypes.EXPLOSION_HUGE, entity.posX + x, entity.posY + height * 2.5F, entity.posZ + z, 0, 0, 0);
////			}
////		}
//		TrailType trail = TrailType.lightnings_orange;
//		Random rand = new Random();
//		AxisAlignedBB aabb = new AxisAlignedBB(entity.posX - radius, entity.posY - radius, entity.posZ - radius, entity.posX + radius, entity.posY + radius, entity.posZ + radius);
//		BlockPos pos = entity.getPosition().add((rand.nextFloat()-0.5F) * radius, (rand.nextFloat()-0.5F) * radius, (rand.nextFloat()-0.5F) * radius);
//		aabb = new AxisAlignedBB(pos, pos.add(1, 1, 1));
//		trail.getSpeedTrailRenderer().renderFlickering(aabb, aabb, trail);
//	}
//
//	@SubscribeEvent
//	public void fogColorEvent(EntityViewRenderEvent.FogColors e) {
//		e.setBlue(0.2F);
//		e.setRed(0.2F);
//		e.setGreen(0.2F);
//	}
	
}
