package uk.co.pandagrove.pipe;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.world.World;

public class PipeItem extends BlockItem{
    
   public static final String ID = "pipe_item";

   public PipeItem(Block block, Settings settings) {
    super(block, settings);

   } 

   @Override
   public void appendTooltip(ItemStack itemstack, World world, List<Text> tooltip, TooltipContext tooltipContect) {
       tooltip.add( new TranslatableText("item.pandapipes.pipeitem.tooltip"));
   }
}
