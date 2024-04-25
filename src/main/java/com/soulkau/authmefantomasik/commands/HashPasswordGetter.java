package com.soulkau.authmefantomasik.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import org.mindrot.jbcrypt.BCrypt;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class HashPasswordGetter {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("getpasswdHash")
                .then(argument("passwd", StringArgumentType.string())
                        .executes(context -> {
                            final String passwd = StringArgumentType.getString(context, "passwd");
                            String salt = BCrypt.gensalt();
                            System.out.println(BCrypt.hashpw(passwd, salt));
                            System.out.println(BCrypt.hashpw(passwd, salt));
                            return 0;
                        })));
    }
}
