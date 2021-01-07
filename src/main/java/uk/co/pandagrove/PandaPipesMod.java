package uk.co.pandagrove;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.screenhandler.v1.ScreenHandlerRegistry;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.Identifier;
import uk.co.pandagrove.pipe.PipeBlock;
import uk.co.pandagrove.pipe.PipeScreenHandler;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PandaPipesMod implements ModInitializer {

    public static Logger LOGGER = LogManager.getLogger();

    public static final String MOD_ID = "pandapipes";
    public static final String MOD_NAME = "Panda Pipes";

    public static final Identifier PipeID = new Identifier(MOD_ID, PipeBlock.ID);
	public static final ScreenHandlerType<PipeScreenHandler> PIPE_SCREEN_HANDLER = ScreenHandlerRegistry.registerSimple(PipeID, PipeScreenHandler::new);

    @Override
    public void onInitialize() {
        log(Level.INFO, "Initializing");
        PandaRegistry.onInit();
    }

    public static void log(Level level, String message){
      // LOGGER.log(level, "["+MOD_NAME+"] " + message);
    }

}