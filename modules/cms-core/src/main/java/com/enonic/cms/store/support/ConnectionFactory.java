/*
 * Copyright 2000-2011 Enonic AS
 * http://www.enonic.com/license
 */
package com.enonic.cms.store.support;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.enonic.cms.framework.jdbc.ConnectionDecorator;

/**
 * This class implements the connection factory.
 */
public final class ConnectionFactory
{
    private SessionFactory sessionFactory;
    private ConnectionDecorator decorator;
    private DataSource dataSource;

    @Autowired
    public void setSessionFactory( SessionFactory sessionFactory )
    {
        this.sessionFactory = sessionFactory;
    }

    @Autowired
    public void setDecorator( ConnectionDecorator decorator )
    {
        this.decorator = decorator;
    }

    @Autowired
    public void setDataSource( final DataSource dataSource )
    {
        this.dataSource = dataSource;
    }

    public Connection getConnection( boolean decorated )
        throws SQLException
    {
        final Connection conn = this.sessionFactory.getCurrentSession().connection();
        return decorated ? this.decorator.decorate( conn ) : conn;
    }

    public Connection getConnectionFromDataSource( boolean decorated )
        throws SQLException
    {
        final Connection conn = this.dataSource.getConnection();
        return decorated ? this.decorator.decorate( conn ) : conn;
    }
}
