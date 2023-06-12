package experiment;

import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Pairing;
import it.unisa.dia.gas.jpbc.PairingParameters;
import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory;
import it.unisa.dia.gas.plaf.jpbc.pairing.f.TypeFCurveGenerator;

import javax.crypto.KeyGenerator;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import java.math.BigInteger;

public class FEMAC {
    public static void main(String[] args) throws Exception {


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

        //int[] r = new int[]{2, -2, -2, 1};
        int[] r = new int[]{4, -4, -4, 2};
        //int[] w = new int[]{1, 3, 4, 25};
        //int[]  w = new int[]{1, 50, 40, 4100};
        //int[] w = new int[]{1, 7, 9, 130};//10
        int[] w = new int[]{2, 14, 18, 260};//10
        //int[] w = new int[]{1, 25, 19, 986};//30
        //int[] w = new int[]{1, 31, 41, 2642};//50
        //int[] w = new int[]{1, 57, 43, 5098};//70
        //int[] w = new int[]{1, 55, 73, 8354};//90

        //-------------------------------------------------------worker-加密和MAC--------------------------------------------------
        long startTime_W1 = System.currentTimeMillis();                             //获取开始时间

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

        long endTime_W1 = System.currentTimeMillis();                                      //获取结束时间
        System.out.println("Worker加密运行时间：" + (endTime_W1 - startTime_W1) + "ms");

        //--------------------------------------------------------------------计算MAC
        long startTime_W2 = System.currentTimeMillis();                                          //获取开始时间
        int workers = 1;
        String epoch = String.valueOf(startTime_W2);
        String[] macc = new String[workers];
        for (int i = 0; i < workers; i++) {
            KeyGenerator keyGen = KeyGenerator.getInstance("HmacMD5");
            //KeyGenerator keyGen = KeyGenerator.getInstance("HMACSHA1");
            //KeyGenerator keyGen = KeyGenerator.getInstance("HMACSHA224");
            //KeyGenerator keyGen = KeyGenerator.getInstance("HMACSHA256");
            //KeyGenerator keyGen = KeyGenerator.getInstance("HMACSHA512");
            SecretKey key = keyGen.generateKey();

            //byte[] skey = key.getEncoded();
            //System.out.println("第"+i+"位："+new BigInteger(1, skey).toString(16));        // 打印随机生成的key:

            Mac mac = Mac.getInstance("HmacMD5");
            //Mac mac = Mac.getInstance("HMACSHA1");
            //Mac mac = Mac.getInstance("HMACSHA224");
            //Mac mac = Mac.getInstance("HMACSHA256");
            //Mac mac = Mac.getInstance("HMACSHA512");

            mac.init(key);
            mac.update(epoch.getBytes("UTF-8"));
            byte[] result = mac.doFinal();
            //System.out.println(new BigInteger(1, result).toString(16));                  // 打印mac值:
            macc[i] = new BigInteger(1, result).toString(16);
        }
        //System.out.println("KAC_Ki(epoch):"+ Arrays.toString(macc));
        long endTime_W2 = System.currentTimeMillis();                                            //获取结束时间
        System.out.println("Worker计算MAC运行时间：" + (endTime_W2 - startTime_W2) + "ms");
        //System.out.println("Worker数量几乎不影响计算MAC运行时间，200多ms");
        System.out.println("Worker总的运行时间：" + (endTime_W2 - startTime_W2 + endTime_W1 - startTime_W1) + "ms");

        //-------------------------------------------------------Requester-加密-----------------------------------------------
        long startTime_R1 = System.currentTimeMillis();                                    //获取开始时间


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

        //--------------------------------------------（请求者和服务器的MAC和异或）----------------------------
        long startTime_R2 = System.currentTimeMillis();                                  //获取开始时间
        long startTime_S3 = startTime_R2;                                    //获取开始时间
        long tag1 = 0;
        long tag2 = 0;
        int times = 1000;
        for (int j = 0; j < times; j++) {
            for (int i = 0; i < workers*0.5; i++) {
                try {
                    tag1 ^= new BigInteger(macc[i], 16).longValue();

                } catch (NumberFormatException e) {
                    e.printStackTrace();

                }
            }
            for (int i = workers-1; i >= workers*0.5; i--) {
                try {
                    tag2 ^= new BigInteger(macc[i], 16).longValue();

                } catch (NumberFormatException e) {
                    e.printStackTrace();

                }
            }
        }

//        System.out.println("tag1：");
//        System.out.println(tag1);
//        System.out.println("tag2：");
//        System.out.println(tag2);



        long endTime_R2 = System.currentTimeMillis();                                         //获取结束时间
        System.out.println("Requestor验证阶段运行时间：" + (endTime_W2 - startTime_W2 +(endTime_R2 - startTime_R2)/times) + "ms");
        System.out.println("Requestor总的运行时间：" + (endTime_R1 - startTime_R1 + endTime_W2 - startTime_W2 +(endTime_R2 - startTime_R2)/times) + "ms");
        long endTime_S3 = endTime_R2;                                                        //获取结束时间
        System.out.println("ServerMAC聚合运行时间：" + (endTime_S3 - startTime_S3)/times + "ms");//结果记得除以times


        //-------------------------------------------------------Server-解密--------------------------------------------------
        long startTime_S2 = System.currentTimeMillis();                                    //获取开始时间

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
                endTime_S2 = System.currentTimeMillis();                                       //获取结束时间
                System.out.println("Server解密运行时间：" + (endTime_S2 - startTime_S2)*workers + "ms");
                break;
            }
        }
        System.out.println("Server总的运行时间：" + (endTime_S1 - startTime_S1 + (endTime_S2 - startTime_S2)*workers + endTime_S3 - startTime_S3) + "ms");
        return;
    }

}


