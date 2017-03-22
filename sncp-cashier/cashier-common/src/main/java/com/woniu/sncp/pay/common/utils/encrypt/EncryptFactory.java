package com.woniu.sncp.pay.common.utils.encrypt;


public class EncryptFactory {
	private static Encryption encryption = null;
	
	public static Encryption getInstance(String type){
		if(Rsa.NAME.equalsIgnoreCase(type)){
			encryption = new Rsa();
		} else if(Md5.NAME.equalsIgnoreCase(type)){
			encryption = new Md5();
		} else if(Dsa.NAME.equalsIgnoreCase(type)){
			encryption = new Dsa();
		} else if(Sha1.NAME.equalsIgnoreCase(type)){
			encryption = new Sha1();
		} else if(Aes.NAME.equalsIgnoreCase(type)){
			encryption = new Aes();
		} else if(Des.NAME.equalsIgnoreCase(type)){
			encryption = new Md5();
		}
		
		return encryption;
	}
}
