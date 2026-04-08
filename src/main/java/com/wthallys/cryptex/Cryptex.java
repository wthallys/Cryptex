package com.wthallys.cryptex;

import com.wthallys.cryptex.cli.CliParser;
import com.wthallys.cryptex.cli.Command;
import com.wthallys.cryptex.exception.CryptexException;
import com.wthallys.cryptex.io.FileProcessor;

public class Cryptex {

    public static void main(String[] args) {
        try {
            Command command = CliParser.parse(args);
            FileProcessor processor = new FileProcessor();
            processor.execute(command);
        } catch (CryptexException e) {
            System.err.println("Error: " + e.getMessage());
            System.exit(1);
        } catch (Exception e) {
            System.err.println("Unexpected error: " + e.getMessage());
            System.exit(2);
        }
    }
}
