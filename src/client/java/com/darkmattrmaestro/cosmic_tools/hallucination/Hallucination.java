package com.darkmattrmaestro.cosmic_tools.hallucination;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.darkmattrmaestro.cosmic_tools.items.Spatula;
import com.darkmattrmaestro.cosmic_tools.utils.Vector3Int;
import finalforeach.cosmicreach.blocks.BlockPosition;
import finalforeach.cosmicreach.gamestates.GameState;
import finalforeach.cosmicreach.gamestates.InGame;

import java.util.ArrayList;
import java.util.Objects;

public class Hallucination {
    public ArrayList<BlockPosition> blocks = new ArrayList<BlockPosition>();

    public static final Color fillColor = new Color(0.64f, 0.64f, 0.64f, 0.05f);
    public static final Color borderColor = new Color(0.64f, 0.64f, 0.64f, 1);

    public static Camera rawWorldCamera = GameState.IN_GAME.getWorldCamera();
    public static final float inflate = -0.05f;
    //public static final float borderInflate = 0.001f;
    private float customInflate = 0f;

    public Hallucination() {

    }

    public Hallucination(ArrayList<BlockPosition> blocks) {
        this.blocks = blocks;
    }

    public void draw(ShapeRenderer sr) {
        this.draw(sr, fillColor, borderColor);
    }

    public void draw(ShapeRenderer sr, Color fill, Color border) {
        if (rawWorldCamera == null) { rawWorldCamera = GameState.IN_GAME.getWorldCamera(); }
        sr.setProjectionMatrix(rawWorldCamera.combined);
        for (BlockPosition blockPosition: blocks) {
            Vector3Int hallucinatedPos = (new Vector3Int(blockPosition.getGlobalX(), blockPosition.getGlobalY(), blockPosition.getGlobalZ())).add(Spatula.blockAxis.axis);
            float width = 1 + 2*getInflate();

//            sr.begin(ShapeRenderer.ShapeType.Filled);
//            sr.setColor(fill);
//            sr.box(hallucinatedPos.x - getInflate(), hallucinatedPos.y - getInflate(), hallucinatedPos.z + getInflate() + 1, width, width, width);
//            sr.box(hallucinatedPos.x + getInflate() + 1, hallucinatedPos.y + getInflate() + 1, hallucinatedPos.z - getInflate(), -width, -width, -width);
//            sr.end();

//            for (int[] diff: new int[][]{{0, 0, 0}, {1, 0, 0}, {0, 1, 0}, {0, 0, 1}, {1, 1, 0}, {1, 0, 1}, {0, 1, 1}, {1, 1, 1}})
            float dist = Math.max(0.01f, blockPosition.dst(InGame.getLocalPlayer().getPosition()));
            Gdx.gl.glLineWidth(10/dist);
            sr.begin(ShapeRenderer.ShapeType.Line);
            sr.setColor(border);
            sr.box(hallucinatedPos.x - getInflate(), hallucinatedPos.y - getInflate(), hallucinatedPos.z + getInflate() + 1, width, width, width);
//            sr.line(hallucinatedPos.x - getInflate(), hallucinatedPos.y - getInflate(), hallucinatedPos.z + getInflate() + 1, hallucinatedPos.x - getInflate(), hallucinatedPos.y - getInflate(), hallucinatedPos.z + getInflate() + 1);
            //sr.box(bb.min.x + eps * 0.5f, bb.min.y + eps * 0.5f, bb.min.z + bb.getDepth() - eps * 0.5f, bb.getWidth() - eps, bb.getHeight() - eps, bb.getDepth() - eps);
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
