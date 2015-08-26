/*
 * Copyright 2000-2013 Enonic AS
 * http://www.enonic.com/license
 */
package com.enonic.cms.core.resource;

import java.io.InputStream;
import java.util.List;

public interface FileResourceService
{
    FileResource getResource( FileResourceName name );

    boolean createFolder( FileResourceName name );

    boolean createFile( FileResourceName name, FileResourceData data );

    boolean deleteResource( FileResourceName name );

    List<FileResourceName> getChildren( FileResourceName name );

    FileResourceData getResourceData( FileResourceName name );

    boolean setResourceData( FileResourceName name, FileResourceData data );

    boolean moveResource( FileResourceName from, FileResourceName to );

    boolean copyResource( FileResourceName from, FileResourceName to );

    InputStream getResourceStream( FileResourceName name, boolean ignoreBom );
}
