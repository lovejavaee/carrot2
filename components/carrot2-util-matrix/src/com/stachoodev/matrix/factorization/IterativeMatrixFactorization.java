/*
 * Carrot2 Project
 * Copyright (C) 2002-2004, Dawid Weiss
 * Portions (C) Contributors listed in carrot2.CONTRIBUTORS file.
 * All rights reserved.
 *
 * Refer to the full license file "carrot2.LICENSE"
 * in the root folder of the CVS checkout or at:
 * http://www.cs.put.poznan.pl/dweiss/carrot2.LICENSE
 */
package com.stachoodev.matrix.factorization;

/**
 * @author Stanislaw Osinski
 * @version $Revision$
 */
public interface IterativeMatrixFactorization extends MatrixFactorization
{
    /**
     * Returns approximation error achieved after the last iteration of the
     * algorithm or -1 if the approximation error is not available.
     * 
     * @return approximation error or -1
     */
    public abstract double getApproximationError();

    /**
     * Returns the number of iterations the algorithm has completed.
     * 
     * @return the number of iterations the algorithm has completed
     */
    public abstract int getIterationsCompleted();
}