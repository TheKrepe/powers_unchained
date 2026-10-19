package lucraft.mods.heroes.speedsterheroes.abilities;

import lucraft.mods.heroes.speedsterheroes.SpeedsterHeroes;
import lucraft.mods.heroes.speedsterheroes.client.render.SHRenderer;
import lucraft.mods.heroes.speedsterheroes.entity.EntityDimensionBreach;
import lucraft.mods.heroes.speedsterheroes.entity.EntityDimensionBreach.BreachActionTypes;
import lucraft.mods.heroes.speedsterheroes.entity.EntityTimeRemnant;
import lucraft.mods.heroes.speedsterheroes.speedstertypes.SpeedsterType;
import lucraft.mods.heroes.speedsterheroes.superpower.SpeedforcePlayerHandler;
import lucraft.mods.heroes.speedsterheroes.util.SHAchievements;
import lucraft.mods.heroes.speedsterheroes.util.SpeedsterAttributeModifiers;
import lucraft.mods.heroes.speedsterheroes.util.SpeedsterHeroesUtil;
import lucraft.mods.lucraftcore.abilities.AbilityAction;
import lucraft.mods.lucraftcore.attributes.LCAttributes;
import lucraft.mods.lucraftcore.superpower.Superpower;
import lucraft.mods.lucraftcore.superpower.SuperpowerHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class AbilityTimeRemnant extends AbilityAction {

	public AbilityTimeRemnant(EntityPlayer player) {
		super(player);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void drawIcon(Minecraft mc, Gui gui, int x, int y) {
		SHRenderer.drawIcon(mc, gui, x, y, 0, 7);
	}
	
	@Override
	public boolean checkConditions() {
		SpeedforcePlayerHandler data = SuperpowerHandler.getSpecificSuperpowerPlayerHandler(player, SpeedforcePlayerHandler.class);
		return data != null && data.isInSpeed && data.speedLevel >= 8;
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
		SpeedforcePlayerHandler data = SuperpowerHandler.getSpecificSuperpowerPlayerHandler(player, SpeedforcePlayerHandler.class);
		EntityTimeRemnant remnant = new EntityTimeRemnant(player.world, player);
		remnant.setOwnerId(player.getUniqueID());
		remnant.setTamed(true);
		BlockPos pos = SpeedsterHeroesUtil.getRandomPositionInNear(player.world, new BlockPos(player), 10);
		EntityDimensionBreach breach = new EntityDimensionBreach(player.world, pos.getX(), pos.getY(), pos.getZ(), BreachActionTypes.SPAWN_ENTITY);
		breach.entityToSpawn = remnant;
		
		if(SpeedsterHeroesUtil.getSpeedsterType(player) == SpeedsterType.savitar)
			remnant.getEntityAttribute(LCAttributes.SIZE).applyModifier(SpeedsterAttributeModifiers.savitarScale);
		
		if(!player.world.isRemote)
			player.world.spawnEntity(breach);
		this.setCooldown(getMaxCooldown());
		player.addStat(SHAchievements.timeRemnant);
		data.addXP(1000);
		
		for(WorldServer worlds : DimensionManager.getWorlds()) {
			for(EntityPlayer players : worlds.playerEntities) {
				if(SpeedsterHeroesUtil.getSpeedsterType(players) == SpeedsterType.blackFlash) {
					players.sendMessage(new TextComponentTranslation("speedsterheroes.info.createdtimeremnant", new Object[] {player.getDisplayName()}));
				}
			}
		}
	}
	
	@Override
	public boolean hasCooldown() {
		return true;
	}
	
	@Override
	public int getMaxCooldown() {
		return 20 * 20;
	}

}
