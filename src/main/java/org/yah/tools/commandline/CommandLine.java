package org.yah.tools.commandline;

import org.yah.tools.commandline.CommandLineDefinition.Option;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * A parsed command line option and parameters.<br/>
 * Options are arguments prefixed with one of the ({@link Option#names()} and an optional following argument
 * for option value (if {@link Option#argName()} is not null).
 * Parameters are list of other arguments.
 *
 * @param definition the command line definition
 * @param parameters the command line parameters
 * @param options    the parsed options with optional value.
 */
public record CommandLine(CommandLineDefinition definition, List<String> parameters, Map<String, String> options) {

    /**
     * Create a {@link CommandLineDefinitionBuilder} to define the command line option and parameters.
     *
     * @param programName the name of the program to display in formatted help()
     */
    public static CommandLineDefinitionBuilder define(String programName) {
        return new CommandLineDefinitionBuilder(programName);
    }

    /**
     * @return the command line formatted help
     */
    public String help() {
        return definition.help();
    }

    /**
     * @return the first parameter if any, null otherwise.
     */
    @Nullable
    public String parameter() {
        return parameters.isEmpty() ? null : parameters.get(0);
    }

    /**
     * @param name the option name
     * @return true if this option is present
     */
    public boolean hasOption(String name) {
        return options.containsKey(name);
    }

    public String option(String name) {
        return option(name, null);
    }

    public String option(String name, String defaultValue) {
        return options.getOrDefault(name, defaultValue);
    }

    public <T> T parseOption(String name, Function<String, T> parser) {
        String s = option(name);
        if (s == null)
            throw new IllegalArgumentException("Missing required option " + name);
        return parser.apply(s);
    }

    public <T> T tryParseOption(String name, Function<String, T> parser, T defaultValue) {
        String s = option(name);
        if (s == null) return defaultValue;
        try {
            return parser.apply(s);
        } catch (IllegalArgumentException e) {
            return defaultValue;
        }
    }

}
