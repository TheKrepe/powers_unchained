package lucraft.mods.lucraftcore.util.commands;

import net.minecraft.command.CommandException;
import net.minecraft.command.CommandLocate;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.*;

public class CommandLocateExt extends CommandLocate {

    public static Map<String, ILocateCommandEntry> ENTRIES = new HashMap<String, ILocateCommandEntry>();

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (args.length != 1) {
            throw new WrongUsageException("commands.locate.usage", new Object[0]);
        } else {
            String s = args[0];
            BlockPos blockpos = getPosFromStructure(s, sender.getEntityWorld(), sender.getPosition());

            if (blockpos != null) {
                TextComponentTranslation textComponent = new TextComponentTranslation("commands.locate.success", new Object[]{s, blockpos.getX(), blockpos.getZ()});
                Style style = new Style();
                ClickEvent event = new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/tp @p " + blockpos.getX() + " ~ " + blockpos.getZ());
                style.setClickEvent(event);
                textComponent.setStyle(style);
                sender.sendMessage(textComponent);
            } else {
                throw new CommandException("commands.locate.failure", new Object[]{s});
            }
        }
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
        List<String> list = new ArrayList<>();
        list.addAll(Arrays.asList("Stronghold", "Monument", "Village", "Mansion", "EndCity", "Fortress", "Temple", "Mineshaft"));
        for (String s : ENTRIES.keySet())
            list.add(s);
        return args.length == 1 ? getListOfStringsMatchingLastWord(args, list) : Collections.emptyList();
    }

    public static BlockPos getPosFromStructure(String structure, World world, BlockPos pos) {
        if (ENTRIES.containsKey(structure))
            return ENTRIES.get(structure).getPosition(world, pos);
        else
            return world.findNearestStructure(structure, pos, false);
    }

    public interface ILocateCommandEntry {

        BlockPos getPosition(World world, BlockPos pos);

    }

}
