/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ciedayap.buffer;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Observable;
import org.ciedayap.changedetectors.DetectorException;
import org.ciedayap.changedetectors.SPCProjectDetector;
import org.ciedayap.filters.SPCException;
import org.ciedayap.pabmm.sh.SimilarityTriangularMatrix;
import org.ciedayap.stat.IncrementalCovMatrix;

/**
 * This class extends the SPCBasedBuffer functionality, incorporating
 * the posibility to estimate mean, variance, and covariance matrix incrementally.
 * 
 * @author Mario José Diván
 * 
 */
public class SPCMeansBasedBuffer extends SPCBasedBuffer {
    //Cumulators useful to estimate the mean
    private double accumulators[];
    //Cumulators of differences to estimate variance, covariation, and correlation matrix
    private IncrementalCovMatrix covMatrix;
    /**
     * The Number of deviations to be used as retaining limit in reference to Z-Scores
     */
    private double deviationLimit=2.0;
    /**
     * It is the acceptance threshold used as a reference to retain or discard a data vector.
     * Each variable's value is analyzed whether it exceeds the deviation limit or not. 
     * All those case where the deviation limit is exceed are identified, and their Z-scores are multiplied with the
     * metric's weighting. When the weighted sum exceeds this value, the data vector is discard and it is not considered
     * to be added in the data buffer.
     */
    private double acceptanceThreshold=3.0;
    /**
     * When this flag is TRUE, the temporal barrier is deactivated while the data are added to the data buffer
     * if and only if they do not exceeds the acceptanceThreshold.
     */
    private boolean loadShedding=false;
        
    public SPCMeansBasedBuffer(String projectID, ArrayList<String> attributes, long max, Double maxSPCTolerance, boolean verbose) 
            throws BufferException, DetectorException, SPCException {
        super(projectID, attributes, max, maxSPCTolerance, verbose);
        
        if(metID==null || metID.length==0)
            throw new BufferException("No metricIDs to create the cumulators");
        
        accumulators=new double[metID.length];
        Arrays.fill(accumulators, (Double)0.0);        
        try {
            covMatrix=IncrementalCovMatrix.create(projectID, metID);
        } catch (Exception ex) {
            throw new SPCException("The covMatrix had not been created");
        }
    }
    
    /**
     * Default factory method for the buffer
     * 
     * @param projectID The associated projectID
     * @param attributes The list contaning the set of "attributeID;metricID;weight"
     * @param max The max number of measures
     * @param maxSPCTolerance The threshold established. When it is exceeded, an alarm is raised from the SPCProjectDetector and it is restarted (Default: 5)
     * @param verbose
     * @return A new and initialized instance of the buffer
     * @throws BufferException
     * @throws DetectorException
     * @throws SPCException 
     */
    public synchronized static SPCMeansBasedBuffer create(String projectID,ArrayList<String> attributes,long max,Double maxSPCTolerance, boolean verbose) 
                throws BufferException, DetectorException, SPCException 
    {
        return new SPCMeansBasedBuffer(projectID,attributes,max,maxSPCTolerance,verbose); 
    }
    
    @Override
    public synchronized void cleanDataBuffer()  
    {
        super.cleanDataBuffer();
        Arrays.fill(getAccumulators(), (Double)0.0);
        getCovMatrix().restart();
    }
        
    
    @Override
    public synchronized void addMeasures(double values[]) throws BufferException
    {
        addMeasures(ZonedDateTime.now(),values);
    }
    
    @Override
    public synchronized void addMeasures(ZonedDateTime ts,double values[]) throws BufferException
    {        
        if(this.loadShedding)
        {
            if(!this.evaluate(values)) return;//Out of retention criteria
        }
        
        SPCMeansBasedBufferNode node=SPCMeansBasedBufferNode.create(ts, values);
        if(node==null) return;
        
        if((this.getCounter()+1)<this.maxMeasures)
        {
            double means[]=new double[getAccumulators().length];
            
            for(int i=0;i<accumulators.length;i++)
            {
                this.accumulators[i]+=values[i];
                means[i]= this.getAccumulators()[i] / ((Double)(queue.size()+1.0));
            }

            node.setEstimatedMeans(means);//it updates the means and differences                
                                    
            if(queue.add(node))
            {
                counter++;
                for(int i=0;i<metID.length;i++)
                {
                    this.detector.addMeasure(metID[i], values[i]);
                }
                
                //Covariance (cummulate the variations)
                this.getCovMatrix().addDifferences((long)queue.size(), node.getDifferences());                
            }
            else
            {//Rest the accumulators because the node was not added
                for(int i=0;i<this.getAccumulators().length;i++) accumulators[i]-=values[i];
            }
        }
        else
        {
            //Acumular
            //restar el primero que sale
            double means[]=new double[accumulators.length];                        
            SPCMeansBasedBufferNode removed=(SPCMeansBasedBufferNode) queue.poll();

            //Discount the  cumulators                
            double oldValues[]=(removed!=null)?removed.getValues():null;
            for(int i=0; i<accumulators.length;i++) 
              {
                accumulators[i]=accumulators[i]-oldValues[i]+values[i];
                
                means[i]=accumulators[i]/((Double)(queue.size()+1.0));
              }  
            node.setEstimatedMeans(means);            
            
            if(queue.add(node))
            {
                counter++;
                for(int i=0; i<metID.length; i++)
                {
                    this.detector.addMeasure(metID[i], values[i]);
                }    
                
                //Covariance (cummulate the variations)                   (-)                       (+)
                if(removed!=null && removed.getDifferences()!=null)
                    this.getCovMatrix().addDifferences((long)queue.size(), removed.getDifferences(), node.getDifferences());
                else
                    this.getCovMatrix().addDifferences((long)queue.size(), node.getDifferences());                                
            }         
            else
            {//Rest the accumulators because the node was not added
                for(int i=0;i<accumulators.length;i++) accumulators[i]-=values[i];                
            }
        }        
    }
    

    /**
     * It returns the estimated means
     * @return 
     */
    public synchronized double[] getEstimatedMeans()
    {
        if(getAccumulators()==null) return null;        
        double means[]=new double[this.getAccumulators().length];
        if(means==null) return null;
        
        double myN=(double)queue.size();
        
        for(int i=0;i<getAccumulators().length;i++)
        {
            means[i]=getAccumulators()[i]/myN;
        }
        
        return means;
    }

    /**
     * It returns the standard deviations of each metric organized by a vector
     * @return 
     */
    public synchronized double[] getEstimatedSD()
    {
        if(getAccumulators()==null) return null;
        if(covMatrix==null) return null;
        double sd[]=new double[this.getAccumulators().length];
        if(sd==null) return null;        
        
        for(int i=0;i<getAccumulators().length;i++)
        {
            Double val=covMatrix.getDeviation(i);
            if(val==null) return null;
            
            sd[i]=val;
        }
        
        return sd;
    }
    
    /**
     * @return the covMatrix
     */
    public IncrementalCovMatrix getCovMatrix() {
        return covMatrix;
    }
    
    /**
     * @return the accumulators
     */
    public double[] getAccumulators() {
        return accumulators;
    } 
    
    /**
     * It will retain the measure with the exception of the weighted sum
     * exceeds a threshold. In the last case, the measure is discarded.
     * 
     * @param values The vector with values to be added
     * @return TRUE when the measure is retained and incorporated to the data buffer, false otherwise
     */
    protected boolean evaluate(double values[])
    {
        if(values==null) return false;
        if(covMatrix.getN()<2) return true;//There is not data, let it proceeds
        
        double means[]=this.getEstimatedMeans();
        double deviations[]=this.getEstimatedSD();
        
        //It could be the first data, for that reason, the arithmetic means and deviations are not available
        if(means==null || deviations==null) return true;
        
        double acc=0;
        for(int i=0;i<values.length;i++)
        {
            double z=((values[i]-means[i])/deviations[i]);
            
            if(z>deviationLimit)
            {
                acc+=(z*this.weighting[i]);
            }
        }

        return !(acc>acceptanceThreshold);
    }
    
    /**
     * @return the deviationLimit
     */
    public double getDeviationLimit() {
        return deviationLimit;
    }

    /**
     * @param deviationLimit the deviationLimit to set
     */
    public synchronized void setDeviationLimit(double deviationLimit) {
        this.deviationLimit = deviationLimit;
    }

    /**
     * @return the acceptanceThreshold
     */
    public double getAcceptanceThreshold() {
        return acceptanceThreshold;
    }

    /**
     * @param acceptanceThreshold the acceptanceThreshold to set
     */
    public synchronized void setAcceptanceThreshold(double acceptanceThreshold) {
        this.acceptanceThreshold = acceptanceThreshold;
    }

    /**
     * @return the loadShedding
     */
    public boolean isLoadShedding() {
        return loadShedding;
    }

    /**
     * @param loadShedding the loadShedding to set
     */
    public synchronized void setLoadShedding(boolean loadShedding) {
        this.loadShedding = loadShedding;        
    }
    
    @Override
    public void update(Observable o, Object arg) {
        boolean notify=false;
        String message="-";
        if(o instanceof SPCProjectDetector)
        {//The data change alarms always will be considered (be load shedding activated or not)
            notify=true;
            message=("[Alarm -Detected Change-] Informing...ready to transmit-> "+ZonedDateTime.now()+" Counter: "+counter);            
        }
        else
        {
            if(o instanceof BufferClock)
            {//
                if(!loadShedding)//The temporal barrier is used only when the load shedding is deactivated
                {
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
        }
        
        if(notify)
        {
            readyToTransmit=true;//SPCFilter has detected a change in the data series                
        
            this.setChanged();
            this.notifyObservers(message);
        }
    }
    
    //1. When the load shedding need to be activated by signal from GF
    //2.a Temporal barrier is ommitted, only data change detection is considered for transmission +
    //2.b Sum(weighting-i*Zscore-i)>=threshold ..The measure is discarded **incremental**
    //Measuring the error in the incremental calculus of variance, mean, deviations (graphic evol values jointly avg-desv)
    //Measuring operation times and memory consumption!!!
    public static void main(String args[]) throws BufferException, DetectorException, SPCException, Exception
    {
        ArrayList<String> atts=new ArrayList();
        StringBuilder sb=new StringBuilder();
        for(int i=0;i<5;i++)
        {
            sb.append("att").append(i).append(";")
              .append("met").append(i).append(";")
              .append("0.2");
            atts.add(sb.toString());
            sb.delete(0, sb.length());
        }
        
        SPCMeansBasedBuffer buffer=SPCMeansBasedBuffer.create("projectID",atts,15,20.0,false);             
        
        for(int i=0;i<120;i++)
        {
            double values[]=new double[5]; 
            values[0]=10.0+i;
            values[1]=10000.0+(i*2);
            values[2]=5000.0+(i* 3);
            values[3]=100.0+i;
            values[4]=300.0+2*i;
            
            sb.append(values[0]).append(";")
               .append(values[1]).append(";")
               .append(values[2]).append(";")
               .append(values[3]).append(";")
               .append(values[4]);
            System.out.println(sb.toString());
            sb.delete(0, sb.length());
            
            buffer.addMeasures(values);
            Thread.sleep(500);
        }
        
        double means[]=buffer.getEstimatedMeans();
        double sd[]=buffer.getEstimatedSD();
        double accumulators[]=buffer.getAccumulators();
        
        IncrementalCovMatrix covMat = buffer.getCovMatrix();

        for(int i=0;i<5;i++)
        {
            System.out.println("Mean"+i+": "+means[i]+" sd"+i+": "+sd[i]+" ACC"+i+": "+accumulators[i]);
        }

        buffer.getBufferDataStream().forEach(nod->{
            SPCMeansBasedBufferNode nod2=(SPCMeansBasedBufferNode)nod;
            double pmeans[]=nod2.getEstimatedMeans();
            double psd[]=buffer.getEstimatedSD();
            
            StringBuilder x=new StringBuilder();
            double val[]=nod2.getValues();
            
            x.append(val[0]).append(";")
                   .append(val[1]).append(";")
                   .append(val[2]).append(";")
                   .append(val[3]).append(";")
                   .append(val[4]).append(";")
                   .append("M: "+pmeans[0]).append(";") 
                   .append("M: "+pmeans[1]).append(";") 
                   .append("M: "+pmeans[2]).append(";") 
                   .append("M: "+pmeans[3]).append(";") 
                   .append("M: "+pmeans[4]);
                          
            System.out.println(x.toString());
            x.delete(0, x.length());                
        });
        
        Object data[]=buffer.getBufferData();
        System.out.println("***Records: "+data.length);
        
        double v1[]={10.0, 10000.0, 5000.0, 100.0, 102.0};
        double v2[]={14.0, 10005.0, 5050.0, 10.0, 102.0};

        SimilarityTriangularMatrix coV = covMat.getCovarianceMatrix();
        for(int i=0;i<coV.getDim();i++)
        {
            for(int j=0;j<coV.getDim();j++)
            {
                sb.append(coV.get(i, j)).append("\t");
            
                System.out.print(sb.toString());
                sb.delete(0, sb.length());
            }
            System.out.println();
        }
        System.out.println();
        SimilarityTriangularMatrix cor = covMat.getCorrelationMatrix();
        for(int i=0;i<cor.getDim();i++)
        {
            for(int j=0;j<cor.getDim();j++)
            {
                sb.append(cor.get(i, j)).append("\t");
            
                System.out.print(sb.toString());
                sb.delete(0, sb.length());
            }
            System.out.println();
        }
        
        //double dist=DistanceCalculator.mahanalobisDistance(covMat.getCovarianceRealMatrix(), v1, v2);
        //System.out.println("M-Distance: "+dist);
        
        buffer.shutdown();
    }    
   
}
