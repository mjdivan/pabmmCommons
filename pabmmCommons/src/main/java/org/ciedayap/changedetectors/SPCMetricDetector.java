/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ciedayap.changedetectors;

import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.ciedayap.filters.SPCBasedFilter;
import org.ciedayap.filters.SPCException;

/**
 * It implements the change detector at the Metric's level 
 * 
 * @author Mario Divan
 * @version 1.0
 */
public class SPCMetricDetector extends Observable implements Observer{
    /**
     * It represents the metricID for whom the data are monitored
     */
    private final String metricID;
    /**
     * It is the attribute ID related to the metric ID
     */
    private final String attID;
    /**
     * It is the instance related to the filter
     */
    private SPCBasedFilter filterTask;
    /**
     * It is the executor associated with the filter task
     */
    private ExecutorService pool;
    /**
     * It represents the  counter from the last triggered alarm.
     */
    private long counter;

    /**
     * It represents the weighting related to the indicated metric
     */
    private double weight;
    
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
     * Default Constructor
     * @param metricID The metric ID
     * @param attributeID The attribute ID
     * @param weight The weighting related to the metricID
     * @param verbose if it is TRUE the status  shown through the standard output, otherwise, it is not shown
     * @throws DetectorException it is raised when the weight is not in [0;1] interval, metricIID or attributeIID is null
     */
    public SPCMetricDetector(String metricID, String attributeID, double weight,boolean verbose) throws DetectorException, SPCException
    {
        if(weight<0 || weight>1) throw new DetectorException("Weight must be between 0 and 1");
        if(metricID==null || metricID.trim().length()==0) 
            throw new DetectorException("metricID has not been defined");
        if(attributeID==null || attributeID.trim().length()==0) 
            throw new DetectorException("attributeID has not been defined");        
        
        this.metricID=metricID;
        this.attID=attributeID;
        this.weight=weight;
        counter=0;
        
        turnFilter(true,  verbose);
    }        

    /**
     * Default Constructor
     * @param metricID The metric ID
     * @param attributeID The attribute ID
     * @param weight The weighting related to the metricID
     * @param verbose if it is TRUE the status  shown through the standard output, otherwise, it is not shown
     * @param m Value of the last elements to be considered for the estimation
     * @param ss  Value of the sensing span for all the detectors
     * @param trigger value for the width in the SPC
     * @throws DetectorException it is raised when the weight is not in [0;1] interval, metricIID or attributeIID is null
     */
    public SPCMetricDetector(String metricID, String attributeID, double weight,boolean verbose,
            Integer m, long ss, Integer trigger) throws DetectorException, SPCException
    {
        if(weight<0 || weight>1) throw new DetectorException("Weight must be between 0 and 1");
        if(metricID==null || metricID.trim().length()==0) 
            throw new DetectorException("metricID has not been defined");
        if(attributeID==null || attributeID.trim().length()==0) 
            throw new DetectorException("attributeID has not been defined");        
        
        this.M=(m!=null && m>=1)?m:11;
        this.SS=(ss<1)?1000:ss;
        this.TRIGGER=(trigger==null || trigger<1)?2:trigger;
        
        this.metricID=metricID;
        this.attID=attributeID;
        this.weight=weight;
        counter=0;
        
        turnFilter(true,  verbose);
    }        
     
    /**
     * Default factory method
     * @param metricID The metric ID
     * @param attributeID The attribute ID
     * @param weight The weighting related to the metricID
     * @param verbose if it is TRUE the status is shown through the standard output, otherwise, it is not shown
     * @return a new instance with the Thread initialized waiting for measures
     * @throws DetectorException it is raised when the weight is not in [0;1] interval, metricIID or attributeIID is null
     */
    public static synchronized SPCMetricDetector create(String metricID, String attributeID, double weight,boolean verbose) throws DetectorException, SPCException
    {
        if(weight<0 || weight>1) throw new DetectorException("Weight must be between 0 and 1");
        if(metricID==null || metricID.trim().length()==0) 
            throw new DetectorException("metricID has not been defined");
        if(attributeID==null || attributeID.trim().length()==0) 
            throw new DetectorException("attributeID has not been defined");        
        
        return new SPCMetricDetector(metricID,attributeID,weight,verbose);
    }          

    /**
     * Default factory method
     * @param metricID The metric ID
     * @param attributeID The attribute ID
     * @param weight The weighting related to the metricID
     * @param verbose if it is TRUE the status is shown through the standard output, otherwise, it is not shown
     * @param item The item to be incorporated as an observer of the metric detector
     * @return a new instance with the Thread initialized waiting for measures
     * @throws DetectorException it is raised when the weight is not in [0;1] interval, metricIID or attributeIID is null
     */
    public static synchronized SPCMetricDetector create(String metricID, String attributeID, 
            double weight,boolean verbose,Observer item) throws DetectorException, SPCException
    {
        if(weight<0 || weight>1) throw new DetectorException("Weight must be between 0 and 1");
        if(metricID==null || metricID.trim().length()==0) 
            throw new DetectorException("metricID has not been defined");
        if(attributeID==null || attributeID.trim().length()==0) 
            throw new DetectorException("attributeID has not been defined");        
        
        return new SPCMetricDetector(metricID,attributeID,weight,verbose);
    }          

    /**
     * Default factory method
     * @param metricID The metric ID
     * @param attributeID The attribute ID
     * @param weight The weighting related to the metricID
     * @param verbose if it is TRUE the status is shown through the standard output, otherwise, it is not shown
     * @param item The item to be incorporated as an observer of the metric detector
     * @param m Value of the last elements to be considered for the estimation
     * @param ss  Value of the sensing span for all the detectors
     * @param trigger value for the width in the SPC
     * @return a new instance with the Thread initialized waiting for measures
     * @throws DetectorException it is raised when the weight is not in [0;1] interval, metricIID or attributeIID is null
     */
    public static synchronized SPCMetricDetector create(String metricID, String attributeID, 
            double weight,boolean verbose,Observer item,
            Integer m, Integer trigger, long ss) throws DetectorException, SPCException
    {
        if(weight<0 || weight>1) throw new DetectorException("Weight must be between 0 and 1");
        if(metricID==null || metricID.trim().length()==0) 
            throw new DetectorException("metricID has not been defined");
        if(attributeID==null || attributeID.trim().length()==0) 
            throw new DetectorException("attributeID has not been defined");        
        
        return new SPCMetricDetector(metricID,attributeID,weight,verbose,m,ss,trigger);
    }          
    
    /**
     * It indicates whether the filter has values pending of processing or not.
     * 
     * @return TRUE indicates that there exists pending values of processing, FALSE indicates that the processing list is empty
     */
    public synchronized boolean isEmpty()
    {
        if(filterTask==null) return true;
        
        return filterTask.isEmpty();
    }
    
    /**
     * This method controls the turn on or off of the filter.By default, it is invoked in the instance startup.
     * @param on When it is TRUE the pool is initialized and the filterTask is running waiting for measures. 
     * @param verbose if it is TRUE the status is shown through the standard output, otherwise, it is not shown
     * When it is FALSE the filterTask is disabled and the pool is shutdown
     * @throws org.ciedayap.filters.SPCException It is raised when an inappropiated value is given to M, TRIGGER or SS (See the class's properties).
     */
    public final synchronized void turnFilter(boolean on,boolean verbose) throws SPCException
    {
        if(on)
        {     
            if(filterTask!=null) return;
            
            filterTask=SPCBasedFilter.create(this,verbose,M,TRIGGER,SS);
            filterTask.setEnabled(true);
            pool = Executors.newFixedThreadPool(1);
            pool.execute(filterTask);

        }
        else            
        {
            filterTask.setEnabled(false);
            filterTask=null;
            pool.shutdown();
            pool=null;
        }
    }
    
    @Override
    public void update(Observable o, Object arg) {
        //SPCFilter has indicated a new change on the data series
        counter++;
        
        //The counter has changed
        this.setChanged();
        this.notifyObservers();
    }

    /**
     * It returns the last estimated mean known
     * @return The last estimated mean
     */
    public Double getEstimatedMean()
    {
        if(filterTask==null) return null;
        
        return filterTask.getXSPC();
    }

    /**
     * The number of measures that support the current estimated mean
     * @return The number of measures from the last change of an estimated mean
     */
    public Long getN()
    {
        if(filterTask==null) return null;
        
        return filterTask.getN();
    }

    /**
     * It indicates whether the filter has processed some measure or not
     * @return TRUE indicates that at least one measure has been processed using the filter, FALSE it indicates that nothing was processed
     */
    public Boolean isSomethingProcessed()
    {
        if(filterTask==null) return null;
        
        return (!filterTask.isFirst_call());
    }
    
    /**
     * @return the metric ID
     */
    public String getMetricID() {
        return metricID;
    }

    /**
     * @return the attribute ID
     */
    public String getAttID() {
        return attID;
    }

    /**
     * @return the current status of the counter at the method invocation instant
     */
    public long getCounter() {
        return counter;
    }

    /**
     * @return the weight associated with the metric ID
     */
    public double getWeight() {
        return weight;
    }

    /**
     * @param weight the weight associated with the metric ID to set
     */
    public void setWeight(double weight) {
        this.weight = weight;
    }
    
    /**
     * It returns the product between counter (number of detected changes) and 
     * the weighting for the attributeID.
     * @return the weighted product per metric
     */
    public synchronized Double getWeightedCounter()
    {
        return weight*counter;
    }
    
    /**
     * It adds a new measure to the filter
     * @param val Thee measure to be added to the filter
     * @return TRUE when the measure was added, false otherwise
     */
    public synchronized boolean addMeasure(double val)
    {
        if(filterTask==null && !filterTask.isEnabled()) return false;
        
        return filterTask.addMeasure(val);
    }

    /**
     * It incorporates a set of measures in the filter's list
     * @param val The set of measures to be added
     * @return NULL when the filter is not running. A boolean array indicating
     * TRUE/FALSE whether the measure has been added according to its position
     */
    public synchronized boolean[] addMeasures(ArrayList<Double> val)
    {
        if(filterTask==null && !filterTask.isEnabled()) return null;
        
        return filterTask.addMeasures(val);
    }
    
    /**
     * It restarts the counter avoiding another process modifies it while
     * it is established to zero
     */
    public synchronized void restartCounter()
    {
        this.counter=0L;
    }
    
}
