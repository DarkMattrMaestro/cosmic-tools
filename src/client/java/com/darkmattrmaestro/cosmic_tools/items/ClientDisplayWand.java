package com.darkmattrmaestro.cosmic_tools.items;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectSet;
import com.darkmattrmaestro.cosmic_tools.Constants;
import com.darkmattrmaestro.cosmic_tools.commands.CommandLoadVideo;
import com.darkmattrmaestro.cosmic_tools.utils.*;
import finalforeach.cosmicreach.blocks.BlockPosition;
import finalforeach.cosmicreach.blocks.BlockState;
import finalforeach.cosmicreach.gamestates.GameState;
import finalforeach.cosmicreach.gamestates.InGame;
import finalforeach.cosmicreach.items.ItemStack;
import finalforeach.cosmicreach.ui.UI;
import finalforeach.cosmicreach.util.Identifier;

import java.util.ArrayList;
import java.util.List;

import static com.darkmattrmaestro.cosmic_tools.utils.BlockSelectionUtil.*;
import static com.darkmattrmaestro.cosmic_tools.utils.ChatUtils.blockPosToString;
import static com.darkmattrmaestro.cosmic_tools.utils.ChatUtils.sendMsg;
import static com.darkmattrmaestro.cosmic_tools.utils.SoundUtils.successFailSound;
import static finalforeach.cosmicreach.entities.projectiles.EntityProjectileLaser.TAG_STOPS_LASERS;
import static java.lang.Math.*;

public class ClientDisplayWand {
    private static final float reachDist = 256.0f;

    private static BlockPosition tlPos = null; // Top Left
    private static BlockPosition brPos = null; // Bottom Right
    private static int hStep = 1;
    private static int vStep = 1;

    private static RenderSelections renderSelection = RenderSelections.PasteSelection;

    private static int selectedPixelX, selectedPixelY = 0;

    public static BlockPosition getTLPos() { return tlPos; }
    public static BlockPosition getBRPos() { return brPos; }
    public static int getHStep() { return hStep; }
    public static int getVStep() { return vStep; }

//    public static Selection getFirstSelection() {
//        if (firstPos == null) return null;
//        return Selection.of(firstPos, firstPos);
//    }
//
//    public static Selection getSecondSelection() {
//        if (secondPos == null) return null;
//        return Selection.of(secondPos, secondPos);
//    }
//
//    public static Selection getSelection() {
//        if (firstPos == null || secondPos == null) return null;
//        return Selection.of(firstPos, secondPos);
//    }

    public static boolean onMousePressed(int button) {
        if (GameState.currentGameState.getClass() != InGame.class) { return false; }

        ItemStack selected = UI.hotbar.getSelectedItemStack();
        if (selected == null || !Identifier.of(Constants.MOD_ID, "selection_wand").toString().equals(selected.getItem().getID())) {
            return false;
        }

        return false;
    }

    public static boolean onKeyPressed(int keycode) {
        boolean captured = true;

        switch (keycode) {
            case Input.Keys.C -> { // Copy selection
                if (Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT) && Gdx.input.isKeyPressed(Input.Keys.ALT_LEFT)) {
                    successFailSound(copyBlocks());
                }
            }
            default -> {
                captured = false;
            }
        }

        if (captured) { return true; }
        captured = true;

        BlockPosition pos = BlockSelectionUtil.getBlockLookingAtFar(reachDist);
        if (pos != null) {
            switch (keycode) {
                case Input.Keys.NUMPAD_DOT -> {
                    if (Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT)) {
                        // Sey top-left position
                        tlPos = pos.copy();
                        sendMsg("Top-left position set to " + blockPosToString(pos));
                    } else if (Gdx.input.isKeyPressed(Input.Keys.ALT_LEFT)) {
                        // Sey bottom-right position
                        brPos = pos.copy();
                        sendMsg("Bottom-right position set to " + blockPosToString(pos));
                    }
                }
                default -> {
                    captured = false;
                }
            }
        }



        //******************

        if (tlPos == null || brPos == null) { return; }

        switch (keycode) {
            case Input.Keys.ENTER -> {
                Constants.LOGGER.warn("Finding bits...");
                if (getSelectedLampPos() == null) {
                    Constants.LOGGER.warn("There is no selected lamp position!");
                    break;
                }
                for (int i = 0;; i++) {
                    BlockPosition ithSwitchPos = getSwitchAtIndex(getSelectedLampPos(), i, 5);
                    if (ithSwitchPos == null) { break; }
//                    Constants.LOGGER.warn("Found laserSwitch #{}: {}", i, ithSwitchPos);
                }
            }
            case Input.Keys.UP -> {
                vStep = max(1, min(abs(tlPos.getGlobalY() - brPos.getGlobalY()), vStep + 1));
            }
            case Input.Keys.DOWN -> {
                vStep = max(1, min(abs(tlPos.getGlobalY() - brPos.getGlobalY()), vStep - 1));
            }
            case Input.Keys.RIGHT -> {
                if (tlPos.getGlobalX() - brPos.getGlobalX() == 0) {
                    hStep = max(1, min(abs(tlPos.getGlobalZ() - brPos.getGlobalZ()), hStep + 1));
                } else {
                    hStep = max(1, min(abs(tlPos.getGlobalX() - brPos.getGlobalX()), hStep + 1));
                }
            }
            case Input.Keys.LEFT -> {
                if (tlPos.getGlobalX() - brPos.getGlobalX() == 0) {
                    hStep = max(1, min(abs(tlPos.getGlobalZ() - brPos.getGlobalZ()), hStep - 1));
                } else {
                    hStep = max(1, min(abs(tlPos.getGlobalX() - brPos.getGlobalX()), hStep - 1));
                }
            }
        }

        //**********************

        return captured;
    }

    public static void renderOverlay(ShapeRenderer shapeRenderer, float opacity) {
//        DrawInfo[] drawInfos = {
//                new DrawInfo( // Draw selection to copy
//                        ClientDisplayWand::getSelection,
//                        new Color(0, 0.7882352941f, 0.7882352941f, 0.25f * opacity),
//                        new Color(0.2745098039f, 0.2745098039f, 0.9098039216f, opacity),
//                        0,
//                        renderSelection == RenderSelections.CopySelection
//                ),
//                new DrawInfo( // Draw first position
//                        ClientDisplayWand::getFirstSelection,
//                        new Color(0, 0.7882352941f, 0.7882352941f, 0.2f * opacity),
//                        new Color(0.25f, 0.25f, 0.75f, opacity),
//                        0.02f,
//                        renderSelection == RenderSelections.FirstPos
//                ),
//                new DrawInfo( // Draw second position
//                        ClientDisplayWand::getSecondSelection,
//                        new Color(0, 0.7882352941f, 0.7882352941f, 0.2f * opacity),
//                        new Color(0.10f, 0.10f, 0.40f, opacity),
//                        0.02f,
//                        renderSelection == RenderSelections.SecondPos
//                ),
//                new DrawInfo( // Draw paste area
//                        ClientDisplayWand::getPasteSelection,
//                        new Color(0.64f, 0.64f, 0.64f, 0.25f * opacity),
//                        new Color(0.2745098039f, 0.9098039216f, 0.2745098039f, opacity),
//                        0,
//                        renderSelection == RenderSelections.PasteSelection
//                ),
//                new DrawInfo( // Draw paste origin
//                        ClientDisplayWand::getPastePositionSelection,
//                        new Color(0, 0.8f, 0, 0.64f * opacity),
//                        new Color(0.2745098039f, 0.9098039216f, 0.2745098039f, opacity),
//                        0.02f,
//                        false
//                )
//        };
//
//        for (DrawInfo drawInfo : drawInfos) {
//            Selection selection = drawInfo.selectionSupplier.get();
//            if (selection != null) {
//                selection.setCustomInflate(drawInfo.customInflate);
//                selection.draw(shapeRenderer, drawInfo.fillColor, drawInfo.borderColor, drawInfo.highlight);
//            }
//        }
//
//        Color dimmedInvertColour = new Color(invertColour.r, invertColour.g, invertColour.b, invertColour.a * opacity);
//
//        Selection pasteSelection = ClientDisplayWand.getPasteSelection();
//        if (pasteSelection != null) {
//            boolean[] inverts = new boolean[]{invertX, invertY, invertZ};
//            for (int dir = 0; dir < 3; dir++) {
//                if (!inverts[dir]) { continue; }
//                shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
//                shapeRenderer.setColor(dimmedInvertColour);
//                shapeRenderer.box(
//                        dir == 0 ? pasteSelection.minPos.x - invertWidth : pasteSelection.minPos.x + (float) pasteSelection.sizeX() / 2 - 0.25f,
//                        dir == 1 ? pasteSelection.minPos.y - invertWidth : pasteSelection.minPos.y + (float) pasteSelection.sizeY() / 2 - 0.25f,
//                        dir == 2 ? pasteSelection.minPos.z : pasteSelection.minPos.z + (float) pasteSelection.sizeZ() / 2 + 0.25f,
//                        dir == 0 ? invertWidth : 0.5f,
//                        dir == 1 ? invertWidth : 0.5f,
//                        dir == 2 ? invertWidth : 0.5f);
//                shapeRenderer.end();
//
//                shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
//                shapeRenderer.setColor(dimmedInvertColour);
//                shapeRenderer.box(
//                        dir == 0 ? pasteSelection.maxPos.x + 1 : pasteSelection.minPos.x + (float) pasteSelection.sizeX() / 2 - 0.25f,
//                        dir == 1 ? pasteSelection.maxPos.y + 1 : pasteSelection.minPos.y + (float) pasteSelection.sizeY() / 2 - 0.25f,
//                        dir == 2 ? pasteSelection.maxPos.z + 1 + invertWidth : pasteSelection.minPos.z + (float) pasteSelection.sizeZ() / 2 + 0.25f,
//                        dir == 0 ? invertWidth : 0.5f,
//                        dir == 1 ? invertWidth : 0.5f,
//                        dir == 2 ? invertWidth : 0.5f);
//                shapeRenderer.end();
//            }
//        }



        // TODO: Fix CommandLoadVideo
        for (Selection failedLampSelection : CommandLoadVideo.failedLampsSelections) {
            failedLampSelection.draw(shapeRenderer, new Color(1f, 1f, 0f, 0.8f), new Color(1f, 1f, 0f, 1));
        }

        for (Selection lampSelection : DisplayWand.getLampPosSelections()) {
            if (CommandLoadVideo.failedLampsSelections.contains(lampSelection, false)) { continue; }
            lampSelection.draw(shapeRenderer, new Color(0.2f, 0.9333333f, 1f, 0.1f), new Color(0.2f, 0.9333333f, 1f, 1));
        }
    }











    public static boolean isParallelX() {
        if (tlPos == null || brPos == null) { return false; }
        return tlPos.getGlobalX() - brPos.getGlobalX() == 0;
    }

    public static int tl2brMultiplier() {
        if (tlPos == null || brPos == null) { return 1; }
        if ((tlPos.getGlobalX() <= brPos.getGlobalX() && !isParallelX()) || (tlPos.getGlobalZ() <= brPos.getGlobalZ() && isParallelX())) { return 1; }
        return -1;
    }

    public static Array<BlockPosition> getAllLampPos() {
        if (tlPos == null || brPos == null) { return new Array<BlockPosition>(); }

        Array<BlockPosition> positions = new Array<BlockPosition>();
        if (isParallelX()) {
            for (int z = 0; z * tl2brMultiplier() <= (getBRPos().getGlobalZ() - getTLPos().getGlobalZ()); z += hStep * tl2brMultiplier()) {
                for (int y = 0; y >= getBRPos().getGlobalY() - getTLPos().getGlobalY(); y -= vStep) {
                    positions.add(getTLPos().getOffsetBlockPos(0, y, z));
                }
            }
        } else {
            for (int x = 0; x * tl2brMultiplier() <= (getBRPos().getGlobalX() - getTLPos().getGlobalX()); x += hStep * tl2brMultiplier()) {
                for (int y = 0; y >= getBRPos().getGlobalY() - getTLPos().getGlobalY(); y -= vStep) {
                    positions.add(getTLPos().getOffsetBlockPos(x, y, 0));
                }
            }
        }

        return positions;
    }

    public static int getWidthPixels() {
        if (tlPos == null || brPos == null) { return 0; }
        if (isParallelX()) {
            return abs(brPos.getGlobalZ() - tlPos.getGlobalZ()) / hStep + 1;
        } else {
            return abs(brPos.getGlobalX() - tlPos.getGlobalX()) / hStep + 1;
        }
    }

    public static int getHeightPixels() {
        if (tlPos == null || brPos == null) { return 0; }
        return abs(brPos.getGlobalY() - tlPos.getGlobalY()) / vStep + 1;
    }

    public static Array<Selection> getLampPosSelections() {
        if (tlPos == null || brPos == null) { return new Array<Selection>(); }

        Array<Selection> selections = new Array<Selection>();
        if (isParallelX()) {
            for (int z = getTLPos().getGlobalZ(); tl2brMultiplier() * (getBRPos().getGlobalZ() - z) >= 0; z += hStep * tl2brMultiplier()) {
                for (int y = getTLPos().getGlobalY(); y >= getBRPos().getGlobalY(); y -= vStep) {
                    selections.add(Selection.of(getTLPos().getGlobalX(), y, z, getTLPos().getGlobalX(), y, z));
                }
            }
        } else {
            for (int x = getTLPos().getGlobalX(); tl2brMultiplier() * (getBRPos().getGlobalX() - x) >= 0; x += hStep * tl2brMultiplier()) {
                for (int y = getTLPos().getGlobalY(); y >= getBRPos().getGlobalY(); y -= vStep) {
                    selections.add(Selection.of(x, y, getTLPos().getGlobalZ(), x, y, getTLPos().getGlobalZ()));
                }
            }
        }

        return selections;
    }

    public static BlockPosition getPixelLampPos(int x, int y) {
        if (tlPos == null || brPos == null) { return null; }

        if (isParallelX()) {
            return BlockPosition.ofGlobal(
                    InGame.getLocalPlayer().getZone(),
                    tlPos.getGlobalX(),
                    y * vStep * Integer.signum(brPos.getGlobalY() - tlPos.getGlobalY()) + tlPos.getGlobalY(),
                    x * hStep * Integer.signum(brPos.getGlobalZ() - tlPos.getGlobalZ()) + tlPos.getGlobalZ()
            );
        } else {
            return BlockPosition.ofGlobal(
                    InGame.getLocalPlayer().getZone(),
                    x * hStep * Integer.signum(brPos.getGlobalX() - tlPos.getGlobalX()) + tlPos.getGlobalX(),
                    y * vStep * Integer.signum(brPos.getGlobalY() - tlPos.getGlobalY()) + tlPos.getGlobalY(),
                    tlPos.getGlobalZ()
            );
        }
    }

    public static BlockPosition getSelectedLampPos() {
        if (tlPos == null || brPos == null) { return null; }

        if (isParallelX()) {
            return BlockPosition.ofGlobal(
                    InGame.getLocalPlayer().getZone(),
                    tlPos.getGlobalX(),
                    selectedPixelY * vStep * Integer.signum(brPos.getGlobalY() - tlPos.getGlobalY()) + tlPos.getGlobalY(),
                    selectedPixelX * hStep * Integer.signum(brPos.getGlobalZ() - tlPos.getGlobalZ()) + tlPos.getGlobalZ()
            );
        } else {
            return BlockPosition.ofGlobal(
                    InGame.getLocalPlayer().getZone(),
                    selectedPixelX * hStep * Integer.signum(brPos.getGlobalX() - tlPos.getGlobalX()) + tlPos.getGlobalX(),
                    selectedPixelY * vStep * Integer.signum(brPos.getGlobalY() - tlPos.getGlobalY()) + tlPos.getGlobalY(),
                    tlPos.getGlobalZ()
            );
        }
    }

    public static Selection getSelectedLampPosSelection() {
        if (tlPos == null || brPos == null) { return null; }

        if (isParallelX()) {
            return Selection.of(
                    tlPos.getGlobalX(),
                    selectedPixelY * vStep * Integer.signum(brPos.getGlobalY() - tlPos.getGlobalY()) + tlPos.getGlobalY(),
                    selectedPixelX * hStep * Integer.signum(brPos.getGlobalZ() - tlPos.getGlobalZ()) + tlPos.getGlobalZ(),
                    tlPos.getGlobalX(),
                    selectedPixelY * vStep * Integer.signum(brPos.getGlobalY() - tlPos.getGlobalY()) + tlPos.getGlobalY(),
                    selectedPixelX * hStep * Integer.signum(brPos.getGlobalZ() - tlPos.getGlobalZ()) + tlPos.getGlobalZ()
            );
        } else {
            return Selection.of(
                    selectedPixelX * hStep * Integer.signum(brPos.getGlobalX() - tlPos.getGlobalX()) + tlPos.getGlobalX(),
                    selectedPixelY * vStep * Integer.signum(brPos.getGlobalY() - tlPos.getGlobalY()) + tlPos.getGlobalY(),
                    tlPos.getGlobalZ(),
                    selectedPixelX * hStep * Integer.signum(brPos.getGlobalX() - tlPos.getGlobalX()) + tlPos.getGlobalX(),
                    selectedPixelY * vStep * Integer.signum(brPos.getGlobalY() - tlPos.getGlobalY()) + tlPos.getGlobalY(),
                    tlPos.getGlobalZ()
            );
        }
    }

    //----------------------------------//

//    public static BlockPosition getNextSwitch(BlockPosition previousComponent, int maxComponentDist) {
//        BlockPosition currPos = previousComponent.copy();
//        // Keep searching the next component
//        for (int i = 0; i < 200; i++) {
//            boolean freePosX, freeNegX, freePosY, freeNegY, freePosZ, freeNegZ;
//            freePosX = freeNegX = freePosY = freeNegY = freePosZ = freeNegZ = true;
//            // Find the nearest relevant component along each axis
//            axisLoop:
//            for (int d = 1; d <= maxComponentDist; d++) {
//                if (freePosX) {
//                    BlockPosition pos = previousComponent.getOffsetBlockPos(d, 0, 0);
//                    BlockState posState = pos.getBlockState();
//                    if (posState.getBlockId().equals("base:laser_switch")) { // Potentially nearly at the goal
//                        if (posState.getParamDirection("direction").getXOffset() == 1) { continue; } // The laser will hit the wrong face of the lase switch, thus never passing through.
//
//                        for (int j = 1; j < maxComponentDist; j++) {
//                            BlockPosition nextPos = pos.getOffsetBlockPos(j, 0, 0);
//                            BlockState nextPosState = nextPos.getBlockState();
//                            // Check if the switch is in use in this axis. If so, it is the next switch.
//                            if (nextPosState.getBlockId().equals("base:laser_emitter") && nextPosState.getParamDirection("direction").getXOffset() == -1) {
//                                return pos;
//                            }
//                        }
//                    } else if (posState.getBlockId().equals("base:laser_emitter") && posState.getParamDirection("direction").getXOffset() == -1) {
//                        currPos = pos;
//                        break axisLoop;
//                    } else if (!posState.walkThrough && (posState.isOpaque || posState.hasTag(TAG_STOPS_LASERS))) {
//                        freePosX = false;
//                    }
//                }
//            }
//        }
//    }

//    public static BlockPosition getNextSwitch(BlockPosition previousComponent, int maxComponentDist) {
//        BlockPosition currPos = previousComponent.copy();
//        // Keep searching the next component
//        for (int i = 0; i < 200; i++) {
//            int[][] directions = new int[][]{{1,0,0},{-1,0,0},{0,1,0},{0,-1,0},{0,0,1},{0,0,-1}};
//
//            // Find the nearest relevant component along each axis
//            axisLoop:
//            for (int d = 1; d <= maxComponentDist; d++) {
//                for (int iDir = 0; iDir < 6; iDir++) {
//                    if (directions[iDir] != null) {
//                        BlockPosition pos = currPos.getOffsetBlockPos(d * directions[iDir][0], d * directions[iDir][1], d * directions[iDir][2]);
//                        if (pos.equals(previousComponent)) { continue; } // Do not loop back to the starting point
//                        BlockState posState = pos.getBlockState();
//
//                        if (posState.getParamDirection("direction") != null) {
//                            boolean axisMatches = (
//                                    posState.getParamDirection("direction").getXOffset() == directions[iDir][0]
//                                    && posState.getParamDirection("direction").getYOffset() == directions[iDir][1]
//                                    && posState.getParamDirection("direction").getZOffset() == directions[iDir][2]
//                            );
//
//                            boolean axisInverseMatches = (
//                                    posState.getParamDirection("direction").getXOffset() == -directions[iDir][0]
//                                    && posState.getParamDirection("direction").getYOffset() == -directions[iDir][1]
//                                    && posState.getParamDirection("direction").getZOffset() == -directions[iDir][2]
//                            );
//
//                            if (posState.getBlockId().equals("base:laser_switch")) { // Potentially nearly at the goal
//                                if (axisMatches) { continue; } // The laser will hit the wrong face of the lase switch, thus never passing through.
//
//                                for (int j = 1; j < maxComponentDist; j++) {
//                                    BlockPosition nextPos = pos.getOffsetBlockPos(j * directions[iDir][0], j * directions[iDir][1], j * directions[iDir][2]);
//                                    BlockState nextPosState = nextPos.getBlockState();
//                                    boolean nextPosAxisInverseMatches;
//                                    if (nextPosState.getParam("type").equals("split")) {
//                                        // Laser Splitter
//                                        nextPosAxisInverseMatches = nextPosState.getParam("axis").equals(
//                                            directions[iDir][0] != 0 ? "X" :
//                                            directions[iDir][1] != 0 ? "Y" :
//                                            "Z"
//                                        );
//                                    } else if (nextPosState.getParam("type").equals("single")) {
//                                        // Laser Emitter
//                                        String laserSwitchDir = posState.getParam("direction");
//                                        String laserEmitterDir = nextPosState.getParam("direction");
//
//                                        nextPosAxisInverseMatches = (
//                                            laserSwitchDir.charAt(laserSwitchDir.length() - 1) == laserEmitterDir.charAt(laserEmitterDir.length() - 1)
//                                            && !laserSwitchDir.equals(laserEmitterDir)
//                                        );
//                                    } else {
//                                        break;
//                                    }
//                                    if (nextPosState.getBlockId().equals("base:laser_emitter") && nextPosAxisInverseMatches) {
//                                        return pos;
//                                    }
//                                }
//                            } else if (posState.getBlockId().equals("base:laser_emitter") && axisInverseMatches) {
//                                currPos = pos;
//                                break axisLoop;
//                            }
//                        } else if (!posState.walkThrough && (posState.isOpaque || posState.hasTag(TAG_STOPS_LASERS))) {
//                            directions[iDir] = null;
//                        }
//                    }
//                }
//            }
//        }
//
//        sendMsg("Laser Switch search distance limit exceeded! Search failed at " + currPos.toString() + ".");
//        return null;
//    }

    public static BlockPosition getSwitchAtIndex(BlockPosition lampPos, int iSwitch, int maxComponentDist) {
        int remainingIgnoredSwitches = iSwitch;
        ObjectSet<BlockPosition> ignoredBlocks = ObjectSet.with(lampPos);
        BlockPosition currPos = lampPos.copy();
        // Keep searching the next component
        for (int i = 0; i < 200; i++) {
            int[][] directions = new int[][]{{1,0,0},{-1,0,0},{0,1,0},{0,-1,0},{0,0,1},{0,0,-1}};

            // Find the nearest relevant component along each axis
            axisLoop:
            for (int d = 1; d <= maxComponentDist; d++) {
                for (int iDir = 0; iDir < 6; iDir++) {
                    if (directions[iDir] != null) {
                        BlockPosition pos = currPos.getOffsetBlockPos(d * directions[iDir][0], d * directions[iDir][1], d * directions[iDir][2]);
                        if (pos == null || ignoredBlocks.contains(pos)) { continue; } // Do not loop back to the starting point
                        BlockState posState = pos.getBlockState();

                        if (posState.getParamDirection("direction") != null) {
                            boolean axisMatches = (
                                    posState.getParamDirection("direction").getXOffset() == directions[iDir][0]
                                            && posState.getParamDirection("direction").getYOffset() == directions[iDir][1]
                                            && posState.getParamDirection("direction").getZOffset() == directions[iDir][2]
                            );

                            boolean axisInverseMatches = (
                                    posState.getParamDirection("direction").getXOffset() == -directions[iDir][0]
                                            && posState.getParamDirection("direction").getYOffset() == -directions[iDir][1]
                                            && posState.getParamDirection("direction").getZOffset() == -directions[iDir][2]
                            );

                            if (posState.getBlockId().equals("base:laser_switch")) { // Potentially nearly at the goal
                                if (axisMatches) { continue; } // The laser will hit the wrong face of the lase switch, thus never passing through.

                                for (int j = 1; j < maxComponentDist; j++) {
                                    BlockPosition nextPos = pos.getOffsetBlockPos(j * directions[iDir][0], j * directions[iDir][1], j * directions[iDir][2]);
                                    if (nextPos == null) {
                                        Constants.LOGGER.warn("No block at {} + {}, {}, {}", pos, j * directions[iDir][0], j * directions[iDir][1], j * directions[iDir][2]);
                                        break;
                                    }
                                    BlockState nextPosState = nextPos.getBlockState();
                                    if (nextPosState.getBlockId().equals("base:laser_emitter")) {
                                        boolean nextPosAxisInverseMatches;
                                        if (nextPosState.getParam("type").equals("split")) {
                                            // Laser Splitter
                                            nextPosAxisInverseMatches = nextPosState.getParam("axis").equals(
                                                    directions[iDir][0] != 0 ? "X" :
                                                            directions[iDir][1] != 0 ? "Y" :
                                                                    "Z"
                                            );
                                        } else if (nextPosState.getParam("type").equals("single")) {
                                            // Laser Emitter
                                            String laserSwitchDir = posState.getParam("direction");
                                            String laserEmitterDir = nextPosState.getParam("direction");

                                            nextPosAxisInverseMatches = (
                                                    laserSwitchDir.charAt(laserSwitchDir.length() - 1) == laserEmitterDir.charAt(laserEmitterDir.length() - 1)
                                                            && !laserSwitchDir.equals(laserEmitterDir)
                                            );
                                        } else {
                                            break;
                                        }
                                        if (nextPosState.getBlockId().equals("base:laser_emitter") && nextPosAxisInverseMatches) {
                                            if (remainingIgnoredSwitches > 0) {
                                                remainingIgnoredSwitches--;
                                                ignoredBlocks.add(nextPos);
                                                break;
                                            }
                                            return pos;
                                        }
                                    }
                                }
                            } else if (posState.getBlockId().equals("base:laser_emitter") && axisInverseMatches) {
                                currPos = pos;
                                break axisLoop;
                            }
                        } else if (!posState.walkThrough && (posState.isOpaque || posState.hasTag(TAG_STOPS_LASERS))) {
                            directions[iDir] = null;
                        }
                    }
                }
            }
        }

        throw new IndexOutOfBoundsException("Laser Switch search distance limit exceeded! Search failed at " + currPos.toString() + ".");
//        sendMsg("Laser Switch search distance limit exceeded! Search failed at " + currPos.toString() + ".");
//        return null;
    }

    /**
     * Get the maximum number of frames that can fit in the display's memory. This equates to the shortest sequence of
     * consecutive laser switches.
     *
     * @param maxComponentDist The maximal distance between two photonic components that interact with each other.
     * @return The maximum number of frames that can fit in the display's memory.
     */
    public static int getMaxNumFrames(int maxComponentDist) {
        int lowest = Integer.MAX_VALUE;
        for (BlockPosition currLampPos : getAllLampPos()) {
            lowest = min(lowest, getNumSwitchesForLamp(currLampPos, maxComponentDist));
        }

        if (lowest == Integer.MAX_VALUE) { return -1; }
        return lowest;
    }

    /**
     * Get the number of laser switches connected to the lamp at the given block position. Search only for photonic
     * components within a distance of `maxComponentDist` from each other.
     *
     * @param lampPos The target lamp' block position.
     * @param maxComponentDist The maximal distance between two photonic components that interact with each other.
     * @return The number of laser switches connected to the target lamp.
     */
    public static int getNumSwitchesForLamp(BlockPosition lampPos, int maxComponentDist) {
        int numSwitches = 0;
        ObjectSet<BlockPosition> ignoredBlocks = ObjectSet.with(lampPos);
        BlockPosition currPos = lampPos.copy();
        // Keep searching the next component
        for (int i = 0; i < 200; i++) {
            int[][] directions = new int[][]{{1,0,0},{-1,0,0},{0,1,0},{0,-1,0},{0,0,1},{0,0,-1}};

            // Find the nearest relevant component along each axis
            axisLoop:
            for (int d = 1; d <= maxComponentDist; d++) {
                for (int iDir = 0; iDir < 6; iDir++) {
                    if (directions[iDir] != null) {
                        BlockPosition pos = currPos.getOffsetBlockPos(d * directions[iDir][0], d * directions[iDir][1], d * directions[iDir][2]);
                        if (pos == null || ignoredBlocks.contains(pos)) { continue; } // Do not loop back to the starting point
                        BlockState posState = pos.getBlockState();

                        if (posState.getParamDirection("direction") != null) {
                            boolean axisMatches = (
                                    posState.getParamDirection("direction").getXOffset() == directions[iDir][0]
                                            && posState.getParamDirection("direction").getYOffset() == directions[iDir][1]
                                            && posState.getParamDirection("direction").getZOffset() == directions[iDir][2]
                            );

                            boolean axisInverseMatches = (
                                    posState.getParamDirection("direction").getXOffset() == -directions[iDir][0]
                                            && posState.getParamDirection("direction").getYOffset() == -directions[iDir][1]
                                            && posState.getParamDirection("direction").getZOffset() == -directions[iDir][2]
                            );

                            if (posState.getBlockId().equals("base:laser_switch")) { // Potentially nearly at the goal
                                if (axisMatches) { continue; } // The laser will hit the wrong face of the lase switch, thus never passing through.

                                for (int j = 1; j < maxComponentDist; j++) {
                                    BlockPosition nextPos = pos.getOffsetBlockPos(j * directions[iDir][0], j * directions[iDir][1], j * directions[iDir][2]);
                                    BlockState nextPosState = nextPos.getBlockState();
                                    boolean nextPosAxisInverseMatches;
                                    if (nextPosState.getBlockId().equals("base:laser_emitter")) {
                                        if (nextPosState.getParam("type").equals("split")) {
                                            // Laser Splitter
                                            nextPosAxisInverseMatches = nextPosState.getParam("axis").equals(
                                                    directions[iDir][0] != 0 ? "X" :
                                                            directions[iDir][1] != 0 ? "Y" :
                                                                    "Z"
                                            );
                                        } else if (nextPosState.getParam("type").equals("single")) {
                                            // Laser Emitter
                                            String laserSwitchDir = posState.getParam("direction");
                                            String laserEmitterDir = nextPosState.getParam("direction");

                                            nextPosAxisInverseMatches = (
                                                    laserSwitchDir.charAt(laserSwitchDir.length() - 1) == laserEmitterDir.charAt(laserEmitterDir.length() - 1)
                                                            && !laserSwitchDir.equals(laserEmitterDir)
                                            );
                                        } else {
                                            break;
                                        }
                                        if (nextPosAxisInverseMatches) {
                                            numSwitches++;
                                            ignoredBlocks.add(pos);
                                            break;
                                        }
                                    } else {
                                        break;
                                    }
                                }
                            } else if (posState.getBlockId().equals("base:laser_emitter") && axisInverseMatches) {
                                currPos = pos;
                                break axisLoop;
                            }
                        } else if (!posState.walkThrough && (posState.isOpaque || posState.hasTag(TAG_STOPS_LASERS))) {
                            directions[iDir] = null;
                        }
                    }
                }
            }
        }

        return numSwitches;
    }

    //----------------------------------//

//    /**
//     *
//     *
//     * @param blockPos The position of the block that represents the desired pixel
//     * @return True if the pixel can be set (i.e. the pixel exists), else false
//     */
//    public static boolean setSelectedPixel(BlockPosition blockPos) {
//        if (
//            BlockSelectionUtil.isWithin(blockPos, Selection.of(tlPos, brPos))
//            && abs(blockPos.getGlobalY() - tlPos.getGlobalY()) % vStep == 0
//            && abs(blockPos.getGlobalZ() - tlPos.getGlobalZ()) % hStep == 0
//            && abs(blockPos.getGlobalX() - tlPos.getGlobalX()) % hStep == 0
//        ) {
//            selectedPixelY = abs(blockPos.getGlobalY() - tlPos.getGlobalY()) / vStep;
//            if (isParallelX()) {
//                selectedPixelX = abs(blockPos.getGlobalZ() - tlPos.getGlobalZ()) / hStep;
//            } else {
//                selectedPixelX = abs(blockPos.getGlobalX() - tlPos.getGlobalX()) / hStep;
//            }
//
//            return true;
//        }
//
//        return false;
//    }
//
//    public static void onMousePressed(int button) {
//        if (tlPos == null || brPos == null) { return; }
//
//        switch (button) {
//            case Input.Buttons.LEFT -> {
//                BlockPosition lookingBlock = BlockSelectionUtil.getBlockPositionLookingAt().copy();
//                BlockState blockState = lookingBlock.getBlockState();
//
//                // Attempt to set the current pixel, else start selecting rows
//                if (!setSelectedPixel(lookingBlock)) {
//                    Constants.LOGGER.info("Setting rows");
//                }
//            }
//        }
//    }
//}

}
