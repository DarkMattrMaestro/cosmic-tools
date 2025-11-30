package com.darkmattrmaestro.cosmic_tools.actions;

import finalforeach.cosmicreach.world.Zone;
import com.darkmattrmaestro.cosmic_tools.threading.FillingThread;
import com.darkmattrmaestro.cosmic_tools.utils.UndoUtils;

import static com.darkmattrmaestro.cosmic_tools.utils.ChatUtils.sendMsg;

public interface IAction {
    /**
     * Do not call.
     * @param zone the zone to apply the action to
     * @param verbose whether to print details about the action
     */
    void applyInternal(Zone zone, boolean verbose);

    /**
     * Applies the action to the given zone.
     * @param zone the zone to apply the action to
     * @param verbose whether to print details about the action
     */
    default void apply(Zone zone, boolean verbose){
        if(FillingThread.isRunning()){
            sendMsg("Please wait for the current action to finish before applying a new one.");
        }
        applyInternal(zone, verbose);
        UndoUtils.pushUndo(this);
    }

    /**
     * Applies the action to the given zone and prints details about the action.
     * @param zone the zone to apply the action to
     */
    default void apply(Zone zone) {
        apply(zone, true);
    }

    /**
     * Do not call.
     * @param zone the zone to apply the action to
     * @param verbose whether to print details about the action
     */
    void pasteInternal(Zone zone, boolean verbose);

    /**
     * Applies the action to the given zone.
     * @param zone the zone to apply the action to
     * @param verbose whether to print details about the action
     */
    default void paste(Zone zone, boolean verbose){
        if(FillingThread.isRunning()){
            sendMsg("Please wait for the current action to finish before applying a new one.");
        }
        pasteInternal(zone, verbose);
        UndoUtils.pushUndo(this);
    }

    /**
     * Applies the action to the given zone and prints details about the action.
     * @param zone the zone to apply the action to
     */
    default void paste(Zone zone) {
        paste(zone, true);
    }

    /**
     * Do not call.
     * @param zone the zone to undo the action on
     * @param verbose whether to print details about the action
     * @param onDone the action to run when the undo is done
     */
    void undoInternal(Zone zone, boolean verbose, Runnable onDone);

    /**
     * Undoes the action on the given zone.
     * @param zone the zone to undo the action on
     * @param verbose whether to print details about the action
     */
    default void undo(Zone zone, boolean verbose) {
        if(FillingThread.isRunning()){
            sendMsg("Please wait for the current action to finish before applying a new one.");
            UndoUtils.pushUndoNoReset(this);
        }
        undoInternal(zone, verbose, () -> UndoUtils.pushRedo(this));
    }

    /**
     * Undoes the action on the given zone.
     * @param zone the zone to undo the action on
     */
    default void undo(Zone zone) {
        undo(zone, true);
    }
}
