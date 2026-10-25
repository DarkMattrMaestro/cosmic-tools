package com.darkmattrmaestro.cosmic_tools.utils;

import com.badlogic.gdx.utils.ByteArray;
import com.darkmattrmaestro.cosmic_tools.Constants;
import finalforeach.cosmicreach.blocks.blockentities.BlockEntity;
import finalforeach.cosmicreach.blocks.blockentities.BlockEntityCreator;
import finalforeach.cosmicreach.savelib.IByteArray;
import finalforeach.cosmicreach.savelib.crbin.CRBinDeserializer;
import finalforeach.cosmicreach.savelib.crbin.CRBinSerializer;
import finalforeach.cosmicreach.savelib.utils.DynamicArrays;
import finalforeach.cosmicreach.singletons.GameSingletons;
import finalforeach.cosmicreach.lighting.LightPropagator;
import finalforeach.cosmicreach.world.Chunk;
import finalforeach.cosmicreach.world.Zone;

import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;

/**
 * This class was originally part of nab138's <a href="https://github.com/nab138/Ecliptic">Ecliptic</a> mod, licensed under the MIT license.
 */
public class ChunkUtils {
    private static final LightPropagator lightProp = new LightPropagator();

    public static void remesh(List<Chunk> chunksToUpdate, Zone zone) {
        for(Chunk c : chunksToUpdate){
            c.setGenerated(true);
        }
        for(Chunk c : chunksToUpdate){
            remesh(c, zone, false);
        }
        GameSingletons.meshGenThread.requestImmediateResorting();
    }

    public static void remesh(Chunk c, Zone zone, boolean single) {
        if(single) c.setGenerated(true);
        c.flagForRemeshing(true);
        boolean isSky = c.chunkY >= -1;
        if (isSky) {
            for(int i = c.chunkY; i < c.chunkY + 16; ++i) {
                if (zone.getChunkAtChunkCoords(c.chunkX, c.chunkY + i, c.chunkZ) != null) {
                    isSky = false;
                    break;
                }
            }
        }
        lightProp.calculateLightingForChunk(zone, c, isSky);
        if(single) GameSingletons.meshGenThread.requestImmediateResorting();
    }

    /**
     * Set a block entity at the given block position, where the new block entity is a duplicate not linked to the
     * provided block entity. Not referencing the same object ensures that both blocks at different positions refer to
     * separate block entity objects.
     *
     * @param zone zone of the new blocks
     * @param block descriptor for the new block
     * @param x new block's x position
     * @param y new block's y position
     * @param z new block's z position
     */
    public static void setBlockEntity(Zone zone, MainBlockDescriptor block, int x, int y, int z) {
        if (block.blockEntity == null) { return; }
        int cx = Math.floorDiv(x, 16);
        int cy = Math.floorDiv(y, 16);
        int cz = Math.floorDiv(z, 16);
        Chunk c = zone.getChunkAtChunkCoords(cx, cy, cz);
        if (c == null) {
            c = new Chunk(cx, cy, cz);
            c.initChunkData();
            zone.addChunk(c);
        }

        // Write to byte array
        CRBinSerializer serial = new CRBinSerializer();
        serial.reset();
        block.blockEntity.write(serial);
        byte[] tempBytes = serial.toBytes();

        // Read back from byte array, creating new object
        CRBinDeserializer deserializer = new CRBinDeserializer();
        deserializer.prepareForRead(ByteBuffer.wrap(tempBytes));
        BlockEntity entity = BlockEntityCreator.getBlockEntityCreator(block.blockEntity.getBlockEntityId()).create(block.blockState, zone, x, y, z);
        if (entity != null) {
            entity.read(deserializer);
            entity.setGlobalPosition(x, y, z);
            c.setBlockEntityDirect(block.blockState, entity, x, y, z, true);
        } else {
            Constants.LOGGER.warn("Could not copy block entity to {} {} {}", x, y, z);
        }
    }
}
