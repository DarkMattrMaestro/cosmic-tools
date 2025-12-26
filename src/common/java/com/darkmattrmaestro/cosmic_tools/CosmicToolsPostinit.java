package com.darkmattrmaestro.cosmic_tools;

//import dev.puzzleshq.puzzleloader.loader.mod.entrypoint.client.ClientPostModInit;
import dev.puzzleshq.puzzleloader.loader.mod.entrypoint.common.PostModInit;
import finalforeach.cosmicreach.items.Item;
import io.github.puzzle.cosmic.item.AbstractCosmicItem;
import com.darkmattrmaestro.cosmic_tools.items.Spatula;

@SuppressWarnings("unused")
public class CosmicToolsPostinit implements PostModInit {
    public static Item spatula;
    @Override
    public void onPostInit() {
        AbstractCosmicItem spat = new Spatula();
        spatula = spat;
        AbstractCosmicItem.register(spat);
    }
}
