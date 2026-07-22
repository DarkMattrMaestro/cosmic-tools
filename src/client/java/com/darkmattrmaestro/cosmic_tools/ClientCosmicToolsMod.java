package com.darkmattrmaestro.cosmic_tools;

import com.darkmattrmaestro.cosmic_tools.commands.CommandLoadVideo;
import com.darkmattrmaestro.cosmic_tools.utils.Selection;
import dev.puzzleshq.puzzleloader.loader.mod.entrypoint.client.ClientModInit;
import dev.puzzleshq.puzzleloader.loader.mod.entrypoint.client.ClientPostModInit;
import finalforeach.cosmicreach.gamestates.GameState;

import static finalforeach.cosmicreach.chat.commands.Command.registerCommand;

@SuppressWarnings("unused")
public class ClientCosmicToolsMod implements ClientModInit {
    @Override
    public void onClientInit() {
        registerCommand(CommandLoadVideo::new, "load_video", "vid");
    }
}
