package com.darkmattrmaestro.cosmic_tools.utils;

import finalforeach.cosmicreach.audio.SoundManager;

public class SoundUtils {
    public static void successFailSound(boolean success) {
        if (success) {
            SoundManager.INSTANCE.playSound("cosmic_tools:sounds/success.ogg", 1, 1, 0);
        } else {
            SoundManager.INSTANCE.playSound("cosmic_tools:sounds/error.ogg", 1, 1, 0);
        }
    }

    public static void scrollSound() {
        SoundManager.INSTANCE.playSound("cosmic_tools:sounds/scroll.ogg", 1, 1, 0);
    }
}
