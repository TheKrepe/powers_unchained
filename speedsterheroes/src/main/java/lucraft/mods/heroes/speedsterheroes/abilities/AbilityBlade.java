package lucraft.mods.heroes.speedsterheroes.abilities;

import lucraft.mods.heroes.speedsterheroes.client.sounds.SHSoundEvents;
import lucraft.mods.heroes.speedsterheroes.items.SHItems;
import lucraft.mods.heroes.speedsterheroes.util.SHAchievements;
import lucraft.mods.lucraftcore.abilities.AbilityToggle;
import lucraft.mods.lucraftcore.util.LucraftCoreUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.SoundCategory;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class AbilityBlade extends AbilityToggle {

	public AbilityBlade(EntityPlayer player) {
		super(player);
	}

	@Override
	public void updateTick() {
		
	}
	
	@Override
	public void action() {
		super.action();
		
		player.addStat(SHAchievements.savitarBlade);
		
		if(isEnabled())
			LucraftCoreUtil.playSoundToAll(player.world, player.posX, player.posY, player.posZ, 40, SHSoundEvents.savitarBladeDraw, SoundCategory.PLAYERS);
		else
			LucraftCoreUtil.playSoundToAll(player.world, player.posX, player.posY, player.posZ, 40, SHSoundEvents.savitarBladeBack, SoundCategory.PLAYERS);
	}
	
	@Override
	public void onHurt(LivingHurtEvent e) {
		if(isEnabled() && player.getHeldItemMainhand().isEmpty()) {
			e.setAmount(e.getAmount() + 3);
		}
	}

	private static final ItemStack iconBlade = new ItemStack(SHItems.savitarBlade);
	
	@Override
	@SideOnly(Side.CLIENT)
	public void drawIcon(Minecraft mc, Gui gui, int x, int y) {
		mc.getRenderItem().renderItemIntoGUI(iconBlade, x, y);
	}
	
}
