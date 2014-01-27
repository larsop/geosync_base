package no.skogoglandskap.ar5;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
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
import no.geonorge.skjema.util.gml_geos.inspire.JTS2GML321;
import no.skogoglandskap.datamodel.postgres.provider.Ar5FlateProvSimpleFeatureEntity;
import opengis.net.gml_3_2_1.gml.AbstractCodeType;
import opengis.net.gml_3_2_1.gml.BoundingShapeType;
import opengis.net.gml_3_2_1.gml.CurvePropertyType;
import opengis.net.gml_3_2_1.gml.CurveType;
import opengis.net.gml_3_2_1.gml.EnvelopeType;
import opengis.net.gml_3_2_1.gml.LineStringSegmentType;
import opengis.net.gml_3_2_1.gml.LineStringType;
import opengis.net.gml_3_2_1.gml.PolygonType;
import opengis.net.gml_3_2_1.gml.SegmentsElement;
import opengis.net.gml_3_2_1.gml.SurfacePropertyType;

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
 * This class handles mapping of local provider DAO of type no.skogoglandskap.datamodel.postgres.provider.Ar5FlateProvSimpleFeatureEntity to the ArealressursFlateType and ArealressursGrenseType
 * product specification
 * 
 * This is a test class not to be used in production
 * 
 * @author lop
 * 
 */
public class SimpleAr5Transformerer1 implements IConvert2ArealressursType {

	opengis.net.gml_3_2_1.gml.ObjectFactory of = new opengis.net.gml_3_2_1.gml.ObjectFactory();
	no.geonorge.skjema.sosi.produktspesifikasjon.Arealressurs_45.ObjectFactory ofar5 = new no.geonorge.skjema.sosi.produktspesifikasjon.Arealressurs_45.ObjectFactory();
	GeometryFactory gf = new GeometryFactory();
	
	private Logger logger = Logger.getLogger(SimpleAr5Transformerer1.class);


	/*
	 * This mapping is one to for no.skogoglandskap.datamodel.postgres.provider.Ar5FlateProvSimpleFeatureEntity to ArealressursFlateType. The geometry is of type surface. Topology is not used here.
	 * 
	 * 
	 * @see no.geonorge.skjema.changelogfile.util.IConvert2ArealressursType#convert2FlateFromProv(java.util.UUID, java.lang.Object)
	 */
	public ArealressursFlateType convert2FlateFromProv(UUID lokalId, Object input) {

		Ar5FlateProvSimpleFeatureEntity a = (Ar5FlateProvSimpleFeatureEntity) input;

		ArealressursFlateType ar5 = ofar5.createArealressursFlateType();

		IdentifikasjonPropertyType v = new IdentifikasjonPropertyType();
		IdentifikasjonType v1 = new IdentifikasjonType();
		v1.setLokalId(lokalId.toString());
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

		
		
        
        
		ar5.setDatafangstdato(getXmlCalender(a.getDatafangstdato()));

		PolygonType polygonType = (PolygonType) JTS2GML321.toGML(a.getGeo());
		JAXBElement<PolygonType> jaxbElementPolygonType = of.createPolygon(polygonType);
		SurfacePropertyType surfacePropertyType = new SurfacePropertyType();
		surfacePropertyType.setAbstractSurface(jaxbElementPolygonType);

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

	// move to common classes
	private XMLGregorianCalendar getXmlCalender(Date dato) {
		XMLGregorianCalendar now = null; 
		        
		GregorianCalendar gregorianCalendar = new GregorianCalendar();
		gregorianCalendar.setTime(dato);
        DatatypeFactory datatypeFactory;
		try {
			datatypeFactory = DatatypeFactory.newInstance();
	        now = 
	                datatypeFactory.newXMLGregorianCalendar(gregorianCalendar);

		} catch (DatatypeConfigurationException e) {
			logger.error("Failed to convert " + dato + " to XMLGregorianCalendar"); 
			
		}
		
		return now;
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
	 * @see no.geonorge.skjema.changelogfile.util.IConvert2ArealressursType#convert2GrenseType(java.util.UUID, java.lang.Object)
	 */
	@Override
	public ArealressursGrenseType convert2GrenseType(UUID lokalId, Object input) {

		LineString borderLineString = (LineString) input;

		ArealressursGrenseType ar5Border = ofar5.createArealressursGrenseType();

		IdentifikasjonPropertyType v = new IdentifikasjonPropertyType();
		IdentifikasjonType v1 = new IdentifikasjonType();
		v1.setLokalId(lokalId.toString());
		v.setIdentifikasjon(v1);
		ar5Border.setIdentifikasjon(v);

		// ar5.setArealtype(arealtype);

		Calendar datafangstdato = Calendar.getInstance();
		ar5Border.setDatafangstdato(getXmlCalender(datafangstdato.getTime()));

		CurvePropertyType curvePropertyType2 = creatCurveType(borderLineString);

		ar5Border.setGrense(curvePropertyType2);

		ar5Border.setAvgrensingType(makeAbstractType("12", "ArealressursAvgrensingType"));

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

	/**
	 * A simple way create closed CurvePropertyType of a closed ring type.
	 * 
	 * @param of
	 * @param borderLineString
	 * @return
	 */
	// TODO move this to a common util method
	private CurvePropertyType creatCurveType(LineString borderLineString) {

		LineStringType lineStringType = (LineStringType) JTS2GML321.toGML(borderLineString);
		LineStringSegmentType lineStringSegmentType = of.createLineStringSegmentType();
		lineStringSegmentType.setPosList(lineStringType.getPosList());
		JAXBElement<LineStringSegmentType> e = of.createLineStringSegment(lineStringSegmentType);
		SegmentsElement segments = of.createSegmentsElement();
		segments.getAbstractCurveSegments().add(e);
		CurveType createCurveType = of.createCurveType();
		createCurveType.setSegments(segments);

		// a hack to set id
		createCurveType.setId("" + borderLineString.getUserData() + borderLineString.hashCode());
		String srsName = "urn:ogc:def:crs:EPSG::" + borderLineString.getSRID();
		createCurveType.setSrsName(srsName);
		JAXBElement<CurveType> abstractCurve = of.createCurve(createCurveType);
		CurvePropertyType curvePropertyType = of.createCurvePropertyType();
		curvePropertyType.setAbstractCurve(abstractCurve);

		return curvePropertyType;
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
		// String codeSpaceTypeNew = codeSpaceType.substring(0, 1).toUpperCase() + codeSpaceType.substring(1).toLowerCase();
		String codeSpace = "http://www.geosynkronisering.no/files/skjema/Arealressurs/4.5/" + codeSpaceType + ".xml";
		abstractCodeType.setCodeSpace(codeSpace);
		abstractCodeType.setValue(value);
		return abstractCodeType;
	}

	/**
	 * Find all common line strings from the list of provider data Works only for polygons.
	 * 
	 * @param providerData
	 *            The list of input data to work on
	 * 
	 * @return the list of common line strings where duplicates are removed.
	 * 
	 *         TODO move to a util method
	 */
	public ArrayList<LineString> findAllCommonLinestrings(ArrayList<Ar5FlateProvSimpleFeatureEntity> providerData) {

		// holds the list of all line strings
		ArrayList<LineString> lineStringsOrg = new ArrayList<LineString>();

		// the srid used
		Integer srid = null;

		// find all line strings in all geometries

		for (Ar5FlateProvSimpleFeatureEntity aa : providerData) {

			Polygon p = (Polygon) aa.getGeo();

			// not good coding
			if (srid == null) {
				srid = p.getSRID();
			}

			LineString lineString = getLineString(p.getExteriorRing());

			lineStringsOrg.add(lineString);

			for (int i = 0; i < p.getNumInteriorRing(); i++) {
				LineString ls = getLineString(p.getInteriorRingN(i));

				lineStringsOrg.add(ls);

			}

		}

		// find common line string
		// Create a collection of org linestrings
		MultiLineString geometryCollectionOrgMultiLineString = gf.createMultiLineString(lineStringsOrg.toArray(new LineString[lineStringsOrg.size()]));

		// // Get a union of all of the line strings
		Geometry newLinstringGeo = geometryCollectionOrgMultiLineString.union();

		// hold the list of linstrings after union
		ArrayList<LineString> lineStringsNew = new ArrayList<LineString>();

		// make a collection of only line strings, remove points and other geos
		for (int i = 0; i < newLinstringGeo.getNumGeometries(); i++) {
			Geometry g = newLinstringGeo.getGeometryN(i);
			if (g instanceof LineString) {
				g.setUserData(g.hashCode());
				lineStringsNew.add((LineString) g);
				g.setSRID(srid);

			}
		}

		return lineStringsNew;
	}

	/**
	 * Use the list of common line strings an recreate the polygons
	 * 
	 * @param providerData
	 * @param gf
	 * @param lineStringsNew
	 */
	public void replaceGeoWithCommonLinestrings(ArrayList<Ar5FlateProvSimpleFeatureEntity> providerData, ArrayList<LineString> lineStringsNew) {
		Integer srid = null;

		// replace the borders with the new linstrings that are common for all polygons
		for (Ar5FlateProvSimpleFeatureEntity aa : providerData) {

			Polygon p = (Polygon) aa.getGeo();

			// not good coding
			if (srid == null) {
				srid = p.getSRID();
			}

			// get exterior ring
			LineString exteriorLineString = getLineString(p.getExteriorRing());

			ArrayList<LineString> exteriorLineStringtmp = getLineStringsThatIntersects(lineStringsNew, exteriorLineString);

			LinearRing shell = createNewLinearString(exteriorLineStringtmp, exteriorLineString);

			LinearRing[] holes = new LinearRing[p.getNumInteriorRing()];

			for (int i = 0; i < p.getNumInteriorRing(); i++) {
				LineString holeLineString = getLineString(p.getInteriorRingN(i));
				ArrayList<LineString> holeLineStringTmp = getLineStringsThatIntersects(lineStringsNew, holeLineString);
				LinearRing h = createNewLinearString(holeLineStringTmp, holeLineString);
				holes[i] = h;
			}

			Geometry newtopoGeometry = gf.createPolygon(shell, holes);
			newtopoGeometry.setSRID(srid);

			aa.setGeo(newtopoGeometry);
		}
	}

	private LinearRing createNewLinearString(ArrayList<LineString> lineStringsListToUse, LineString oldLineString) {
		
		// TODO add methods for verify 

		MultiLineString newLis = gf.createMultiLineString(lineStringsListToUse.toArray(new LineString[lineStringsListToUse.size()]));
		CoordinateList ls = new CoordinateList(newLis.getCoordinates());
		ls.closeRing();
		LinearRing ring = gf.createLinearRing(ls.toCoordinateArray());
		return ring;
	}

	private ArrayList<LineString> getLineStringsThatIntersects(ArrayList<LineString> lineStringsNew, LineString thisLineString) {
		// hold the list linstrings that is of interest for this polygon
		ArrayList<LineString> lineStringstmp = new ArrayList<LineString>();

		// get all that touch touches this polygon
		for (LineString ls : lineStringsNew) {
			if (thisLineString.intersects(ls) && thisLineString.intersection(ls).getLength() > 0.00000) {
				lineStringstmp.add(ls);
			}
		}
		return lineStringstmp;
	}

	private LineString getLineString(LineString exteriorRing) {
		Coordinate[] coordinates = exteriorRing.getCoordinates();
		LineString createLineString = gf.createLineString(coordinates);
		return createLineString;
	}

}
