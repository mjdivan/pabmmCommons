/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ciedayap.stat;

import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.ciedayap.pabmm.sh.SimilarityTriangularMatrix;

/**
 * This class provides a way to compute the covariance matrix incrementally following the 
 * incorporation order of the data in a given data buffer instance
 * 
 * @author Mario  José Diván
 */
public class IncrementalCovMatrix{
    private final String projectID;
    /**
     * The set of metricID in the same project's order. The positioning is synchornized with attID and weighting
     */
    private final String metsID[];
    
    /**
     * Estimated Covariance Matrix
     */
    private SimilarityTriangularMatrix covMatrixAccu;
    /**
     * It indicates the number of processed measures
     */
    private long N=0;
    
    /**
     *  It creates a tringular covariance matrix
     * @param projectID The associated projectID
     * @param metsID the array of metricsID in the same order as it was provided in the data buffer
     * @throws StatException It is raised when the projectID or metricsID is invalid, or even, when the triangular matrix has not been created
     * @throws Exception
     */
    public IncrementalCovMatrix(String projectID,String metsID[]) throws StatException, Exception
    {
        if(projectID==null || projectID.trim().length()==0)
            throw new StatException("There is not a project ID");
        if(metsID==null || metsID.length<2)
            throw new StatException("MetricsID is null or it contains lesser than 2 metrics");
            
        this.projectID=projectID;
        this.metsID=metsID;       

        covMatrixAccu=SimilarityTriangularMatrix.createSimilarityTriangularMatrix(metsID.length, (Double) 0.0);
        if(covMatrixAccu==null) throw new StatException("The Triangular Matrix could not be created");                
    }
    
    /**
     * Default static creation method
     * @param projectID The project identifier
     * @param metsID The set of metric's IDs under monitoring
     * @return A new instance of IncrementalCovMatrix
     * @throws Exception It is raised when the projectID or metricsID is invalid, or even, when the triangular matrix has not been created
     */
    public static synchronized IncrementalCovMatrix create(String projectID,String metsID[]) throws Exception
    {
        return new IncrementalCovMatrix(projectID,metsID);
    }
    
    /**
     * It increments each region of the matrix using the differences between x and its mean
     * @param qelements The total number of elements including the differences to add
     * @param diff The differences between the x and its mean
     * @return TRUE when the differences updated all the matrix, FALSE otherwise
     */
    public boolean addDifferences(long qelements,double diff[])
    {
        if(qelements<1) return false;
        if(diff==null) return false;
        if(diff.length!=metsID.length) return false;
        this.N=qelements;
        
        for(int i=0;i<metsID.length;i++)
            for(int j=i;j<metsID.length;j++)
            {
                double previous=covMatrixAccu.get(i, j);
                previous+=(diff[i]*diff[j]);
                
                covMatrixAccu.set(i, j, previous);
            }
        
        return true;
    }
    
    /**
     * It increments each region of the matrix using the differences between x and its mean
     * @param qelements The total number of elements including the differences to add
     * @param rest_diff The differences between the x and its mean to be removed from the matrix
     * @param add_diff The differences between the x and its mean to be added to the matrix
     * @return TRUE when the differences updated all the matrix, FALSE otherwise
     */
    public boolean addDifferences(long qelements,double rest_diff[], double add_diff[])
    {
        if(qelements<1) return false;
        if(rest_diff==null || add_diff==null) return false;
        if(rest_diff.length!=metsID.length || add_diff.length!=metsID.length) return false;
        this.N=qelements;
        
        for(int i=0;i<metsID.length;i++)
            for(int j=i;j<metsID.length;j++)
            {
                double previous=covMatrixAccu.get(i, j);
                previous-= (rest_diff[i]*rest_diff[j]);
                previous+= (add_diff[i]*add_diff[j]);
                
                covMatrixAccu.set(i, j, previous);
            }
        
        return true;
    }
    
    /**
     * This method returns the covariance matrix
     * @return The covariance matrix related to metrics.
     * @throws Exception 
     */
    public synchronized SimilarityTriangularMatrix getCovarianceMatrix() throws Exception
    {
        return this.covMatrixAccu.dividedBy((double)getN());        
    }

    /**
     * It returns the covariance matrix as a RealMatrix instance
     * @return A org.apache.commons.math3.linear.RealMatrix instance
     * @throws Exception
     */
    public synchronized RealMatrix getCovarianceRealMatrix() throws Exception
    {
        SimilarityTriangularMatrix cov=getCovarianceMatrix();
        if(cov==null) return null;
        
        RealMatrix rm=MatrixUtils.createRealMatrix(cov.getDim(), cov.getDim());
        
        for(int i=0;i<cov.getDim();i++)
            for(int j=i;j<cov.getDim();j++)
            {
                if(i==j) rm.setEntry(i, j, cov.get(i, j));
                else
                {
                    rm.setEntry(i, j, cov.get(i, j));
                    rm.setEntry(j, i, cov.get(j, i));
                }                
            }
        
        return rm;
    }

    /**
     * This methid returns the correlation matrix
     * @return The correlation matrix related to metrics
     * @throws Exception 
     */
    public synchronized SimilarityTriangularMatrix getCorrelationMatrix() throws Exception
    {
        //a New matrix where DESVxy / (DESVx*DESVy) **The FORMULA needs to be reviewed*
        SimilarityTriangularMatrix cov=getCovarianceMatrix();
        SimilarityTriangularMatrix corr=SimilarityTriangularMatrix.createSimilarityTriangularMatrix(cov.getDim());
        
        for(int i=0;i<cov.getDim();i++)
        {
            double prod1=Math.sqrt(cov.get(i, i));
            for(int j=i;j<cov.getDim();j++)
            {
                if(i==j) corr.set(i, j, 1.0);
                else
                {    
                    double pcor_n=cov.get(i, j);
                    double pcor_d=prod1*Math.sqrt(cov.get(j, j));

                    if(pcor_d==0) return null;

                    corr.set(i, j, pcor_n/pcor_d);
                }
            }
        }
        
        return corr;
    }
   
    /**
     * It returns an estimation of the variance based on the last processed measures
     * @param i The metric index based on the data buffer
     * @return The variance when it is available, null otherwise
     */
    public synchronized Double getVariance(int i)
    {
        if(i<0 || i>=metsID.length) return null;
        if(covMatrixAccu==null || getN()<1) return null;
        
        return (covMatrixAccu.get(i, i)/(double)getN()-1);
    }
    
    /**
     * It obtains an estimated deviation from the processed measures
     * @param i The metric index based on the data buffer
     * @return The standard deviation when it is available, null otherwise
     */
    public synchronized Double getDeviation(int i)
    {
        Double var=getVariance(i);
        
        return (var==null)?null:Math.sqrt(var);
    }
    
    /**
     * It returns the Z-score given a mean and standard deviation
     * @param value The value to be standarized
     * @param mean The arithmetic mean
     * @param sd The standard deviation
     * @return The Z-score obtained given the mentioned parameters, null otherwise
     */
    public static synchronized Double zscore(double value, double mean, double sd)
    {
        if(sd==0) return null;
        
        return (value-mean)/sd;
    }
    
    /**
     * It keeps the projectID, metricsID, and covariance matrix structure, but it 
     * restarts all the acumulators to zero
     * @return TRUE when all the elements have been restarted, false otherwise.
     */
    public synchronized boolean restart()
    {
        if(covMatrixAccu==null) return false;
        
        this.covMatrixAccu.reinitialize(0.0);
        this.N=0;
        return true;
    }

    /**
     * @return the N
     */
    public synchronized long getN() {
        return N;
    }
}
