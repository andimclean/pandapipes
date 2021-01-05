package uk.co.pandagrove.pipe;

import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;

public class FilteredSlot extends Slot{

	private final int index;

	public FilteredSlot(Inventory inventory, int index, int x, int y) {
		super(inventory, index, x, y);
		this.index = index;
	}
    
    public int getMaxItemCount() {
        return 1;
    }

    @Override
    public void setStack(ItemStack stack) {
        if (stack.getCount()> 1) {
            stack.setCount(1);
        }
        this.inventory.setStack(this.index, stack);
        this.markDirty();
    }

    public ItemStack getStack() {
        return this.inventory.getStack(this.index);
    }
}
