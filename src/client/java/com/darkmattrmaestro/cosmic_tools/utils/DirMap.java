package com.darkmattrmaestro.cosmic_tools.utils;

import finalforeach.cosmicreach.util.constants.Direction;

public class DirMap {
    public static String dirToString(Direction dir) {
        switch (dir) {
            case NEG_X -> {
                return "NegX";
            }
            case NEG_Y -> {
                return "NegY";
            }
            case NEG_Z -> {
                return "NegZ";
            }
            case POS_X -> {
                return "PosX";
            }
            case POS_Y -> {
                return "PosY";
            }
            case POS_Z -> {
                return "PosZ";
            }
            case null, default -> {
                return null;
            }
        }
    }
}
