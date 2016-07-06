package com.woniu.sncp.cbss.api.profile.config;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.Security;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;

/**
 * DES 加密、解密工具类
 * @author sun.xc
 */
public class EncryptUtils {
    public static Key createKey() throws NoSuchAlgorithmException {//创建一个密钥
        Security.insertProviderAt(new com.sun.crypto.provider.SunJCE(), 1);
        KeyGenerator generator = KeyGenerator.getInstance("DES");
        generator.init(new SecureRandom());

        return generator.generateKey();
    }
    public static Key getKey(InputStream is) {
        try {
            ObjectInputStream ois = new ObjectInputStream(is);
            return (Key) ois.readObject();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
    private static byte[] doEncrypt(Key key, byte[] data) {//对数据进行加密
        try {
            Cipher cipher = Cipher.getInstance("DES/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, key);
            return cipher.doFinal(data);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
    public static InputStream doDecrypt(Key key, InputStream in) {//对数据进行解密
        try {
            Cipher cipher = Cipher.getInstance("DES/ECB/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, key);
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            byte[] tmpbuf = new byte[1024];
            int count = 0;
            while ((count = in.read(tmpbuf)) != -1) {
                bout.write(tmpbuf, 0, count);
                tmpbuf = new byte[1024];
            }
            in.close();
            byte[] orgData = bout.toByteArray();
            byte[] raw = cipher.doFinal(orgData);
            return new ByteArrayInputStream(raw);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
    
//    public static void main(String[] args) throws Exception {//提供了Java命令使用该工具的功能
//        if (args.length == 2 && args[0].equals("key")) {// 生成密钥文件
//            Key key = EncryptUtils.createKey();
//            String path = System.getProperty("user.dir") + "/" + args[1];
//            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(path));
//            oos.writeObject(key);
//            oos.close();
//            System.out.println("gen encrypt key: " + path);
//        } else if (args.length == 3 && args[0].equals("encrypt")) {//对文件进行加密
////            File file = new File(System.getProperty("user.dir") + "/" + args[1]);
//            File file = new File("/opt/security/db/" + args[1]);
//            FileInputStream in = new FileInputStream(file);
//            ByteArrayOutputStream bout = new ByteArrayOutputStream();
//            byte[] tmpbuf = new byte[1024];
//            int count = 0;
//            while ((count = in.read(tmpbuf)) != -1) {
//                bout.write(tmpbuf, 0, count);
//                tmpbuf = new byte[1024];
//            }
//            in.close();
//            byte[] orgData = bout.toByteArray();
//            Key key = getKey(new FileInputStream("/opt/security/db/" + args[2]));
//            byte[] raw = EncryptUtils.doEncrypt(key, orgData);
//            file = new File(file.getParent() + "\\en_" + file.getName());
//            FileOutputStream out = new FileOutputStream(file);
//            out.write(raw);
//            out.close();
//            System.out.println("成功加密，加密文件位于:"+file.getAbsolutePath());
//        } else if (args.length == 3 && args[0].equals("decrypt")) {//对文件进行解密
//            File file = new File(System.getProperty("user.dir") + "/" + args[1]);
//            FileInputStream fis = new FileInputStream(file);
//            Key key = getKey(new FileInputStream(args[2]));
//            InputStream raw = EncryptUtils.doDecrypt(key, fis);
//            ByteArrayOutputStream bout = new ByteArrayOutputStream();
//            byte[] tmpbuf = new byte[1024];
//            int count = 0;
//            while ((count = raw.read(tmpbuf)) != -1) {
//                bout.write(tmpbuf, 0, count);
//                tmpbuf = new byte[1024];
//            }
//            raw.close();
//            byte[] orgData = bout.toByteArray();
//            file = new File(file.getParent() + "\\rs_" + file.getName());
//            FileOutputStream fos = new FileOutputStream(file);
//            fos.write(orgData);
//            System.out.println("成功解密，解密文件位于:"+file.getAbsolutePath());
//        }
//    }
    
}
