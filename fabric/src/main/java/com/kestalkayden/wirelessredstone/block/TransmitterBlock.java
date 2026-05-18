package com.kestalkayden.wirelessredstone.block;

import com.mojang.serialization.MapCodec;

import java.util.Map;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class TransmitterBlock extends BaseEntityBlock {

    public static final MapCodec<TransmitterBlock> CODEC = simpleCodec(TransmitterBlock::new);

    public static final EnumProperty<Direction> FACING = BlockStateProperties.FACING;

    private static final Map<Direction, VoxelShape> SHAPES = Map.of(
        Direction.UP,    box(1, 0, 1, 15, 2, 15),
        Direction.DOWN,  box(1, 14, 1, 15, 16, 15),
        Direction.NORTH, box(1, 1, 14, 15, 15, 16),
        Direction.SOUTH, box(1, 1, 0, 15, 15, 2),
        Direction.EAST,  box(0, 1, 1, 2, 15, 15),
        Direction.WEST,  box(14, 1, 1, 16, 15, 15));

    public TransmitterBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.UP));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getClickedFace());
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        Direction facing = state.getValue(FACING);
        BlockPos attachedTo = pos.relative(facing.getOpposite());
        return level.getBlockState(attachedTo).isFaceSturdy(level, attachedTo, facing);
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks,
                                     BlockPos pos, Direction direction, BlockPos neighborPos,
                                     BlockState neighborState, RandomSource random) {
        Direction facing = state.getValue(FACING);
        if (direction == facing.getOpposite() && !canSurvive(state, level, pos)) {
            return Blocks.AIR.defaultBlockState();
        }
        return super.updateShape(state, level, ticks, pos, direction, neighborPos, neighborState, random);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (!level.isClientSide() && placer instanceof Player player
                && level.getBlockEntity(pos) instanceof TransmitterBlockEntity be) {
            be.setOwner(player.getUUID());
        }
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        super.onPlace(state, level, pos, oldState, isMoving);
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof TransmitterBlockEntity be) {
            be.onRedstoneInputChanged(level.getBestNeighborSignal(pos));
        }
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock,
                                   Orientation orientation, boolean isMoving) {
        if (level.isClientSide()) return;
        if (level.getBlockEntity(pos) instanceof TransmitterBlockEntity be) {
            be.onRedstoneInputChanged(level.getBestNeighborSignal(pos));
        }
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPES.get(state.getValue(FACING));
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TransmitterBlockEntity(pos, state);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof TransmitterBlockEntity be) {
            player.openMenu(be);
        }
        return InteractionResult.SUCCESS;
    }
}
