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
package com.dawidweiss.carrot.core.local.clustering;

import com.dawidweiss.carrot.core.local.linguistic.tokens.*;

/**
 * Represents a tokenized snippet of a document. Does <b>not </b> provide
 * document's content.
 * 
 * @author Stanislaw Osinski
 * @version $Revision$
 */
public class TokenizedDocumentSnippet extends TokenizedDocumentBase
{
    /** Thie snippet's title */
    private TokenSequence title;

    /** This snippets id */
    private String id;

    /** This snippets score */
    private float score;

    /**
     * Creates a new <code>TokenizedDocumentSnippet</code> with a
     * <code>null</code> id and url. The score is set to -1.
     * 
     * @param title
     * @param snippet
     */
    public TokenizedDocumentSnippet(TokenSequence title, TokenSequence snippet)
    {
        this(null, title, snippet, null, -1);
    }

    /**
     * Creates a new <code>TokenizedDocumentSnippet</code> with given id,
     * title, snippet, URL and score.
     * 
     * @param id
     * @param title
     * @param snippet
     * @param url
     * @param score
     */
    public TokenizedDocumentSnippet(String id, TokenSequence title,
        TokenSequence snippet, String url, float score)
    {
        this.id = id;
        this.title = title;
        this.score = score;
        propertyHelper.setProperty(PROPERTY_SNIPPET, snippet);
        propertyHelper.setProperty(PROPERTY_URL, url);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.dawidweiss.carrot.util.tokenizer.TokenizedDocumentBase#getSnippet()
     */
    public TokenSequence getSnippet()
    {
        return (TokenSequence) propertyHelper.getProperty(PROPERTY_SNIPPET);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.dawidweiss.carrot.core.local.clustering.TokenizedDocument#getId()
     */
    public Object getId()
    {
        return id;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.dawidweiss.carrot.core.local.clustering.TokenizedDocument#getUrl()
     */
    public String getUrl()
    {
        return (String) propertyHelper.getProperty(PROPERTY_URL);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.dawidweiss.carrot.core.local.clustering.TokenizedDocument#getTitle()
     */
    public TokenSequence getTitle()
    {
        return title;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.dawidweiss.carrot.core.local.clustering.TokenizedDocument#getScore()
     */
    public float getScore()
    {
        return score;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    public String toString()
    {
        String title = "";
        if (getTitle() != null)
        {
            title = getTitle().toString();
        }

        String snippet = "";
        if (getSnippet() != null)
        {
            snippet = getSnippet().toString();
        }
        
        return "[" + title + "] " + snippet;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object obj)
    {
        if (obj == this)
        {
            return true;
        }

        if (obj == null)
        {
            return false;
        }

        if (obj.getClass() != getClass())
        {
            return false;
        }

        TokenizedDocumentSnippet otherSnippet = (TokenizedDocumentSnippet) obj;
        boolean result = true;
        if (title != null)
        {
            result = result && title.equals(otherSnippet.title);
        }
        if (otherSnippet != null)
        {
            result = result && getSnippet().equals(otherSnippet.getSnippet());
        }

        return result && propertyHelper.equals(otherSnippet.propertyHelper);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
     */
    public int hashCode()
    {
        // Try with the title first
        if (title != null && title.getLength() > 0)
        {
            return title.hashCode();
        }

        // Snippet?
        if (getSnippet() != null && getSnippet().getLength() > 0)
        {
            return getSnippet().hashCode();
        }

        return propertyHelper.hashCode();
    }
}