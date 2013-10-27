package no.geonorge.skjema.sosi.produktspesifikasjon.util;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.namespace.QName;

import opengis.net.wfs_2_0.wfs.AbstractTransactionActionType;
import opengis.net.wfs_2_0.wfs.DeleteType;
import opengis.net.wfs_2_0.wfs.InsertType;
import opengis.net.wfs_2_0.wfs.ObjectFactory;
import opengis.net.wfs_2_0.wfs.Property;
import opengis.net.wfs_2_0.wfs.Transaction;
import opengis.net.wfs_2_0.wfs.UpdateType;
import opengis.net.wfs_2_0.wfs.Property.ValueReference;
import opengis.net_gml_3_2_1.AbstractCodeType;
import opengis.net_gml_3_2_1.SurfacePropertyType;

import org.apache.log4j.Logger;
import org.springframework.oxm.Marshaller;
import org.springframework.oxm.Unmarshaller;

import com.sun.xml.internal.bind.marshaller.NamespacePrefixMapper;
import com.vividsolutions.jts.io.ParseException;

import net.opengis.fes._2.BinaryComparisonOpType;
import net.opengis.fes._2.FilterType;
import net.opengis.fes._2.LiteralType;
import net.opengis.fes._2.MatchActionType;
import no.geonorge.skjema.changelogfile.util.SupportedWFSOperationType;
import no.geonorge.skjema.changelogfile.util.WSFOperation;
import no.geonorge.skjema.sosi.produktspesifikasjon.Arealressurs_45.ArealressursFlateType;
import no.geonorge.skjema.sosi.produktspesifikasjon.Arealressurs_45.FellesegenskaperType;
import no.geonorge.skjema.sosi.produktspesifikasjon.Arealressurs_45.IdentifikasjonPropertyType;

import org.springframework.oxm.Marshaller;
import org.springframework.oxm.Unmarshaller;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

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
	 * A helper methid to set name spaces
	 * 
	 * @param marshaller
	 */
	private void setMarshaller(Marshaller marshaller) {
		this.marshaller = marshaller;

		final NamespacePrefixMapper mapper = new NamespacePrefixMapper() {
			@Override
			public String getPreferredPrefix(String s, String s2, boolean b) {
				if ("http://www.opengis.net/gml/3.2".equals(s)) {
					return "gml";
				} else if ("http://skjema.geonorge.no/standard/geosynkronisering/1.0/produkt".equals(s)) {
					return "chlogf";
				} else if ("http://www.opengis.net/fes/2.0".equals(s)) {
					return "fes";
				} else if ("http://www.opengis.net/wfs/2.0".equals(s)) {
					return "wfs";
				} else if ("http://skjema.geonorge.no/SOSI/produktspesifikasjon/Arealressurs/4.5".equals(s)) {
					return "ar5";
				} else {
					return s2;
				}
			}
		};
		((Jaxb2Marshaller) marshaller).setMarshallerProperties(new HashMap<String, Object>() {
			{
				put("com.sun.xml.internal.bind.namespacePrefixMapper", mapper);
			}
		});
	}

	/**
	 * 
	 * @param m
	 *            handles Marshall and Unmarshall classes in http://skjema.geonorge.no/SOSI/produktspesifikasjon/
	 */
	public SosiProduktMarshallerHelper(Marshaller m) {
		if (logger.isDebugEnabled()) {
			logger.debug("enter");
		}
		setMarshaller(m);
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