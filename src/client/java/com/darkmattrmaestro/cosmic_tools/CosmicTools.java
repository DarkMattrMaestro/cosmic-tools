package com.darkmattrmaestro.cosmic_tools;

import dev.puzzleshq.puzzleloader.loader.mod.entrypoint.client.ClientModInit;

@SuppressWarnings("unused")
public class CosmicTools implements ClientModInit {

    @Override
    public void onClientInit() {
        Constants.LOGGER.error("Initialized Cosmic Tools Client");
    }


}