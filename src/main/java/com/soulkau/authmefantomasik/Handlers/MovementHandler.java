package com.soulkau.authmefantomasik.Handlers;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;

import static com.soulkau.authmefantomasik.server.UnLoggedBehavior.*;

public class MovementHandler {

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


    private static boolean shouldBeCancelled(ServerPlayerEntity player) {
        return UnLogged.contains(player.getUuid());
    }

}

