package uk.co.pandagrove.pipe;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import uk.co.pandagrove.PandaPipesMod;

public class PipeScreenHandler extends ScreenHandler {

    private final Inventory inventory;
    

    public PipeScreenHandler(int syncId, PlayerInventory playerInventory) {
        this(syncId, playerInventory, new SimpleInventory(9));
    }
    public PipeScreenHandler(int syncId, PlayerInventory playerInventory, Inventory inventory) {
        super(PandaPipesMod.PIPE_SCREEN_HANDLER, syncId);
        this.inventory = inventory;
        int x,y;
        inventory.onOpen(playerInventory.player);

        this.addSlot(new Slot(inventory, 0, 80,14));

        this.addSlot(new FilteredSlot(inventory, 1, 80, 41));

        // The player Inventory
        for(y = 0; y < 3; y+=1) {
            for (x =0; x< 9; x+=1) {
                this.addSlot(new Slot(playerInventory, y * 9 + x + 9 ,8 + x * 18, 67 + y * 18));
            }
        }
        for(x = 0; x < 9; x+=1) {
            this.addSlot(new Slot(playerInventory, x  ,8 + x * 18, 125));
        }
    }
	
	@Override
	public boolean canUse(PlayerEntity player) {
		return this.inventory.canPlayerUse(player);
	}

    @Override
    public ItemStack onSlotClick(int slotId, int clickData, SlotActionType actionType, PlayerEntity playerEntity) {
        /*if (slotId > 0) {
            ItemStack stack = getSlot(slotId).getStack();
            int upgradeSlot = inventory.getFirstEmptyUpgradeSlot();
            if (stack.getItem() instanceof HopperUpgrade && actionType == SlotActionType.QUICK_MOVE) {
                if (slotId >= 5 && slotId <= 13) {
                    if(playerInventory.insertStack(stack)) {
                        getSlot(slotId).setStack(ItemStack.EMPTY);
                        hopperInventory.onUpgradesUpdated();
                        return ItemStack.EMPTY;
                    }
                } else if(upgradeSlot != -1 && !hasUpgrade(stack)) {
                    ItemStack stack2 = stack.copy();
                    stack2.setCount(1);
                    stack.decrement(1);
                    hopperInventory.getUpgrades().set(upgradeSlot, stack2);
                    getSlot(slotId).setStack(stack);
                    hopperInventory.onUpgradesUpdated();
                    return ItemStack.EMPTY;
                }
            }
        }*/
        ItemStack stack = super.onSlotClick(slotId, clickData, actionType, playerEntity);
        return stack;
    }

    // Shift + Player Inv Slot
    @Override
    public ItemStack transferSlot(PlayerEntity player, int invSlot) {
        ItemStack newStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(invSlot);
        if (slot != null && slot.hasStack()) {
            ItemStack originalStack = slot.getStack();
            //newStack = originalStack.copy();
            if (invSlot < this.inventory.size()) {
                if (!this.insertItem(originalStack, this.inventory.size(), this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.insertItem(originalStack, 0, this.inventory.size(), false)) {
                return ItemStack.EMPTY;
            }
    
            if (originalStack.isEmpty()) {
                slot.setStack(ItemStack.EMPTY);
            } else {
                slot.markDirty();
            }
        }
    
        return newStack;
    }


    public void close(PlayerEntity player) {
        super.close(player);
        this.inventory.onClose(player);
    }
}
