package lucraft.mods.heroes.speedsterheroes.abilities;

import lucraft.mods.heroes.speedsterheroes.SpeedsterHeroes;
import lucraft.mods.heroes.speedsterheroes.client.gui.GuiIds;
import lucraft.mods.heroes.speedsterheroes.client.render.SHRenderer;
import lucraft.mods.heroes.speedsterheroes.entity.EntityDimensionBreach;
import lucraft.mods.heroes.speedsterheroes.entity.EntityDimensionBreach.BreachActionTypes;
import lucraft.mods.heroes.speedsterheroes.superpower.SpeedforcePlayerHandler;
import lucraft.mods.heroes.speedsterheroes.util.SHAchievements;
import lucraft.mods.heroes.speedsterheroes.util.TeleportDestination;
import lucraft.mods.lucraftcore.abilities.AbilityAction;
import lucraft.mods.lucraftcore.superpower.Superpower;
import lucraft.mods.lucraftcore.superpower.SuperpowerHandler;
import lucraft.mods.lucraftcore.util.LucraftCoreUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class AbilityDimensionBreach extends AbilityAction {

	public AbilityDimensionBreach(EntityPlayer player) {
		super(player);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void drawIcon(Minecraft mc, Gui gui, int x, int y) {
		SHRenderer.drawIcon(mc, gui, x, y, 0, 8);
	}
	
	@Override
	public boolean checkConditions() {
		SpeedforcePlayerHandler data = SuperpowerHandler.getSpecificSuperpowerPlayerHandler(player, SpeedforcePlayerHandler.class);
		return data != null && data.isInSpeed;
	}
	
	@Override
	public boolean showInAbilityBar() {
		return checkConditions();
	}
	
	@Override
	public Superpower getDependentSuperpower() {
		return SpeedsterHeroes.speedforce;
	}
	
	@Override
	public void action() {
		if(player.isSneaking() && player instanceof EntityPlayerMP) {
			LucraftCoreUtil.sendSuperpowerUpdatePacket(player);
			player.openGui(SpeedsterHeroes.instance, GuiIds.dimensionBreach, player.world, (int)player.posX, (int)player.posY, (int)player.posZ);
		} else {
			SpeedforcePlayerHandler data = SuperpowerHandler.getSpecificSuperpowerPlayerHandler(player, SpeedforcePlayerHandler.class);
			if(data.chosenWaypointIndex > -1 && data.chosenWaypointIndex < data.waypoints.size()) {
				TeleportDestination td = data.waypoints.get(data.chosenWaypointIndex);
				double x = player.getLookVec().xCoord * 30D;
				double z = player.getLookVec().zCoord * 30D;
				
				BlockPos pos = new BlockPos(player.posX + player.width / 2 + x, player.posY, player.posZ + player.width / 2 + z);
				
				if(player.world.getBlockState(pos).isFullBlock())
					return;
				
				EntityDimensionBreach breach = new EntityDimensionBreach(player.world, pos.getX(), pos.getY(), pos.getZ(), BreachActionTypes.PORTAL);
				breach.teleportDestination = td;
				if(!player.world.isRemote)
					player.world.spawnEntity(breach);
				player.addStat(SHAchievements.dimensionBreach);
				data.addXP(1000);
			}
		}
	}
	
	@Override
	public boolean hasCooldown() {
		return true;
	}
	
	@Override
	public int getMaxCooldown() {
		return 60*20;
	}

}
