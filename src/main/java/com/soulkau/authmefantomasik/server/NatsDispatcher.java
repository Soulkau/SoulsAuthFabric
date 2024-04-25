package com.soulkau.authmefantomasik.server;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.nats.client.Connection;
import io.nats.client.Dispatcher;
import io.nats.client.Nats;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;

import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.time.Duration;

import java.util.concurrent.CountDownLatch;

import static com.soulkau.authmefantomasik.SoulsAuth.PluginLogger;
import static com.soulkau.authmefantomasik.server.FileManager.assetsFilePath;


public class NatsDispatcher {

    private static String assetsFileName = "assets.yml";

    private static String usageString = "";


    private static void setUsageString() {
        try (FileReader reader = new FileReader(assetsFilePath)) {
            JsonElement jsonElement = JsonParser.parseReader(reader);

            if (jsonElement.isJsonObject()) {
                JsonObject jsonObject = jsonElement.getAsJsonObject();

                JsonElement userElement = jsonObject.get("user");
                JsonElement passwdElement = jsonObject.get("password");

                if (userElement != null && passwdElement != null) {
                    String user = userElement.getAsString();
                    String passwd = passwdElement.getAsString();

                    usageString = "tls://" + user + ":" + passwd + "@navr.io:9622";
                } else {
                    PluginLogger.error("AssetsFileIsNull");
                }
            } else {
                PluginLogger.error("Invalid JSON format");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Connection nastConnection;

    public Registration registration;

    public void startNats() {
        setUsageString();
        registration = new Registration();

        try(Connection nc = Nats.connect(usageString)) {
            nastConnection = nc;


            CountDownLatch latch = new CountDownLatch(Integer.MAX_VALUE);

            Dispatcher newUser = nc.createDispatcher(msg -> {
                byte[] receivedMessageBytes = msg.getData();

                if (receivedMessageBytes == null) {
                    nc.publish("minecraft.new.user.failure", "messageIsNull".getBytes(StandardCharsets.UTF_8));
                    return;
                }

                PluginLogger.info("Message Recived");

                String message = new String(receivedMessageBytes, StandardCharsets.UTF_8);
                String[] userData = message.split("::");

                if (userData.length != 2) {
                    nc.publish("minecraft.new.user.failure", (userData[0] + "::invalidArguments").getBytes(StandardCharsets.UTF_8));
                    return;
                }

                try {
                    if (registration.registerPlayerInDatabase(userData[1], userData[0])) {
                        nc.publish("minecraft.new.user.success", (userData[0] + "::ok").getBytes(StandardCharsets.UTF_8));
                    } else {
                        nc.publish("minecraft.new.user.failure", (userData[0] + "::UserAlreadyExistsFabric").getBytes(StandardCharsets.UTF_8));
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                    nc.publish("minecraft.new.user.failure", (userData[0] + "::SQLExceptionOccurredFabric").getBytes(StandardCharsets.UTF_8));
                }

                latch.countDown();
            });

            newUser.subscribe("minecraft.new.user");

            Dispatcher deleteUserDispatcher = nc.createDispatcher(msg -> {
                PluginLogger.info("PlayerDelete Recived");


                byte[] receivedMessageBytes = msg.getData();

                if (receivedMessageBytes == null) {
                    nc.publish("minecraft.user.delete.failure", "messageIsNull".getBytes(StandardCharsets.UTF_8));
                    return;
                }

                String message = new String(receivedMessageBytes, StandardCharsets.UTF_8);
                String[] userData = message.split("::");

                try {
                    if (registration.deactivate(userData[0])) {
                        nc.publish("minecraft.user.delete.success", (userData[0] + "::ok").getBytes(StandardCharsets.UTF_8));
                    } else {
                        nc.publish("minecraft.user.delete.failure", (userData[0] + "::Deactivate went wrong").getBytes(StandardCharsets.UTF_8));
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                    nc.publish("minecraft.user.delete.failure", (userData[0] + "::SQLExceptionOccurredFabric").getBytes(StandardCharsets.UTF_8));
                }

                latch.countDown();
            });


            deleteUserDispatcher.subscribe("minecraft.user.delete");


            PluginLogger.info("NATS STARTED");

            nc.flush(Duration.ZERO);
            latch.await();
            System.out.println("latch awaiting");
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

}
