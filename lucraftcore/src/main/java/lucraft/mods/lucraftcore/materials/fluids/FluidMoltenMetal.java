package lucraft.mods.lucraftcore.materials.fluids;

import lucraft.mods.lucraftcore.LucraftCore;
import lucraft.mods.lucraftcore.materials.Material;
import net.minecraft.item.EnumRarity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.Fluid;

public class FluidMoltenMetal extends Fluid {

    public static ResourceLocation ICON_METAL_STIL = new ResourceLocation(LucraftCore.MODID, "blocks/fluids/molten_metal");
    public static ResourceLocation ICON_METAL_FLOW = new ResourceLocation(LucraftCore.MODID, "blocks/fluids/molten_metal_flow");

    public final int color;

    public FluidMoltenMetal(Material material) {
        this(material, ICON_METAL_STIL, ICON_METAL_FLOW);
    }

    public FluidMoltenMetal(Material material, ResourceLocation still, ResourceLocation flowing) {
        super(material.getIdentifier().toLowerCase(), still, flowing);

        int color = material.getColor();

        if (((color >> 24) & 0xFF) == 0) {
            color |= 0xFF << 24;
        }
        this.color = color;

        this.setDensity(2000);
        this.setViscosity(10000);
        this.setTemperature(material.getTemperature());
        this.setLuminosity(10);
        this.setRarity(EnumRarity.UNCOMMON);
    }

    @Override
    public int getColor() {
        return color;
    }

}
