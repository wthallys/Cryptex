package com.wthallys.cryptex.io;

import com.wthallys.cryptex.exception.CryptexException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

public record CptxHeader(
    byte flags,
    byte[] salt,
    byte[] iv,
    byte[] authTag
) {
    public static final byte[] MAGIC = {0x43, 0x50, 0x54, 0x58, 0x00};
    public static final byte VERSION = 0x01;

    private static final int VERSION_LENGTH = 1;
    private static final int FLAGS_LENGTH   = 1;

    public static final int SALT_LENGTH     = 16;
    public static final int IV_LENGTH       = 12;
    public static final int AUTH_TAG_LENGTH = 16;
    public static final int TOTAL_SIZE      = MAGIC.length + VERSION_LENGTH + FLAGS_LENGTH + SALT_LENGTH + IV_LENGTH + AUTH_TAG_LENGTH;

    public static final byte FLAG_PASSWORD_BASED = 0x01;

    public boolean isPasswordBased() {
        return (flags & FLAG_PASSWORD_BASED) != 0;
    }

    public void writeTo(OutputStream out) throws IOException {
        out.write(MAGIC);
        out.write(VERSION);
        out.write(flags);
        out.write(salt);
        out.write(iv);
        out.write(authTag);
    }

    public static CptxHeader readFrom(InputStream in) throws IOException, CryptexException {
        byte[] magic = in.readNBytes(MAGIC.length);
        if (!Arrays.equals(magic, MAGIC)) {
            throw new CryptexException("Not a valid .cptx file (invalid magic bytes).");
        }

        int version = in.read();
        if (version != VERSION) {
            throw new CryptexException("Unsupported .cptx version: " + version);
        }

        byte   flags   = (byte) in.read();
        byte[] salt    = in.readNBytes(SALT_LENGTH);
        byte[] iv      = in.readNBytes(IV_LENGTH);
        byte[] authTag = in.readNBytes(AUTH_TAG_LENGTH);

        return new CptxHeader(flags, salt, iv, authTag);
    }

}
