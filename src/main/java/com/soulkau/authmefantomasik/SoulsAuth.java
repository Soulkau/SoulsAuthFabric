package com.soulkau.authmefantomasik;

import com.soulkau.authmefantomasik.commands.LogCommand;
import com.soulkau.authmefantomasik.server.FileManager;
import com.soulkau.authmefantomasik.server.Handlers;
import com.soulkau.authmefantomasik.server.NatsDispatcher;
import com.soulkau.authmefantomasik.server.Registration;
import net.fabricmc.api.ModInitializer;
import io.nats.client.Connection;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.dedicated.DedicatedServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.locks.ReentrantLock;


public class SoulsAuth implements ModInitializer {

    public static final String MOD_ID = "soulsauth";
    private Connection natsConnection;

    public static Logger PluginLogger = LoggerFactory.getLogger(MOD_ID);

    public static ReentrantLock MUTEX = new ReentrantLock();

    private NatsDispatcher natsDispatcher;

    private Thread natsThread;

    @Override
    public void onInitialize() {
        onDisable();
        natsDispatcher = new NatsDispatcher();

        natsThread = new Thread(() -> {
            natsDispatcher.startNats();
        });

        CommandRegistrationCallback.EVENT.register(((dispatcher, registryAccess, environment) -> {
            LogCommand.register(dispatcher);
        }));


        natsThread.start();
        natsThread.setName("NastDispatcher");
        Handlers.registerJoinEvent();
        Handlers.registerQuitEvent();
        Handlers.startMoveCancelThread();
        FileManager.getPluginDataFolder();
        FileManager.CreateJsonFiles();
        FileManager.connectToDatabase();


    }

    public void onDisable() {
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            natsThread.interrupt();
        });
    }



}

