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
package com.stachoodev.suffixarrays;

import com.stachoodev.suffixarrays.wrapper.*;

/**
 * A naive implementation of the LcpSuffixSortingStrategy interface employing
 * Quick Sort for suffix sorting and a home-grown algorithm for LCP computation.
 * The complexity of suffix-sorting is O(N^2logN) and that of LCP finding is
 * O(N^2), which does not present any substantial difference in efficiency when
 * applied to small-scale problems, such as search results clustering.
 * 
 * @author Stanislaw Osinski
 * @version $Revision$
 */
public class QSDualLcpSuffixSortingStrategy implements
    DualLcpSuffixSortingStrategy
{
    /**
     * LCP suffix-sorts the input data using the Quick Sort algorithm.
     */
    public DualLcpSuffixArray dualLcpSuffixSort(MaskableIntWrapper intWrapper)
    {
        int [] suffixData = intWrapper.asIntArray();
        int [] suffixArray = new int [suffixData.length - 1];
        int [] lcpArray = new int [suffixData.length];
        int [] secondaryLcpArray = new int [suffixData.length];

        SuffixArraysUtils.qsSuffixSort(suffixArray, suffixData,
            MaskableIntWrapper.SECONDARY_BITS);

        if (intWrapper instanceof TypeAwareIntWrapper)
        {
            SuffixArraysUtils.naiveLcp(suffixArray, lcpArray, suffixData, 0,
                (TypeAwareIntWrapper) intWrapper);
            SuffixArraysUtils.naiveLcp(suffixArray, secondaryLcpArray,
                suffixData, MaskableIntWrapper.SECONDARY_BITS,
                (TypeAwareIntWrapper) intWrapper);
        }
        else
        {
            SuffixArraysUtils.naiveLcp(suffixArray, lcpArray, suffixData);
            SuffixArraysUtils.naiveLcp(suffixArray, secondaryLcpArray,
                suffixData, MaskableIntWrapper.SECONDARY_BITS);
        }

        return new DualLcpSuffixArray(suffixArray, lcpArray, secondaryLcpArray);
    }
}