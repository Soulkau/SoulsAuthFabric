package com.soulkau.authmefantomasik.server;

import net.minecraft.server.MinecraftServer;
import net.minecraft.text.Text;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.soulkau.authmefantomasik.server.UnLoggedBehavior.UnLogged;

public class SendMessagesWhenUnlogged {

    private static ScheduledExecutorService executor;

    public static void stopMessagesExecutor() {
        executor.shutdown();
    }

    public static void startMessageSending(MinecraftServer minecraftServer) {


        executor = Executors.newSingleThreadScheduledExecutor();
        executor.scheduleAtFixedRate(() -> minecraftServer.execute(() -> {

            minecraftServer.getPlayerManager().getPlayerList().forEach(player -> {
                if (UnLogged.contains(player.getUuid())) {
                    player.sendMessage(Text.of("§cЗалогинься чтобы играть! /log пароль"));
                }
            });

        }), 0, 15, TimeUnit.SECONDS);
    }
}
