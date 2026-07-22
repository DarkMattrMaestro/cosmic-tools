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
import static finalforeach.cosmicreach.entities.projectiles.EntityProjectileLaser.TAG_STOPS_LASERS;
import static java.lang.Math.*;

public class ClientDisplayWand {
    private static final float reachDist = 256.0f;

    private static BlockPosition tlPos = null; // Top Left
    private static BlockPosition brPos = null; // Bottom Right
    private static int hStep = 1;
    private static int vStep = 1;

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

        BlockPosition pos = BlockSelectionUtil.getBlockLookingAtFar(reachDist);
        if (pos != null) {
            cacheAllSwitches(10, 20);
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

        if (tlPos == null || brPos == null) { return false; }

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

    public static void renderInWorld(ShapeRenderer shapeRenderer) {
        // TODO: Fix CommandLoadVideo
        for (Selection failedLampSelection : CommandLoadVideo.failedLampsSelections) {
            failedLampSelection.draw(shapeRenderer, new Color(1f, 1f, 0f, 0.8f), new Color(1f, 1f, 0f, 1));
        }

        for (Selection lampSelection : ClientDisplayWand.getLampPosSelections()) {
            if (CommandLoadVideo.failedLampsSelections.contains(lampSelection, false)) { continue; }
            lampSelection.draw(shapeRenderer, new Color(0.2f, 0.9333333f, 1f, 0.1f), new Color(0.2f, 0.9333333f, 1f, 1));
        }
    }

    public static void renderOverlay(ShapeRenderer shapeRenderer) {
        if (screenSwitches == null) { return; }
        for (int ix = getWidthPixels() - 1; ix >= 0; ix--) {
            for (int iy = getHeightPixels() - 1; iy >= 0; iy--) {
                for (BlockPosition blockPos : screenSwitches[ix][iy]) {
                    Selection.of(blockPos, blockPos).draw(shapeRenderer, new Color(0.2f, 0.9333333f, 1f, 0.1f), new Color(0.2f, 0.9333333f, 1f, 0.4f));
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
            BlockPosition potentialPos = curr.blockPos.getOffsetBlockPos(
                    dist * pointingDir[0],
                    dist * pointingDir[1],
                    dist * pointingDir[2]
            );
            if (isRelevantComponent(potentialPos.getBlockState())) {
                // Relevant component found. Add it to the queue and start searching other directions
                Constants.LOGGER.info("Found relevant component {}", potentialPos);
                return new OutgoingBlock(
                        potentialPos,
                        new int[]{
                                -pointingDir[0],
                                -pointingDir[1],
                                -pointingDir[2]
                        },
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
            Constants.LOGGER.warn(componentQueue);
            OutgoingBlock curr = componentQueue.poll();

            // Check if the block was already traversed
            if (explored.contains(curr.blockPos)) { continue; }
            // Prevent being stuck in an accidental loop
            if (curr.distFromComponent > maxTotalDist) { continue; }

            BlockState currBlockState = curr.blockPos.getBlockState();

            if (currBlockState.getBlockId().equals("base:laser_switch")) {
                Constants.LOGGER.info("Found potential laser switch {}", curr.blockPos);

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
                Constants.LOGGER.info("Laser switch valid {}", curr.blockPos);
                foundSwitches.add(curr.blockPos);
                explored.add(curr.blockPos);
            } else if (currBlockState.getBlockId().equals("base:laser_emitter")) {
                // Ignore the laser emitter if it is pointing elsewhere
                if (!isCausalLaserEmitter(currBlockState, curr.outgoingDir)) { continue; }

                multiscanlineSearchComponent(curr, componentQueue, maxComponentDist);
            } else {
                // Stop searching further through the solid block
                Constants.LOGGER.info("Found solid block {}", curr.blockPos);
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
    }

//    public static boolean stepNextSwitch(BlockPosition startPos, ObjectSet<BlockPosition> ignoredBlocks, int maxComponentDist, int maxComponentsTraversed) {
//        int[][] directions = new int[][]{{1, 0, 0}, {-1, 0, 0}, {0, 1, 0}, {0, -1, 0}, {0, 0, 1}, {0, 0, -1}};
//
//        BlockPosition currPos = startPos.copy();
//        // Keep searching the next component starting from the last found component
//        for (int i = 0; i < maxComponentsTraversed; i++) {
//            // Find the nearest relevant component along each axis
//            axisLoop:
//            for (int d = 1; d <= maxComponentDist; d++) { // Iterate distance
//                for (int iDir = 0; iDir < 6; iDir++) { // Iterate potential directions
//                    if (directions[iDir] == null) {
//                        continue;
//                    }
//
//                    // Get potential component's blockPos and blockState
//                    BlockPosition pos = currPos.getOffsetBlockPos(d * directions[iDir][0], d * directions[iDir][1], d * directions[iDir][2]);
//                    if (pos == null || ignoredBlocks.contains(pos)) {
//                        continue;
//                    } // Do not loop back to the starting point
//                    BlockState posState = pos.getBlockState();
//
//                    // Check that the block is not a directional component such as a laser emitter
//                    if (posState.getParamDirection("direction") == null) {
//                        /* Stop searching in directions obstructed by opaque blocks.
//                         * Subject to inaccuracies for blocks with hitboxes smaller than a full block where lasers are able
//                         * to pass
//                         */
//                        if (!posState.walkThrough && (posState.isOpaque || posState.hasTag(TAG_STOPS_LASERS))) {
//                            directions[iDir] = null;
//                        }
//                        continue;
//                    }
//
//                    boolean axisMatches = (
//                            posState.getParamDirection("direction").getXOffset() == directions[iDir][0]
//                                    && posState.getParamDirection("direction").getYOffset() == directions[iDir][1]
//                                    && posState.getParamDirection("direction").getZOffset() == directions[iDir][2]
//                    );
//
//                    boolean axisInverseMatches = (
//                            posState.getParamDirection("direction").getXOffset() == -directions[iDir][0]
//                                    && posState.getParamDirection("direction").getYOffset() == -directions[iDir][1]
//                                    && posState.getParamDirection("direction").getZOffset() == -directions[iDir][2]
//                    );
//
//                    // Check that we aren't at the laser switch (end goal) yet
//                    if (!posState.getBlockId().equals("base:laser_switch")) {
//                        // If the block is a laser emitter, continue searching from there
//                        if (posState.getBlockId().equals("base:laser_emitter") && axisInverseMatches) {
//                            currPos = pos;
//                            break axisLoop;
//                        }
//                        continue;
//                    }
//
//                    // If the switch's axis matches, the laser will hit the wrong face thus never passing through.
//                    if (axisMatches) { continue; }
//
//                    for (int j = 1; j < maxComponentDist; j++) {
//                        BlockPosition nextPos = pos.getOffsetBlockPos(j * directions[iDir][0], j * directions[iDir][1], j * directions[iDir][2]);
//                        if (nextPos == null) {
//                            Constants.LOGGER.warn("No block at {} + {}, {}, {}", pos, j * directions[iDir][0], j * directions[iDir][1], j * directions[iDir][2]);
//                            break;
//                        }
//                        BlockState nextPosState = nextPos.getBlockState();
//                        if (nextPosState.getBlockId().equals("base:laser_emitter")) {
//                            boolean nextPosAxisInverseMatches;
//                            if (nextPosState.getParam("type").equals("split")) {
//                                // Laser Splitter
//                                nextPosAxisInverseMatches = nextPosState.getParam("axis").equals(
//                                        directions[iDir][0] != 0 ? "X" :
//                                                directions[iDir][1] != 0 ? "Y" :
//                                                        "Z"
//                                );
//                            } else if (nextPosState.getParam("type").equals("single")) {
//                                // Laser Emitter
//                                String laserSwitchDir = posState.getParam("direction");
//                                String laserEmitterDir = nextPosState.getParam("direction");
//
//                                nextPosAxisInverseMatches = (
//                                        laserSwitchDir.charAt(laserSwitchDir.length() - 1) == laserEmitterDir.charAt(laserEmitterDir.length() - 1)
//                                                && !laserSwitchDir.equals(laserEmitterDir)
//                                );
//                            } else {
//                                break;
//                            }
//                            if (nextPosState.getBlockId().equals("base:laser_emitter") && nextPosAxisInverseMatches) {
//                                if (remainingIgnoredSwitches > 0) {
//                                    remainingIgnoredSwitches--;
//                                    ignoredBlocks.add(nextPos);
//                                    break;
//                                }
//                                return pos;
//                            }
//                        }
//                    }
//                }
//            }
//        }
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
