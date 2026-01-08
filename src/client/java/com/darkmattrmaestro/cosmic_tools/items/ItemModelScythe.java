package com.darkmattrmaestro.cosmic_tools.items;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import finalforeach.cosmicreach.ClientSingletons;
import finalforeach.cosmicreach.GameAssetLoader;
import finalforeach.cosmicreach.blocks.BlockPosition;
import finalforeach.cosmicreach.entities.Entity;
import finalforeach.cosmicreach.entities.ItemEntity;
import finalforeach.cosmicreach.entities.player.Player;
import finalforeach.cosmicreach.gamestates.InGame;
import finalforeach.cosmicreach.items.Item;
import finalforeach.cosmicreach.items.ItemStack;
import finalforeach.cosmicreach.items.ItemThing;
import finalforeach.cosmicreach.rendering.RenderOrder;
import finalforeach.cosmicreach.rendering.items.*;
import finalforeach.cosmicreach.rendering.meshes.MeshData;
import finalforeach.cosmicreach.rendering.shaders.GameShader;
import finalforeach.cosmicreach.rendering.shaders.ItemShader;
import finalforeach.cosmicreach.util.GameTag;
import finalforeach.cosmicreach.world.Chunk;
import finalforeach.cosmicreach.world.Sky;
import finalforeach.cosmicreach.world.Zone;

import java.lang.ref.WeakReference;
import java.util.Objects;

public class ItemModelScythe extends ItemModel {
        private static final GameTag TAG_TOOL = GameTag.get("tool");
        Mesh mesh;
        Texture texture;
        Texture hueMaskTexture;
        GameShader program;
        private static final Matrix4 noRotMat = new Matrix4();
        private static final Camera itemCam2 = new OrthographicCamera(100.0F, 100.0F);
        private static final PerspectiveCamera heldItemCamera = new PerspectiveCamera();
        private static final Matrix4 tmpHeldMat4 = new Matrix4();
        private static final Color tintColor = new Color();
        private static final BlockPosition tmpBlockPos = new BlockPosition((Chunk)null, 0, 0, 0);
        private static final BlockPosition tmpBlockPos2 = new BlockPosition((Chunk)null, 0, 0, 0);
        Item item;
        private static final transient Color tmpColor;
        private Vector3 tmpVec;
        private static final IItemStackRenderParams itemStackRenderParams;
        private static Vector3 currentVelInfluence;

        public ItemModelScythe(ItemThing item) {
            this(item, (String)item.itemProperties.get("texture"), (String)item.itemProperties.get("colorMask"));
        }

        public ItemModelScythe(Item item, String texturePath, String hueMaskTexturePath) {
            this.program = (new MeshData(ItemShader.DEFAULT_ITEM_SHADER, RenderOrder.FULLY_TRANSPARENT)).getShader();
            this.tmpVec = new Vector3(0.0F, 0.0F, 0.0F);
            this.texture = GameAssetLoader.getTexture(texturePath);
            this.item = item;
            this.texture = ItemModelBuilder.flip(this.texture);
            if (hueMaskTexturePath != null) {
                this.hueMaskTexture = GameAssetLoader.getTexture(hueMaskTexturePath);
                this.hueMaskTexture = ItemModelBuilder.flip(this.hueMaskTexture);
            }

            if (item instanceof ItemThing) {
                ItemThing thing = (ItemThing)item;
                switch (thing.getModelTypeString()) {
                    case "base:item2D" -> this.mesh = ItemModelBuilder.build2DMesh();
                    case "base:item3D" -> this.mesh = ItemModelBuilder.build2_5DMesh(this.texture);
                }
            } else {
                this.mesh = ItemModelBuilder.build2_5DMesh(this.texture);
            }

        }

        public boolean isTool() {
            return this.item.hasTag(TAG_TOOL);
        }

        public void renderGeneric(Vector3 pos, Camera cam, Matrix4 tmpMatrix, boolean isSlot, IItemRenderParams renderParams) {
            if (isSlot) {
                tintColor.set(renderParams.getSlotColor());
            } else {
                Zone zone = InGame.getLocalPlayer().getZone();

                try {
                    Entity.setLightingColor(zone, pos, Sky.currentSky.currentAmbientColor, tintColor, tmpBlockPos, tmpBlockPos2);
                } catch (Exception var12) {
                    tintColor.set(renderParams.getSlotColor());
                }
            }

            this.program.bind(cam);
            this.program.bindOptionalBool("u_isItem", true);
            this.program.bindOptionalMatrix4("u_projViewTrans", cam.combined);
            this.program.bindOptionalMatrix4("u_modelMat", tmpMatrix);
            this.program.bindOptionalUniform4f("tintColor", tintColor);
            this.program.bindOptionalTexture("texDiffuse", this.texture, 0);
            tmpColor.set(Color.WHITE);
            if (this.hueMaskTexture != null) {
                this.program.bindOptionalTexture("u_hueMask", this.hueMaskTexture, 1);
                Item var8 = this.item;
                if (var8 instanceof ItemThing) {
                    ItemThing it = (ItemThing)var8;
                    if (renderParams instanceof IItemStackRenderParams) {
                        IItemStackRenderParams p = (IItemStackRenderParams)renderParams;
                        String key = (String)it.getProperty("colorMaskKey");
                        String source = (String)it.getProperty("colorMaskSource");
                        ItemStack itemStack = p.getItemStack();
                        if (itemStack != null && Objects.equals(source, "stackMetadata")) {
                            if (itemStack.stackMetadata.has(key)) {
                                int colorMetadata = itemStack.stackMetadata.getInt(key, -1);
                                tmpColor.set(colorMetadata);
                            } else {
                                Color defaultColor = (Color)it.getProperty("colorMaskDefaultColor");
                                if (defaultColor != null) {
                                    tmpColor.set(defaultColor);
                                }
                            }
                        }
                    }
                }
            } else {
                this.program.bindOptionalTexture("u_hueMask", ClientSingletons.transparentPixel.get(), 1);
            }

            this.program.bindOptionalUniform4f("u_colorMask", tmpColor);
            this.program.bindOptionalInt("isInSlot", isSlot ? 1 : 0);
            this.mesh.render(this.program.shader, 4);
            this.program.unbind();
        }

        public void render(Vector3 vector3, Camera slotCamera, Matrix4 matrix4, boolean b, boolean applyFog, ItemStack itemStack, Color slotColor) {
            itemStackRenderParams.setItemStack(itemStack);
            itemStackRenderParams.setSlotColor(slotColor);
            this.renderGeneric(this.tmpVec, slotCamera, noRotMat, true, itemStackRenderParams);
        }

        public void dispose(WeakReference<Item> itemRef) {
            this.texture.dispose();
            this.mesh.dispose();
        }

        public Camera getItemSlotCamera() {
            return itemCam2;
        }

        public void renderAsItemEntity(Vector3 pos, Camera entityCam, Matrix4 tmpMatrix, ItemEntity ie) {
            tmpMatrix.translate(0.5F, 0.2F, 0.5F);
            tmpMatrix.scale(0.7F, 0.7F, 0.7F);
            itemStackRenderParams.setItemStack(ie.getItemStack());
            this.renderGeneric(pos, entityCam, tmpMatrix, false, itemStackRenderParams);
        }

        public void renderAsHeldItem(Vector3 pos, Camera handCam, HeldItemRenderParams renderParams) {
            heldItemCamera.fieldOfView = 50.0F;
            heldItemCamera.viewportHeight = handCam.viewportHeight;
            heldItemCamera.viewportWidth = handCam.viewportWidth;
            heldItemCamera.near = handCam.near;
            heldItemCamera.far = handCam.far;
            heldItemCamera.update();
            tmpHeldMat4.idt();
            if (renderParams.popUpTimer > 0.0F) {
                float swing = (float)Math.pow((double)(renderParams.popUpTimer / renderParams.maxPopUpTimer), (double)2.0F);
                tmpHeldMat4.translate(0.0F, -1.0F * swing, 0.0F);
            }

            Player p = InGame.getLocalPlayer();
            Vector3 vel = p.getEntity().velocity;
            float clampedDt = Math.min(Gdx.graphics.getDeltaTime() * 0.5F, 1.0F);
            float l = vel.len();
            if (l == 0.0F) {
                l = 1.0F;
            }

            currentVelInfluence.scl(-l).lerp(vel, clampedDt).scl(-1.0F / l);
            currentVelInfluence.clamp(0.0F, 0.05F);
            tmpHeldMat4.translate(currentVelInfluence);
            tmpHeldMat4.translate(1.65F, -1.25F, -2.5F);
            tmpHeldMat4.rotate(Vector3.Y, -75.0F);
            tmpHeldMat4.translate(-0.25F, -0.25F, -0.25F);
            if (renderParams.swingTimer > 0.0F) {
                float swing = renderParams.swingTimer / renderParams.maxSwingTimer;
                swing = 1.0F - (float)Math.pow((double)(swing - 0.5F), (double)2.0F) / 0.25F;
                tmpHeldMat4.rotate(Vector3.Z, 90.0F * swing);
                float st = -swing;
                tmpHeldMat4.translate(st * 2.0F, st, 0.0F);
            }

            if (this.isTool()) {
                tmpHeldMat4.translate(0.6F, 0.0F, 0.0F);
                tmpHeldMat4.translate(0.0F, -0.2F, 0.0F);
                tmpHeldMat4.rotate(Vector3.Z, 20.0F);
                tmpHeldMat4.rotate(Vector3.X, 15.0F);
            }

            Gdx.gl.glDisable(2929);
            this.renderGeneric(pos, heldItemCamera, tmpHeldMat4, false, renderParams);
            Gdx.gl.glEnable(2929);
        }

        static {
            noRotMat.setTranslation(0.0F, -1.0F, 0.0F);
            itemCam2.position.set(0.0F, 0.0F, 2.0F);
            itemCam2.lookAt(0.0F, 0.0F, 0.0F);
            ((OrthographicCamera)itemCam2).zoom = 0.027F;
            itemCam2.update();
            tmpColor = new Color();
            itemStackRenderParams = new IItemStackRenderParams() {
                private final Color slotColor;
                ItemStack itemStack;

                {
                    this.slotColor = Color.WHITE.cpy();
                }

                public Color getSlotColor() {
                    return this.slotColor;
                }

                public ItemStack getItemStack() {
                    return this.itemStack;
                }

                public void setItemStack(ItemStack itemStack) {
                    this.itemStack = itemStack;
                }
            };
            currentVelInfluence = new Vector3();
        }
    }

