/*
 * Carrot2 project.
 *
 * Copyright (C) 2002-2012, Dawid Weiss, Stanisław Osiński.
 * All rights reserved.
 *
 * Refer to the full license file "carrot2.LICENSE"
 * in the root folder of the repository checkout or at:
 * http://www.carrot2.org/carrot2.LICENSE
 */

package org.carrot2.dcs;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;

import org.carrot2.core.Document;
import org.carrot2.core.ProcessingResult;
import org.carrot2.log4j.BufferingAppender;
import org.carrot2.util.StreamUtils;
import org.carrot2.util.SystemPropertyStack;
import org.carrot2.util.resource.IResource;
import org.carrot2.util.resource.ResourceLookup;
import org.carrot2.util.resource.ResourceLookup.Location;
import org.carrot2.util.tests.CarrotTestCase;
import org.carrot2.util.tests.UsesExternalServices;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.carrotsearch.randomizedtesting.annotations.ThreadLeakLingering;
import com.carrotsearch.randomizedtesting.annotations.ThreadLeakScope;
import com.carrotsearch.randomizedtesting.annotations.ThreadLeakScope.Scope;
import com.google.common.base.Charsets;
import com.google.common.collect.Maps;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import com.sun.jersey.multipart.FormDataMultiPart;
import com.sun.jersey.multipart.file.StreamDataBodyPart;

/**
 * Test cases for the {@link DcsApp}.
 */
@ThreadLeakLingering(linger = 1000)
@ThreadLeakScope(Scope.SUITE)
public class DcsRestTest extends CarrotTestCase
{
    private static DcsApp dcs;
    private static Client client;
    private static WebResource baseUrl;
    private static WebResource xmlUrl;
    private static WebResource jsonUrl;

    private static SystemPropertyStack appenderProperty;
    private static SystemPropertyStack classpathLocatorProperty;

    private static String RESOURCE_KACZYNSKI_UTF8 = "/xml/carrot2-kaczynski.utf8.xml";
    private static String RESOURCE_KACZYNSKI_UTF16 = "/xml/carrot2-kaczynski.utf16.xml";

    /**
     * Buffered log stream.
     */
    private static BufferingAppender logStream;

    @BeforeClass
    public static void startDcs() throws Throwable
    {
        appenderProperty = new SystemPropertyStack(
            DcsApplication.DISABLE_LOGFILE_APPENDER);
        appenderProperty.push("true");

        classpathLocatorProperty = new SystemPropertyStack(
            DcsApplication.ENABLE_CLASSPATH_LOCATOR);
        classpathLocatorProperty.push("true");

        // Tests run with slf4j-log4j, so attach to the logger directly.
        logStream = BufferingAppender.attachToRootLogger();

        dcs = new DcsApp("dcs");
        dcs.port = 57913;
        try
        {
            dcs.start(System.getProperty("dcs.test.web.dir.prefix"));
        }
        catch (Throwable e)
        {
            dcs = null;
            throw e;
        }

        // Prepare client
        client = Client.create();
        baseUrl = client.resource("http://localhost:" + dcs.port + "/");
        xmlUrl = baseUrl.path("cluster/xml");
        jsonUrl = baseUrl.path("cluster/json");
    }

    @AfterClass
    public static void stopDcs() throws Exception
    {
        dcs.stop();

        BufferingAppender.detachFromRootLogger(logStream);
        logStream = null;

        appenderProperty.pop();
        classpathLocatorProperty.pop();
    }

    @Before
    public void clearLogStream()
    {
        logStream.clear();
    }

    @Before
    public void checkDcsStarted()
    {
        if (dcs == null)
        {
            Assert.fail("DCS not started.");
        }
    }

    @Test
    @UsesExternalServices
    public void getXmlFromExternalSourceDefaultParameters() throws Exception
    {
        assertXmlHasDocumentsAndClusters(xmlUrl.queryParam("query", "test").get(
            String.class));
    }

    @Test
    @UsesExternalServices
    public void getJsonFromExternalSourceDefaultParameters() throws Exception
    {
        assertJsonHasDocumentsAndClusters(jsonUrl.queryParam("query", "test").get(
            String.class));
    }

    @Test
    @UsesExternalServices
    public void postUrlencodedXmlFromExternalSource() throws Exception
    {
        assertXmlHasDocumentsAndClusters(requestExternalSource(Method.POST_URLENCODED,
            xmlUrl));
    }

    @Test
    @UsesExternalServices
    public void postUrlencodedJsonFromExternalSource() throws Exception
    {
        assertJsonHasDocumentsAndClusters(requestExternalSource(Method.POST_URLENCODED,
            jsonUrl));
    }

    @Test
    @UsesExternalServices
    public void postMultipartXmlFromExternalSource() throws Exception
    {
        assertXmlHasDocumentsAndClusters(requestExternalSource(Method.POST_MULTIPART,
            xmlUrl));
    }

    @Test
    @UsesExternalServices
    public void postMultipartJsonFromExternalSource() throws Exception
    {
        assertJsonHasDocumentsAndClusters(requestExternalSource(Method.POST_MULTIPART,
            jsonUrl));
    }

    @Test
    public void postUrlencodedXmlFromStream() throws Exception
    {
        final String result = xmlUrl.type(MediaType.APPLICATION_FORM_URLENCODED).post(
            String.class,
            resourceFormData(RESOURCE_KACZYNSKI_UTF8, Charsets.UTF_8.name()));
        assertXmlHasDocumentsAndClusters(result);
    }

    @Test
    public void postMultipartXmlFromStream() throws Exception
    {
        final String result = xmlUrl.type(MediaType.MULTIPART_FORM_DATA).post(
            String.class, resourceMultiPart(RESOURCE_KACZYNSKI_UTF8));
        assertXmlHasDocumentsAndClusters(result);
    }

    @Test
    public void postMultipartJsonFromStream() throws Exception
    {
        assertJsonHasDocumentsAndClusters(jsonUrl.type(MediaType.MULTIPART_FORM_DATA)
            .post(String.class, resourceMultiPart(RESOURCE_KACZYNSKI_UTF8)));
    }

    @Test
    public void postMultipartWithVariousEncodings() throws Exception
    {
        final ProcessingResult utf16Result = ProcessingResult.deserialize(xmlUrl.type(
            MediaType.MULTIPART_FORM_DATA).post(String.class,
            resourceMultiPart(RESOURCE_KACZYNSKI_UTF16)));
        final ProcessingResult utf8Result = ProcessingResult.deserialize(xmlUrl.type(
            MediaType.MULTIPART_FORM_DATA).post(String.class,
            resourceMultiPart(RESOURCE_KACZYNSKI_UTF8)));

        final List<Document> doc16 = utf16Result.getDocuments();
        final List<Document> doc8 = utf8Result.getDocuments();
        assertThat(doc16.size()).isEqualTo(doc8.size());
        for (int i = 0; i < Math.min(doc16.size(), doc8.size()); i++)
        {
            final Document d1 = doc16.get(i);
            final Document d2 = doc8.get(i);
            assertThat(d1.getTitle()).isEqualTo(d2.getTitle());
            assertThat(d1.getSummary()).isEqualTo(d2.getSummary());
        }
    }

    @Test
    public void queryOverriding() throws Exception
    {
        // Check query from the XML
        FormDataMultiPart multiPart = resourceMultiPart(RESOURCE_KACZYNSKI_UTF8);
        assertXmlHasQuery(
            xmlUrl.type(MediaType.MULTIPART_FORM_DATA).post(String.class, multiPart),
            "kaczyński");

        // Add query override
        final String query = "overridden";
        multiPart = resourceMultiPart(RESOURCE_KACZYNSKI_UTF8);
        multiPart.field("query", query);
        assertXmlHasQuery(
            xmlUrl.type(MediaType.MULTIPART_FORM_DATA).post(String.class, multiPart),
            query);
    }

    @Test
    public void attributeOverriding() throws Exception
    {
        // Check attribute from the XML
        FormDataMultiPart multiPart = resourceMultiPart(RESOURCE_KACZYNSKI_UTF8);
        assertXmlHasAttribute(
            xmlUrl.type(MediaType.MULTIPART_FORM_DATA).post(String.class, multiPart),
            "DocumentAssigner.exactPhraseAssignment", true);

        // Add attribute override
        multiPart = resourceMultiPart(RESOURCE_KACZYNSKI_UTF8);
        multiPart.field("DocumentAssigner.exactPhraseAssignment", "false");
        assertXmlHasAttribute(
            xmlUrl.type(MediaType.MULTIPART_FORM_DATA).post(String.class, multiPart),
            "DocumentAssigner.exactPhraseAssignment", "false");
    }

    @Test
    @UsesExternalServices
    public void getCustomAttributes() throws Exception
    {
        assertXmlHasAttribute(
            requestExternalSource(Method.GET, xmlUrl,
                "DocumentAssigner.exactPhraseAssignment", "true"),
            "DocumentAssigner.exactPhraseAssignment", "true");
    }

    @Test
    @UsesExternalServices
    public void postUrlencodedCustomAttributes() throws Exception
    {
        assertXmlHasAttribute(
            requestExternalSource(Method.POST_URLENCODED, xmlUrl,
                "DocumentAssigner.exactPhraseAssignment", "true"),
            "DocumentAssigner.exactPhraseAssignment", "true");
    }

    @Test
    @UsesExternalServices
    public void postMultipartCustomAttributes() throws Exception
    {
        assertXmlHasAttribute(
            requestExternalSource(Method.POST_MULTIPART, xmlUrl,
                "DocumentAssigner.exactPhraseAssignment", "true"),
            "DocumentAssigner.exactPhraseAssignment", "true");
    }

    @Test
    @UsesExternalServices
    public void getJsonCallback() throws Exception
    {
        assertJsonHasCallback(
            requestExternalSource(Method.GET, jsonUrl, "dcs.json.callback", "cb"), "cb");
    }

    @Test
    @UsesExternalServices
    public void postUrlencodedJsonCallback() throws Exception
    {
        assertJsonHasCallback(
            requestExternalSource(Method.POST_URLENCODED, jsonUrl, "dcs.json.callback",
                "cb"), "cb");
    }

    @Test
    @UsesExternalServices
    public void postMultipartJsonCallback() throws Exception
    {
        assertJsonHasCallback(
            requestExternalSource(Method.POST_MULTIPART, jsonUrl, "dcs.json.callback",
                "cb"), "cb");
    }

    enum Method
    {
        GET, POST_URLENCODED, POST_MULTIPART;
    }

    private String requestExternalSource(Method method, WebResource resource)
    {
        return requestExternalSource(method, resource, new String [0]);
    }

    private String requestExternalSource(Method method, WebResource resource,
        String... attrs)
    {
        assertTrue("Attribute key/values must be paired", attrs.length % 2 == 0);

        final Map<String, String> extraAttrs = Maps.newHashMap();
        for (int i = 0; i < attrs.length / 2; i++)
        {
            extraAttrs.put(attrs[i * 2], attrs[i * 2 + 1]);
        }

        if (!extraAttrs.containsKey("query"))
        {
            extraAttrs.put("query", "test");
        }
        if (!extraAttrs.containsKey("dcs.source"))
        {
            extraAttrs.put("dcs.source", "etools");
        }

        switch (method)
        {
            case GET:
                return resource.queryParams(toMultivalueMap(extraAttrs))
                    .get(String.class);

            case POST_URLENCODED:
                return resource.type(MediaType.APPLICATION_FORM_URLENCODED_TYPE).post(
                    String.class, toMultivalueMap(extraAttrs));

            case POST_MULTIPART:
                final FormDataMultiPart formData = new FormDataMultiPart();
                for (Entry<String, String> entry : extraAttrs.entrySet())
                {
                    formData.field(entry.getKey(), entry.getValue());
                }
                return resource.type(MediaType.MULTIPART_FORM_DATA).post(String.class,
                    formData);
        }

        throw new IllegalArgumentException("Method must not be null");
    }

    private MultivaluedMap<String, String> toMultivalueMap(Map<String, String> map)
    {
        final MultivaluedMap<String, String> result = new MultivaluedMapImpl();
        for (Entry<String, String> entry : map.entrySet())
        {
            result.add(entry.getKey(), entry.getValue());
        }
        return result;
    }

    private MultivaluedMap<String, String> resourceFormData(final String res,
        String encoding) throws IOException
    {
        final MultivaluedMap<String, String> formData = new MultivaluedMapImpl();
        formData.add("dcs.c2stream",
            new String(StreamUtils.readFully(prefetchResource(res)), encoding));
        return formData;
    }

    private FormDataMultiPart resourceMultiPart(final String res) throws IOException
    {
        return (FormDataMultiPart) new FormDataMultiPart()
            .bodyPart(new StreamDataBodyPart("dcs.c2stream", prefetchResource(res)));
    }

    private InputStream prefetchResource(String resource) throws IOException
    {
        final IResource res = new ResourceLookup(Location.CONTEXT_CLASS_LOADER)
            .getFirst(resource);
        assertThat(res).isNotNull();
        return StreamUtils.prefetch(res.open());
    }

    private void assertXmlHasDocumentsAndClusters(final String xml) throws Exception
    {
        final ProcessingResult result = ProcessingResult.deserialize(xml);
        assertThat(result.getDocuments().size()).isGreaterThan(0);
        assertThat(result.getClusters().size()).isGreaterThan(1);
    }

    private void assertXmlHasQuery(final String xml, String expectedQuery)
        throws Exception
    {
        assertXmlHasAttribute(xml, "query", expectedQuery);
    }

    private void assertXmlHasAttribute(final String xml, String key, Object expectedValue)
        throws Exception
    {
        final ProcessingResult result = ProcessingResult.deserialize(xml);
        assertThat(result.getAttribute(key)).isEqualTo(expectedValue);
    }

    private void assertJsonHasDocumentsAndClusters(final String json)
    {
        assertThat(json).contains("\"documents\":").contains("\"clusters\":");
    }

    private void assertJsonHasCallback(final String json, String callback)
    {
        assertThat(json).startsWith(callback + "(").endsWith(")");
    }
}