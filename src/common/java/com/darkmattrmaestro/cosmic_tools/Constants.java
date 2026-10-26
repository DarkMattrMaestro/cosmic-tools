package com.darkmattrmaestro.cosmic_tools;

import finalforeach.cosmicreach.chat.Chat;
import finalforeach.cosmicreach.networking.packets.MessagePacket;
import finalforeach.cosmicreach.networking.server.ServerSingletons;
import finalforeach.cosmicreach.singletons.GameSingletons;
import finalforeach.cosmicreach.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.function.Consumer;

public class Constants {

    public static final String MOD_ID = "cosmic_tools";
    public static final Identifier MOD_NAME = Identifier.of(MOD_ID, "CosmicTools");
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    public static Consumer<String> relevantChatSender = GameSingletons.isClient() ?
            (String msg) -> Chat.MAIN_CLIENT_CHAT.addMessage(null, msg) :
            (String msg) -> ServerSingletons.SERVER.broadcastToAll(new MessagePacket(msg));

}
