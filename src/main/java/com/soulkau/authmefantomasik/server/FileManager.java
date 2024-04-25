package com.soulkau.authmefantomasik.server;


import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import static com.soulkau.authmefantomasik.SoulsAuth.MOD_ID;

public class FileManager {

    private static final String dataFolder = getPluginDataFolder().toString();
    public static Path getPluginDataFolder() {  //Метод для создание/получения папки плагина, созданый вручную ибо не найден в Api
        Path modsDir = Paths.get("mods");
        Path pluginDir = modsDir.resolve(MOD_ID);

        try {
            if (!Files.exists(pluginDir)) {
                Files.createDirectories(pluginDir);
            }
        } catch (IOException e) {
            e.printStackTrace();

            return null;
        }

        return pluginDir;
    }


    // SQL database
    private static final String SQL = "CREATE TABLE IF NOT EXISTS playersFabricData (PASSWORD TEXT NOT NULL, USERNAME TEXT NOT NULL);";

    public static Connection c;

    public static String databaseFilePath = dataFolder + File.separator + "RegistrationData.db";

    public static void connectToDatabase() {
        try {
            c = DriverManager.getConnection("jdbc:sqlite:" + databaseFilePath);

            Statement statement = c.createStatement();

            statement.executeUpdate(SQL);

            statement.close();

            c.setAutoCommit(false);

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    //Assets File
    public static String assetsFilePath = dataFolder + File.separator + "assets.json";

    private static File assetsFile;

    public static String playersLastLocationFilePath = dataFolder +  File.separator + "players_positions.json";

    private static File playersLastLocationFile;

    private static String operatorListPath = dataFolder + File.separator + "operatorList.json";

    private static File operatorListFile;

    public static void CreateJsonFiles() {
        assetsFile = new File(assetsFilePath);
        playersLastLocationFile = new File(playersLastLocationFilePath);
        operatorListFile = new File(operatorListPath);
        if (!assetsFile.exists()) {
            try {
                assetsFile.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        if (!playersLastLocationFile.exists()) {
            try {
                assetsFile.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        if (!operatorListFile.exists()) {
            try {
                operatorListFile.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static File getOperatorListFile() { return operatorListFile; }

    public static File getAssetsFile() {return assetsFile;}

    public static File getPlayersLastLocationFile() {return playersLastLocationFile;}

}
