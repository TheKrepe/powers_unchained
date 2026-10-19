package lucraft.mods.lucraftcore.addonpacks;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.eventhandler.Event;

import java.io.File;
import java.io.InputStream;

public class AddonPackReadEvent extends Event {

    private InputStream stream;
    private ResourceLocation loc;
    private File packFile;
    private String fileName;
    private String directory;
    private ModContainer mod;

    public AddonPackReadEvent(InputStream stream, ResourceLocation loc, File packFile, String fileName, String directory, ModContainer mod) {
        this.stream = stream;
        this.loc = loc;
        this.packFile = packFile;
        this.fileName = fileName;
        this.directory = directory;
        this.mod = mod;
    }

    public InputStream getInputStream() {
        return stream;
    }

    public ResourceLocation getResourceLocation() {
        return loc;
    }

    public File getPackFile() {
        return packFile;
    }

    public String getFileName() {
        return fileName;
    }

    public String getDirectory() {
        return directory;
    }

    public ModContainer getMod() {
        return mod;
    }
}
