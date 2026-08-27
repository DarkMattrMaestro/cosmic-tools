package com.darkmattrmaestro.cosmic_tools.utils;

import dev.puzzleshq.puzzleloader.loader.launch.Piece;

import java.net.URL;
import java.util.concurrent.atomic.AtomicBoolean;

public class OptionalMods {
    public static boolean hasImgui() {
        AtomicBoolean foundImgui = new AtomicBoolean(false);
        Piece.classLoader.sources.forEach((URL url) -> {
            if (url.getFile().toLowerCase().contains("imgui-integration")) { foundImgui.set(true); }
        });

        return foundImgui.get();
    }
}
