package io.erhardt.dacol.log;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import io.erhardt.dacol.DacolMap;
import io.erhardt.dacol.DacolSender;
import io.erhardt.pirg.Message;
import io.erhardt.pirg.config.Config;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by Waldemar Erhardt on 27.03.18.
 */
public class DacolLogbackAppender extends AppenderBase<ILoggingEvent> {

    private static String url;
    private Queue<Message> queue = new ConcurrentLinkedQueue<>();

    public static void init(Config config) {
        if (url == null) {
            url = config.getString("dacollogappender.url", null);

            if (url == null) {
                url = config.getString("dacol.url", null);
            }
        }
    }

    @Override
    protected void append(ILoggingEvent event) {

        Message msg = Message.build().body(
            DacolMap.build()
                .put("level", event.getLevel().toString())
                .put("threadName", event.getThreadName())
                .put("loggerName", event.getLoggerName())
                .put("message", event.getMessage())
                .put("timestamp", String.valueOf(event.getTimeStamp()))
                .put("currentTimeMillis", String.valueOf(System.currentTimeMillis())).toJson());

        // as long as the sender is not initialized, queue the messages.
        if(!DacolSender.isInitialized()) {
            if(!queue.contains(msg)) {
                queue.add(msg);
            }
        } else {
            // if the sender is initialized, send all queued messages and empty the queue
            if(!queue.isEmpty()) {
                for (Message m = queue.poll(); m != null; m = queue.poll()) {
                    m.setUrl(url);
                    DacolSender.getInstance().send(m);
                }
            }
            msg.setUrl(url);
            DacolSender.getInstance().send(msg);
        }
    }
}
