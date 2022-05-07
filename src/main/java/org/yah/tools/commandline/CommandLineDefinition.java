package org.yah.tools.commandline;

import javax.annotation.Nullable;
import java.util.*;

/**
 * @param programName the name of the program. Used to create help.
 * @param parameter   the program parameter definitions. Parameters are command line argument not prefixed by an option.
 * @param options     the command line options per name. An option is an argument matching the name and an optional argument
 *                    ofr option's value.
 */
public record CommandLineDefinition(String programName, @Nullable Parameter parameter, List<Option> options,
                                    Map<String, Option> optionsMap) {

    public CommandLine parse(String... args) {
        List<String> parsedParameters = null;
        Map<Option, String> optionValues = new LinkedHashMap<>();
        ArgsQueue argsQueue = new ArgsQueue(args);
        while (argsQueue.hasNext()) {
            String arg = argsQueue.next();
            Option option = optionsMap.get(arg);
            if (option != null) {
                parseOption(arg, option, argsQueue, optionValues);
            } else {
                if (parsedParameters == null)
                    parsedParameters = new ArrayList<>();
                parsedParameters.add(arg);
            }
        }

        if (parameter != null) {
            if (parsedParameters == null) {
                if (parameter.required)
                    throw newCommandLineParsingException("Missing required parameter '" + parameter.name + "'");
                parsedParameters = parameter.defaultValue();
            }
        } else {
            parsedParameters = List.of();
        }

        return new CommandLine(this, List.copyOf(parsedParameters), Collections.unmodifiableMap(optionValues));
    }

    private void parseOption(String optionName, Option option, ArgsQueue argsQueue, Map<Option, String> optionValues) {
        String optionValue = option.defaultValue();

        if (option.argName() != null) {
            if (!argsQueue.hasOptionValue()) {
                String message = String.format("Option '%s' requires argument '%s'", optionName, option.argName);
                throw newCommandLineParsingException(message);
            }
            optionValue = argsQueue.next();
        }

        optionValues.put(option, optionValue);
    }

    public String help() {
        StringBuilder sb = new StringBuilder();
        sb.append(programName);
        if (!options.isEmpty())
            sb.append(" [options]");
        if (parameter != null)
            sb.append(" ").append(parameter.help());

        String lineSeparator = System.lineSeparator();
        if (!options.isEmpty()) {
            sb.append(lineSeparator).append("  Options:");
            for (Option value : options) {
                sb.append(lineSeparator).append("  ").append(value.help());
            }
        }

        return sb.toString();
    }

    private CommandLineParsingException newCommandLineParsingException(String message) {
        String fullMessage = String.format("%s%nUsage: %s", message, help());
        return new CommandLineParsingException(this, fullMessage);
    }

    private final class ArgsQueue {
        private final String[] args;
        private int index;

        public ArgsQueue(String[] args) {
            this.args = args;
        }

        String next() {
            if (hasNext())
                return args[index++];
            throw new NoSuchElementException();
        }

        boolean hasNext() {
            return index < args.length;
        }

        boolean hasOptionValue() {
            return hasNext() && !optionsMap.containsKey(args[index]);
        }

    }

    record Option(List<String> names,
                  String description,
                  @Nullable String argName,
                  @Nullable String defaultValue) {
        String help() {
            String h = String.join(", ", names);
            if (argName != null) {
                h += " ";
                if (defaultValue != null) h += "[";
                h += "<" + argName + ">";
                if (defaultValue != null) h += "]";
            }
            if (description != null)
                h += " : " + description;
            return h;
        }
    }

    record Parameter(String name,
                     String description,
                     boolean required,
                     boolean list,
                     List<String> defaultValue) {
        String help() {
            String h;
            if (required) {
                h = name;
                if (list) h += String.format(" [%1$s2 %1$s3 ...]", name);
            } else {
                h = "[" + name;
                if (list) h += String.format(" [%1$s2 [%1$s3] ...]", name);
                h += "]";
            }
            return h;
        }
    }

    static Builder builder(String programName) {
        return new Builder(programName);
    }

    public static final class Builder {
        private final String programName;
        private CommandLineDefinition.Parameter parameter;
        private final List<Option> options = new ArrayList<>();

        private Builder(String programName) {
            this.programName = Objects.requireNonNull(programName, "programName is null");
        }

        public Builder withOption(String name, String description) {
            return withOption(name, description, null, null);
        }

        public Builder withOption(String name, String description, String argName) {
            return withOption(name, description, argName, null);
        }

        public Builder withOption(Collection<String> names, String description) {
            return withOption(names, description, null, null);
        }

        public Builder withOption(Collection<String> names, String description, String argName) {
            return withOption(names, description, argName, null);
        }

        public Builder withOption(String name, String description, String argName, String defaultValue) {
            return withOption(List.of(name), description, argName, defaultValue);
        }

        public Builder withOption(Collection<String> names, String description, String argName, String defaultValue) {
            Objects.requireNonNull(names, "names is null");
            if (names.isEmpty())
                throw new IllegalArgumentException("names is empty");
            if (description == null)
                description = "";
            options.add(new Option(List.copyOf(names), description, argName, defaultValue));
            return this;
        }

        public Builder withParameter(String name, String description, String defaultValue) {
            parameter = new CommandLineDefinition.Parameter(name, description, false, false, List.of(defaultValue));
            return this;
        }

        public Builder withParameter(String name, String description) {
            parameter = new CommandLineDefinition.Parameter(name, description, true, false, List.of());
            return this;
        }

        public Builder withParameters(String name, String description, List<String> defaultValues) {
            parameter = new CommandLineDefinition.Parameter(name, description, false, true, List.copyOf(defaultValues));
            return this;
        }

        public Builder withParameters(String name, String description) {
            parameter = new CommandLineDefinition.Parameter(name, description, true, true, List.of());
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

}
