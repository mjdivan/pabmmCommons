/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ciedayap.buffer;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Stream;
import org.ciedayap.changedetectors.DetectorException;
import org.ciedayap.changedetectors.SPCProjectDetector;
import org.ciedayap.filters.SPCException;

/**
 * This class implements a buffer can keep the last "maxMeasures" per metric.
 * When the number of metrics is exceeded, the last measures are discarded to
 * keep the new ones.
 * 
 * When the buffer receives the alarm from the SPCProjectDetector filtering online
 * the added measures, it raises an alarm indicating that the measures need to
 * be informed through the proper media (e.g. CINCAMI/MIS). The last will depend 
 * on the used format.
 * 
 * @author Mario Divan
 * @version 1.0
 */
public class SPCBasedBuffer extends Observable implements Observer{
    /**
     * The projectID related to the buffer
     */
    private String projectID;
    /**
     * The max number of measures to be kept in memory per metric
     */
    private final long maxMeasures;
    /**
     * The attributeID. The positioning is synchornized with metID and weighting
     */
    private final String attID[];
    /**
     * The metricID. The positioning is synchornized with attID and weighting
     */
    private final String metID[];
    /**
     * The weighting associated with a given metIID and attID
     */
    private final double weighting[];
    /**
     * The queue cotaining the last <b>maxMeasures</b> measures
     */
    private ConcurrentLinkedQueue<SPCBasedBufferNode> queue;
    /**
     * The SPC Based detector for the project
     */
    private SPCProjectDetector detector;
    /**
     * It is a counter of the added measures. It is restarted when the transmission occurs
     */
    private long counter=0;
    
    /**
     * It is true when the detector has indicated the change or the maxTime has been exceeded
     */
    private boolean readyToTransmit=false;
    /**
     * It incorporates the last time that cleanDataBuffer was invoked. Initially, it is initialized to 
     * the creation time.
     */
    private long nanoLastTransmission;
    /**
     * It implements a clock notifying this instance each time that given time is reached.
//     */
    private BufferClock clock;
    /**
     * When it is activated, this pool is responsible for the clock
     */
    private ExecutorService pool;    
    private boolean verbose;

    /**
     * It creates a new buffer instance
     * @param projectID the projectID associated with the buffer
     * @param attributes The list contaning the set of "attributeID;metricID;weight"
     * @param max the max number of measures to be kept per metricID
     * @param maxSPCTolerance The max deviations to be tolerated buy the SPCBasedFilter (Default: 2)
     * @throws BufferException
     * @throws DetectorException
     * @throws SPCException 
     */
    public SPCBasedBuffer(String projectID,ArrayList<String> attributes,long max,Double maxSPCTolerance,boolean verbose) throws BufferException, DetectorException, SPCException
    {
        this.verbose=verbose;
        if(projectID==null || projectID.trim().length()==0) throw new BufferException("The ProjectID has not indicated");
        this.projectID=projectID;
        
        if(max<1000) this.maxMeasures=1000;
        else this.maxMeasures=max;
        
        if(attributes==null || attributes.isEmpty()) throw new BufferException("Attributes do not defined");
        
        attID=new String[attributes.size()];
        metID=new String[attributes.size()];
        weighting=new double[attributes.size()];
        
        int i=0;
        for(String item:attributes)
        {
            String compo[]=item.split(";");
            if(compo.length!=3) throw new BufferException("Attributes' Bad List. The format must be 'attributeID;metricID;weighting'");
            
            if(compo[0]==null) throw new BufferException("'<attributeID>;metricID;weighting' Attribute not found");
            attID[i]=compo[0];
            if(compo[1]==null) throw new BufferException("'attributeID;<metricID>;weighting' MetricID not found");
            metID[i]=compo[1];
            if(compo[2]==null) throw new BufferException("'attributeID;metricID;<weighting>' Weighting not found");
            try{
                weighting[i]=Double.valueOf(compo[2]);
            }catch(NumberFormatException nfe)
            {
                throw new BufferException("The Attribute's Weighting is not a double: "+compo[2]);
            }
            
            i++;
        }

        nanoLastTransmission=System.nanoTime();
        queue=new ConcurrentLinkedQueue();
        detector=initializeDetector(projectID,attributes,maxSPCTolerance,verbose);
    }
    
    /**
     * Default factory method for the buffer
     * 
     * @param projectID The associated projectID
     * @param attributes The list contaning the set of "attributeID;metricID;weight"
     * @param max The max number of measures
     * @param maxSPCTolerance The threshold established. When it is exceeded, an alarm is raised from the SPCProjectDetector and it is restarted (Default: 5)
     * @return A new and initialized instance of the buffer
     * @throws BufferException
     * @throws DetectorException
     * @throws SPCException 
     */
    public synchronized static SPCBasedBuffer create(String projectID,ArrayList<String> attributes,long max,Double maxSPCTolerance, boolean verbose) throws BufferException, DetectorException, SPCException 
    {
        return new SPCBasedBuffer(projectID,attributes,max,maxSPCTolerance,verbose); 
    }
    
    /**
     * Initializes the project detector indicating the current buffer instance as an observer
     * @param projectID The associated projectID
     * @param attributes The list contaning the set of "attributeID;metricID;weight"
     * @param maxSPCTolerance The threshold established. When it is exceeded, an alarm is raised from the SPCProjectDetector and it is restarted (Default: 5)
     * @return An instance of the SPCDetector where the buffer is an observer
     * @throws DetectorException
     * @throws SPCException 
     */
    private synchronized SPCProjectDetector initializeDetector(String projectID,ArrayList<String> attributes,Double maxSPCTolerance,boolean verbose) throws DetectorException, SPCException
    {
        return SPCProjectDetector.create(attributes,projectID,(maxSPCTolerance==null || maxSPCTolerance<1)?5:maxSPCTolerance,verbose,
            this,11, 2, 100);//Buffer is a SPCProjectDetector's Observer-
    }
    
    /**
     * It return the unidimensional array for the metric IDs
     * @return a unidimensional array for the metric IDs
     */
    public String[] getMetricIDs()
    {
        if(metID==null) return null;
        
        return this.metID;
    }
    
    /**
     * It returns the the metricID for the given position
     * @param idx The index to be inquired
     * @return The metricID when the index is correct, null otherwise
     */
    public String getMetricID(int idx)
    {
        if(metID==null) return null;
        if(idx<0 || idx>metID.length) return null;
        
        return metID[idx];
    }

    /**
     * It returns the the index for the given metric
     * @param MetricID The metricID to be inquired
     * @return The index when the metricID exists, -1 otherwise
     */    
    public int getMetricPosition(String MetricID)
    {
        if(metID==null) return -1;
        if(MetricID==null) return -1;
        
        for(int i=0;i<metID.length;i++) if(metID[i].equalsIgnoreCase(MetricID)) return i;
        
        return -1;
    }
    
    /**
     * It adds measures to the queue. When the  queue size exceeds the maxMeasures parameter,
     * the oldest data is discarded and the newest data is incorporated at the end of the queue.
     * @param values The measures to be incorporated
     * @throws BufferException When there no exist values to be added
     */
    public synchronized void addMeasures(Double values[]) throws BufferException
    {
        addMeasures(ZonedDateTime.now(),values);
    }

    /**
     * It adds measures to the queue. When the  queue size exceeds the maxMeasures parameter,
     * the oldest data is discarded and the newest data is incorporated at the end of the queue.
     * @param values The measures to be incorporated
     * @param ts The timestamp associated with measures
     * @throws BufferException When there no exist values to be added
    */    
    public synchronized void addMeasures(ZonedDateTime ts,Double values[]) throws BufferException
    {
        SPCBasedBufferNode node=SPCBasedBufferNode.create(ts, values);
        if(node==null) return;
        
        if((this.getCounter()+1)<this.maxMeasures)
        {
            if(queue.add(node))
            {
                counter++;
                for(int i=0;i<metID.length;i++)
                {
                    if(values[i]!=null)
                        this.detector.addMeasure(metID[i], values[i]);
                }
            }
        }
        else
        {
            queue.poll();
            if(queue.add(node))
            {
                counter++;
                for(int i=0; i<metID.length; i++)
                {
                    if(values[i]!=null)
                        this.detector.addMeasure(metID[i], values[i]);
                }                
            }            
        }
    }
    
    @Override
    public void update(Observable o, Object arg) {
        boolean notify=false;
        String message="-";
        if(o instanceof SPCProjectDetector)
        {
            notify=true;
            message=("[Alarm -Detected Change-] Informing...ready to transmit-> "+ZonedDateTime.now()+" Counter: "+counter);            
        }
        else
        {
            if(o instanceof BufferClock)
            {//
                long triggered=(long)arg;
                
                if(triggered>this.nanoLastTransmission)
                {
                    if(clock!=null)
                    {
                        notify=(((triggered-this.nanoLastTransmission)/1000000)>=clock.getTimespan());    
                        long elapsed=(triggered-this.nanoLastTransmission)/1000000000;
                        if(notify) message=("[Alarm -Time Limit-] Informing...ready to transmit-> "+ZonedDateTime.now()+" Elapsed: "+elapsed+"s Counter: "+counter);                                
                    }
                }
                else
                {
                    notify=false;
                    //Nothing to do
                }
            }
        }
        
        if(notify)
        {
            readyToTransmit=true;//SPCFilter has detected a change in the data series                
        
            this.setChanged();
            this.notifyObservers(message);
        }
    }

    /**
     * @return the projectID
     */
    public String getProjectID() {
        return projectID;
    }

    /**
     * @return the maxMeasures
     */
    public long getMaxMeasures() {
        return maxMeasures;
    }
    
    /**
     * It cleans the data from buffer but the history related to SPCBasedFilter is kept.
     * The counter is restarted jointly with the readyToTransmit variable
     */
    public synchronized void cleanDataBuffer()
    {
        if(queue!=null) this.queue.clear();
        this.counter=0;        
        this.readyToTransmit=false;
        this.nanoLastTransmission=System.nanoTime();
    }
    
    /**
     * It returns a stream based on the buffer data
     * @return a Stream based on the buffer data
     */
    public Stream<SPCBasedBufferNode> getBufferDataStream()
    {
        return (queue==null)?null:queue.stream();
    }
    
    /**
     * It returrns an Iterator based on the buffer data
     * @return an Iterator based on the buffer data
     */
    public Iterator<SPCBasedBufferNode> getBufferDataIterator()
    {
        return (queue==null)?null:queue.iterator();
    }
    
    /**
     * It returns an independent copy of measures from the data buffer
     * @return An independent copy from the data buffer containing the measures
     */
    public Object[] getBufferData()
    {
        return (queue==null)?null:queue.toArray();
    }
    
    /**
     * It returns a copy from the data buffer, cleaning the last one
     * @return An independent copy from the data buffer containing the measures
     */
    public synchronized Object[] getBufferData_and_clean()
    {
        Object ret[]=getBufferData();
        
        this.cleanDataBuffer();
        return ret;        
    }
    
    /**
     * It starts a new clock to inform the buffer each certain time
     * @param timespan Time span where the clock will notify to this instance for transmission
     * @return TRUE when the clock has been activated and the current instance registered as an observer
     */
    public synchronized boolean activateClock(long timespan)
    {
        if(timespan<1) return false;
        this.deactivateClock();
        
        clock=new BufferClock(timespan);
        clock.addObserver(this);
        pool = Executors.newFixedThreadPool(1);
        pool.execute(clock);

        return true;
    }
    
    /**
     * It stops the clock's thread and goes off the pool when they are activated.     
     * @return TRUE when the pool has been turned off and the thread deactivated, FALSE otherwise
     */
    public synchronized boolean deactivateClock()
    {
        if(clock!=null) clock.turnOff();
        clock=null;
        if(pool!=null) pool.shutdownNow();
        pool=null;

        return true;
    }
    
    /**
     * It goes off the threads related to the  SPCFilter and cleans the buffer
     * @throws SPCException 
     */
    public synchronized void shutdown() throws SPCException
    {
        if(detector!=null) this.detector.turnFilters(false, false);
        deactivateClock();
        this.cleanDataBuffer();
    }

    /**
     * @return the counter
     */
    public long getCounter() {
        return counter;
    }
    
}
