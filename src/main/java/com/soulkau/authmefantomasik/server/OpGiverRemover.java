package com.soulkau.authmefantomasik.server;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.authlib.GameProfile;
import net.minecraft.server.OperatorEntry;
import net.minecraft.server.OperatorList;
import net.minecraft.server.command.ServerCommandSource;

import java.io.*;
import java.util.UUID;

import static com.soulkau.authmefantomasik.SoulsAuth.PluginLogger;

public class OpGiverRemover {


    public static void giveOp(ServerCommandSource source, GameProfile gameProfile, Integer permissionLvl) {
        OperatorList operatorList = source.getServer().getPlayerManager().getOpList();
        operatorList.add(new OperatorEntry(gameProfile, permissionLvl, false));
    }

    public static void addToOpListJson(String name, UUID uuid, Integer permissionLevel) {
        JsonObject content = new JsonObject();
        content.addProperty("Name", name);
        content.addProperty("PermissionLvL", permissionLevel);

        JsonObject opData;
        try (BufferedReader reader = new BufferedReader(new FileReader(FileManager.getOperatorListFile()))) {
            JsonElement jsonElement = JsonParser.parseReader(reader);
            opData = jsonElement.isJsonObject() ? jsonElement.getAsJsonObject() : new JsonObject();
        } catch (IOException e) {
            opData = new JsonObject();
            e.printStackTrace();
        }

        if (opData.has(uuid.toString())) {
            JsonObject existingData = opData.getAsJsonObject(uuid.toString());
            existingData.addProperty("Name", name);
            existingData.addProperty("PermissionLvL", permissionLevel);
        } else {
            opData.add(uuid.toString(), content);
        }

        JsonElement jsonTree = new Gson().toJsonTree(opData);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FileManager.getOperatorListFile()))) {
            new Gson().toJson(jsonTree, writer);
            writer.flush();
            System.out.println("Новые данные успешно добавлены в JSON файл: " + FileManager.getOperatorListFile());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void checkForOp(UUID uuid, ServerCommandSource source, GameProfile targetGameProfile) {
        try(BufferedReader reader = new BufferedReader(new FileReader(FileManager.getOperatorListFile()))) {
            JsonObject opData = new Gson().fromJson(reader, JsonObject.class);

            if (opData == null) {
                return;
            }

            JsonObject playerData = opData.getAsJsonObject(uuid.toString());
            if (playerData != null) {
                PluginLogger.info("Op activated: " + targetGameProfile.getName());
                giveOp(source, targetGameProfile, playerData.get("PermissionLvL").getAsInt());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void removeOp(ServerCommandSource source, GameProfile gameProfile) {
        OperatorList operatorList = source.getServer().getPlayerManager().getOpList();
        if (operatorList.get(gameProfile) == null) {
            return;
        }
        operatorList.remove(gameProfile);
        System.out.println("Player op deactivated");
    }


}
