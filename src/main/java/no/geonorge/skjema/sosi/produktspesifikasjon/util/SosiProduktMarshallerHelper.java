package no.geonorge.skjema.sosi.produktspesifikasjon.util;

import org.apache.log4j.Logger;
import org.springframework.oxm.Marshaller;
import org.springframework.oxm.Unmarshaller;

/**
 * This is a class that is used for handling Marshall and Unmarshall classes in http://skjema.geonorge.no/SOSI/produktspesifikasjon/ This Marshaller must also
 * know about classes used by product classes for instance gml, xlink, ......
 * 
 * We here add util methods around Marshall and Unmarshall
 * 
 * @author lars
 * 
 */
public class SosiProduktMarshallerHelper {
	static Logger logger = Logger.getLogger(SosiProduktMarshallerHelper.class);

	private Marshaller marshaller;
	private Unmarshaller unmarshaller;

	/**
	 * 
	 * @param m
	 *            handles Marshall and Unmarshall classes in http://skjema.geonorge.no/SOSI/produktspesifikasjon/
	 */
	public SosiProduktMarshallerHelper(Marshaller m) {
		if (logger.isDebugEnabled()) {
			logger.debug("enter");
		}
		this.marshaller = m;
		this.unmarshaller = (Unmarshaller) m;
		
		if (logger.isDebugEnabled()) {
			logger.debug("leave");
		}

	}


	/**
	 * 
	 * @return Unmarshaller that handles classes in http://skjema.geonorge.no/SOSI/produktspesifikasjon/*
	 */
	public Unmarshaller getUnmarshaller() {
		return unmarshaller;
	}

	/**
	 * 
	 * @return Marshaller that handles classes in http://skjema.geonorge.no/SOSI/produktspesifikasjon/*
	 */
	public Marshaller getMarshaller() {
		return marshaller;
	}

}