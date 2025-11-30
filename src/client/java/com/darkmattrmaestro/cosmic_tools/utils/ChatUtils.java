package com.darkmattrmaestro.cosmic_tools.utils;

import finalforeach.cosmicreach.blocks.BlockPosition;
import finalforeach.cosmicreach.chat.Chat;

public class ChatUtils {
    public static String blockPosToString(BlockPosition pos) {
        return "(" + pos.getGlobalX() + ", " + pos.getGlobalY() + ", " + pos.getGlobalZ() + ")";
    }

    public static void sendMsg(String msg) {
        Chat.MAIN_CLIENT_CHAT.addMessage(null, "[Cosmic Tools] " + msg);
    }

    public static String nanoToSec(long nano) {
        return String.format("%.2f", nano / 1e+9);
    }
}