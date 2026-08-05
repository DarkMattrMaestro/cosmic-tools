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
import finalforeach.cosmicreach.util.constants.Direction;

import java.util.*;

import static com.darkmattrmaestro.cosmic_tools.utils.ChatUtils.blockPosToString;
import static com.darkmattrmaestro.cosmic_tools.utils.ChatUtils.sendMsg;
import static com.darkmattrmaestro.cosmic_tools.utils.SoundUtils.scrollSound;
import static com.darkmattrmaestro.cosmic_tools.utils.SoundUtils.successFailSound;
import static finalforeach.cosmicreach.entities.projectiles.EntityProjectileLaser.TAG_STOPS_LASERS;
import static java.lang.Math.*;

public class ClientDisplayWand {
    private static final float reachDist = 256.0f;
    private static final int MAX_COMPONENT_DIST = 16;
    private static final int MAX_TOTAL_DIST = 1000000;

    private static BlockPosition tlPos = null; // Top Left
    private static BlockPosition brPos = null; // Bottom Right
    private static int hStep = 1;
    private static int vStep = 1;

    private static void setTlPos(BlockPosition val) {
        tlPos = val;
        screenSwitches = null;
    }

    private static void setBRPos(BlockPosition val) {
        brPos = val;
        screenSwitches = null;
    }

    private static void setHStep(int val) {
        hStep = val;
        screenSwitches = null;
    }

    private static void setVStep(int val) {
        vStep = val;
        screenSwitches = null;
    }

    private static int selectedPixelX, selectedPixelY = 0;

    private static ArrayList<BlockPosition>[][] screenSwitches = null;

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

        if (button == Input.Buttons.RIGHT) {
            cacheAllSwitches(MAX_COMPONENT_DIST, MAX_TOTAL_DIST);
            successFailSound(true);
            return true;
        }

        return false;
    }

    public static boolean onKeyPressed(int keycode) {
        boolean captured = true;

        switch (keycode) {
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
                        setTlPos(pos.copy());
                        sendMsg("Top-left position set to " + blockPosToString(pos));
                    } else if (Gdx.input.isKeyPressed(Input.Keys.ALT_LEFT)) {
                        // Sey bottom-right position
                        setBRPos(pos.copy());
                        sendMsg("Bottom-right position set to " + blockPosToString(pos));
                    }
                }
                default -> {
                    captured = false;
                }
            }
        }



        //******************

        if (tlPos == null || brPos == null) { return false; }

        switch (keycode) {
            case Input.Keys.UP -> {
                setVStep(max(1, min(abs(tlPos.getGlobalY() - brPos.getGlobalY()), vStep + 1)));
                scrollSound();
            }
            case Input.Keys.DOWN -> {
                setVStep(max(1, min(abs(tlPos.getGlobalY() - brPos.getGlobalY()), vStep - 1)));
                scrollSound();
            }
            case Input.Keys.RIGHT -> {
                if (tlPos.getGlobalX() - brPos.getGlobalX() == 0) {
                    setHStep(max(1, min(abs(tlPos.getGlobalZ() - brPos.getGlobalZ()), hStep + 1)));
                } else {
                    setHStep(max(1, min(abs(tlPos.getGlobalX() - brPos.getGlobalX()), hStep + 1)));
                }
                scrollSound();
            }
            case Input.Keys.LEFT -> {
                if (tlPos.getGlobalX() - brPos.getGlobalX() == 0) {
                    setHStep(max(1, min(abs(tlPos.getGlobalZ() - brPos.getGlobalZ()), hStep - 1)));
                } else {
                    setHStep(max(1, min(abs(tlPos.getGlobalX() - brPos.getGlobalX()), hStep - 1)));
                }
                scrollSound();
            }
        }

        //**********************

        return captured;
    }

    public static void renderInWorld(ShapeRenderer shapeRenderer) {
        for (Selection failedLampSelection : CommandLoadVideo.failedLampsSelections) {
            failedLampSelection.draw(shapeRenderer, new Color(1f, 1f, 0f, 0.8f), new Color(1f, 1f, 0f, 1));
        }

        for (Selection lampSelection : ClientDisplayWand.getLampPosSelections()) {
            if (CommandLoadVideo.failedLampsSelections.contains(lampSelection, false)) { continue; }
            lampSelection.draw(shapeRenderer, new Color(0.2f, 0.9333333f, 1f, 0.1f), new Color(0.2f, 0.9333333f, 1f, 1));
        }
    }

    public static void renderOverlay(ShapeRenderer shapeRenderer) {
        if (getMaxNumFrames() > 0) {
            int currFrame = ((int) InGame.getWorld().currentWorldTick) % getMaxNumFrames();

            if (screenSwitches == null) {
                return;
            }
            for (int ix = getWidthPixels() - 1; ix >= 0; ix--) {
                for (int iy = getHeightPixels() - 1; iy >= 0; iy--) {
                    if (screenSwitches[ix][iy] == null) {
                        continue;
                    }

//                for (BlockPosition blockPos : screenSwitches[ix][iy]) {
                    BlockPosition blockPos = screenSwitches[ix][iy].get(currFrame);
                    Selection.of(blockPos, blockPos).draw(shapeRenderer, new Color(0.2f, 0.9333333f, 1f, 0.1f), new Color(0.2f, 0.9333333f, 1f, 0.4f));
//                }
                }
            }
        }
    }








    public static int[] frontfacingDir() {
        if (getTLPos().getGlobalX() > getBRPos().getGlobalX()) {
            return new int[]{0, 0, -1};
        } else if (getTLPos().getGlobalX() < getBRPos().getGlobalX()) {
            return new int[]{0, 0, 1};
        } else {
            if (getTLPos().getGlobalZ() > getBRPos().getGlobalZ()) {
                return new int[]{-1, 0, 0};
            } else {
                return new int[]{1, 0, 0};
            }
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

    /**
     * Zero-indexed?
     *
     * @param x
     * @param y
     * @return
     */
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

    /**
     * Return true if the block is relevant to the traversal algorithm (e.g. laser switch, laser emitter, solid block).
     * Return false otherwise (e.g. air, transparent block)
     *
     * @param blockState The BlockState of the relevant block.
     * @return True if the block is relevant, else false.
     */
    public static boolean isRelevantComponent(BlockState blockState) {
        return (
            blockState.getBlockId().equals("base:laser_switch") ||
            blockState.getBlockId().equals("base:laser_emitter") ||
            (!blockState.walkThrough && (blockState.isOpaque || blockState.hasTag(TAG_STOPS_LASERS)))
        );
    }

    /**
     * Scan in a straight line for any relevant photonic component, returning the block if found.
     *
     * @param curr The starting outgoing block.
     * @param pointingDir The direction to search.
     * @param maxComponentDist The maximum distance to search for a photonic component.
     * @return The nearest photonic component found by traversing a straight line in the given direction.
     */
    public static OutgoingBlock scanlineSearchComponent(OutgoingBlock curr, int[] pointingDir, int maxComponentDist) {
        if (
                pointingDir[0] == curr.outgoingDir[0] &&
                pointingDir[1] == curr.outgoingDir[1] &&
                pointingDir[2] == curr.outgoingDir[2]
        ) { return null; } // Avoid going back where previously traversed

        for (int dist = 1; dist <= maxComponentDist; dist++) { // Limit search distance, in blocks
            // Avoid unloaded or empty chunks
            if (curr.blockPos.getZone().getChunkAtBlock(
                curr.blockPos.getGlobalX() + dist * pointingDir[0],
                curr.blockPos.getGlobalY() + dist * pointingDir[1],
                curr.blockPos.getGlobalZ() + dist * pointingDir[2]
            ) == null) { continue; }

            BlockPosition potentialPos = curr.blockPos.getOffsetBlockPos(dist * pointingDir[0], dist * pointingDir[1], dist * pointingDir[2]);
            if (isRelevantComponent(potentialPos.getBlockState())) {
                // Relevant component found. Add it to the queue and start searching other directions
                return new OutgoingBlock(
                    potentialPos,
                    new int[]{-pointingDir[0], -pointingDir[1], -pointingDir[2]},
                    dist + curr.distFromComponent
                );
            }
        }

        return null;
    }

    /**
     * Scan all axial directions, except for the current block's outgoing direction, for the nearest photonic component.
     * Add any found blocks to the component queue.
     *
     * @param curr The starting block.
     * @param componentQueue The queue of components to be processed via breadth-first search outside of this method.
     * @param maxComponentDist The maximum distance to search for a photonic component.
     */
    public static void multiscanlineSearchComponent(OutgoingBlock curr, Queue<OutgoingBlock> componentQueue, int maxComponentDist) {
        int[][] directions = new int[][]{{1, 0, 0}, {-1, 0, 0}, {0, 1, 0}, {0, -1, 0}, {0, 0, 1}, {0, 0, -1}};

        // Find the next relevant component to add to the queue
        for (int iDir = 0; iDir < 6; iDir++) { // Iterate potential pointing directions
            int[] pointingDir = directions[iDir];
            OutgoingBlock foundBlock = scanlineSearchComponent(curr, pointingDir, maxComponentDist);
            if (foundBlock != null) { componentQueue.add(foundBlock); }
        }
    }

    /**
     * Check that the laser emitter is pointed in a direction that can have a causal effect in the postulated direction.
     *
     * @param blockState BlockState of the laser emitter.
     * @param outgoingDir The outgoing direction that a laser entity is wanted to be output.
     * @return True if the laser emitter can output in the desired direction, else false.
     */
    private static boolean isCausalLaserEmitter(BlockState blockState, int[] outgoingDir) {
        boolean dirMatches;
        switch (blockState.getParam("type")) {
            case "single" -> {
                // Check that the incoming direction matches the laser emitter's direction
                dirMatches = (
                    blockState.getParamDirection("direction").getXOffset() == outgoingDir[0] &&
                    blockState.getParamDirection("direction").getYOffset() == outgoingDir[1] &&
                    blockState.getParamDirection("direction").getZOffset() == outgoingDir[2]
                );
            }
            case "split" -> {
                // Check that the incoming direction is along the laser emitter's axis
                dirMatches = (
                    "X".equals(blockState.getParam("axis")) == (outgoingDir[0] != 0) &&
                    "Y".equals(blockState.getParam("axis")) == (outgoingDir[1] != 0) &&
                    "Z".equals(blockState.getParam("axis")) == (outgoingDir[2] != 0)
                );
            }
            case null, default -> dirMatches = false;
        }

        return dirMatches;
    }

    /**
     * Search for all laser switches that point into the given starting position `startPos` via breadth-first search.
     *
     * @param startPos The position to start the search. All components point terminate into this position.
     * @param maxComponentDist The maximal distance between components.
     * @param maxTotalDist The maximal distance traveled for a given branch of the search. Prevents loops in branches.
     */
    public static ArrayList<BlockPosition> breadthFirstSearchSwitches(BlockPosition startPos, int maxComponentDist, int maxTotalDist) {
        Constants.LOGGER.warn("Starting breadth-first search");

        ArrayList<BlockPosition> foundSwitches = new ArrayList<BlockPosition>();

        HashSet<BlockPosition> explored = new HashSet<BlockPosition>();
        Queue<OutgoingBlock> componentQueue = new ArrayDeque<OutgoingBlock>();

        explored.add(startPos);

        multiscanlineSearchComponent(new OutgoingBlock(startPos, frontfacingDir(), 0), componentQueue, maxComponentDist);

        while (!componentQueue.isEmpty()) {
//            Constants.LOGGER.warn(componentQueue);
            OutgoingBlock curr = componentQueue.poll();

            // Check if the block was already traversed
            if (explored.contains(curr.blockPos)) { continue; }
            // Prevent being stuck in an accidental loop
//            if (curr.distFromComponent > maxTotalDist) { continue; }

            BlockState currBlockState = curr.blockPos.getBlockState();

            if (currBlockState.getBlockId().equals("base:laser_switch")) {
                // Check that the laser direction is orthogonal to the laser switch's input face
                Direction switchDirection = currBlockState.getParamDirection("direction");
                if (!(
                    switchDirection != null &&
                    !(switchDirection.getXOffset() != 0 && curr.outgoingDir[0] != 0) &&
                    !(switchDirection.getYOffset() != 0 && curr.outgoingDir[1] != 0) &&
                    !(switchDirection.getZOffset() != 0 && curr.outgoingDir[2] != 0)
                )) { continue; }

                // Check that there is a laser emitter on the other side of the laser switch pointing through it
                OutgoingBlock nextBlock = scanlineSearchComponent(curr, new int[]{-curr.outgoingDir[0], -curr.outgoingDir[1], -curr.outgoingDir[2]}, maxComponentDist);
                if (nextBlock == null || !isCausalLaserEmitter(nextBlock.blockPos.getBlockState(), curr.outgoingDir)) { continue; }

                // Indicate that the laser switch was found and end the search branch
                foundSwitches.add(curr.blockPos);
                explored.add(curr.blockPos);
            } else if (currBlockState.getBlockId().equals("base:laser_emitter")) {
                // Ignore the laser emitter if it is pointing elsewhere
                if (!isCausalLaserEmitter(currBlockState, curr.outgoingDir)) { continue; }

                multiscanlineSearchComponent(curr, componentQueue, maxComponentDist);
            } else {
                // Stop searching further through the solid block
                explored.add(curr.blockPos);
            }
        }

        return foundSwitches;
    }

    public static void cacheAllSwitches(int maxComponentDist, int maxTotalDist) {
        screenSwitches = new ArrayList[getWidthPixels()][getHeightPixels()];
        for (int ix = getWidthPixels() - 1; ix >= 0; ix--) {
            for (int iy = getHeightPixels() - 1; iy >= 0; iy--) {
                screenSwitches[ix][iy] = breadthFirstSearchSwitches(getPixelLampPos(ix, iy), maxComponentDist, maxTotalDist);
            }
        }
        Constants.LOGGER.error("DONE CACHING");
    }

    public static BlockPosition getSwitchAtIndex(int x, int y, int frameIndex) {
        return screenSwitches[x][y].get(screenSwitches[x][y].size() - 1 - frameIndex);
    }

    /**
     * Get the maximum number of frames that can fit in the display's memory. This equates to the shortest sequence of
     * consecutive laser switches.
     *
     * @return The maximum number of frames that can fit in the display's memory.
     */
    public static int getMaxNumFrames() {
        // Guard against unloaded switches
        if (screenSwitches == null) { return 0; }

        int smallest = Integer.MAX_VALUE;
        for (int ix = getWidthPixels() - 1; ix >= 0; ix--) {
            for (int iy = getHeightPixels() - 1; iy >= 0; iy--) {
                if (screenSwitches[ix][iy] == null) { return 0; }

                smallest = min(smallest, screenSwitches[ix][iy].size());
            }
        }

        if (smallest == Integer.MAX_VALUE) { return 0; }
        return smallest;
    }

}
