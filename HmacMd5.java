package experiment;

import javax.crypto.KeyGenerator;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import java.math.BigInteger;

public class HmacMd5 {
    public static void main(String[] args) throws Exception {

        long startTime = System.currentTimeMillis();
        int workers = 30;                                                             //workers 数量
        int max = 10;                                                                 //h-vm
        BigInteger agg = new BigInteger("1", 16);

        //HmacMD5
        String epoch = String.valueOf(startTime);                                      //获取开始时间
        String[] macc=new String[workers];
        for (int i = 0; i < workers; i++) {
            KeyGenerator keyGen = KeyGenerator.getInstance("HmacMD5");
            SecretKey key = keyGen.generateKey();

            //byte[] skey = key.getEncoded();
            //System.out.println("第"+i+"位："+new BigInteger(1, skey).toString(16));        // 打印随机生成的key:
            Mac mac = Mac.getInstance("HmacMD5");
            mac.init(key);
            mac.update(epoch.getBytes("UTF-8"));
            byte[] result = mac.doFinal();
            //System.out.println(new BigInteger(1, result).toString(16));                  // 打印mac值:
            macc[i] = new BigInteger(1, result).toString(16);
        }
        //System.out.println("KAC_Ki(epoch):"+Arrays.toString(macc));

        //RSA
        BigInteger p = new BigInteger("e86c7f16fd24818ffc502409d33a83c2a2a07fdfe971eb52de97a3de092980279ea29e32f378f5e6b7ab1049bb9e8c5eae84dbf2847eb94ff14c1e84cf568415", 16);
        BigInteger q = new BigInteger("d7d9d94071fcc67ede82084bbedeae1aaf765917b6877f3193bbaeb5f9f36007127c9aa98d436a80b3cce3fcd56d57c4103fb18f1819d5c238a49b0985fe7b49", 16);
        BigInteger e = new BigInteger("10001", 16);
        RSA rsa = new RSA(p, q, e);

        BigInteger[] ciph=new BigInteger[workers];
        for (int i = 0; i < workers; i++) {
            BigInteger plaintext = new BigInteger(macc[i], 16);
//            System.out.println("明文" + i + "：" + plaintext.toString(16));
            for (int j = 0; j <max ; j++) {
                BigInteger ciphertext = RSA.encrypt(plaintext, rsa.n, e);
//                System.out.println(ciphertext);
//                System.out.println("密文" + i+j + "：" + ciphertext.toString(16));
                plaintext = ciphertext;
                if(j==max-1){
                    ciph[i]=plaintext;
//                    System.out.println("F_e_(h-vm):"+plaintext);
                }
            }
//            System.out.println("ciph"+i+":"+ciph[i]);
        }
        for (int i = 0; i < workers; i++) {
            agg = ciph[i].multiply(agg).mod(p.multiply(q));
        }
        System.out.println("agg:"+agg.toString(16));

        long endTime = System.currentTimeMillis();                                //获取结束时间

        System.out.println("程序运行时间：" + (endTime - startTime) + "ms");         //输出程序运行时间
    }
}
