package lucraft.mods.lucraftcore.test;

import lucraft.mods.lucraftcore.utilities.items.ItemInjection.Injection;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextComponentString;

public class InjectionTest extends Injection {

	public InjectionTest(String name) {
		super(name);
	}

	@Override
	public int chanceForInjection(EntityPlayer player, ItemStack stack) {
		return 52;
	}

	@Override
	public ItemStack inject(EntityPlayer player, ItemStack stack) {
		player.sendMessage(new TextComponentString("JOAH"));
		return stack;
	}

	@Override
	public int priorityForRemoving(EntityPlayer player, ItemStack stack) {
		return 50;
	}

	@Override
	public ItemStack removeFromPlayer(EntityPlayer player, ItemStack stack) {
		player.sendMessage(new TextComponentString("NEEE"));
		return stack;
	}

	@Override
	public int getColor() {
		return 5432834;
	}

}
