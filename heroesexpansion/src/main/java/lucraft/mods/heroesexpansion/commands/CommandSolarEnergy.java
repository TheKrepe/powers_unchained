package lucraft.mods.heroesexpansion.commands;

import lucraft.mods.heroesexpansion.abilities.AbilitySolarEnergy;
import lucraft.mods.lucraftcore.superpowers.abilities.Ability;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CommandSolarEnergy extends CommandBase {

    @Override
    public String getName() {
        return "solarenergy";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 2;
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "commands.solarenergy.usage";
    }

    // /solarenergy <set/add/remove> <value> [player] [ability]
    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (args.length <= 0 || args.length > 4) {
            throw new WrongUsageException("commands.solarenergy.usage", new Object[0]);
        }
        EntityPlayer player = args.length >= 3 ? getPlayer(server, sender, args[2]) : getCommandSenderAsPlayer(sender);
        List<AbilitySolarEnergy> abilities = Ability.getAbilitiesFromClass(Ability.getAbilities(player), AbilitySolarEnergy.class);
        AbilitySolarEnergy ability = getAbility(abilities, args);
        String operation = args[0];
        int energy = parseInt(args[1]);

        if (operation.equalsIgnoreCase("set")) {
            ability.getDataManager().set(AbilitySolarEnergy.SOLAR_ENERGY, energy);
        } else if (operation.equalsIgnoreCase("add")) {
            ability.receiveEnergy(energy, false);
        } else if (operation.equalsIgnoreCase("remove")) {
            ability.extractEnergy(energy, false);
        }

        sender.sendMessage(new TextComponentTranslation("commands.solarenergy.setenergyto", player.getDisplayName(), ability.getDataManager().get(AbilitySolarEnergy.SOLAR_ENERGY), ability.getDataManager().get(AbilitySolarEnergy.MAX_SOLAR_ENERGY)));
    }

    public AbilitySolarEnergy getAbility(List<AbilitySolarEnergy> abilities, String[] args) throws CommandException {
        if (abilities.size() == 0)
            throw new CommandException("commands.solarenergy.noability");
        else if (args.length < 4 && abilities.size() == 1)
            return abilities.get(0);
        else if (args.length < 3)
            throw new CommandException("commands.solarenergy.nogivenid");
        for (AbilitySolarEnergy ab : abilities) {
            if (ab.getKey().equalsIgnoreCase(args[3])) {
                return ab;
            }
        }
        throw new CommandException("commands.solarenergy.nosuchability");
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
        if (args.length == 1)
            return Arrays.asList("set", "add", "remove");
        else if (args.length == 3)
            return getListOfStringsMatchingLastWord(args, server.getOnlinePlayerNames());
        else if (args.length == 4) {
            if (sender instanceof EntityPlayer) {
                List<AbilitySolarEnergy> abilities = Ability.getAbilitiesFromClass(Ability.getAbilities((EntityPlayer) sender), AbilitySolarEnergy.class);
                List<String> list = new ArrayList<>();

                for (AbilitySolarEnergy ab : abilities) {
                    list.add(ab.getKey());
                }

                if (list.size() > 0)
                    return list;
            }
        }
        return super.getTabCompletions(server, sender, args, targetPos);
    }

}
