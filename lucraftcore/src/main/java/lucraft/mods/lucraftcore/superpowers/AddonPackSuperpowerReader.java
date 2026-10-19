package lucraft.mods.lucraftcore.superpowers;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lucraft.mods.lucraftcore.LucraftCore;
import lucraft.mods.lucraftcore.addonpacks.AddonPackReadEvent;
import lucraft.mods.lucraftcore.addonpacks.AddonPackRecipeReader;
import lucraft.mods.lucraftcore.network.LCPacketDispatcher;
import lucraft.mods.lucraftcore.superpowers.network.MessageSyncJsonSuperpower;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.JsonUtils;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import org.apache.commons.io.FilenameUtils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@EventBusSubscriber(modid = LucraftCore.MODID)
public class AddonPackSuperpowerReader {

    private static final List<JsonSuperpower> SUPERPOWERS = new ArrayList<JsonSuperpower>();

    @SubscribeEvent
    public static void onRead(AddonPackReadEvent e) {
        if (!ModuleSuperpowers.INSTANCE.isEnabled())
            return;

        if (e.getDirectory().equals("superpowers") && FilenameUtils.getExtension(e.getFileName()).equalsIgnoreCase("json")) {
            try {
                BufferedReader bufferedreader = new BufferedReader(new InputStreamReader(e.getInputStream(), StandardCharsets.UTF_8));
                JsonObject jsonobject = (new JsonParser()).parse(bufferedreader).getAsJsonObject();
                JsonSuperpower superpower = new JsonSuperpower("name");
                superpower.setRegistryName(e.getResourceLocation());
                superpower.jsonOriginal = jsonobject;

                if (JsonUtils.hasField(jsonobject, "conditions") && !CraftingHelper.processConditions(JsonUtils.getJsonArray(jsonobject, "conditions"), new AddonPackRecipeReader.JsonContextExt(e.getResourceLocation().getNamespace())))
                    return;

                superpower.deserialize(jsonobject);
                SUPERPOWERS.add(superpower);
            } catch (Exception e2) {
                LucraftCore.LOGGER.error("Wasn't able to read superpower '" + e.getFileName() + "' in addon pack '" + e.getPackFile().getName() + "': " + e2.getMessage());
                e2.printStackTrace();
            }
        }
    }

    @SubscribeEvent
    public static void onRegisterSuperpowers(RegistryEvent.Register<Superpower> e) {
        if (!ModuleSuperpowers.INSTANCE.isEnabled())
            return;

        for (Superpower superpower : SUPERPOWERS)
            e.getRegistry().register(superpower);
    }

    @SubscribeEvent
    public static void onLogin(PlayerLoggedInEvent e) {
        if (!ModuleSuperpowers.INSTANCE.isEnabled())
            return;

        if (e.player instanceof EntityPlayerMP) {
            for (JsonSuperpower js : SUPERPOWERS) {
                LCPacketDispatcher.sendTo(new MessageSyncJsonSuperpower(js, js.jsonOriginal), (EntityPlayerMP) e.player);
            }
        }
    }

}
