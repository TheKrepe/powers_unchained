package lucraft.mods.lucraftcore.superpowers.commands;

import lucraft.mods.lucraftcore.superpowers.Superpower;
import lucraft.mods.lucraftcore.superpowers.SuperpowerHandler;
import lucraft.mods.lucraftcore.superpowers.abilities.Ability;
import lucraft.mods.lucraftcore.superpowers.abilities.supplier.AbilityContainer;
import lucraft.mods.lucraftcore.superpowers.abilities.supplier.AbilityContainerSuperpower;
import lucraft.mods.lucraftcore.superpowers.capabilities.CapabilitySuperpower;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

public class CommandSuperpowerXP extends CommandBase {

    @Override
    public String getName() {
        return "superpowerxp";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 2;
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "commands.superpowerxp.usage";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (args.length <= 0) {
            throw new WrongUsageException("commands.superpowerxp.usage", new Object[0]);
        } else {
            String s = args[0];
            boolean flag = s.endsWith("l") || s.endsWith("L");

            if (flag && s.length() > 1) {
                s = s.substring(0, s.length() - 1);
            }

            int i = parseInt(s);
            boolean flag1 = i < 0;

            if (flag1) {
                i *= -1;
            }

            Entity en = args.length > 1 ? getEntity(server, sender, args[1]) : getCommandSenderAsPlayer(sender);
            if (!en.hasCapability(CapabilitySuperpower.SUPERPOWER_CAP, null))
                throw new CommandException("commands.superpower.not_living_entity");
            EntityLivingBase entity = (EntityLivingBase) en;
            Superpower superpower = SuperpowerHandler.getSuperpower(entity);
            AbilityContainer container = Ability.getAbilityContainer(Ability.EnumAbilityContext.SUPERPOWER, entity);
            AbilityContainerSuperpower handler = container instanceof AbilityContainerSuperpower ? (AbilityContainerSuperpower) container : null;

            if (handler == null)
                throw new CommandException("commands.superpowerxp.nosuperpower");
            if (!superpower.canLevelUp())
                throw new CommandException("commands.superpowerxp.cantlevelup");

            if (flag) {
                if (flag1) {
                    handler.setLevel(handler.getLevel() - i);
                    notifyCommandListener(sender, this, "commands.superpowerxp.success.negative.levels", new Object[]{i, entity.getName()});
                } else {
                    handler.setLevel(handler.getLevel() + i);
                    notifyCommandListener(sender, this, "commands.superpowerxp.success.levels", new Object[]{i, entity.getName()});
                }
            } else {
                if (flag1) {
                    throw new CommandException("commands.superpowerxp.failure.widthdrawXp", new Object[0]);
                }

                handler.addXP(i, false);
                notifyCommandListener(sender, this, "commands.superpowerxp.success", new Object[]{i, entity.getName()});
            }
        }
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
        return args.length == 2 ? getListOfStringsMatchingLastWord(args, server.getOnlinePlayerNames()) : Collections.emptyList();
    }

    @Override
    public boolean isUsernameIndex(String[] args, int index) {
        return index == 1;
    }

}
