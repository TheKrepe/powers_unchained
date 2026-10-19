package lucraft.mods.heroes.speedsterheroes.abilities;

import lucraft.mods.heroes.speedsterheroes.SpeedsterHeroes;
import lucraft.mods.heroes.speedsterheroes.client.render.SHRenderer;
import lucraft.mods.heroes.speedsterheroes.superpower.SpeedforcePlayerHandler;
import lucraft.mods.heroes.speedsterheroes.util.SHAchievements;
import lucraft.mods.lucraftcore.abilities.AbilityHeld;
import lucraft.mods.lucraftcore.superpower.Superpower;
import lucraft.mods.lucraftcore.superpower.SuperpowerHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class AbilityPhasing extends AbilityHeld {

	public AbilityPhasing(EntityPlayer player) {
		super(player);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void drawIcon(Minecraft mc, Gui gui, int x, int y) {
		SHRenderer.drawIcon(mc, gui, x, y, 0, 2);
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
	public void updateTick() {
		if (!player.world.isRemote) {
			player.addStat(SHAchievements.phasing);
			if (!player.world.isBlockFullCube(new BlockPos(player.posX, player.posY - 0.1F, player.posZ))) {
				setEnabled(false);
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
	
	@Override
	public void onHurt(LivingHurtEvent e) {
		if(isUnlocked() && isEnabled()) {
			e.setAmount(e.getAmount() * 3);
			this.setCooldown(getMaxCooldown());
		}
	}
	
}
