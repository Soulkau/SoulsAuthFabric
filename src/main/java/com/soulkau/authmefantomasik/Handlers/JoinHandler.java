package com.soulkau.authmefantomasik.Handlers;

import com.soulkau.authmefantomasik.server.FileManager;
import com.soulkau.authmefantomasik.server.OpGiverRemover;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.world.GameMode;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static com.soulkau.authmefantomasik.server.UnLoggedBehavior.UnLogged;

public class JoinHandler {


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

}
