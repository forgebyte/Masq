package com.rcythr.secretsms;

import java.math.BigInteger;

import org.bouncycastle.crypto.BufferedBlockCipher;
import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.bouncycastle.crypto.engines.AESEngine;
import org.bouncycastle.crypto.paddings.PaddedBufferedBlockCipher;
import org.bouncycastle.crypto.paddings.ZeroBytePadding;
import org.bouncycastle.crypto.params.KeyParameter;

public class AES {

	public static String toHex(byte[] bytes) {
	    BigInteger bi = new BigInteger(1, bytes);
	    return String.format("%0" + (bytes.length << 1) + "X", bi);
	}
	
	public static byte[] fromHex(String s) {
	    int len = s.length();
	    
	    byte[] data = new byte[len / 2];
	    for (int i = 0; i < len; i += 2) {
	        data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
	                             + Character.digit(s.charAt(i+1), 16));
	    }
	    return data;
	}
	
	public static byte[] handle(boolean encrypt, byte[] input, byte[] key) throws InvalidCipherTextException {
		
		//16 bytes = 128 bits
		//24 bytes = 192 bits
		//32 bytes = 256 bits
		
	    CipherParameters cipherParameters = new KeyParameter(key);

	    BufferedBlockCipher bufferedBlockCipher = new PaddedBufferedBlockCipher(new AESEngine(), new ZeroBytePadding());

	    return process(input, bufferedBlockCipher, cipherParameters, encrypt);
	}

	private static byte[] process(byte[] input, BufferedBlockCipher bufferedBlockCipher, CipherParameters cipherParameters, boolean forEncryption) throws InvalidCipherTextException {
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
