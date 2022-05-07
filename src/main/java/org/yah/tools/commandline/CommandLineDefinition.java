package org.yah.tools.commandline;

import javax.annotation.Nullable;
import java.util.*;

/**
 * @param programName the name of the program. Used to create help.
 * @param parameter   the program parameter definitions. Parameters are command line argument not prefixed by an option.
 * @param options     the command line options per name. An option is an argument matching the name and an optional argument
 *                    ofr option's value.
 */
public record CommandLineDefinition(String programName, @Nullable Parameter parameter, List<Option> options, Map<String, Option> optionsMap) {

    public CommandLine parse(String... args) {
        List<String> parsedArguments = new ArrayList<>();
        Map<String, String> optionValues = new LinkedHashMap<>();
        ArgsQueue argsQueue = new ArgsQueue(args);
        while (argsQueue.hasNext()) {
            String arg = argsQueue.next();
            Option option = optionsMap.get(arg);
            if (option != null) {
                if (option.argName != null) {
                    String optionValue;
                    if (argsQueue.hasOptionValue()) {
                        optionValue =  argsQueue.next();
                    } else if (option.defaultValue != null) {
                        optionValue =  option.defaultValue;
                    } else {
                        String message = String.format("Option %s requires argument %s", arg, option.argName);
                        throw newCommandLineParsingException(message);
                    }
                    option.names().forEach(name -> optionValues.put(name, optionValue));
                } else {
                    optionValues.put(arg, null);
                }
            } else {
                parsedArguments.add(arg);
            }
        }

        if (parameter != null) {
            if (parsedArguments.isEmpty()) {
                if (parameter.required)
                    throw newCommandLineParsingException("Missing required parameter " + parameter.name);
                parsedArguments.addAll(parameter.defaultValue());
            }
        }

        return new CommandLine(this, List.copyOf(parsedArguments), Collections.unmodifiableMap(optionValues));
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

    private CommandLineParsingException newCommandLineParsingException(String message, String... args) {
        String fullMessage = String.format("Error parsing %s%n%s%n%s",
                String.join(" ", args), message, help());
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

    record Parameter(String name, String description, boolean required, boolean list, List<String> defaultValue) {
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

    record Option(List<String> names, @Nullable String description, @Nullable String argName, @Nullable String defaultValue) {
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

}
