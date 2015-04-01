/*
 * Copyright 2000-2013 Enonic AS
 * http://www.enonic.com/license
 */

package com.enonic.cms.web.portal.services;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUpload;
import org.apache.commons.fileupload.FileUploadBase;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletRequestContext;
import org.apache.commons.lang.StringUtils;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.servlet.ModelAndView;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

import com.enonic.esl.containers.ExtendedMap;
import com.enonic.esl.containers.MultiValueMap;
import com.enonic.vertical.adminweb.VerticalAdminLogger;
import com.enonic.vertical.engine.VerticalCreateException;
import com.enonic.vertical.engine.VerticalEngineException;
import com.enonic.vertical.engine.VerticalRemoveException;
import com.enonic.vertical.engine.VerticalSecurityException;

import com.enonic.cms.framework.util.UrlPathDecoder;

import com.enonic.cms.core.Attribute;
import com.enonic.cms.core.captcha.CaptchaService;
import com.enonic.cms.core.content.ContentParserService;
import com.enonic.cms.core.content.ContentService;
import com.enonic.cms.core.content.access.ContentAccessException;
import com.enonic.cms.core.content.category.CategoryAccessException;
import com.enonic.cms.core.mail.SendMailService;
import com.enonic.cms.core.portal.VerticalSession;
import com.enonic.cms.core.portal.cache.PageCacheService;
import com.enonic.cms.core.portal.httpservices.HttpServicesException;
import com.enonic.cms.core.security.SecurityService;
import com.enonic.cms.core.security.userstore.UserStoreService;
import com.enonic.cms.core.service.UserServicesService;
import com.enonic.cms.core.structure.SiteContext;
import com.enonic.cms.core.structure.SiteKey;
import com.enonic.cms.core.structure.SitePath;
import com.enonic.cms.core.structure.SiteService;
import com.enonic.cms.store.dao.CategoryDao;
import com.enonic.cms.store.dao.ContentDao;
import com.enonic.cms.store.dao.SiteDao;
import com.enonic.cms.web.portal.PortalSitePathResolver;
import com.enonic.cms.web.portal.PortalWebContext;
import com.enonic.cms.web.portal.SiteRedirectHelper;

public abstract class ServicesProcessorBase
    implements ServicesProcessor
{
    private final String handlerName;

    // fatal errors

    public final static int ERR_OPERATION_BACKEND = 504;  // http 500 Internal Server Error

    public final static int ERR_SECURITY_EXCEPTION = 506;  // http 401 Unautorized or 403 Forbidden, depending on anonymous

    // general errors

    public final static int ERR_PARAMETERS_MISSING = 400;  // http 400 Bad Request

    public final static int ERR_PARAMETERS_INVALID = 401;  // http 400 Bad Request

    public final static int ERR_EMAIL_SEND_FAILED = 402;  // http 500 Internal Server Error

    public final static int ERR_INVALID_CAPTCHA = 405;  // http 400 Bad Request

    public final static int ERR_CONTENT_NOT_FOUND = 406;  // http 400 Bad Request

    // HTTP response status codes in use with http services:

    public final static int HTTP_STATUS_BAD_REQUEST = HttpServletResponse.SC_BAD_REQUEST;

    public final static int HTTP_STATUS_UNAUTHORIZED = HttpServletResponse.SC_UNAUTHORIZED;

    public final static int HTTP_STATUS_FORBIDDEN = HttpServletResponse.SC_FORBIDDEN;

    public final static int HTTP_STATUS_INTERNAL_SERVER_ERROR = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;

    protected static final DateTimeFormatter DATE_FORMAT_FROM = DateTimeFormat.forPattern( "dd.MM.yyyy" );

    protected static final DateTimeFormatter DATE_FORMAT_TO = DateTimeFormat.forPattern( "yyyy-MM-dd" );

    private final FileUploadBase fileUpload;

    protected CaptchaService captchaService;

    private UserServicesRedirectUrlResolver userServicesRedirectUrlResolver;

    private UserServicesAccessManager userServicesAccessManager;

    private SiteService siteService;

    private PortalSitePathResolver sitePathResolver;

    private SiteRedirectHelper siteRedirectHelper;

    protected SiteDao siteDao;

    protected CategoryDao categoryDao;

    protected ContentDao contentDao;

    protected SecurityService securityService;

    protected UserStoreService userStoreService;

    protected SendMailService sendMailService;

    protected ContentParserService contentParserService;

    protected ContentService contentService;

    protected PageCacheService pageCacheService;

    protected boolean transliterate;

    private UserServicesService userServicesService;

    private ImmutableList<String> allowedRedirectDomains;

    public ServicesProcessorBase( final String handlerName )
    {
        this.handlerName = handlerName;
        fileUpload = new FileUpload( new DiskFileItemFactory() );
        fileUpload.setHeaderEncoding( "UTF-8" );
        this.allowedRedirectDomains = ImmutableList.of( "*" );
    }

    public final String getHandlerName()
    {
        return this.handlerName;
    }

    public final void handle( final PortalWebContext context )
        throws Exception
    {
        final HttpServletRequest request = context.getRequest();
        final HttpServletResponse response = context.getResponse();

        handleRequest( request, response );
    }

    @Autowired
    public void setUserServicesRedirectHelper( UserServicesRedirectUrlResolver value )
    {
        this.userServicesRedirectUrlResolver = value;
    }

    @Autowired
    public void setCaptchaService( CaptchaService service )
    {
        captchaService = service;
    }

    @Autowired
    public void setUserServicesService( UserServicesService userServicesService )
    {
        this.userServicesService = userServicesService;
    }

    @Autowired
    public void setPageCacheService( PageCacheService value )
    {
        this.pageCacheService = value;
    }

    @Autowired
    public void setContentDao( ContentDao contentDao )
    {
        this.contentDao = contentDao;
    }

    @Autowired
    public void setContentParserService( ContentParserService contentParserService )
    {
        this.contentParserService = contentParserService;
    }

    @Autowired
    public void setContentService( ContentService contentService )
    {
        this.contentService = contentService;
    }

    @Autowired
    public void setSiteDao( SiteDao siteDao )
    {
        this.siteDao = siteDao;
    }

    @Autowired
    public void setSiteRedirectHelper( SiteRedirectHelper value )
    {
        this.siteRedirectHelper = value;
    }

    @Autowired
    public void setSiteService( SiteService value )
    {
        this.siteService = value;
    }

    @Autowired
    public void setSitePathResolver( PortalSitePathResolver value )
    {
        this.sitePathResolver = value;
    }

    @Autowired
    public void setCategoryDao( CategoryDao categoryDao )
    {
        this.categoryDao = categoryDao;
    }

    @Autowired
    public void setSecurityService( SecurityService value )
    {
        this.securityService = value;
    }

    @Autowired
    public void setUserStoreService( UserStoreService userStoreService )
    {
        this.userStoreService = userStoreService;
    }

    @Autowired
    public void setSendMailService( SendMailService sendMailService )
    {
        this.sendMailService = sendMailService;
    }

    protected void handlerCreate( HttpServletRequest request, HttpServletResponse response, HttpSession session, ExtendedMap formItems,
                                  UserServicesService userServices, SiteKey siteKey )
        throws VerticalUserServicesException, VerticalCreateException, VerticalSecurityException, RemoteException
    {
        String message = "OperationWrapper CREATE not implemented.";
        VerticalUserServicesLogger.error( message );
    }

    protected void handlerRemove( HttpServletRequest request, HttpServletResponse response, HttpSession session, ExtendedMap formItems,
                                  UserServicesService userServices, SiteKey siteKey )
        throws VerticalUserServicesException, VerticalRemoveException, VerticalSecurityException, RemoteException
    {
        String message = "OperationWrapper REMOVE not implemented.";
        VerticalUserServicesLogger.error( message );
    }

    protected void handlerCustom( HttpServletRequest request, HttpServletResponse response, HttpSession session, ExtendedMap formItems,
                                  UserServicesService userServices, SiteKey siteKey, String operation )
        throws VerticalUserServicesException, VerticalEngineException, IOException, ClassNotFoundException, IllegalAccessException,
        InstantiationException
    {
        String message = "Custom operation not implemented: {0}";
        if ( operation != null )
        {
            operation = operation.toUpperCase();
        }
        VerticalUserServicesLogger.error( message, operation, null );
    }

    protected void handlerUpdate( HttpServletRequest request, HttpServletResponse response, HttpSession session, ExtendedMap formItems,
                                  UserServicesService userServices, SiteKey siteKey )
    {
        String message = "OperationWrapper UPDATE not implemented.";
        VerticalUserServicesLogger.error( message );
    }

    private UserServicesService lookupUserServices()
    {
        return userServicesService;
    }

    public boolean isArrayFormItem( Map formItems, String string )
    {
        if ( !formItems.containsKey( string ) )
        {
            return false;
        }

        return formItems.get( string ).getClass() == String[].class;
    }

    private ExtendedMap parseSimpleRequest( HttpServletRequest request )
    {

        ExtendedMap formItems = new ExtendedMap( true );
        Enumeration paramNames = request.getParameterNames();

        while ( paramNames.hasMoreElements() )
        {
            String key = paramNames.nextElement().toString();
            String[] values = request.getParameterValues( key );

            if ( values != null )
            {
                if ( values.length == 1 && values[0] != null )
                {
                    String value = values[0];
                    if ( "true".equals( value ) )
                    {
                        formItems.putBoolean( key, true );
                    }
                    else if ( "false".equals( value ) )
                    {
                        formItems.putBoolean( key, false );
                    }
                    else
                    {
                        formItems.putString( key, value );
                    }
                }
                else if ( values.length > 1 )
                {
                    formItems.put( key, values );
                }
            }
            else
            {
                formItems.put( key, "" );
            }
        }

        return formItems;
    }

    private ExtendedMap parseMultiPartRequest( HttpServletRequest request )
    {
        ExtendedMap formItems = new ExtendedMap( true );
        try
        {
            List paramList = fileUpload.parseRequest( new ServletRequestContext( request ) );
            for ( Iterator iter = paramList.iterator(); iter.hasNext(); )
            {
                FileItem fileItem = (FileItem) iter.next();

                String name = fileItem.getFieldName();

                if ( fileItem.isFormField() )
                {
                    String value = fileItem.getString( "UTF-8" );
                    if ( formItems.containsKey( name ) )
                    {
                        ArrayList<Object> values = new ArrayList<Object>();
                        Object obj = formItems.get( name );
                        if ( obj instanceof Object[] )
                        {
                            String[] objArray = (String[]) obj;
                            for ( int i = 0; i < objArray.length; i++ )
                            {
                                values.add( objArray[i] );
                            }
                        }
                        else
                        {
                            values.add( obj );
                        }
                        values.add( value );
                        formItems.put( name, values.toArray( new String[values.size()] ) );
                    }
                    else
                    {
                        formItems.put( name, value );
                    }
                }
                else
                {
                    if ( fileItem.getSize() > 0 )
                    {
                        if ( formItems.containsKey( name ) )
                        {
                            ArrayList<Object> values = new ArrayList<Object>();
                            Object obj = formItems.get( name );
                            if ( obj instanceof FileItem[] )
                            {
                                FileItem[] objArray = (FileItem[]) obj;
                                for ( int i = 0; i < objArray.length; i++ )
                                {
                                    values.add( objArray[i] );
                                }
                            }
                            else
                            {
                                values.add( obj );
                            }
                            values.add( fileItem );
                            formItems.put( name, values.toArray( new FileItem[values.size()] ) );
                        }
                        else
                        {
                            formItems.put( name, fileItem );
                        }
                    }
                }
            }
        }
        catch ( FileUploadException fue )
        {
            String message = "Error occurred with file upload: %t";
            VerticalAdminLogger.error( message, fue );
        }
        catch ( UnsupportedEncodingException uee )
        {
            String message = "Character encoding not supported: %t";
            VerticalAdminLogger.error( message, uee );
        }

        // Add parameters from url
        Map paramMap = request.getParameterMap();
        for ( Iterator iter = paramMap.entrySet().iterator(); iter.hasNext(); )
        {
            Map.Entry entry = (Map.Entry) iter.next();
            String key = (String) entry.getKey();
            if ( entry.getValue() instanceof String[] )
            {
                String[] values = (String[]) entry.getValue();
                for ( int i = 0; i < values.length; i++ )
                {
                    formItems.put( key, values[i] );
                }
            }
            else
            {
                formItems.put( key, entry.getValue() );
            }
        }

        return formItems;
    }

    private ExtendedMap parseForm( HttpServletRequest request )
    {
        if ( FileUploadBase.isMultipartContent( new ServletRequestContext( request ) ) )
        {
            return parseMultiPartRequest( request );
        }
        else
        {
            return parseSimpleRequest( request );
        }
    }

    /**
     * Process incoming HTTP requests.
     */
    private ModelAndView handleRequestInternal( HttpServletRequest request, HttpServletResponse response, SitePath sitePath )
        throws IOException
    {

        HttpSession session = request.getSession( true );
        ExtendedMap formItems = parseForm( request );
        UserServicesService userServices = lookupUserServices();
        SiteKey siteKey = sitePath.getSiteKey();

        SitePath originalSitePath = (SitePath) request.getAttribute( Attribute.ORIGINAL_SITEPATH );
        String handler = UserServicesParameterResolver.resolveHandlerFromSitePath( originalSitePath );
        String operation = UserServicesParameterResolver.resolveOperationFromSitePath( originalSitePath );

        if ( !userServicesAccessManager.isOperationAllowed( siteKey, handler, operation ) )
        {
            String message = "Access to http service '" + handler + "." + operation + "' on site " + siteKey +
                " is not allowed by configuration. Check the settings in site-" + siteKey + ".properties";
            VerticalUserServicesLogger.warn( message );
            String httpErrorMsg = "Access denied to http service '" + handler + "." + operation + "' on site " + siteKey;
            response.sendError( HttpServletResponse.SC_FORBIDDEN, httpErrorMsg );
            return null;
        }

        // check if domain in redirect URL is allowed
        final String redirect = formItems.getString( "_redirect", null );
        final String redirectUrl = userServicesRedirectUrlResolver.resolveRedirectUrlToPage( request, redirect, null );
        if ( !isRedirectUrlAllowed( redirectUrl ) )
        {
            final String domain = new URL( redirectUrl ).getHost();

            final String message = String.format(
                "Domain '%s' of redirect URL not allowed (%s), in request to HTTP service '%s.%s' on site %s. " +
                    "Check setting 'cms.httpServices.redirect.allowedDomains' in cms.properties", domain, redirectUrl, handler, operation,
                siteKey );
            VerticalUserServicesLogger.warn( message );

            final String httpErrorMsg = String.format( "Domain of redirect URL not allowed: %s", domain );
            response.sendError( HttpServletResponse.SC_FORBIDDEN, httpErrorMsg );
            return null;
        }

        try
        {
            if ( !( this instanceof FormServicesProcessor ) )
            {
                // Note: The FormHandlerController is doing its own validation.
                Boolean captchaOk = captchaService.validateCaptcha( formItems, request, handler, operation );
                if ( ( captchaOk != null ) && ( !captchaOk ) )
                {
                    VerticalSession vsession = (VerticalSession) session.getAttribute( VerticalSession.VERTICAL_SESSION_OBJECT );
                    if ( vsession == null )
                    {
                        vsession = new VerticalSession();
                        session.setAttribute( VerticalSession.VERTICAL_SESSION_OBJECT, vsession );
                    }
                    vsession.setAttribute( "error_" + handler + "_" + operation,
                                           captchaService.buildErrorXMLForSessionContext( formItems ).getAsDOMDocument() );
                    redirectToErrorPage( request, response, ERR_INVALID_CAPTCHA );
                    return null;
                }
            }

            if ( "create".equals( operation ) )
            {
                handlerCreate( request, response, session, formItems, userServices, siteKey );
            }
            else if ( "update".equals( operation ) )
            {
                handlerUpdate( request, response, session, formItems, userServices, siteKey );
            }
            else if ( "remove".equals( operation ) )
            {
                handlerRemove( request, response, session, formItems, userServices, siteKey );
            }
            else
            {
                handlerCustom( request, response, session, formItems, userServices, siteKey, operation );
            }
        }
        catch ( VerticalSecurityException vse )
        {
            // If user = anonymous, 401 (Unauthorized), otherwise (403) forbidden.
            String message = "No rights to handle request: %t";
            VerticalUserServicesLogger.warn( message, vse );
            redirectToErrorPage( request, response, ERR_SECURITY_EXCEPTION );
        }
        catch ( ContentAccessException vse )
        {
            // If user = anonymous, 401 (Unauthorized), otherwise (403) forbidden.
            String message = "No rights to handle request: %t";
            VerticalUserServicesLogger.warn( message, vse );
            redirectToErrorPage( request, response, ERR_SECURITY_EXCEPTION );
        }
        catch ( CategoryAccessException vse )
        {
            // If user = anonymous, 401 (Unauthorized), otherwise (403) forbidden.
            String message = "No rights to handle request: %t";
            VerticalUserServicesLogger.warn( message, vse );
            redirectToErrorPage( request, response, ERR_SECURITY_EXCEPTION );
        }
        catch ( HttpServicesException hse )
        {
            throw hse;
        }
        catch ( Exception e )
        {
            // 500, Internal Server Error
            String message = "Failed to handle request: %t";
            VerticalUserServicesLogger.error( message, e );
            redirectToErrorPage( request, response, ERR_OPERATION_BACKEND );
        }
        return null;
    }

    protected void redirectToPage( HttpServletRequest request, HttpServletResponse response, ExtendedMap formItems )
    {
        redirectToPage( request, response, formItems, null );
    }

    protected void redirectToPage( HttpServletRequest request, HttpServletResponse response, ExtendedMap formItems,
                                   MultiValueMap queryParams )
    {
        String redirect = formItems.getString( "_redirect", null );

        String url = userServicesRedirectUrlResolver.resolveRedirectUrlToPage( request, redirect, queryParams );

        if ( isAbsoluteUrl( url ) )
        {
            siteRedirectHelper.sendRedirectWithAbsoluteURL( response, url );
        }
        else
        {
            String decodedUrl = UrlPathDecoder.decode( url );
            siteRedirectHelper.sendRedirectWithPath( request, response, decodedUrl );
        }
    }

    private boolean isRedirectUrlAllowed( final String redirectUrl )
    {
        return !isAbsoluteUrl( redirectUrl ) || isRedirectDomainAllowed( redirectUrl );
    }

    private boolean isRedirectDomainAllowed( final String redirectUrl )
    {
        try
        {
            final URL url = new URL( redirectUrl );
            final String domain = url.getHost().toLowerCase();
            for ( String allowedDomain : this.allowedRedirectDomains )
            {
                if ( "*".equals( allowedDomain ) || domain.equals( allowedDomain ) || domain.endsWith( "." + allowedDomain ) )
                {
                    return true;
                }
            }
            return false;
        }
        catch ( MalformedURLException e )
        {
            return false;
        }
    }

    private boolean isAbsoluteUrl( String url )
    {
        return url.matches( "^[a-z]{3,6}://.+" );
    }

    protected void redirectToErrorPage( HttpServletRequest request, HttpServletResponse response, int code )
    {
        Integer[] codes = new Integer[1];
        codes[0] = code;
        redirectToErrorPage( request, response, codes, this );
    }

    protected void redirectToErrorPage( HttpServletRequest request, HttpServletResponse response, Integer[] codes,
                                        ServicesProcessor errorSource )
    {
        String url = userServicesRedirectUrlResolver.resolveRedirectUrlToErrorPage( request, codes, errorSource );
        siteRedirectHelper.sendRedirect( request, response, url );
    }

    protected static String createMissingParametersMessage( String operation, List<String> missingParameters )
    {
        StringBuffer message = new StringBuffer();
        message.append( operation ).append( " : Missing " ).append( missingParameters.size() ).append( " parameters: " );

        boolean isFirst = true;

        for ( String missingParameter : missingParameters )
        {
            if ( isFirst )
            {
                isFirst = false;
            }
            else
            {
                message.append( ", " );
            }
            message.append( missingParameter );
        }
        return message.toString();
    }

    protected static List<String> findMissingRequiredParameters( String[] requiredParameters, ExtendedMap formItems, boolean allowEmpty )
    {
        List<String> missingParameters = new ArrayList<String>();

        for ( String requiredParameter : requiredParameters )
        {
            if ( !formItems.containsKey( requiredParameter ) )
            {
                missingParameters.add( requiredParameter );
                continue;
            }

            String submittedValue = formItems.getString( requiredParameter );

            if ( StringUtils.isEmpty( submittedValue ) && !allowEmpty )
            {
                missingParameters.add( requiredParameter );
            }
        }

        return missingParameters;
    }

    @Autowired
    public void setUserServicesAccessManager( UserServicesAccessManager userServicesAccessManager )
    {
        this.userServicesAccessManager = userServicesAccessManager;
    }

    @Value("${cms.name.transliterate}")
    public void setTransliterate( boolean transliterate )
    {
        this.transliterate = transliterate;
    }

    @Value("${cms.httpServices.redirect.allowedDomains}")
    public void setAllowedRedirectDomains( final String allowedRedirectDomains )
    {
        final Iterable<String> domainPrefixes = Splitter.on( "," ).omitEmptyStrings().trimResults().split( allowedRedirectDomains );
        if ( Iterables.isEmpty( domainPrefixes ) )
        {
            this.allowedRedirectDomains = ImmutableList.of( "*" );
        }
        else
        {
            final ImmutableList.Builder<String> domainPrefixList = ImmutableList.builder();
            for ( String domainPrefix : domainPrefixes )
            {
                domainPrefixList.add( domainPrefix.toLowerCase() );
            }
            this.allowedRedirectDomains = domainPrefixList.build();
        }
    }

    public ModelAndView handleRequest( HttpServletRequest request, HttpServletResponse response )
        throws Exception
    {

        // Get check and eventually set original sitePath
        SitePath originalSitePath = (SitePath) request.getAttribute( Attribute.ORIGINAL_SITEPATH );
        if ( originalSitePath == null )
        {
            originalSitePath = sitePathResolver.resolveSitePath( request );
            request.setAttribute( Attribute.ORIGINAL_SITEPATH, originalSitePath );
        }

        // Get and set the current sitePath
        SitePath currentSitePath = sitePathResolver.resolveSitePath( request );

        return handleRequestInternal( request, response, currentSitePath );
    }

    protected SiteContext getSiteContext( SiteKey siteKey )
    {
        return siteService.getSiteContext( siteKey );
    }

    protected SitePath getSitePath( HttpServletRequest request )
    {
        SitePath sitePath = (SitePath) request.getAttribute( Attribute.ORIGINAL_SITEPATH );
        if ( sitePath == null )
        {
            sitePath = sitePathResolver.resolveSitePath( request );
        }
        return sitePath;
    }

    @Override
    public Integer httpResponseCodeTranslator( final Integer[] errorCodes )
    {
        if ( errorCodes.length != 1 )
        {
            throw new HttpServicesException( ERR_OPERATION_BACKEND );
        }

        Integer errorCode = errorCodes[0];

        switch ( errorCode )
        {
            case ERR_PARAMETERS_MISSING:
            case ERR_PARAMETERS_INVALID:
            case ERR_INVALID_CAPTCHA:
            case ERR_CONTENT_NOT_FOUND:
                return HTTP_STATUS_BAD_REQUEST;
            case ERR_SECURITY_EXCEPTION:
                if ( securityService.getLoggedInPortalUserAsEntity().isAnonymous() )
                {
                    return HTTP_STATUS_UNAUTHORIZED;
                }
                else
                {
                    return HTTP_STATUS_FORBIDDEN;
                }
            case ERR_OPERATION_BACKEND:
            case ERR_EMAIL_SEND_FAILED:
                return HTTP_STATUS_INTERNAL_SERVER_ERROR;
            default:
                throw new HttpServicesException( ERR_OPERATION_BACKEND );
        }
    }
}
