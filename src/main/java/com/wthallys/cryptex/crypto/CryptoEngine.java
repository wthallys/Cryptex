package com.wthallys.cryptex.crypto;

import com.wthallys.cryptex.exception.CryptexException;
import com.wthallys.cryptex.io.CptxHeader;

import javax.crypto.SecretKey;
import java.util.Arrays;

public class CryptoEngine {

    private CryptoEngine() {}

    public static EncryptResult encryptWithPassword(byte[] plaintext, char[] password) throws CryptexException {
        byte[] salt = KeyDerivation.generateSalt(CptxHeader.SALT_LENGTH);
        byte[] iv = KeyDerivation.generateIv(CptxHeader.IV_LENGTH);

        SecretKey key = KeyDerivation.deriveKeyFromPassword(password, salt);

        try {
            AesGcmCipher.EncryptionResult result = AesGcmCipher.encrypt(plaintext, key, iv);
            CptxHeader header = new CptxHeader(CptxHeader.FLAG_PASSWORD_BASED, salt, iv, result.authTag());
            return new EncryptResult(header, result.ciphertext(), null);
        } finally {
            wipeKey(key);
        }
    }

    public static EncryptResult encryptWithRandomKey(byte[] plaintext) throws CryptexException {
        byte[] salt = KeyDerivation.generateSalt(CptxHeader.SALT_LENGTH);
        byte[] iv = KeyDerivation.generateIv(CptxHeader.IV_LENGTH);
        byte[] rawKey = KeyDerivation.generateRandomKey();
        SecretKey key = KeyDerivation.fromRawBytes(rawKey);

        try {
            AesGcmCipher.EncryptionResult result = AesGcmCipher.encrypt(plaintext, key, iv);
            CptxHeader header = new CptxHeader((byte) 0x00, salt, iv, result.authTag());
            return new EncryptResult(header, result.ciphertext(), rawKey);
        } finally {
            wipeKey(key);
        }
    }

    public static byte[] decryptWithPassword(byte[] ciphertext, CptxHeader header, char[] password) throws CryptexException {
        SecretKey key = KeyDerivation.deriveKeyFromPassword(password, header.salt());
        try {
            return AesGcmCipher.decrypt(ciphertext, header.authTag(), key, header.iv());
        } finally {
            wipeKey(key);
        }
    }

    public static byte[] decryptWithRawKey(byte[] ciphertext, CptxHeader header, byte[] rawKey) throws CryptexException {
        SecretKey key = KeyDerivation.fromRawBytes(rawKey);
        try {
            return AesGcmCipher.decrypt(ciphertext, header.authTag(), key, header.iv());
        } finally {
            wipeKey(key);
        }
    }

    private static void wipeKey(SecretKey key) {
        byte[] encoded = key.getEncoded();
        if (encoded != null) {
            Arrays.fill(encoded, (byte) 0);
        }
    }

    public record EncryptResult(
            CptxHeader header,
            byte[] ciphertext,
            byte[] rawKey
    ) {
        public boolean requiresKeyFile() {
            return rawKey != null;
        }
    }
}
