package com.wthallys.cryptex.crypto;

import com.wthallys.cryptex.exception.CryptexException;

import javax.crypto.AEADBadTagException;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import java.security.GeneralSecurityException;

public class AesGcmCipher {

    private static final String ALGORITHM = "AES/GCM/NoPadding";
    public static final int TAG_LENGTH_BIT = 128;

    private AesGcmCipher() {}

    public static EncryptionResult encrypt(byte[] plaintext, SecretKey key, byte[] iv) throws CryptexException {
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(TAG_LENGTH_BIT, iv));

            byte[] ciphertextWithTag = cipher.doFinal(plaintext);

            int ciphertextLen = ciphertextWithTag.length - 16;
            byte[] ciphertext = new byte[ciphertextLen];
            byte[] authTag    = new byte[16];

            System.arraycopy(ciphertextWithTag, 0, ciphertext, 0, ciphertextLen);
            System.arraycopy(ciphertextWithTag, ciphertextLen, authTag, 0, 16);

            return new EncryptionResult(ciphertext, authTag);
        } catch (GeneralSecurityException e) {
            throw new CryptexException("Encrypt failed: " + e.getMessage(), e);
        }

    }

    public static byte[] decrypt(byte[] ciphertext, byte[] authTag, SecretKey key, byte[] iv) throws CryptexException {
        try {
            byte[] ciphertextWithTag = new byte[ciphertext.length + authTag.length];
            System.arraycopy(ciphertext, 0, ciphertextWithTag, 0, ciphertext.length);
            System.arraycopy(authTag, 0, ciphertextWithTag, ciphertext.length, authTag.length);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(TAG_LENGTH_BIT, iv));

            return cipher.doFinal(ciphertextWithTag);
        } catch (AEADBadTagException e) {
            throw new CryptexException(
                    "Authentication failed: wrong password/key, or the file was tampered with.", e);
        } catch (GeneralSecurityException e) {
            throw new CryptexException("Decryption failed: " + e.getMessage(), e);
        }
    }


    public record EncryptionResult(byte[] ciphertext, byte[] authTag) {}
}
