package com.darkmattrmaestro.cosmic_tools.items;

import com.darkmattrmaestro.cosmic_tools.Constants;
import finalforeach.cosmicreach.items.Item;
import finalforeach.cosmicreach.util.Identifier;
import io.github.puzzle.cosmic.item.AbstractCosmicItem;

/**
 *
 */
public class DisplayWand extends AbstractCosmicItem {

    public DisplayWand(){
        super(Identifier.of(Constants.MOD_ID, "display_wand"));
        addTexture(ItemModelType.ITEM_MODEL_3D, Identifier.of(Constants.MOD_ID, "display_wand.png"));
    }

    @Override
    public boolean isTool() {
        return true;
    }

    @Override
    public boolean canMergeWith(Item item) {
        return false;
    }

    @Override
    public String toString() {
        return id.toString();
    }

    @Override
    public String getName() {
        return "Display Wand";
    }
}
