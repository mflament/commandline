package org.yah.tools.commandline;

import org.yah.tools.commandline.CommandLineDefinition.Option;

import java.util.*;

public final class CommandLineDefinitionBuilder {
    private final String programName;
    private CommandLineDefinition.Parameter parameter;
    private final List<Option> options = new ArrayList<>();

    CommandLineDefinitionBuilder(String programName) {
        this.programName = Objects.requireNonNull(programName, "programName is null");
    }

    public CommandLineDefinitionBuilder withOption(String name, String description) {
        return withOption(name, description, null, null);
    }

    public CommandLineDefinitionBuilder withOption(Collection<String> names, String description) {
        return withOption(names, description, null, null);
    }

    public CommandLineDefinitionBuilder withOption(String name, String description, String argName) {
        return withOption(name, description, argName, null);
    }

    public CommandLineDefinitionBuilder withOption(Collection<String> names, String description, String argName) {
        return withOption(names, description, argName, null);
    }

    public CommandLineDefinitionBuilder withOption(String name, String description, String argName, String defaultValue) {
        return withOption(List.of(name), description, argName, defaultValue);
    }

    public CommandLineDefinitionBuilder withOption(Collection<String> names, String description, String argName, String defaultValue) {
        Option option = new Option(List.copyOf(names), description, argName, defaultValue);
        options.add(option);
        return this;
    }

    public CommandLineDefinitionBuilder withParameters(String name, String description, List<String> defaultValues) {
        parameter = new CommandLineDefinition.Parameter(name, description, false, true, List.copyOf(defaultValues));
        return this;
    }

    public CommandLineDefinitionBuilder withParameters(String name, String description) {
        parameter = new CommandLineDefinition.Parameter(name, description, true, true, List.of());
        return this;
    }

    public CommandLineDefinitionBuilder withParameter(String name, String description, String defaultValue) {
        parameter = new CommandLineDefinition.Parameter(name, description, false, false, List.of(defaultValue));
        return this;
    }

    public CommandLineDefinitionBuilder withParameter(String name, String description) {
        parameter = new CommandLineDefinition.Parameter(name, description, true, false, List.of());
        return this;
    }

    /**
     * Parse the given arguments using the current command line definition.
     *
     * @param args the program arguments
     * @return the parsed command line
     * @throws CommandLineParsingException if the arguments does not match the command line definition
     */
    public CommandLine parse(String... args) throws CommandLineParsingException {
        return build().parse(args);
    }

    /**
     * Build the {@link CommandLineDefinition} that can be reused for parsing arguments.
     *
     * @return A command line definition.
     */
    public CommandLineDefinition build() {
        Map<String, Option> optionsMap = new LinkedHashMap<>(options.size());
        options.forEach(option -> option.names().forEach(name -> {
            if (optionsMap.putIfAbsent(name, option) != null)
                System.err.println("Duplicate option name " + name);
        }));
        return new CommandLineDefinition(programName, parameter, options, optionsMap);
    }
}
