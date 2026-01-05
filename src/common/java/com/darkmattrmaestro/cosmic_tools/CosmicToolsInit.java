package com.darkmattrmaestro.cosmic_tools;

import com.badlogic.gdx.utils.Json;
import com.darkmattrmaestro.cosmic_tools.items.Spatula;
import com.darkmattrmaestro.cosmic_tools.packets.PasteBlocksPacket;
import dev.puzzleshq.puzzleloader.cosmic.game.GameRegistries;
import dev.puzzleshq.puzzleloader.loader.mod.entrypoint.common.ModInit;
import finalforeach.cosmicreach.GameAssetLoader;
import finalforeach.cosmicreach.items.recipes.CraftingRecipes;
import finalforeach.cosmicreach.networking.GamePacket;
import finalforeach.cosmicreach.util.Identifier;
import io.github.puzzle.cosmic.impl.event.EventLoadingQueue;
import io.github.puzzle.cosmic.item.AbstractCosmicItem;
import net.neoforged.bus.api.SubscribeEvent;

public class CosmicToolsInit implements ModInit {
    public CosmicToolsInit() {
        GameRegistries.COSMIC_EVENT_BUS.register(this);
    }

    @Override
    public void onInit() {}

    @SubscribeEvent
    public void onEvent(EventLoadingQueue event) {
        event.registerToQueue(() -> {
            AbstractCosmicItem.register(new Spatula());
            GamePacket.registerPacket(PasteBlocksPacket.class);

            CraftingRecipes.loadRecipe(Identifier.of(Constants.MOD_ID, "recipes/crafting/tools/spatula.json"), new Json(), GameAssetLoader.loadJson(Identifier.of(Constants.MOD_ID, "recipes/crafting/tools/spatula.json")));
        });
    }
}