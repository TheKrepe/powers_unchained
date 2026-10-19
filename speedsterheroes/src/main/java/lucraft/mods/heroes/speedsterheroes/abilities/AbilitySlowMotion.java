package lucraft.mods.heroes.speedsterheroes.abilities;

import lucraft.mods.heroes.speedsterheroes.SpeedsterHeroes;
import lucraft.mods.heroes.speedsterheroes.client.render.SHRenderer;
import lucraft.mods.heroes.speedsterheroes.network.MessageSendInfoToClient;
import lucraft.mods.heroes.speedsterheroes.network.MessageSendInfoToClient.InfoType;
import lucraft.mods.heroes.speedsterheroes.network.SHPacketDispatcher;
import lucraft.mods.heroes.speedsterheroes.superpower.SpeedforcePlayerHandler;
import lucraft.mods.lucraftcore.abilities.AbilityToggle;
import lucraft.mods.lucraftcore.superpower.Superpower;
import lucraft.mods.lucraftcore.superpower.SuperpowerHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class AbilitySlowMotion extends AbilityToggle {

	public AbilitySlowMotion(EntityPlayer player) {
		super(player);
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
	public void updateTick() {
		SpeedforcePlayerHandler data = SuperpowerHandler.getSpecificSuperpowerPlayerHandler(player, SpeedforcePlayerHandler.class);
		
		if(data == null || !data.isInSpeed) {
			if(player instanceof EntityPlayerMP)
				SHPacketDispatcher.sendTo(new MessageSendInfoToClient(InfoType.SLOWMO, 0), (EntityPlayerMP) player);
			this.setEnabled(false);
		}
	}
	
	@Override
	public void action() {
		super.action();
		
		if(isEnabled()) {
			if(player instanceof EntityPlayerMP) {
				SHPacketDispatcher.sendTo(new MessageSendInfoToClient(InfoType.SLOWMO, 1), (EntityPlayerMP) player);
			}
		} else
			if(player instanceof EntityPlayerMP)
				SHPacketDispatcher.sendTo(new MessageSendInfoToClient(InfoType.SLOWMO, 0), (EntityPlayerMP) player);
	}
	
	@Override
	public void firstTick() {
		super.firstTick();
//		this.setEnabled(false);
//		LucraftTickrateHooks.updateClientTickrate(LucraftTickrateHooks.DEFAULT_TICKS_PER_SECOND);
	}
	
	@Override
	public void lastTick() {
		super.lastTick();
		this.setEnabled(false);
		if(player instanceof EntityPlayerMP)
			SHPacketDispatcher.sendTo(new MessageSendInfoToClient(InfoType.SLOWMO, 0), (EntityPlayerMP) player);
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void drawIcon(Minecraft mc, Gui gui, int x, int y) {
		SHRenderer.drawIcon(mc, gui, x, y, 0, 11);
	}

	@Override
	public Superpower getDependentSuperpower() {
		return SpeedsterHeroes.speedforce;
	}
	
}
