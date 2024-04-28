package com.soulkau.authmefantomasik.server;

import com.soulkau.authmefantomasik.classes.Position;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.world.GameMode;
import net.minecraft.world.World;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.locks.ReentrantLock;

import static com.soulkau.authmefantomasik.server.UnLoggedBehavior.*;

public class Handlers {



    public static void registerJoinEvent() {
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayerEntity target = handler.player;
            target.changeGameMode(GameMode.SPECTATOR);

            if (WhiteLister(target.getName().getString())) {
                System.out.println("Player Joined: " + target.getName().getString());
            } else {
                target.networkHandler.disconnect(Text.of("Тебя нету в списке игроков"));
            }

            UnLogged.add(target.getUuid());
        });
    }

    public static void registerQuitEvent() {
        ServerPlayConnectionEvents.DISCONNECT.register(((handler, minecraftServer) -> {
            ServerPlayerEntity target = handler.player;


            if (UnLogged.contains(target.getUuid())) {
                UnLogged.remove(target.getUuid());
                lockedSessions.remove(target.getUuid());
                return;
            }


            if (target.isDead()) {
                lockedSessions.remove(target.getUuid());
                saveLocation(target.getName().toString(),-193.0, 67.0, 74.0,  -2.0F, 1.6F, World.OVERWORLD.getValue());
                return;
            }

            saveLocation(target.getName().toString(), target.getX(), target.getZ(), target.getY(), target.getYaw(), target.getPitch(), target.method_48926().getRegistryKey().getValue());
            lockedSessions.remove(target.getUuid());
        }));
    }

    public static void saveLocation(String name, Double x, Double z, Double y, Float yaw, Float pitch, Identifier worldIdent) {

        JSONObject playerPosition = new JSONObject();

        playerPosition.put(name, new Position(x, y, z, yaw, pitch, worldIdent.getPath(), worldIdent.getNamespace() ).turnToJson());

        savePlayerPosition(playerPosition, name);
    }

    private static final ReentrantLock lock = new ReentrantLock();

    public static void savePlayerPosition(JSONObject playerPosition, String name) {
        JSONParser jsonParser = new JSONParser();

        try (BufferedReader reader = new BufferedReader(new FileReader(FileManager.getPlayersLastLocationFile()))) {
            StringBuilder fileContent = new StringBuilder();
            String line;
            boolean found = false;

            lock.lock();

            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    Object obj = jsonParser.parse(line);
                    if (obj instanceof JSONObject) {
                        JSONObject jsonObject = (JSONObject) obj;
                        if (jsonObject.containsKey(name)) {
                            // Обновление существующего объекта
                            System.out.println("Updating existing object with key: " + name);
                            jsonObject.putAll(playerPosition);
                            found = true;
                        }
                        fileContent.append(jsonObject.toJSONString()).append("\n");
                    }
                }
            }

            // Если объект с данным именем не найден, добавляем новый объект
            if (!found) {
                JSONObject newData = new JSONObject();
                newData.put(name, playerPosition.get(name));
                fileContent.append(newData.toJSONString()).append("\n");
            }

            // Запись обновленных данных в файл
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(FileManager.getPlayersLastLocationFile()))) {
                writer.write(fileContent.toString());
            }
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
    }


    public static String getPlayerPositionJsonString(String playerName) {
        JSONParser jsonParser = new JSONParser();

        try (BufferedReader reader = new BufferedReader(new FileReader(FileManager.getPlayersLastLocationFile()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                // Парсим каждую строку как JSONObject
                Object obj = jsonParser.parse(line);
                if (obj instanceof JSONObject) {
                    JSONObject jsonObject = (JSONObject) obj;
                    // Проверяем, содержит ли JSONObject данные об игроке с заданным именем
                    if (jsonObject.containsKey(playerName)) {
                        return jsonObject.get(playerName).toString();
                    }
                }
            }
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }

        return null;
    }



    public static void startMoveCancelThread() {

        ServerTickEvents.END_SERVER_TICK.register(server -> {

            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                ServerWorld world = player.getServer().getWorld(World.OVERWORLD);



                if (shouldBeCancelled(player)) {
                    player.teleport(world, -193, 67, 74, -2.0F, 1.6F);
                }
            }
        });
    }

    public static final String checkDatabase = "SELECT USERNAME FROM playersFabricData where LOWER(USERNAME) = LOWER(?);";

    public static boolean WhiteLister(String name) {
        try {
            PreparedStatement sts1 = FileManager.c.prepareStatement(checkDatabase);
            sts1.setString(1, name);
            ResultSet rs = sts1.executeQuery();
            if (rs.next()) {
                rs.close();
                sts1.close();
                return true;
            } else {
                rs.close();
                sts1.close();
                return false;
            }

        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    private static boolean shouldBeCancelled(ServerPlayerEntity player) {
        return UnLogged.contains(player.getUuid());
    }

}

