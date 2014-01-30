package no.skogoglandskap.ar5;



import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.UUID;

import javax.xml.bind.JAXBElement;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import no.geonorge.skjema.changelogfile.util.IConvert2ArealressursType;
import no.geonorge.skjema.sosi.produktspesifikasjon.Arealressurs_45.ArealressursFlateType;
import no.geonorge.skjema.sosi.produktspesifikasjon.Arealressurs_45.ArealressursGrenseType;
import no.geonorge.skjema.sosi.produktspesifikasjon.Arealressurs_45.IdentifikasjonPropertyType;
import no.geonorge.skjema.sosi.produktspesifikasjon.Arealressurs_45.IdentifikasjonType;
import no.geonorge.skjema.sosi.produktspesifikasjon.Arealressurs_45.PosisjonskvalitetPropertyType;
import no.geonorge.skjema.sosi.produktspesifikasjon.Arealressurs_45.PosisjonskvalitetType;
import no.geonorge.skjema.sosi.produktspesifikasjon.Arealressurs_45.RegistreringsversjonPropertyType;
import no.geonorge.skjema.sosi.produktspesifikasjon.Arealressurs_45.RegistreringsversjonType;
import no.geonorge.skjema.util.gml_geos.inspire.JTS2GML321;
import no.skogoglandskap.datamodel.postgres.provider.Ar5FlateProvSimpleFeatureEntity;
import no.skogoglandskap.util.TopoGeometry;
import opengis.net.gml_3_2_1.gml.AbstractCodeType;
import opengis.net.gml_3_2_1.gml.AbstractRingPropertyType;
import opengis.net.gml_3_2_1.gml.AbstractSurfacePatchType;
import opengis.net.gml_3_2_1.gml.BoundingShapeType;
import opengis.net.gml_3_2_1.gml.CurvePropertyType;
import opengis.net.gml_3_2_1.gml.CurveType;
import opengis.net.gml_3_2_1.gml.EnvelopeType;
import opengis.net.gml_3_2_1.gml.LineStringSegmentType;
import opengis.net.gml_3_2_1.gml.LineStringType;
import opengis.net.gml_3_2_1.gml.PolygonPatchType;
import opengis.net.gml_3_2_1.gml.PolygonType;
import opengis.net.gml_3_2_1.gml.RingType;
import opengis.net.gml_3_2_1.gml.SegmentsElement;
import opengis.net.gml_3_2_1.gml.SurfacePatchArrayPropertyType;
import opengis.net.gml_3_2_1.gml.SurfacePropertyType;
import opengis.net.gml_3_2_1.gml.SurfaceType;

import org.apache.log4j.Logger;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateList;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.Polygon;

/**
 * This class handles mapping of local provider DAO of type no.skogoglandskap.datamodel.postgres.provider.Ar5FlateProvSimpleFeatureEntity to the
 * ArealressursFlateType and ArealressursGrenseType product specification
 * 
 * This is a test class not to be used in production
 * 
 * This is test where we don't use xlink and to keep track of commonon border but just stor all coordinates with id
 * 
 * <gml:CompositeSurface srsName="urn:ogc:def:crs:EPSG::4258" gml:id="NO.SK.PKYST.COMPOSITESURFACE1"> <gml:surfaceMember> <gml:Surface
 * srsName="urn:ogc:def:crs:EPSG::4258" gml:id="NO.SK.PKYST.SURFACE1"> <gml:patches> <gml:PolygonPatch interpolation="planar"> <gml:exterior> <gml:Ring>
 * <gml:curveMember xlink:href="#NO.SK.PKYST.CURVE1"/> <gml:curveMember xlink:href="#NO.SK.PKYST.CURVE3"/> </gml:Ring> </gml:exterior> </gml:PolygonPatch>
 * </gml:patches> </gml:Surface> </gml:surfaceMember> </gml:CompositeSurface>
 * 
 * 
 * <gml:Curve srsName="urn:ogc:def:crs:EPSG::4258" gml:id="NO.SK.PKYST.CURVE1"> <gml:segments> <gml:LineStringSegment interpolation="linear"> <gml:posList>59.8
 * 11.0 59.8 10.8 60.0 10.5 60.2 10.7</gml:posList> </gml:LineStringSegment> </gml:segments> </gml:Curve>
 * 
 * 
 * <ar5:område> <gml:Surface> <gml:polygonPatches> <gml:PolygonPatch> <gml:exterior> <gml:Ring> <gml:curveMember> <gml:Curve
 * srsName="urn:ogc:def:crs:EPSG::4258" gml:id="-1872613-1872613"> <gml:segments>
 * 
 * 
 * @author lop
 * 
 */
public class SimpleAr5Transformerer1 implements IConvert2ArealressursType {

	opengis.net.gml_3_2_1.gml.ObjectFactory of = new opengis.net.gml_3_2_1.gml.ObjectFactory();
	no.geonorge.skjema.sosi.produktspesifikasjon.Arealressurs_45.ObjectFactory ofar5 = new no.geonorge.skjema.sosi.produktspesifikasjon.Arealressurs_45.ObjectFactory();
	GeometryFactory gf = new GeometryFactory();

	Locale nLocale = new Locale.Builder().setLanguage("nb").setRegion("NO").build();

	private Logger logger = Logger.getLogger(SimpleAr5Transformerer1.class);

	/*
	 * This mapping is one to for no.skogoglandskap.datamodel.postgres.provider.Ar5FlateProvSimpleFeatureEntity to ArealressursFlateType. The geometry is of
	 * type surface. Topology is not used here.
	 * 
	 * 
	 * @see no.geonorge.skjema.changelogfile.util.IConvert2ArealressursType# convert2FlateFromProv(java.util.UUID, java.lang.Object)
	 */
	public ArealressursFlateType convert2FlateFromProv(UUID lokalId, Object input, boolean useXlinKHref ) {

		

		TopoGeometry tg = (TopoGeometry) input;

		Ar5FlateProvSimpleFeatureEntity a = (Ar5FlateProvSimpleFeatureEntity) tg.ar5FlateProvSimpleFeatureEntity;

		ArealressursFlateType ar5 = ofar5.createArealressursFlateType();

		IdentifikasjonPropertyType v = new IdentifikasjonPropertyType();
		IdentifikasjonType v1 = new IdentifikasjonType();
		v1.setLokalId(lokalId.toString());
		v1.setNavnerom("no.skogoglandskap.ar5.ArealressursFlate");
		v1.setVersjonId("1.0");
		v.setIdentifikasjon(v1);
		

		ar5.setIdentifikasjon(v);

		ar5.setArealtype(makeAbstractType("" + a.getArtype(), "ArealressursArealtype"));

		ar5.setSkogbonitet(makeAbstractType("" + a.getArskogbon(), "ArealressursSkogbonitet"));

		ar5.setTreslag(makeAbstractType("" + a.getArtreslag(), "ArealressursTreslag"));

		ar5.setGrunnforhold(makeAbstractType("" + a.getArgrunnf(), "ArealressursGrunnforhold"));

		ar5.setKartstandard(makeAbstractType("" + a.getArkartstd(), "ArealressursKartstandard"));
		
		
		
		


		{
			PosisjonskvalitetPropertyType posisjonskvalitetPropertyType = new PosisjonskvalitetPropertyType();
			PosisjonskvalitetType posisjonskvalitetType = new PosisjonskvalitetType();
			posisjonskvalitetPropertyType.setPosisjonskvalitet(posisjonskvalitetType);
			ar5.setKvalitet(posisjonskvalitetPropertyType);

			// posisjonskvalitetType.setNøyaktighet(new BigInteger("10"));
			// not used on flate
			posisjonskvalitetType.setSynbarhet(makeAbstractType("" + a.getSynbarhet(), "Synbarhet"));
			posisjonskvalitetType.setMålemetode(makeAbstractType("" + a.getMaalemetode(), "Målemetode"));

		}

		Calendar datafangstdato = Calendar.getInstance(nLocale);
		ar5.setDatafangstdato(getXmlCalender(datafangstdato.getTime()));
		
		RegistreringsversjonPropertyType registreringsversjon = new RegistreringsversjonPropertyType();
		RegistreringsversjonType registreringsversjonsss = new RegistreringsversjonType();
		registreringsversjonsss.setProdukt("ar5");
		registreringsversjonsss.setVersjon("0.1");
		
		registreringsversjon.setRegistreringsversjon(registreringsversjonsss );
		ar5.setRegistreringsversjon(registreringsversjon);
		
		ar5.setOppdateringsdato(getXmlCalender(datafangstdato.getTime()));
		ar5.setOpphav("opphav");
		ar5.setVerifiseringsdato(getXmlCalender(datafangstdato.getTime()));

		ar5.getProsesshistories().add("Prosesshistories");


		PolygonPatchType newPolygonPatch = of.createPolygonPatchType();

		AbstractRingPropertyType valueExterior = of.createAbstractRingPropertyType();

		RingType eee = of.createRingType();

		MultiLineString exteriorLineStrings = tg.exteriorLineStrings;
		for (int i = 0; i < exteriorLineStrings.getNumGeometries(); i++) {
			LineString ln = (LineString) exteriorLineStrings.getGeometryN(i);
			CurvePropertyType cs = creatCurveType(ln, useXlinKHref);
			eee.getCurveMembers().add(cs);

		}

		JAXBElement<RingType> valueRingType = of.createRing(eee);

		valueExterior.setAbstractRing(valueRingType);
		newPolygonPatch.setExterior(valueExterior);
		JAXBElement<? extends AbstractSurfacePatchType> e = of.createPolygonPatch(newPolygonPatch);

		SurfacePatchArrayPropertyType sss = of.createSurfacePatchArrayPropertyType();

		sss.getAbstractSurfacePatches().add(e);

		JAXBElement<SurfacePatchArrayPropertyType> surfacePatchArrayPropertyType = of.createPatches(sss);

		SurfacePropertyType surfacePropertyType = new SurfacePropertyType();

		SurfaceType surfaceType = of.createSurfaceType();

		surfaceType.setPatches(surfacePatchArrayPropertyType);

		JAXBElement<SurfaceType> surfaceTypeAbs = of.createSurface(surfaceType);

		surfacePropertyType.setAbstractSurface(surfaceTypeAbs);

		SurfacePropertyType surfacePropertyTypeValid = of.createSurfacePropertyType();
		surfacePropertyTypeValid.setAbstractSurface(surfaceTypeAbs);

		ar5.setOmråde(surfacePropertyType);

		// set bounded by
		String srsName = "urn:ogc:def:crs:EPSG::" + a.getGeo().getSRID();
		Envelope env = a.getGeo().getEnvelopeInternal();
		EnvelopeType v2 = JTS2GML321.toGML(env, srsName);
		JAXBElement<EnvelopeType> createEnvelope = of.createEnvelope(v2);
		BoundingShapeType value = new BoundingShapeType();
		value.setEnvelope(createEnvelope);
		ar5.setBoundedBy(value);

		return ar5;

	}

	/*
	 * This is just a testing implementation just to be able handle common line strings between polygons.
	 * 
	 * The geometry is of type surface. Topology is not used here. The CurvePropertyType is used for the geometry
	 * 
	 * The value attributes is just testing
	 * 
	 * (non-Javadoc)
	 * 
	 * @see no.geonorge.skjema.changelogfile.util.IConvert2ArealressursType# convert2GrenseType(java.util.UUID, java.lang.Object)
	 */
	@Override
	public ArealressursGrenseType convert2GrenseType(UUID lokalId, Object input) {

		LineString borderLineString = (LineString) input;

		ArealressursGrenseType ar5Border = ofar5.createArealressursGrenseType();

		IdentifikasjonPropertyType v = new IdentifikasjonPropertyType();
		IdentifikasjonType v1 = new IdentifikasjonType();
		v1.setLokalId(lokalId.toString());
		v1.setNavnerom("no.skogoglandskap.ar5.ArealressursGrense");
		v1.setVersjonId("1.0");

		v.setIdentifikasjon(v1);
		ar5Border.setIdentifikasjon(v);
		

		// ar5.setArealtype(arealtype);

		Calendar datafangstdato = Calendar.getInstance(nLocale);
		ar5Border.setDatafangstdato(getXmlCalender(datafangstdato.getTime()));

		CurvePropertyType curvePropertyType2 = creatCurveType(borderLineString, false);

		ar5Border.setGrense(curvePropertyType2);

		ar5Border.setAvgrensingType(makeAbstractType("12", "ArealressursAvgrensingType"));
		
		RegistreringsversjonPropertyType registreringsversjon = new RegistreringsversjonPropertyType();
		RegistreringsversjonType registreringsversjonsss = new RegistreringsversjonType();
		registreringsversjonsss.setProdukt("ar5");
		registreringsversjonsss.setVersjon("0.1");
		
		registreringsversjon.setRegistreringsversjon(registreringsversjonsss );
		ar5Border.setRegistreringsversjon(registreringsversjon);
		
		ar5Border.setOppdateringsdato(getXmlCalender(datafangstdato.getTime()));
		ar5Border.setOpphav("opphav");
		ar5Border.setVerifiseringsdato(getXmlCalender(datafangstdato.getTime()));

		ar5Border.getProsesshistories().add("Prosesshistories");


		{
			PosisjonskvalitetPropertyType posisjonskvalitetPropertyType = new PosisjonskvalitetPropertyType();
			PosisjonskvalitetType posisjonskvalitetType = new PosisjonskvalitetType();
			posisjonskvalitetPropertyType.setPosisjonskvalitet(posisjonskvalitetType);
			ar5Border.setKvalitet(posisjonskvalitetPropertyType);

			posisjonskvalitetType.setNøyaktighet(new BigInteger("10"));
			posisjonskvalitetType.setSynbarhet(makeAbstractType("10", "Synbarhet"));
			posisjonskvalitetType.setMålemetode(makeAbstractType("12", "Målemetode"));

		}

		String srsName = "urn:ogc:def:crs:EPSG::" + borderLineString.getSRID();

		Envelope env = borderLineString.getEnvelopeInternal();
		EnvelopeType v2 = JTS2GML321.toGML(env, srsName);
		JAXBElement<EnvelopeType> createEnvelope = of.createEnvelope(v2);
		BoundingShapeType value = new BoundingShapeType();
		value.setEnvelope(createEnvelope);
		ar5Border.setBoundedBy(value);

		return ar5Border;

	}

	// move to common classes
	private XMLGregorianCalendar getXmlCalender(Date dato) {
		XMLGregorianCalendar now = null;

		GregorianCalendar gregorianCalendar = new GregorianCalendar();
		gregorianCalendar.setTime(dato);
		DatatypeFactory datatypeFactory;
		try {
			datatypeFactory = DatatypeFactory.newInstance();
			now = datatypeFactory.newXMLGregorianCalendar(gregorianCalendar);

		} catch (DatatypeConfigurationException e) {
			logger.error("Failed to convert " + dato + " to XMLGregorianCalendar");

		}

		return now;
	}

	/**
	 * A simple way create CurvePropertyType of a linstring
	 * 
	 * @param of
	 * @param borderLineString
	 * @param useXlinKHref
	 * @return
	 */
	// TODO move this to a common util method
	private CurvePropertyType creatCurveType(LineString borderLineString, boolean useXlinKHref) {

		LineStringType lineStringType = (LineStringType) JTS2GML321.toGML(borderLineString);
		LineStringSegmentType lineStringSegmentType = of.createLineStringSegmentType();
		lineStringSegmentType.setPosList(lineStringType.getPosList());
		JAXBElement<LineStringSegmentType> e = of.createLineStringSegment(lineStringSegmentType);
		SegmentsElement segments = of.createSegmentsElement();
		segments.getAbstractCurveSegments().add(e);
		CurveType createCurveType = of.createCurveType();
		createCurveType.setSegments(segments);

		CurvePropertyType curvePropertyType = null;
		curvePropertyType = of.createCurvePropertyType();

		// a hack to set id
		if (!useXlinKHref) {
			createCurveType.setId(getGmlId(borderLineString));
			String srsName = "urn:ogc:def:crs:EPSG::" + borderLineString.getSRID();
			createCurveType.setSrsName(srsName);
			JAXBElement<CurveType> abstractCurve = of.createCurve(createCurveType);
			curvePropertyType.setAbstractCurve(abstractCurve);

		} else {
			curvePropertyType.setHref("#" + getGmlId(borderLineString));
		}

		return curvePropertyType;
	}

	private String getGmlId(LineString borderLineString) {
		return "ArealressursGrense." + +borderLineString.hashCode();
	}

	/**
	 * The is helper method to set code space. Will be removed when this info is added to annotations in ths xsd
	 * 
	 * @param value
	 * @param codeSpaceType
	 * @return
	 */
	private AbstractCodeType makeAbstractType(String value, String codeSpaceType) {
		AbstractCodeType abstractCodeType = new AbstractCodeType();

		// this is moved to the the xsd, wait for accept on this
		// String codeSpace = "http://www.geosynkronisering.no/files/skjema/Arealressurs/4.5/" + codeSpaceType + ".xml";
		// abstractCodeType.setCodeSpace(codeSpace);
		abstractCodeType.setValue(value);
		return abstractCodeType;
	}

}
