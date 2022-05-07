package org.yah.tools.commandline;

import org.junit.jupiter.api.Test;
import org.yah.tools.commandline.CommandLineDefinition.Option;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CommandLineTest {

    @Test
    void define() {
        CommandLineDefinition definition = createDefinition();

        assertNotNull(definition.parameter());
        assertEquals("file", definition.parameter().name());
        assertEquals("The output file", definition.parameter().description());
        assertTrue(definition.parameter().required());

        assertEquals(2, definition.options().size());
        assertEquals(3, definition.optionsMap().size());

        Option option = definition.optionsMap().get("-m");
        assertNotNull(option);
        assertEquals(List.of("-m", "--maxSize"), option.names());
        assertEquals("max file size in KB", option.description());
        assertEquals("size", option.argName());
        assertEquals("1024", option.defaultValue());

        assertSame(option, definition.optionsMap().get("--maxSize"));

        option = definition.optionsMap().get("-v");
        assertNotNull(option);
        assertEquals(List.of("-v"), option.names());
        assertEquals("verbose", option.description());
        assertNull(option.argName());
        assertNull(option.defaultValue());
    }

    @Test
    void parse() {
        CommandLineDefinition definition = createDefinition();
        CommandLine parsed = definition.parse("-m", "512", "out.txt");

        assertFalse(parsed.hasOption("-v"));
        assertNull(parsed.option("-v"));

        assertTrue(parsed.hasOption("-m"));
        int ms = parsed.parseOption("-m", Integer::parseInt);
        assertEquals(512, ms);

        assertTrue(parsed.hasOption("--maxSize"));
        ms = parsed.parseOption("--maxSize", Integer::parseInt);
        assertEquals(512, ms);

        assertEquals("out.txt", parsed.parameter());
    }

    @Test
    void parsingError() {
        CommandLineDefinition definition = createDefinition();
        // missing arg value
        assertThrows(CommandLineParsingException.class, () -> definition.parse("-m"));

        // missing parameter
        assertThrows(CommandLineParsingException.class, () -> definition.parse("-m", "512"));
    }

    private CommandLineDefinition createDefinition() {
        return CommandLine.define("test")
                .withOption(List.of("-m", "--maxSize"), "max file size in KB", "size", "1024")
                .withOption("-v", "verbose")
                .withParameter("file", "The output file")
                .build();
    }


}