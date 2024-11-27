package com.etslabs.Commands;

import java.util.Stack;

import com.etslabs.Interfaces.Command;

/**
 * Singleton CommandManager to manage command history for undo/redo functionality.
 */
public class CommandManager {
    // Eager initialization of Singleton instance
    private static final CommandManager instance = new CommandManager();

    private final Stack<Command> undoStack = new Stack<>();
    private final Stack<Command> redoStack = new Stack<>();

    // Private constructor to prevent instantiation
    private CommandManager() {}

    /**
     * Get the Singleton instance of CommandManager.
     * @return the CommandManager instance
     */
    public static CommandManager getInstance() {
        return instance;
    }

    /**
     * Execute a command and add it to the undo stack.
     * @param command the Command to execute
     */
    public void executeCommand(Command command) {
        command.execute();
        undoStack.push(command);
        redoStack.clear(); // Clear redo stack when a new command is executed
    }

    /**
     * Add a command to the undo stack without executing it.
     * Useful when commands are executed elsewhere.
     * @param command the Command to add
     */
    public void addCommand(Command command) {
        undoStack.push(command);
        redoStack.clear();
    }

    /**
     * Undo the last executed command.
     */
    public void undo() {
        if (!undoStack.isEmpty()) {
            Command command = undoStack.pop();
            command.undo();
            redoStack.push(command);
        }
    }

    /**
     * Redo the last undone command.
     */
    public void redo() {
        if (!redoStack.isEmpty()) {
            Command command = redoStack.pop();
            command.execute();
            undoStack.push(command);
        }
    }
}
