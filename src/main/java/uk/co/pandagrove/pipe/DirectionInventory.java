package uk.co.pandagrove.pipe;

import net.minecraft.inventory.Inventory;
import net.minecraft.util.math.Direction;



public class DirectionInventory implements Comparable<DirectionInventory> {

	private Direction direction;
    private Inventory inventory;
    private int priority;

	public DirectionInventory(Inventory inventory, Direction dir, int priority){
       // PandaPipesMod.log(Level.INFO,"Adding Inventory for " + dir + " priority " + priority);
        this.setInventory(inventory);
        this.setDirection(dir);
        this.setPriority(priority);
        
	}

	public int getPriority() {
		return priority;
	}

	public void setPriority(int priority) {
		this.priority = priority;
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

	@Override
	public int compareTo(DirectionInventory other) {		
		return other.priority - this.priority;
	}

}
