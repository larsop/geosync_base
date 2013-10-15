package no.skogoglandskap.db.util;

import java.sql.SQLException;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;

/**
 * Database Utility Class
 * Executes sql in GPI Temp Database
 * 
 * @author Mette Larsen
 */
public class DatabaseUtility {
	
	SpringHibernateTemplate dbConnection;

	/**
	 * Constructor.
	 * The dbConnection must be passed from class running in spring context
	 * @param dbConnection connection to the GPI database
	 */
	public DatabaseUtility(SpringHibernateTemplate dbConnection) {
		super();
		this.dbConnection = dbConnection;
	}

	/**
	 * Executes sql in database
	 * @param sql - the complete sql to execute
	 */
	public List<Object[]> executeSql(final String sql) {
		Object result  = dbConnection.execute(new HibernateCallback() {
			public Object doInHibernate(Session session) throws HibernateException, SQLException {
				SQLQuery sqlQuery = session.createSQLQuery(sql);
				return sqlQuery.list();
			}
		});
		
		return (List<Object[]>) result ;
	}
}
