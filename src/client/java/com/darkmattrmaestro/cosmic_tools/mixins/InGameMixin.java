package com.darkmattrmaestro.cosmic_tools.mixins;

import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
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
            // Spatula

//            Constants.LOGGER.warn(BlockSelectionUtil.getBlockSideLookingAtFar(20));

//            // Selection to copy
//            if (Spatula.getSelection() != null) {
//                Spatula.getSelection().draw(cosmictools$sr2, new Color(0, 0.7882352941f, 0, 0.25f), new Color(0.2745098039f, 0.9098039216f, 0.2745098039f, 1));
//            }
            // Where to paste
            if (Spatula.getPasteSelection(true) != null) {
                Spatula.getPasteSelection(false).draw(cosmictools$sr2, new Color(0.7882352941f, 0, 0, 0.25f), new Color(0.9098039216f, 0.2745098039f, 0.2745098039f, 1));
            }
        }

    }

//    @Inject(method = "renderWorld", at = @At(value = "HEAD"), cancellable = true)
//    protected void renderWorld(Zone playerZone, CallbackInfo ci) {
////        boolean usePostProcessing = false;
////        boolean useUnderwaterPostProcessing = false;
////        boolean useUnderLavaPostProcessing = false;
////        if (localPlayer != null && localPlayer.getEntity() != null) {
////            playerHeadPosition.set(localPlayer.getPosition());
////            playerHeadPosition.add(localPlayer.getEntity().viewPositionOffset);
////            BlockState headBlock = playerZone.getBlockState(playerHeadPosition);
////            if (headBlock != null) {
////                useUnderwaterPostProcessing = headBlock.getBlockId().equals("base:water");
////                useUnderLavaPostProcessing = headBlock.getBlockId().equals("base:lava");
////            }
////        }
////
////        usePostProcessing = useUnderLavaPostProcessing || useUnderwaterPostProcessing;
////        Sky sky = Sky.getCurrentSky(playerZone);
////        sky.update();
////        if (usePostProcessing) {
////            this.postProcessing.begin();
////            if (useUnderwaterPostProcessing) {
////                sky.fogDensity *= 1.25F;
////                this.postProcessing.shader.settings = UnderwaterShader.WATER_SETTINGS;
////            }
////
////            if (useUnderLavaPostProcessing) {
////                sky.fogDensity *= 4.0F;
////                this.postProcessing.shader.settings = UnderwaterShader.LAVA_SETTINGS;
////            }
////        }
////
////        ScreenUtils.clear(sky.currentSkyColor, true);
////        this.targetFovOffset = 0.0F;
////        if (localPlayer.isSprinting) {
////            this.targetFovOffset += 7.5F;
////        }
////
////        this.fovOffset = TickRunner.INSTANCE.partTickLerp(this.fovOffset, this.targetFovOffset, 1.0F);
////        PerspectiveCamera var10000 = rawWorldCamera;
////        var10000.fieldOfView += this.fovOffset;
////        this.viewport.apply();
////        sky.drawSky(rawWorldCamera);
////        GameSingletons.zoneRenderer.render(playerZone, rawWorldCamera);
////        Gdx.gl.glDepthMask(true);
////        Array<IRenderable> allRenderableBlockEntities = playerZone.allRenderableBlockEntities;
////
////        for(int i = 0; i < allRenderableBlockEntities.size; ++i) {
////            IRenderable renderable = (IRenderable)allRenderableBlockEntities.get(i);
////            if (renderable != null) {
////                renderable.onRender(rawWorldCamera);
////            }
////        }
////
////        Array<Entity> entities = playerZone.getAllEntities();
////
////        for(int i = 0; i < entities.size; ++i) {
////            Entity e = (Entity)entities.get(i);
////            if (e != null) {
////                e.render(rawWorldCamera);
////                e.clientUpdate();
////            }
////        }
////
////        this.blockSelection.render(rawWorldCamera);
////        this.gameParticles.render(rawWorldCamera, !TickRunner.INSTANCE.isRunning() && GameSingletons.isHost ? 0.0F : Gdx.graphics.getDeltaTime());
////        if (UI.renderDebugInfo) {
////            drawEntityDebugBoundingBoxes(playerZone);
////        }
////
////        if (localPlayer != null) {
////            ItemStack heldItemStack = UI.hotbar.getSelectedItemStack();
////            Entity e = localPlayer.getEntity();
////            this.heldItemPosition.set(e.getPosition()).add(0.0F, 1.25F, 0.0F);
////            ItemRenderer.renderHeldItem(this.heldItemPosition, heldItemStack, rawWorldCamera);
////        }
////
////        if (usePostProcessing) {
////            this.postProcessing.end();
////            this.postProcessing.render();
////        }
//
//        ci.cancel();
//    }

//    @Inject(method = "create", at = @At(value = "TAIL"))
//    public void create(CallbackInfo ci) {
//        InputMultiplexer multiplexer = new InputMultiplexer();
//        multiplexer.addProcessor(new EclipticInputProcessor());
//        multiplexer.addProcessor(this.inputMultiplexer); // Cosmic Reach's default processors
//        this.inputMultiplexer = multiplexer;
//        Gdx.input.setInputProcessor(this.inputMultiplexer);
//    }
}
