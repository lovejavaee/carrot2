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
package com.dawidweiss.carrot.core.local;

/**
 * Provides results of query processing.
 * 
 * @see com.dawidweiss.carrot.core.local.LocalController
 * @author Stanislaw Osinski
 * @version $Revision$
 */
public interface ProcessingResult
{
    /**
     * Result of the query exactly as returned by the output component in the
     * chain.
     * 
     * @return result of the query
     */
    public Object getQueryResult();

    /**
     * The {@link RequestContext}associated with the request that produced this
     * {@link ProcessingResult}.
     * 
     * @return The {@link RequestContext}associated with the request that
     *         produced this {@link ProcessingResult}.
     */
    public RequestContext getRequestContext();
}