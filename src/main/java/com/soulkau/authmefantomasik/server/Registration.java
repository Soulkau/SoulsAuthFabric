package com.soulkau.authmefantomasik.server;

import org.mindrot.jbcrypt.BCrypt;

import java.nio.charset.StandardCharsets;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.UUID;

import static com.soulkau.authmefantomasik.SoulsAuth.MUTEX;
import static com.soulkau.authmefantomasik.SoulsAuth.PluginLogger;

public class Registration {

    private String salt = BCrypt.gensalt();
    public boolean registerPlayerInDatabase(String password, String name) throws SQLException {
        if (checkForExist(name)) {
            return false;
        }


        try {
            MUTEX.lock();
            String registrationSqlCode = "INSERT INTO playersFabricData (PASSWORD, USERNAME) VALUES (?, ?)";
            PreparedStatement sts1 = FileManager.c.prepareStatement(registrationSqlCode);
            sts1.setString(1, BCrypt.hashpw(password, salt));
            sts1.setString(2, name);
            sts1.executeUpdate();
            FileManager.c.commit();
            sts1.close();
            PluginLogger.info("PlayerRegistred");
            return true;
        } finally {
            MUTEX.unlock();
        }
    }

    private final static String Deactivate = "DELETE FROM playersFabricData WHERE LOWER(USERNAME) = LOWER(?);";

    public boolean deactivate(String targetName) throws SQLException {
        MUTEX.lock();
        PreparedStatement sts1 = FileManager.c.prepareStatement(Deactivate);
        sts1.setString(1, targetName);
        sts1.executeUpdate();
        FileManager.c.commit();
        sts1.close();
        MUTEX.unlock();
        return true;
    }



    private static final String CheckExist = "SELECT USERNAME FROM playersData WHERE LOWER(USERNAME) = LOWER(?);";

    public boolean checkForExist(String name) {
        try (PreparedStatement sts1 = FileManager.c.prepareStatement(CheckExist)) {
            sts1.setString(1, name);
            try (ResultSet rs = sts1.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

}
