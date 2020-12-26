package uk.co.pandagrove;

import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Material;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import uk.co.pandagrove.pipe.PipeBlock;

public class PandaRegistry {
    public static final PipeBlock PIPE_BLOCK = new PipeBlock(FabricBlockSettings.of(Material.METAL));

    public void onInit() {
        Registry.register(Registry.BLOCK, new Identifier(PandaPipesMod.MOD_ID), PIPE_BLOCK);
    }

}