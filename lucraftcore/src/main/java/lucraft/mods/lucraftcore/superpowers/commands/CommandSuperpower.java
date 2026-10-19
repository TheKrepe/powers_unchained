package lucraft.mods.lucraftcore.superpowers.commands;

import lucraft.mods.lucraftcore.superpowers.Superpower;
import lucraft.mods.lucraftcore.superpowers.SuperpowerHandler;
import lucraft.mods.lucraftcore.superpowers.capabilities.CapabilitySuperpower;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class CommandSuperpower extends CommandBase {

    @Override
    public String getName() {
        return "superpower";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 2;
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "commands.superpower.usage";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (args.length == 0 || args.length > 2) {
            throw new WrongUsageException("commands.superpower.usage", new Object[0]);
        } else {
            Entity en = getEntity(server, sender, args[0]);
            if (!en.hasCapability(CapabilitySuperpower.SUPERPOWER_CAP, null))
                throw new CommandException("commands.superpower.not_living_entity");
            EntityLivingBase entity = (EntityLivingBase) en;

            if (args.length == 1) {
                Superpower power = SuperpowerHandler.getSuperpower(entity);
                if (power == null)
                    sender.sendMessage(new TextComponentTranslation("commands.superpower.nopower", new Object[]{entity.getName()}));
                else
                    sender.sendMessage(new TextComponentTranslation("commands.superpower.playerspower", new Object[]{entity.getName(), power.getDisplayName()}));
            } else {
                if (args[1].equalsIgnoreCase("remove")) {
                    SuperpowerHandler.removeSuperpower(entity);

                    if (entity != sender) {
                        notifyCommandListener(sender, this, 1, "commands.superpower.remove.success.other", new Object[]{entity.getName()});
                    } else {
                        sender.sendMessage(new TextComponentTranslation("commands.superpower.remove.success.self", new Object[]{}));
                    }

                } else {
                    Superpower power = SuperpowerHandler.SUPERPOWER_REGISTRY.getValue(new ResourceLocation(args[1]));

                    if (power == null)
                        sender.sendMessage(new TextComponentTranslation("commands.superpower.powerdoesntexist", new Object[0]));
                    else {
                        SuperpowerHandler.setSuperpower(entity, power);

                        if (entity != sender) {
                            notifyCommandListener(sender, this, 1, "commands.superpower.success.other", new Object[]{entity.getName(), power.getDisplayName()});
                        } else {
                            sender.sendMessage(new TextComponentTranslation("commands.superpower.success.self", new Object[]{power.getDisplayName()}));
                        }
                    }
                }
            }
        }
    }

    @Override
    public boolean isUsernameIndex(String[] args, int index) {
        return index == 0;
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos pos) {
        ArrayList<String> list = new ArrayList<String>();

        for (ResourceLocation power : SuperpowerHandler.SUPERPOWER_REGISTRY.getKeys())
            list.add(power.toString());

        list.add("remove");

        return args.length == 1 ? (getListOfStringsMatchingLastWord(args, server.getOnlinePlayerNames())) : getListOfStringsMatchingLastWord(args, list);
    }
}
