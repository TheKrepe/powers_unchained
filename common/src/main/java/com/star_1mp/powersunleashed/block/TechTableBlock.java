package com.star_1mp.powersunleashed.block;

import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;

/**
 * Tech Table: a vanilla-style workbench (dark oak, riveted steel rim and work plate, a circuit board, tools). Where
 * gadgets, weapons and other tech are built ({@link com.star_1mp.powersunleashed.menu.TechTableMenu}).
 */
public class TechTableBlock extends Block {

    public static final EnumProperty<Direction> FACING = HorizontalDirectionalBlock.FACING;

    public TechTableBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    private static final net.minecraft.network.chat.Component TITLE = net.minecraft.network.chat.Component.translatable("container.powers_unleashed.tech_table");

    @Override
    protected net.minecraft.world.InteractionResult useWithoutItem(BlockState state, net.minecraft.world.level.Level level,
            net.minecraft.core.BlockPos pos, net.minecraft.world.entity.player.Player player, net.minecraft.world.phys.BlockHitResult hit) {
        if (!level.isClientSide()) {
            player.openMenu(state.getMenuProvider(level, pos));
        }
        return net.minecraft.world.InteractionResult.SUCCESS;
    }

    @Override
    protected net.minecraft.world.MenuProvider getMenuProvider(BlockState state, net.minecraft.world.level.Level level, net.minecraft.core.BlockPos pos) {
        return new net.minecraft.world.SimpleMenuProvider((id, inventory, player) -> new com.star_1mp.powersunleashed.menu.TechTableMenu(
                id, inventory, net.minecraft.world.inventory.ContainerLevelAccess.create(level, pos)), TITLE);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        // The front (tools) faces the player who placed the block.
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }
}
