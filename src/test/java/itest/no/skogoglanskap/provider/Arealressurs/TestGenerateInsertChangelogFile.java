package itest.no.skogoglanskap.provider.Arealressurs;

/**
 * This class is used to test moving data from provider to subscriber by using ArealressursFlateType and a changelog file
 * 
 * This code is adjusted to how SkogogandLandskap store their data
 *  
 */

// 
import java.io.FileOutputStream;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.UUID;

import javax.xml.bind.JAXBElement;
import javax.xml.transform.stream.StreamResult;

import no.geonorge.skjema.changelogfile.TransactionCollection;
import no.geonorge.skjema.changelogfile.util.ChangeLogMarshallerHelper;
import no.geonorge.skjema.changelogfile.util.IConvert2ArealressursType;
import no.geonorge.skjema.changelogfile.util.SupportedWFSOperationType;
import no.geonorge.skjema.changelogfile.util.WSFOperation;
import no.geonorge.skjema.sosi.produktspesifikasjon.Arealressurs_45.ArealressursFlateType;
import no.geonorge.skjema.sosi.produktspesifikasjon.Arealressurs_45.ArealressursGrenseType;
import no.geonorge.skjema.sosi.produktspesifikasjon.Arealressurs_45.IdentifikasjonPropertyType;
import no.geonorge.skjema.sosi.produktspesifikasjon.Arealressurs_45.IdentifikasjonType;
import no.geonorge.skjema.sosi.produktspesifikasjon.Arealressurs_45.PosisjonskvalitetPropertyType;
import no.geonorge.skjema.sosi.produktspesifikasjon.Arealressurs_45.PosisjonskvalitetType;
import no.geonorge.skjema.sosi.produktspesifikasjon.Arealressurs_45.util.InspireWayDaoDummyAr5Classes;
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

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.oxm.Marshaller;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.sun.imageio.plugins.common.BogusColorSpace;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateList;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;
import com.vividsolutions.jts.operation.overlay.snap.GeometrySnapper;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/testSetup.xml", "/geosyncBaseMarshallerAppContext.xml" })
public class TestGenerateInsertChangelogFile {

	@Autowired
	private ChangeLogMarshallerHelper changelogfileJaxb2Helper;
	
	
	

	/**
	 * Test transfomation of Ar5FlateProvSimpleFeatureEntity ArealressursFlateType used the changelog file
	 * 
	 * @throws Exception
	 */
	@Test
	public void testMappingOfAr5FlateProvSimpleFeatureEntity() throws Exception {

		// add 2 polygons with a commmon border
		ArrayList<Ar5FlateProvSimpleFeatureEntity> providerData = new ArrayList<>();
		providerData.add(addPolygonOne());
		providerData.add(addPolygonTwo());

		Assert.assertTrue("To few rows created " + providerData.size(), providerData.size() > 0);

		SimpleConverImpl testConver = new SimpleConverImpl();

		GeometryFactory gf = new GeometryFactory();

		GeometrySnapper geometrySnapper = null;

		double computeOverlaySnapTolerance = 0.00001;

		// LineMerger lm = new LineMerger();
		ArrayList<LineString> lineStringsOrg = new ArrayList<LineString>();
		// find all line strings in all geometries
		

		Integer srid = null;

		for (Ar5FlateProvSimpleFeatureEntity aa : providerData) {

			Polygon p = (Polygon) aa.getGeo();
			
			// not good coding
			if (srid == null ) {
				srid = p.getSRID();
			}

			LineString lineString = getLineString(p.getExteriorRing(), gf);
			

			// // test add single linestrings
			// Coordinate[] coordinates = lineString.getCoordinates();
			// for (int i = 0; i < coordinates.length - 1; i++) {
			//
			// Coordinate[] cp = { coordinates[i], coordinates[i + 1] };
			// LineString createLineString = gf.createLineString(cp);
			// lineStringsOrg.add(createLineString);
			//
			// }

			lineStringsOrg.add(lineString);

			for (int i = 0; i < p.getNumInteriorRing(); i++) {
				LineString ls = getLineString(p.getInteriorRingN(i), gf);

				lineStringsOrg.add(ls);

			}

		}

		for (int i = 0; i < lineStringsOrg.size(); i++) {
			Geometry g = lineStringsOrg.get(i);
			if (g instanceof LineString) {
				System.out.println("Test Used " + g.getUserData() + ":" + g.toText());
			} else {
				System.out.println("Test Not used " + g.getUserData() + ":" + g.toText());
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
				
				System.out.println("Used " + g.getUserData() + ":" + g.toText());
			} else {
				System.out.println("Not used " + g.getUserData() + ":" + g.toText());
			}
		}

		// Collection mergedLineStrings = lm.getMergedLineStrings();
		// for (Object a : mergedLineStrings) {
		// Geometry g = (Geometry) a;
		// if (g instanceof LineString) {
		// g.setUserData(g.hashCode());
		// lineStringsNew.add((LineString) g);
		// System.out.println("Used " + g.getUserData() + ":" + g.toText());
		// } else {
		// System.out.println("Not used " + g.getUserData() + ":" + g.toText());
		// }
		//
		// }

		// craete a collection of new linsetrings
		// MultiLineString geometryCollectionNewMultiLineString = gf.createMultiLineString(lineStringsNew.toArray(new LineString[lineStringsNew.size()]));

		// GeometrySnapper geometrySnapper = new GeometrySnapper(commonLinsStringList);

		// replace the borders with the new linstrings that are common for all polygons
		for (Ar5FlateProvSimpleFeatureEntity aa : providerData) {

			Polygon p = (Polygon) aa.getGeo();

			// get exterior ring
			LineString exteriorLineString = getLineString(p.getExteriorRing(), gf);

			ArrayList<LineString> exteriorLineStringtmp = getLineStringsThatIntersects(lineStringsNew, exteriorLineString);

			LinearRing shell = findCommonLineString(gf, exteriorLineStringtmp, exteriorLineString);

			LinearRing[] holes = new LinearRing[p.getNumInteriorRing()];

			for (int i = 0; i < p.getNumInteriorRing(); i++) {
				LineString holeLineString = getLineString(p.getInteriorRingN(i), gf);
				ArrayList<LineString> holeLineStringTmp = getLineStringsThatIntersects(lineStringsNew, holeLineString);
				LinearRing h = findCommonLineString(gf, holeLineStringTmp, holeLineString);
				holes[i] = h;
			}

			Geometry newtopoGeometry = gf.createPolygon(shell, holes);
			newtopoGeometry.setSRID(srid);

			aa.setGeo(newtopoGeometry);
			System.out.println(newtopoGeometry.toText());
		}

		// all geometries now use the same geomtries so we are ready to Flate and Border polygons for the changelog

		// this the list of surface objects
		
		
		ArrayList<ArealressursFlateType> subscriberSurfcaeData = new ArrayList<>();

		// convert the Flate object from local provider format to the format used by the changelog files
		for (Ar5FlateProvSimpleFeatureEntity aa : providerData) {

			// convert the surface object
			ArealressursFlateType ar5Surface = testConver.convert2FlateFromProv(UUID.randomUUID(),aa);
			subscriberSurfcaeData.add(ar5Surface);

		}

		// this the list of common border objects
		ArrayList<ArealressursGrenseType> subscriberBorderData = new ArrayList<>();

		// create the grense objects
		for (LineString ls : lineStringsNew) {
			// convert the border object
			ArealressursGrenseType ar5Border = testConver.convert2GrenseType(UUID.randomUUID(),ls);
			subscriberBorderData.add(ar5Border);

		}

		// prepare data for change log

		ArrayList<WSFOperation> wfsOperationList = new ArrayList<>();
		int operationNumber = 0;

		for (Object object : subscriberBorderData) {
			WSFOperation wfs = new WSFOperation(operationNumber++, SupportedWFSOperationType.InsertType, object);
			wfsOperationList.add(wfs);
		}

		for (Object object : subscriberSurfcaeData) {
			WSFOperation wfs = new WSFOperation(operationNumber++, SupportedWFSOperationType.InsertType, object);
			wfsOperationList.add(wfs);
		}

		TransactionCollection transactionCollection = changelogfileJaxb2Helper.getTransactionCollection(wfsOperationList);

		Marshaller marshaller = changelogfileJaxb2Helper.getMarshaller();

		FileOutputStream os = null;
		try {
			os = new FileOutputStream("/tmp/log2.xml");
			marshaller.marshal(transactionCollection, new StreamResult(os));

		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} finally {
			if (os != null) {
				os.close();
			}
		}

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

	// @Test
	public void testSimpleLinestringUnion() throws Exception {

		// two overlapping lines
		WKTReader reader = new WKTReader();

		// LINESTRING (59.310381 4.90523, 59.310381 4.905582)
		// LINESTRING (59.310381 4.905582, 59.310381533333334 4.905355308333333, 59.310381 4.90523)
		// LineString l1 = (LineString) reader.read("LINESTRING (59.310381 4.90523, 59.310381 4.905582)");
		// LineString l2 = (LineString) reader.read("LINESTRING (59.310381 4.905582, 59.310381533333334 4.905355308333333, 59.310381 4.90523)");

		LineString l1 = (LineString) reader.read("LINESTRING (59.310381 4.90523, 59.310381 4.905582)");
		LineString l2 = (LineString) reader.read("LINESTRING (59.310381 4.90523, 59.310381533333334 4.905355308333333, 59.310381 4.905582)");

		System.out.println("Hashcode before :" + l1.hashCode() + ":" + l2.hashCode());

		GeometrySnapper geometrySnapper = new GeometrySnapper(l1);

		// double computeOverlaySnapTolerance = 0.000001;
		// gir
		// 1.0E-6:LINESTRING (59.310381 4.90523, 59.310381 4.905582)
		// 1.0E-6:LINESTRING (59.310381 4.90523, 59.310381 4.905582, 59.310381 4.905583)

		// double computeOverlaySnapTolerance = 0.00001;
		// gir
		// 1.0E-5:LINESTRING (59.310381 4.90523, 59.310381 4.905583)
		// 1.0E-5:LINESTRING (59.310381 4.90523, 59.310381 4.905583)

		double computeOverlaySnapTolerance = 0.00001;

		Geometry[] snap = geometrySnapper.snap(l1, l2, computeOverlaySnapTolerance);
		for (Geometry geometry : snap) {
			System.out.println(computeOverlaySnapTolerance + ":" + geometry.toText());
		}

		l1 = (LineString) snap[0];
		l2 = (LineString) snap[1];

		System.out.println("Hashcode after :" + l1.hashCode() + ":" + l2.hashCode());

		System.out.println(l1.union(l2));

		System.out.println(l1.equalsTopo(l2));

	}

	/**
	 * Replace the linerstring with linerstring from commonLinsStringList
	 * 
	 * @param gf
	 * @param lineStringsListToUse
	 * @param lineString
	 * @return
	 */
	private LinearRing findCommonLineString(GeometryFactory gf, ArrayList<LineString> lineStringsListToUse, LineString lineString) {

		// ArrayList<LineString> resultLinsStringList = new ArrayList<>();
		//
		// Coordinate[] coordinates = lineString.getCoordinates();
		// Coordinate lastOkPoint = coordinates[0];
		//
		// for (int i = 0; i < coordinates.length - 1; i++) {
		//
		// Coordinate[] cp = { lastOkPoint, coordinates[i + 1] };
		// LineString createLineString = gf.createLineString(cp);
		//
		//
		// for (LineString orgLs : lineStringsListToUse) {
		//
		// }
		//
		//
		// // TODO fins a better way to this
		// // get the intersection from the list of common ponts
		// Geometry intersection = lineStringsListToUse.intersection(createLineString);
		// //if (commonLinsStringList.contains(createLineString)) {
		// if (intersection.getLength() == createLineString.getLength()) {
		// //if (intersection.equalsTopo(createLineString)) {
		// coordinatesNewList.add(lastOkPoint);
		// lastOkPoint = coordinates[i + 1];
		//
		// }
		//
		// }
		//
		// for (int zi = 0; zi < coordinates.length - 1; zi++) {
		//
		// Coordinate[] cp = { lastOkPoint, coordinates[zi + 1] };
		// LineString createLineString = gf.createLineString(cp);
		//
		// // TODO fins a better way to this
		// // get the intersection from the list of common ponts
		// Geometry intersection = lineStringsListToUse.intersection(createLineString);
		// //if (commonLinsStringList.contains(createLineString)) {
		// if (intersection.getLength() == createLineString.getLength()) {
		// //if (intersection.equalsTopo(createLineString)) {
		// coordinatesNewList.add(lastOkPoint);
		// lastOkPoint = coordinates[zi + 1];
		//
		// }
		//
		// }
		//
		//
		// // add first point to close polygon
		// coordinatesNewList.add(coordinatesNewList.get(0));
		// Coordinate[] coordinatesNewListArray = coordinatesNewList.toArray(new Coordinate[coordinatesNewList.size()]);

		MultiLineString newLis = gf.createMultiLineString(lineStringsListToUse.toArray(new LineString[lineStringsListToUse.size()]));
		System.out.println(newLis);
		CoordinateList ls = new CoordinateList(newLis.getCoordinates());
		ls.closeRing();
		LinearRing ring = gf.createLinearRing(ls.toCoordinateArray());
		return ring;
	}

	private LineString getLineString(LineString exteriorRing, GeometryFactory gf) {
		Coordinate[] coordinates = exteriorRing.getCoordinates();
		// Coordinate[] coordinatesRemovedFirstPoint = Arrays.copyOfRange(coordinates, 1, coordinates.length);
		LineString createLineString = gf.createLineString(coordinates);
		Assert.assertTrue("Linstring is not valid ", createLineString.isValid());

		return createLineString;
	}

	/**
	 * The left polygon
	 * 
	 * @return
	 * @throws ParseException
	 * @throws java.text.ParseException
	 */
	private Ar5FlateProvSimpleFeatureEntity addPolygonOne() throws ParseException, java.text.ParseException {
		WKTReader reader = new WKTReader();


		// with a extra point on shared line
		// Polygon borderPolygon = (Polygon)
		// reader.read("POLYGON((59.31038099999999957 4.90558199999999989,59.31026539166666822 4.90555743333333449,59.31024421666666768 4.90523724166666675,59.31038099999999957 4.90523000000000042,59.3103815333333344 4.90535530833333322,59.3103815333333344 4.90535530833333322,59.31038099999999957 4.90558199999999989))");

		Polygon borderPolygon = (Polygon) reader
				.read("POLYGON((59.31038099999999957 4.90558199999999989,59.31026539166666822 4.90555743333333449,59.31024421666666768 4.90523724166666675,59.31038099999999957 4.90523000000000042,59.31038099999999957 4.90558199999999989))");

		borderPolygon.setSRID(4258);
		borderPolygon.setUserData("NO.SK.AR5:");

		Assert.assertTrue("Boder not valid", borderPolygon.isValid());

		Ar5FlateProvSimpleFeatureEntity ar5f = new Ar5FlateProvSimpleFeatureEntity();
		// what should we use this id for		
		ar5f.setId(1);
		ar5f.setGeo(borderPolygon);
		ar5f.setArtype(new Byte("30"));
		ar5f.setArtreslag(new Byte("33"));
		ar5f.setArskogbon(new Byte("14"));
		ar5f.setArgrunnf(new Byte("44"));
		ar5f.setArkartstd("AR5");
		java.text.SimpleDateFormat formatter = new java.text.SimpleDateFormat("yyyyMMdd");
		ar5f.setDatafangstdato(formatter.parse("20040801"));
		ar5f.setVerifiseringsdato(formatter.parse("20120502"));

		// Nøyaktighet is not used on flate in Ar5

		// missing from gml sosi file
		ar5f.setMaalemetode(new Byte("01"));
		ar5f.setSynbarhet(new Byte("01"));

		// not handle from gml
		// <sgm:KVALITET>82</sgm:KVALITET>
		// <sgm:OPPHAV>Skogoglandskap</sgm:OPPHAV>
		// <sgm:REGISTRERINGSVERSJON>FKB-AR5 "4.5 20140101"</sgm:REGISTRERINGSVERSJON>

		// todo handle grense
		// ArealressursGrenseType ar5g = new ArealressursGrenseType();
		return ar5f;
	}

	/**
	 * The right polygon
	 * 
	 * @return
	 * @throws ParseException
	 * @throws java.text.ParseException
	 */
	private Ar5FlateProvSimpleFeatureEntity addPolygonTwo() throws ParseException, java.text.ParseException {
		WKTReader reader = new WKTReader();

		// with a extra point on shared line
		// Polygon borderPolygon = (Polygon)
		// reader.read("POLYGON((59.31038099999999957 4.90523000000000042,59.31056000000000239 4.90555599999999981,59.31057200000000051 4.90555599999999981,59.31057200000000051 4.90558199999999989,59.31038099999999957 4.90558199999999989,59.3103815333333344 4.90535530833333322,59.31038099999999957 4.90523000000000042,59.31038099999999957 4.90523000000000042))");

		Polygon borderPolygon = (Polygon) reader
				.read("POLYGON((59.31038099999999957 4.90523000000000042,59.31056000000000239 4.90555599999999981,59.31057200000000051 4.90555599999999981,59.31057200000000051 4.90558199999999989,59.31038099999999957 4.90558199999999989,59.31038099999999957 4.90523000000000042,59.31038099999999957 4.90523000000000042))");

		borderPolygon.setSRID(4258);
		borderPolygon.setUserData("NO.SK.AR5:");

		Assert.assertTrue("Boder not valid", borderPolygon.isValid());

		Ar5FlateProvSimpleFeatureEntity ar5f = new Ar5FlateProvSimpleFeatureEntity();
		// what should we use this id for		
		ar5f.setId(2);
		ar5f.setGeo(borderPolygon);
		ar5f.setArtype(new Byte("31"));
		ar5f.setArtreslag(new Byte("33"));
		ar5f.setArskogbon(new Byte("15"));
		ar5f.setArgrunnf(new Byte("45"));
		ar5f.setArkartstd("AR5");
		java.text.SimpleDateFormat formatter = new java.text.SimpleDateFormat("yyyyMMdd");
		ar5f.setDatafangstdato(formatter.parse("20040801"));
		ar5f.setVerifiseringsdato(formatter.parse("20120502"));

		// Nøyaktighet is not used on flate in Ar5

		// missing from gml sosi file
		ar5f.setMaalemetode(new Byte("01"));
		ar5f.setSynbarhet(new Byte("01"));

		// not handle from gml
		// <sgm:KVALITET>82</sgm:KVALITET>
		// <sgm:OPPHAV>Skogoglandskap</sgm:OPPHAV>
		// <sgm:REGISTRERINGSVERSJON>FKB-AR5 "4.5 20140101"</sgm:REGISTRERINGSVERSJON>

		// todo handle grense
		// ArealressursGrenseType ar5g = new ArealressursGrenseType();
		return ar5f;
	}

	/**
	 * This is a simple class that converts data from a internal ar5format used at Skogoglandskap and the format used
	 * 
	 * @author lop
	 * 
	 */
	private class SimpleConverImpl implements IConvert2ArealressursType {

		opengis.net.gml_3_2_1.gml.ObjectFactory of = new opengis.net.gml_3_2_1.gml.ObjectFactory();
		no.geonorge.skjema.sosi.produktspesifikasjon.Arealressurs_45.ObjectFactory ofar5 = new no.geonorge.skjema.sosi.produktspesifikasjon.Arealressurs_45.ObjectFactory();

		@Override
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

			Calendar datafangstdato = Calendar.getInstance();
			datafangstdato.setTime(a.getDatafangstdato());

			ar5.setDatafangstdato(datafangstdato);

			PolygonType polygonType = (PolygonType) JTS2GML321.toGML(a.getGeo());
			JAXBElement<PolygonType> jaxbElementPolygonType = of.createPolygon(polygonType);
			SurfacePropertyType surfacePropertyType = new SurfacePropertyType();
			surfacePropertyType.setAbstractSurface(jaxbElementPolygonType);

			ar5.setOmråde(surfacePropertyType);
			
			String srsName = "urn:ogc:def:crs:EPSG::" + a.getGeo().getSRID();

			Envelope env = a.getGeo().getEnvelopeInternal();
			EnvelopeType v2 = JTS2GML321.toGML(env, srsName);
			JAXBElement<EnvelopeType> createEnvelope = of.createEnvelope(v2);
			BoundingShapeType value = new BoundingShapeType();
			value.setEnvelope(createEnvelope);
			ar5.setBoundedBy(value);
			

			return ar5;

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
			ar5Border.setDatafangstdato(datafangstdato);

			opengis.net.gml_3_2_1.gml.ObjectFactory of = new opengis.net.gml_3_2_1.gml.ObjectFactory();

			CurvePropertyType curvePropertyType2 = creatCurveType(of, borderLineString);

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

			String srsName = "urn:ogc:def:crs:EPSG::" + borderLineString .getSRID();

			Envelope env = borderLineString .getEnvelopeInternal();
			EnvelopeType v2 = JTS2GML321.toGML(env, srsName);
			JAXBElement<EnvelopeType> createEnvelope = of.createEnvelope(v2);
			BoundingShapeType value = new BoundingShapeType();
			value.setEnvelope(createEnvelope);
			ar5Border.setBoundedBy(value);

			return ar5Border;

		}

		private CurvePropertyType creatCurveType(opengis.net.gml_3_2_1.gml.ObjectFactory of, LineString borderLineString) {

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

	}


}
