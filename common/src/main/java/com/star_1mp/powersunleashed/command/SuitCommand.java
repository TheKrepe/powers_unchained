package com.star_1mp.powersunleashed.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.star_1mp.powersunleashed.registry.PUItems;
import com.star_1mp.powersunleashed.suit.Suit;
import com.star_1mp.powersunleashed.suit.Suits;
import java.util.Collection;
import java.util.List;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.ArmorType;

/**
 * {@code /suit give <suit> [targets]} — gives the full set (mask, suit, pants, boots) of a suit to the targets
 * (default: the player running the command). Suit names are tab-completed. Needs permission level 2.
 */
public final class SuitCommand {

    private static final DynamicCommandExceptionType UNKNOWN_SUIT = new DynamicCommandExceptionType(
            name -> Component.translatable("commands.powers_unleashed.suit.unknown", name));
    private static final ArmorType[] PIECES = {ArmorType.HELMET, ArmorType.CHESTPLATE, ArmorType.LEGGINGS, ArmorType.BOOTS};

    private SuitCommand() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("suit")
                .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                .then(unlockCommand("unlock", true))
                .then(unlockCommand("lock", false))
                .then(Commands.literal("give").then(Commands.argument("suit", StringArgumentType.word())
                        .suggests((context, builder) -> SharedSuggestionProvider.suggest(
                                Suits.ALL.stream().map(suit -> suit.id().getPath()), builder))
                        .executes(context -> give(context, List.of(context.getSource().getPlayerOrException())))
                        .then(Commands.argument("targets", EntityArgument.players())
                                .executes(context -> give(context, EntityArgument.getPlayers(context, "targets")))))));
    }

    /** {@code /suit unlock|lock <suit|all> [targets]}: Suit Creator progress. */
    private static com.mojang.brigadier.builder.LiteralArgumentBuilder<CommandSourceStack> unlockCommand(String name, boolean unlock) {
        return Commands.literal(name)
                .then(Commands.argument("suit", StringArgumentType.word())
                        .suggests((context, builder) -> SharedSuggestionProvider.suggest(
                                java.util.stream.Stream.concat(java.util.stream.Stream.of("all"), Suits.ALL.stream().map(suit -> suit.id().getPath())), builder))
                        .executes(context -> setUnlocked(context, List.of(context.getSource().getPlayerOrException()), unlock))
                        .then(Commands.argument("targets", EntityArgument.players())
                                .executes(context -> setUnlocked(context, EntityArgument.getPlayers(context, "targets"), unlock))));
    }

    private static int setUnlocked(CommandContext<CommandSourceStack> context, Collection<ServerPlayer> players, boolean unlock)
            throws CommandSyntaxException {
        String name = StringArgumentType.getString(context, "suit");
        List<Suit> suits;
        if (name.equals("all")) {
            suits = Suits.ALL;
        } else {
            Suit suit = Suits.byName(name);
            if (suit == null) {
                throw UNKNOWN_SUIT.create(name);
            }
            suits = List.of(suit);
        }
        for (ServerPlayer player : players) {
            for (Suit suit : suits) {
                if (unlock) {
                    com.star_1mp.powersunleashed.suit.SuitUnlocks.unlock(player, suit);
                } else {
                    com.star_1mp.powersunleashed.suit.SuitUnlocks.lock(player, suit);
                }
            }
        }
        context.getSource().sendSuccess(() -> Component.translatable(
                "commands.powers_unleashed.suit." + (unlock ? "unlocked" : "locked"), suits.size(), players.size()), true);
        return players.size();
    }

    private static int give(CommandContext<CommandSourceStack> context, Collection<ServerPlayer> players)
            throws CommandSyntaxException {
        String name = StringArgumentType.getString(context, "suit");
        Suit suit = Suits.byName(name);
        if (suit == null) {
            throw UNKNOWN_SUIT.create(name);
        }
        for (ServerPlayer player : players) {
            for (ArmorType type : PIECES) {
                ItemStack stack = PUItems.piece(suit, type).get().getDefaultInstance();
                if (!player.getInventory().add(stack) || !stack.isEmpty()) {
                    // inventory full: drop it at the player's feet, only they can pick it up
                    ItemEntity drop = player.createItemStackToDrop(stack, false, false);
                    if (drop != null) {
                        drop.setNoPickUpDelay();
                        drop.setTarget(player.getUUID());
                        drop.level().addFreshEntity(drop);
                    }
                }
            }
            player.containerMenu.broadcastChanges();
        }
        Component suitName = PUItems.piece(suit, ArmorType.CHESTPLATE).get().getDefaultInstance().getHoverName();
        if (players.size() == 1) {
            Component target = players.iterator().next().getDisplayName();
            context.getSource().sendSuccess(() -> Component.translatable("commands.powers_unleashed.suit.success.single", suitName, target), true);
        } else {
            context.getSource().sendSuccess(() -> Component.translatable("commands.powers_unleashed.suit.success.multiple", suitName, players.size()), true);
        }
        return players.size();
    }
}
