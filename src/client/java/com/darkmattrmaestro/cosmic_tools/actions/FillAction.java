package com.darkmattrmaestro.cosmic_tools.actions;

import finalforeach.cosmicreach.blocks.BlockPosition;
import finalforeach.cosmicreach.blocks.BlockState;
import finalforeach.cosmicreach.world.Zone;
import com.darkmattrmaestro.cosmic_tools.threading.FillingThread;
import com.darkmattrmaestro.cosmic_tools.utils.Selection;
import com.darkmattrmaestro.cosmic_tools.volume.IBlockVolume;
import com.darkmattrmaestro.cosmic_tools.volume.RealVolume;
import com.darkmattrmaestro.cosmic_tools.volume.SolidVolume;

import static com.darkmattrmaestro.cosmic_tools.utils.ChatUtils.nanoToSec;
import static com.darkmattrmaestro.cosmic_tools.utils.ChatUtils.sendMsg;

public class FillAction implements IAction {
    private final IBlockVolume volume;
    private final RealVolume oldBlocks;
    private final Selection selection;
    private final BlockPosition pasteStartPos;

    public FillAction(Selection selection, BlockPosition pasteStartPos, IBlockVolume volume) {
        this.volume = volume;
        this.selection = selection;
        this.oldBlocks = selection.blankVolume();
        this.pasteStartPos = pasteStartPos;
    }

    public static FillAction of(Selection selection, BlockPosition pasteStartPos, IBlockVolume volume) {
        return new FillAction(selection, pasteStartPos, volume);
    }

    public static FillAction of(Selection selection, BlockPosition pasteStartPos, BlockState state) {
        return new FillAction(selection, pasteStartPos, SolidVolume.of(state));
    }

    @Override
    public void applyInternal(Zone zone, boolean verbose) {
        FillingThread.post(zone, oldBlocks, volume, selection, (b) -> true, (t, n) -> {
            if(verbose) sendMsg("Filled " + n + " block(s) in " + nanoToSec(t) + "s");
        },true);
    }

    @Override
    public void pasteInternal(Zone zone, boolean verbose) {
        FillingThread.postPaste(zone, oldBlocks, selection, pasteStartPos, (b) -> true, (t, n) -> {
            if(verbose) sendMsg("Filled " + n + " block(s) in " + nanoToSec(t) + "s");
        },true);
    }

    @Override
    public void undoInternal(Zone zone, boolean verbose, Runnable onSuccess) {
        FillingThread.post(zone, null, oldBlocks, selection, (b) -> true, (t, n) -> {
            if(verbose) sendMsg("Undid filling " + n + " block(s) in " + nanoToSec(t) + "s");
            if(onSuccess != null) onSuccess.run();
        },true);
    }
}
