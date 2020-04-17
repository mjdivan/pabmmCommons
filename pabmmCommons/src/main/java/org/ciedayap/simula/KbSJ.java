/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ciedayap.simula;

import java.math.BigDecimal;
import java.security.NoSuchAlgorithmException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.ciedayap.cincamimis.Cincamimis;
import org.ciedayap.cincamimis.Context;
import org.ciedayap.cincamimis.LikelihoodDistribution;
import org.ciedayap.cincamimis.LikelihoodDistributionException;
import org.ciedayap.cincamimis.MeasurementItem;
import org.ciedayap.cincamimis.MeasurementItemSet;
import org.ciedayap.mair.BDTreeException;
import org.ciedayap.mair.GlobalIntegrityRecord;
import org.ciedayap.mair.IntegrityRecordException;
import org.ciedayap.mair.MAIntegrityRecord;
import org.ciedayap.mair.TreeNodeException;
import org.ciedayap.mair.utils.InstrumentationAgent;
import org.ciedayap.pabmm.pd.Synthesizer;
import org.ciedayap.pabmm.pd.SynthesizerException;
import org.ciedayap.utils.TranslateJSON;
import org.ciedayap.utils.TranslateXML;
import org.ciedayap.utils.ZipUtil;

/**
 *
 * @author mjdivan
 */
public class KbSJ {
    
    
    public static void main(String args[]) throws SynthesizerException, NoSuchAlgorithmException, LikelihoodDistributionException, Exception
    {
        InstrumentationAgent ia=new InstrumentationAgent();
        String jsonpd="{\"IDMessage\":\"1\",\"version\":\"1.0\",\"creation\":\"2020-03-26T07:50:57.05-03:00[America/Argentina/Salta]\",\"projects\":{\"projects\":[{\"ID\":\"PRJ_1\",\"name\":\"Outpatient Monitoring\",\"startDate\":\"2020-03-26T07:50:57.046-03:00[America/Argentina/Salta]\",\"infneed\":{\"ID\":\"IN_1\",\"purpose\":\"Avoid severe damages through the prevention of risks with direct impact in the outpatient health\",\"shortTitle\":\"Monitor the Outpatient\",\"specifiedEC\":{\"ID\":\"EC1\",\"name\":\"Outpatient\",\"superCategory\":{\"describedBy\":{\"characteristics\":[]},\"monitored\":{\"entitiesList\":[]}},\"describedBy\":{\"characteristics\":[{\"ID\":\"ctemp\",\"name\":\"The Corporal Temperature\",\"definition\":\"Value of the axilar temperature in Celsius degree\",\"quantifiedBy\":{\"related\":[{\"IDmetric\":\"dm_ctemp\",\"name\":\"Value of Corporal Temperature\",\"version\":\"1.0\",\"IDAttribute\":\"ctemp\",\"scale\":{\"IDScale\":\"sca_temp\",\"name\":\"Environmental Temperature\\u0027s Scale\",\"scaleType\":\"INTERVAL\",\"expressedIn\":{\"units\":[{\"IDUnit\":\"u_temp\",\"name\":\"Celsius degreee\",\"symbol\":\"C\"}]}},\"sources\":{\"sources\":[{\"dataSourceID\":\"ds_temp\",\"name\":\"Corporal Temperature\\u0027s Sensor\",\"groups\":{\"groups\":[{\"traceGroupID\":\"TG1\",\"name\":\"Peter\\u0027s Galaxy S6\"}]},\"adapters\":{\"adapters\":[{\"dsAdapterID\":\"DSA_1\",\"name\":\"Samsung Galaxy S6\"}]}}]}}]},\"indicator\":{\"modeledBy\":{\"idEM\":\"elmo_corptemp\",\"name\":\"Corporal Temperature\\u0027s Elementary Model \",\"criteria\":{\"criteria\":[{\"idDecisionCriterion\":\"corptemp_normal\",\"name\":\"Corporal Temperature\",\"lowerThreshold\":36.0,\"upperThreshold\":37.1,\"notifiableUnderLowerThreshold\":true,\"nult_message\":\"Warning. The Corporal Temperature is Under 36 celsiud degree\",\"notifiableBetweenThreshold\":false,\"notifiableAboveUpperThreshold\":true,\"naut_message\":\"Warning. The Corporal Temperature is Above 37.1 celsius degree\"}]}},\"indicatorID\":\"ind_corpTemp\",\"name\":\"Level of the Corporal Temperature\",\"weight\":1}},{\"ID\":\"heartrate\",\"name\":\"The Heart Rate\",\"definition\":\"Number of beats per minute (bpm)\",\"quantifiedBy\":{\"related\":[{\"IDmetric\":\"dm_heart\",\"name\":\"Value of Heart Rate\",\"version\":\"1.0\",\"IDAttribute\":\"heartrate\",\"scale\":{\"IDScale\":\"sca_heart\",\"name\":\"Heart Rate\\u0027s Scale\",\"scaleType\":\"RATIO\",\"expressedIn\":{\"units\":[{\"IDUnit\":\"u_heart\",\"name\":\"Beats per minute\",\"symbol\":\"bpm\"}]}},\"sources\":{\"sources\":[{\"dataSourceID\":\"ds_heart\",\"name\":\"Heart Rate\\u0027s Sensor\",\"groups\":{\"groups\":[{\"traceGroupID\":\"TG1\",\"name\":\"Peter\\u0027s Galaxy S6\"}]},\"adapters\":{\"adapters\":[{\"dsAdapterID\":\"DSA_1\",\"name\":\"Samsung Galaxy S6\"}]}}]}}]},\"indicator\":{\"modeledBy\":{\"idEM\":\"elmo_hearttemp\",\"name\":\"Heart Ratee\\u0027s Elementary Model \",\"criteria\":{\"criteria\":[{\"idDecisionCriterion\":\"heartRate_normal\",\"name\":\"Heart Rate\",\"lowerThreshold\":62.0,\"upperThreshold\":75,\"notifiableUnderLowerThreshold\":true,\"nult_message\":\"Warning. The Heart Rate is under than 62 bpm\",\"notifiableBetweenThreshold\":false,\"notifiableAboveUpperThreshold\":true,\"naut_message\":\"Warning. The Heart Rate is upper than 75 bpm\"}]}},\"indicatorID\":\"ind_heartRate\",\"name\":\"Level of the Heart Rate\",\"weight\":1}}]},\"monitored\":{\"entitiesList\":[{\"ID\":\"Ent1\",\"name\":\"Outpatient A (Peter)\",\"relatedTo\":{\"entitiesList\":[]}}]}},\"describedBy\":{\"calculableConcepts\":[{\"ID\":\"calcon1\",\"name\":\"Health\",\"combines\":{\"characteristics\":[]},\"representedBy\":{\"representedList\":[{\"ID\":\"cmod\",\"name\":\"Outpatient Monitoring version 1.0\"}]},\"subconcepts\":{\"calculableConcepts\":[]}}]},\"characterizedBy\":{\"describedBy\":{\"contextProperties\":[{\"related\":{\"contextProperties\":[]},\"ID\":\"pc_humi\",\"name\":\"The Environmental Humidity\",\"definition\":\"Amount of the water vapor in the air\",\"quantifiedBy\":{\"related\":[{\"IDmetric\":\"dm_pc_humi\",\"name\":\"Value of Environmental Humidity\",\"version\":\"1.0\",\"IDAttribute\":\"pc_humi\",\"scale\":{\"IDScale\":\"sca_humi\",\"name\":\"Environmental Humidity\\u0027s Scale\",\"scaleType\":\"INTERVAL\",\"expressedIn\":{\"units\":[{\"IDUnit\":\"u_humi\",\"name\":\"Percentage\",\"symbol\":\"%\"}]}},\"sources\":{\"sources\":[{\"dataSourceID\":\"ds_env_humi\",\"name\":\"Environmental Humidity\\u0027s Sensor\",\"groups\":{\"groups\":[{\"traceGroupID\":\"TG1\",\"name\":\"Peter\\u0027s Galaxy S6\"}]},\"adapters\":{\"adapters\":[{\"dsAdapterID\":\"DSA_1\",\"name\":\"Samsung Galaxy S6\"}]}}]}}]},\"indicator\":{\"modeledBy\":{\"idEM\":\"elmo_humidity\",\"name\":\"Environmental Humidity\\u0027s Elementary Model \",\"criteria\":{\"criteria\":[{\"idDecisionCriterion\":\"humidity_low\",\"name\":\"Low Humidity\",\"lowerThreshold\":0,\"upperThreshold\":40.0,\"notifiableUnderLowerThreshold\":false,\"notifiableBetweenThreshold\":false,\"notifiableAboveUpperThreshold\":false},{\"idDecisionCriterion\":\"humidity_normal\",\"name\":\"Normal Humidity\",\"lowerThreshold\":40.01,\"upperThreshold\":60,\"notifiableUnderLowerThreshold\":false,\"notifiableBetweenThreshold\":false,\"notifiableAboveUpperThreshold\":true,\"naut_message\":\"The Environmental Humidity is upper than 60%\"},{\"idDecisionCriterion\":\"humidity_high\",\"name\":\"High Humidity\",\"lowerThreshold\":60.01,\"upperThreshold\":100,\"notifiableUnderLowerThreshold\":false,\"notifiableBetweenThreshold\":true,\"nbt_message\":\"The Environmental Humidity is High\",\"notifiableAboveUpperThreshold\":true,\"naut_message\":\"The Environmental Humidity is High\"}]}},\"indicatorID\":\"ind_env_humidity\",\"name\":\"Level of the Environmental Humidity\",\"weight\":0.34}},{\"related\":{\"contextProperties\":[]},\"ID\":\"pc_temp\",\"name\":\"The Environmental Temperature\",\"definition\":\"Value of the environmental temperature in Celsius degree\",\"quantifiedBy\":{\"related\":[{\"IDmetric\":\"dm_pc_temp\",\"name\":\"Value of Environmental Temperature\",\"version\":\"1.0\",\"IDAttribute\":\"pc_temp\",\"scale\":{\"IDScale\":\"sca_temp\",\"name\":\"Environmental Temperature\\u0027s Scale\",\"scaleType\":\"INTERVAL\",\"expressedIn\":{\"units\":[{\"IDUnit\":\"u_temp\",\"name\":\"Celsius degreee\",\"symbol\":\"C\"}]}},\"sources\":{\"sources\":[{\"dataSourceID\":\"ds_env_temp\",\"name\":\"Environmental Temperature\\u0027s Sensor\",\"groups\":{\"groups\":[{\"traceGroupID\":\"TG1\",\"name\":\"Peter\\u0027s Galaxy S6\"}]},\"adapters\":{\"adapters\":[{\"dsAdapterID\":\"DSA_1\",\"name\":\"Samsung Galaxy S6\"}]}}]}}]},\"indicator\":{\"modeledBy\":{\"idEM\":\"elmo_env_temp\",\"name\":\"Environmental Temperature\\u0027s Elementary Model \",\"criteria\":{\"criteria\":[{\"idDecisionCriterion\":\"temp_low\",\"name\":\"Low Temperature\",\"lowerThreshold\":10.0,\"upperThreshold\":18,\"notifiableUnderLowerThreshold\":true,\"nult_message\":\"The Environmental Temperature is under 10 celsius degree\",\"notifiableBetweenThreshold\":false,\"notifiableAboveUpperThreshold\":false},{\"idDecisionCriterion\":\"temp_normal\",\"name\":\"Normal Temperature\",\"lowerThreshold\":18.01,\"upperThreshold\":29,\"notifiableUnderLowerThreshold\":false,\"notifiableBetweenThreshold\":false,\"notifiableAboveUpperThreshold\":false},{\"idDecisionCriterion\":\"temp_high\",\"name\":\"High Temperature\",\"lowerThreshold\":29.01,\"upperThreshold\":36,\"notifiableUnderLowerThreshold\":false,\"notifiableBetweenThreshold\":true,\"nbt_message\":\"Warning. High Temperature\",\"notifiableAboveUpperThreshold\":true,\"naut_message\":\"Alert. Very High Temperature\"}]}},\"indicatorID\":\"ind_env_temp\",\"name\":\"Level of the Environmental Temperature\",\"weight\":0.33}},{\"related\":{\"contextProperties\":[]},\"ID\":\"pc_press\",\"name\":\"The Environmental Pressure\",\"definition\":\"Pressures resulting from human activities which bring about changes in the state of the environment\",\"quantifiedBy\":{\"related\":[{\"IDmetric\":\"dm_pc_press\",\"name\":\"Value of Environmental Pressure\",\"version\":\"1.0\",\"IDAttribute\":\"pc_press\",\"scale\":{\"IDScale\":\"sca_press\",\"name\":\"Environmental Pressure\\u0027s Scale\",\"scaleType\":\"RATIO\",\"expressedIn\":{\"units\":[{\"IDUnit\":\"u_press\",\"name\":\"Hectopascals\",\"symbol\":\"hPa\"}]}},\"sources\":{\"sources\":[{\"dataSourceID\":\"ds_env_press\",\"name\":\"Environmental Pressure\\u0027s Sensor\",\"groups\":{\"groups\":[{\"traceGroupID\":\"TG1\",\"name\":\"Peter\\u0027s Galaxy S6\"}]},\"adapters\":{\"adapters\":[{\"dsAdapterID\":\"DSA_1\",\"name\":\"Samsung Galaxy S6\"}]}}]}}]},\"indicator\":{\"modeledBy\":{\"idEM\":\"elmo_env_press\",\"name\":\"Environmental Pressure\\u0027s Elementary Model \",\"criteria\":{\"criteria\":[{\"idDecisionCriterion\":\"press_normal\",\"name\":\"Normal Enviromental Pressure\",\"lowerThreshold\":900.0,\"upperThreshold\":1100,\"notifiableUnderLowerThreshold\":false,\"notifiableBetweenThreshold\":false,\"notifiableAboveUpperThreshold\":true}]}},\"indicatorID\":\"ind_env_press\",\"name\":\"Level of the Environmental Pressure\",\"weight\":0.33}}]},\"ID\":\"ctx_outpatient\",\"name\":\"The Outpatient Context\",\"relatedTo\":{\"entitiesList\":[]}}},\"lastChange\":\"2020-03-26T07:50:57.046-03:00[America/Argentina/Salta]\"}]}}";
        Synthesizer metadata=new Synthesizer(jsonpd,"PRJ_1");
        
        //XML JSON Brief 
        /**
         * A) When the number of measures varies B) Durring 5 minutes with 500 measures per message
         *  1. Generation Time from the object model  (Translate) with Synthesizer
         *  2. Compression Time and Size (XML, JSON, BRIEF)
         *  3. Decompresion Time
         *  4. Regeneration Time
         */
        System.out.println("Choose one simulation to perform:");
        System.out.println("\t1. [BRIEF] Evolution of measures per message");
        System.out.println("\t2. [BRIEF] Operation's Individual Time (during 20 minutes)");
        System.out.println("\t3. [BDTree] Creation Process");
        System.out.println("\t4. [BDTree] Operation's Individual Time (during 20 minutes)");
        Scanner keyboard = new Scanner(System.in);
        System.out.println("Choose your option [1-4]: ");
        int myint = keyboard.nextInt();
        switch(myint)
        {
            case 1:
                sim_brief_EvolMeasuresPerMessage(100,5000,100,metadata,ia);
                break;
            case 2:
                sim_brief_EvolTime(200,20,metadata,ia);    
                break;
            case 3:
                ArrayList<Integer> prjList=new ArrayList();
                for(int i=0;i<50;i++) prjList.add(i);
                
                Task tasks[]=new Task[10];
                for(int i=0;i<10;i++) tasks[i]=new Task(prjList,(short)3,(short)200,(short)10,ia);
                
                ExecutorService pool = Executors.newFixedThreadPool(7);
                for(int i=0;i<10;i++) pool.execute(tasks[i]);
                
                pool.shutdown();
                break;
            case 4:
                sim_mt_operation(20,ia);
                break;
            default:
                System.out.println("Wrong choice!");                
        }
        //
        //
        //
        //
        //Integrity
        /**
         * 5. BDTree Generation Time and Size (Varying number of projects, ma,  and #transactions)
         * 6. During five minutes, verify the individual time of operations
         */
    }

    public static void sim_mt_creationByProject(int puntualProj,int max_nma,int max_power,InstrumentationAgent ia) throws BDTreeException, IntegrityRecordException, NoSuchAlgorithmException, TreeNodeException
    {
        StringBuilder sb;
        for(int nma=1; nma<=max_nma;nma++)
        {
            for(int power=3;power<=max_power;power++)
            {
                System.out.println(mt_creation(puntualProj,nma,power,ia,false));
            }
        }
    }
    
    public static void sim_mt_creation(int max_nproj,int max_nma,int max_power,InstrumentationAgent ia) throws BDTreeException, IntegrityRecordException, NoSuchAlgorithmException, TreeNodeException
    {
        StringBuilder sb;
        boolean header=true;
        for(int nproj=1;nproj<=max_nproj;nproj++)
        {
            for(int nma=1; nma<=max_nma;nma++)
            {
                for(int power=3;power<=max_power;power++)
                {
                    System.out.println(mt_creation(nproj,nma,power,ia,header));
                    header=false;
                }
            }
        }
    }
    
    public static void sim_mt_operation(int minutes,InstrumentationAgent ia) throws BDTreeException, IntegrityRecordException, NoSuchAlgorithmException, TreeNodeException, InterruptedException
    {
     GlobalIntegrityRecord gir=new GlobalIntegrityRecord(4,1);
     
     for(int i=1;i<=16;i++)
     {         
         gir.addTransaction("prjID_1", "maID_1", MAIntegrityRecord.ROLE_DATA_COLLECTOR, String.valueOf(i));
     }
     
     boolean header=true;
     long start=System.nanoTime();
     long trx=1L;
     while((System.nanoTime()-start)<=(minutes*60000000000L))
     {               
        System.out.println(mt_operation(gir,Long.toString(trx),ia,header));
        trx++;
        header=false;                
        
        Thread.sleep(1000);
     }          
     
    }
    
    public static String mt_operation(GlobalIntegrityRecord gir,String trx,InstrumentationAgent ia, boolean showHeader) throws BDTreeException, IntegrityRecordException, NoSuchAlgorithmException, TreeNodeException
    {
        long start=System.nanoTime();
        gir.addTransaction("prjID_1", "maID_1", MAIntegrityRecord.ROLE_DATA_COLLECTOR, trx);
        long end=System.nanoTime();
        long addTransaction=end-start;
                
        start=System.nanoTime();
        gir.hasWholeIntegrity("prjID_1", "maID_1", "testValue");
        end=System.nanoTime();
        long hasWholeIntegrity=end-start;
        
        start=System.nanoTime();
        gir.verifyIntegrityFirsts("prjID_1", "maID_1", "testValue", 2);//Verify the first 2^2 transactions  (4)
        end=System.nanoTime();
        long verifyIntegrityFirsts=end-start;
        
        start=System.nanoTime();
        gir.verifyIntegrityLasts("prjID_1", "maID_1", "testValue", 2);//Verify the last 2^2 transactions  (4)
        end=System.nanoTime();
        long verifyIntegrityLasts=end-start;

        start=System.nanoTime();
        gir.verifyTransactionIntegrity("prjID_1", "maID_1", "testValue", 4);//It verifies the integrity of the transaction 4th 
        end=System.nanoTime();
        long verifyTransactionIntegrity=end-start;
        
        StringBuilder  sb=new StringBuilder();
        if(showHeader)
        {
            sb.append("nanoTime").append(";")
              .append("addTransaction").append(";")
              .append("hasWholeIntegrity").append(";")
              .append("verifyIntegrityFirsts").append(";")
              .append("verifyIntegrityLasts").append(";")
              .append("verifyTransactionIntegrity").append("\n");
        }

        sb.append(System.nanoTime()).append(";")
          .append(addTransaction).append(";")
          .append(hasWholeIntegrity).append(";")
          .append(verifyIntegrityFirsts).append(";")
          .append(verifyIntegrityLasts).append(";")
          .append(verifyTransactionIntegrity);

        return sb.toString();
    }
    
    public static String mt_creation(int nproj,int nma,int power,InstrumentationAgent ia,boolean showHeader) throws BDTreeException, IntegrityRecordException, NoSuchAlgorithmException, TreeNodeException
    {
     long start=System.nanoTime();
     GlobalIntegrityRecord gir=new GlobalIntegrityRecord(power,nma);
     
     for(int  prj=1;prj<=nproj;prj++)
     {
         for(int ma=1;ma<=nma;ma++)
         {
             if(!gir.addTransaction(String.valueOf(prj), String.valueOf(ma), MAIntegrityRecord.ROLE_DATA_COLLECTOR, "hash"))
                 throw new BDTreeException("Prj: "+String.valueOf(prj)+" MA: "+String.valueOf(ma)+" does not added");
         }
     }     
     long end=System.nanoTime();//All the Trees were initialized
     long startup_time=end-start;
     
     long startup_size=ia.sizeDeepOf(gir);
     
     StringBuilder sb=new StringBuilder();
     if(showHeader)
     {
         sb.append("nanoTime").append(";")
           .append("nproj").append(";")
           .append("nma").append(";")
           .append("power").append(";")
           .append("startup_time").append(";")
           .append("startup_size").append("\n");
     }

     sb.append(System.nanoTime()).append(";")
       .append(nproj).append(";")
       .append(nma).append(";")
       .append(power).append(";")
       .append(startup_time).append(";")
       .append(startup_size);
     
     return sb.toString();     
    }
    
    public static void sim_brief_EvolMeasuresPerMessage(int from, int threshold,int jump, Synthesizer metadata, InstrumentationAgent ia) throws LikelihoodDistributionException, NoSuchAlgorithmException, Exception
    {
        //The first for regulates the volume of measures per message
        boolean header=true;        
        for(int i=from; i<=threshold;i+=jump)
        {
            ArrayList<Cincamimis> list=generateMessagesList(metadata,1,i, KbSJ.DETERMINISTIC);
            for(Cincamimis mis:list)
            {
                System.out.println(generationTimeandBytes_dataFormats(metadata,ia,mis,header));
                header=false;
            }            
        }
    }

    public static void sim_brief_EvolTime(int nmeasures, int minutes, Synthesizer metadata, InstrumentationAgent ia) throws LikelihoodDistributionException, NoSuchAlgorithmException, Exception
    {
        long start=System.nanoTime();
        boolean header=true;
        Cincamimis mis=generateMessage(metadata,nmeasures,KbSJ.DETERMINISTIC);        
        if(mis==null) return;
        
        while((System.nanoTime()-start)<=(minutes*60000000000L))
        {               
            System.out.println(generationTimeandBytes_dataFormats(metadata,ia,mis,header));
            header=false;                

        }
    }
    
    public static String generationTimeandBytes_dataFormats(Synthesizer metadata,InstrumentationAgent ia,Cincamimis dwindow, boolean showHeader) throws NoSuchAlgorithmException, Exception
    {
        if(dwindow==null || ia==null  || metadata==null) return null;
        
        //Message Generation
        long start=System.nanoTime();
        String briefcontent=dwindow.measureToText();
        String briefmessage=metadata.writeBrief(briefcontent);
        long end=System.nanoTime();
        long gen_brief=end-start;
        long gen_brief_length=briefmessage.length();
        
        start=System.nanoTime();
        String xml=TranslateXML.toXml(dwindow);
        end=System.nanoTime();
        long gen_xml=end-start;
        long gen_xml_length=xml.length();
        
        start=System.nanoTime();
        String json=TranslateJSON.toJSON(dwindow);
        end=System.nanoTime();
        long gen_json=end-start;
        long gen_json_length=json.length();
        
        long gen_brief_normalSize=ia.sizeDeepOf(briefmessage);
        long gen_xml_normalSize=ia.sizeDeepOf(xml);
        long gen_json_normalSize=ia.sizeDeepOf(json);
        
        //Compression
        start=System.nanoTime();
        byte[] compGZIP_brief=ZipUtil.compressGZIP(briefmessage);
        end=System.nanoTime();
        long gen_brief_compressed_time=end-start;
        long gen_brief_compressed_size=ia.sizeDeepOf(compGZIP_brief);
                
        start=System.nanoTime();
        byte[] compGZIP_xml=ZipUtil.compressGZIP(xml);
        end=System.nanoTime();
        long gen_xml_compressed_time=end-start;
        long gen_xml_compressed_size=ia.sizeDeepOf(compGZIP_xml);
        
        start=System.nanoTime();
        byte[] compGZIP_json=ZipUtil.compressGZIP(json);
        end=System.nanoTime();
        long gen_json_compressed_time=end-start;
        long gen_json_compressed_size=ia.sizeDeepOf(compGZIP_json);
        
        //Decompression
        start=System.nanoTime();
        String briefmessage_d=ZipUtil.decompressGZIP(compGZIP_brief);
        end=System.nanoTime();
        long gen_brief_decompressed_time=end-start;
        //if(briefmessage_d.equalsIgnoreCase(briefmessage)) System.out.println("[Brief ] Compress/Decompress OK");
        //else System.out.println("[Brief ] Compress/Decompress OK");
                        
        start=System.nanoTime();
        String xml_d=ZipUtil.decompressGZIP(compGZIP_xml);
        end=System.nanoTime();
        long gen_xml_decompressed_time=end-start;        
        //if(xml_d.equalsIgnoreCase(xml)) System.out.println("[XML] Compress/Decompress OK");
        //else System.out.println("[XML] Compress/Decompress OK");
                
        start=System.nanoTime();
        String json_d=ZipUtil.decompressGZIP(compGZIP_json);
        end=System.nanoTime();
        long gen_json_decompressed_time=end-start;
        //if(json_d.equalsIgnoreCase(json)) System.out.println("[JSON] Compress/Decompress OK");
        //else System.out.println("[JSON] Compress/Decompress OK");

        
        //Object Model Regeneration
        start=System.nanoTime();
        Cincamimis brief_o=Cincamimis.fromText(metadata.readBrief(briefmessage_d));
        end=System.nanoTime();
        long gen_brief_regeneration_time=end-start;
        
        start=System.nanoTime();
        Cincamimis xml_o=(Cincamimis) TranslateXML.toObject(Cincamimis.class, xml_d);
        end=System.nanoTime();
        long gen_xml_regeneration_time=end-start;
        
        start=System.nanoTime();
        Cincamimis json_o=(Cincamimis) TranslateJSON.toObject(Cincamimis.class, json_d);
        end=System.nanoTime();
        long gen_json_regeneration_time=end-start;
        
        StringBuilder sb=new StringBuilder();
        if(showHeader)
        {
            sb.append("nanoTime").append(";")
              .append("nmeasures").append(";")
              .append("gen_brief").append(";")
              .append("gen_brief_length").append(";")
              .append("gen_xml").append(";")
              .append("gen_xml_length").append(";")
              .append("gen_json").append(";")
              .append("gen_json_length").append(";")
              .append("gen_brief_normalSize").append(";")
              .append("gen_xml_normalSize").append(";")
              .append("gen_json_normalSize").append(";")
              .append("gen_brief_compressed_time").append(";")
              .append("gen_brief_compressed_size").append(";")
              .append("gen_xml_compressed_time").append(";")
              .append("gen_xml_compressed_size").append(";")
              .append("gen_json_compressed_time").append(";")
              .append("gen_json_compressed_size").append(";")
              .append("gen_brief_decompressed_time").append(";")
              .append("gen_xml_decompressed_time").append(";")
              .append("gen_json_decompressed_time").append(";")
              .append("gen_brief_regeneration_time").append(";")
              .append("gen_xml_regeneration_time").append(";")
              .append("gen_json_regeneration_time").append("\n");            
        }
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy - HH:mm:ss Z");
        ZonedDateTime zdt=ZonedDateTime.now();
        
        sb.append(System.nanoTime()).append(";")
          .append(dwindow.getMeasurements().getItemsQuantity()).append(";")
          .append(gen_brief).append(";")
          .append(gen_brief_length).append(";")
          .append(gen_xml).append(";")
          .append(gen_xml_length).append(";")
          .append(gen_json).append(";")
          .append(gen_json_length).append(";")
          .append(gen_brief_normalSize).append(";")
          .append(gen_xml_normalSize).append(";")
          .append(gen_json_normalSize).append(";")
          .append(gen_brief_compressed_time).append(";")
          .append(gen_brief_compressed_size).append(";")
          .append(gen_xml_compressed_time).append(";")
          .append(gen_xml_compressed_size).append(";")
          .append(gen_json_compressed_time).append(";")
          .append(gen_json_compressed_size).append(";")
          .append(gen_brief_decompressed_time).append(";")
          .append(gen_xml_decompressed_time).append(";")
          .append(gen_json_decompressed_time).append(";")
          .append(gen_brief_regeneration_time).append(";")
          .append(gen_xml_regeneration_time).append(";")
          .append(gen_json_regeneration_time);
        
        return sb.toString();
                
       /* for(MeasurementItem mi:brief_o.getMeasurements().getMeasurementItems())
        {
            System.out.println(mi.toString());
        }
        
        System.out.println("Original");
        System.out.println(briefmessage);
        String briefcontent2=brief_o.measureToText();
        String briefmessage2=metadata.writeBrief(briefcontent2);
        
        if(briefmessage2.equalsIgnoreCase(briefmessage)) System.out.println("The regenerated matches");
        else System.out.println(" Regenerated BAD:\n"+briefmessage2);
        
        
        System.out.println("[Brief] Time: "+gen_brief+" Length: "+briefmessage.length()+" Size: "+gen_brief_normalSize+" bytes");
        System.out.println("[XML] Time: "+gen_xml+" Length: "+xml.length()+" Size: "+gen_xml_normalSize+" bytes");
        System.out.println("[JSON] Time: "+gen_json+" Length: "+json.length()+" Size: "+gen_json_normalSize+" bytes");       */ 
    }
    
    public static final short DETERMINISTIC=0;
    public static final short ESTIMATED=1;
    public static final short ALTERNATED=2;
    
    /**
     * It generates a list of data window as a Cincamimis object where the number of data window
     * is given by the parameter nmessages. Each messsage will contain {nmeasures} measures.
     * Because the measures could be deterministic, estimated, or a mix of them the parameter {kind} 
     * indicates the kind of measures to be contained in each data window.
     * @param nmessages The number of messages to be incorporated in the list to be returned
     * @param nmeasures The number of measures per message
     * @param kind The kind of measures to be incorporated in the list of measures. It could assume the following
     * values: 0: All the measures will be deterministic 1: All the measures will ve estimated 2: A mix between estimated
     * and deterministic measures will be incorporated in the list.
     * @return An arraylist with a set of Cincamimis instances.
     */
    public static ArrayList<Cincamimis> generateMessagesList(Synthesizer metadata,Integer nmessages,Integer nmeasures, short kind) throws LikelihoodDistributionException, NoSuchAlgorithmException
    {
        if(metadata==null) return null;
        if(nmeasures==null || nmessages==null || nmeasures<1 || nmessages<1) return null;
        switch(kind)
        {
            case DETERMINISTIC:
            case ESTIMATED:
            case ALTERNATED:
                break;
            default:
                System.out.println("0: Deterministic 1: Estimated 2: Alternated");
                return null;
        }
        
        ArrayList<Cincamimis> list=new ArrayList();
        for(int i=0;i<nmessages;i++)
        {
            Cincamimis generated=generateMessage(metadata,nmeasures,kind);
            
            if(generated!=null) list.add(generated);
        }

        return list;
    }
    
    /**
     * It generates a data window following the aspects indicated as parameters.
     * @param nmeasures The number of measures to be incorporated in each data window
     * @param kind The kind of measures to be incorporated in the list of measures. It could assume the following
     * values: 0: All the measures will be deterministic 1: All the measures will ve estimated 2: A mix between estimated
     * and deterministic measures will be incorporated in the list.
     * @return a data window under the Cincamimis organization
     * @throws LikelihoodDistributionException It is raised when the likelihood distribution cannot be generated
     * @throws NoSuchAlgorithmException It is raised when MD5 is not present
     */
    public static Cincamimis generateMessage(Synthesizer metadata,Integer nmeasures,short kind) throws LikelihoodDistributionException, NoSuchAlgorithmException
    {
        Random r=new Random();

        String ecid=metadata.getEntityCategory();
        String projectID=metadata.getProjectID();
        ArrayList<String> attMetric=metadata.getAttMetricsID();
        ArrayList<String> ctxMetric=metadata.getCtxMetricsID();
        int nmetrics=attMetric.size();
        
        if(ecid==null || projectID==null || attMetric==null ||  attMetric.isEmpty()) return null;
        if(ctxMetric==null || ctxMetric.isEmpty() || r==null) return null;
        
        LikelihoodDistribution ld;
        ld = LikelihoodDistribution.factoryRandomDistributionEqualLikelihood(4L, 5L);        
        Context myContext=Context.factoryEstimatedValuesWithoutCD(ctxMetric.get(0), ld);        
        
        Cincamimis dwindow=new Cincamimis();
        dwindow.setDsAdapterID("dsAdapter1");
        MeasurementItemSet mis=new MeasurementItemSet();
        for(int j=0;j<nmeasures;j++)
          {//Genero desde 1 hasta i (multiplo de salto) mensajes hastya llegar a volMax
              MeasurementItem mi=null;
                      
              String myMetric=attMetric.get(j%nmetrics);
              
                switch(kind)
                {
                    case DETERMINISTIC:
                        mi=MeasurementItem.factory("idEntity1", "DS_"+myMetric, "myFormat", myMetric, 
                              BigDecimal.TEN.multiply(BigDecimal.valueOf(r.nextGaussian())),projectID,ecid);
                        break;
                    case ESTIMATED:
                        ld = LikelihoodDistribution.factoryRandomDistributionEqualLikelihood(4L, 5L);        
                        mi=MeasurementItem.factory("idEntity1", "DS_"+myMetric, "myFormat", myMetric,ld,projectID,ecid);
                        break;
                    case ALTERNATED:
                        if(j%2==0)
                        {
                            mi=MeasurementItem.factory("idEntity1", "DS_"+myMetric, "myFormat", myMetric, 
                                  BigDecimal.TEN.multiply(BigDecimal.valueOf(r.nextGaussian())),projectID,ecid);                            
                        }
                        else
                        {
                            ld = LikelihoodDistribution.factoryRandomDistributionEqualLikelihood(4L, 5L);        
                            mi=MeasurementItem.factory("idEntity1", "DS_"+myMetric, "myFormat", myMetric,ld,projectID,ecid);                            
                        }
                        break;
                }
                            
              if(mi!=null)
              {
                  mi.setContext(myContext);
                  mis.add(mi);
              }                
          }
          
         dwindow.setMeasurements(mis);
        
         return dwindow;
    }    
}
