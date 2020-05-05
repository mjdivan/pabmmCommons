/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ciedayap.changedetectors;

/**
 *  It is a specialized class for exceptions related to Detectors
 * 
 * @author Mario Divan
 * @version 1.0
 */
public class DetectorException extends Exception{
    public DetectorException(String mens)
    {
        super(mens);
    }
    
}
