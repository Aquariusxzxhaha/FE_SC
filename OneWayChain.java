package experiment;

import com.sun.org.apache.xalan.internal.xsltc.util.IntegerArray;
import java.math.BigInteger;

public class OneWayChain {
    private static IntegerArray Convert;

    public static void main(String[] args) {

        long startTime = System.currentTimeMillis(); //获取开始时间

        BigInteger p = new BigInteger("e86c7f16fd24818ffc502409d33a83c2a2a07fdfe971eb52de97a3de092980279ea29e32f378f5e6b7ab1049bb9e8c5eae84dbf2847eb94ff14c1e84cf568415", 16);
        BigInteger q = new BigInteger("d7d9d94071fcc67ede82084bbedeae1aaf765917b6877f3193bbaeb5f9f36007127c9aa98d436a80b3cce3fcd56d57c4103fb18f1819d5c238a49b0985fe7b49", 16);
        BigInteger e = new BigInteger("10001", 16);
        RSA rsa = new RSA(p, q, e);
        System.out.println("p=" + p.toString(16));
        System.out.println("q=" + q.toString(16));
        System.out.println("n=" + rsa.n.toString(16));
        System.out.println("φ(n)=" + rsa.getPhi_n().toString(16));
        System.out.println("e=" + e.toString(16));
        System.out.println("d=" + rsa.getD().toString(16));

        String[] str = {"16eba0e46f21a42db11820a453f5150a","16eba0e46f21a42db11820a453f5150a","ea4a92b","3c5a99da","47f37ef1","5a01ccaa"};


        int max = 5;
        for (int i = 0; i < 6; i++) {
            BigInteger plaintext = new BigInteger(str[i], 16);
            System.out.println("明文" + i + "：" + plaintext.toString(16));

            for (int j = 0; j <max ; j++) {
                BigInteger ciphertext = RSA.encrypt(plaintext, rsa.n, e);
                System.out.println("密文" + i+j + "：" + ciphertext.toString(16));
                plaintext = ciphertext;
            }


        }
        long endTime = System.currentTimeMillis(); //获取结束时间

        System.out.println("程序运行时间：" + (endTime - startTime) + "ms"); //输出程序运行时间
    }
}