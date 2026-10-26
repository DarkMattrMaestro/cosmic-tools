package com.darkmattrmaestro.cosmic_tools.items;

import com.darkmattrmaestro.cosmic_tools.utils.*;

import finalforeach.cosmicreach.items.Item;
import finalforeach.cosmicreach.util.Identifier;
import io.github.puzzle.cosmic.item.AbstractCosmicItem;
import com.darkmattrmaestro.cosmic_tools.Constants;

/**
 * <p>
 * A spatula (or a wand) item that copies semi-contiguous patterns of similar blocks to the block in front of the
 * selected face.
 * </p>
 * <p>
 * Spatulas tend to be used for spreading things, and I imagine this tool is spreading blocks on the previous layer of
 * blocks. This is the reason for the name that, in retrospect, is slightly odd.
 * </p>
 */
public class Spatula extends AbstractCosmicItem {

    public Spatula(){
        super(Identifier.of(Constants.MOD_ID, "spatula"));
        addTexture(ItemModelType.ITEM_MODEL_3D, Identifier.of(Constants.MOD_ID, "spatula.png"));
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
        return "Spatula";
    }
}
