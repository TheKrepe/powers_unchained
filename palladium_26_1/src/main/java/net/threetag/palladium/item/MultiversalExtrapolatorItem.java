package net.threetag.palladium.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import net.minecraft.resources.ResourceKey;
import net.threetag.palladium.component.PalladiumDataComponents;
import net.threetag.palladium.item.multiverse.Universe;
import net.threetag.palladium.logic.context.DataContext;
import net.threetag.palladium.sound.PalladiumSoundEvents;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.Consumer;

public class MultiversalExtrapolatorItem extends Item {

    public static final int SEARCH_TIME = 60;
    public static final int DISPLAY_TIME = 80;

    public MultiversalExtrapolatorItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag flag) {
        var universe = getUniverse(stack);

        if (universe != null) {
            var title = Universe.getTitle(universe.identifier()).copy();

            context.registries().get(universe)
                    .map(un -> un.value().description())
                    .filter(Optional::isPresent)
                    .ifPresent(desc -> {
                        title.append(" (").append(desc.get()).append(")");
                    });

            builder.accept(title.withStyle(ChatFormatting.GOLD));
        }
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        var stack = player.getItemInHand(hand);

        if (getUniverse(stack) != null || isSearching(stack)) {
            return InteractionResult.PASS;
        }

        stack.set(PalladiumDataComponents.Items.EXTRAPOLATOR_SEARCH_TIMER.get(), 0);
        return InteractionResult.SUCCESS;
    }

    @Override
    public void inventoryTick(ItemStack stack, ServerLevel level, Entity owner, @Nullable EquipmentSlot slot) {
        var timer = stack.get(PalladiumDataComponents.Items.EXTRAPOLATOR_SEARCH_TIMER.get());

        if (timer == null) {
            return;
        }

        if (timer >= DISPLAY_TIME) {
            stack.remove(PalladiumDataComponents.Items.EXTRAPOLATOR_SEARCH_TIMER.get());
            return;
        }

        stack.set(PalladiumDataComponents.Items.EXTRAPOLATOR_SEARCH_TIMER.get(), timer + 1);

        if (timer == SEARCH_TIME && getUniverse(stack) == null) {
            var universe = Universe.getWeightedRandom(level.registryAccess(), DataContext.forEntity(owner), level.getRandom());

            if (universe != null) {
                setUniverse(stack, universe);
            }
        }

        if (!(owner instanceof ServerPlayer player)) {
            return;
        }

        if (timer < SEARCH_TIME) {
            RandomSource random = RandomSource.create(player.tickCount / 2);
            String number = "" + random.nextInt(10) + random.nextInt(10) + random.nextInt(10);
            player.sendOverlayMessage(Universe.getGenericUniverseComponent(number));

            if (timer % 2 == 0) {
                level.playSound(null, player.getX(), player.getY(), player.getZ(), PalladiumSoundEvents.MULTIVERSE_SEARCH.get(), SoundSource.PLAYERS, 1F, 1F);
            }
        } else {
            var universe = getUniverse(stack);

            if (universe != null) {
                player.sendOverlayMessage(Universe.getTitle(universe.identifier()).copy().withStyle(ChatFormatting.GOLD));
            } else {
                player.sendOverlayMessage(Component.translatable("item.palladium.multiversal_extrapolator.desc.error").withStyle(ChatFormatting.RED));
            }
        }
    }

    public static boolean isSearching(ItemStack stack) {
        return stack.has(PalladiumDataComponents.Items.EXTRAPOLATOR_SEARCH_TIMER.get());
    }

    public static void setUniverse(ItemStack stack, ResourceKey<Universe> universe) {
        stack.set(PalladiumDataComponents.Items.EXTRAPOLATOR_UNIVERSE.get(), universe);
    }

    @Nullable
    public static ResourceKey<Universe> getUniverse(ItemStack stack) {
        return stack.get(PalladiumDataComponents.Items.EXTRAPOLATOR_UNIVERSE.get());
    }
}
