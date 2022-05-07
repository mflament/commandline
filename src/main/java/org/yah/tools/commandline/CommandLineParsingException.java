package org.yah.tools.commandline;

public class CommandLineParsingException extends RuntimeException {
    private final CommandLineDefinition definition;

    public CommandLineParsingException(CommandLineDefinition definition, String message) {
        super(message);
        this.definition = definition;
    }

    public CommandLineDefinition getDefinition() {
        return definition;
    }
}
