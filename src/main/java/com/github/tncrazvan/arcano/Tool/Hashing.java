/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.tncrazvan.arcano.Tool;

import static com.github.tncrazvan.arcano.SharedObject.LOGGER;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.logging.Level;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 *
 * @author Administrator
 */
public interface Hashing {
    
    /**
     * Encodes String to sha1.
     * @param str input String.
     * @param charset character set to use
     * @return encoded String.
     */
    public static String getSha1String(String str,String charset){
        try {
            MessageDigest crypt = MessageDigest.getInstance("SHA-1");
            crypt.reset();
            crypt.update(str.getBytes(charset));
            
            return new BigInteger(1, crypt.digest()).toString(16);
        } catch (UnsupportedEncodingException | NoSuchAlgorithmException ex) {
            LOGGER.log(Level.WARNING,null,ex);
            return null;
        }
    }
    
    /**
     * Encodes String to sha1 byte array.
     * @param input input String.
     * @param charset character set to use
     * @return encoded byte array.
     * @throws NoSuchAlgorithmException
     * @throws UnsupportedEncodingException 
     */
    public static byte[] getSha1Bytes(String input,String charset) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        
        return MessageDigest.getInstance("SHA-1").digest(input.getBytes(charset));
    }
    
    
    
    /**
     * Encodes the given String to sha512.
     * @param value input String.
     * @param salt salt String. Can be empty.
     * @param charset character set to use
     * @return encoded String.
     */
    public static String getSha512String(String value, String salt, String charset){
        String result = null;
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-512");
            md.update(salt.getBytes(charset));
            byte[] bytes = md.digest(value.getBytes(charset));
            StringBuilder sb = new StringBuilder();
            for(int i=0; i< bytes.length ;i++){
               sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
            }
            result = sb.toString();
            
        } 
            catch (NoSuchAlgorithmException e){
        } catch (UnsupportedEncodingException ex) {
            LOGGER.log(Level.WARNING,null,ex);
        }
        return result;
    }
    
    /**
     * Encodes the given String to a sha512 byte array.
     * @param value input String.
     * @param salt salt String. Can be empty.
     * @param charset character set to use
     * @return encoded byte array.
     */
    public static byte[] getSha512Bytes(String value, String salt, String charset){
        try {
            return getSha512String(value, salt, charset).getBytes(charset);
        } catch (UnsupportedEncodingException ex) {
            LOGGER.log(Level.WARNING,null,ex);
            return null;
        }
    }
    
    
    
    static class BCrypt{
        private static String generateStorngPasswordHash(String password) throws NoSuchAlgorithmException, InvalidKeySpecException{
            int iterations = 1000;
            char[] chars = password.toCharArray();
            byte[] salt = getSalt();

            PBEKeySpec spec = new PBEKeySpec(chars, salt, iterations, 64 * 8);
            SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            byte[] hash = skf.generateSecret(spec).getEncoded();
            return iterations + ":" + toHex(salt) + ":" + toHex(hash);
        }

        private static byte[] getSalt() throws NoSuchAlgorithmException{
            SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
            byte[] salt = new byte[16];
            sr.nextBytes(salt);
            return salt;
        }

        private static String toHex(byte[] array) throws NoSuchAlgorithmException{
            BigInteger bi = new BigInteger(1, array);
            String hex = bi.toString(16);
            int paddingLength = (array.length * 2) - hex.length();
            if(paddingLength > 0)
            {
                return String.format("%0"  +paddingLength + "d", 0) + hex;
            }else{
                return hex;
            }
        }
        
        private static boolean validatePassword(String originalPassword, String storedPassword) throws NoSuchAlgorithmException, InvalidKeySpecException{
            String[] parts = storedPassword.split(":");
            int iterations = Integer.parseInt(parts[0]);
            byte[] salt = fromHex(parts[1]);
            byte[] hash = fromHex(parts[2]);

            PBEKeySpec spec = new PBEKeySpec(originalPassword.toCharArray(), salt, iterations, hash.length * 8);
            SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            byte[] testHash = skf.generateSecret(spec).getEncoded();

            int diff = hash.length ^ testHash.length;
            for(int i = 0; i < hash.length && i < testHash.length; i++)
            {
                diff |= hash[i] ^ testHash[i];
            }
            return diff == 0;
        }
        private static byte[] fromHex(String hex) throws NoSuchAlgorithmException{
            byte[] bytes = new byte[hex.length() / 2];
            for(int i = 0; i<bytes.length ;i++)
            {
                bytes[i] = (byte)Integer.parseInt(hex.substring(2 * i, 2 * i + 2), 16);
            }
            return bytes;
        }
    }
    
    /**
     * Encodes the value to BCrypt. Note that encoding the same value twice will generate a different BCrypt string.
     * This means you cannot simply check two encoded string to find out if they were generated from the same value like Sha1.
     * Use Server#validateBCryptString to validate an encoded strings.
     * @param value input String.
     * @return encoded String.
     */
    public static String getBCryptString(String value){
        try {
            return BCrypt.generateStorngPasswordHash(value);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException ex) {
            LOGGER.log(Level.WARNING,null,ex);
            return null;
        }
    }
    
    /**
     * Checks if the given cryptoStrong has been created from the given originalString.
     * @param originalString this is the validation string. The encrypted string will be validated using this value.
     * @param cryptoString this is the encoded string.
     * @return true if the cryptoString was created from the originalString, false otherwise.
     */
    public static boolean validateBCryptString(String originalString, String cryptoString){
        try {
            return BCrypt.validatePassword(originalString, cryptoString);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException ex) {
            LOGGER.log(Level.WARNING,null,ex);
            return false;
        }
    }
}
