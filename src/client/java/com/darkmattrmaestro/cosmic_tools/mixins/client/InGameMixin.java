package com.darkmattrmaestro.cosmic_tools.mixins.client;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.darkmattrmaestro.cosmic_tools.Constants;
import com.darkmattrmaestro.cosmic_tools.items.ClientSelectionWand;
import com.darkmattrmaestro.cosmic_tools.items.ClientSpatula;
import com.darkmattrmaestro.cosmic_tools.items.SelectionWand;
import com.darkmattrmaestro.cosmic_tools.utils.*;
import finalforeach.cosmicreach.gamestates.GameState;
import finalforeach.cosmicreach.gamestates.InGame;
import finalforeach.cosmicreach.items.ItemStack;
import finalforeach.cosmicreach.ui.UI;
import finalforeach.cosmicreach.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGame.class)
public class InGameMixin {
    @Shadow InputProcessor inputMultiplexer;
    @Unique
    private static ShapeRenderer cosmictools$sr2;

    @Inject(method = "renderWorld", at = @At(value = "TAIL"))
    public void renderOverlayWorld(CallbackInfo ci) {
        if (Selection.rawWorldCamera == null) { Selection.rawWorldCamera = GameState.IN_GAME.getWorldCamera(); }

        if (cosmictools$sr2 == null) { cosmictools$sr2 = new ShapeRenderer(); }

        ItemStack selected = UI.hotbar.getSelectedItemStack();
        if (selected == null) { return; }

        // In-world rendering
        if(Identifier.of(Constants.MOD_ID, "selection_wand").toString().equals(selected.getItem().getID())) {
            ClientSelectionWand.renderOverlay(cosmictools$sr2, 0.64f);
        }

        Gdx.gl.glEnable(3042);
        Gdx.gl.glDepthFunc(519);
        Gdx.gl.glBlendFunc(770, GL20.GL_ONE_MINUS_SRC_ALPHA);

        // Spectral rendering

        if(BlockSelectionUtil.isInActiveGame() && Identifier.of(Constants.MOD_ID, "spatula").toString().equals(selected.getItem().getID())) {
            // Spatula Paste Highlighting
            ClientSpatula clientSpatula = new ClientSpatula(selected.getItem().getID());
            if (GameState.currentGameState.getClass() == InGame.class && clientSpatula.getHallucination(InGame.getLocalPlayer()) != null) {
                Color colour = clientSpatula.playerHasEnoughItems(InGame.getLocalPlayer()) ? new Color(0.64f, 0.64f, 0.64f, 1f) : new Color(1f, 0.2f, 0.2f, 1f);
                ClientHallucination.draw(clientSpatula.copyBlocks, cosmictools$sr2, clientSpatula.blockAxis.axis.toVector3(), colour);
            }
        }

        if(Identifier.of(Constants.MOD_ID, "selection_wand").toString().equals(selected.getItem().getID())) {
            // Selection Wand Highlighting
            ClientSelectionWand.renderOverlay(cosmictools$sr2, 0.36f);
        }

        if(Identifier.of(Constants.MOD_ID, "selection_wand").toString().equals(selected.getItem().getID())) {
            // Selection Wand Highlighting
            ClientSelectionWand.renderOverlay(cosmictools$sr2, 0.36f);
        }

        Gdx.gl.glClear(256);

    }

    @Inject(method = "create", at = @At(value = "TAIL"))
    public void create(CallbackInfo ci) {
        InputMultiplexer multiplexer = new InputMultiplexer();
        multiplexer.addProcessor(new CosmicToolsInputProcessor());
        multiplexer.addProcessor(this.inputMultiplexer); // Cosmic Reach's default processors
        this.inputMultiplexer = multiplexer;
        Gdx.input.setInputProcessor(this.inputMultiplexer);
    }
}
