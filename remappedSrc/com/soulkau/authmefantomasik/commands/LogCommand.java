package com.soulkau.authmefantomasik.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.soulkau.authmefantomasik.classes.Position;
import com.soulkau.authmefantomasik.server.FileManager;
import com.soulkau.authmefantomasik.server.Handlers;
import com.soulkau.authmefantomasik.server.UnLoggedBehavior;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.world.GameMode;
import net.minecraft.world.World;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.CompletableFuture;


import static com.soulkau.authmefantomasik.SoulsAuth.PluginLogger;
import static com.soulkau.authmefantomasik.server.UnLoggedBehavior.lockedSessions;
import static net.minecraft.server.command.CommandManager.*;


public class LogCommand {



    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("log")
                .then(argument("passwd", StringArgumentType.string())
                        .executes(context -> {
                            try {
                                final String passwd = StringArgumentType.getString(context, "passwd");
                                ServerPlayerEntity target = context.getSource().getPlayer();
                                PluginLogger.info("Password: " + passwd);
                                if (checkPasswd(passwd, target.getGameProfile().getName(), target)) {
                                    UnLoggedBehavior.UnLogged.remove(target.getUuid());
                                    PluginLogger.info("Password Correct");
                                    Position pos = getPosition(target.getName().toString());
                                    if (pos != null) {
                                        Identifier worldIdent = new Identifier(pos.getWorldnamespace(), pos.getWorldpath());
                                        RegistryKey<World> worldKey = RegistryKey.of(RegistryKeys.WORLD, worldIdent);
                                        target.teleport(target.getServer().getWorld(worldKey), pos.getX(), pos.getY(), pos.getZ(), pos.getYaw(), pos.getPitch());
                                    } else {
                                        ServerWorld world = target.getServer().getWorld(World.OVERWORLD);
                                        target.teleport(world, -193, 67, 74, -2.0F, 1.6F);
                                    }
                                    target.changeGameMode(GameMode.SURVIVAL);
                                    lockedSessions.add(target.getUuid());
                                } else {
                                    PluginLogger.info("Password Incorrect");
                                }

                                return 1;
                            } catch(Exception e) {
                                e.printStackTrace();
                            }
                            return 0;
                        })));
    }

    public static Position getPosition(String targetName) {
        String positionJsonString = Handlers.getPlayerPositionJsonString(targetName);
        System.out.println("String: " + positionJsonString);
        if (positionJsonString != null) {
            return Position.getPositionFromJson(positionJsonString);
        }
        return null;
    }

    private static final String CheckExist = "SELECT PASSWORD FROM playersFabricData where LOWER(USERNAME) = LOWER(?);";

    public static boolean checkPasswd(String plaintextPassword, String targetName, ServerPlayerEntity target) {
        if (lockedSessions.contains(target.getUuid())) {
            return false;
        }

        try {
            PreparedStatement sts1 = FileManager.c.prepareStatement(CheckExist);
            System.out.println(targetName);
            sts1.setString(1, targetName);
            ResultSet rs = sts1.executeQuery();
            if (rs.next()) {
                System.out.println("Checking password");
                String hashedPassword = rs.getString("PASSWORD");

                return BCrypt.checkpw(plaintextPassword, hashedPassword);
            }
            sts1.close();
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }


}

