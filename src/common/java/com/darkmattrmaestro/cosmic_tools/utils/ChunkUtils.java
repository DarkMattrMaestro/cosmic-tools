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

    public static void setBlockEntity(Zone zone, MainBlockDescriptor block, int x, int y, int z) {
        int cx = Math.floorDiv(x, 16);
        int cy = Math.floorDiv(y, 16);
        int cz = Math.floorDiv(z, 16);
        Chunk c = zone.getChunkAtChunkCoords(cx, cy, cz);
        if (c == null) {
            c = new Chunk(cx, cy, cz);
            c.initChunkData();
            zone.addChunk(c);
        }

        // Attempt 1

        // Write to byte array
        CRBinSerializer serial = new CRBinSerializer();
        serial.reset();
        block.blockEntity.write(serial);
        byte[] tempBytes = serial.toBytes();

        Constants.LOGGER.info("Read byteArray: {}", tempBytes);

        // Read back from byte array, creating new object
        CRBinDeserializer deserializer = new CRBinDeserializer();
        deserializer.prepareForRead(ByteBuffer.wrap(tempBytes));
        BlockEntity entity = BlockEntityCreator.getBlockEntityCreator(block.blockEntity.getBlockEntityId()).create(block.blockState, zone, x, y, z);
        Constants.LOGGER.warn(entity);
        Constants.LOGGER.warn(BlockEntityCreator.getBlockEntityCreator(block.blockEntity.getBlockEntityId()));
        if (entity != null) {
            Constants.LOGGER.warn("Entity not null");
            entity.read(deserializer);
        } else {
            Constants.LOGGER.warn("Entity is null!");
        }


//        // Attempt 2
//
//        BlockEntity newBlockEntity = BlockEntityCreator.get(block.blockState, zone, x, y, z);
//
//        for (Field field : block.blockEntity.getClass().getFields()) {
////            if (Arrays.asList((new String[]{"signalMap"})).contains(field.getName())) {
////                continue;
////            }
//            try {
//                field.setAccessible(true);
//                Object resObj = field.get(block.blockEntity);
//                Constants.LOGGER.info("Copied field {}", field.getName());
//                field.set(newBlockEntity, resObj);
//            } catch (Exception _) {
//                Constants.LOGGER.warn("Could not set field {}", field.getName());
//            }
//        }
//
//        newBlockEntity.setGlobalPosition(x, y, z);
        c.setBlockEntityDirect(block.blockState, entity, x, y, z, false);
    }
}
