package com.darkmattrmaestro.cosmic_tools.items;

import com.darkmattrmaestro.cosmic_tools.Constants;
import finalforeach.cosmicreach.blocks.BlockState;
import finalforeach.cosmicreach.items.Item;
import finalforeach.cosmicreach.util.Identifier;
import io.github.puzzle.cosmic.item.AbstractCosmicItem;

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
public class Scythe extends AbstractCosmicItem {

    public Scythe(){
        super(Identifier.of(Constants.MOD_ID, "scythe"));
        addTexture(ItemModelType.ITEM_MODEL_3D, Identifier.of(Constants.MOD_ID, "scythe.png"));
    }

    @Override
    public boolean isTool() {
        return true;
    }

    @Override
    public boolean canTargetBlockForBreaking(BlockState blockState) {
        return false;
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
        return "Scythe";
    }
}
