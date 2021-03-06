/*
 * Copyright 2000-2013 Enonic AS
 * http://www.enonic.com/license
 */

package com.enonic.cms.core.xslt.portal;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamSource;

import com.google.common.io.ByteStreams;

import com.enonic.cms.core.resource.FileResource;
import com.enonic.cms.core.resource.FileResourceName;
import com.enonic.cms.core.resource.FileResourceService;
import com.enonic.cms.core.xslt.XsltResourceHelper;

final class XsltResourceLoader
{
    private final FileResourceService resourceService;

    public XsltResourceLoader( final FileResourceService resourceService )
    {
        this.resourceService = resourceService;
    }

    public Source load( final FileResourceName name )
        throws TransformerException
    {
        final FileResource resource = this.resourceService.getResource( name );

        if ( resource == null )
        {
            throw new TransformerException( "Failed to find resource [" + name.toString() + "]" );
        }

        InputStream resourceData = null;

        try
        {
            resourceData = this.resourceService.getResourceStream( name, true );

            return doGetSource( name, resourceData );
        }
        finally
        {
            if ( resourceData != null )
            {
                try
                {
                    resourceData.close();
                }
                catch ( IOException e )
                {
                    throw new TransformerException( "Error closing input file: " + name, e );
                }
            }
        }
    }

    private Source doGetSource( final FileResourceName name, final InputStream resourceData )
        throws TransformerException
    {
        if ( resourceData == null )
        {
            throw new TransformerException( "Failed to find resource data for [" + name.toString() + "]" );
        }

        byte[] file;

        try
        {
            file = ByteStreams.toByteArray( resourceData );
        }
        catch ( IOException e )
        {
            throw new TransformerException( "Could not read input file: " + name, e );
        }

        final StreamSource source = new StreamSource();
        source.setInputStream( new ByteArrayInputStream( file ) );
        source.setSystemId( XsltResourceHelper.createUri( name.getPath() ) );
        return source;
    }
}