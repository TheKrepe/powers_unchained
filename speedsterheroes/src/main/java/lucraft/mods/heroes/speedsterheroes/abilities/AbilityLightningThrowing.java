package lucraft.mods.heroes.speedsterheroes.abilities;

import lucraft.mods.heroes.speedsterheroes.SpeedsterHeroes;
import lucraft.mods.heroes.speedsterheroes.client.render.SHRenderer;
import lucraft.mods.heroes.speedsterheroes.entity.EntityLightning;
import lucraft.mods.heroes.speedsterheroes.speedstertypes.SpeedsterType;
import lucraft.mods.heroes.speedsterheroes.superpower.SpeedforcePlayerHandler;
import lucraft.mods.heroes.speedsterheroes.util.SpeedsterHeroesUtil;
import lucraft.mods.lucraftcore.abilities.AbilityAction;
import lucraft.mods.lucraftcore.superpower.Superpower;
import lucraft.mods.lucraftcore.superpower.SuperpowerHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class AbilityLightningThrowing extends AbilityAction {

	public AbilityLightningThrowing(EntityPlayer player) {
		super(player);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void drawIcon(Minecraft mc, Gui gui, int x, int y) {
		SHRenderer.drawIcon(mc, gui, x, y, 0, 3);
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
		
	}
	
	@Override
	public boolean hasCooldown() {
		return true;
	}
	
	@Override
	public int getMaxCooldown() {
		return 5 * 20;
	}
	
	public void throwLightning() {
		SpeedforcePlayerHandler data = SuperpowerHandler.getSpecificSuperpowerPlayerHandler(player, SpeedforcePlayerHandler.class);
		boolean isMoving = SpeedsterHeroesUtil.isMoving(player);
		SpeedsterType type = SpeedsterHeroesUtil.getSpeedsterType(player);
		
		if (!player.world.isRemote && isMoving && !player.onGround && player.getHeldItemMainhand().isEmpty() && this.getCooldown() == 0) {
			Vec3d v1 = new Vec3d(-10.6F, 0, 0);
			Vec3d v2 = v1.rotateYaw(-player.rotationYaw * (float) Math.PI / 180F);
			this.setCooldown(getMaxCooldown());
			data.addXP(1000);

			for (int i = 0; i < 3; i++)
				player.world.spawnEntity(new EntityLightning(player.world, player, type, player.posX + v2.xCoord, player.posY + (player.height / 1.8F) * 0.8F, player.posZ + v2.zCoord));
		}
	}

}
