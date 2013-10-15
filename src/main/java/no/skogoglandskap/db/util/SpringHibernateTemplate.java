package no.skogoglandskap.db.util;

import java.io.Serializable;
import java.sql.BatchUpdateException;
import java.sql.SQLException;

import org.apache.log4j.Logger;
import org.hibernate.exception.ConstraintViolationException;
import org.hibernate.exception.SQLGrammarException;
import org.springframework.orm.hibernate3.HibernateTemplate;

import com.mchange.v2.sql.SqlUtils;

/**
 * 
 * This is main class interaction with the database
 * 
 */
public class SpringHibernateTemplate extends HibernateTemplate {

	private Logger logger = Logger.getLogger(SpringHibernateTemplate.class);

	@Override
	/**
	 * Added to get more logging to file when a error occurs
	 */
	public Serializable save(Object entity) {
		Exception sqle = null;

		

		try {
			return super.save(entity);
		} catch (Exception e) {

			// if (e instanceof org.springframework.dao.DataIntegrityViolationException) {
			// DataIntegrityViolationException e2 = (DataIntegrityViolationException) e.getCause();
			// logger.error("Testtt error " + e.getClass().getName(), e); // Log
			//
			// SQLException s2 = (SQLException) e;
			// while ((s2 = s2.getNextException()) != null) {
			// logger.error("Nested SQLException or SQLWarning: ", s2);
			// }
			// }

			if (e instanceof SQLGrammarException) {

				SQLException exception = ((SQLGrammarException) e).getSQLException();
				if (!getGetDetailError(exception)) {
					String constraintName = ((ConstraintViolationException) e).getConstraintName();
					logger.error("Failed commit data to database for " + e.getClass().getName() + "," + constraintName, e);
				}
			} else if (e instanceof ConstraintViolationException) {
				SQLException exception = ((ConstraintViolationException) e).getSQLException();
				if (!getGetDetailError(exception)) {
					String constraintName = ((ConstraintViolationException) e).getConstraintName();
					logger.error("Failed commit data to database for " + e.getClass().getName() + "," + constraintName, e);
				}
			} else if (e instanceof java.sql.BatchUpdateException) {
				SQLException exception = ((java.sql.BatchUpdateException) e).getNextException();
				if (!getGetDetailError(exception)) {
					String constraintName = ((ConstraintViolationException) e).getConstraintName();
					logger.error("Failed commit data to database for " + e.getClass().getName() + "," + constraintName, e);
				}
			} else if (e instanceof org.hibernate.exception.DataException) {
				SQLException exception = ((org.hibernate.exception.DataException) e).getSQLException();
				if (!getGetDetailError(exception)) {
					String constraintName = ((ConstraintViolationException) e).getConstraintName();
					logger.error("Failed commit data to database for " + e.getClass().getName() + "," + constraintName, e);
				}
			} else if (e instanceof org.springframework.dao.DataIntegrityViolationException) {
				Throwable exception1 = ((org.springframework.dao.DataIntegrityViolationException) e).getCause();
				java.sql.BatchUpdateException cause = (BatchUpdateException) exception1.getCause();
				//logger.error("Help handled error " + cause.getClass().getName(), cause); // Log
				
				//SQLException exception = ((java.sql.BatchUpdateException) cause).getNextException();
				if (!getGetDetailError(cause)) {
					String constraintName = ((ConstraintViolationException) e).getConstraintName();
					logger.error("Failed commit data to database for " + e.getClass().getName() + "," + constraintName, e);
				}

			} else {

				logger.error("Not handled error " + e.getClass().getName(), e); // Log
			}

		}

		throw new RuntimeException(sqle);

	}

	private boolean getGetDetailError(SQLException exception) {
		int a = 0;

		SQLException s2 = exception;
		while ((s2 = s2.getNextException()) != null) {
			logger.error("error num:" + (a++), s2); // Log the
			Throwable t = s2.getCause();
			while (t != null) {
				logger.error("Exception class name:" + t.getClass().getName() + " error num: " + a + " Cause:", t);
				t = t.getCause();
			}
		}
		return (a > 0);
	}

}