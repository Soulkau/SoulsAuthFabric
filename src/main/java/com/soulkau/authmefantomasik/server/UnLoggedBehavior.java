package com.soulkau.authmefantomasik.server;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class UnLoggedBehavior {

        public static List<UUID> UnLogged = new ArrayList<>();

        public static List<UUID> ExceptionList = new ArrayList<>();

        public static List<UUID> lockedSessions = new ArrayList<>();
}
