package lucraft.mods.lucraftcore.addonpacks;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.awt.image.BufferedImage;
import java.io.File;

public class AddonPackInfo {

    private String name;
    private String author;
    private String description;

    private File file;
    private String imageLocation;
    public BufferedImage image;
    private ResourceLocation icon;
    private ModContainer mod;

    public AddonPackInfo(String name, String author, String description, String imageLocation, File file) {
        this.name = name;
        this.author = author;
        this.description = description;
        this.imageLocation = imageLocation;
        this.file = file;
    }

    public String getName() {
        return name;
    }

    public String getAuthor() {
        return author;
    }

    public String getDescription() {
        return description;
    }

    public File getFile() {
        return file;
    }

    public String getImageLocation() {
        return imageLocation;
    }

    @SideOnly(Side.CLIENT)
    public void registerIcon() {
        if (!this.imageLocation.isEmpty())
            this.icon = Minecraft.getMinecraft().getTextureManager().getDynamicTextureLocation("pack_icon_" + getName(), new DynamicTexture(image));
    }

    public ResourceLocation getIcon() {
        return icon;
    }

    public ModContainer getMod() {
        return mod;
    }

    public void setMod(ModContainer mod) {
        this.mod = mod;
    }
}
