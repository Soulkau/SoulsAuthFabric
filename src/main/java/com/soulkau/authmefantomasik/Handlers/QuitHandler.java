package com.soulkau.authmefantomasik.Handlers;

import com.soulkau.authmefantomasik.classes.Position;
import com.soulkau.authmefantomasik.server.FileManager;
import com.soulkau.authmefantomasik.server.OpGiverRemover;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.util.concurrent.locks.ReentrantLock;

import static com.soulkau.authmefantomasik.server.UnLoggedBehavior.UnLogged;
import static com.soulkau.authmefantomasik.server.UnLoggedBehavior.lockedSessions;

public class QuitHandler {


    public static void registerQuitEvent() {
        ServerPlayConnectionEvents.DISCONNECT.register(((handler, minecraftServer) -> {


            ServerPlayerEntity target = handler.player;

            OpGiverRemover.removeOp(minecraftServer.getCommandSource(), target.getGameProfile());


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

            saveLocation(target.getName().toString(), target.getX(), target.getZ(), target.getY(), target.getYaw(), target.getPitch(), target.getWorld().getRegistryKey().getValue());
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



}
