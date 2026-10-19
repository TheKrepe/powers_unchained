package lucraft.mods.heroes.speedsterheroes.superpower;

import java.util.List;

import lucraft.mods.heroes.speedsterheroes.SpeedsterHeroes;
import lucraft.mods.heroes.speedsterheroes.abilities.AbilityAccelerate;
import lucraft.mods.heroes.speedsterheroes.abilities.AbilityDecelerate;
import lucraft.mods.heroes.speedsterheroes.abilities.AbilitySlowMotion;
import lucraft.mods.heroes.speedsterheroes.abilities.AbilitySuperSpeed;
import lucraft.mods.heroes.speedsterheroes.abilities.AbilityWallRunning;
import lucraft.mods.heroes.speedsterheroes.abilities.AbilityWaterRunning;
import lucraft.mods.heroes.speedsterheroes.client.gui.GuiTrailCustomizer;
import lucraft.mods.heroes.speedsterheroes.trailtypes.TrailType.EnumTrailType;
import lucraft.mods.lucraftcore.abilities.Ability;
import lucraft.mods.lucraftcore.abilities.AbilityHealing;
import lucraft.mods.lucraftcore.client.gui.GuiCustomizer;
import lucraft.mods.lucraftcore.superpower.ISuperpowerPlayerRenderer;
import lucraft.mods.lucraftcore.superpower.Superpower;
import lucraft.mods.lucraftcore.superpower.SuperpowerPlayerHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class SuperpowerSpeedforce extends Superpower {

	public SuperpowerSpeedforce() {
		super("speedforce");
	}

	@Override
	public SuperpowerPlayerHandler getNewSuperpowerHandler(EntityPlayer player) {
		return new SpeedforcePlayerHandler(player);
	}

	private static final ResourceLocation TEX = new ResourceLocation(SpeedsterHeroes.ASSETDIR + "textures/gui/superpowerSpeedforce.png");
	
	@Override
	@SideOnly(Side.CLIENT)
	public void renderIcon(Minecraft mc, int x, int y) {
		GlStateManager.color(1, 1, 1);
		mc.renderEngine.bindTexture(TEX);
		mc.currentScreen.drawTexturedModalRect(x, y, 0, 0, 32, 32);
	}
	
	@Override
	public boolean canLevelUp() {
		return true;
	}
	
	@Override
	public int getMaxLevel() {
		return 30;
	}
	
	@Override
	public ISuperpowerPlayerRenderer getPlayerRenderer() {
		return new SpeedforcePlayerRenderer();
	}
	
	@Override
	public List<Ability> addDefaultAbilities(EntityPlayer player, List<Ability> list) {
		list.add(new AbilitySuperSpeed(player).setUnlocked(true));
		list.add(new AbilityAccelerate(player).setUnlocked(true));
		list.add(new AbilityDecelerate(player).setUnlocked(true));
		list.add(new AbilityHealing(player, 10, 2, true).setUnlocked(true));
		list.add(new AbilityWallRunning(player).setUnlocked(true).setRequiredLevel(5));
		list.add(new AbilityWaterRunning(player).setUnlocked(true).setRequiredLevel(10));
		list.add(new AbilitySlowMotion(player).setUnlocked(true).setRequiredLevel(30));
		
		return super.addDefaultAbilities(player, list);
	}
	
	@Override
	public boolean canCustomize() {
		return true;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public GuiCustomizer getCustomizerGui(EntityPlayer player) {
		return new GuiTrailCustomizer();
	}
	
	@Override
	public NBTTagCompound getDefaultStyleTag() {
		NBTTagCompound nbt = new NBTTagCompound();
		nbt.setString("TrailType", EnumTrailType.PARTICLES.toString());
		nbt.setFloat("PrimaryRed", 1);
		nbt.setFloat("PrimaryGreen", 1);
		nbt.setFloat("PrimaryBlue", 1);
		nbt.setFloat("SecondaryRed", 1);
		nbt.setFloat("SecondaryGreen", 1);
		nbt.setFloat("SecondaryBlue", 1);
		return nbt;
	}
	
	@Override
	public int getRequiredLevelForCustomization() {
		return 5;
	}
	
}
