package lucraft.mods.heroes.speedsterheroes.blocks;

import lucraft.mods.heroes.speedsterheroes.items.SHItems;
import net.minecraft.block.Block;
import net.minecraft.item.Item;

public class SHBlocks {

	public static Block particleAccelerator;
	public static Block particleAcceleratorPart;
	public static Block speedforceDampener;
	public static Block philosophersStoneChest;
	
	public static void preInit() {
		
		particleAccelerator = new BlockParticleAccelerator();
		particleAcceleratorPart = new BlockParticleAcceleratorPart();
		speedforceDampener = new BlockSpeedforceDampener();
		philosophersStoneChest = new BlockPhilosophersStoneChest();
		
		SHItems.items.add(Item.getItemFromBlock(particleAccelerator));
		SHItems.items.add(Item.getItemFromBlock(particleAcceleratorPart));
		SHItems.items.add(Item.getItemFromBlock(speedforceDampener));
		
	}
	
}
