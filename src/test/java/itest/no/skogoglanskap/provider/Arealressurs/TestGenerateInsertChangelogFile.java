package itest.no.skogoglanskap.provider.Arealressurs;

/**
 * This class is used to test moving data from provider to subscriber by using ArealressursFlateType and a changelog file
 * 
 * This code is adjusted to how SkogogandLandskap store their data
 *  
 */

import java.math.BigInteger;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Iterator;

import javax.xml.bind.JAXBElement;

import no.geonorge.skjema.changelogfile.util.ChangeLogMarshallerHelper;
import no.geonorge.skjema.changelogfile.util.IConvert2ArealressursType;
import no.geonorge.skjema.sosi.produktspesifikasjon.Arealressurs_45.ArealressursFlateType;
import no.geonorge.skjema.sosi.produktspesifikasjon.Arealressurs_45.ArealressursGrenseType;
import no.geonorge.skjema.sosi.produktspesifikasjon.Arealressurs_45.IdentifikasjonPropertyType;
import no.geonorge.skjema.sosi.produktspesifikasjon.Arealressurs_45.IdentifikasjonType;
import no.geonorge.skjema.sosi.produktspesifikasjon.Arealressurs_45.PosisjonskvalitetPropertyType;
import no.geonorge.skjema.sosi.produktspesifikasjon.Arealressurs_45.PosisjonskvalitetType;
import no.geonorge.skjema.sosi.produktspesifikasjon.util.SosiProduktMarshallerHelper;
import no.geonorge.skjema.util.gml_geos.inspire.JTS2GML321;
import no.geonorge.subscriber.Arealressurs.Ar5FlateEntity;
import no.skogoglandskap.datamodel.postgres.provider.Ar5FlateProvSimpleFeatureEntity;

import opengis.net.gml_3_2_1.gml.AbstractCodeType;
import opengis.net.gml_3_2_1.gml.PolygonType;
import opengis.net.gml_3_2_1.gml.SurfacePropertyType;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateArrays;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.impl.CoordinateArraySequence;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;
import com.vividsolutions.jts.operation.overlay.snap.GeometrySnapper;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/geosyncBaseMarshallerAppContext.xml" })
public class TestGenerateInsertChangelogFile {

	@Autowired
	private ChangeLogMarshallerHelper changelogfileJaxb2Helper;

	@Autowired
	private SosiProduktMarshallerHelper genericMarshaller;

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

		// this the list of surface objects
		ArrayList<ArealressursFlateType> subscriberSurfcaeData = new ArrayList<>();
		// this the list of common border objects
		ArrayList<ArealressursGrenseType> subscriberBorderData = new ArrayList<>();
		ArrayList<LineString> lineStringsOrg = new ArrayList<LineString>();

		Assert.assertTrue("To few rows created " + providerData.size(), providerData.size() > 0);

		TestConver testConver = new TestConver();

		GeometryFactory gf = new GeometryFactory();

		// find all line strings in all geometries
		for (Ar5FlateProvSimpleFeatureEntity aa : providerData) {

			Polygon p = (Polygon) aa.getGeo();

			LineString lineString = getLineString(p.getExteriorRing(), gf);
			lineStringsOrg.add(lineString);

			for (int i = 0; i < p.getNumInteriorRing(); i++) {
				LineString ls = getLineString(p.getInteriorRingN(i), gf);
				lineStringsOrg.add(ls);
			}

		}

		// find common line string
		LineString[] lineStringsTable = lineStringsOrg.toArray(new LineString[lineStringsOrg.size()]);

		MultiLineString createMultiLineString = gf.createMultiLineString(lineStringsTable);

		Geometry commonLinsStringList = createMultiLineString.union();

		for (int i = 0; i < commonLinsStringList.getNumGeometries(); i++) {
			Geometry g = commonLinsStringList.getGeometryN(i);
			System.out.println(g.getUserData() + ":" + g.toText());
		}


		//GeometrySnapper geometrySnapper = new GeometrySnapper(commonLinsStringList);

		// replace the borders with the new linstrings
		for (Ar5FlateProvSimpleFeatureEntity aa : providerData) {
			// find all line strings in all geometries
			Polygon p = (Polygon) aa.getGeo();
			LineString lineString = getLineString(p.getExteriorRing(), gf);

			LinearRing shell = findCommonLineString(gf, commonLinsStringList, lineString);
			
			LinearRing[] holes = new LinearRing[p.getNumInteriorRing()];
			for (int i = 0; i < p.getNumInteriorRing(); i++) {
				LineString ls = getLineString(p.getInteriorRingN(i), gf);
				LinearRing h = findCommonLineString(gf, commonLinsStringList, ls);
				holes[i] = h;
			}

			Geometry newtopoGeometry = gf.createPolygon(shell);
			
			aa.setGeo(newtopoGeometry);
			//System.out.println(newtopoGeometry.toText());
		}
		
		// we a list common linstrings and they are used  

	}

	private LinearRing findCommonLineString(GeometryFactory gf, Geometry commonLinsStringList, LineString lineString) {
		ArrayList<Coordinate> coordinatesNewList = new ArrayList<>();

		Coordinate[] coordinates = lineString.getCoordinates();
		Coordinate lastOkPoint = coordinates[0];

		for (int i = 0; i < coordinates.length - 1; i++) {

			Coordinate[] cp = { lastOkPoint, coordinates[i + 1] };
			LineString createLineString = gf.createLineString(cp);

			// TODO fins a better way to this
			// get the intersection from the list of common ponts
			Geometry intersection = commonLinsStringList.intersection(createLineString);
			//if (commonLinsStringList.contains(createLineString)) {
			if (intersection.getLength() == createLineString.getLength()) {
			//if (intersection.equalsTopo(createLineString)) {
				coordinatesNewList.add(lastOkPoint);
				lastOkPoint = coordinates[i + 1];
				
			}

		}

		// add first point to close polygon
		coordinatesNewList.add(coordinatesNewList.get(0));
		Coordinate[] coordinatesNewListArray = coordinatesNewList.toArray(new Coordinate[coordinatesNewList.size()]);

		LinearRing shell = gf.createLinearRing(coordinatesNewListArray);
		return shell;
	}

	private LineString getLineString(LineString exteriorRing, GeometryFactory gf) {
		Coordinate[] coordinates = exteriorRing.getCoordinates();
		// Coordinate[] coordinatesRemovedFirstPoint = Arrays.copyOfRange(coordinates, 1, coordinates.length);
		LineString createLineString = gf.createLineString(coordinates);
		Assert.assertTrue("Linstring is not valid ", createLineString.isValid());

		return createLineString;
	}

	private Ar5FlateProvSimpleFeatureEntity addPolygonOne() throws ParseException, java.text.ParseException {
		WKTReader reader = new WKTReader();

		// with a extra point on shared line
		//Polygon borderPolygon = (Polygon) reader.read("POLYGON((59.31038099999999957 4.90558199999999989,59.31026539166666822 4.90555743333333449,59.31024421666666768 4.90523724166666675,59.31038099999999957 4.90523000000000042,59.3103815333333344 4.90535530833333322,59.3103815333333344 4.90535530833333322,59.31038099999999957 4.90558199999999989))");

		Polygon borderPolygon = (Polygon) reader.read("POLYGON((59.31038099999999957 4.90558199999999989,59.31026539166666822 4.90555743333333449,59.31024421666666768 4.90523724166666675,59.31038099999999957 4.90523000000000042,59.31038099999999957 4.90558199999999989))");
		
		
		borderPolygon.setSRID(4258);
		borderPolygon.setUserData("NO.SK.AR5:");

		Assert.assertTrue("Boder not valid", borderPolygon.isValid());

		Ar5FlateProvSimpleFeatureEntity ar5f = new Ar5FlateProvSimpleFeatureEntity();
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

	private Ar5FlateProvSimpleFeatureEntity addPolygonTwo() throws ParseException, java.text.ParseException {
		WKTReader reader = new WKTReader();

		// with a extra point on shared line
		// Polygon borderPolygon = (Polygon) reader.read("POLYGON((59.31038099999999957 4.90523000000000042,59.31056000000000239 4.90555599999999981,59.31057200000000051 4.90555599999999981,59.31057200000000051 4.90558199999999989,59.31038099999999957 4.90558199999999989,59.3103815333333344 4.90535530833333322,59.31038099999999957 4.90523000000000042,59.31038099999999957 4.90523000000000042))");

		Polygon borderPolygon = (Polygon) reader.read("POLYGON((59.31038099999999957 4.90523000000000042,59.31056000000000239 4.90555599999999981,59.31057200000000051 4.90555599999999981,59.31057200000000051 4.90558199999999989,59.31038099999999957 4.90558199999999989,59.31038099999999957 4.90523000000000042,59.31038099999999957 4.90523000000000042))");
		
		borderPolygon.setSRID(4258);
		borderPolygon.setUserData("NO.SK.AR5:");

		Assert.assertTrue("Boder not valid", borderPolygon.isValid());

		Ar5FlateProvSimpleFeatureEntity ar5f = new Ar5FlateProvSimpleFeatureEntity();
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
	private class TestConver implements IConvert2ArealressursType {

		opengis.net.gml_3_2_1.gml.ObjectFactory of = new opengis.net.gml_3_2_1.gml.ObjectFactory();
		no.geonorge.skjema.sosi.produktspesifikasjon.Arealressurs_45.ObjectFactory ofar5 = new no.geonorge.skjema.sosi.produktspesifikasjon.Arealressurs_45.ObjectFactory();

		@Override
		public ArealressursFlateType convert2FlateFromProv(Object input) {

			Ar5FlateProvSimpleFeatureEntity a = (Ar5FlateProvSimpleFeatureEntity) input;

			ArealressursFlateType ar5 = ofar5.createArealressursFlateType();

			IdentifikasjonPropertyType v = new IdentifikasjonPropertyType();
			IdentifikasjonType v1 = new IdentifikasjonType();
			v1.setLokalId(System.currentTimeMillis() + "");
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

			return ar5;

		}

		private AbstractCodeType makeAbstractType(String value, String codeSpaceType) {
			AbstractCodeType abstractCodeType = new AbstractCodeType();

			// String codeSpaceTypeNew = codeSpaceType.substring(0, 1).toUpperCase() + codeSpaceType.substring(1).toLowerCase();

			String codeSpace = "http://www.geosynkronisering.no/files/skjema/Arealressurs/4.5/" + codeSpaceType + ".xml";

			abstractCodeType.setCodeSpace(codeSpace);
			abstractCodeType.setValue(value);
			return abstractCodeType;
		}

		@Override
		public ArealressursGrenseType convert2GrenseType(Object input) {
			// TODO Auto-generated method stub
			return null;
		}

	}

	/**
	 * Test transfomation of Ar5FlateProvSimpleFeatureEntity ArealressursFlateType used the changelog file
	 * 
	 * @throws Exception
	 */
	// @Test
	public void tmptestMappingOfAr5FlateProvSimpleFeatureEntity() throws Exception {

		// add 2 polygons with a commmon border
		ArrayList<Ar5FlateProvSimpleFeatureEntity> providerData = new ArrayList<>();
		providerData.add(addPolygonOne());
		providerData.add(addPolygonTwo());

		// this the list of surface objects
		ArrayList<ArealressursFlateType> subscriberSurfcaeData = new ArrayList<>();
		// this the list of common border objects
		ArrayList<ArealressursGrenseType> subscriberBorderData = new ArrayList<>();
		ArrayList<LineString> lineStrings = new ArrayList<LineString>();

		Assert.assertTrue("To few rows created " + providerData.size(), providerData.size() > 0);

		TestConver testConver = new TestConver();

		GeometryFactory gf = new GeometryFactory();

		// convert from local provider format to the format used by the changelog files
		for (Ar5FlateProvSimpleFeatureEntity aa : providerData) {

			// convert the surface object
			ArealressursFlateType ar5 = testConver.convert2FlateFromProv(aa);
			subscriberSurfcaeData.add(ar5);

			Assert.assertNotNull("new object should not be null", ar5);

			Assert.assertEquals(aa.getArkartstd(), ar5.getKartstandard().getValue());

			Assert.assertEquals("" + aa.getArtype(), ar5.getArealtype().getValue());

			// Assert.assertEquals(aa.getGeo().getArea(), borderPolygon.getArea(),0);

			// the border object from the simple feature object

			Polygon p = (Polygon) aa.getGeo();

			LineString lineString = getLineString(p.getExteriorRing(), gf);
			lineStrings.add(lineString);

			for (int i = 0; i < p.getNumInteriorRing(); i++) {
				LineString ls = getLineString(p.getInteriorRingN(i), gf);
				lineStrings.add(ls);
			}

		}

		// find common border
		// convert from local provider format to the format used by the changelog files

		LineString[] lineStringsTable = lineStrings.toArray(new LineString[lineStrings.size()]);

		MultiLineString createMultiLineString = gf.createMultiLineString(lineStringsTable);

		Geometry union = createMultiLineString.union();

		for (int i = 0; i < union.getNumGeometries(); i++) {
			Geometry g = union.getGeometryN(i);
			System.out.println(g.getUserData() + ":" + g.toText());
		}

	}

}
