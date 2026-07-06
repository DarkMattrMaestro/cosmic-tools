package com.darkmattrmaestro.cosmic_tools.items;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.darkmattrmaestro.cosmic_tools.Constants;
import com.darkmattrmaestro.cosmic_tools.packets.PasteBlocksPacket;
import com.darkmattrmaestro.cosmic_tools.utils.*;
import finalforeach.cosmicreach.audio.SoundManager;
import finalforeach.cosmicreach.blocks.BlockPosition;
import finalforeach.cosmicreach.blocks.BlockState;
import finalforeach.cosmicreach.blocks.MissingBlockStateResult;
import finalforeach.cosmicreach.entities.player.Player;
import finalforeach.cosmicreach.gameevents.blockevents.BlockEventTrigger;
import finalforeach.cosmicreach.gamestates.GameState;
import finalforeach.cosmicreach.gamestates.InGame;
import finalforeach.cosmicreach.items.ItemStack;
import finalforeach.cosmicreach.networking.client.ClientNetworkManager;
import finalforeach.cosmicreach.ui.UI;
import finalforeach.cosmicreach.util.Identifier;
import finalforeach.cosmicreach.world.Chunk;
import finalforeach.cosmicreach.world.Zone;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static com.darkmattrmaestro.cosmic_tools.utils.BlockSelectionUtil.*;
import static com.darkmattrmaestro.cosmic_tools.utils.ChatUtils.blockPosToString;
import static com.darkmattrmaestro.cosmic_tools.utils.ChatUtils.sendMsg;
import static java.lang.Math.abs;

public class ClientSelectionWand {
    private static float reachDist = 12.0f;

    private static BlockPosition firstPos = null;
    private static BlockPosition secondPos = null;

    private static BlockPosition pasteStartPos = null;

    private static Volume copiedVolume = null;

    private static boolean invertX = false;
    private static boolean invertY = false;
    private static boolean invertZ = false;
    private static final Color invertColour = new Color(1, 0, 0, 0.8f);
    private static final float invertWidth = 0.064f;

    public static Selection getFirstSelection() {
        if (firstPos == null) return null;
        return Selection.of(firstPos, firstPos);
    }

    public static Selection getSecondSelection() {
        if (secondPos == null) return null;
        return Selection.of(secondPos, secondPos);
    }

    public static Selection getSelection() {
        if (firstPos == null || secondPos == null) return null;
        return Selection.of(firstPos, secondPos);
    }

    public static Selection getPasteSelection(){
        if(pasteStartPos == null || firstPos == null || secondPos == null) return null;
        return Selection.of(firstPos, secondPos).reposition(pasteStartPos);
    }

    public static Selection getPastePositionSelection(){
        if(pasteStartPos == null) return null;
        return Selection.of(pasteStartPos, pasteStartPos);
    }

    public static void viewDirShiftPastePositionIn(int shift, int rotation) {
        Vector3Int offset = (rotation == 2 ? upViewDir() : rotation == 1 ? rightViewDir() : primaryViewDir()).mult(shift);
        pasteStartPos = pasteStartPos.getOffsetBlockPos(offset.x, offset.y, offset.z);
    }

    public static boolean onMousePressed(int button) {
        if (GameState.currentGameState.getClass() != InGame.class) { return false; }

        ItemStack selected = UI.hotbar.getSelectedItemStack();
        if (selected == null || !Identifier.of(Constants.MOD_ID, "selection_wand").toString().equals(selected.getItem().getID())) {
            return false;
        }

        int shift = Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT) ? 1 : Gdx.input.isKeyPressed(Input.Keys.ALT_LEFT) ? 2 : 0;
        if (button == Input.Buttons.FORWARD) {
            viewDirShiftPastePositionIn(1, shift);
        } else if (button == Input.Buttons.BACK) {
            viewDirShiftPastePositionIn(-1, shift);
        }

//        if(button == Input.Buttons.RIGHT) {
//            // Paste
//            Selection selection = ClientSelectionWand.getSelection();
//            if (selection != null && pasteStartPos != null) {
//                sendMsg("Pasting " + selection.nBlocks() + " blocks");
//                FillAction.of(selection, pasteStartPos, BlockState.getInstance("base:wood_planks", MissingBlockStateResult.MISSING_OBJECT)).paste(InGame.getLocalPlayer().getZone(), invertX, invertY, invertZ);
//            } else {
//                sendMsg("Selection areas not selected");
//            }
//        }

//        ClientSelectionWand clientSpatula = new ClientSelectionWand(selected.getItem().getID());
//
//        if(!BlockSelectionUtil.doesCollideFar(clientSpatula.reachDist) || !BlockSelectionUtil.isInActiveGame()) { return false; }
//
//        if(button == Input.Buttons.RIGHT) {
//            // Right Click => paste
//            clientSpatula.getHallucination(InGame.getLocalPlayer()); // Update copyBlocks
//            if (clientSpatula.playerHasEnoughItems(InGame.getLocalPlayer())) {
//                List<Chunk> chunksToUpdate = new ArrayList<>();
//                String triggerName = "onPlace";
//
//                Zone zone = InGame.getLocalPlayer().getZone();
//                for (BlockPosition blockPos : clientSpatula.copyBlocks.blocks) {
//                    BlockEventTrigger[] triggers = blockPos.getBlockState().getTrigger(triggerName);
//
//                    Vector3Int hallucinatedPos = (new Vector3Int(blockPos.getGlobalX(), blockPos.getGlobalY(), blockPos.getGlobalZ())).add(clientSpatula.blockAxis.axis);
//
//                    zone.setBlockState(blockPos.getBlockState(), hallucinatedPos.x, hallucinatedPos.y, hallucinatedPos.z);
//                    Chunk c = zone.getChunkAtBlock(hallucinatedPos.x, hallucinatedPos.y, hallucinatedPos.z);
//                    if (!chunksToUpdate.contains(c)) {
//                        chunksToUpdate.add(c);
//                    }
//
////                    if (triggers != null) {
////                        if (GameSingletons.isHost) {
////                            BlockEventArgs args = new BlockEventArgs();
////                            args.srcPlayer = InGame.getLocalPlayer();
////                            args.srcBlockState = blockPos.getBlockState();
////                            args.zone = zone;
////                            args.blockPos = blockPos;
////    //                        for (GameEventTrigger trigger: triggers) {
////    //                            Constants.LOGGER.warn("Triggers: {}", trigger.getAction());
////    //                        }
////                            args.run(triggers);
////                            args.runScheduledTriggers();
////                        }
////                    }
//
//                }
//
//                if (ClientNetworkManager.isConnected()) {
//                    List<BlockPosition> offsetBlocks = clientSpatula.copyBlocks.blocks.stream().map((BlockPosition blockPos) -> {
//                        return blockPos.getOffsetBlockPos(clientSpatula.blockAxis.axis.x, clientSpatula.blockAxis.axis.y, clientSpatula.blockAxis.axis.z);
//                    }).toList();
//                    ClientNetworkManager.sendAsClient(new PasteBlocksPacket(BlockMappings.ofZonePositions(zone, offsetBlocks)));
//                }
//
//                ChunkUtils.remesh(chunksToUpdate, zone);
//
//                SoundManager.INSTANCE.playSound("cosmic_tools:sounds/items/spatula-place.ogg", 1, 1, 0);
//            }
//        }
        return false;
    }

    public static boolean copyBlocks() {
        Selection selection = getSelection();
        if (selection == null) {
            Constants.LOGGER.warn("Tried to copy but no selection was made!");
            return false;
        }

        copiedVolume = Volume.of(selection.sizeX(), selection.sizeY(), selection.sizeZ());

        int minX = selection.minPos.x;
        int minY = selection.minPos.y;
        int minZ = selection.minPos.z;
        for (int x = 0; x < selection.sizeX(); x++) {
            for (int y = 0; y < selection.sizeY(); y++) {
                for (int z = 0; z < selection.sizeZ(); z++) {
                    BlockState blockState = BlockPosition.ofGlobal(
                            InGame.getLocalPlayer().getZone(),
                            minX + x,
                            minY + y,
                            minZ + z
                    ).getBlockState();

                    copiedVolume.setBlock(x, y, z, blockState);
                }
            }
        }

        return true;
    }

    public static BlockState getMappedCoords(int x, int y, int z) {
        int mappedX = invertX ? (copiedVolume.getSizeX() - 1 - x) : x;
        int mappedY = invertY ? (copiedVolume.getSizeY() - 1 - y) : y;
        int mappedZ = invertZ ? (copiedVolume.getSizeZ() - 1 - z) : z;

        return copiedVolume.getBlock(mappedX, mappedY, mappedZ);
    }

    public static void pasteBlocks() {
        if (pasteStartPos == null) {
            sendMsg("Tried to paste but no paste location was set!");
            return;
        }

        List<Chunk> chunksToUpdate = new ArrayList<>();
        String triggerName = "onPlace";

        Zone zone = InGame.getLocalPlayer().getZone();

        for (int x = 0, pasteX = pasteStartPos.getGlobalX(); x < copiedVolume.getSizeX(); x++, pasteX++) {
            for (int y = 0, pasteY = pasteStartPos.getGlobalY(); y < copiedVolume.getSizeY(); y++, pasteY++) {
                for (int z = 0, pasteZ = pasteStartPos.getGlobalZ(); z < copiedVolume.getSizeZ(); z++, pasteZ++) {
                    BlockState blockState = getMappedCoords(x, y, z);

                    if (blockState.getParamDirection("direction") != null && (
                            (invertX && blockState.getParamDirection("direction").isXAxis()) ||
                            (invertY && blockState.getParamDirection("direction").isYAxis()) ||
                            (invertZ && blockState.getParamDirection("direction").isZAxis())
                    )) {
                        blockState = blockState.getVariantWithParam("direction", DirMap.dirToString(blockState.getParamDirection("direction").getOpposite()));
                    }

                    zone.setBlockState(blockState, pasteX, pasteY, pasteZ);
                    Chunk c = zone.getChunkAtBlock(pasteX, pasteY, pasteZ);
                    if (!chunksToUpdate.contains(c)) {
                        chunksToUpdate.add(c);
                    }

                    BlockEventTrigger[] triggers = blockState.getTrigger(triggerName);
//                    if (triggers != null) {
//                        if (GameSingletons.isHost) {
//                            BlockEventArgs args = new BlockEventArgs();
//                            args.srcPlayer = InGame.getLocalPlayer();
//                            args.srcBlockState = blockPos.getBlockState();
//                            args.zone = zone;
//                            args.blockPos = blockPos;
//    //                        for (GameEventTrigger trigger: triggers) {
//    //                            Constants.LOGGER.warn("Triggers: {}", trigger.getAction());
//    //                        }
//                            args.run(triggers);
//                            args.runScheduledTriggers();
//                        }
//                    }
                }
            }
        }

        // TODO: Make compatible with server
//        if (ClientNetworkManager.isConnected()) {
//            List<BlockPosition> offsetBlocks = clientSpatula.copyBlocks.blocks.stream().map((BlockPosition blockPos) -> {
//                return blockPos.getOffsetBlockPos(clientSpatula.blockAxis.axis.x, clientSpatula.blockAxis.axis.y, clientSpatula.blockAxis.axis.z);
//            }).toList();
//            ClientNetworkManager.sendAsClient(new PasteBlocksPacket(BlockMappings.ofZonePositions(zone, offsetBlocks)));
//        }

        ChunkUtils.remesh(chunksToUpdate, zone);

        SoundManager.INSTANCE.playSound("cosmic_tools:sounds/success.ogg", 1, 1, 0);
    }

    public static void duplicateBlocks() {
        if (copyBlocks()) {
            pasteBlocks();
        }
    }

    public static boolean onKeyPressed(int keycode) {
        BlockPosition pos = BlockSelectionUtil.getBlockLookingAtFar(20); // TODO: vary reach dist
        if (pos == null) {
            return false;
        }

        switch (keycode) {
            case Input.Keys.D -> {
                if (Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT)) {
                    // Duplicate selection
                    duplicateBlocks();
                }
                return true;
            }
            case Input.Keys.NUMPAD_DOT -> {
                if (Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT)) {
                    // Sey first position
                    sendMsg("First position set to " + blockPosToString(pos));
                    firstPos = pos.copy();
                } else if (Gdx.input.isKeyPressed(Input.Keys.ALT_LEFT)) {
                    // Sey second position
                    sendMsg("Second position set to " + blockPosToString(pos));
                    secondPos = pos.copy();
                } else {
                    // Set paste origin
                    sendMsg("Set paste origin to " + blockPosToString(pos));
                    pasteStartPos = pos.copy();
                }
            }
            case Input.Keys.NUMPAD_1 -> { // Flip Z axis
                invertX = !invertX;
                sendMsg("X axis is now " + (invertX ? "" : "NOT ") + "inverted");
            }
            case Input.Keys.NUMPAD_2 -> { // Flip Z axis
                invertY = !invertY;
                sendMsg("Y axis is now " + (invertY ? "" : "NOT ") + "inverted");
            }
            case Input.Keys.NUMPAD_3 -> { // Flip Z axis
                invertZ = !invertZ;
                sendMsg("Z axis is now " + (invertZ ? "" : "NOT ") + "inverted");
            }
            default -> {
                return false;
            }
        }

        return true;
    }

    public static void renderOverlay(ShapeRenderer shapeRenderer) {
        DrawInfo[] drawInfos = {
                new DrawInfo( // Draw selection to copy
                        ClientSelectionWand::getSelection,
                        new Color(0, 0.7882352941f, 0.7882352941f, 0.25f),
                        new Color(0.2745098039f, 0.2745098039f, 0.9098039216f, 1),
                        0
                ),
                new DrawInfo( // Draw first position
                        ClientSelectionWand::getFirstSelection,
                        new Color(0, 0.7882352941f, 0.7882352941f, 0.2f),
                        new Color(0.25f, 0.25f, 0.75f, 1),
                        0.01f
                ),
                new DrawInfo( // Draw second position
                        ClientSelectionWand::getSecondSelection,
                        new Color(0, 0.7882352941f, 0.7882352941f, 0.2f),
                        new Color(0.10f, 0.10f, 0.40f, 1),
                        0.01f
                ),
                new DrawInfo( // Draw paste area
                        ClientSelectionWand::getPasteSelection,
                        new Color(0.64f, 0.64f, 0.64f, 0.25f),
                        new Color(0.2745098039f, 0.9098039216f, 0.2745098039f, 1),
                        0
                ),
                new DrawInfo( // Draw paste origin
                        ClientSelectionWand::getPastePositionSelection,
                        new Color(0, 0.8f, 0, 0.64f),
                        new Color(0.2745098039f, 0.9098039216f, 0.2745098039f, 1),
                        0.01f
                )
        };

        for (DrawInfo drawInfo : drawInfos) {
            Selection selection = drawInfo.selectionSupplier.get();
            if (selection != null) {
                selection.setCustomInflate(drawInfo.customInflate);
                selection.draw(shapeRenderer, drawInfo.fillColor, drawInfo.borderColor);
            }
        }

        Selection pasteSelection = ClientSelectionWand.getPasteSelection();
        if (pasteSelection != null) {
            boolean[] inverts = new boolean[]{invertX, invertY, invertZ};
            for (int dir = 0; dir < 3; dir++) {
                if (!inverts[dir]) { continue; }
                shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
                shapeRenderer.setColor(invertColour);
                shapeRenderer.box(
                        dir == 0 ? pasteSelection.minPos.x - invertWidth : pasteSelection.minPos.x + (float) pasteSelection.sizeX() / 2 - 0.25f,
                        dir == 1 ? pasteSelection.minPos.y - invertWidth : pasteSelection.minPos.y + (float) pasteSelection.sizeY() / 2 - 0.25f,
                        dir == 2 ? pasteSelection.minPos.z : pasteSelection.minPos.z + (float) pasteSelection.sizeZ() / 2 + 0.25f,
                        dir == 0 ? invertWidth : 0.5f,
                        dir == 1 ? invertWidth : 0.5f,
                        dir == 2 ? invertWidth : 0.5f);
                shapeRenderer.end();

                shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
                shapeRenderer.setColor(invertColour);
                shapeRenderer.box(
                        dir == 0 ? pasteSelection.maxPos.x + 1 : pasteSelection.minPos.x + (float) pasteSelection.sizeX() / 2 - 0.25f,
                        dir == 1 ? pasteSelection.maxPos.y + 1 : pasteSelection.minPos.y + (float) pasteSelection.sizeY() / 2 - 0.25f,
                        dir == 2 ? pasteSelection.maxPos.z + 1 + invertWidth : pasteSelection.minPos.z + (float) pasteSelection.sizeZ() / 2 + 0.25f,
                        dir == 0 ? invertWidth : 0.5f,
                        dir == 1 ? invertWidth : 0.5f,
                        dir == 2 ? invertWidth : 0.5f);
                shapeRenderer.end();
            }
        }
    }
}
