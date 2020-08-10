/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ciedayap.changedetectors;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.ConcurrentHashMap;
import org.ciedayap.filters.SPCException;

/**
 * A project definition establishes the relative weight of each metric for an entity under monitoring.
 * The SPCMetricDetector class allows to detect individual data series changes based on an Online SPCBasedFilter (Russell Rhinehart)
 * This class establishes a threshold. When it is exceeded an alarm is triggered to the class's observers.
 * This class implements monitoring on the data series behavior globally.
 * Because it does not need to store any data, it could be used in online data processing and on mobile devices.
 * @author Mario Diván
 * @version 1.0
 */
public class SPCProjectDetector extends Observable implements Observer{
    /**
     * It manages the detectors associated with each metricID
     */
    private final ConcurrentHashMap<String,SPCMetricDetector> map;
    /**
     * The maxThreshold is used to trigger an alarm to observers.
     * The alarm is triggered when the weighted sum (i.e. the sum of the weight per number of detected changes for each metric in the  
     * ConcutrentHashMap)exceeds the maxThreshold parameter
     */
    private Double maxThreshold;
    /**
     * The project ID associated with the metrics
     */
    private final String projectID;
    /**
     * if it is TRUE the status  shown through the standard output, otherwise, it is not shown
     */
    private boolean verbose;
    /**
     * Default value of the sensing span for all the detectors  (Default: 1000ms)
     */    
    private long SS=1000;
    /**
     * Default value of the last elements to be considered for the estimation (Default:  11)
     */
    private Integer M=11;
    /**
     * Default value for the width in the SPC (Default: 2)
     */
    private Integer TRIGGER=2;
    
    /**
     * It creates a new instance of SPC-based filters for each specified metric.
     * @param undermonitoring The list contaning the set of "attributeID;metricID;weight"
     * @param projectID The project identification
     * @param maxThreshold The max threshold established to control the triggering of an alarm
     * @param verbose if it is TRUE the status  shown through the standard output, otherwise, it is not shown
     * @param m Value of the last elements to be considered for the estimation
     * @param ss  Value of the sensing span for all the detectors
     * @param trigger value for the width in the SPC
     * @throws DetectorException It is raised when the projectID is null, the maxThreshold is lower than 0, or the ConcurrenceMap has not been created.
     */
    public SPCProjectDetector(ArrayList<String> undermonitoring,String projectID,Double maxThreshold,boolean verbose,
            long ss, Integer m, Integer trigger) throws DetectorException, SPCException
    {
        if(projectID==null || projectID.trim().length()==0) throw new DetectorException("The projectID is not defined");
        if(maxThreshold<0) throw new DetectorException("The maxThreshold must be upper or equal to zero");

        this.M=(m!=null && m>=1)?m:11;
        this.SS=(ss<1)?1000:ss;
        this.TRIGGER=(trigger==null || trigger<1)?2:trigger;
        
        this.maxThreshold=maxThreshold;
        this.projectID=projectID;
        this.verbose=verbose;
        
        map=extractProjectInformation(undermonitoring,projectID,verbose);
        if(map==null) throw new DetectorException("The ConcurrentHashMap instance has not been created");
        turnFilters(true,verbose);//To activate and incorporate this instance as observer of each MetricDetector
    }
    
    /**
     * A default factory method
     * @param undermonitoring The list contaning the set of "attributeID;metricID;weight"
     * @param projectID The project identification
     * @param maxThreshold The max threshold established to control the triggering of an alarm
     * @param verbose if it is TRUE the status  shown through the standard output, otherwise, it is not shown
     * @param m Value of the last elements to be considered for the estimation
     * @param ss  Value of the sensing span for all the detectors
     * @param trigger value for the width in the SPC
     * @return When neither exception occurs, it returns a new instance of SPCProjectDetector with initialized threads
     * @throws DetectorException It is raised when the projectID is null, the maxThreshold is lower than 0, or the ConcurrenceMap has not been created.
     */
    public static final SPCProjectDetector create(ArrayList<String> undermonitoring,String projectID,Double maxThreshold,boolean verbose,
        Integer m, Integer trigger, long ss) throws DetectorException, SPCException
    {
        return new SPCProjectDetector(undermonitoring,projectID,maxThreshold,verbose,ss,m,trigger);
    }

    /**
     * A default factory method
     * @param undermonitoring The list contaning the set of "attributeID;metricID;weight"
     * @param projectID The project identification
     * @param maxThreshold The max threshold established to control the triggering of an alarm
     * @param verbose if it is TRUE the status  shown through the standard output, otherwise, it is not shown
     * @param obs An object to be incorporated as an observer
     * @param m Value of the last elements to be considered for the estimation
     * @param ss  Value of the sensing span for all the detectors
     * @param trigger value for the width in the SPC
     * @return When neither exception occurs, it returns a new instance of SPCProjectDetector with initialized threads
     * @throws DetectorException It is raised when the projectID is null, the maxThreshold is lower than 0, or the ConcurrenceMap has not been created.
     */
    public static final SPCProjectDetector create(ArrayList<String> undermonitoring,String projectID,Double maxThreshold,boolean verbose,
            Observer obs,Integer m, Integer trigger, long ss) throws DetectorException, SPCException
    {
        SPCProjectDetector item= new SPCProjectDetector(undermonitoring,projectID,maxThreshold,verbose,ss,m,trigger);
        item.addObserver(obs);
        return item;
    }
    /**
     * A default factory method
     * @param undermonitoring The list contaning the set of "attributeID;metricID;weight"
     * @param projectID The project identification
     * @param maxThreshold The max threshold established to control the triggering of an alarm
     * @param verbose if it is TRUE the status  shown through the standard output, otherwise, it is not shown
     * @param obs The list of objects to be incorporated as observers
     * @param m Value of the last elements to be considered for the estimation
     * @param ss  Value of the sensing span for all the detectors
     * @param trigger value for the width in the SPC
     * @return When neither exception occurs, it returns a new instance of SPCProjectDetector with initialized threads
     * @throws DetectorException It is raised when the projectID is null, the maxThreshold is lower than 0, or the ConcurrenceMap has not been created.
     */
    public static final SPCProjectDetector create(ArrayList<String> undermonitoring,String projectID,Double maxThreshold,boolean verbose,
            ArrayList<Observer> obs,Integer m, Integer trigger, long ss) throws DetectorException, SPCException
    {
        SPCProjectDetector item= new SPCProjectDetector(undermonitoring,projectID,maxThreshold,verbose,ss,m,trigger);
        if(obs!=null)
        {
            obs.forEach((o) -> {
                item.addObserver(o);
            });
        }
        
        return item;
    }
   
    /**
     * It turn on or off each SPCMetricDetector in the ConcurrentHashMap, incorporating the SPCProjectDetector as an observer
     * of each contained SPCMetricDetector
     * @param on if TRUE then the threads are initialized and activated, otherwise, they going to be off.
     * @param verbose if it is TRUE the status  shown through the standard output, otherwise, it is not shown
     */
    public final synchronized void turnFilters(boolean on,boolean verbose) throws SPCException
    {        
        for(SPCMetricDetector item:map.values())
        {
            item.turnFilter(on, verbose);
            if(on) item.addObserver(this);
        }
    }
     
    /**
     * It returns an Enumeration with the contained metrics being currently monitored.
     * @return An enumeration containing the monitored metrics
     */
    public Enumeration<String> getMetrics() 
    {
        if(map==null) return null;
        
        return map.keys();
    }
    
    @Override
    public String toString()
    {
        if(map==null) return "Empty Map";
        
        StringBuilder sb=new StringBuilder();
        
        for(SPCMetricDetector item:map.values())
        {
            if(item!=null)
                sb.append("AttributeID: ").append(item.getAttID())
                  .append(" MetricID: ").append(item.getMetricID())
                  .append(" Weight: ").append(item.getWeight())
                  .append(" Estimated Mean: ").append(item.getEstimatedMean())
                  .append(" N: ").append(item.getN())
                  .append(" Something processed: ").append(item.isSomethingProcessed()).append("\n");
        }        
        
        return sb.toString();
    }
    /**
     * It process a list of Strings under a specific organization (See undermonitoring parameter) and it creates a ConcurrentHashMap containing
     * the detectors for each metricID.
     * 
     * @param undermonitoring A list of strings. Each string must follow strictly the next organization "attributeID;metricID;likelihood".
     * For example: attribute1;metric1;0.25  attribute2;metric2;0.25    attribute3;metric3;0.25     attribute4;metric4;0.25
     * It is important that the sum of the likelihoods must be 1, otherwise, an exception is raised.
     * @param projectID The project ID related to the monitored metrics
     * @param verbose if it is TRUE the status  shown through the standard output, otherwise, it is not shown
     * @return a new instance of HashMap with the detectors per metric initialized
     * @throws DetectorException It is raised when the undermonitoring list was not provided properly, or when the sum of likelihoods is not 1
     */
    public synchronized final ConcurrentHashMap<String,SPCMetricDetector> extractProjectInformation(ArrayList<String> undermonitoring,
            String projectID, boolean verbose) throws DetectorException, SPCException
    {
        if(undermonitoring==null || undermonitoring.isEmpty()) throw new DetectorException("The ArrayList's instance is null");
        if(projectID==null || projectID.trim().length()==0) throw new DetectorException("The projectID is not indicated");
        StringBuilder sb=new  StringBuilder();
        
        String met[]=new String[undermonitoring.size()];
        String att[]=new String[undermonitoring.size()];
        double prob[]=new double[undermonitoring.size()];
        
        int i=0;
        double acu=0;
        for(String item:undermonitoring)            
        {
            String ret[]=item.split(";");
            if(ret.length!=3) throw new DetectorException("Malformed Item: "+item);
            
            att[i]=ret[0];
            met[i]=ret[1];
            try{
                prob[i]=new Double(ret[2]);
            }catch(NumberFormatException nfe)
            {
                throw new DetectorException("Invalid Likelihood in Item  (It cannot be converted to double): "+item);
            }
            acu=acu+prob[i];
            i++;
        }
        
        if(acu>1.001 || acu<0.99) throw new DetectorException("The sum of likelihoods must be between [0.99;1] and it is (0.01 is the max tolerance related to the rounding) "+acu);
        
        ConcurrentHashMap<String,SPCMetricDetector> map=new ConcurrentHashMap(met.length);
        
        for(i=0;i<met.length;i++)
        {
            SPCMetricDetector det=SPCMetricDetector.create(met[i], att[i], prob[i], verbose,this,M,TRIGGER,SS);
            
            map.put(met[i], det);
        }
        
        return map;
    }
 
    /**
     * It adds a measure to update the state of the associated detector for a given metric.
     * @param metricID The metricID used to locate the detector
     * @param val The value to be used to update the state
     * @return TRUE when the measure has been updated, false otherwise.
     */
    public boolean addMeasure(String metricID, double val)
    {
        if(map==null || metricID==null) return false;
        
        SPCMetricDetector detector=map.get(metricID);
        if(detector==null) return false;
        
        return detector.addMeasure(val);
    }
    
    /**
     * It adds a set of measuures to update the state of the associated detector for a given metric.
     * @param metricID The metricID used to locate the detector
     * @param vals The set of measures to be used to update the state
     * @return It returns null when the map is not created or the indicated metric does not exist. 
     * Otherwise, a boolean array is returned indicating for each position whether the measure has been added  (TRUE) or  not  (FALSE).
     */
    public boolean[] addMeasures(String metricID, ArrayList<Double> vals)
    {
        if(map==null || metricID==null) return null;
        
        SPCMetricDetector detector=map.get(metricID);
        if(detector==null) return null;
        
        return detector.addMeasures(vals);
    }
    
    @Override
    public void update(Observable o, Object arg) {

        double global=map.values().stream().mapToDouble(p->p.getWeightedCounter()).sum();
        
        if(global>maxThreshold)
        {
            if(verbose)
                System.out.println("[Project Alarm] "+global+" > "+maxThreshold);
            
            //Restart counters
            map.values().forEach((p) -> p.restartCounter());

            this.setChanged();
            this.notifyObservers(global);        
        }
        else
        {
            if(verbose)
                System.out.println("[Contained] "+global+" < "+maxThreshold);
        }
    }
    
    /**
     * It indicates whether all the detectors contain or not values pending of processing
     * @return FALSE some detector has pending values to process, TRUE neither detector has pending measures to process
     */
    public synchronized boolean isEmpty()
    {
        int total=map.values().stream().mapToInt(p->(p.isEmpty())?0:1).sum();
        
        if(verbose) System.out.println("..."+total+" pendings");
        
        return (total==0);
    }
    
    /**
     * This method will return the estimated mean when it exists
     * @param metID the metricID for which the estimated mean needs to be obtained
     * @return The estimated mean for the metID, null otherwise
     */
    public synchronized Double getEstimatedMean(String metID)
    {
        if(metID==null || metID.trim().length()==0) return null;
        if(map==null || map.isEmpty()) return null;
        
        SPCMetricDetector var = map.get(metID);
        return (var==null)?null:var.getEstimatedMean();        
    }

    /**
     * This method will return the estimated deviation when it exists
     * @param metID the metricID for which the estimated mean needs to be obtained
     * @return The estimated mean for the metID, null otherwise
     */
    public synchronized Double getEstimatedDeviation(String metID)
    {
        if(metID==null || metID.trim().length()==0) return null;
        if(map==null || map.isEmpty()) return null;
        
        SPCMetricDetector var = map.get(metID);
        return (var==null)?null:var.getEstimatedDeviation();
    }
    
    /**
     * This method will add a given observer to the SPCMetricDetector instance
     * @param metID the metricID for which the observer would be incorporatede
     * @param o the observer to be incorporateede
     * @return TRUE when the observer has been added, false otherwise
     */
    public synchronized boolean addObserverToSPCMetric(String metID, Observer o)
    {
        if(metID==null || metID.trim().length()==0) return false;
        if(map==null || map.isEmpty()) return false;
        
        SPCMetricDetector var = map.get(metID);
        if(var==null) return false;
        var.addObserver(o);        
        
        return true;
    }
}
