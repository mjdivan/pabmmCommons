/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ciedayap.mair.utils;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Observable;
import java.util.Observer;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.ciedayap.buffer.BufferException;
import org.ciedayap.buffer.SPCBasedBuffer;
import org.ciedayap.buffer.SPCBasedBufferNode;
import org.ciedayap.changedetectors.DetectorException;
import org.ciedayap.changedetectors.SPCProjectDetector;
import org.ciedayap.filters.SPCBasedFilter;
import org.ciedayap.filters.SPCException;

/**
 *
 * @author mjdivan
 */
public class mainDetectors implements Observer{
    public int alarms=0;
    public int alarms_chd=0;
    public int alarms_tl=0;
    private int ksim=-1;
    private InstrumentationAgent ia;
    
    public static void main(String args[]) throws DetectorException, InterruptedException, SPCException, BufferException
    {        
        InstrumentationAgent ia=new InstrumentationAgent();        
        
        System.out.println("Choose one simulation to perform:");
        System.out.println("\t1. [Creation] Detectors. Creation Time and Associated Sizes (Min:1, Max:100)");
        System.out.println("\t2. [Alarms] Evaluating the raise alarms when TRIGGER and the numner of metrics varies (Up to 2sigma).");
        System.out.println("\t3. [Alarms] Evaluating the raise alarms when TRIGGER and the numner of metrics varies (Up to 3sigma).");
        System.out.println("\t4. [Filter] Estimatig the individual operation times in the 'compute' and 'addMeasure' operations. ");
        System.out.println("\t5. [Buffer] Analyzing the Buffer sizes during 5 minutes.... ");
        System.out.println("\t6. [Buffer] Analyzing the Clock Behaviour during 5 minutes in verbose mode.... ");
        
        Scanner keyboard = new Scanner(System.in);
        System.out.println("Choose your option [1-6]: ");
        int myint = keyboard.nextInt();

        switch(myint)
        {
            case 1:
                sim1_SizePerMetrics(ia,1,100);
                break;
            case 2:
                sim2_evaluatingAlarms(ia, 5, 7,2);//The random values will be generated with up to 2sigma
                break;
            case 3:
                sim2_evaluatingAlarms(ia, 5, 7,3);//The random values will be generated with up to 3sigma
                break;              
            case 4:
                sim3_opTimes(5);//Measure the individual operation time of the filter
                break;
            case 5:
                sim5_BufferSizes(ia, 5);
                break;
            case 6:
                sim6_BufferSizesandClock(ia,5);
                break;
                
                //Verbose mode individual filtering to see the values
            default:
        }
        
       /* String json="{\"IDMessage\":\"1\",\"version\":\"1.0\",\"creation\":\"2020-04-22T14:12:38.205-03:00[America/Argentina/Salta]\",\"projects\":{\"projects\":[{\"ID\":\"PRJ_1\",\"name\":\"Outpatient Monitoring\",\"startDate\":\"2020-04-22T14:12:38.203-03:00[America/Argentina/Salta]\",\"infneed\":{\"ID\":\"IN_1\",\"purpose\":\"Avoid severe damages through the prevention of risks with direct impact in the outpatient health\",\"shortTitle\":\"Monitor the Outpatient\",\"specifiedEC\":{\"ID\":\"EC1\",\"name\":\"Outpatient\",\"superCategory\":{\"describedBy\":{\"characteristics\":[]},\"monitored\":{\"entitiesList\":[]}},\"describedBy\":{\"characteristics\":[{\"ID\":\"ctemp\",\"name\":\"The Corporal Temperature\",\"definition\":\"Value of the axilar temperature in Celsius degree\",\"weight\":0.23,\"quantifiedBy\":{\"related\":[{\"IDmetric\":\"dm_ctemp\",\"name\":\"Value of Corporal Temperature\",\"version\":\"1.0\",\"IDAttribute\":\"ctemp\",\"scale\":{\"IDScale\":\"sca_temp\",\"name\":\"Environmental Temperature\\u0027s Scale\",\"scaleType\":\"INTERVAL\",\"expressedIn\":{\"units\":[{\"IDUnit\":\"u_temp\",\"name\":\"Celsius degreee\",\"symbol\":\"C\"}]}},\"sources\":{\"sources\":[{\"dataSourceID\":\"ds_temp\",\"name\":\"Corporal Temperature\\u0027s Sensor\",\"groups\":{\"groups\":[{\"traceGroupID\":\"TG1\",\"name\":\"Peter\\u0027s Galaxy S6\"}]},\"adapters\":{\"adapters\":[{\"dsAdapterID\":\"DSA_1\",\"name\":\"Samsung Galaxy S6\"}]}}]}}]},\"indicator\":{\"modeledBy\":{\"idEM\":\"elmo_corptemp\",\"name\":\"Corporal Temperature\\u0027s Elementary Model \",\"criteria\":{\"criteria\":[{\"idDecisionCriterion\":\"corptemp_normal\",\"name\":\"Corporal Temperature\",\"lowerThreshold\":36.0,\"upperThreshold\":37.1,\"notifiableUnderLowerThreshold\":true,\"nult_message\":\"Warning. The Corporal Temperature is Under 36 celsiud degree\",\"notifiableBetweenThreshold\":false,\"notifiableAboveUpperThreshold\":true,\"naut_message\":\"Warning. The Corporal Temperature is Above 37.1 celsius degree\"}]}},\"indicatorID\":\"ind_corpTemp\",\"name\":\"Level of the Corporal Temperature\",\"weight\":1}},{\"ID\":\"heartrate\",\"name\":\"The Heart Rate\",\"definition\":\"Number of beats per minute (bpm)\",\"weight\":0.27,\"quantifiedBy\":{\"related\":[{\"IDmetric\":\"dm_heart\",\"name\":\"Value of Heart Rate\",\"version\":\"1.0\",\"IDAttribute\":\"heartrate\",\"scale\":{\"IDScale\":\"sca_heart\",\"name\":\"Heart Rate\\u0027s Scale\",\"scaleType\":\"RATIO\",\"expressedIn\":{\"units\":[{\"IDUnit\":\"u_heart\",\"name\":\"Beats per minute\",\"symbol\":\"bpm\"}]}},\"sources\":{\"sources\":[{\"dataSourceID\":\"ds_heart\",\"name\":\"Heart Rate\\u0027s Sensor\",\"groups\":{\"groups\":[{\"traceGroupID\":\"TG1\",\"name\":\"Peter\\u0027s Galaxy S6\"}]},\"adapters\":{\"adapters\":[{\"dsAdapterID\":\"DSA_1\",\"name\":\"Samsung Galaxy S6\"}]}}]}}]},\"indicator\":{\"modeledBy\":{\"idEM\":\"elmo_hearttemp\",\"name\":\"Heart Ratee\\u0027s Elementary Model \",\"criteria\":{\"criteria\":[{\"idDecisionCriterion\":\"heartRate_normal\",\"name\":\"Heart Rate\",\"lowerThreshold\":62.0,\"upperThreshold\":75,\"notifiableUnderLowerThreshold\":true,\"nult_message\":\"Warning. The Heart Rate is under than 62 bpm\",\"notifiableBetweenThreshold\":false,\"notifiableAboveUpperThreshold\":true,\"naut_message\":\"Warning. The Heart Rate is upper than 75 bpm\"}]}},\"indicatorID\":\"ind_heartRate\",\"name\":\"Level of the Heart Rate\",\"weight\":1}}]},\"monitored\":{\"entitiesList\":[{\"ID\":\"Ent1\",\"name\":\"Outpatient A (Peter)\",\"relatedTo\":{\"entitiesList\":[]}}]}},\"describedBy\":{\"calculableConcepts\":[{\"ID\":\"calcon1\",\"name\":\"Health\",\"combines\":{\"characteristics\":[]},\"representedBy\":{\"representedList\":[{\"ID\":\"cmod\",\"name\":\"Outpatient Monitoring version 1.0\"}]},\"subconcepts\":{\"calculableConcepts\":[]}}]},\"characterizedBy\":{\"describedBy\":{\"contextProperties\":[{\"related\":{\"contextProperties\":[]},\"ID\":\"pc_humi\",\"name\":\"The Environmental Humidity\",\"definition\":\"Amount of the water vapor in the air\",\"weight\":0.2,\"quantifiedBy\":{\"related\":[{\"IDmetric\":\"dm_pc_humi\",\"name\":\"Value of Environmental Humidity\",\"version\":\"1.0\",\"IDAttribute\":\"pc_humi\",\"scale\":{\"IDScale\":\"sca_humi\",\"name\":\"Environmental Humidity\\u0027s Scale\",\"scaleType\":\"INTERVAL\",\"expressedIn\":{\"units\":[{\"IDUnit\":\"u_humi\",\"name\":\"Percentage\",\"symbol\":\"%\"}]}},\"sources\":{\"sources\":[{\"dataSourceID\":\"ds_env_humi\",\"name\":\"Environmental Humidity\\u0027s Sensor\",\"groups\":{\"groups\":[{\"traceGroupID\":\"TG1\",\"name\":\"Peter\\u0027s Galaxy S6\"}]},\"adapters\":{\"adapters\":[{\"dsAdapterID\":\"DSA_1\",\"name\":\"Samsung Galaxy S6\"}]}}]}}]},\"indicator\":{\"modeledBy\":{\"idEM\":\"elmo_humidity\",\"name\":\"Environmental Humidity\\u0027s Elementary Model \",\"criteria\":{\"criteria\":[{\"idDecisionCriterion\":\"humidity_low\",\"name\":\"Low Humidity\",\"lowerThreshold\":0,\"upperThreshold\":40.0,\"notifiableUnderLowerThreshold\":false,\"notifiableBetweenThreshold\":false,\"notifiableAboveUpperThreshold\":false},{\"idDecisionCriterion\":\"humidity_normal\",\"name\":\"Normal Humidity\",\"lowerThreshold\":40.01,\"upperThreshold\":60,\"notifiableUnderLowerThreshold\":false,\"notifiableBetweenThreshold\":false,\"notifiableAboveUpperThreshold\":true,\"naut_message\":\"The Environmental Humidity is upper than 60%\"},{\"idDecisionCriterion\":\"humidity_high\",\"name\":\"High Humidity\",\"lowerThreshold\":60.01,\"upperThreshold\":100,\"notifiableUnderLowerThreshold\":false,\"notifiableBetweenThreshold\":true,\"nbt_message\":\"The Environmental Humidity is High\",\"notifiableAboveUpperThreshold\":true,\"naut_message\":\"The Environmental Humidity is High\"}]}},\"indicatorID\":\"ind_env_humidity\",\"name\":\"Level of the Environmental Humidity\",\"weight\":0.34}},{\"related\":{\"contextProperties\":[]},\"ID\":\"pc_temp\",\"name\":\"The Environmental Temperature\",\"definition\":\"Value of the environmental temperature in Celsius degree\",\"weight\":0.15,\"quantifiedBy\":{\"related\":[{\"IDmetric\":\"dm_pc_temp\",\"name\":\"Value of Environmental Temperature\",\"version\":\"1.0\",\"IDAttribute\":\"pc_temp\",\"scale\":{\"IDScale\":\"sca_temp\",\"name\":\"Environmental Temperature\\u0027s Scale\",\"scaleType\":\"INTERVAL\",\"expressedIn\":{\"units\":[{\"IDUnit\":\"u_temp\",\"name\":\"Celsius degreee\",\"symbol\":\"C\"}]}},\"sources\":{\"sources\":[{\"dataSourceID\":\"ds_env_temp\",\"name\":\"Environmental Temperature\\u0027s Sensor\",\"groups\":{\"groups\":[{\"traceGroupID\":\"TG1\",\"name\":\"Peter\\u0027s Galaxy S6\"}]},\"adapters\":{\"adapters\":[{\"dsAdapterID\":\"DSA_1\",\"name\":\"Samsung Galaxy S6\"}]}}]}}]},\"indicator\":{\"modeledBy\":{\"idEM\":\"elmo_env_temp\",\"name\":\"Environmental Temperature\\u0027s Elementary Model \",\"criteria\":{\"criteria\":[{\"idDecisionCriterion\":\"temp_low\",\"name\":\"Low Temperature\",\"lowerThreshold\":10.0,\"upperThreshold\":18,\"notifiableUnderLowerThreshold\":true,\"nult_message\":\"The Environmental Temperature is under 10 celsius degree\",\"notifiableBetweenThreshold\":false,\"notifiableAboveUpperThreshold\":false},{\"idDecisionCriterion\":\"temp_normal\",\"name\":\"Normal Temperature\",\"lowerThreshold\":18.01,\"upperThreshold\":29,\"notifiableUnderLowerThreshold\":false,\"notifiableBetweenThreshold\":false,\"notifiableAboveUpperThreshold\":false},{\"idDecisionCriterion\":\"temp_high\",\"name\":\"High Temperature\",\"lowerThreshold\":29.01,\"upperThreshold\":36,\"notifiableUnderLowerThreshold\":false,\"notifiableBetweenThreshold\":true,\"nbt_message\":\"Warning. High Temperature\",\"notifiableAboveUpperThreshold\":true,\"naut_message\":\"Alert. Very High Temperature\"}]}},\"indicatorID\":\"ind_env_temp\",\"name\":\"Level of the Environmental Temperature\",\"weight\":0.33}},{\"related\":{\"contextProperties\":[]},\"ID\":\"pc_press\",\"name\":\"The Environmental Pressure\",\"definition\":\"Pressures resulting from human activities which bring about changes in the state of the environment\",\"weight\":0.15,\"quantifiedBy\":{\"related\":[{\"IDmetric\":\"dm_pc_press\",\"name\":\"Value of Environmental Pressure\",\"version\":\"1.0\",\"IDAttribute\":\"pc_press\",\"scale\":{\"IDScale\":\"sca_press\",\"name\":\"Environmental Pressure\\u0027s Scale\",\"scaleType\":\"RATIO\",\"expressedIn\":{\"units\":[{\"IDUnit\":\"u_press\",\"name\":\"Hectopascals\",\"symbol\":\"hPa\"}]}},\"sources\":{\"sources\":[{\"dataSourceID\":\"ds_env_press\",\"name\":\"Environmental Pressure\\u0027s Sensor\",\"groups\":{\"groups\":[{\"traceGroupID\":\"TG1\",\"name\":\"Peter\\u0027s Galaxy S6\"}]},\"adapters\":{\"adapters\":[{\"dsAdapterID\":\"DSA_1\",\"name\":\"Samsung Galaxy S6\"}]}}]}}]},\"indicator\":{\"modeledBy\":{\"idEM\":\"elmo_env_press\",\"name\":\"Environmental Pressure\\u0027s Elementary Model \",\"criteria\":{\"criteria\":[{\"idDecisionCriterion\":\"press_normal\",\"name\":\"Normal Enviromental Pressure\",\"lowerThreshold\":900.0,\"upperThreshold\":1100,\"notifiableUnderLowerThreshold\":false,\"notifiableBetweenThreshold\":false,\"notifiableAboveUpperThreshold\":true}]}},\"indicatorID\":\"ind_env_press\",\"name\":\"Level of the Environmental Pressure\",\"weight\":0.33}}]},\"ID\":\"ctx_outpatient\",\"name\":\"The Outpatient Context\",\"relatedTo\":{\"entitiesList\":[]}}},\"lastChange\":\"2020-04-22T14:12:38.204-03:00[America/Argentina/Salta]\"}]}}";
        CINCAMIPD def=(CINCAMIPD)TranslateJSON.toObject(CINCAMIPD.class, json);
        ArrayList<String> ret=CINCAMIPD.toDetectorInitializationList(def, "PRJ_1");
        
        mainDetectors myObserver=new mainDetectors();
        
        SPCProjectDetector det=SPCProjectDetector.create(ret, "PRJ_1", 5.0, false, myObserver);
        Random r=new Random();
        int i=0;
        int minutes=2;
        long start=System.nanoTime();
        long measures=0;
        while((System.nanoTime()-start)<=(minutes*60000000000L))
        {
            
            String str=ret.get(i%(ret.size()));
            String arr[]=str.split(";");
            
            Double val=((double)i%ret.size())+2*r.nextGaussian();//Gaussian Mean: i%ret.size() Variance: 2           
            
            det.addMeasure(arr[1], val);   
            i++;
            
            if(i>(ret.size()-1))
            {
                i=0;
                measures++;
                System.out.println("["+ZonedDateTime.now()+"] Measures/filter: "+measures);
            }
            
            Thread.sleep(100);            
        }

        System.out.println(det.toString());        
        System.out.println("Required Size: "+ia.sizeDeepOf(det)+" bytes");
        
        det.turnFilters(false, true);    

        System.out.println("Total processed measures: "+measures); */       
    }

    public static void sim1_SizePerMetrics(InstrumentationAgent ia, int minMetric, int maxMetric) throws DetectorException, InterruptedException, SPCException
    {
        System.out.println("Analyzing creation times and total consumed sizes for the Change Detector...");
        
        //Generating measures
        Random r=new Random();
        ArrayList<Double> vals=new ArrayList(11);
        
        for(int i=0;i<11;i++)
            vals.add(6.0+r.nextGaussian()*2.0);
        
        StringBuilder sb=new StringBuilder();
        System.out.println("numberMetrics;creationTime;sizeBeforeProcessing;sizeAfterProcessing");
        for(int i=minMetric;i<=maxMetric;i++)
        {
            sb.delete(0, sb.length());
            
            ArrayList defmet=genAttMetricList(i);
            
            long start=System.nanoTime();
            SPCProjectDetector det=SPCProjectDetector.create(defmet, "PRJ_1",5.0, false,11,2,1000);//No verbose, M;TRIGGER;SS
            long end=System.nanoTime();
            
            long sizeBeforeProcessing=ia.sizeDeepOf(det);
            
            //Provide at least 11 measures to each filter
            Enumeration<String> keys=det.getMetrics();
            while(keys!=null && keys.hasMoreElements())
            {
                String key=keys.nextElement();
                det.addMeasures(key, vals);
            }
            
            long sizeAfterProcessing=ia.sizeDeepOf(det);
            
            sb.append(i).append(";")
              .append((end-start)).append(";")
              .append(sizeBeforeProcessing).append(";")
              .append(sizeAfterProcessing);
            System.out.println(sb);
            
            det.turnFilters(false, true);//Shutting down the current threads
            Thread.sleep(1000);
        }
    }
    
    public static void sim2_evaluatingAlarms(InstrumentationAgent ia, int minutes, double mean,double ndev) throws DetectorException, SPCException, InterruptedException
    {
        if(minutes<1) return;
        System.out.println("Analyzing raised alarms for "+minutes+" minutes "
                + "for each combination of (number of metrics  [10;100] and trigger [2; 3])...");
                
        Random r=new Random();
        StringBuilder sb=new StringBuilder();
        
        System.out.println("Starting "+ZonedDateTime.now()+" Cycle: "+minutes+" minutes");
        System.out.println("timestamp;nMetrics;trigger;measurespermetric;totalmeasures;alarms");
        for(int nm=10;nm<=30;nm++)
        {
            ArrayList defmet=genAttMetricList(nm);
            
            for(int trigger=2;trigger<=3;trigger++)
            {
                mainDetectors observer=new mainDetectors();
                
                SPCProjectDetector det=SPCProjectDetector.create(defmet, "PRJ_1",5.0, false,observer,11,trigger,25);//No verbose
                long startsim=System.nanoTime();
                long measures=0;
                while((System.nanoTime()-startsim)<=(minutes*60000000000L))
                {
                    //Add one measure per metric
                    Enumeration<String> keys=det.getMetrics();
                    while(keys!=null && keys.hasMoreElements())
                    {
                        String key=keys.nextElement();
                        det.addMeasure(key, (mean+ndev*r.nextGaussian()));                        
                    }
                    measures++;//measures per metric
                    
                    Thread.sleep(100);
                }
                
                det.turnFilters(false, true);//Shutdown the threads
                long nalarms=observer.alarms;
                System.out.println(ZonedDateTime.now()+";"+nm+";"+trigger+";"+(measures)+";"+(measures*nm)+";"+nalarms);
            }
            
            defmet.clear();
        }
    }

    public static void sim3_opTimes(int minutes) throws DetectorException, SPCException, InterruptedException
    {
        if(minutes<1) return;
        System.out.println("Analyzing operation times for "+minutes+" minutes... ");
                
        Random r=new Random();
        StringBuilder sb=new StringBuilder();
        double mean,ndev;
        mean=7.0;
        ndev=2.0;
        
        System.out.println("Starting "+ZonedDateTime.now()+" Cycle: "+minutes+" minutes");

            
        SPCBasedFilter filter =SPCBasedFilter.create();
        filter.initializeMeasurementRecord();
        
        ExecutorService pool = Executors.newFixedThreadPool(1);
        pool.execute(filter);

        long startsim=System.nanoTime();

        while((System.nanoTime()-startsim)<=(minutes*60000000000L))
        {
            filter.addMeasure((mean+ndev*r.nextGaussian()));                        

            Thread.sleep(100);
        }

        System.out.println("timestamp;nanoTime;computeTime");        
        SPCFilterMeasurementRecord record=filter.getRecord();
        record.getCompute().stream().forEach(p->{
            System.out.println(p.getTimestamp()+";"+p.getNanoTime()+";"+p.getConsumedOperationTime());
        });
                        
        System.out.println("timestamp;nanoTime;addMeasureTime");        
        record.getAddMeasure().stream().forEach(p->{
            System.out.println(p.getTimestamp()+";"+p.getNanoTime()+";"+p.getConsumedOperationTime());
        });
        
        filter.setEnabled(false);    
        pool.shutdown();
    }

    public static void sim5_BufferSizes(InstrumentationAgent ia, int minutes) throws DetectorException, SPCException, InterruptedException, BufferException
    {
        System.out.println("Analyzing the buffer sizes for "+minutes+" minutes ");
                
        Random r=new Random();
        StringBuilder sb=new StringBuilder();
        
        System.out.println("Starting "+ZonedDateTime.now()+" Cycle: "+minutes+" minutes");
        System.out.println("nanoTime;measurepermetric;nmetric;size");
        double mean=9;
        double ndev=2.0;
        for(int nm=10;nm<=10;nm++)
        {
            ArrayList defmet=genAttMetricList(nm);
            for(Object s:defmet)
            {
                System.out.println((s==null)?"null":s);
            }
            
            for(int trigger=2;trigger<=2;trigger++)
            {
                SPCBasedBuffer buffer=SPCBasedBuffer.create("PRJ_1", defmet, 1000, 5.0,false);//No verbose
                
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
                    System.out.println(System.nanoTime()+";"+measures+";"+nm+";"+size);
                    
                    Thread.sleep(100);
                }

                System.out.print("Timestamp\t");
                for(String s:buffer.getMetricIDs())
                    System.out.print(s+"\t");    
                System.out.println();
                
                for(Object o:buffer.getBufferData_and_clean())
                {
                    SPCBasedBufferNode node=(SPCBasedBufferNode)o;
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
    
    public static void sim6_BufferSizesandClock(InstrumentationAgent ia, int minutes) throws DetectorException, SPCException, InterruptedException, BufferException
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
                mainDetectors trx=new mainDetectors();
                trx.setKsim(6);
                trx.setIa(ia);
                SPCBasedBuffer buffer=SPCBasedBuffer.create("PRJ_1", defmet, 1000, 5.0,false);//verbose
                buffer.addObserver(trx);
                buffer.activateClock(1000*15);//15 seconds
                
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
