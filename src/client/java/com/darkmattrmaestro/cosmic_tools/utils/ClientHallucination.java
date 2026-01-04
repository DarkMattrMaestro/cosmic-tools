package com.darkmattrmaestro.cosmic_tools.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector3;
import finalforeach.cosmicreach.blocks.BlockPosition;
import finalforeach.cosmicreach.gamestates.GameState;
import finalforeach.cosmicreach.gamestates.InGame;

import java.util.ArrayList;

/**
 * Representation of blocks that do not yet exist, that are rendered as ghostly blocks.
 */
public class ClientHallucination {

    public static void draw(Hallucination hallucination, ShapeRenderer sr, Vector3 offset, Color border) {
        if (Hallucination.rawWorldCamera == null) { Hallucination.rawWorldCamera = GameState.IN_GAME.getWorldCamera(); }
        sr.setProjectionMatrix(Hallucination.rawWorldCamera.combined);
        for (BlockPosition blockPosition: hallucination.blocks) {
            Vector3 hallucinatedPos = (new Vector3(blockPosition.getGlobalX(), blockPosition.getGlobalY(), blockPosition.getGlobalZ())).add(offset);
            float width = 1 + 2*hallucination.getInflate();

            float dist = Math.max(0.01f, blockPosition.dst(InGame.getLocalPlayer().getPosition()));
            Gdx.gl.glLineWidth(10/dist);
            sr.begin(ShapeRenderer.ShapeType.Line);
            sr.setColor(border);
            sr.box(hallucinatedPos.x - hallucination.getInflate(), hallucinatedPos.y - hallucination.getInflate(), hallucinatedPos.z + hallucination.getInflate() + 1, width, width, width);
            sr.end();
        }
    }
}
