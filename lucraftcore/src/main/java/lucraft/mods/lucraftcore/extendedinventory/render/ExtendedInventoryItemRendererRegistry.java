package lucraft.mods.lucraftcore.extendedinventory.render;

import lucraft.mods.lucraftcore.extendedinventory.IItemExtendedInventory;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import java.util.HashMap;

public class ExtendedInventoryItemRendererRegistry {

    private static HashMap<IItemExtendedInventory, IItemExtendedInventoryRenderer> renderers = new HashMap<IItemExtendedInventory, IItemExtendedInventoryRenderer>();

    public static void registerRenderer(IItemExtendedInventory item, IItemExtendedInventoryRenderer renderer) {
        if (!renderers.containsKey(item))
            renderers.put(item, renderer);
    }

    public static IItemExtendedInventoryRenderer getRenderer(IItemExtendedInventory item) {
        return renderers.get(item);
    }

    public static interface IItemExtendedInventoryRenderer {

        public abstract void render(EntityPlayer player, RenderLivingBase<?> render, ItemStack stack, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale, boolean isHead);

    }

}
