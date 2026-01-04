package com.darkmattrmaestro.cosmic_tools.mixins.common;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.*;
import com.darkmattrmaestro.cosmic_tools.Constants;
import finalforeach.cosmicreach.GameAssetLoader;
import finalforeach.cosmicreach.items.recipes.CraftingRecipes;
import finalforeach.cosmicreach.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CraftingRecipes.class)
public class CraftingRecipesMixin {
    @Shadow
    private static void loadRecipe(Identifier identifier, Json json, JsonValue jsonValue) {  }

    @Inject(method = "loadCraftingRecipes", at = @At(value = "TAIL"))
    private static void loadCraftingRecipes(Queue<Runnable> loadingQueue, CallbackInfo ci) {
//        Json json = new Json();
//        loadingQueue.addLast((Runnable)() -> {
//            try {
//                FileHandle spatulaHandle = GameAssetLoader.loadAsset("cosmic_tools:recipes/crafting/tools/spatula.json");
//                JsonValue spatulaJson = GameAssetLoader.loadJson(spatulaHandle);
//                loadRecipe(Identifier.of(Constants.MOD_ID, "spatula"), json, spatulaJson);
//            } catch (Exception ex) {
//                throw new RuntimeException("Error parsing recipes: " + "cosmic_tools:recipes/crafting/tools/spatula.json", ex);
//            }
//        });
    }
}
