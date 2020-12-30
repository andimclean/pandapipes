package uk.co.pandagrove.pipe;

import net.minecraft.inventory.Inventory;
import net.minecraft.util.math.Direction;

public class DirectionInventory {

	private Direction direction;
	private Inventory inventory;

	public DirectionInventory(Inventory inventory, Direction dir){
        this.setInventory(inventory);
        this.setDirection(dir);
        
	}

	public Direction getDirection() {
		return direction;
	}

	private void setDirection(Direction direction) {
		this.direction = direction;
	}

	public Inventory getInventory() {
		return inventory;
	}

	private void setInventory(Inventory inventory) {
		this.inventory = inventory;
	}

}
