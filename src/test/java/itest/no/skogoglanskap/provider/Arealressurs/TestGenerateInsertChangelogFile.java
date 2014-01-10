package itest.no.skogoglanskap.provider.Arealressurs;

/**
 * This class is used to test moving data from provider to subscriber by using ArealressursFlateType and a changelog file
 * 
 * This code is adjusted to how SkogogandLandskap store their data
 *  
 */

// 
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.UUID;

import javax.xml.transform.stream.StreamResult;

import no.geonorge.skjema.changelogfile.TransactionCollection;
import no.geonorge.skjema.changelogfile.util.ChangeLogMarshallerHelper;
import no.geonorge.skjema.changelogfile.util.SupportedWFSOperationType;
import no.geonorge.skjema.changelogfile.util.WSFOperation;
import no.geonorge.skjema.sosi.produktspesifikasjon.Arealressurs_45.ArealressursFlateType;
import no.geonorge.skjema.sosi.produktspesifikasjon.Arealressurs_45.ArealressursGrenseType;
import no.skogoglandskap.ar5.SimpleAr5Transformerer1;
import no.skogoglandskap.datamodel.postgres.provider.Ar5FlateProvSimpleFeatureEntity;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.oxm.Marshaller;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateList;
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

		SimpleAr5Transformerer1 testConver = new SimpleAr5Transformerer1();

		GeometryFactory gf = new GeometryFactory();

		ArrayList<LineString> lineStringsNew = testConver.findAllCommonLinestrings(providerData);
		
		testConver.replaceGeoWithCommonLinestrings(providerData, lineStringsNew);

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


}
