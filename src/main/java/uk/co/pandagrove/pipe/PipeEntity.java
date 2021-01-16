package uk.co.pandagrove.pipe;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.InventoryProvider;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.block.entity.HopperBlockEntity;
import net.minecraft.block.entity.LockableContainerBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Tickable;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import uk.co.pandagrove.PandaRegistry;

public class PipeEntity extends  LockableContainerBlockEntity implements Tickable, SidedInventory, Comparator<DirectionInventory> {

    private static final int NumSlots = 1;
    private static final int NumFilters = 1;
    private static final int InventoryStart = 0;
    private static final int FilterStart = InventoryStart + NumSlots;


	private DefaultedList<ItemStack> inventory;
    private DefaultedList<ItemStack> filters;
	private int transferCooldown;
	protected long lasTickTime;
	private int maxCooldown = 8;
	
    public PipeEntity() {
		super(PandaRegistry.PIPE_BLOCK_ENTITY);
        this.inventory = DefaultedList.ofSize(NumSlots, ItemStack.EMPTY);
        this.filters = DefaultedList.ofSize(NumFilters, ItemStack.EMPTY);
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
        if (slot >= FilterStart) {
            return this.filters.get(slot - FilterStart);
        }
		return this.getInvStackList().get(slot);
	}

	@Override
	public ItemStack removeStack(int slot, int amount) {

		return slot < FilterStart ?  Inventories.splitStack(this.getInvStackList(), slot, amount) : Inventories.splitStack(this.filters, slot - FilterStart, amount);
	}

	@Override
	public ItemStack removeStack(int slot) {		
        return slot < FilterStart ? 
            Inventories.removeStack(this.getInvStackList(), slot) :
            Inventories.removeStack(this.filters, slot- FilterStart);
	}

	@Override
	public void setStack(int slot, ItemStack stack) {
        if (slot < FilterStart) {
        this.getInvStackList().set(slot, stack);
        } else {
            this.filters.set(slot - FilterStart, stack);
        }

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
        this.filters.clear();
        
	}

	@Override
	public int[] getAvailableSlots(Direction side) {
		return new int[]{0};
	}

	@Override
	public boolean canInsert(int slot, ItemStack stack, Direction dir) {		
        Direction facing = this.getWorld().getBlockState(this.pos).get(PipeBlock.FACING);
        //PandaPipesMod.log(Level.INFO, "Can Insert: Facing = " + facing.toString()+ ", direction: " + dir.toString());        

        ItemStack filterStack = this.filters.get(0);
        if (!canMergeItems(filterStack, stack)) {
            return false;
        } 
		return facing == dir && !this.needsCooldown() && 
              canMergeItems( this.getInvStackList().get(slot), stack);        
	}

	@Override
	public boolean canExtract(int slot, ItemStack stack, Direction dir) {
        Direction facing = this.getWorld().getBlockState(this.pos).get(PipeBlock.FACING);
        // PandaPipesMod.log(Level.INFO, "Can Extract: Facing = " + facing.toString()+ ", direction+ " + dir.toString());
		return facing != dir;
	}

	@Override
	public void tick() {
		if (this.world != null && !this.world.isClient) {
            --this.transferCooldown;
            if (this.transferCooldown > -1) {
                this.lasTickTime = this.world.getTime();
            }
            if (!this.needsCooldown()){
                this.setCooldown(0);
                this.insertAndExtract( () -> extract(this));
            } else {
                // PandaPipesMod.log(Level.INFO, "Waiting for cooldown " + this.transferCooldown);
            }
        }
		
	}

    private List<DirectionInventory> getOutInventories() {
        
        List<DirectionInventory> inventories = new ArrayList<DirectionInventory>();
        BlockState state = this.getCachedState();
        Object facing = state.get(PipeBlock.FACING);

        

        if (facing != Direction.DOWN && state.get(PipeBlock.CONNECTED_BOTTOM)){
            addInventory(inventories, this.pos.down(), Direction.DOWN);
        }
    
        if (facing != Direction.NORTH && state.get(PipeBlock.CONNECTED_NORTH)){
            addInventory(inventories, this.pos.north(), Direction.NORTH);
        }

        if (facing != Direction.EAST && state.get(PipeBlock.CONNECTED_EAST)){
            addInventory(inventories, this.pos.east(), Direction.EAST);
        }

        if (facing != Direction.SOUTH && state.get(PipeBlock.CONNECTED_SOUTH)){
            addInventory(inventories, this.pos.south(), Direction.SOUTH);
        }

        if (facing != Direction.WEST && state.get(PipeBlock.CONNECTED_WEST)){
            addInventory(inventories, this.pos.west(), Direction.WEST);
        }

        if (facing != Direction.UP && state.get(PipeBlock.CONNECTED_TOP)){
            addInventory(inventories, this.pos.up(), Direction.UP);
        }


        return inventories;

    }
	private void addInventory(List<DirectionInventory> inventories, BlockPos pos, Direction dir) {
        
        BlockState state = world.getBlockState(pos);
        BlockEntity blockEntity = world.getBlockEntity(pos);
        Block block = state.getBlock();

        // PandaPipesMod.log(Level.INFO,"Getting block for " + dir + " is block " + block + "State:" + state + " pos " + pos);
        
        if ( block instanceof InventoryProvider) {
            // PandaPipesMod.log(Level.INFO,"Have a InventoryProvider" + blockEntity.getClass());
            inventories.add( new DirectionInventory(((InventoryProvider) block).getInventory(state, world, pos), dir, 0));
        }
        else if ( blockEntity instanceof Inventory) {
            
            Inventory inve = (Inventory)blockEntity;
            if (inve instanceof ChestBlockEntity && block instanceof ChestBlock ) {
                inventories.add(new DirectionInventory(ChestBlock.getInventory((ChestBlock) block, state, world, pos, true), dir,10));
            } else if (block instanceof PipeBlock && (state.get(PipeBlock.FACING) == dir.getOpposite())){
                // PandaPipesMod.log(Level.INFO,"Adding in a pipe inventory");
                if (((PipeEntity) blockEntity).hasFilter() ) {
                    inventories.add(new DirectionInventory((Inventory) blockEntity, dir.getOpposite(),100));
                } else {
                inventories.add(new DirectionInventory((Inventory) blockEntity, dir.getOpposite(),90));}
            } else if (blockEntity instanceof HopperBlockEntity){
               // PandaPipesMod.log(Level.INFO,"Have a block Inventory" + blockEntity.getClass());
                inventories.add(new DirectionInventory((Inventory) blockEntity, dir,100));
            } else {
                inventories.add(new DirectionInventory((Inventory) blockEntity, dir,20));
            }
        } 
	}

    private boolean insert() {
        ItemStack ourStack = getInvStackList().get(0);
        List<DirectionInventory> inventories = getOutInventories();
        inventories.sort(this);
        for( DirectionInventory di : inventories) {        
            Inventory inv = di.getInventory();
            Direction side = di.getDirection();

            if (inv != null && ourStack.getCount() > 0){
                if (inv instanceof SidedInventory) {
                    // PandaPipesMod.log(Level.INFO, "Got a sided inventory");
                    int[] slots = ((SidedInventory)inv).getAvailableSlots(side);
                    for (int slot: slots) {
                        // PandaPipesMod.log(Level.INFO, "Trying slot " + slot);
                        if (transfer(ourStack, inv,slot,side)){
                            // PandaPipesMod.log(Level.INFO, "Added to slot " + slot);
                            return true;
                        }
                    }
                } else {
                    //PandaPipesMod.log(Level.INFO, "Got a normal inventory");
                    int size = inv.size();
                    for( int slot = 0; slot < size; slot +=1) {
                      //  PandaPipesMod.log(Level.INFO, "Trying slot " + slot);
                        if (transfer(ourStack, inv,slot,side)) {
                        //    PandaPipesMod.log(Level.INFO, "Added to slot " + slot);
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    private boolean transfer(ItemStack from, Inventory to, int slot, @Nullable Direction direction) {        
        ItemStack slotStack = to.getStack(slot);
        if ( canInsert(to, from, slot, direction)){
            // PandaPipesMod.log(Level.INFO, "Adding into slot " + slotStack.toString() +" " + direction);
            
            if (slotStack.isEmpty()) {
               // PandaPipesMod.log(Level.INFO, "To slot is empty");
                slotStack = from.copy();
                slotStack.setCount(1);
                // PandaPipesMod.log(Level.INFO, "New stack item = " + from.getItem());
            } else {
                slotStack = slotStack.copy();
                slotStack.increment(1);
            }
            from.decrement(1);
            // PandaPipesMod.log(Level.INFO, "Setting inventory to " + slotStack.getItem().toString());
            to.setStack(slot, slotStack);
            this.inventory.set(0, from);
            this.markDirty();            
            to.markDirty();

            if (to instanceof PipeEntity) {
               ((PipeEntity)to).setCooldown(((PipeEntity) to).maxCooldown);
            }
            return true;
        }
        return false;
    }

    private boolean canInsert(Inventory inventory, ItemStack stack, int slot, Direction dir) {
        if (!inventory.isValid(slot, stack)) {
            return false;
        } else if ( !(inventory instanceof SidedInventory) || ((SidedInventory)inventory).canInsert(slot, stack, dir)) {
            return canMergeItems(inventory.getStack(slot), stack);

        }
        return false;            
    }

    private static boolean canMergeItems(ItemStack to, ItemStack from) {
        if (to.isEmpty()) {
            return true;
        } else if (to.getItem() != from.getItem()) {
            return false;
        } else if (to.getDamage() != from.getDamage()) {
            return false;
        } else if (to.getCount() >= to.getMaxCount()) {
            return false;
        } else {
            return ItemStack.areTagsEqual(to, from);
        }
    }
    
	private boolean extract(PipeEntity pipeEntity) {
        
		return false;
	}

	private void insertAndExtract(Supplier<Boolean> extractMethod) {
        if (this.world != null && !this.world.isClient && !this.needsCooldown()) {
            // PandaPipesMod.log(Level.INFO, "Inventory " + this.inventory.toString()+ " pos " +this.pos);
            boolean bl = false;
            if (!isEmpty()) {                
                bl |= insert();
            }
            if (!this.isFull()) {
             bl |= extractMethod.get();  
            }

            if (bl) {
                this.setCooldown(maxCooldown);
                this.markDirty();
            }
        }
    }
    

    private boolean isFull() {
        Iterator<ItemStack> inventoryIterator = this.inventory.iterator();

        ItemStack itemStack;
        do {
            if (!inventoryIterator.hasNext()) {
                return true;
            }

            itemStack = inventoryIterator.next();
        } while (!itemStack.isEmpty() && itemStack.getCount() == itemStack.getMaxCount());

        return false;
    }

	private void setCooldown(int coolDown) {
        //PandaPipesMod.log(Level.INFO, "Setting cooldown");
        this.transferCooldown = coolDown;
	}

	private boolean needsCooldown() {
        //PandaPipesMod.log(Level.INFO, "Cooldown = " + transferCooldown);
        return this.transferCooldown > 0;
	}

	@Override
	protected Text getContainerName() {
        return new TranslatableText("container.pandapipes.pipe");
	}

	@Override
	protected ScreenHandler createScreenHandler(int syncId, PlayerInventory playerInventory) {
		return new PipeScreenHandler(syncId, playerInventory, this);
	}

    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
        //We provide *this* to the screenHandler as our class Implements Inventory
        //Only the Server has the Inventory at the start, this will be synced to the client in the ScreenHandler
        return new PipeScreenHandler(syncId, playerInventory, this);
    }
    protected DefaultedList<ItemStack> getInvStackList() {
        return this.inventory;
    }

    protected boolean hasFilter() {
        return this.filters.get(0).getCount() > 0;
    }

    public void fromTag(BlockState state, CompoundTag tag) {
        super.fromTag(state, tag);
        this.inventory = DefaultedList.ofSize(this.size(), ItemStack.EMPTY);
        Inventories.fromTag(tag, this.inventory);
        Tag items = tag.get("Items");
        Tag filters = tag.get("Filters");
        tag.put("Items", filters);
        Inventories.fromTag(tag, this.filters);
        tag.put("Items", items);
    }
    
    public CompoundTag toTag(CompoundTag tag) {
        super.toTag(tag);
        Inventories.toTag(tag, this.filters);
        Tag filters = tag.get("Items");
        tag.put("Filters", filters);
        tag.remove("Items");
        Inventories.toTag(tag, this.inventory);
        return tag;
    }

	@Override
	public int compare(DirectionInventory fst, DirectionInventory snd) {	
        return fst.compareTo(snd);
	}
}