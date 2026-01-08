package com.darkmattrmaestro.cosmic_tools.utils;

import com.badlogic.gdx.InputProcessor;
import com.darkmattrmaestro.cosmic_tools.Constants;
import com.darkmattrmaestro.cosmic_tools.items.ClientScythe;
import com.darkmattrmaestro.cosmic_tools.items.ClientSpatula;
import finalforeach.cosmicreach.items.ItemStack;
import finalforeach.cosmicreach.ui.UI;
import finalforeach.cosmicreach.util.Identifier;

public class CosmicToolsInputProcessor implements InputProcessor {
    @Override
    public boolean keyDown (int keycode) { return false; }

    @Override
    public boolean keyUp (int keycode) {
        return false;
    }

    @Override
    public boolean keyTyped (char character) {
        return false;
    }

    @Override
    public boolean touchDown (int x, int y, int pointer, int button) {
        Constants.LOGGER.warn("Pressed {}", button);
        boolean blockInput = false;
        hotbarItems:
        try {
            ItemStack selected = UI.hotbar.getSelectedItemStack();
            if (selected == null) { break hotbarItems; }

            if(Identifier.of(Constants.MOD_ID, "spatula").toString().equals(selected.getItem().getID())) {
                blockInput |= ClientSpatula.onMousePressed(button);
            } else if (Identifier.of(Constants.MOD_ID, "scythe").toString().equals(selected.getItem().getID())) {
                blockInput |= ClientScythe.onMousePressed(button);
            }
        } catch (Exception e) {}

        return blockInput;
    }

    @Override
    public boolean touchUp (int x, int y, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchCancelled(int i, int i1, int i2, int i3) {
        return false;
    }

    @Override
    public boolean touchDragged (int x, int y, int pointer) {
        return false;
    }

    @Override
    public boolean mouseMoved (int x, int y) {
        return false;
    }

    @Override
    public boolean scrolled (float amountX, float amountY) {
        return false;
    }
}
