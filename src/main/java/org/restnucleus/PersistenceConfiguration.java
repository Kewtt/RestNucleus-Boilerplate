package org.restnucleus;

import java.util.Properties;

import javax.jdo.Constants;
import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManagerFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is a Singleton to configure the database connection at container start.
 * It will be initialized with either AWS environment variables, general
 * environment variables, or by datanucleus.properties file.
 * 
 * @author johba
 * 
 */
public class PersistenceConfiguration {

	private static Logger log = LoggerFactory.getLogger(PersistenceConfiguration.class);

	protected PersistenceManagerFactory emf;

	public PersistenceConfiguration createEntityManagerFactory() {
		// one of the system properties is set, so try to construct own config
		if (null != System.getProperty("RDS_USERNAME")) {
			Properties p = new Properties();
			p.setProperty(Constants.PROPERTY_PERSISTENCE_MANAGER_FACTORY_CLASS,
					"org.datanucleus.api.jdo.JDOPersistenceManagerFactory");
			p.setProperty("datanucleus.autoCreateSchema", "true");
			p.setProperty("datanucleus.validateTables", "true");
			p.setProperty("datanucleus.validateConstraints", "true");

			String jcs = System.getProperty("JDBC_CONNECTION_STRING");
			if (null != jcs && jcs.length() > 3) {
				// connection string is set, so use that
				log.info("loading PersistenceManagerFactory properties from Connection URL property.");
				p.setProperty(Constants.PROPERTY_CONNECTION_DRIVER_NAME,
						"org.hsqldb.jdbcDriver");
				p.setProperty(Constants.PROPERTY_CONNECTION_URL,
						System.getProperty("JDBC_CONNECTION_STRING"));
				p.setProperty(Constants.PROPERTY_CONNECTION_USER_NAME,
						System.getProperty("RDS_USERNAME"));
			} else {
				// if there is no connection string, then we have to build it
				log.info("loading PersistenceManagerFactory properties from Connection AWS properties.");
				p.setProperty(Constants.PROPERTY_CONNECTION_DRIVER_NAME,
						"com.mysql.jdbc.Driver");
				p.setProperty(Constants.PROPERTY_CONNECTION_URL,
						"jdbc:mysql://" + System.getProperty("RDS_HOSTNAME")
								+ ":" + System.getProperty("RDS_PORT") + "/"
								+ System.getProperty("RDS_DB_NAME")
								+"?useUnicode=true&characterEncoding=UTF-8");
				p.setProperty(Constants.PROPERTY_CONNECTION_USER_NAME,
						System.getProperty("RDS_USERNAME"));
				p.setProperty(Constants.PROPERTY_CONNECTION_PASSWORD,
						System.getProperty("RDS_PASSWORD"));
			}
			this.emf = JDOHelper.getPersistenceManagerFactory(p);
		} else {
			// otherwise load the config from classpath
			log.info("loading PersistenceManagerFactory properties from classpath.");
			this.emf = JDOHelper
					.getPersistenceManagerFactory("datanucleus.properties");
		}
		log.info("n*** Persistence started at " + new java.util.Date());
		return this;
	}

	
	public PersistenceManagerFactory getPersistenceManagerFactory(){
		return emf;
	}
}