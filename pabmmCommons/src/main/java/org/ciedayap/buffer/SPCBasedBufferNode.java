/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ciedayap.buffer;

import java.time.ZonedDateTime;

/**
 * A node containing the measures in a given timestamp to be stored in a SPCBufferNode instance
 * 
 * @author Mario Diván
 * @version 1.0
 */
public class SPCBasedBufferNode {
    /**
     * Timestamp related to the measures contained in the unidimensional array (i.e. values)
     */
    private final ZonedDateTime timestamp;
    /**
     * It is a unidimensional array with measures ordered analogously to the metID array in the corresponding SPCBasedBuffer instance.
     */
    private final Double values[];
    
    /**
     * Initializes the node with the indicated measures, establishing the timestamp to the creation time
     * @param values Measures ordered analogously to the metID array in the corresponding SPCBasedBuffer instance.
     * @throws BufferException It is raised when no values are provided
     */
    public SPCBasedBufferNode(Double values[]) throws BufferException
    {
        if(values==null) throw new BufferException("There not exist values");
        this.values=values;
        timestamp=ZonedDateTime.now();
    }

    /**
     * Initializes the node with the indicated measures and providing a timestamp
     * @param ts The specific timestamp
     * @param values Measures ordered analogously to the metID array in the corresponding SPCBasedBuffer instance.
     * @throws BufferException It is raised when no values are provided
     */
    public SPCBasedBufferNode(ZonedDateTime ts,Double values[]) throws BufferException
    {
        if(values==null) throw new BufferException("There not exist values");
        this.values=values;
        timestamp=ts;
    }
    
    /**
     * It creates a new node with the indicated measures. The timestamp is established to the creation time
     * 
     * @param values Measures ordered analogously to the metID array in the corresponding SPCBasedBuffer instance.
     * @return A new instance with the indicated values
     * @throws BufferException 
     */
    public static synchronized SPCBasedBufferNode create(Double values[]) throws BufferException
    {
        return new SPCBasedBufferNode(values);
    }

    /**
     * It creates a new node with the indicated measures in a specific timestamp
     * @param ts The specific timestamp
     * @param values Measures ordered analogously to the metID array in the corresponding SPCBasedBuffer instance.
     * @return A new instance with the indicated values and timestamp
     * @throws BufferException 
     */
    public static synchronized SPCBasedBufferNode create(ZonedDateTime ts,Double values[]) throws BufferException
    {
        return new SPCBasedBufferNode(ts,values);
    }

    /**
     * @return the timestamp
     */
    public ZonedDateTime getTimestamp() {
        return timestamp;
    }

    /**
     * @return the values
     */
    public Double[] getValues() {
        return values;
    }
    
}
