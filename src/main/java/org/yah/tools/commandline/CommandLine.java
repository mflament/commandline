package org.yah.tools.commandline;

import org.yah.tools.commandline.CommandLineDefinition.Option;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
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
public record CommandLine(CommandLineDefinition definition, List<String> parameters, Map<Option, String> options) {

    /**
     * Create a {@link CommandLineDefinition.Builder} to define the command line option and parameters.
     *
     * @param programName the name of the program to display in formatted help()
     */
    public static CommandLineDefinition.Builder define(String programName) {
        return CommandLineDefinition.builder(programName);
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
        return options.containsKey(getOption(name));
    }

    @Nullable
    public String option(String name) {
        Option option = getOption(name);
        return options.getOrDefault(option, option.defaultValue());
    }

    @Nullable
    public String option(String name, @Nullable String defaultValue) {
        return options.getOrDefault(getOption(name), defaultValue);
    }

    public <T> T parseOption(String name, Function<String, T> parser) {
        return parser.apply(option(name));
    }

    @Nonnull
    private Option getOption(String name) {
        Objects.requireNonNull(name, "name is null");
        Option option = definition.optionsMap().get(name);
        if (option == null)
            throw new NoSuchElementException("Unknown option " + name);
        return option;
    }

}
