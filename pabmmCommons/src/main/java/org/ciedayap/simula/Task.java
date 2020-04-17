/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ciedayap.simula;

import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.ciedayap.mair.BDTreeException;
import org.ciedayap.mair.IntegrityRecordException;
import org.ciedayap.mair.TreeNodeException;
import org.ciedayap.mair.utils.InstrumentationAgent;

/**
 *
 * @author mjdivan
 */
public class Task implements Runnable{
    private final ArrayList<Integer> projectLists;
    private final short option;
    private final short nmax;
    private final short maxpower;
    private final InstrumentationAgent ia;

    public Task(ArrayList<Integer> items, short poption,short ma,short power,InstrumentationAgent iag)
    {
        projectLists=items;
        option=poption;
        nmax=ma;
        maxpower=power;        
        ia=iag;
    }
    
    public synchronized Integer pop()
    {
        if(projectLists==null || projectLists.isEmpty()) return null;
        
        return projectLists.remove(0);
    }
    
    public synchronized boolean isEmpty()
    {
        if(projectLists==null) return true;
        
        return projectLists.isEmpty();
    }
    
    @Override
    public void run() {
        
        while(!isEmpty())
        {
            Integer prj=pop();
            
            try {
                switch(this.option)
                {
                    case 3:
                        KbSJ.sim_mt_creationByProject(prj,this.nmax,this.maxpower,ia);//It runs the simulation limited to the given number of prj
                        break;
                    default:
                        System.out.println("Wrong option!!");
                }
            } catch (BDTreeException | IntegrityRecordException | NoSuchAlgorithmException | TreeNodeException ex) {
                Logger.getLogger(KbSJ.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
}
