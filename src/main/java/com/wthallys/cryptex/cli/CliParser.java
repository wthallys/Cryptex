package com.wthallys.cryptex.cli;

import com.wthallys.cryptex.exception.CryptexException;

import java.nio.file.Files;
import java.nio.file.Path;

public class CliParser {

    private static final String FLAG_DECRYPT = "-d";
    private static final String FLAG_PASSWORD = "-p";
    private static final String USAGE =
            """
            Usage:
              cryptex <file>                           Encrypt (generates .key file)
              cryptex <file> -p <password>             Encrypt with password
              cryptex <file>.cptx -d                   Decrypt (reads .key automatically)
              cryptex <file>.cptx -d -p <password>     Decrypt with password
            """;

    private CliParser() {}

    public static Command parse(String[] args) throws CryptexException {
        if (args == null || args.length == 0) {
            throw new CryptexException("No arguments provided.\n" + USAGE);
        }

        Path inputFile = Path.of(args [0]);
        boolean decrypt = false;
        char[] password = null;

        for (int i = 1; i < args.length; i++) {
            switch (args[i]) {
                case FLAG_DECRYPT -> decrypt = true;
                case FLAG_PASSWORD -> {
                    if (i + 1 >= args.length) {
                        throw new CryptexException("Flag -p requires a password argument.");
                    }
                    password = args[++i].toCharArray();
                }
                default -> throw new CryptexException("Unknown flag: " + args[i] + "\n" + USAGE);
            }
        }

        validateInputFile(inputFile, decrypt);

        return new Command(inputFile, decrypt, password);

    }

    private static void validateInputFile(Path file, boolean decrypt) throws CryptexException {
        if (!Files.exists(file)) {
            throw new CryptexException("File not found: " + file);
        }

        if (!Files.isRegularFile(file)) {
            throw new CryptexException("Not a regular file: " + file);
        }

        if (decrypt && !file.toString().endsWith(".cptx")) {
            throw new CryptexException(
                    "Decryption (-d) requires a .cptx file, but got: " + file.getFileName()
            );
        }
    }
}
