package no.geonorge.skjema.changelogfile.util;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import no.geonorge.skjema.changelogfile.TransactionCollection;

import org.apache.log4j.Logger;
import org.springframework.oxm.Marshaller;
import org.springframework.oxm.Unmarshaller;
import org.springframework.oxm.XmlMappingException;

/**
 * This is use to read and write files TransactionCollection http://skjema.geonorge.no/standard/geosynkronisering/1.0/endringslogg
 * 
 * The code here is now using jJAX but it will be replaces by StAX
 * 
 * This parser now also know about Marshall and Unmarshall for classes in http://skjema.geonorge.no/SOSI/produktspesifikasjon/
 * 
 * 
 * @author lars
 * 
 */
public class ChangeLogMarshallerHelper {
	static Logger logger = Logger.getLogger(ChangeLogMarshallerHelper.class);

	private Marshaller uarshaller;
	private Unmarshaller unmarshaller;

	/**
	 * A Marshaller that handles
	 * 
	 * @param m
	 */
	public ChangeLogMarshallerHelper(Marshaller m) {
		if (logger.isDebugEnabled()) {
			logger.debug("enter");
		}

		this.uarshaller = m;
		this.unmarshaller = (Unmarshaller) m;

		if (logger.isDebugEnabled()) {
			logger.debug("leave");
		}

	}

	/**
	 * 
	 * @return Unmarshaller that handles classes in http://skjema.geonorge.no/standard/geosynkronisering/1.0/endringslogg
	 */
	public Unmarshaller getUnmarshaller() {
		return unmarshaller;
	}

	/**
	 * 
	 * @return marsheller Unmarshaller that handles classes in http://skjema.geonorge.no/standard/geosynkronisering/1.0/endringslogg
	 */
	public Marshaller getMarshaller() {
		return uarshaller;
	}

	/**
	 * A helper method for unmarshaling content in an files
	 * 
	 * @param fileName
	 * @return
	 * @throws IOException
	 * @throws XmlMappingException
	 */
	public TransactionCollection unmarshal(String fileName) throws XmlMappingException, IOException {
		if (logger.isDebugEnabled()) {
			logger.debug("enter");
		}
		FileInputStream is = new FileInputStream(fileName);
		TransactionCollection transactionCollection = null;

		try {

			StreamSource streamSource = new StreamSource(is);
			transactionCollection = (TransactionCollection) unmarshaller.unmarshal(streamSource);
		} finally {
			is.close();
		}

		if (logger.isDebugEnabled()) {
			logger.debug("leave");
		}

		return transactionCollection;
	}

}