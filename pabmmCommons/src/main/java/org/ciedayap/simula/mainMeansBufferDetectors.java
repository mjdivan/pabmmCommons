/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ciedayap.simula;

import org.ciedayap.mair.utils.*;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;
import java.util.Random;
import java.util.Scanner;
import org.ciedayap.buffer.BufferException;
import org.ciedayap.buffer.SPCBasedBuffer;
import org.ciedayap.buffer.SPCMeansBasedBuffer;
import org.ciedayap.buffer.SPCMeansBasedBufferNode;
import org.ciedayap.changedetectors.DetectorException;
import org.ciedayap.filters.SPCException;

/**
 *
 * @author mjdivan
 */
public class mainMeansBufferDetectors implements Observer{
    public int alarms=0;
    public int alarms_chd=0;
    public int alarms_tl=0;
    private int ksim=-1;
    private InstrumentationAgent ia;
    
    public static void main(String args[]) throws DetectorException, InterruptedException, SPCException, BufferException
    {        
        InstrumentationAgent ia=new InstrumentationAgent();        
        
        System.out.println("Choose one simulation to perform:");
        System.out.println("\t1. [Buffer] Analyzing the Buffer sizes and 'add' operation without load-shedding for 5 minutes.... ");
        System.out.println("\t2. [Buffer] Analyzing the Buffer sizes and 'add' operation with load-shedding for 5 minutes.... ");
        System.out.println("\t3. [Buffer] Analyzing the Buffer operations for 15 minutes with LD deactivated.... ");               
        System.out.println("\t4. [Buffer] Analyzing the Buffer operations for 15 minutes with LD activated.... ");               
        System.out.println("\t5. [Buffer] Analyzing the Buffer operations for 15 minutes without LD and temporal barrier.... ");               
        
        Scanner keyboard = new Scanner(System.in);
        System.out.println("Choose your option [1-5]: ");
        int myint = keyboard.nextInt();

        switch(myint)
        {
            case 1:
                sim5_BufferSizesOpTime(ia, 5,false);//it assesses the buffer size and the add operation without load shedding
                break;
            case 2:
                sim5_BufferSizesOpTime(ia, 5,true);//it assesses the buffer size and the add operation with load shedding
                break;              
            case 3:
                sim6_BufferSizesandClockLD(ia,15,false,false);//it analyzes the alarms activating without LD for 5 minutes
                break;
            case 4:
                sim6_BufferSizesandClockLD(ia,15,true,false);//it analyzes the alarms activating without LD for 5 minutes
                break;                
            case 5:
                sim6_BufferSizesandClockLD(ia,15,false,true);//only data change filter enabled (LD and temporalBarrier disabled)
                break;                                
            default:
        }        
    }
    
    public static void sim5_BufferSizesOpTime(InstrumentationAgent ia, int minutes, boolean loadshedding) throws DetectorException, SPCException, InterruptedException, BufferException
    {
        System.out.println("Analyzing the buffer sizes for "+minutes+" minutes ");
                
        Random r=new Random();
        StringBuilder sb=new StringBuilder();
        
        System.out.println("Starting "+ZonedDateTime.now()+" Cycle: "+minutes+" minutes");
        System.out.println("nanoTime;measurepermetric;nmetric;size;addTime");
        double mean=35;
        double ndev=2.0;
        for(int nm=15;nm<=15;nm++)//15 metrics
        {
            ArrayList defmet=genAttMetricList(nm);
            for(Object s:defmet)
            {
                System.out.println((s==null)?"null":s);
            }
            
            for(int trigger=2;trigger<=2;trigger++)
            {
                SPCMeansBasedBuffer buffer=SPCMeansBasedBuffer.create("PRJ_1", defmet, 1000, 5.0,false);//No verbose
                buffer.setLoadShedding(loadshedding);
                
                long startsim=System.nanoTime();
                long measures=0;
                while((System.nanoTime()-startsim)<=(minutes*60000000000L))
                {
                    //Add one measure per metric
                    String met[]=buffer.getMetricIDs();
                    double vals[]=new double[met.length];
                    for(int i=0;i<vals.length;i++) vals[i]=(mean+ndev*r.nextGaussian());
                    
                    long startOpTime=System.nanoTime();
                    buffer.addMeasures(vals);
                    long endOpTime=System.nanoTime();
                    measures++;
                    
                    long size=ia.sizeDeepOf(buffer);
                    System.out.println(System.nanoTime()+";"+measures+";"+nm+";"+size+";"+(endOpTime-startOpTime));
                    
                    Thread.sleep(100);
                }

                System.out.print("Timestamp\t");
                for(String s:buffer.getMetricIDs())
                    System.out.print(s+"\t");    
                System.out.println();
                
                for(Object o:buffer.getBufferData_and_clean())
                {
                    SPCMeansBasedBufferNode node=(SPCMeansBasedBufferNode)o;
                    System.out.print(node.getTimestamp()+"\t");
                    for(Double d:node.getValues())
                        System.out.print(d+"\t");
                    System.out.println();
                }
                buffer.shutdown();                
            }
            
            defmet.clear();
        }
    }
    
    public static void sim6_BufferSizesandClockLD(InstrumentationAgent ia, int minutes, boolean loadshedding,boolean onlyFilter) throws DetectorException, SPCException, InterruptedException, BufferException
    {
        System.out.println("Analyzing the buffer sizes and clock behaviours for "+minutes+" minutes in verbose mode");
                
        Random r=new Random();
        StringBuilder sb=new StringBuilder();
        
        System.out.println("Starting "+ZonedDateTime.now()+" Cycle: "+minutes+" minutes");
        sb.append("nanoTime").append(";")
          .append("#records").append(";")
          .append("kind").append(";")
          .append("alarms_tl").append(";")
          .append("alarms_chd").append(";")
          .append("total_alarms").append(";")
          .append("RecordBytes");

        System.out.println(sb.toString());
        
        double mean=9;
        double ndev=2.0;
        for(int nm=10;nm<=10;nm++)
        {
            ArrayList defmet=genAttMetricList(nm);
            
            for(int trigger=2;trigger<=2;trigger++)
            {
                mainMeansBufferDetectors trx=new mainMeansBufferDetectors();
                trx.setKsim(6);
                trx.setIa(ia);
                
                SPCBasedBuffer buffer;
                if(!onlyFilter)
                {
                    buffer=SPCMeansBasedBuffer.create("PRJ_1", defmet, 1000, 5.0,false);//verbose
                    ((SPCMeansBasedBuffer)buffer).setLoadShedding(loadshedding);                                
                    buffer.activateClock(1000*15);//15 seconds (the load shedding flag will determine whether the transmission is performed or not)
                }
                else
                {//Only data change filter..Load-shedding and temporal barrier disabled                   
                    buffer=SPCMeansBasedBuffer.create("PRJ_1", defmet, 1000, 5.0,false);//verbose                    
                }
                
                buffer.addObserver(trx);
                
                long startsim=System.nanoTime();
                long measures=0;
                while((System.nanoTime()-startsim)<=(minutes*60000000000L))
                {
                    //Add one measure per metric
                    String met[]=buffer.getMetricIDs();
                    double vals[]=new double[met.length];
                    for(int i=0;i<vals.length;i++) vals[i]=(mean+ndev*r.nextGaussian());
                    
                    buffer.addMeasures(vals);
                    measures++;
                    
                    long size=ia.sizeDeepOf(buffer);                    
                    
                    Thread.sleep(100);                    
                }

                System.out.println("[ "+ZonedDateTime.now()+"] The Time has finished.");
                buffer.shutdown();                
            }
            
            defmet.clear();
        }
    }
    
    /**
     * It generates a number of metric with the same likelihood to initialize the filters
     * @param metrics The nuumber of metrics to be contained in the list
     * @return The list with the number of metrics under the format "<attID>;<metID>;<likelihood>", NULL when metrics parameter is loweer than 1
     */
    private static ArrayList<String> genAttMetricList(int metrics)
    {
        if(metrics<1) return null;
        
        double prob=1.0/(double)metrics;
        
        StringBuilder sb=new StringBuilder();
        ArrayList<String> list=new ArrayList();
        for(int i=0;i<metrics;i++)
        {
            sb.delete(0, sb.length());
            
            sb.append("attID").append(i).append(";")
              .append("mID").append(i).append(";")
              .append(prob);
                   
            list.add(sb.toString());
        }
        
        return list;
    }
    
    @Override
    public void update(Observable o, Object arg) {
        alarms++;
        if(o instanceof SPCBasedBuffer)
        {
            SPCBasedBuffer buffer=(SPCBasedBuffer)o;
            Object data[]=buffer.getBufferData_and_clean();//transmitting
            //System.out.println(ZonedDateTime.now()+" [Data: "+data.length+" records] "+arg);
            
            if(ksim==6)
            {
                String mes=(String)arg;
                StringBuilder sb=new StringBuilder();
                sb.append(System.nanoTime()).append(";")
                  .append(data.length).append(";");
                if(mes.contains("Detected Change")){
                    this.alarms_chd++;
                    sb.append("DC").append(";");
                }
                else {
                    this.alarms_tl++;
                    sb.append("TL").append(";");
                }

                sb.append(alarms_tl).append(";")
                  .append(alarms_chd).append(";")
                  .append(alarms).append(";")
                  .append(ia.sizeDeepOf(data));
                
                System.out.println(sb.toString());
            }

        }
        
        //System.out.println("***Notified Observer. Global: "+arg+" Accumulated Alarms: "+alarms);
    }

    /**
     * @return the ksim
     */
    public int getKsim() {
        return ksim;
    }

    /**
     * @param ksim the ksim to set
     */
    public void setKsim(int ksim) {
        this.ksim = ksim;
    }

    /**
     * @return the ia
     */
    public InstrumentationAgent getIa() {
        return ia;
    }

    /**
     * @param ia the ia to set
     */
    public void setIa(InstrumentationAgent ia) {
        this.ia = ia;
    }
    
}
