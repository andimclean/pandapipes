package uk.co.pandagrove;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Material;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import uk.co.pandagrove.pipe.PipeBlock;
import uk.co.pandagrove.pipe.PipeEntity;
import uk.co.pandagrove.pipe.PipeItem;

public class PandaRegistry {
    public static final PipeBlock PIPE_BLOCK = new PipeBlock(FabricBlockSettings.of(Material.METAL));
    public static final PipeItem PIPE_ITEM = new PipeItem(PIPE_BLOCK,new FabricItemSettings().group(ItemGroup.REDSTONE));
	public static BlockEntityType<PipeEntity> PIPE_BLOCK_ENTITY = null;

    public static void onInit() {
        Registry.register(Registry.BLOCK, new Identifier(PandaPipesMod.MOD_ID,PipeBlock.ID), PIPE_BLOCK);
        Registry.register(Registry.ITEM, new Identifier(PandaPipesMod.MOD_ID, PipeBlock.ID), PIPE_ITEM);
        PIPE_BLOCK_ENTITY = Registry.register(Registry.BLOCK_ENTITY_TYPE, "pandapipes:pipe_block", BlockEntityType.Builder.create(PipeEntity::new, PIPE_BLOCK).build(null));
    }

}