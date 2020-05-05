/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ciedayap.mair.utils;

import java.util.concurrent.ConcurrentLinkedQueue;

/**
 *
 * @author mjdivan
 */
public class SPCFilterMeasurementRecord {
    private final ConcurrentLinkedQueue<MeasurementNode> compute;
    private final ConcurrentLinkedQueue<MeasurementNode> addMeasure;
    
    public SPCFilterMeasurementRecord()
    {
        compute=new ConcurrentLinkedQueue();
        addMeasure=new ConcurrentLinkedQueue();
    }
    
    /**
     * It returns an instance where the measures are kept using a queue
     * @return A new instance of the measurement record
     */
    public static synchronized SPCFilterMeasurementRecord create()
    {
        return new SPCFilterMeasurementRecord();
    }
    
    /**
     * It incorporates a new measurement in the queue. opTime or opSize must be a value.
     * @param opTime The calculated operation time
     * @param opSize The calculate operation size (optional)
     */
    public synchronized void addComputeOp(Long opTime,Long opSize)
    {
        if(opTime==null && opSize==null) return;
        
        getCompute().add(MeasurementNode.create(opTime, opSize));
    }
    
    /**
     * It incorporates a new measurement in the queue. opTime or opSize must be a value.
     * @param opTime The calculated operation time
     * @param opSize The calculate operation size (optional)
     */
    public synchronized void addMeasureOp(Long opTime,Long opSize)
    {
        if(opTime==null && opSize==null) return;
        
        getAddMeasure().add(MeasurementNode.create(opTime, opSize));
    }

    /**
     * @return the compute
     */
    public ConcurrentLinkedQueue<MeasurementNode> getCompute() {
        return compute;
    }

    /**
     * @return the addMeasure
     */
    public ConcurrentLinkedQueue<MeasurementNode> getAddMeasure() {
        return addMeasure;
    }
    
    
}
