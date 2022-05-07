# Simple command line parser

Minimalist java command line parse.

Usage:

```java
class MyProgram {
    public static void main(String[] args) {
        CommandLine commandLine = CommandLine.define("myProgram")
                .withOption(List.of("-m", "--maxSize"), "max file size in KB", "size", "1024")
                .withOption("-v", "verbose")
                .withParameter("file", "The output file", "out.txt")
                .parse(args);

        boolean verbose = commandLine.hasOption("-v");
        int maxSize = commandLine.parseOption("--maxSize", Integer::parseInt);
        String parameter = commandLine.parameter();
        System.out.printf("verbose: %s maxSize: %d parameter: %s%n", verbose, maxSize, parameter);
    }
}
```

Run with `java -jar MyProgram.jar` will give   
`verbose: false maxSize: 1024 parameter: out.txt`

Run with `java -jar MyProgram.jar -v` will give
`verbose: true maxSize: 1024 parameter: out.txt`

Run with `java -jar MyProgram.jar myfile.txt` will give
`verbose: false maxSize: 1024 parameter: myfile.txt`

Run with   
`java -jar MyProgram.jar -m 512 myfile.txt` or   
`java -jar MyProgram.jar --maxSize 512 myfile.txt` will give   
`verbose: false maxSize: 512 parameter: myfile.txt`
