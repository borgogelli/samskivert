//
// $Id: MySQLRepository.java,v 1.6 2001/05/30 17:02:52 mdb Exp $

package com.samskivert.jdbc;

import java.sql.*;
import java.util.Properties;

/**
 * The MySQL repository class provides functionality useful to repository
 * implementations that make use of MySQL as their underlying database.
 */
public abstract class MySQLRepository extends Repository
{
    /**
     * Constructs a MySQL repository implementation with the supplied
     * configuration properties.
     */
    public MySQLRepository (Properties props)
	throws SQLException
    {
	super(props);
    }

    /**
     * @return the most recent ID generated by an insert into an
     * AUTO_INCREMENT table.
     */
    protected int lastInsertedId ()
	throws SQLException
    {
        // make sure we've got a connection
        ensureConnection();

	// we have to do this by hand. alas all is not roses.
	Statement stmt = _session.connection.createStatement();
	ResultSet rs = stmt.executeQuery("select LAST_INSERT_ID()");
	if (rs.next()) {
	    return rs.getInt(1);
	} else {
	    return -1;
	}
    }

    /**
     * Determines whether or not the supplied SQL exception originated
     * from a duplicate row error.
     *
     * @return true if the exception was thrown because a duplicate row
     * was inserted into a table that does not allow such things, false if
     * the exception is not related to duplicate rows.
     */
    protected boolean isDuplicateRowException (SQLException sqe)
    {
	String msg = sqe.getMessage();
	return (msg != null && msg.indexOf("Duplicate entry") != -1);
    }

    /**
     * Determines whether or not the supplied SQL exception is a transient
     * failure, meaning one that is not related to the SQL being executed,
     * but instead to the environment at the time of execution, like the
     * connection to the database having been lost.
     *
     * @return true if the exception was thrown due to a transient
     * failure, false if not.
     */
    protected boolean isTransientException (SQLException sqe)
    {
	String msg = sqe.getMessage();
	return (msg != null &&
                (msg.indexOf("Lost connection") != -1 ||
                 msg.indexOf("Communication link failure") != -1 ||
                 msg.indexOf("Broken pipe") != -1));
    }
}
