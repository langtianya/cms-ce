package com.enonic.cms.itest.search;

import org.junit.Test;

import com.enonic.cms.core.content.index.ContentIndexQuery;
import com.enonic.cms.core.content.resultset.ContentResultSet;
import com.enonic.cms.core.search.result.DateHistogramFacetResultSet;
import com.enonic.cms.core.search.result.FacetResultSet;

import static org.junit.Assert.*;

public class ContentIndexServiceImpl_facetDateHistogramFacetTest
    extends ContentIndexServiceFacetTestBase
{

    @Test
    public void minute_interval()
    {
        createAndIndexContent( 1, "2012-01-01", "data.myDate" );
        createAndIndexContent( 2, "2012-01-01 12:00", "data.myDate" );
        createAndIndexContent( 3, "2012-01-02", "data.myDate" );
        createAndIndexContent( 4, "2012-01-03 23:59:59", "data.myDate" );
        createAndIndexContent( 5, "2012-01-04 10:00", "data.myDate" );
        createAndIndexContent( 6, "2012-01-04 12:00:42", "data.myDate" );
        createAndIndexContent( 7, "2012-01-04 14:10", "data.myDate" );
        createAndIndexContent( 8, "2012-01-04 16:01:59", "data.myDate" );

        flushIndex();

        ContentIndexQuery query = new ContentIndexQuery( "" );
        final String facetDefinition = "<facets>\n" +
            "    <date-histogram name=\"myHistogramFacet\">\n" +
            "        <index>data.myDate</index>\n" +
            "        <interval>minute</interval>\n" +
            "    </date-histogram>\n" +
            "</facets>\n";

        query.setFacets( facetDefinition );
        final ContentResultSet result = contentIndexService.query( query );

        final FacetResultSet next = result.getFacetsResultSet().iterator().next();
        assertNotNull( next );
        assertTrue( next instanceof DateHistogramFacetResultSet );
        DateHistogramFacetResultSet histogramFacetResultSet = (DateHistogramFacetResultSet) next;
        //assertEquals( 4, histogramFacetResultSet.getResultEntries().size() );

        String expectedXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<content>\n" +
            "  <facets>\n" +
            "    <date-histogram name=\"myHistogramFacet\">\n" +
            "      <interval count=\"1\">2012-01-01 00:00:00</interval>\n" +
            "      <interval count=\"1\">2012-01-01 12:00:00</interval>\n" +
            "      <interval count=\"1\">2012-01-02 00:00:00</interval>\n" +
            "      <interval count=\"1\">2012-01-03 23:59:00</interval>\n" +
            "      <interval count=\"1\">2012-01-04 10:00:00</interval>\n" +
            "      <interval count=\"1\">2012-01-04 12:00:00</interval>\n" +
            "      <interval count=\"1\">2012-01-04 14:10:00</interval>\n" +
            "      <interval count=\"1\">2012-01-04 16:01:00</interval>\n" +
            "    </date-histogram>\n" +
            "  </facets>\n" +
            "</content>";

        createAndCompareResultAsXml( result, expectedXml );
    }

    @Test
    public void two_minute_interval()
    {
        createAndIndexContent( 1, "2012-01-01", "data.myDate" );
        createAndIndexContent( 2, "2012-01-01 00:00:01", "data.myDate" );
        createAndIndexContent( 3, "2012-01-01 00:01:30", "data.myDate" );
        createAndIndexContent( 4, "2012-01-01 00:01:59", "data.myDate" );
        createAndIndexContent( 5, "2012-01-01 00:02:00", "data.myDate" );
        createAndIndexContent( 6, "2012-01-01 01:00:00", "data.myDate" );
        createAndIndexContent( 7, "2012-01-01 01:01:59", "data.myDate" );

        flushIndex();

        ContentIndexQuery query = new ContentIndexQuery( "" );
        final String facetDefinition = "<facets>\n" +
            "    <date-histogram name=\"myHistogramFacet\">\n" +
            "        <index>data.myDate</index>\n" +
            "        <interval>2m</interval>\n" +
            "    </date-histogram>\n" +
            "</facets>\n";

        query.setFacets( facetDefinition );
        final ContentResultSet result = contentIndexService.query( query );

        final FacetResultSet next = result.getFacetsResultSet().iterator().next();
        assertNotNull( next );
        assertTrue( next instanceof DateHistogramFacetResultSet );
        DateHistogramFacetResultSet histogramFacetResultSet = (DateHistogramFacetResultSet) next;
        assertEquals( 3, histogramFacetResultSet.getResultEntries().size() );

        String expectedXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<content>\n" +
            "  <facets>\n" +
            "    <date-histogram name=\"myHistogramFacet\">\n" +
            "      <interval count=\"4\">2012-01-01 00:00:00</interval>\n" +
            "      <interval count=\"1\">2012-01-01 00:02:00</interval>\n" +
            "      <interval count=\"2\">2012-01-01 01:00:00</interval>\n" +
            "    </date-histogram>\n" +
            "  </facets>\n" +
            "</content>";

        createAndCompareResultAsXml( result, expectedXml );
    }

    @Test
    public void hour_interval()
    {
        createAndIndexContent( 1, "2012-01-01", "data.myDate" );
        createAndIndexContent( 2, "2012-01-01 12:00", "data.myDate" );
        createAndIndexContent( 3, "2012-01-02", "data.myDate" );
        createAndIndexContent( 4, "2012-01-03 23:59:59", "data.myDate" );
        createAndIndexContent( 5, "2012-01-04 10:00", "data.myDate" );
        createAndIndexContent( 6, "2012-01-04 12:00", "data.myDate" );
        createAndIndexContent( 7, "2012-01-04 14:10", "data.myDate" );
        createAndIndexContent( 8, "2012-01-04 16:00", "data.myDate" );

        flushIndex();

        ContentIndexQuery query = new ContentIndexQuery( "" );
        final String facetDefinition = "<facets>\n" +
            "    <date-histogram name=\"myHistogramFacet\">\n" +
            "        <index>data.myDate</index>\n" +
            "        <interval>hour</interval>\n" +
            "    </date-histogram>\n" +
            "</facets>\n";

        query.setFacets( facetDefinition );
        final ContentResultSet result = contentIndexService.query( query );

        final FacetResultSet next = result.getFacetsResultSet().iterator().next();
        assertNotNull( next );
        assertTrue( next instanceof DateHistogramFacetResultSet );
        DateHistogramFacetResultSet histogramFacetResultSet = (DateHistogramFacetResultSet) next;
        assertEquals( 8, histogramFacetResultSet.getResultEntries().size() );

        String expectedXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<content>\n" +
            "  <facets>\n" +
            "    <date-histogram name=\"myHistogramFacet\">\n" +
            "      <interval count=\"1\">2012-01-01 00:00:00</interval>\n" +
            "      <interval count=\"1\">2012-01-01 12:00:00</interval>\n" +
            "      <interval count=\"1\">2012-01-02 00:00:00</interval>\n" +
            "      <interval count=\"1\">2012-01-03 23:00:00</interval>\n" +
            "      <interval count=\"1\">2012-01-04 10:00:00</interval>\n" +
            "      <interval count=\"1\">2012-01-04 12:00:00</interval>\n" +
            "      <interval count=\"1\">2012-01-04 14:00:00</interval>\n" +
            "      <interval count=\"1\">2012-01-04 16:00:00</interval>\n" +
            "    </date-histogram>\n" +
            "  </facets>\n" +
            "</content>";

        createAndCompareResultAsXml( result, expectedXml );
    }

    @Test
    public void hour_and_a_half_interval()
    {
        createAndIndexContent( 1, "2012-01-01", "data.myDate" );
        createAndIndexContent( 2, "2012-01-01 01:29:59", "data.myDate" );
        createAndIndexContent( 3, "2012-01-01 01:30", "data.myDate" );
        createAndIndexContent( 4, "2012-01-01 02:00", "data.myDate" );
        createAndIndexContent( 5, "2012-01-01 03:00", "data.myDate" );
        createAndIndexContent( 6, "2012-01-01 04:00", "data.myDate" );
        createAndIndexContent( 7, "2012-01-01 05:00", "data.myDate" );
        createAndIndexContent( 8, "2012-01-01 06:00", "data.myDate" );

        flushIndex();

        ContentIndexQuery query = new ContentIndexQuery( "" );
        final String facetDefinition = "<facets>\n" +
            "    <date-histogram name=\"myHistogramFacet\">\n" +
            "        <index>data.myDate</index>\n" +
            "        <interval>1.5h</interval>\n" +
            "    </date-histogram>\n" +
            "</facets>\n";

        query.setFacets( facetDefinition );
        final ContentResultSet result = contentIndexService.query( query );

        final FacetResultSet next = result.getFacetsResultSet().iterator().next();
        assertNotNull( next );
        assertTrue( next instanceof DateHistogramFacetResultSet );
        DateHistogramFacetResultSet histogramFacetResultSet = (DateHistogramFacetResultSet) next;
        assertEquals( 5, histogramFacetResultSet.getResultEntries().size() );

        String expectedXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<content>\n" +
            "  <facets>\n" +
            "    <date-histogram name=\"myHistogramFacet\">\n" +
            "      <interval count=\"2\">2012-01-01 00:00:00</interval>\n" +
            "      <interval count=\"2\">2012-01-01 01:30:00</interval>\n" +
            "      <interval count=\"2\">2012-01-01 03:00:00</interval>\n" +
            "      <interval count=\"1\">2012-01-01 04:30:00</interval>\n" +
            "      <interval count=\"1\">2012-01-01 06:00:00</interval>\n" +
            "    </date-histogram>\n" +
            "  </facets>\n" +
            "</content>";

        createAndCompareResultAsXml( result, expectedXml );
    }

    @Test
    public void day_interval()
    {
        createAndIndexContent( 1, "2012-01-01", "data.myDate" );
        createAndIndexContent( 2, "2012-01-01 12:00", "data.myDate" );
        createAndIndexContent( 3, "2012-01-02", "data.myDate" );
        createAndIndexContent( 4, "2012-01-02 23:59:59", "data.myDate" );
        createAndIndexContent( 5, "2012-01-04 00:00", "data.myDate" );
        createAndIndexContent( 6, "2012-01-04 12:00", "data.myDate" );
        createAndIndexContent( 7, "2012-01-04 22:10", "data.myDate" );
        createAndIndexContent( 8, "2012-01-04 23:50", "data.myDate" );

        flushIndex();

        ContentIndexQuery query = new ContentIndexQuery( "" );
        final String facetDefinition = "<facets>\n" +
            "    <date-histogram name=\"myHistogramFacet\">\n" +
            "        <index>data.myDate</index>\n" +
            "        <interval>day</interval>\n" +
            "    </date-histogram>\n" +
            "</facets>\n";

        query.setFacets( facetDefinition );
        final ContentResultSet result = contentIndexService.query( query );

        final FacetResultSet next = result.getFacetsResultSet().iterator().next();
        assertNotNull( next );
        assertTrue( next instanceof DateHistogramFacetResultSet );
        DateHistogramFacetResultSet histogramFacetResultSet = (DateHistogramFacetResultSet) next;
        assertEquals( 3, histogramFacetResultSet.getResultEntries().size() );

        String expectedXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<content>\n" +
            "  <facets>\n" +
            "    <date-histogram name=\"myHistogramFacet\">\n" +
            "      <interval count=\"2\">2012-01-01 00:00:00</interval>\n" +
            "      <interval count=\"2\">2012-01-02 00:00:00</interval>\n" +
            "      <interval count=\"4\">2012-01-04 00:00:00</interval>\n" +
            "    </date-histogram>\n" +
            "  </facets>\n" +
            "</content>\n" +
            "\n";

        createAndCompareResultAsXml( result, expectedXml );
    }


    @Test
    public void month_interval()
    {
        createAndIndexContent( 1, "2012-01-01", "data.myDate" );
        createAndIndexContent( 2, "2012-02-01 12:00", "data.myDate" );
        createAndIndexContent( 3, "2012-03-02", "data.myDate" );
        createAndIndexContent( 4, "2012-04-03 23:59:59", "data.myDate" );
        createAndIndexContent( 5, "2012-05-04 10:00", "data.myDate" );
        createAndIndexContent( 6, "2012-06-04 12:00:42", "data.myDate" );
        createAndIndexContent( 7, "2012-07-04 14:10", "data.myDate" );
        createAndIndexContent( 8, "2012-08-04 16:01:59", "data.myDate" );
        createAndIndexContent( 9, "2012-08-31 23:59:59", "data.myDate" );

        flushIndex();

        ContentIndexQuery query = new ContentIndexQuery( "" );
        final String facetDefinition = "<facets>\n" +
            "    <date-histogram name=\"myHistogramFacet\">\n" +
            "        <index>data.myDate</index>\n" +
            "        <interval>month</interval>\n" +
            "    </date-histogram>\n" +
            "</facets>\n";

        query.setFacets( facetDefinition );
        final ContentResultSet result = contentIndexService.query( query );

        final FacetResultSet next = result.getFacetsResultSet().iterator().next();
        assertNotNull( next );
        assertTrue( next instanceof DateHistogramFacetResultSet );
        DateHistogramFacetResultSet histogramFacetResultSet = (DateHistogramFacetResultSet) next;
        assertEquals( 8, histogramFacetResultSet.getResultEntries().size() );

        String expectedXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<content>\n" +
            "  <facets>\n" +
            "    <date-histogram name=\"myHistogramFacet\">\n" +
            "      <interval count=\"1\">2012-01-01 00:00:00</interval>\n" +
            "      <interval count=\"1\">2012-02-01 00:00:00</interval>\n" +
            "      <interval count=\"1\">2012-03-01 00:00:00</interval>\n" +
            "      <interval count=\"1\">2012-04-01 00:00:00</interval>\n" +
            "      <interval count=\"1\">2012-05-01 00:00:00</interval>\n" +
            "      <interval count=\"1\">2012-06-01 00:00:00</interval>\n" +
            "      <interval count=\"1\">2012-07-01 00:00:00</interval>\n" +
            "      <interval count=\"2\">2012-08-01 00:00:00</interval>\n" +
            "    </date-histogram>\n" +
            "  </facets>\n" +
            "</content>";

        createAndCompareResultAsXml( result, expectedXml );
    }

    @Test
    public void year_interval()
    {
        createAndIndexContent( 1, "2001-01-01", "data.myDate" );
        createAndIndexContent( 2, "2002-02-01 12:00", "data.myDate" );
        createAndIndexContent( 3, "2003-03-02", "data.myDate" );
        createAndIndexContent( 4, "2004-04-03 23:59:59", "data.myDate" );
        createAndIndexContent( 5, "2005-05-04 10:00", "data.myDate" );
        createAndIndexContent( 6, "2006-06-04 12:00:42", "data.myDate" );
        createAndIndexContent( 7, "2007-07-04 14:10", "data.myDate" );
        createAndIndexContent( 8, "2008-08-04 16:01:59", "data.myDate" );
        createAndIndexContent( 9, "2008-08-31 23:59:59", "data.myDate" );

        flushIndex();

        ContentIndexQuery query = new ContentIndexQuery( "" );
        final String facetDefinition = "<facets>\n" +
            "    <date-histogram name=\"myHistogramFacet\">\n" +
            "        <index>data.myDate</index>\n" +
            "        <interval>year</interval>\n" +
            "    </date-histogram>\n" +
            "</facets>\n";

        query.setFacets( facetDefinition );
        final ContentResultSet result = contentIndexService.query( query );

        final FacetResultSet next = result.getFacetsResultSet().iterator().next();
        assertNotNull( next );
        assertTrue( next instanceof DateHistogramFacetResultSet );
        DateHistogramFacetResultSet histogramFacetResultSet = (DateHistogramFacetResultSet) next;
        //assertEquals( 4, histogramFacetResultSet.getResultEntries().size() );

        String expectedXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<content>\n" +
            "  <facets>\n" +
            "    <date-histogram name=\"myHistogramFacet\">\n" +
            "      <interval count=\"1\">2001-01-01 00:00:00</interval>\n" +
            "      <interval count=\"1\">2002-01-01 00:00:00</interval>\n" +
            "      <interval count=\"1\">2003-01-01 00:00:00</interval>\n" +
            "      <interval count=\"1\">2004-01-01 00:00:00</interval>\n" +
            "      <interval count=\"1\">2005-01-01 00:00:00</interval>\n" +
            "      <interval count=\"1\">2006-01-01 00:00:00</interval>\n" +
            "      <interval count=\"1\">2007-01-01 00:00:00</interval>\n" +
            "      <interval count=\"2\">2008-01-01 00:00:00</interval>\n" +
            "    </date-histogram>\n" +
            "  </facets>\n" +
            "</content>";

        createAndCompareResultAsXml( result, expectedXml );
    }


    @Test
    public void preZone_settings()
    {
        /*
            Setting preZone to Pacific/Tongatapu means that facet-data settings is threaded to be in timezone+13
         */

        createAndIndexContent( 1, "2012-01-01", "data.myDate" );
        createAndIndexContent( 2, "2012-01-01 12:00", "data.myDate" );
        createAndIndexContent( 3, "2012-01-02", "data.myDate" );
        createAndIndexContent( 4, "2012-01-02 23:59:59", "data.myDate" );
        createAndIndexContent( 5, "2012-01-04 00:00", "data.myDate" );
        createAndIndexContent( 6, "2012-01-04 12:00", "data.myDate" );
        createAndIndexContent( 7, "2012-01-04 22:10", "data.myDate" );
        createAndIndexContent( 8, "2012-01-04 23:50", "data.myDate" );

        flushIndex();

        ContentIndexQuery query = new ContentIndexQuery( "" );
        final String facetDefinition = "<facets>\n" +
            "    <date-histogram name=\"myHistogramFacet\">\n" +
            "        <index>data.myDate</index>\n" +
            "        <interval>day</interval>\n" +
            "        <pre-zone>Pacific/Tongatapu</pre-zone>\n" +
            "    </date-histogram>\n" +
            "</facets>\n";

        query.setFacets( facetDefinition );
        final ContentResultSet result = contentIndexService.query( query );

        final FacetResultSet next = result.getFacetsResultSet().iterator().next();
        assertNotNull( next );
        assertTrue( next instanceof DateHistogramFacetResultSet );
        DateHistogramFacetResultSet histogramFacetResultSet = (DateHistogramFacetResultSet) next;
        assertEquals( 5, histogramFacetResultSet.getResultEntries().size() );

        String expectedXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<content>\n" +
            "  <facets>\n" +
            "    <date-histogram name=\"myHistogramFacet\">\n" +
            "      <interval count=\"1\">2011-12-31 12:00:00</interval>\n" +
            "      <interval count=\"2\">2012-01-01 12:00:00</interval>\n" +
            "      <interval count=\"1\">2012-01-02 12:00:00</interval>\n" +
            "      <interval count=\"1\">2012-01-03 12:00:00</interval>\n" +
            "      <interval count=\"3\">2012-01-04 12:00:00</interval>\n" +
            "    </date-histogram>\n" +
            "  </facets>\n" +
            "</content>";

        createAndCompareResultAsXml( result, expectedXml );
    }


    @Test
    public void key_and_value()
    {
        createAndIndexContent( 1, new String[]{"2012-01-01", "10"}, new String[]{"data.myDate", "data.myValue"} );
        createAndIndexContent( 2, new String[]{"2012-01-01 12:00", "20"}, new String[]{"data.myDate", "data.myValue"} );
        createAndIndexContent( 3, new String[]{"2012-01-02", "30"}, new String[]{"data.myDate", "data.myValue"} );
        createAndIndexContent( 4, new String[]{"2012-01-02 23:59:59", "40"}, new String[]{"data.myDate", "data.myValue"} );
        createAndIndexContent( 5, new String[]{"2012-01-04 00:00", "50"}, new String[]{"data.myDate", "data.myValue"} );
        createAndIndexContent( 6, new String[]{"2012-01-04 12:00", "60"}, new String[]{"data.myDate", "data.myValue"} );
        createAndIndexContent( 7, new String[]{"2012-01-04 22:10", "70"}, new String[]{"data.myDate", "data.myValue"} );
        createAndIndexContent( 8, new String[]{"2012-01-04 23:50", "80"}, new String[]{"data.myDate", "data.myValue"} );

        flushIndex();

        ContentIndexQuery query = new ContentIndexQuery( "" );
        final String facetDefinition = "<facets>\n" +
            "    <date-histogram name=\"myHistogramFacet\">\n" +
            "        <index>data.myDate</index>\n" +
            "        <value-index>data.myValue</value-index>\n" +
            "        <interval>day</interval>\n" +
            "    </date-histogram>\n" +
            "</facets>\n";

        query.setFacets( facetDefinition );
        final ContentResultSet result = contentIndexService.query( query );

        final FacetResultSet next = result.getFacetsResultSet().iterator().next();
        assertNotNull( next );
        assertTrue( next instanceof DateHistogramFacetResultSet );
        DateHistogramFacetResultSet histogramFacetResultSet = (DateHistogramFacetResultSet) next;
        assertEquals( 3, histogramFacetResultSet.getResultEntries().size() );

        String expectedXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<content>\n" +
            "  <facets>\n" +
            "    <date-histogram name=\"myHistogramFacet\">\n" +
            "      <interval total=\"30.0\" count=\"2\" min=\"10.0\" mean=\"15.0\" max=\"20.0\">2012-01-01 00:00:00</interval>\n" +
            "      <interval total=\"70.0\" count=\"2\" min=\"30.0\" mean=\"35.0\" max=\"40.0\">2012-01-02 00:00:00</interval>\n" +
            "      <interval total=\"260.0\" count=\"4\" min=\"50.0\" mean=\"65.0\" max=\"80.0\">2012-01-04 00:00:00</interval>\n" +
            "    </date-histogram>\n" +
            "  </facets>\n" +
            "</content>\n" +
            "\n";

        createAndCompareResultAsXml( result, expectedXml );
    }


}
