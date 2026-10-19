package lucraft.mods.lucraftcore.addonpacks.resourcepacks;

import net.minecraft.client.resources.FolderResourcePack;
import org.apache.commons.io.FilenameUtils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class AddonPackFolderResourcePack extends FolderResourcePack {

    public AddonPackFolderResourcePack(File resourcePackFileIn) {
        super(resourcePackFileIn);
    }

    @Override
    public String getPackName() {
        return "LCFolderAddonPack:" + FilenameUtils.removeExtension(resourcePackFile.getName());
    }

    @Override
    protected InputStream getInputStreamByName(String resourceName) throws IOException {
        try {
            return super.getInputStreamByName(resourceName);
        } catch (IOException ioe) {
            if ("pack.mcmeta".equals(resourceName)) {
                return new ByteArrayInputStream(("{\n" + " \"pack\": {\n" + "   \"description\": \"dummy addonpack pack for " + getPackName() + "\",\n" + "   \"pack_format\": 2\n" + "}\n" + "}").getBytes(StandardCharsets.UTF_8));
            } else
                throw ioe;
        }
    }

}
