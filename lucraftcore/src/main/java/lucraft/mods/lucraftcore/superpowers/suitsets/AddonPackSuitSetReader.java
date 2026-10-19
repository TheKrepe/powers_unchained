package lucraft.mods.lucraftcore.superpowers.suitsets;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lucraft.mods.lucraftcore.LucraftCore;
import lucraft.mods.lucraftcore.addonpacks.AddonPackReadEvent;
import lucraft.mods.lucraftcore.addonpacks.AddonPackRecipeReader;
import lucraft.mods.lucraftcore.network.LCPacketDispatcher;
import lucraft.mods.lucraftcore.superpowers.ModuleSuperpowers;
import lucraft.mods.lucraftcore.superpowers.network.MessageSyncJsonSuitSet;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.io.FilenameUtils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@EventBusSubscriber(modid = LucraftCore.MODID)
public class AddonPackSuitSetReader {

    public static final List<JsonSuitSet> SUIT_SETS = new ArrayList<>();

    @SubscribeEvent
    public static void onRead(AddonPackReadEvent e) {
        if (!ModuleSuperpowers.INSTANCE.isEnabled())
            return;

        if (e.getDirectory().equals("suitsets") && FilenameUtils.getExtension(e.getFileName()).equalsIgnoreCase("json")) {
            try {
                BufferedReader bufferedreader = new BufferedReader(new InputStreamReader(e.getInputStream(), StandardCharsets.UTF_8));
                JsonObject jsonobject = (new JsonParser()).parse(bufferedreader).getAsJsonObject();
                JsonSuitSet suit = new JsonSuitSet("name");
                suit.setRegistryName(e.getResourceLocation());
                suit.jsonOriginal = jsonobject;

                if (JsonUtils.hasField(jsonobject, "conditions") && !CraftingHelper.processConditions(JsonUtils.getJsonArray(jsonobject, "conditions"), new AddonPackRecipeReader.JsonContextExt(e.getResourceLocation().getNamespace()))) {
                    return;
                }

                suit.deserialize(jsonobject, e.getResourceLocation());
                SUIT_SETS.add(suit);
            } catch (Exception e2) {
                LucraftCore.LOGGER.error("Wasn't able to read suit set '" + e.getFileName() + "' in addon pack '" + e.getPackFile().getName() + "': " + e2.getMessage());
            }
        }
    }

    @SubscribeEvent
    public static void onRegisterSuitSets(RegisterSuitSetEvent e) {
        for (SuitSet sets : SUIT_SETS)
            e.register(sets);
        SUIT_SETS.clear();
    }

    @SubscribeEvent
    public static void onRegisterItems(RegistryEvent.Register<Item> e) {
        if (!ModuleSuperpowers.INSTANCE.isEnabled())
            return;

        for (ResourceLocation sets : SuitSet.REGISTRY.getKeys()) {
            if (SuitSet.REGISTRY.getObject(sets) instanceof JsonSuitSet) {
                SuitSet.REGISTRY.getObject(sets).registerItems(e);
            }
        }
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public static void onRegisterModels(ModelRegistryEvent e) {
        if (!ModuleSuperpowers.INSTANCE.isEnabled())
            return;

        for (ResourceLocation sets : SuitSet.REGISTRY.getKeys()) {
            if (SuitSet.REGISTRY.getObject(sets) instanceof JsonSuitSet) {
                SuitSet.REGISTRY.getObject(sets).registerModels();
            }
        }
    }

    @SubscribeEvent
    public static void onLogin(PlayerLoggedInEvent e) {
        if (!ModuleSuperpowers.INSTANCE.isEnabled())
            return;

        if (e.player instanceof EntityPlayerMP) {
            for (JsonSuitSet jss : SUIT_SETS) {
                LCPacketDispatcher.sendTo(new MessageSyncJsonSuitSet(jss, jss.jsonOriginal), (EntityPlayerMP) e.player);
            }
        }
    }

}
