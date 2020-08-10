/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ciedayap.filters;

import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.ciedayap.mair.utils.SPCFilterMeasurementRecord;

/**
 * This implementation is based on the proposed algorithm by R. Russell Rhinehart (www.r3eda.com)
 * 
 * @author Mario Diván
 * @version 1.o
 */
public class SPCBasedFilter extends Observable implements  Runnable {

    /**
     * @return the first_call
     */
    public boolean isFirst_call() {
        return first_call;
    }
    /**
     * The list of measures to be analyzed
     */
    private ConcurrentLinkedQueue<Double> values;
    /**
     * It affects the expected variation range for the process. When it is 2, the limit is (+-)2 standard deviations for CUSUM
     * representing the 95% confidence level. When it is 3, the limit is (+-)3 standard deviations for CUSUM reprersenting the 99% confidence level,
     * and 4 will represent 99.9% and then so on.
     */
    private Integer TRIGGER;
    /**
     * The number of past measures that are used in the estimation of variance
     */
    private final Integer M;
    /**
     * It represents a counter from the last change
     */
    private long N;
    /**
     * It represents the accumulative number of (deviations from x) that the process has deviated from (the old X average)
     */
    private Double CUSUM;
    /**
     * It is the SPC-filtered value of x (The  estimated average value of x)
     */
    private Double XSPC=0.0;
    /**
     * The old x average
     */
    private Double XOLD=0.0;
    /**
     * It is a variance estimation
     */
    private Double V;
    /**
     * First factor for the variance estimation
     */
    private Double FF1;
    /**
     * Second factor for the variance estimation
     */
    private Double FF2;
    /**
     * It is a flag variable to indicate whether the first invocation is the currrent or not.
     */
    private boolean first_call;
    /**
     * It is the control variable for the Runnable Instance
     */
    private boolean enabled;
    /**
     * It is the sensing span in milliseconds where the thread read the queue
     */
    private long SensingSpan;
    /**
     * It shows the computing on the standard output while it occurs
     */
    private boolean verbose;
    /**
     * It is optional. When it is defined, each compute and addMeasure operation will be measured
     */
    private SPCFilterMeasurementRecord record=null;
    
    public SPCBasedFilter()
    {
        this.first_call = true;
        N=0;
        CUSUM=0.0;
        XSPC=0.0;
        XOLD=0.0;
        M=11;
        V=0.0;
        FF2=1.0/((M-1)*2.0);
        FF1=(M-2.0)/(M-1.0);   
        TRIGGER=2;
        values=new ConcurrentLinkedQueue();
        enabled=true;
        SensingSpan=1000;
        verbose=false;
        record=null;
    }
    
    /**
     * A constructor with the possibility to change the M and TRIGGER parameter
     * @param m The number of past measures that are used in the estimation of variance
     * @param trigger It is a factor that affects the number of standard deviations to be considered along with the process monitoring
     * @param ss The sensing span to read the queue of pending values
     * @param verb It indicates that partial computing should be shown on the standard output
     * @throws SPCException It is raised when m<1, trigger<1, or ss<1.
     */
    public SPCBasedFilter(Integer m, Integer trigger,long ss,boolean verb) throws SPCException
    {
        if(m<1) throw new SPCException("Invalid M value. It must be upper than 0");
        if(trigger<1) throw new SPCException("TRIGGER must be upper or equal than 1");
        if(ss<1) throw new SPCException("ss must be upper or equal than 1");
        
        M=m;
        values=new ConcurrentLinkedQueue();
        TRIGGER=trigger;
        this.first_call = true;
        N=0;
        CUSUM=0.0;
        XSPC=0.0;
        XOLD=0.0;
        V=0.0;
        FF2=1.0/((M-1)*2.0);
        FF1=(M-2.0)/(M-1.0);         
        enabled=true;
        SensingSpan=ss;
        this.verbose=verb;
        record=null;
    }
    
    /**
     * It computes and updates the expected mean, detecting the changes
     * @param x The value related to the new measure
     * @return TRUE when a change has been detected, FALSE otherwise.
     */
    private synchronized boolean compute(Double X)
    {
        long start,end;
        start=System.nanoTime();
        
        if(isFirst_call())
        {
            N=0;
            XOLD=0.0;
            XSPC=0.0;
            V=0.0;
            CUSUM=0.0;
            FF2=1.0/((M-1)*2.0);
            FF1=(M-2.0)/(M-1.0);                        
            first_call=false;            
        }
     
        N++;
        V=FF1*V  + FF2*Math.pow((X-XOLD),2);
        XOLD=X;
        
        CUSUM= CUSUM + (X-XSPC);
        
        if(isVerbose()) System.out.println("N: "+N+" X: "+X+" XSPC: "+XSPC+"  TRIGGER: "+TRIGGER+" CUSUM: "+CUSUM);
        
        if(Math.abs(CUSUM)>(getTRIGGER()*Math.sqrt(V*N)))
        {
            XSPC= XSPC+(CUSUM/N);
            N=0;
            CUSUM=0.0;
            
            this.setChanged();            
            this.notifyObservers();//It informs to all observers waiting for a change in the data series            
            
            if(record!=null) getRecord().addComputeOp(System.nanoTime()-start, null);
            return true;//A change was found
        }
        
        if(record!=null) getRecord().addComputeOp(System.nanoTime()-start, null);
        
        return false;//There is not change
    }

    /**
     * It adds a measure at the end of the list
     * @param val The measure to be added
     * @return TRUE when the measure has been added, FALSE otherwise.
     */
    public synchronized boolean addMeasure(Double val)
    {
        long start=System.nanoTime();
        
        if(val==null) 
        {
            if(getRecord()!=null) getRecord().addMeasureOp(System.nanoTime()-start, null);
            return false;
        }
        
        boolean ret=values.add(val);
        if(getRecord()!=null) getRecord().addMeasureOp(System.nanoTime()-start, null);
        
        return ret;
    }
    
    /**
     * It adds a set of measures contained in an ArrayList
     * @param vals The set of measures to be added
     * @return An array indicating for each position the operation result
     */
    public synchronized boolean[] addMeasures(ArrayList<Double> vals)
    {
        if(vals==null || vals.isEmpty()) return null;
        boolean ret[]=new boolean[vals.size()];

        int i=0;
        for(Double val:vals)
        {
            ret[i]=values.add(val);
            i++;
        }
        
        return ret;
    }
    
    @Override
    public void run() {
        //It keeps working when 1) It is active, or 2) When it is indicated as inactive but it has pending values to process
        while(isEnabled() || (!enabled && !values.isEmpty()))
        {
          while(values!=null && !values.isEmpty())
          {
              Double first=values.poll();
              

              if(compute(first))
              {                  
                  if(isVerbose())  System.out.println("Notifying - Detected Change...");
              }
          }
          
          try
          {
                Thread.sleep(this.SensingSpan);
          } catch (InterruptedException ex) 
          {
                Logger.getLogger(SPCBasedFilter.class.getName()).log(Level.SEVERE, null, ex);
          }
        }
        
        if(record!=null)
        {
            record.getAddMeasure().clear();
            record.getCompute().clear();
        }
    }

    /**
     * @return the TRIGGER
     */
    public Integer getTRIGGER() {
        return TRIGGER;
    }

    /**
     * @return the M
     */
    public Integer getM() {
        return M;
    }

    /**
     * @return the N
     */
    public long getN() {
        return N;
    }

    /**
     * @return the CUSUM
     */
    public Double getCUSUM() {
        return CUSUM;
    }

    /**
     * @return the XSPC
     */
    public Double getXSPC() {
        return XSPC;
    }

    public Double getEstimatedDeviation(){
        if(V==null || N==0) return null;
        
        return Math.sqrt(V*N);
    }
    /**
     * @return the enabled
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * @param enabled It is the control variable of the thread. The thread is activated while this variable is TRUE, otherwise, the thread will finish
     */
    public synchronized void setEnabled(boolean enabled) {
        this.enabled = enabled;
        if(!this.enabled) {
            first_call=true;
        } //It indicates that the next time that the filter be activated, the calculus start from scratch
    }
    
    /**
     * It is a Default factory method
     * @return A new instance of SPCBasedfFilter
     */
    public static synchronized SPCBasedFilter create()
    {
        return new SPCBasedFilter();
    }

    /**
     * It creates a new instance incorporating the item as a new observer
     * @param item It creates the filter and incorporates item as an observer of it
     * @param verbose if it is TRUE the status is shown through the standard output, otherwise, it is not shown
     * @return It creates a new instance incorporating item as an observer
     */
    public static synchronized SPCBasedFilter create(Observer item, boolean verbose)
    {
        SPCBasedFilter filter= new SPCBasedFilter();
        filter.addObserver(item);
        filter.setVerbose(verbose);
        
        return filter;
    }
    
    /**
     * It creates a new instance incorporating the item as a new observer
     * @param item It creates the filter and incorporates item as an observer of it
     * @param verbose if it is TRUE the status is shown through the standard output, otherwise, it is not shown
     * @param m It is the number of measures used in the estimation of the mean (Default: 11)
     * @param trigger It is the number of deviations to SPC (Default: 2)
     * @param ss It is the sensing span configurated for the internal queue  (Default 1000ms)
     * @return It creates a new instance incorporating item as an observer
     * @throws org.ciedayap.filters.SPCException
     */
    public static synchronized SPCBasedFilter create(Observer item, boolean verbose,Integer m, Integer trigger,long ss) throws SPCException
    {
        Integer M=(m==null || m<1)?11:m;
        Integer TRIGGER=(trigger==null || trigger<1)?2:trigger;
        long SS=(ss<1)?1000:ss;
        
        SPCBasedFilter filter= new SPCBasedFilter(M, TRIGGER,SS,verbose);
        filter.addObserver(item);
        filter.setVerbose(verbose);
        
        return filter;
    }
    
    /**
     * It creates a new instance of SPCBasedFilter, incorporating the list as observers of it
     * @param items The items to be incorporated as observers
     * @param verbose if it is TRUE the status is shown through the standard output, otherwise, it is not shown
     * @return A new instance of the filter incorporating the observers
     */
    public static synchronized SPCBasedFilter create(ArrayList<Observer> items, boolean verbose)
    {
        SPCBasedFilter filter= new SPCBasedFilter();
        
        items.forEach((item) -> {            
            filter.addObserver(item);
        });
        filter.setVerbose(verbose);
        
        return filter;
    }

    /**
     * @return the verbose
     */
    public boolean isVerbose() {
        return verbose;
    }

    /**
     * @param verbose the verbose to set
     */
    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }
    
    /**
     * It indicates whether the filter has values pending of processing or not.
     * @return TRUE indicates that there exists pending values of processing, FALSE indicates that the processing list is empty
     */
    public synchronized boolean isEmpty()
    {
        if(values==null) return true;
        
        return values.isEmpty();
    }
    
    public synchronized void initializeMeasurementRecord()
    {
        record=SPCFilterMeasurementRecord.create();
    }

    /**
     * @return the record
     */
    public SPCFilterMeasurementRecord getRecord() {
        return record;
    }
}
