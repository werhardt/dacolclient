package io.erhardt.dacol.agent;

import io.erhardt.dacol.DacolMap;
import io.erhardt.dacol.DacolSender;
import io.erhardt.pirg.Message;
import io.erhardt.pirg.config.Config;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;




/**
 * Agent to gather basic information about the system like memory, disk usage etc.
 * The information is send periodically to the dacol-server system.
 * 
 * @author Waldemar Erhardt
 *
 */
public class DacolHostAgent extends Thread {

   private static final Logger LOGGER = LoggerFactory.getLogger(DacolHostAgent.class);

   private Config config;
   private String url;

   private int updateInterval;
   private int dataSizeMultiplier;

   public DacolHostAgent() {
   }

   public DacolHostAgent(String url) {
      this.url = url;

   }

   public static void startAgent() {
      startAgent(null);
   }

   public static void startAgent(String url) {
      DacolHostAgent agent = new DacolHostAgent(url);
      agent.start();
   }

   @Override
   public void run() {
      this.startAgentThread();
   }

   private void startAgentThread() {

      if (!this.init()) {
         return;
      }

      while (true) {

         Message msg = Message.build().url(url).body(this.collectData());
         DacolSender.getInstance().send(msg);

         try {
            Thread.sleep(updateInterval * 1000);
         } catch (InterruptedException e) {
         }
      }

   }

   /**
    * Initializes the configuration for the agent.
    * @return <code>true</code> if the configuration is valid, else <code>false</code> in case of
    * missing configuration.
    */
   public boolean init() {
      try {
         this.config = DacolSender.getInstance().getConfig();
      } catch (Exception e) {
         LOGGER.error("DacolHostAgent could not be initialized: {}", e.getMessage());
         return false;
      }

      boolean isActive = this.config.getBoolean("dacolhostagent.active", false);

      if (!isActive) {
         LOGGER.info("DacolHostAgent is not active.");
         return false;
      } else {
         LOGGER.info("DacolHostAgent is active.");
      }

      this.updateInterval = this.config.getInt("dacolhostagent.updateinterval", 5);
      
      String dataSize = this.config.getString("dacolhostagent.datasize", "mb");
      
      this.dataSizeMultiplier = 1024 * 1024; // default is megabyte

      if(dataSize.equalsIgnoreCase("byte")) {
         this.dataSizeMultiplier = 1;
      } else if(dataSize.equalsIgnoreCase("kb")) {
         this.dataSizeMultiplier = 1024;
      } else if(dataSize.equalsIgnoreCase("gb")) {
         this.dataSizeMultiplier = 1024 * 1024 * 1024;
      }



      if (this.url == null) {
         this.url = this.config.getString("dacolhostagent.url", null);

         if (this.url == null) {
            this.url = this.config.getString("dacol.url", null);
         }
      }

      if (this.url == null) {
         LOGGER.error("DacolHostAgent is active but no URL was found. Check your configuration (dacolhostagent.url or dacol.url).");
         return false;
      }

      return true;
   }



   /**
    * Collects some data from the system. At the moment:
    * <li> memory
    * <li> disk usage
    * @return The collected information as JSON.
    */
   private String collectData() {

      DacolMap map = new DacolMap();

      long freeMemory = Runtime.getRuntime().freeMemory() / this.dataSizeMultiplier;
      long maxMemory = Runtime.getRuntime().maxMemory() / this.dataSizeMultiplier;
      long totalMemory = Runtime.getRuntime().totalMemory() / this.dataSizeMultiplier;
      long usedMemory = totalMemory - freeMemory;

      map.put("timestamp", String.valueOf(System.currentTimeMillis()));
      map.put("memory", DacolMap.buildMap("free", String.valueOf(freeMemory),
                                 "max", String.valueOf(maxMemory), 
                                 "total", String.valueOf(totalMemory), 
                                 "used", String.valueOf(usedMemory)));

      File[] roots = File.listRoots();

      List<Map<String, Object>> volumes = new ArrayList<>();

      for (File root : roots) {
         Map<String, Object> volume = new HashMap<>();

         long total = root.getTotalSpace() / this.dataSizeMultiplier;
         long free = root.getUsableSpace() / this.dataSizeMultiplier;
         volume.put("volume", DacolMap.buildMap("name", root.getAbsolutePath().replaceAll("\\\\", ""),
                                       "total", String.valueOf(total), 
                                       "free", String.valueOf(free), "used", String.valueOf(total - free)));
         volumes.add(volume);
      }

      map.put("volumes", volumes);

      return map.toJson();
   }

}
