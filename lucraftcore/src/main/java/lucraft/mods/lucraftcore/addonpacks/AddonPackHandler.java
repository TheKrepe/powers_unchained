package lucraft.mods.lucraftcore.addonpacks;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lucraft.mods.lucraftcore.LCConfig;
import lucraft.mods.lucraftcore.LucraftCore;
import lucraft.mods.lucraftcore.util.creativetabs.CreativeTabRegistry;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.io.FilenameUtils;

import javax.imageio.ImageIO;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

@EventBusSubscriber(modid = LucraftCore.MODID, value = Side.CLIENT)
public class AddonPackHandler {

    public static final List<AddonPackInfo> ADDON_PACKS = new ArrayList<>();
    public static int tabIconTimer = 0;
    public static int tabIconIndex = 0;

    public static CreativeTabs CREATIVE_TAB = new CreativeTabs("tabAddonPacks") {

        @Override
        public ItemStack createIcon() {
            NonNullList<ItemStack> items = NonNullList.<ItemStack>create();
            CREATIVE_TAB.displayAllRelevantItems(items);

            if (items.size() == 0)
                return new ItemStack(Blocks.BARRIER);

            if (tabIconIndex >= items.size())
                tabIconIndex = 0;

            return items.get(tabIconIndex);
        }

    };

    public static File getAddonPacksDir() {
        return new File(AddonPackConfig.addonpacksFolder);
    }

    public static void load() {
        CreativeTabRegistry.addCreativeTab("addon_packs", CREATIVE_TAB);

        LucraftCore.LOGGER.info("Loading addonpacks from '../" + getAddonPacksDir().getName() + "'");

        if (!getAddonPacksDir().exists()) {
            getAddonPacksDir().mkdir();
        }

        for (ModContainer mod : Loader.instance().getActiveModList()) {
            if (mod.getSource() != null && !mod.getName().equalsIgnoreCase("Minecraft") && !mod.getName().equalsIgnoreCase("Minecraft Coder Pack") && !mod.getName().equalsIgnoreCase("Forge Mod Loader") && !mod.getName().equalsIgnoreCase("Minecraft Forge")) {
                if (mod.getSource().isDirectory()) {
                    processPackFolder(mod.getSource(), mod);
                } else if (FilenameUtils.getExtension(mod.getSource().getName()).equalsIgnoreCase("zip") || FilenameUtils.getExtension(mod.getSource().getName()).equalsIgnoreCase("jar")) {
                    processPackZip(mod.getSource(), mod);
                }
            }
        }

        for (File file : getAddonPacksDir().listFiles()) {
            if (file.isDirectory()) {
                processPackFolder(file, null);
            } else if (FilenameUtils.getExtension(file.getName()).equalsIgnoreCase("zip") || FilenameUtils.getExtension(file.getName()).equalsIgnoreCase("jar")) {
                processPackZip(file, null);
            }
        }
    }

    public static void processPackFolder(File file, ModContainer mod) {
        List<File> files = getFilesRecursively(file, new ArrayList<File>());
        AddonPackInfo info = null;

        if (mod == null)
            LucraftCore.LOGGER.info("The addon-pack '" + file.getName() + "' is loading...");

        for (File f : files) {
            String path = f.getPath().replace(file.getPath(), "");
            path = path.replace("/", "\\");
            while (path.startsWith("\\"))
                path = path.substring(1);

            if (path.equalsIgnoreCase("addonpack.mcmeta")) {
                try {
                    FileInputStream stream = new FileInputStream(f);
                    info = readAddonPackInfo(stream, file.getName(), file);
                    stream.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            }
        }

        if (info == null) {
            if (mod == null)
                LucraftCore.LOGGER.error("The addon-pack '" + file.getName() + "' is missing a addonpack.mcmeta file.");
            return;
        }

        for (File f : files) {
            String path = f.getPath().replace(file.getPath(), "");
            path = path.replace("/", "\\");
            while (path.startsWith("\\"))
                path = path.substring(1);

            String s = FilenameUtils.removeExtension(path).replace("assets\\", "");
            String[] astring = s.split("\\\\", 3);
            if (!f.isDirectory() && astring.length >= 3) {
                ResourceLocation loc = new ResourceLocation(astring[0], astring[2]);
                try {
                    FileInputStream stream = new FileInputStream(f);
                    MinecraftForge.EVENT_BUS.post(new AddonPackReadEvent(stream, loc, file, f.getName(), astring[1], mod));
                    stream.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            if (path.equalsIgnoreCase(info.getImageLocation()) && FMLCommonHandler.instance().getSide() == Side.CLIENT) {
                try {
                    FileInputStream stream = new FileInputStream(f);
                    info.image = ImageIO.read(stream);
                    stream.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        if (mod != null)
            info.setMod(mod);
        LucraftCore.LOGGER.info("The addon-pack '" + file.getName() + "' has finished loading.");
        ADDON_PACKS.add(info);
    }

    public static void processPackZip(File file, ModContainer mod) {
        ZipFile zipFile;
        try {
            zipFile = new ZipFile(file);
            String zipName = FilenameUtils.getName(zipFile.getName());
            AddonPackInfo info = null;

            if (mod == null)
                LucraftCore.LOGGER.info("The addon-pack '" + zipName + "' is loading...");

            {
                Enumeration<? extends ZipEntry> entries = zipFile.entries();

                while (entries.hasMoreElements()) {
                    ZipEntry entry = entries.nextElement();

                    if (entry.getName().equals("addonpack.mcmeta") && !entry.getName().replace("/", "\\").contains("\\")) {
                        InputStream stream = zipFile.getInputStream(entry);
                        info = readAddonPackInfo(stream, zipFile.getName(), file);
                        stream.close();
                    }
                }
            }

            if (info == null) {
                if (mod == null)
                    LucraftCore.LOGGER.error("The addon-pack '" + zipName + "' is missing a addonpack.mcmeta file.");
                return;
            }

            {
                Enumeration<? extends ZipEntry> entries = zipFile.entries();
                while (entries.hasMoreElements()) {
                    ZipEntry entry = entries.nextElement();
                    InputStream stream = zipFile.getInputStream(entry);
                    String s = FilenameUtils.removeExtension(entry.getName()).replace("assets/", "").replace("/", "\\");
                    String[] astring = s.split("\\\\", 3);
                    if (!entry.isDirectory() && astring.length >= 3) {
                        ResourceLocation loc = new ResourceLocation(astring[0], astring[2]);
                        MinecraftForge.EVENT_BUS.post(new AddonPackReadEvent(stream, loc, file, entry.getName(), astring[1], mod));
                    }

                    if (entry.getName().equalsIgnoreCase(info.getImageLocation()) && FMLCommonHandler.instance().getSide() == Side.CLIENT) {
                        info.image = ImageIO.read(stream);
                    }

                    stream.close();
                }
            }

            if (mod != null)
                info.setMod(mod);
            LucraftCore.LOGGER.info("The addon-pack '" + zipName + "' has finished loading.");
            ADDON_PACKS.add(info);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static AddonPackInfo readAddonPackInfo(InputStream stream, String zipName, File file) {
        BufferedReader bufferedreader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8));
        JsonObject jsonobject = (new JsonParser()).parse(bufferedreader).getAsJsonObject();

        String name = "";
        String author = "";
        String desc = "";
        String icon = "";

        // Name
        if (JsonUtils.hasField(jsonobject, "name"))
            name = JsonUtils.getString(jsonobject, "name");
        else {
            LucraftCore.LOGGER.error("The addonpack.mcmeta of the addon-pack \"" + zipName + "\" is missing \"name\".");
            return null;
        }

        // Author
        author = JsonUtils.hasField(jsonobject, "author") ? JsonUtils.getString(jsonobject, "author") : "";

        // Author
        desc = JsonUtils.hasField(jsonobject, "description") ? JsonUtils.getString(jsonobject, "description") : "";

        // Icon
        icon = JsonUtils.hasField(jsonobject, "icon") ? JsonUtils.getString(jsonobject, "icon") : "";

        return new AddonPackInfo(name, author, desc, icon, file);
    }

    public static List<File> getFilesRecursively(File folder, List<File> list) {
        if (folder == null || folder.list() == null)
            return list;
        for (File files : folder.listFiles()) {
            if (files.isDirectory()) {
                getFilesRecursively(files, list);
            } else {
                list.add(files);
            }
        }

        return list;
    }

    @SideOnly(Side.CLIENT)
    public static void loadImages() {
        for (AddonPackInfo info : ADDON_PACKS) {
            info.registerIcon();
        }
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public static void onTick(TickEvent.ClientTickEvent e) {
        if (e.phase == Phase.END) {
            tabIconTimer++;

            if (tabIconTimer == 40) {
                NonNullList<ItemStack> items = NonNullList.<ItemStack>create();
                CREATIVE_TAB.displayAllRelevantItems(items);
                tabIconIndex++;
                if (tabIconIndex >= items.size())
                    tabIconIndex = 0;
                tabIconTimer = 0;
            }
        }
    }

}
