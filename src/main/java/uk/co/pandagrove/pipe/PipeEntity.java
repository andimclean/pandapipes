package uk.co.pandagrove.pipe;

import org.apache.logging.log4j.Level;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.LockableContainerBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Tickable;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.Direction;
import uk.co.pandagrove.PandaRegistry;

public class PipeEntity extends  LockableContainerBlockEntity implements Tickable, SidedInventory {

	private DefaultedList<ItemStack> inventory;
	
    public PipeEntity() {
		super(PandaRegistry.PIPE_BLOCK_ENTITY);
        this.inventory = DefaultedList.ofSize(1, ItemStack.EMPTY);
    }
    
	@Override
	public int size() {
		return this.inventory.size();
	}

	@Override
	public boolean isEmpty() {		
		return this.getInvStackList().stream().allMatch(ItemStack::isEmpty);
	}

	@Override
	public ItemStack getStack(int slot) {
		return this.getInvStackList().get(slot);
	}

	@Override
	public ItemStack removeStack(int slot, int amount) {
		return Inventories.splitStack(this.getInvStackList(), slot, amount);
	}

	@Override
	public ItemStack removeStack(int slot) {		
		return Inventories.removeStack(this.getInvStackList(), slot);
	}

	@Override
	public void setStack(int slot, ItemStack stack) {
        this.getInvStackList().set(slot, stack);
        if (stack.getCount() > this.getMaxCountPerStack()) {
            stack.setCount(this.getMaxCountPerStack());
        }		
	}

    @Override
    public boolean canPlayerUse(PlayerEntity player) {
        if (this.world.getBlockEntity(this.pos) != this)
            return false;
        else
            return player.squaredDistanceTo((double) this.pos.getX() + 0.5D, (double) this.pos.getY() + 0.5D, (double) this.pos.getZ() + 0.5D) <= 64.0D;

    }

	@Override
	public void clear() {
        this.getInvStackList().clear();
        
	}

	@Override
	public int[] getAvailableSlots(Direction side) {
		return new int[]{0};
	}

	@Override
	public boolean canInsert(int slot, ItemStack stack, Direction dir) {		
        Direction facing = this.getWorld().getBlockState(this.pos).get(PipeBlock.FACING);
        // PandaPipesMod.log(Level.INFO, "Can Insert: Facing = " + facing.toString()+ ", direction+ " + dir.toString());
		return facing == dir;
	}

	@Override
	public boolean canExtract(int slot, ItemStack stack, Direction dir) {
        Direction facing = this.getWorld().getBlockState(this.pos).get(PipeBlock.FACING);
        // PandaPipesMod.log(Level.INFO, "Can Extract: Facing = " + facing.toString()+ ", direction+ " + dir.toString());
		return facing != dir;
	}

	@Override
	public void tick() {
		// PandaPipesMod.log(Level.INFO, "Tick");
		
	}

	@Override
	protected Text getContainerName() {
        return new TranslatableText("container.pandapipes.pipe");
	}

	@Override
	protected ScreenHandler createScreenHandler(int syncId, PlayerInventory playerInventory) {
		// TODO Auto-generated method stub
		return null;
	}

    protected DefaultedList<ItemStack> getInvStackList() {
        return this.inventory;
    }

    public void fromTag(BlockState state, CompoundTag tag) {
        super.fromTag(state, tag);
        this.inventory = DefaultedList.ofSize(this.size(), ItemStack.EMPTY);
        Inventories.fromTag(tag, this.inventory);        
    }
    
    public CompoundTag toTag(CompoundTag tag) {
        super.toTag(tag);
        Inventories.toTag(tag, this.inventory);
        return tag;
    }
}