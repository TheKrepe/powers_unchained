package net.threetag.palladium.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.threetag.palladium.menu.MultiversalIteratorMenu;
import net.threetag.palladium.menu.MultiversalIteratorSuitStandMenu;
import org.jetbrains.annotations.Nullable;

public class MultiversalIteratorBlock extends HorizontalDirectionalBlock {

    public static final Component CONTAINER_TITLE = Component.translatable("container.palladium.multiversal_iterator");
    public static final MapCodec<MultiversalIteratorBlock> CODEC = simpleCodec(MultiversalIteratorBlock::new);

    public MultiversalIteratorBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends HorizontalDirectionalBlock> codec() {
        return CODEC;
    }

    @Override
    protected MenuProvider getMenuProvider(BlockState state, Level level, BlockPos pos) {
        return new SimpleMenuProvider(
                (containerId, inv, player) -> new MultiversalIteratorMenu(containerId, inv, ContainerLevelAccess.create(level, pos)), CONTAINER_TITLE
        );
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState blockState, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!level.isClientSide()) {
            var suitStand = findSuitStand(level, pos);

            if (suitStand == null) {
                player.openMenu(blockState.getMenuProvider(level, pos));
            } else {
                player.openMenu(new SimpleMenuProvider(
                        (containerId, inv, p) -> new MultiversalIteratorSuitStandMenu(containerId, inv, suitStand, ContainerLevelAccess.create(level, pos)), CONTAINER_TITLE
                ), buf -> buf.writeVarInt(suitStand.getId()));
            }
        }

        return InteractionResult.SUCCESS;
    }

    @Nullable
    private static ArmorStand findSuitStand(Level level, BlockPos pos) {
        var stands = level.getEntitiesOfClass(ArmorStand.class, new AABB(pos.above()));
        return stands.isEmpty() ? null : stands.getFirst();
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }
}
