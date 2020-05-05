/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ciedayap.filters;

/**
 * It represents an specialized class for managing the specific exceptions
 * 
 * @author Mario Diván
 * @version 1.0
 */
public class SPCException extends Exception{
    public SPCException()
    {
        super();
    }
    
    public SPCException(String me)
    {
        super(me);
    }
}
