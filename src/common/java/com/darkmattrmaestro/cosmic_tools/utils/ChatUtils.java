package com.darkmattrmaestro.cosmic_tools.utils;

import com.darkmattrmaestro.cosmic_tools.Constants;
import finalforeach.cosmicreach.blocks.BlockPosition;

public class ChatUtils {
    public static void sendMsg(String msg) {
        if (Constants.relevantChatSender == null) {
            Constants.LOGGER.error("relevantChatSender not set!");
            return;
        }
        Constants.relevantChatSender.accept("[Cosmic Tools] " + msg);
    }

    public static String blockPosToString(BlockPosition pos) {
        return "(" + pos.getGlobalX() + ", " + pos.getGlobalY() + ", " + pos.getGlobalZ() + ")";
    }
}
