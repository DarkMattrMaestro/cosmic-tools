package com.darkmattrmaestro.cosmic_tools.utils;

import finalforeach.cosmicreach.items.Item;
import finalforeach.cosmicreach.items.ItemBlock;

public class ItemBlockComparison {
    public static boolean compareItemBlockID(Item item, String blockID) {
        if (!(item instanceof ItemBlock)) { return false; }

        return ((ItemBlock) item).getBlockState().getBlockId().equals(blockID);
    }
}
