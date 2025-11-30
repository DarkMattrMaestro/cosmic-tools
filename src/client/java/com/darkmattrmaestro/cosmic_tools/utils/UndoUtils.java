package com.darkmattrmaestro.cosmic_tools.utils;

import finalforeach.cosmicreach.world.Zone;
import com.darkmattrmaestro.cosmic_tools.actions.IAction;

import java.util.Stack;

import static com.darkmattrmaestro.cosmic_tools.utils.ChatUtils.sendMsg;

public class UndoUtils {
    private static final Stack<IAction> undoStack = new Stack<>();
    private static final Stack<IAction> redoStack = new Stack<>();

    public static void pushUndo(IAction action) {
        undoStack.push(action);
        redoStack.clear();
    }

    public static void pushUndoNoReset(IAction action) {
        undoStack.push(action);
    }

    public static void pushRedo(IAction action) {
        redoStack.push(action);
    }

    public static void undo(Zone zone){
        if(!undoStack.isEmpty()){
            IAction action = undoStack.pop();
            action.undo(zone);
        } else {
            sendMsg("Nothing to undo.");
        }
    }

    public static void redo(Zone zone){
        if(!redoStack.isEmpty()){
            IAction action = redoStack.pop();
            action.apply(zone);
        } else {
            sendMsg("Nothing to redo.");
        }
    }
}
