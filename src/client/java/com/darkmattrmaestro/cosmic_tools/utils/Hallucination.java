package com.darkmattrmaestro.cosmic_tools.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector3;
import finalforeach.cosmicreach.blocks.BlockPosition;
import finalforeach.cosmicreach.gamestates.GameState;
import finalforeach.cosmicreach.gamestates.InGame;

import java.util.ArrayList;
import java.util.Objects;

public class Hallucination {
    public ArrayList<BlockPosition> blocks = new ArrayList<BlockPosition>();

    public static final Color borderColor = new Color(0.64f, 0.64f, 0.64f, 1);

    public static Camera rawWorldCamera = GameState.IN_GAME.getWorldCamera();
    public static final float inflate = -0.05f;
    private float customInflate = 0f;

    public Hallucination() {

    }

    public Hallucination(ArrayList<BlockPosition> blocks) {
        this.blocks = blocks;
    }

    public void draw(ShapeRenderer sr, Vector3 offset) {
        this.draw(sr, offset, borderColor);
    }

    public void draw(ShapeRenderer sr, Vector3 offset, Color border) {
        if (rawWorldCamera == null) { rawWorldCamera = GameState.IN_GAME.getWorldCamera(); }
        sr.setProjectionMatrix(rawWorldCamera.combined);
        for (BlockPosition blockPosition: blocks) {
            Vector3 hallucinatedPos = (new Vector3(blockPosition.getGlobalX(), blockPosition.getGlobalY(), blockPosition.getGlobalZ())).add(offset);
            float width = 1 + 2*getInflate();

            float dist = Math.max(0.01f, blockPosition.dst(InGame.getLocalPlayer().getPosition()));
            Gdx.gl.glLineWidth(10/dist);
            sr.begin(ShapeRenderer.ShapeType.Line);
            sr.setColor(border);
            sr.box(hallucinatedPos.x - getInflate(), hallucinatedPos.y - getInflate(), hallucinatedPos.z + getInflate() + 1, width, width, width);
            sr.end();
        }
    }

    public Hallucination setCustomInflate(float customInflate) {
        this.customInflate = customInflate;
        return this;
    }

    public float getInflate() {
        return inflate + this.customInflate;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Hallucination hallucination = (Hallucination) o;
        return blocks.equals(hallucination.blocks);
    }

    @Override
    public int hashCode() {
            return Objects.hash(blocks);
        }
}
