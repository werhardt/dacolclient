package io.erhardt.dacol;

import io.erhardt.dacol.agent.DacolHostAgent;
import io.erhardt.dacol.log.DacolLogbackAppender;
import io.erhardt.pirg.Message;
import io.erhardt.pirg.Sender;
import io.erhardt.pirg.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Waldemar Erhardt on 23.12.18.
 */
public class DacolSender {

    private static final Logger LOGGER = LoggerFactory.getLogger(DacolSender.class);

    private Config config;
    private Sender sender;

    private static DacolSender instance;
    private static boolean isInitialized = false;

    private DacolSender () {
    }

    public static boolean isInitialized() {
        return  isInitialized;
    }

    public static DacolSender getInstance (Config config) {
        if (DacolSender.instance == null) {
            DacolSender.instance = new DacolSender ();
            isInitialized = DacolSender.instance.init(config);
        }
        return DacolSender.instance;
    }

    public static DacolSender getInstance () {
        if (DacolSender.instance == null) {
            DacolSender.instance = new DacolSender ();
        }
        return DacolSender.instance;
    }

    public void start() {

    }

    public boolean init(Config config) {
        try {
            this.config = config;
        } catch (Exception e) {
            LOGGER.error("dacol could not be initialized: {}", e.getMessage());
            return false;
        }
        sender = new Sender(this.config);

        DacolHostAgent.startAgent();

        DacolLogbackAppender.init(this.config);
        LOGGER.info("Dacol initialized");
        return true;
    }

    public void send(Message msg) {
        this.sender.send(msg);
    }

    public Config getConfig() {
        return this.config;
    }
}
