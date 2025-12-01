package com.darkmattrmaestro.cosmic_tools.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.utils.Array;
import finalforeach.cosmicreach.gamestates.GameState;
import finalforeach.cosmicreach.gamestates.InGame;
import com.darkmattrmaestro.cosmic_tools.Constants;
import org.jetbrains.annotations.NotNull;
import java.lang.reflect.InvocationTargetException;

//import com.github.puzzle.core.loader.util.MethodUtil;
//import com.github.puzzle.core.loader.util.Reflection;
import finalforeach.cosmicreach.blocks.BlockPosition;
import finalforeach.cosmicreach.blocks.BlockState;
import finalforeach.cosmicreach.blocks.BlockStateMissing;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static com.badlogic.gdx.math.MathUtils.floor;

public class BlockSelectionUtil {
    @SuppressWarnings("unchecked")
    public static @NotNull Field getField(@NotNull Class<?> clazz, String fieldName){
        try {
            Field f = clazz.getField(fieldName);
            f.setAccessible(true);
            return f;
        } catch (NoSuchFieldException ignore) {
            try {
                Field f = clazz.getDeclaredField(fieldName);
                f.setAccessible(true);
                return f;
            } catch (NoSuchFieldException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static Field getField(Object instance, String fieldName) {
        return getField(instance.getClass(), fieldName);
    }

    public static @NotNull Method getMethod(@NotNull Class<?> clazz, String name) {
        try {
            Method m = clazz.getMethod(name);
            m.setAccessible(true);
            return m;
        } catch (NoSuchMethodException ignore) {
            try {
                Method m = clazz.getDeclaredMethod(name);
                m.setAccessible(true);
                return m;
            } catch (NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static Object runMethod(Object obj, @NotNull Method method) {
        try {
            return method.invoke(obj);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    public static BlockPosition getBlockPositionLookingAt() {
        try {
            Class<?> gameState = Class.forName("finalforeach.cosmicreach.gamestates.GameState");
            Field inGameField = gameState.getDeclaredField("IN_GAME");
            Object inGame = inGameField.get(null);
            Object blockSelection = getField(inGame, "blockSelection").get(inGame);
            Method method = getMethod(blockSelection.getClass(), "getBlockPositionLookingAt");
            return (BlockPosition) runMethod(blockSelection, method);
        } catch (ClassNotFoundException | NoSuchFieldException | IllegalAccessException e) {
            return new BlockPosition(null, 0, 0, 0);
        }
    }

    public static BlockState getBlockLookingAt() {
        try {
            Class<?> gameState = Class.forName("finalforeach.cosmicreach.gamestates.GameState");
            Field inGameField = gameState.getDeclaredField("IN_GAME");
            Object inGame = inGameField.get(null);
            Object blockSelection = getField(inGame, "blockSelection").get(inGame);
            Method method = getMethod(blockSelection.getClass(), "getBlockLookingAt");
            return (BlockState) runMethod(blockSelection, method);
        } catch (ClassNotFoundException | NoSuchFieldException | IllegalAccessException e) {
            return BlockStateMissing.fromMissingKey("nuhuh");
        }
    }

    public static boolean isWithin(BlockPosition blockPos, Selection sel) {
        return (
            blockPos.getGlobalX() >= sel.minX
            && blockPos.getGlobalY() >= sel.minY
            && blockPos.getGlobalZ() >= sel.minZ
            && blockPos.getGlobalX() <= sel.maxX
            && blockPos.getGlobalY() <= sel.maxY
            && blockPos.getGlobalZ() <= sel.maxZ
        );
    }

    /**
     * Check if there is a block collision within the given max distance.
     *
     * See http://www.cse.yorku.ca/~amana/research/grid.pdf for the voxel iteration method.
     *
     * @param maxDist
     * @return
     */
    public static boolean doesCollideFar(float maxDist) {
        if (GameState.currentGameState.getClass() != InGame.class) { return false; }

        Camera worldCamera = GameState.IN_GAME.getWorldCamera();
        Ray ray = new Ray();
        Vector3 mouseCoords = new Vector3();
        Vector3 mouseCoords2 = new Vector3();
        if (Gdx.input.isCursorCatched()) {
            ray.set(worldCamera.position, worldCamera.direction);
        } else {
            mouseCoords.set((float)Gdx.input.getX(), (float)Gdx.input.getY(), 0.0F);
            mouseCoords2.set((float)Gdx.input.getX(), (float)Gdx.input.getY(), 1.0F);
            worldCamera.unproject(mouseCoords);
            worldCamera.unproject(mouseCoords2);
            mouseCoords2.sub(mouseCoords).nor();
            ray.set(mouseCoords, mouseCoords2);
        }

        //-------------------------//

        int x = (int) Math.floor(ray.origin.x);
        int y = (int) Math.floor(ray.origin.y);
        int z = (int) Math.floor(ray.origin.z);

        int stepX = 0 < ray.direction.x ? 1 : (ray.direction.x < 0 ? -1 : 0);
        int stepY = 0 < ray.direction.y ? 1 : (ray.direction.y < 0 ? -1 : 0);
        int stepZ = 0 < ray.direction.z ? 1 : (ray.direction.z < 0 ? -1 : 0);

        double tMaxX = Integer.MAX_VALUE;
        double tDeltaX = Integer.MAX_VALUE;
        if (ray.direction.x != 0) {
            tMaxX = ((x + (stepX > 0 ? 1 : 0)) - ray.origin.x) / ray.direction.x;
            tDeltaX = stepX / ray.direction.x;
        }

        double tMaxY = Integer.MAX_VALUE;
        double tDeltaY = Integer.MAX_VALUE;
        if (ray.direction.y != 0) {
            tMaxY = ((y + (stepY > 0 ? 1 : 0)) - ray.origin.y) / ray.direction.y;
            tDeltaY = stepY / ray.direction.y;
        }

        double tMaxZ = Integer.MAX_VALUE;
        double tDeltaZ = Integer.MAX_VALUE;
        if (ray.direction.z != 0) {
            tMaxZ = ((z + (stepZ > 0 ? 1 : 0)) - ray.origin.z) / ray.direction.z;
            tDeltaZ = stepZ / ray.direction.z;
        }

        BlockAxis blockAxis = new BlockAxis();
        double closestCollision = Double.POSITIVE_INFINITY;

        iterateBlocks:
        // For each step, determine which distance to the next voxel boundary is lowest (i.e.
        // which voxel boundary is nearest) and walk that way.
        for (int i = 0; i < maxDist; i++)
        {
            // Check for collisions with a block
            BlockPosition blockPosAdj = BlockPosition.ofGlobal(InGame.getLocalPlayer().getZone(), x, y, z);
            if (blockPosAdj.chunk() == null) {
                return false;
            }
            BlockState blockAdj = blockPosAdj.getBlockState();
            if (blockAdj != null) {// && !blockAdj.walkThrough && (blockAdj.isOpaque || blockAdj.hasTag(TAG_STOPS_LASERS))) {
                // Get main AABB of block and check for collision
                BoundingBox mainAABB = new BoundingBox();
                blockAdj.getBoundingBox(mainAABB, x, y, z);
                if (CustomGameMath.rayAABBTest(ray, mainAABB, maxDist)) {
                    // Get sub AABBs and check individually for collisions
                    Array<BoundingBox> subAABBs = new Array<>(BoundingBox.class);

                    Array<BoundingBox> tmpBlockBoundingBoxes = new Array<BoundingBox>();
                    blockAdj.getAllBoundingBoxes(subAABBs, x, y, z);
                    for (BoundingBox subAABB : subAABBs) {
                        if (CustomGameMath.rayAABBTest(ray, subAABB, maxDist)) {

                            double dist = CustomGameMath.rayAABBTest(ray, subAABB);
//                            Constants.LOGGER.warn("closestCollision: {} dist: {}", closestCollision, dist);
                            if (dist < maxDist && !(subAABB.getWidth() == 0 || subAABB.getHeight() == 0 || subAABB.getDepth() == 0)) {
                                return true;
                            }
                        }
                    }
                }
            }

            // Do the next step.
            if (tMaxX < tMaxY && tMaxX < tMaxZ)
            {
                // tMax.X is the lowest, an YZ cell boundary plane is nearest.
                x += stepX;
                tMaxX += tDeltaX;
            }
            else if (tMaxY < tMaxZ)
            {
                // tMax.Y is the lowest, an XZ cell boundary plane is nearest.
                y += stepY;
                tMaxY += tDeltaY;
            }
            else
            {
                // tMax.Z is the lowest, an XY cell boundary plane is nearest.
                z += stepZ;
                tMaxZ += tDeltaZ;
            }
        }

        return false;
    }

    /**
     * See http://www.cse.yorku.ca/~amana/research/grid.pdf for the voxel iteration method.
     *
     * @param maxDist
     * @return
     */
    public static BlockAxis getBlockSideLookingAtFar(float maxDist) {
        if (GameState.currentGameState.getClass() != InGame.class) { return null; }

        Camera worldCamera = GameState.IN_GAME.getWorldCamera();
        Ray ray = new Ray();
        Vector3 mouseCoords = new Vector3();
        Vector3 mouseCoords2 = new Vector3();
        if (Gdx.input.isCursorCatched()) {
            ray.set(worldCamera.position, worldCamera.direction);
        } else {
            mouseCoords.set((float)Gdx.input.getX(), (float)Gdx.input.getY(), 0.0F);
            mouseCoords2.set((float)Gdx.input.getX(), (float)Gdx.input.getY(), 1.0F);
            worldCamera.unproject(mouseCoords);
            worldCamera.unproject(mouseCoords2);
            mouseCoords2.sub(mouseCoords).nor();
            ray.set(mouseCoords, mouseCoords2);
        }

        //-------------------------//

        int x = (int) Math.floor(ray.origin.x);
        int y = (int) Math.floor(ray.origin.y);
        int z = (int) Math.floor(ray.origin.z);

        int stepX = 0 < ray.direction.x ? 1 : (ray.direction.x < 0 ? -1 : 0);
        int stepY = 0 < ray.direction.y ? 1 : (ray.direction.y < 0 ? -1 : 0);
        int stepZ = 0 < ray.direction.z ? 1 : (ray.direction.z < 0 ? -1 : 0);

        double tMaxX = Integer.MAX_VALUE;
        double tDeltaX = Integer.MAX_VALUE;
        if (ray.direction.x != 0) {
            tMaxX = ((x + (stepX > 0 ? 1 : 0)) - ray.origin.x) / ray.direction.x;
            tDeltaX = stepX / ray.direction.x;
        }

        double tMaxY = Integer.MAX_VALUE;
        double tDeltaY = Integer.MAX_VALUE;
        if (ray.direction.y != 0) {
            tMaxY = ((y + (stepY > 0 ? 1 : 0)) - ray.origin.y) / ray.direction.y;
            tDeltaY = stepY / ray.direction.y;
        }

        double tMaxZ = Integer.MAX_VALUE;
        double tDeltaZ = Integer.MAX_VALUE;
        if (ray.direction.z != 0) {
            tMaxZ = ((z + (stepZ > 0 ? 1 : 0)) - ray.origin.z) / ray.direction.z;
            tDeltaZ = stepZ / ray.direction.z;
        }

        BlockAxis blockAxis = new BlockAxis();
        double closestCollision = Double.POSITIVE_INFINITY;

        iterateBlocks:
        // For each step, determine which distance to the next voxel boundary is lowest (i.e.
        // which voxel boundary is nearest) and walk that way.
        for (int i = 0; i < maxDist; i++)
        {
            // Check for collisions with a block
            BlockPosition blockPosAdj = BlockPosition.ofGlobal(InGame.getLocalPlayer().getZone(), x, y, z);
            if (blockPosAdj.chunk() == null) {
                return null;
            }
            BlockState blockAdj = blockPosAdj.getBlockState();
            if (blockAdj != null) {// && !blockAdj.walkThrough && (blockAdj.isOpaque || blockAdj.hasTag(TAG_STOPS_LASERS))) {
                // Get main AABB of block and check for collision
                BoundingBox mainAABB = new BoundingBox();
                blockAdj.getBoundingBox(mainAABB, x, y, z);
                if (CustomGameMath.rayAABBTest(ray, mainAABB, maxDist)) {
                    // Get sub AABBs and check individually for collisions
                    Array<BoundingBox> subAABBs = new Array<>(BoundingBox.class);

                    Array<BoundingBox> tmpBlockBoundingBoxes = new Array<BoundingBox>();
                    blockAdj.getAllBoundingBoxes(subAABBs, x, y, z);
                    for (BoundingBox subAABB : subAABBs) {
                        if (CustomGameMath.rayAABBTest(ray, subAABB, maxDist)) {
                            // Collision detected
                            blockAxis.pos = BlockPosition.ofGlobal(InGame.getLocalPlayer().getZone(), x, y, z);

                            double dist = CustomGameMath.rayAABBTest(ray, subAABB);
//                            Constants.LOGGER.warn("closestCollision: {} dist: {}", closestCollision, dist);
                            if (dist < closestCollision && !(subAABB.getWidth() == 0 || subAABB.getHeight() == 0 || subAABB.getDepth() == 0)) {
                                closestCollision = dist;

                                Vector3 collisionPos = new Vector3();
                                ray.getEndPoint(collisionPos, (float) dist);

                                float dx = Math.abs(subAABB.getCenterX() - collisionPos.x) / subAABB.getWidth();
                                float dy = Math.abs(subAABB.getCenterY() - collisionPos.y) / subAABB.getHeight();
                                float dz = Math.abs(subAABB.getCenterZ() - collisionPos.z) / subAABB.getDepth();

                                if (dx > dy && dx > dz) { // X axis collision
                                    blockAxis.axis = new Vector3Int(
                                            collisionPos.x < subAABB.getCenterX() ? -1 : 1,
                                            0,
                                            0
                                    );
                                } else if (dy > dz) { // X axis collision
                                    blockAxis.axis = new Vector3Int(
                                            0,
                                            collisionPos.y < subAABB.getCenterY() ? -1 : 1,
                                            0
                                    );
                                } else {
                                    blockAxis.axis = new Vector3Int(
                                            0,
                                            0,
                                            collisionPos.z < subAABB.getCenterZ() ? -1 : 1
                                    );
                                }

                                Vector3 center = new Vector3();
                                subAABB.getCenter(center);
                            }
                        }
                    }
                    if (blockAxis.pos != null) { break iterateBlocks; }
                }
            }

            // Do the next step.
            if (tMaxX < tMaxY && tMaxX < tMaxZ)
            {
                // tMax.X is the lowest, an YZ cell boundary plane is nearest.
                x += stepX;
                tMaxX += tDeltaX;
            }
            else if (tMaxY < tMaxZ)
            {
                // tMax.Y is the lowest, an XZ cell boundary plane is nearest.
                y += stepY;
                tMaxY += tDeltaY;
            }
            else
            {
                // tMax.Z is the lowest, an XY cell boundary plane is nearest.
                z += stepZ;
                tMaxZ += tDeltaZ;
            }
        }

        if (blockAxis.pos != null && blockAxis.axis != null) {
            return blockAxis;
        }

        return null;
    }

    public static BlockPosition getBlockPositionLookingAtFar(float maxDist) {
        if (GameState.currentGameState.getClass() != InGame.class) { return null; }

        Camera worldCamera = GameState.IN_GAME.getWorldCamera();
        Ray ray = new Ray();
        Vector3 mouseCoords = new Vector3();
        Vector3 mouseCoords2 = new Vector3();
        if (Gdx.input.isCursorCatched()) {
            ray.set(worldCamera.position, worldCamera.direction);
        } else {
            mouseCoords.set((float)Gdx.input.getX(), (float)Gdx.input.getY(), 0.0F);
            mouseCoords2.set((float)Gdx.input.getX(), (float)Gdx.input.getY(), 1.0F);
            worldCamera.unproject(mouseCoords);
            worldCamera.unproject(mouseCoords2);
            mouseCoords2.sub(mouseCoords).nor();
            ray.set(mouseCoords, mouseCoords2);
        }

        //-------------------------//

        // Blocks
        Vector3 nearestBlock = null;
        double nearestCollisionDist = Double.POSITIVE_INFINITY;

        Vector3 pos = new Vector3();
        Vector3 lastPos = new Vector3();
        ray.getEndPoint(pos, 0f);
        for (int len = 1; len <= maxDist; len++) {
            lastPos.set(pos);
            ray.getEndPoint(pos, len);
            int minBx = (int) Math.floor(Math.min(lastPos.x, pos.x));
            int minBy = (int) Math.floor(Math.min(lastPos.y, pos.y));
            int minBz = (int) Math.floor(Math.min(lastPos.z, pos.z));
            int maxBx = (int) Math.ceil(Math.max(lastPos.x, pos.x));
            int maxBy = (int) Math.ceil(Math.max(lastPos.y, pos.y));
            int maxBz = (int) Math.ceil(Math.max(lastPos.z, pos.z));

            int bxStart = minBx;
            int byStart = minBy;
            int bzStart = minBz;
            int stepX = 1;
            int stepY = 1;
            int stepZ = 1;
            if (lastPos.x > pos.x) {
                bxStart = maxBx;
                stepX = -1;
            }

            if (lastPos.y > pos.y) {
                byStart = maxBy;
                stepY = -1;
            }

            if (lastPos.z > pos.z) {
                bzStart = maxBz;
                stepZ = -1;
            }

            // Iterate each block
            for (int bx = bxStart; bx >= minBx && bx <= maxBx; bx += stepX) {
                for (int by = byStart; by >= minBy && by <= maxBy; by += stepY) {
                    for (int bz = bzStart; bz >= minBz && bz <= maxBz; bz += stepZ) {

                        // Check for collisions with a block
                        BlockPosition blockPosAdj = BlockPosition.ofGlobal(InGame.getLocalPlayer().getZone(), bx, by, bz);
                        if (blockPosAdj.chunk() == null) {
//                            Constants.LOGGER.error("(See getBlockPositionLookingAtFar) Chunk is null! Zone: {}", InGame.getLocalPlayer().getZone());
                            return null;
                        }
                        BlockState blockAdj = blockPosAdj.getBlockState();
                        if (blockAdj != null) {// && !blockAdj.walkThrough && (blockAdj.isOpaque || blockAdj.hasTag(TAG_STOPS_LASERS))) {
                            // Get main AABB of block and check for collision
                            BoundingBox mainAABB = new BoundingBox();
                            blockAdj.getBoundingBox(mainAABB, bx, by, bz);
                            double mainCollisionDistance = CustomGameMath.segmentAABBCollisionDist(ray, mainAABB);
                            if (mainCollisionDistance != -1 && mainCollisionDistance < nearestCollisionDist) {
                                // Get sub AABBs and check individually for collisions
                                com.badlogic.gdx.utils.Array<com.badlogic.gdx.math.collision.BoundingBox> subAABBs = new Array<>(BoundingBox.class);

                                Array<BoundingBox> tmpBlockBoundingBoxes = new Array<BoundingBox>();
                                blockAdj.getAllBoundingBoxes(subAABBs, bx, by, bz);
                                for (BoundingBox subAABB : subAABBs) {
                                    double subCollisionDistance = CustomGameMath.segmentAABBCollisionDist(ray, subAABB);
                                    if (subCollisionDistance != -1 && subCollisionDistance < nearestCollisionDist) {
                                        nearestCollisionDist = subCollisionDistance;
                                        nearestBlock = new Vector3(bx, by, bz);
                                    }
                                }
                            }
                        }

                    }
                }

                // TODO: Use weighted method and break early since the ray length will only be increasing.
            }

            // Check that a collision occurs within the distance travelled by the entity
            if (nearestCollisionDist <= maxDist) {
                if (nearestBlock != null) {
                    return BlockPosition.ofGlobal(InGame.getLocalPlayer().getZone(), floor(nearestBlock.x), floor(nearestBlock.y), floor(nearestBlock.z));
                }
            }
        }

        return null;
    }
}
