package uk.co.pandagrove;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.screenhandler.v1.ScreenRegistry;
import uk.co.pandagrove.pipe.PipeScreen;

@Environment(EnvType.CLIENT)
public class PandaPipesClientMod implements ClientModInitializer{
    @Override
    public void onInitializeClient() {
        ScreenRegistry.register(PandaPipesMod.PIPE_SCREEN_HANDLER, PipeScreen::new);
    }
}
