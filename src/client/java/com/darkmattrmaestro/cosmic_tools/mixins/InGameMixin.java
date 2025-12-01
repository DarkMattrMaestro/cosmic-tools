package com.darkmattrmaestro.cosmic_tools.mixins;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.darkmattrmaestro.cosmic_tools.utils.CosmicToolsInputProcessor;
import finalforeach.cosmicreach.gamestates.InGame;
import finalforeach.cosmicreach.items.ItemStack;
import finalforeach.cosmicreach.ui.UI;
import com.darkmattrmaestro.cosmic_tools.items.Spatula;
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

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lfinalforeach/cosmicreach/ui/UI;render()V"))
    public void renderOverlayUI(CallbackInfo ci) {
        if (cosmictools$sr2 == null) {
            cosmictools$sr2 = new ShapeRenderer();
        }

        ItemStack selected = UI.hotbar.getSelectedItemStack();

        if(selected != null && selected.getItem() instanceof Spatula) {
            // Spatula Paste Highlighting
            if (Spatula.getSelection() != null) {
                Spatula.copyBlocks.draw(cosmictools$sr2, Spatula.blockAxis.axis.toVector3(), Spatula.playerHasEnoughItems() ? new Color(0.64f, 0.64f, 0.64f, 1f) : new Color(1f, 0.2f, 0.2f, 1f));
            }
        }

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
