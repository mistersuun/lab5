package com.etslabs.Commands;

import java.util.Stack;

import com.etslabs.Interfaces.Command;

public class CommandManager {
    private static final CommandManager instance = new CommandManager();

    private final Stack<Command> undoStack = new Stack<>();
    private final Stack<Command> redoStack = new Stack<>();

    private CommandManager() {}

    public static CommandManager getInstance() {
        return instance;
    }

    public void executeCommand(Command command) {
        command.execute();
        undoStack.push(command);
        redoStack.clear(); 
    }

    public void addCommand(Command command) {
        undoStack.push(command);
        redoStack.clear();
    }

    public void undo() {
        if (!undoStack.isEmpty()) {
            Command command = undoStack.pop();
            command.undo();
            redoStack.push(command);
        }
    }

    public void redo() {
        if (!redoStack.isEmpty()) {
            Command command = redoStack.pop();
            command.execute();
            undoStack.push(command);
        }
    }
}
