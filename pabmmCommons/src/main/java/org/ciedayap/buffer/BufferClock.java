/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ciedayap.buffer;

import java.time.ZonedDateTime;
import java.util.Observable;

/**
 * It represents a time limit for transmission. For example, even when no change has been produced
 * in the last five minutes, at least one transmission could be made as proof of life.
 * @author mjdivan
 */
public class BufferClock extends Observable implements Runnable{
    private final long timespan;
    private boolean enabled;
    private ZonedDateTime lastChange;
    private long nanolastChange;
    
    public BufferClock(long tspan)
    {
        timespan=(tspan<1)?1000:tspan;
        enabled=true;
    }
    
    @Override
    public void run() {
        while(enabled)
        {
            boolean exception=false;
            try {
                Thread.sleep(getTimespan());
            } catch (InterruptedException ex) {
                exception=true;
            }
            
            if(!exception)
            {
                nanolastChange=System.nanoTime();
                lastChange=ZonedDateTime.now();
                this.setChanged();
                this.notifyObservers(getNanolastChange());
            }
            else
            {
                this.turnOff();                
            }
            
        }
    }
    
    public synchronized void turnOff()
    {
        enabled=false;
    }

    /**
     * @return the timespan
     */
    public long getTimespan() {
        return timespan;
    }

    /**
     * @return the enabled
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * @return the lastChange
     */
    public ZonedDateTime getLastChange() {
        return lastChange;
    }

    /**
     * @return the nanolastChange
     */
    public long getNanolastChange() {
        return nanolastChange;
    }
    
}
