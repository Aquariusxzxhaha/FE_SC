package experiment;

import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Pairing;
import it.unisa.dia.gas.jpbc.PairingParameters;
import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory;
import it.unisa.dia.gas.plaf.jpbc.pairing.f.TypeFCurveGenerator;

import javax.crypto.KeyGenerator;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class FE {
 public static void main(String[] args) throws NoSuchAlgorithmException, InvalidKeyException, UnsupportedEncodingException {

  int workers = 1;

  //int[]  r = new int[]{2, -2, -2, 1};//算奇数距离的
  //int[] w = new int[]{1, 3, 4, 25};
  //int[]  w = new int[]{1, 50, 40, 4100};
  //int[] w = new int[]{1, 7, 9, 130};//10
  //int[] w = new int[]{1, 25, 19, 986};//30
  //int[] w = new int[]{1, 31, 41, 2642};//50
  //int[] w = new int[]{1, 57, 43, 5098};//70
  //int[] w = new int[]{1, 55, 73, 8354};//90
  int[]  r = new int[]{4, -4, -4, 2};//算偶数距离的
  //int[] w = new int[]{2, 14, 18, 260};//20
  int[] w = new int[]{2, 50, 38, 1972};//30
  //-------------------------------------------------------Server-初始化--------------------------------------------------
  long startTime_S1 = System.currentTimeMillis();

  int rBits = 160;
  TypeFCurveGenerator pg = new TypeFCurveGenerator(rBits);
  PairingParameters typeFParams = pg.generate();
  Pairing pairing = PairingFactory.getPairing(typeFParams);
  Element g1 = pairing.getG1().newRandomElement().getImmutable();//G1生成元
  Element g2 = pairing.getG2().newRandomElement().getImmutable();//G2生成元
  int[][] B = new int[][]{{1, 0, 0, 0, 0, 0, 0, 0}, {0, 1, 0, 0, 0, 0, 0, 0}, {0, 0, 1, 0, 0, 0, 0, 0}, {0, 0, 0, 1, 0, 0, 0, 0}, {0, 0, 0, 0, 1, 0, 0, 0}, {0, 0, 0, 0, 0, 1, 0, 0}, {0, 0, 0, 0, 0, 0, 1, 0}, {0, 0, 0, 0, 0, 0, 0, 1}};
  int[][] C = new int[][]{{1, 0, 0, 0, 0, 0, 0, 0}, {0, 1, 0, 0, 0, 0, 0, 0}, {0, 0, 1, 0, 0, 0, 0, 0}, {0, 0, 0, 1, 0, 0, 0, 0}, {0, 0, 0, 0, 1, 0, 0, 0}, {0, 0, 0, 0, 0, 1, 0, 0}, {0, 0, 0, 0, 0, 0, 1, 0}, {0, 0, 0, 0, 0, 0, 0, 1}};
  //int[][] D = new int[][]{{1,0},{0,1}};
  //int[][] E = new int[][]{{1,0},{0,1}};

  long endTime_S1 = System.currentTimeMillis();
  System.out.println("Server初始化运行时间：" + (endTime_S1 - startTime_S1) + "ms");


  //-------------------------------------------------------worker-加密--------------------------------------------------
  long startTime_W1 = System.currentTimeMillis();



  Element b = pairing.getZr().newRandomElement().getImmutable();//worker的随机数
  Element b1 = pairing.getZr().newRandomElement().getImmutable();
  int[][] temp4 = new int[4][8];
  int[] temp5 = new int[8];
  int[][] temp6 = new int[4][8];
  int[] temp7 = new int[8];
  for (int i = 0; i < 4; i++) {
   for (int j = 0; j < 8; j++) {
    temp4[i][j] = w[i] * B[i][j];
    temp5[j] += temp4[i][j];
    temp6[i][j] = w[i] * B[4 + i][j];
    temp7[j] += temp6[i][j];
   }
  }
  Element[] g2_e = new Element[8];//g2的指数
  for (int i = 0; i < 8; i++) {
   g2_e[i] = b.mul(temp5[i]).add(b1.mul(temp7[i]));
  }
  Element[] V1 = new Element[8];
  for (int i = 0; i < 8; i++) {
   V1[i] = g2.powZn(g2_e[i]);//CTw的第一项
  }
  Element V20 = g2.powZn(b);//CTw的第二项
  Element V21 = g2.powZn(b1);

  long endTime_W1 = System.currentTimeMillis();
  System.out.println("Worker加密运行时间：" + (endTime_W1 - startTime_W1) + "ms");

  //-------------------------------------------------------worker-算单向链--------------------------------------------------
  long startTime_W2 = System.currentTimeMillis();
  int worker = 1;                                                             //workers 数量
  int max = 10;                                                                 //h-vm
  BigInteger agg = new BigInteger("1", 16);

  //HmacMD5
  String epoch = String.valueOf(startTime_W2);                                      //获取开始时间
  String[] macc=new String[worker];
  for (int i = 0; i < worker; i++) {
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

  BigInteger[] ciph=new BigInteger[worker];
  for (int i = 0; i < worker; i++) {
   BigInteger plaintext = new BigInteger(macc[i], 16);
   //System.out.println("明文" + i + "：" + plaintext.toString(16));
   for (int j = 0; j <max ; j++) {
    BigInteger ciphertext = RSA.encrypt(plaintext, rsa.n, e);
    //System.out.println(ciphertext);
    //System.out.println("密文" + i+j + "：" + ciphertext.toString(16));
    plaintext = ciphertext;
    if(j==max-1){
     ciph[i]=plaintext;
     //System.out.println("F_e_(h-vm):"+plaintext);
    }
   }
   //System.out.println("ciph"+i+":"+ciph[i]);
  }
  for (int i = 0; i < worker; i++) {
   agg = ciph[i].multiply(agg).mod(p.multiply(q));
  }
  //System.out.println("agg:"+agg.toString(16));

  long endTime_W2 = System.currentTimeMillis();                                //获取结束时间

  System.out.println("Worker算单向链运行时间：" + (endTime_W2 - startTime_W2) + "ms");         //输出程序运行时间
  System.out.println("Worker总的运行时间：" + (endTime_W1 - startTime_W1 + endTime_W2 - startTime_W2) + "ms");         //输出程序运行时间


  //-------------------------------------------------------Requester-加密-----------------------------------------------
  long startTime_R1 = System.currentTimeMillis();


  Element a = pairing.getZr().newRandomElement().getImmutable();//Requester的随机数
  Element a1 = pairing.getZr().newRandomElement().getImmutable();
  int[][] temp = new int[4][8];
  int[] temp1 = new int[8];
  int[][] temp2 = new int[4][8];
  int[] temp3 = new int[8];
  for (int i = 0; i < 4; i++) {
   for (int j = 0; j < 8; j++) {
    temp[i][j] = r[i] * C[i][j];
    temp1[j] += temp[i][j];
    temp2[i][j] = r[i] * C[4 + i][j];
    temp3[j] += temp2[i][j];
   }
  }
  Element[] g1_e = new Element[8];//g1的指数
  for (int i = 0; i < 8; i++) {
   g1_e[i] = a.mul(temp1[i]).add(a1.mul(temp3[i]));
  }
  Element[] C1 = new Element[8];
  for (int i = 0; i < 8; i++) {
   C1[i] = g1.powZn(g1_e[i]);//CTr的第一项
  }
  Element C20 = g1.powZn(a);//CTr的第二项
  Element C21 = g1.powZn(a1);

  long endTime_R1 = System.currentTimeMillis();                                //获取结束时间

  System.out.println("Requestor加密运行时间：" + (endTime_R1 - startTime_R1) + "ms");

  //-------------------------------------------------------Server-解密--------------------------------------------------
  long startTime_S2 = System.currentTimeMillis();

  Element[] g1_e_g2_e = new Element[8];//g1*g2
  for (int i = 0; i < 8; i++) {
   g1_e_g2_e[i] = g1_e[i].mulZn(g2_e[i]);
  }
  Element t = g1_e_g2_e[0];
  for (int i = 1; i < 8; i++) {
   t = t.add(g1_e_g2_e[i]);
  }
  Element D1 = pairing.pairing(g1, g2).powZn(t);

  Element s0 = a.mulZn(b);
  Element s1 = a1.mulZn(b1);
  Element s = s0.add(s1);
  Element D2 = pairing.pairing(g1, g2).powZn(s);

  long endTime_S2 = 0;
  for (double i = 1; i < 100; i++) {
   D2 = pairing.pairing(g1, g2).powZn(s.mul((int) Math.pow((double) i, 2)));
   if (D1.isEqual(D2)) {
    System.out.println("distance:" + i);
    endTime_S2 = System.currentTimeMillis();
    System.out.println("Server解密运行时间：" + (endTime_S2 - startTime_S2)*workers + "ms");
    break;
   }
  }

  //-------------------------------------------------------Server-算单向链--------------------------------------------------
  long startTime_S3 = System.currentTimeMillis();
  BigInteger aggSer = new BigInteger("1", 16);

  //HmacMD5
  String epochSer = String.valueOf(startTime_S3);                                      //获取开始时间
  String[] maccSer=new String[workers];
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
   maccSer[i] = new BigInteger(1, result).toString(16);
  }
  //System.out.println("KAC_Ki(epoch):"+Arrays.toString(macc));

  //RSA
  BigInteger[] ciphSer=new BigInteger[workers];
  for (int i = 0; i < workers; i++) {
   BigInteger plaintext = new BigInteger(maccSer[i], 16);
   //System.out.println("明文" + i + "：" + plaintext.toString(16));
   for (int j = 0; j <max ; j++) {
    BigInteger ciphertext = RSA.encrypt(plaintext, rsa.n, e);
    //System.out.println(ciphertext);
    //System.out.println("密文" + i+j + "：" + ciphertext.toString(16));
    plaintext = ciphertext;
    if(j==max-1){
     ciphSer[i]=plaintext;
     //System.out.println("F_e_(h-vm):"+plaintext);
    }
   }
   //System.out.println("ciph"+i+":"+ciph[i]);
  }
  for (int i = 0; i < workers; i++) {
   aggSer = ciphSer[i].multiply(aggSer).mod(p.multiply(q));
  }
  //System.out.println("agg:"+agg.toString(16));

  long endTime_S3 = System.currentTimeMillis();                                //获取结束时间

  System.out.println("Server算单向链运行时间：" + (endTime_S3 - startTime_S3) + "ms");
  System.out.println("Server总的运行时间：" + (endTime_S1 - startTime_S1 + (endTime_S2 - startTime_S2)*workers + endTime_S3 - startTime_S3) + "ms");


  //-------------------------------------------------------Requestor-算单向链--------------------------------------------------
  long startTime_R2 = startTime_S3;
  long endTime_R2 = endTime_S3;
  System.out.println("Requestor算单向链运行时间：" + (endTime_R2 - startTime_R2) + "ms");
  System.out.println("Requestor总的运行时间：" + (endTime_R1 - startTime_R1 + endTime_R2 - startTime_R2) + "ms");
  return;
 }

}


