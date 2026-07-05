package com.darkmattrmaestro.cosmic_tools.items;

import com.darkmattrmaestro.cosmic_tools.Constants;
import com.darkmattrmaestro.cosmic_tools.utils.Selection;
import finalforeach.cosmicreach.blocks.BlockPosition;
import finalforeach.cosmicreach.items.Item;
import finalforeach.cosmicreach.util.Identifier;
import io.github.puzzle.cosmic.item.AbstractCosmicItem;

import static java.lang.Math.abs;

/**
 *
 */
public class SelectionWand extends AbstractCosmicItem {

    public SelectionWand(){
        super(Identifier.of(Constants.MOD_ID, "selection_wand"));
        addTexture(ItemModelType.ITEM_MODEL_3D, Identifier.of(Constants.MOD_ID, "selection_wand.png"));
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
        return "Selection Wand";
    }
}
