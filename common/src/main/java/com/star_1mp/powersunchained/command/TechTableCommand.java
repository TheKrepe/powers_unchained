package com.star_1mp.powersunchained.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.star_1mp.powersunchained.item.WebShootersItem;
import com.star_1mp.powersunchained.platform.Services;
import com.star_1mp.powersunchained.registry.PUItems;
import com.star_1mp.powersunchained.tech.WebModules;
import java.util.ArrayList;
import net.minecraft.world.item.ItemStack;
import com.star_1mp.powersunchained.tech.TechFeats;
import com.star_1mp.powersunchained.tech.TechProgress;
import com.star_1mp.powersunchained.tech.WebModule;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

/**
 * {@code /techtable module unlock|lock <module|all> [targets]} — unlocks / locks modules on the targets' web-shooters (the
 * pair in the main hand, else the worn pair; default target: the player running the command). Locking also takes the
 * module out. {@code /techtable achieve unlock|lock <task|all> [targets]} sets Tech Table tasks (feats).
 * Module ids are tab-completed. Needs permission level 2.
 */
public final class TechTableCommand {

    private static final DynamicCommandExceptionType UNKNOWN_MODULE = new DynamicCommandExceptionType(
            name -> Component.translatable("commands.powers_unchained.techtable.unknown", name));

    private static final DynamicCommandExceptionType UNKNOWN_TASK = new DynamicCommandExceptionType(
            name -> Component.translatable("commands.powers_unchained.techtable.unknown_task", name));

    private TechTableCommand() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("techtable")
                .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                .then(Commands.literal("module")
                        .then(moduleCommand("unlock", true))
                        .then(moduleCommand("lock", false)))
                .then(Commands.literal("achieve")
                        .then(achieveCommand("unlock", true))
                        .then(achieveCommand("lock", false))));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> moduleCommand(String name, boolean unlock) {
        return Commands.literal(name)
                .then(Commands.argument("module", StringArgumentType.word())
                        .suggests((context, builder) -> SharedSuggestionProvider.suggest(
                                Stream.concat(Stream.of("all"), Arrays.stream(WebModule.values()).map(WebModule::id)), builder))
                        .executes(context -> set(context, List.of(context.getSource().getPlayerOrException()), unlock))
                        .then(Commands.argument("targets", EntityArgument.players())
                                .executes(context -> set(context, EntityArgument.getPlayers(context, "targets"), unlock))));
    }

    /** {@code /techtable achieve unlock|lock <task|all> [targets]}: Tech Table tasks (e.g. lightning in web-shooters). */
    private static LiteralArgumentBuilder<CommandSourceStack> achieveCommand(String name, boolean unlock) {
        return Commands.literal(name)
                .then(Commands.argument("task", StringArgumentType.word())
                        .suggests((context, builder) -> SharedSuggestionProvider.suggest(
                                Stream.concat(Stream.of("all"), TechFeats.ALL.stream()), builder))
                        .executes(context -> achieve(context, List.of(context.getSource().getPlayerOrException()), unlock))
                        .then(Commands.argument("targets", EntityArgument.players())
                                .executes(context -> achieve(context, EntityArgument.getPlayers(context, "targets"), unlock))));
    }

    private static int achieve(CommandContext<CommandSourceStack> context, Collection<ServerPlayer> players, boolean unlock)
            throws CommandSyntaxException {
        String name = StringArgumentType.getString(context, "task");
        List<String> feats;
        if (name.equals("all")) {
            feats = TechFeats.ALL;
        } else if (TechFeats.ALL.contains(name)) {
            feats = List.of(name);
        } else {
            throw UNKNOWN_TASK.create(name);
        }
        for (ServerPlayer player : players) {
            TechProgress progress = Services.GAMEPLAY.techProgress(player);
            for (String feat : feats) {
                progress = unlock ? progress.withFeat(feat) : progress.withoutFeat(feat);
            }
            Services.GAMEPLAY.setTechProgress(player, progress);
        }
        Component what = feats.size() == 1 ? TechFeats.title(feats.get(0)) : Component.translatable("commands.powers_unchained.techtable.all_tasks");
        context.getSource().sendSuccess(() -> Component.translatable(
                "commands.powers_unchained.techtable.task_" + (unlock ? "done" : "reset"), what, players.size()), true);
        return players.size();
    }

    private static int set(CommandContext<CommandSourceStack> context, Collection<ServerPlayer> players, boolean unlock)
            throws CommandSyntaxException {
        String name = StringArgumentType.getString(context, "module");
        List<WebModule> modules;
        if (name.equals("all")) {
            modules = List.of(WebModule.values());
        } else {
            WebModule module = WebModule.byId(name);
            if (module == null) {
                throw UNKNOWN_MODULE.create(name);
            }
            modules = List.of(module);
        }
        int done = 0;
        for (ServerPlayer player : players) {
            java.util.function.Consumer<ItemStack> change = shooters -> {
                List<WebModule> installed = new ArrayList<>(WebModules.installed(shooters));
                for (WebModule module : modules) {
                    WebModules.setUnlocked(shooters, module, unlock);
                    if (!unlock) {
                        installed.remove(module);  // locked = it can't stay installed either
                    }
                }
                if (!unlock) {
                    WebModules.set(shooters, installed);
                }
            };
            // owner: modules are unlocked per pair of web-shooters -> the ones in the main hand, else the worn ones
            ItemStack hand = player.getMainHandItem();
            if (hand.getItem() instanceof WebShootersItem) {
                change.accept(hand);
            } else if (!Services.GAMEPLAY.modifyAccessory(player, PUItems.WEB_SHOOTERS.get(), change)) {
                continue;
            }
            player.containerMenu.broadcastChanges();
            done++;
        }
        if (done == 0) {
            context.getSource().sendFailure(Component.translatable("commands.powers_unchained.techtable.no_shooters"));
            return 0;
        }
        int count = done;
        Component what = modules.size() == 1 ? modules.get(0).displayName()
                : Component.translatable("commands.powers_unchained.techtable.all");
        context.getSource().sendSuccess(() -> Component.translatable(
                "commands.powers_unchained.techtable." + (unlock ? "unlocked" : "locked"), what, count), true);
        return count;
    }
}
