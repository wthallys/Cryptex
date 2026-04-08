package com.wthallys.cryptex.io;

import com.wthallys.cryptex.cli.Command;
import com.wthallys.cryptex.crypto.CryptoEngine;
import com.wthallys.cryptex.exception.CryptexException;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

public class FileProcessor {

    public static final String CPTX_EXTENSION = ".cptx";
    public static final String KEY_EXTENSION = ".key";

    public void execute(Command command) throws CryptexException {
        if (command.decrypt()) {
            decrypt(command);
        } else {
            encrypt(command);
        }
    }

    private void encrypt(Command command) throws CryptexException {
        Path inputPath  = command.inputFile();
        Path outputPath = Path.of(inputPath + CPTX_EXTENSION);
        Path keyPath    = resolveKeyPath(inputPath);

        byte[] plaintext = readFile(inputPath);

        CryptoEngine.EncryptResult result = command.hasPassword()
                ? CryptoEngine.encryptWithPassword(plaintext, command.password())
                : CryptoEngine.encryptWithRandomKey(plaintext);

        writeCptxFile(outputPath, result);

        if (result.requiresKeyFile()) {
            writeKeyFile(keyPath, result.rawKey());
            Arrays.fill(result.rawKey(), (byte) 0);
            System.out.println("Encrypted -> " + outputPath.getFileName());
            System.out.println("Key file -> " + keyPath + " (keep this to decrypt later");
        } else {
            System.out.println("Encrypted -> " + outputPath.getFileName());
        }
    }

    private void decrypt(Command command) throws CryptexException {
        Path cptxPath = command.inputFile();
        Path outputPath = resolveDecryptedPath(cptxPath);

        CptxHeader header = readHeader(cptxPath);
        byte[] ciphertext = readCiphertext(cptxPath);

        byte[] plaintext;

        if (header.isPasswordBased()) {
            if (!command.hasPassword()) {
                throw new CryptexException(
                        "This file was encrypted with a password. Please provide it with -p."
                );
            }
            plaintext = CryptoEngine.decryptWithPassword(ciphertext, header, command.password());
        } else {
            if (command.hasPassword()) {
                System.out.println("Warning: -p was provided but this file uses a key file. The password will be ignored.");
            }
            byte[] rawKey = readKeyFile(resolveKeyPath(cptxPath));
            plaintext = CryptoEngine.decryptWithRawKey(ciphertext, header, rawKey);
            Arrays.fill(rawKey, (byte) 0);
        }

        writeFile(outputPath, plaintext);
        System.out.println("Decrypted -> " + outputPath.getFileName());
    }

    private byte[] readFile(Path path) throws CryptexException {
        try {
            return Files.readAllBytes(path);
        } catch (IOException e) {
            throw new CryptexException("Could not read file: " + path, e);
        }
    }

    private void writeFile(Path path, byte[] data) throws CryptexException {
        try {
            Files.write(path, data);
        } catch (IOException e) {
            throw new CryptexException("Could not write file: " + path, e);
        }
    }

    private void writeCptxFile(Path path, CryptoEngine.EncryptResult result) throws CryptexException {
        try (OutputStream out = new BufferedOutputStream(new FileOutputStream(path.toFile()))) {
            result.header().writeTo(out);
            out.write(result.ciphertext());
        } catch (IOException e) {
            throw new CryptexException("Could not write encrypted file: " + path, e);
        }
    }

    private CptxHeader readHeader(Path path) throws CryptexException {
        try (InputStream in = new BufferedInputStream(new FileInputStream(path.toFile()))) {
            return CptxHeader.readFrom(in);
        } catch (IOException e) {
            throw new CryptexException("Could not read .cptx file: " + path, e);
        }
    }

    private byte[] readCiphertext(Path path) throws CryptexException {
        try (InputStream in = new BufferedInputStream(new FileInputStream(path.toFile()))) {
            long skipped = in.skip(CptxHeader.TOTAL_SIZE);
            if (skipped != CptxHeader.TOTAL_SIZE) {
                throw new CryptexException("File is too short to contain a valid header: " + path);
            }
            return in.readAllBytes();
        } catch (IOException e) {
            throw new CryptexException("Could not read ciphertext from: " + path, e);
        }
    }

    private void writeKeyFile(Path path, byte[] key) throws CryptexException {
        try {
            Files.createDirectories(path.getParent());
            Files.write(path, key);
        } catch (IOException e) {
            throw new CryptexException("Could not write key file: " + path, e);
        }
    }

    private byte[] readKeyFile(Path path) throws CryptexException {
        if (!Files.exists(path)) {
            throw new CryptexException(
                    "Key file not found: " + path +
                            "\nThis file was encrypted without a password. The .key file must exist to decrypt."
            );
        }
        try {
            return Files.readAllBytes(path);
        } catch (IOException e) {
            throw new CryptexException("Could not read key file: " + path, e);
        }
    }

    private Path resolveDecryptedPath(Path cptxPath) throws CryptexException {
        String name = cptxPath.toString();
        if (!name.endsWith(CPTX_EXTENSION)) {
            throw new CryptexException("Expected a .cptx file, got: " + cptxPath.getFileName());
        }
        return Path.of(name.substring(0, name.length() - CPTX_EXTENSION.length()));
    }

    private Path resolveKeyPath(Path inputPath) {
        String home = System.getProperty("user.home");
        String keyName = inputPath.toAbsolutePath().toString()
                .replaceAll("[^a-zA-Z0-9]", "_") + KEY_EXTENSION;
        return Path.of(home, ".cryptex", "keys", keyName);
    }
}
