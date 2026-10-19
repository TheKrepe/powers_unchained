package lucraft.mods.heroes.speedsterheroes.abilities;

import lucraft.mods.heroes.speedsterheroes.SpeedsterHeroes;
import lucraft.mods.heroes.speedsterheroes.client.render.SHRenderer;
import lucraft.mods.heroes.speedsterheroes.proxies.SpeedsterHeroesProxy;
import lucraft.mods.heroes.speedsterheroes.superpower.SpeedforcePlayerHandler;
import lucraft.mods.lucraftcore.abilities.AbilityAction;
import lucraft.mods.lucraftcore.superpower.Superpower;
import lucraft.mods.lucraftcore.superpower.SuperpowerHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class AbilitySpeedforceJump extends AbilityAction {

	public AbilitySpeedforceJump(EntityPlayer player) {
		super(player);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void drawIcon(Minecraft mc, Gui gui, int x, int y) {
		SHRenderer.drawIcon(mc, gui, x, y, 0, 9);
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
	public void action() {
		AxisAlignedBB a = new AxisAlignedBB(new BlockPos(player.getPosition().getX() - 2, player.getPosition().getY(), player.getPosition().getZ() - 2), new BlockPos(player.getPosition().getX() + 3, player.getPosition().getY() + 1, player.getPosition().getZ() + 3));
		for (EntityLivingBase entity : player.world.getEntitiesWithinAABB(EntityLivingBase.class, a)) {
			if (entity != player) {
				entity.addPotionEffect(new PotionEffect(SpeedsterHeroesProxy.speedShock, 200));
				player.world.newExplosion(entity, entity.posX, entity.posY, entity.posZ, 0.1F, false, true);
			}

			int amount = 10;
			for (int x = 0; x < amount; x++) {
				for (int z = 0; z < amount; z++) {
					float x_ = (float) (((float) x / (float) amount * 6) - 3 + player.world.rand.nextDouble() / 2D);
					float z_ = (float) (((float) z / (float) amount * 6) - 3 + player.world.rand.nextDouble() / 2D);
					player.world.spawnParticle(EnumParticleTypes.SMOKE_LARGE, player.posX + x_, player.posY, player.posZ + z_, 0, 0, 0);
				}
			}
		}
		
		this.setCooldown(getMaxCooldown());
	}
	
	@Override
	public boolean hasCooldown() {
		return true;
	}
	
	@Override
	public int getMaxCooldown() {
		return 20 * 10;
	}

	@Override
	public Superpower getDependentSuperpower() {
		return SpeedsterHeroes.speedforce;
	}
	
}
