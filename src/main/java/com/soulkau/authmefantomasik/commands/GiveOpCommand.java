package com.soulkau.authmefantomasik.commands;

import com.mojang.brigadier.CommandDispatcher;
import static net.minecraft.server.command.CommandManager.*;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.soulkau.authmefantomasik.server.OpGiverRemover;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public class GiveOpCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
                literal("giveop").requires(source -> source.hasPermissionLevel(4))
                        .then(argument("username", StringArgumentType.string())
                                .then(argument("permLvL", IntegerArgumentType.integer())
                                        .executes(context -> {
                                            try {
                                                final String name = StringArgumentType.getString(context, "username");
                                                final int permLvL = IntegerArgumentType.getInteger(context, "permLvL");
                                                MinecraftServer server = context.getSource().getServer();
                                                ServerPlayerEntity target = server.getPlayerManager().getPlayer(name);
                                                if (target != null) {
                                                    OpGiverRemover.addToOpListJson(name, target.getUuid(), permLvL);
                                                    return 1;
                                                }
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }
                                            return 0;
                                        })
                                )
                                .executes(context -> {
                                    context.getSource().sendFeedback(() -> Text.literal("Неправильное использование, <ИМЯ> <УРОВЕНЬ ДОСТУПА>"), false);
                                    return 0;
                                })
                        )
        );
    }



}
