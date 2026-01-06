package com.darkmattrmaestro.cosmic_tools.utils;

import finalforeach.cosmicreach.blocks.BlockPosition;
import finalforeach.cosmicreach.blocks.BlockState;
import finalforeach.cosmicreach.entities.player.Player;
import finalforeach.cosmicreach.items.ItemSlot;

import java.util.HashMap;

public class PasteUtils {
    /**
     * Check that the player has enough of the correct item to paste all blocks.
     *
     * @return <code>true</code> if the player has enough blocks, else <code>false</code>
     */
    public static boolean playerHasEnoughItems(Player player, BlockMappings blockMappings) {
        // Sum required items
        HashMap<String, Integer> requiredItems = sumRequiredItems(blockMappings);

        // Subtract sum of items owned by the player
        player.inventory.forEachSlot(((ItemSlot itemSlot) -> {
            if (itemSlot.getItem() == null) { return; }

            String itemID = itemSlot.getItem().getID();

            if (requiredItems.containsKey(itemID)) {
                if (requiredItems.get(itemID) - itemSlot.getItemAmount() <= 0) {
                    requiredItems.remove(itemID);
                } else {
                    requiredItems.put(itemID, requiredItems.get(itemID) - itemSlot.getItemAmount());
                }
            }
        }));

        // Check if the player has enough items
        return requiredItems.isEmpty();
    }

    public static HashMap<String, Integer> sumRequiredItems(BlockMappings blockMappings) {
        HashMap<String, Integer> requiredItems = new HashMap<>();
        for (BlockState blockState: blockMappings.blockStates()) {
            String item = blockState.getItem().getID();
            if (requiredItems.containsKey(item)) {
                requiredItems.put(item, requiredItems.get(item) + 1);
            } else {
                requiredItems.put(item, 1);
            }
        }

        return requiredItems;
    }
}
