/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ciedayap.buffer;

import java.time.ZonedDateTime;
import org.apache.storm.shade.org.apache.commons.lang.ArrayUtils;

/**
 * It containes an specialization of the SPCBufferNode to contain the estimated means of each variable
 * at the time in which it is incorporated
 * @author Mario Divan
 * @version 1.0
 */
public class SPCMeansBasedBufferNode extends SPCBasedBufferNode{
    /**
     * The estimated means
     */
    private double estimatedMeans[];
    /**
     * Differencee between the value and the estimated mean (x-avg)
     */
    private double differences[];
    
    public SPCMeansBasedBufferNode(double[] values, double emeans[]) throws BufferException {
        super(values);
        
        if(emeans==null || emeans.length==0) throw new BufferException("There not exist estimated means");
        if(emeans.length!=values.length) throw new BufferException("There  is a difference in the length of the estimated means and values");
        
        estimatedMeans=emeans;
        if(differences==null) differences=new double[estimatedMeans.length];
               
        for(int i=0;i<estimatedMeans.length;i++)
            differences[i]=this.values[i]-estimatedMeans[i];
    }
 
    public SPCMeansBasedBufferNode(ZonedDateTime ts,double values[], double emeans[]) throws BufferException
    {
        super(ts,values);
        if(emeans==null) throw new BufferException("There not exist estimated means");
        if(emeans.length!=values.length) throw new BufferException("There  is a difference in the length of the estimated means and values");
        
        estimatedMeans=emeans;
        if(differences==null) differences=new double[estimatedMeans.length];
               
        for(int i=0;i<estimatedMeans.length;i++)
            differences[i]=this.values[i]-estimatedMeans[i];
    }    

    /**
     * It creates a new node with the indicated measures in a specific timestamp. Because no means are provided, the same
     * values will be used as estimated averages.
     * @param ts The specific timestamp
     * @param values Measures ordered analogously to the metID array in the corresponding SPCBasedBuffer instance.
     * @return A new instance with the indicated values and timestamp
     * @throws BufferException 
     */
    public static synchronized SPCMeansBasedBufferNode create(ZonedDateTime ts,double values[]) throws BufferException
    {        
        return new SPCMeansBasedBufferNode(ts,values, ArrayUtils.clone(values));
    }

    /**
     * It creates a new node with the indicated measures in a specific timestamp. 
     * @param ts The specific timestamp
     * @param values Measures ordered analogously to the metID array in the corresponding SPCBasedBuffer instance.
     * @param emeans The estimated means related to each measure
     * @return A new instance with the indicated values and timestamp
     * @throws BufferException 
     */
    public static synchronized SPCMeansBasedBufferNode create(ZonedDateTime ts,double values[], double emeans[]) throws BufferException
    {
        return new SPCMeansBasedBufferNode(ts,values, emeans);
    }
    
    /**
     * @return the estimatedMeans
     */
    public double[] getEstimatedMeans() {
        return estimatedMeans;
    }

    /**
     * @return the differences
     */
    public double[] getDifferences() {        
        return differences;
    }

    /**
     * @param estimatedMeans the estimatedMeans to set and it updates the differences' array
     */
    public void setEstimatedMeans(double[] estimatedMeans) {
        this.estimatedMeans = estimatedMeans;
        
        if(differences==null) differences=new double[estimatedMeans.length];
               
        for(int i=0;i<estimatedMeans.length;i++)
            differences[i]=this.values[i]-estimatedMeans[i];        
    }

}
