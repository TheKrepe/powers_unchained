package lucraft.mods.lucraftcore.sizechanging.commands;

import lucraft.mods.lucraftcore.sizechanging.capabilities.CapabilitySizeChanging;
import lucraft.mods.lucraftcore.sizechanging.sizechanger.SizeChanger;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class CommandSize extends CommandBase {

    @Override
    public String getName() {
        return "sizechange";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 2;
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "commands.size.usage";
    }

    // /size [Size] [size changer] OR /size <Entity> [Size] [size changer]
    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (args.length > 3)
            throw new WrongUsageException("commands.size.usage", new Object[0]);
        else {

            if (args.length == 0) {
                if (sender instanceof EntityPlayer) {
                    EntityPlayer player = (EntityPlayer) sender;
                    float size = player.getCapability(CapabilitySizeChanging.SIZE_CHANGING_CAP, null).getSize();
                    player.sendMessage(new TextComponentTranslation("commands.size.yoursize", size));
                } else {
                    sender.sendMessage(new TextComponentTranslation("commands.size.notaplayer"));
                }
            } else {
                Entity entity = null;
                float size = 0;
                try {
                    entity = getEntity(server, sender, args[0]);
                } catch (CommandException e) {
                    size = (float) parseDouble(args[0], CapabilitySizeChanging.MIN_SIZE, CapabilitySizeChanging.MAX_SIZE);
                }

                if (entity != null) {
                    if (!CapabilitySizeChanging.EventHandler.canChangeInSize(entity))
                        throw new CommandException("commands.size.wrongentity");

                    if (args.length == 1)
                        sender.sendMessage(new TextComponentTranslation("commands.size.entitysize", entity.getDisplayName(), entity.getCapability(CapabilitySizeChanging.SIZE_CHANGING_CAP, null).getSize()));
                    else if (args.length > 1 && args.length < 4) {
                        size = (float) parseDouble(args[1], CapabilitySizeChanging.MIN_SIZE, CapabilitySizeChanging.MAX_SIZE);

                        if (args.length == 2)
                            entity.getCapability(CapabilitySizeChanging.SIZE_CHANGING_CAP, null).setSize(size);
                        else {
                            SizeChanger sizeChanger = parseSizeChanger(args[2]);
                            entity.getCapability(CapabilitySizeChanging.SIZE_CHANGING_CAP, null).setSize(size, sizeChanger);
                        }
                    }
                } else {
                    if (!(sender instanceof EntityPlayer))
                        throw new CommandException("commands.size.notaplayer");

                    EntityPlayer player = (EntityPlayer) sender;

                    if (args.length == 2) {
                        SizeChanger sizeChanger = parseSizeChanger(args[1]);
                        player.getCapability(CapabilitySizeChanging.SIZE_CHANGING_CAP, null).setSize(size, sizeChanger);
                    } else if (args.length == 1) {
                        player.getCapability(CapabilitySizeChanging.SIZE_CHANGING_CAP, null).setSize(size);
                    } else {
                        throw new WrongUsageException("commands.size.usage", new Object[0]);
                    }
                }
            }

        }
    }

    public static SizeChanger parseSizeChanger(String input) throws CommandException {
        SizeChanger sizeChanger = SizeChanger.SIZE_CHANGER_REGISTRY.getValue(new ResourceLocation(input));
        if (sizeChanger != null)
            return sizeChanger;
        throw new CommandException("commands.size.nosizechanger");
    }

    public List<String> getSizeChangerStrings() {
        List<String> list = new ArrayList<>();
        for (SizeChanger sc : SizeChanger.SIZE_CHANGER_REGISTRY.getValuesCollection()) {
            list.add(sc.getRegistryName().toString());
        }
        return list;
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
        if (args.length == 1)
            return getListOfStringsMatchingLastWord(args, server.getOnlinePlayerNames());
        else if (args.length == 2) {
            try {
                Entity entity = getEntity(server, sender, args[0]);
            } catch (CommandException e) {
                return getSizeChangerStrings();
            }
        } else if (args.length == 3) {
            try {
                Entity entity = getEntity(server, sender, args[0]);
                return getSizeChangerStrings();
            } catch (CommandException e) {

            }
        }
        return args.length == 0 ? getListOfStringsMatchingLastWord(args, server.getOnlinePlayerNames()) : super.getTabCompletions(server, sender, args, targetPos);
    }
}
