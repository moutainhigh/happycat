package com.woniu.sncp.pay.common.utils.encrypt;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.PublicKey;
import java.security.Security;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.woniu.sncp.security.signature.DSASignature;
import com.woniu.sncp.security.signature.SignatureFailureException;

/**
 * dsa 签名，验证 比rsa快
 * @author luzz
 *
 */
public class Dsa extends Signature{
	
	final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	public static final String NAME = "DSA";
	static final int START_TAG = 0x30;
	static final int SEP_TAG = 0x02;
	/** 将base64中的大写字母escape的char。 */
	char BASE64_ESCAPE_CHAR = '_';

	@Override
	public String sign(String data, String key, String encode) throws Exception {
		if(StringUtils.isBlank(encode)){
			encode = Encryption.ENCODE_UTF8;
		}
		
		return new DSASignature().sign(data, key,encode);
		
//		PrivateKey priKey = KeyReader.getPrivateKeyFromPKCS8(NAME, new ByteArrayInputStream(key.getBytes()));
//		java.security.Signature signature = java.security.Signature.getInstance(NAME);
//		signature.initSign(priKey);
//		signature.update(data.getBytes(encode));
//		byte[] signed = signature.sign();
//		byte[] signedRS = signatureToRS(signed);
//		String sign = new String(Base64.encodeBase64(signedRS));
//		return sign;
	}

	@Override
	public Boolean verify(String sign, String data, String key, String encode){
		if(StringUtils.isBlank(encode)){
			encode = Encryption.ENCODE_UTF8;
		}
		try {
			return new DSASignature().verify(data, sign, key,encode);
		} catch (SignatureFailureException e) {
			logger.error(e.getMessage(),e);
		}
		return false;
	}
	
	/**
	 * 
	 * 将Sun provider提供的signature的格式转换为固定的40字节长的格式。
	 * @param signature Sun provider提供的signature
	 * @return
	 * @throws Exception
	 */
	private byte[] signatureToRS(byte[] signature) throws Exception {
		if ((signature == null) || (signature[0] != START_TAG)) {
			throw new Exception("签名格式错误");
		}

		byte[] rs = new byte[40];

		// offsetR - 原始signature中R值的起始位移
		// lengthR - 原始signature中R值的长度
		// startR  - 目标signature中R值的起始位移
		int offsetR = 4;
		int lengthR = signature[offsetR - 1];
		int startR = 0;

		if (signature[offsetR - 2] != SEP_TAG) {
			throw new Exception("签名格式错误");
		}

		if (lengthR > 20) {
			offsetR += (lengthR - 20);
			lengthR = 20;
		} else if (lengthR < 20) {
			startR += (20 - lengthR);
		}

		// offsetS - 原始signature中S值的起始位移
		// lengthS - 原始signature中S值的长度
		// startS  - 目标signature中S值的起始位移
		int offsetS = signature[3] + 6;
		int lengthS = signature[offsetS - 1];
		int startS = 20;

		if (signature[offsetS - 2] != SEP_TAG) {
			throw new Exception("签名格式错误");
		}

		if (lengthS > 20) {
			offsetS += (lengthS - 20);
			lengthS = 20;
		} else if (lengthS < 20) {
			startS += (20 - lengthS);
		}

		System.arraycopy(signature, offsetR, rs, startR, lengthR);
		System.arraycopy(signature, offsetS, rs, startS, lengthS);

		return rs;
	}
	
	/**
	 * 将"_"+小写字母转换成大写字母。
	 * @param str
	 * @return
	 */
	private byte[] decodeUpperCase(String str) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream(str.length());

		for (int i = 0; i < str.length(); i++) {
			char ch = str.charAt(i);

			if ((ch == BASE64_ESCAPE_CHAR) && (i < (str.length() - 1))) {
				char nextChar = Character.toUpperCase(str.charAt(++i));

				baos.write((int) nextChar);
			} else {
				baos.write((int) ch);
			}
		}

		try {
			baos.close();
		} catch (IOException e) {
			logger.error(e.getMessage(),e);
		}

		return baos.toByteArray();
	}
	
	/**
	 * 
	 * 将固定的40字节长的格式转换为Sun provider提供的signature的格式。
	 * @param rs 固定的40字节长的格式signature
	 * @return
	 * @throws Exception
	 */
	private byte[] rsToSignature(byte[] rs) throws Exception {
		if ((rs == null) || (rs.length != 40)) {
			throw new Exception("签名格式错误");
		}

		int length = 46;
		int offsetR = 4;
		int lengthR = 20;

		if ((rs[0] & 0x80) != 0) {
			length++;
			offsetR++;
			lengthR++;
		}

		int offsetS = offsetR + 22;
		int lengthS = 20;

		if ((rs[20] & 0x80) != 0) {
			length++;
			offsetS++;
			lengthS++;
		}

		byte[] signature = new byte[length];

		signature[0] = START_TAG;
		signature[1] = (byte) (length - 2);
		signature[2] = SEP_TAG;
		signature[3] = (byte) lengthR;
		System.arraycopy(rs, 0, signature, offsetR, 20);
		signature[offsetR + 20] = SEP_TAG;
		signature[offsetR + 21] = (byte) lengthS;
		System.arraycopy(rs, 20, signature, offsetS, 20);

		return signature;
	}
	
	public static void main(String[] args) throws Exception {
		String key = "MIIBSwIBADCCASwGByqGSM44BAEwggEfAoGBAP1/U4EddRIpUt9KnC7s5Of2EbdSPO9EAMMeP4C2"+
				"USZpRV1AIlH7WT2NWPq/xfW6MPbLm1Vs14E7gB00b/JmYLdrmVClpJ+f6AR7ECLCT7up1/63xhv4"+
				"O1fnxqimFQ8E+4P208UewwI1VBNaFpEy9nXzrith1yrv8iIDGZ3RSAHHAhUAl2BQjxUjC8yykrmC"+
				"ouuEC/BYHPUCgYEA9+GghdabPd7LvKtcNrhXuXmUr7v6OuqC+VdMCz0HgmdRWVeOutRZT+ZxBxCB"+
				"gLRJFnEj6EwoFhO3zwkyjMim4TwWeotUfI0o4KOuHiuzpnWRbqN/C/ohNWLx+2J6ASQ7zKTxvqhR"+
				"kImog9/hWuWfBpKLZl6Ae1UlZAFMO/7PSSoEFgIUYf4Wb4sF4fnGep19ekS/Rg7DeNU=";
		
		String pubKey="MIIBuDCCASwGByqGSM44BAEwggEfAoGBAP1/U4EddRIpUt9KnC7s5Of2EbdSPO9EAMMeP4C2USZp"+
				"RV1AIlH7WT2NWPq/xfW6MPbLm1Vs14E7gB00b/JmYLdrmVClpJ+f6AR7ECLCT7up1/63xhv4O1fn"+
				"xqimFQ8E+4P208UewwI1VBNaFpEy9nXzrith1yrv8iIDGZ3RSAHHAhUAl2BQjxUjC8yykrmCouuE"+
				"C/BYHPUCgYEA9+GghdabPd7LvKtcNrhXuXmUr7v6OuqC+VdMCz0HgmdRWVeOutRZT+ZxBxCBgLRJ"+
				"FnEj6EwoFhO3zwkyjMim4TwWeotUfI0o4KOuHiuzpnWRbqN/C/ohNWLx+2J6ASQ7zKTxvqhRkImo"+
				"g9/hWuWfBpKLZl6Ae1UlZAFMO/7PSSoDgYUAAoGBAM5LG64TJ9FlaQQuAOKnhglsvOymFP1kL/43"+
				"OaGo0xuOy9rfeK+RXRQ+n8vNkE3cyPfHIqG2MX7Jjw/k1E0AyHGmFduu0A6MbLv4CPjk1R6LyJqX"+
				"3uHyOJ4EJfhUcd9kgddv9rMXHoZUDOQJxd/fTeb9jJGpzeFaKn9Yc/u/x2l8";
		Dsa dsa = new Dsa();
		System.out.println(dsa.sign("123123123", key, ""));
		
		String sign = "FO+RuAneDzL0ClrYNz2Xw3rdj9B5AdSiLDxNCyhk8BOP8iV9kkdnrQ==";
		System.out.println(dsa.verify(sign, "123123123", pubKey, ""));
		
		System.out.println(System.getProperty("java.version"));
//		for (Provider provider : Security.getProviders())
//		    System.out.println(provider);
	}

}
