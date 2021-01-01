package uk.co.pandagrove.pipe;

import org.apache.logging.log4j.Level;


import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.InventoryProvider;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.stat.Stats;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import uk.co.pandagrove.PandaPipesMod;


public class PipeBlock extends BlockWithEntity {

    public static final DirectionProperty FACING = Properties.FACING;
    public static final BooleanProperty CONNECTED_TOP = BooleanProperty.of("connected_top");
    public static final BooleanProperty CONNECTED_BOTTOM = BooleanProperty.of("connected_bottom");
    public static final BooleanProperty CONNECTED_NORTH = BooleanProperty.of("connected_north");
    public static final BooleanProperty CONNECTED_EAST = BooleanProperty.of("connected_east");
    public static final BooleanProperty CONNECTED_SOUTH = BooleanProperty.of("connected_south");
    public static final BooleanProperty CONNECTED_WEST = BooleanProperty.of("connected_west");
	public static final String ID = "pipe_block";

    public static void log(Level level, String message){
        LOGGER.log(level, "["+PandaPipesMod.MOD_NAME+"] " + message);
    }

	public PipeBlock(FabricBlockSettings settings) {
        super(settings);
        this.setDefaultState(this.stateManager.getDefaultState()
            .with(FACING,Direction.NORTH)
            .with(CONNECTED_TOP,false)
            .with(CONNECTED_BOTTOM,false)
            .with(CONNECTED_NORTH, false)
            .with(CONNECTED_EAST,false)
            .with(CONNECTED_SOUTH, true)
            .with(CONNECTED_WEST, false));
	}

    public BlockState getPlacementState(ItemPlacementContext ctx) {
        Direction facing =ctx.getSide().getOpposite(); 
        BlockView blockView = ctx.getWorld();
        BlockPos blockPos = ctx.getBlockPos();

        BlockState state = this.stateManager.getDefaultState()
            .with(FACING, facing)
            .with(CONNECTED_TOP, isConnected(facing, blockView , blockPos.up(), Direction.UP))
            .with(CONNECTED_BOTTOM, isConnected(facing, blockView,blockPos.down() ,Direction.DOWN))
            .with(CONNECTED_NORTH, isConnected(facing, blockView,blockPos.north(), Direction.NORTH))
            .with(CONNECTED_EAST, isConnected(facing, blockView,blockPos.east(), Direction.EAST))
            .with(CONNECTED_SOUTH, isConnected(facing, blockView,blockPos.south(), Direction.SOUTH))
            .with(CONNECTED_WEST, isConnected(facing, blockView,blockPos.west(), Direction.WEST));
        log(Level.INFO, "Setting Default pipe state to :" + state.toString());
        return state;
    }

    private boolean isConnected(Direction facing, BlockView blockView, BlockPos pos, Direction direction) {
        BlockState state = blockView.getBlockState(pos);
        Block block = state.getBlock();
        BlockEntity blockEntity = blockView.getBlockEntity(pos);

        if (facing == direction) {
            return true;
        } else if (block instanceof PipeBlock) {
            log(Level.INFO, " We have a pipe. Facing: " + state.get(FACING).toString() + " Direction = "+ direction.toString());
            return (state.get(FACING) == direction.getOpposite());
        }
        else {
            return block instanceof InventoryProvider || blockEntity instanceof Inventory;
        }
    }

    public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState newState, WorldAccess world, BlockPos pos, BlockPos posFrom) {
        log( Level.INFO, "Direction: " + direction.toString());
        log( Level.INFO, "Pos: " + pos.toShortString());
        log( Level.INFO, "NewFrom:" + posFrom.toShortString());
        Direction facing = state.get(FACING);
        switch (direction) {
            case UP:
            state = state.with(CONNECTED_TOP, isConnected(facing, world, posFrom, Direction.UP));
            break;
            case DOWN:
            state = state.with(CONNECTED_BOTTOM, isConnected(facing, world,posFrom ,Direction.DOWN));
            break;
            case NORTH:
            state = state.with(CONNECTED_NORTH, isConnected(facing, world,posFrom, Direction.NORTH));
            break;
            case EAST:
            state = state.with(CONNECTED_EAST, isConnected(facing, world,posFrom, Direction.EAST));
            break;
            case SOUTH:
            state = state.with(CONNECTED_SOUTH, isConnected(facing, world,posFrom, Direction.SOUTH));
            break;
            case WEST:
            state = state.with(CONNECTED_WEST, isConnected(facing, world,posFrom, Direction.WEST));
            break;
        }
        return state;
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView view, BlockPos pos, ShapeContext context) {
       return VoxelShapes.cuboid(0.3f, 0.3f, 0.3f, 0.7f, 0.7f, 0.7f);
   }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack itemStack) {
        if (itemStack.hasCustomName()) {
            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (blockEntity instanceof PipeEntity) {
                ((PipeEntity) blockEntity).setCustomName(itemStack.getName());
            }
        }
    }

	@Override
	public BlockEntity createBlockEntity(BlockView world) {
	   return new PipeEntity();
    }

    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }
    
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING, CONNECTED_BOTTOM,CONNECTED_EAST,CONNECTED_NORTH,CONNECTED_SOUTH,CONNECTED_TOP,CONNECTED_WEST);
    }

    @Override
    public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        if (!state.isOf(newState.getBlock())) {
            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (blockEntity instanceof PipeEntity) {
                ItemScatterer.spawn(world, pos, (PipeEntity) blockEntity);
                world.updateComparators(pos, this);
            }
            super.onStateReplaced(state, world, pos, newState, moved);
        }
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (world.isClient) {
            return ActionResult.SUCCESS;
        } else {
            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (blockEntity instanceof PipeEntity) {
                player.openHandledScreen((PipeEntity) blockEntity);
                player.incrementStat(Stats.INSPECT_HOPPER);
            }

            return ActionResult.CONSUME;
        }
    }

}