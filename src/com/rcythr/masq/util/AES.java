package com.rcythr.masq.util;

import org.bouncycastle.crypto.BufferedBlockCipher;
import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.bouncycastle.crypto.engines.AESEngine;
import org.bouncycastle.crypto.paddings.PaddedBufferedBlockCipher;
import org.bouncycastle.crypto.paddings.ZeroBytePadding;
import org.bouncycastle.crypto.params.KeyParameter;

/**
 * Utility class used to encrypt/decrypt data
 * 
 * @author Richard Laughlin
 */
public class AES {

	/**
	 * Designates the key size to use in bytes.
	 * 
	 * 16 bytes = 128 bits
	 * 24 bytes = 192 bits
	 * 32 bytes = 256 bits
	 * 
	 */
	public static final int AES_KEY_SIZE = 32;
	
	/**
	 * Uses inputs to encrypt/decrypt based on the value of encrypt
	 * @param forEncryption if true encrypt, if false decrypt
	 * @param input the data to work with
	 * @param key the key to use
	 * @return the result
	 * 
	 * @throws InvalidCipherTextException if something goes wrong
	 */
	public static byte[] handle(boolean forEncryption, byte[] input, byte[] key) throws InvalidCipherTextException {
	    CipherParameters cipherParameters = new KeyParameter(key);
	    BufferedBlockCipher bufferedBlockCipher = new PaddedBufferedBlockCipher(new AESEngine(), new ZeroBytePadding());

	    bufferedBlockCipher.init(forEncryption, cipherParameters);

	    int inputOffset = 0;
	    int inputLength = input.length;

	    int maximumOutputLength = bufferedBlockCipher.getOutputSize(inputLength);
	    byte[] output = new byte[maximumOutputLength];
	    int outputOffset = 0;
	    int outputLength = 0;

	    int bytesProcessed;

	    bytesProcessed = bufferedBlockCipher.processBytes(
	            input, inputOffset, inputLength,
	            output, outputOffset
	        );
	    outputOffset += bytesProcessed;
	    outputLength += bytesProcessed;

	    bytesProcessed = bufferedBlockCipher.doFinal(output, outputOffset);
	    outputOffset += bytesProcessed;
	    outputLength += bytesProcessed;

	    if (outputLength == output.length) {
	        return output;
	    } else {
	        byte[] truncatedOutput = new byte[outputLength];
	        System.arraycopy(
	                output, 0,
	                truncatedOutput, 0,
	                outputLength
	            );
	        return truncatedOutput;
	    }
	}
}
