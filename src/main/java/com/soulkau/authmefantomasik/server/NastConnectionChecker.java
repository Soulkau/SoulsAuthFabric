package com.soulkau.authmefantomasik.server;

import io.nats.client.Connection;
import io.nats.client.Nats;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.soulkau.authmefantomasik.SoulsAuth.PluginLogger;

public class NastConnectionChecker {


    private static ScheduledExecutorService natsCheckerExecutor;

    public static void stopNatsCheckerExecutor() {
        natsCheckerExecutor.shutdown();
    }

    public static void startNatsTracker() {
        natsCheckerExecutor = Executors.newSingleThreadScheduledExecutor();

        natsCheckerExecutor.scheduleAtFixedRate(() -> {
            try {
                Connection nc = Nats.connect("nats://demo.nats.io:4222");
                PluginLogger.info("The Connection is: " + nc.getStatus());
                nc.close();
                PluginLogger.info("The Connection is: " + nc.getStatus());
            } catch (Exception e) {
                PluginLogger.error("Exception Occurred in NastConnectionTracker: ");
                e.printStackTrace();
            }
        }, 5, 5, TimeUnit.SECONDS);
    }

}
