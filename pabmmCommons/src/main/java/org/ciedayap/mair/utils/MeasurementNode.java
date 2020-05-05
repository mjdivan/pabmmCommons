/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ciedayap.mair.utils;

import java.time.ZonedDateTime;

/**
 *
 * @author mjdivan
 */
public class MeasurementNode {
    private ZonedDateTime timestamp;
    private long nanoTime;
    private Long consumedOperationTime;
    private Long consumedsize;
    
    public MeasurementNode(Long opTime,Long opSize)
    {
        timestamp=ZonedDateTime.now();
        nanoTime=System.nanoTime();
        this.consumedOperationTime=opTime;
        this.consumedsize=opSize;
    }
    
    public synchronized static MeasurementNode create(Long opTime,Long opSize)
    {
        return new MeasurementNode(opTime,opSize);
    }

    /**
     * @return the timestamp
     */
    public ZonedDateTime getTimestamp() {
        return timestamp;
    }

    /**
     * @return the nanoTime
     */
    public long getNanoTime() {
        return nanoTime;
    }

    /**
     * @return the consumedOperationTime
     */
    public Long getConsumedOperationTime() {
        return consumedOperationTime;
    }

    /**
     * @return the consumedsize
     */
    public Long getConsumedsize() {
        return consumedsize;
    }
    
}
