package lucraft.mods.lucraftcore;

import io.netty.buffer.ByteBuf;
import lucraft.mods.lucraftcore.materials.Material;
import lucraft.mods.lucraftcore.module.Module;
import lucraft.mods.lucraftcore.network.AbstractClientMessageHandler;
import lucraft.mods.lucraftcore.network.AbstractServerMessageHandler;
import lucraft.mods.lucraftcore.network.LCPacketDispatcher;
import lucraft.mods.lucraftcore.util.abilitybar.AbilityBarPos;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import java.util.*;

@Config(modid = LucraftCore.MODID)
public class LCConfig {

    public static Modules modules = new Modules();
    public static Materials materials = new Materials();
    public static Superpowers superpowers = new Superpowers();
    public static SizeChanging sizeChanging = new SizeChanging();
    public static Infinity infinity = new Infinity();
    public static AdvancedCombat ac = new AdvancedCombat();

    @Config.RequiresMcRestart
    public static boolean updateChecker = true;

    public static AbilityBarPos abilityBar = AbilityBarPos.LEFT;

    public static boolean abilityBarScrolling = true;

    @Config.RequiresMcRestart
    @Config.Comment("When enabled the limit of the armor attribute (which is 30 by default) increases to 1024")
    public static boolean increaseArmorLimit = true;

    @Config.RequiresMcRestart
    public static boolean overrideLocateCommand = true;

    public static class Modules {

        @Config.RequiresMcRestart
        public boolean materials = true;

        @Config.RequiresMcRestart
        public boolean superpowers = true;

        @Config.RequiresMcRestart
        public boolean extended_inventory = true;

        @Config.RequiresMcRestart
        public boolean size_changing = true;

        @Config.RequiresMcRestart
        public boolean karma = true;

        @Config.RequiresMcRestart
        public boolean infinity = true;

        @Config.RequiresMcRestart
        @Config.Comment(value = "Requires superpower module")
        public boolean advanced_combat = true;

        public static String moduleSettingsToString() {
            Iterator<Module> it = LucraftCore.MODULES.iterator();
            String s = "";
            while (it.hasNext()) {
                Module m = it.next();
                s = s + m.getName() + ":" + m.isEnabled() + ",";
            }

            return s;
        }

    }

    public static class Materials {

        @Config.Comment(value = {"Values: minimun vein size, maximum vein size, chance, min Y, max Y"})
        public Map<String, int[]> ore_settings = new HashMap<>();

        @Config.RangeInt(min = 0, max = 20)
        public int meteorite_chance = 3;

        @Config.Comment(value = "If enabled you will get a radiation effect when you carry uranium items or if you are near uranium stuff")
        public boolean radiation_ = false;

        public Materials() {
            Material.init();

            for (Material m : Material.getMaterials()) {
                if (m.generateOre()) {
                    int[] array = new int[]{m.getMinVeinSize(), m.getMaxVeinSize(), m.getChance(), m.getMinY(), m.getMaxY()};
                    ore_settings.put(m.getResourceName(), array);
                }
            }
        }

    }

    public static class OreSettings {

        @Config.RangeInt(min = 0)
        public int maxVeinSize;
        @Config.RangeInt(min = 0)
        public int minVeinSize;
        @Config.RangeInt(min = 0)
        public int chance;
        @Config.RangeInt(min = 0)
        public int minY;
        @Config.RangeInt(min = 0)
        public int maxY;

        public OreSettings(Material m) {
            this(m.getMinVeinSize(), m.getMaxVeinSize(), m.getChance(), m.getMinY(), m.getMaxY());
        }

        public OreSettings(int minVeinSize, int maxVeinSize, int chance, int minY, int maxY) {
            this.minVeinSize = minVeinSize;
            this.maxVeinSize = maxVeinSize;
            this.chance = chance;
            this.minY = minY;
            this.maxY = maxY;
        }

    }

    public static class Superpowers {

        @Config.RequiresWorldRestart
        public String[] disabledAbilities = new String[]{"modid:example_ability"};

        @Config.Comment(value = "If enabled, server owners can specify superpower players can choose when they first join the server")
        public boolean startSuperpowersEnabled = false;

        public String[] startSuperpowers = new String[]{"modid:example_superpower"};

        @Config.RequiresWorldRestart
        public boolean nameChangeEffect = true;

        public boolean suitsetEnchantibility = true;

    }

    public static class SizeChanging {
        @Config.RequiresWorldRestart
        public String[] entityBlackList = new String[]{"modid:example_entity"};

        public boolean changeShadowSizes = true;

        public static boolean isEntityBlacklisted(EntityLivingBase entity) {
            EntityEntry entry = null;
            for (EntityEntry entries : ForgeRegistries.ENTITIES) {
                if (entries.getEntityClass() == entity.getClass()) {
                    entry = entries;
                    break;
                }
            }

            if (entry == null)
                return false;

            for (String s : LCConfig.sizeChanging.entityBlackList) {
                if (s.equalsIgnoreCase(entry.getRegistryName().toString())) {
                    return true;
                }
            }

            return false;
        }
    }

    public static class Infinity {

        @Config.Comment("This allows the Infinity Gauntlet to be made in an anvil")
        public boolean anvilCrafting = true;

        @Config.RequiresMcRestart
        @Config.Comment("If enabled you can make the Infinity Gauntlet in a Tinkers Construct smeltery, ThermalExpansion Fluid Transposer or ImmersiveEngineering Metal Press")
        public boolean modCrafting = true;

        @Config.Comment("If enabled the anvil crafting for the Infinity Gauntlet will get disabled if Tinkers Construct/ThermalExpansion is installed")
        public boolean disableAnvilWithMods = true;

        @Config.RequiresMcRestart
        @Config.Comment("If enabled you will be able to find Infinity Gauntlet Casts in villages")
        public boolean gauntletCastGeneration = true;

        @Config.Comment("If enabled the model of the Infinity Gauntlet will have fingers")
        public boolean fingersOnGauntlet = false;

        public boolean renderInfinityGeneratorBeam = true;

    }

    public static class AdvancedCombat {

        public String[] disabledGrabEntities = new String[]{"modid:mobname"};

    }

    @Mod.EventBusSubscriber(modid = LucraftCore.MODID)
    private static class EventHandler {

        @SubscribeEvent
        public static void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event) {
            if (event.getModID().equals(LucraftCore.MODID)) {
                ConfigManager.sync(LucraftCore.MODID, Config.Type.INSTANCE);
            }
        }

        @SubscribeEvent
        public static void onJoinWorld(EntityJoinWorldEvent event) {
            if (event.getEntity() instanceof EntityPlayer && event.getEntity().world.isRemote) {
                LCPacketDispatcher.sendToServer(new MessageSendModuleSettings());
            }
        }
    }

    public static class ClientConfig {

        public static boolean nameChangeEffect;

    }

    public static class MessageSyncConfig implements IMessage {

        public String[] disabledAbilities;
        public boolean nameChangeEffect;

        public MessageSyncConfig() {
        }

        @Override
        public void fromBytes(ByteBuf buf) {
            NBTTagCompound nbt = ByteBufUtils.readTag(buf);
            {
                List<String> list = new ArrayList<String>();
                NBTTagList nbttaglist = nbt.getTagList("DisabledAbilities", 10);

                for (int i = 0; i < nbttaglist.tagCount(); i++) {
                    list.add(((NBTTagString) nbttaglist.get(i)).getString());
                }

                this.disabledAbilities = new String[list.size()];
                for (int i = 0; i < list.size(); i++) {
                    this.disabledAbilities[i] = list.get(i);
                }
            }
            this.nameChangeEffect = nbt.getBoolean("NameChange");
        }

        @Override
        public void toBytes(ByteBuf buf) {
            NBTTagCompound nbt = new NBTTagCompound();
            {
                NBTTagList nbttaglist = new NBTTagList();

                for (int i = 0; i < disabledAbilities.length; i++) {
                    nbttaglist.appendTag(new NBTTagString(disabledAbilities[i]));
                }

                nbt.setTag("DisabledAbilities", nbttaglist);

                nbt.setBoolean("NameChange", this.nameChangeEffect);
            }

            ByteBufUtils.writeTag(buf, nbt);
        }

        public static class Handler extends AbstractClientMessageHandler<MessageSyncConfig> {

            @Override
            public IMessage handleClientMessage(EntityPlayer player, MessageSyncConfig message, MessageContext ctx) {

                LucraftCore.proxy.getThreadFromContext(ctx).addScheduledTask(() -> {
                    LCConfig.superpowers.disabledAbilities = message.disabledAbilities;
                    ClientConfig.nameChangeEffect = message.nameChangeEffect;
                });

                return null;
            }

        }

    }

    public static class MessageSendModuleSettings implements IMessage {

        public String modules;

        public MessageSendModuleSettings() {
            modules = LCConfig.modules.moduleSettingsToString();
        }

        @Override
        public void fromBytes(ByteBuf buf) {
            modules = ByteBufUtils.readUTF8String(buf);
        }

        @Override
        public void toBytes(ByteBuf buf) {
            ByteBufUtils.writeUTF8String(buf, modules);
        }

        public static class Handler extends AbstractServerMessageHandler<MessageSendModuleSettings> {

            @Override
            public IMessage handleServerMessage(EntityPlayer player, MessageSendModuleSettings message, MessageContext ctx) {

                LucraftCore.proxy.getThreadFromContext(ctx).addScheduledTask(() -> {
                    if (message.modules.equalsIgnoreCase(LCConfig.modules.moduleSettingsToString())) {
                        MessageSyncConfig message1 = new MessageSyncConfig();
                        message1.disabledAbilities = LCConfig.superpowers.disabledAbilities;
                        message1.nameChangeEffect = LCConfig.superpowers.nameChangeEffect;
                        LCPacketDispatcher.sendTo(message1, (EntityPlayerMP) player);
                    } else {
                        String s = LCConfig.modules.moduleSettingsToString();
                        TextComponentTranslation text = new TextComponentTranslation("lucraftcore.info.modulesettingswrong");
                        text.appendText("\n");

                        for (String module : s.split(",")) {
                            TextComponentString t = new TextComponentString(module.replace(":", ": ") + "\n");
                            text.appendSibling(t);
                        }

                        ((EntityPlayerMP) player).connection.disconnect(text);
                    }
                });

                return null;
            }

        }

    }

}
