package uk.co.pandagrove;

import net.fabricmc.api.ModInitializer;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PandaPipesMod implements ModInitializer {

    public static Logger LOGGER = LogManager.getLogger();

    public static final String MOD_ID = "pandapipes";
    public static final String MOD_NAME = "Panda Pipes";

    @Override
    public void onInitialize() {
        log(Level.INFO, "Initializing");
        PandaRe
        
    }

    public static void log(Level level, String message){
        LOGGER.log(level, "["+MOD_NAME+"] " + message);
    }

}