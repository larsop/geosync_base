package no.geonorge.skjema.util;

import java.util.HashMap;

import org.apache.log4j.Logger;
import org.springframework.oxm.Marshaller;
import org.springframework.oxm.Unmarshaller;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

import com.sun.xml.internal.bind.marshaller.NamespacePrefixMapper;

public class GenericMarshallerJaxb2Helper {
	static Logger logger = Logger.getLogger(GenericMarshallerJaxb2Helper.class);


	private Marshaller genericMarshaller;
	private Unmarshaller genericUnmarshaller;

	
	/**
	 * 
	 * @param marshaller
	 */
	private void setMarshaller(Marshaller marshaller) {
		if (logger.isDebugEnabled()) {
			logger.debug("enter");
		}

		final NamespacePrefixMapper mapper = new NamespacePrefixMapper() {
			@Override
			public String getPreferredPrefix(String s, String s2, boolean b) {
				if ("http://www.opengis.net/gml/3.2".equals(s)) {
					return "gml";
				} else if ("http://skjema.geonorge.no/standard/geosynkronisering/1.0/endringslogg".equals(s)) {
					return "chlogf";
				} else if ("http://www.opengis.net/fes/2.0".equals(s)) {
					return "fes";
				} else if ("http://www.opengis.net/wfs/2.0".equals(s)) {
					return "wfs";
				} else if ("http://skjema.geonorge.no/SOSI/produktspesifikasjon/Arealressurs/4.5".equals(s)) {
					return "app";
				} else {
					return s2;
				}
				// return s2;
			}
		};
		((Jaxb2Marshaller) marshaller).setMarshallerProperties(new HashMap<String, Object>() {
			{
				put("com.sun.xml.internal.bind.namespacePrefixMapper", mapper);
			}
		});

		if (logger.isDebugEnabled()) {
			logger.debug("leave");
		}

	}

	/**
	 * @param genericMarshaller the genericMarshaller to set
	 */
	public void setGenericMarshaller(Marshaller genericMarshaller) {
		setMarshaller(genericMarshaller);
		this.genericMarshaller = genericMarshaller;
		this.genericUnmarshaller = (Unmarshaller) genericMarshaller;
	}

	public Unmarshaller getUnmarshaller() {
		return genericUnmarshaller;
	}

	
	public Marshaller getMarshaller() {
		return genericMarshaller;
	}

}