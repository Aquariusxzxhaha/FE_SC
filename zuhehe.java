package experiment;

import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Pairing;
import it.unisa.dia.gas.jpbc.PairingParameters;
import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory;
import it.unisa.dia.gas.plaf.jpbc.pairing.f.TypeFCurveGenerator;

public class zuhehe {
    public static void main(String[] args) {

     int rBits = 160;
     TypeFCurveGenerator pg = new TypeFCurveGenerator(rBits);
     PairingParameters typeFParams = pg.generate();
     Pairing pairing = PairingFactory.getPairing(typeFParams);

     Element g1 = pairing.getG1().newRandomElement().getImmutable();//G1生成元
     Element g2 = pairing.getG2().newRandomElement().getImmutable();//G2生成元
     Element a = pairing.getZr().newRandomElement().getImmutable();//Requester的随机数
     Element a1 = pairing.getZr().newRandomElement().getImmutable();
     Element b = pairing.getZr().newRandomElement().getImmutable();//worker的随机数
     Element b1 = pairing.getZr().newRandomElement().getImmutable();

     int[][] B = new int[][]{{1,0,0,0,0,0,0,0},{0,1,0,0,0,0,0,0},{0,0,1,0,0,0,0,0},{0,0,0,1,0,0,0,0},{0,0,0,0,1,0,0,0},{0,0,0,0,0,1,0,0},{0,0,0,0,0,0,1,0},{0,0,0,0,0,0,0,1}};
     int[][] C = new int[][]{{1,0,0,0,0,0,0,0},{0,1,0,0,0,0,0,0},{0,0,1,0,0,0,0,0},{0,0,0,1,0,0,0,0},{0,0,0,0,1,0,0,0},{0,0,0,0,0,1,0,0},{0,0,0,0,0,0,1,0},{0,0,0,0,0,0,0,1}};
//     int[][] D = new int[][]{{1,0},{0,1}};
//     int[][] E = new int[][]{{1,0},{0,1}};
     int[]  r = new int[]{200, -20, -20, 1};
     int[]  w = new int[]{1, 50, 40, 4100};

     int[][] temp = new int[4][8];int[][] temp4 = new int[4][8];
     int[] temp1 = new int[8];int[] temp5 = new int[8];
     int[][] temp2 = new int[4][8];int[][] temp6 = new int[4][8];
     int[] temp3 = new int[8];int[] temp7 = new int[8];

     for (int i = 0; i < 4; i++) {
      for (int j = 0; j < 8; j++) {
       temp[i][j] = r[i]*C[i][j];
       temp1[j] += temp[i][j];

       temp2[i][j] = r[i]*C[4+i][j];
       temp3[j] += temp2[i][j];

       temp4[i][j] = w[i]*B[i][j];
       temp5[j] += temp4[i][j];

       temp6[i][j] = w[i]*B[4+i][j];
       temp7[j] += temp6[i][j];
      }
     }

     Element[] g1_e = new Element[8];//g1的指数
     Element[] g2_e = new Element[8];//g2的指数
     Element[] g1_e_g2_e = new Element[8];//g1*g2
     for (int i = 0; i < 8; i++) {
      g1_e[i] = a.mul(temp1[i]).add(a1.mul(temp3[i]));
      g2_e[i] = b.mul(temp5[i]).add(b1.mul(temp7[i]));
      g1_e_g2_e[i] = g1_e[i].mulZn(g2_e[i]);
     }
     Element t = g1_e_g2_e[0];
     for (int i = 1; i < 8; i++) {
      t=t.add(g1_e_g2_e[i]);
     }

     Element D1 = pairing.pairing(g1, g2).powZn(t);

     Element s0 = a.mulZn(b);
     Element s1 = a1.mulZn(b1);
     Element s = s0.add(s1);
     Element D2 = pairing.pairing(g1, g2).powZn(s);

     for (int i=1; i < 100; i++) {
      D2 = pairing.pairing(g1,g2).powZn(s.mul((int)Math.pow((double)i,2)));
      if(D1.isEqual(D2)){
       System.out.println("distance:"+i);
       return;
      }
     }

//     //请求者和工作者的位置密文
//     Element[] C1 = new Element[8];
//     Element[] V1 = new Element[8];
//     for (int i = 0; i < 8; i++) {
//      C1[i] = g1.powZn(g1_e[i]);//CTr的第一项
//      V1[i] = g2.powZn(g2_e[i]);//CTw的第一项
//     }
//
//     Element C20 = g1.powZn(a);//CTr的第二项
//     Element C21 = g1.powZn(a1);
//
//     Element V20 = g2.powZn(b);//CTw的第二项
//     Element V21 = g2.powZn(b1);
//
//     System.out.println("g1:"+g1.toString());
//     System.out.println("g2:"+g2.toString());
//     System.out.println("a:"+a.toString());
//     System.out.println("a1:"+a1.toString());
//     System.out.println("b:"+b.toString());
//     System.out.println("b1:"+b1.toString());
//     System.out.println("D1:"+D1.toString());
//     System.out.println("D2:"+D2.toString());

    }

}

