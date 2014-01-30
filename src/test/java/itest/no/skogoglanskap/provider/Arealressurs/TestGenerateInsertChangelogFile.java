package itest.no.skogoglanskap.provider.Arealressurs;

/**
 * This class is used to test moving data from provider to subscriber by using ArealressursFlateType and a changelog file

 * 
 * This code is adjusted to how SkogogandLandskap store their data
 *  
 */

// 
import java.awt.geom.Area;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.UUID;

import javax.xml.bind.JAXBElement;
import javax.xml.transform.stream.StreamResult;

import no.geonorge.skjema.changelogfile.TransactionCollection;
import no.geonorge.skjema.changelogfile.util.ChangeLogMarshallerHelper;
import no.geonorge.skjema.changelogfile.util.ChangeLogResult;
import no.geonorge.skjema.changelogfile.util.SupportedWFSOperationType;
import no.geonorge.skjema.changelogfile.util.WSFOperation;
import no.geonorge.skjema.sosi.produktspesifikasjon.Arealressurs_45.ArealressursFlateType;
import no.geonorge.skjema.sosi.produktspesifikasjon.Arealressurs_45.ArealressursGrenseType;
import no.skogoglandskap.util.BuildTopo;
import no.skogoglandskap.ar5.SimpleAr5Transformerer1;
import no.skogoglandskap.datamodel.postgres.provider.Ar5FlateProvSimpleFeatureEntity;
import no.skogoglandskap.util.PolygonFeature;
import no.skogoglandskap.util.TopoGeometry;
import opengis.net.gml_3_2_1.gml.AbstractCurveSegmentType;
import opengis.net.gml_3_2_1.gml.AbstractRingPropertyType;
import opengis.net.gml_3_2_1.gml.AbstractSurfacePatchType;
import opengis.net.gml_3_2_1.gml.AbstractSurfaceType;
import opengis.net.gml_3_2_1.gml.CurvePropertyType;
import opengis.net.gml_3_2_1.gml.CurveType;
import opengis.net.gml_3_2_1.gml.LineStringSegmentType;
import opengis.net.gml_3_2_1.gml.PolygonPatchType;
import opengis.net.gml_3_2_1.gml.RingType;
import opengis.net.gml_3_2_1.gml.SegmentsElement;
import opengis.net.gml_3_2_1.gml.SurfacePatchArrayPropertyType;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.oxm.Marshaller;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/testSetup.xml", "/geosyncBaseMarshallerAppContext.xml" })
public class TestGenerateInsertChangelogFile {
	private Logger logger = Logger.getLogger(TestGenerateInsertChangelogFile.class);

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
		ArrayList<PolygonFeature> providerData = new ArrayList<>();
		providerData.add(addPolygonOne());
		providerData.add(addPolygonTwo());
		
		double orgArea = addPolygonOne().getGeo().getArea() + addPolygonTwo().getGeo().getArea();
		double areaAfterWriteRead = 0;  


		Assert.assertTrue("To few rows created " + providerData.size(), providerData.size() > 0);

		SimpleAr5Transformerer1 testConver = new SimpleAr5Transformerer1();

		GeometryFactory gf = new GeometryFactory();

		// get common line strings with no duplicates
		ArrayList<LineString> lineStringsNew = BuildTopo.findAllCommonLinestrings(providerData);

		// use those line strings when creating the surface type
		// the geo hashcode is the key
		ArrayList<TopoGeometry> geoWithCommonLinestrings = BuildTopo.getGeoWithCommonLinestrings(providerData, lineStringsNew);

		// all geometries now use the same geomtries so we are ready to Flate
		// and Border polygons for the changelog

		// this the list of surface objects

		ArrayList<ArealressursFlateType> subscriberSurfcaeData = new ArrayList<>();

		boolean useXlinKHref = true;

		// convert the Flate object from local provider format to the format
		// used by the changelog files
		for (TopoGeometry aa : geoWithCommonLinestrings) {

			// convert the surface object
			ArealressursFlateType ar5Surface = testConver.convert2FlateFromProv(UUID.randomUUID(), aa, useXlinKHref);
			subscriberSurfcaeData.add(ar5Surface);

		}

		// this is the list of common border objects
		ArrayList<ArealressursGrenseType> subscriberBorderData = new ArrayList<>();

		// create the grense objects
		for (LineString ls : lineStringsNew) {
			// convert the border object
			ArealressursGrenseType ar5Border = testConver.convert2GrenseType(UUID.randomUUID(), ls);
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

		String name;

		if (!useXlinKHref) {
			name = "/tmp/fil1.xml";
		} else {
			name = "/tmp/fil2.xml";
		}

		FileOutputStream os = null;
		try {
			os = new FileOutputStream(name);
			marshaller.marshal(transactionCollection, new StreamResult(os));

		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} finally {
			if (os != null) {
				os.close();
			}
		}

		// test umarshall file
		TransactionCollection unmarshal = changelogfileJaxb2Helper.unmarshal(name);

		// get all rows
		Assert.assertEquals("To few rows found for insert ", 5, changelogfileJaxb2Helper.getChangeLogResult(unmarshal, null).wfsOerationList.size());

		// get ar5 rows
		Class<?>[] rowTypes = { ArealressursFlateType.class, ArealressursGrenseType.class };
		ChangeLogResult result = changelogfileJaxb2Helper.getChangeLogResult(unmarshal, rowTypes);

		ArrayList<WSFOperation> insertList = result.wfsOerationList;

		Assert.assertEquals("To few rows found for insert ", 5, insertList.size());

		GeometryFactory geometryFactory = new GeometryFactory();

		ArrayList<ArealressursFlateType> ar5ListeFond = new ArrayList<>();

		Hashtable<String, Coordinate[]> hrefLinkList = new Hashtable<>();

		for (WSFOperation object : insertList) {
			Object product = object.product;

			if (product instanceof ArealressursGrenseType) {
				ArealressursGrenseType simpleAr5FromXml = (ArealressursGrenseType) product;

				CurvePropertyType curvePropertyType = simpleAr5FromXml.getGrense();

				CurveType curve = (CurveType) curvePropertyType.getAbstractCurve().getValue();

				Coordinate[] coordinates = getCoordinats(curve);

				hrefLinkList.put("#" + curve.getId(), coordinates);

			} else if (product instanceof ArealressursFlateType) {

				ArealressursFlateType simpleAr5FromXml = (ArealressursFlateType) product;
				ar5ListeFond.add(simpleAr5FromXml);
			}

		}

		for (ArealressursFlateType simpleAr5FromXml : ar5ListeFond) {

			System.out.println("Found area:" + simpleAr5FromXml.getOmråde());

			JAXBElement<? extends AbstractSurfaceType> abstractSurface = simpleAr5FromXml.getOmråde().getAbstractSurface();

			opengis.net.gml_3_2_1.gml.SurfaceType value = (opengis.net.gml_3_2_1.gml.SurfaceType) abstractSurface.getValue();
			JAXBElement<SurfacePatchArrayPropertyType> patches = value.getPatches();

			List<JAXBElement<? extends AbstractSurfacePatchType>> abstractSurfacePatches = patches.getValue().getAbstractSurfacePatches();

			ArrayList<opengis.net.gml_3_2_1.gml.RingType> list = new ArrayList<>();

			for (JAXBElement<? extends AbstractSurfacePatchType> jaxbElement : abstractSurfacePatches) {
				opengis.net.gml_3_2_1.gml.PolygonPatchType value2 = (PolygonPatchType) jaxbElement.getValue();
				AbstractRingPropertyType exterior = value2.getExterior();

				opengis.net.gml_3_2_1.gml.RingType value3 = (RingType) exterior.getAbstractRing().getValue();

				list.add(value3);
			}

			// list of linestrings for for the pologon
			ArrayList<Coordinate[]> lineStringList = new ArrayList<>();

			int icounter = 0;
			for (RingType ringType : list) {

				opengis.net.gml_3_2_1.gml.RingType ssgeom = ringType;

				List<CurvePropertyType> curveMembers = ssgeom.getCurveMembers();
				for (CurvePropertyType curvePropertyType : curveMembers) {

					Coordinate[] coordinates;

					if (curvePropertyType.getAbstractCurve() != null) {
						CurveType curve = (CurveType) curvePropertyType.getAbstractCurve().getValue();
						coordinates = getCoordinats(curve);

					} else {
						coordinates = hrefLinkList.get(curvePropertyType.getHref());
						if (coordinates == null) {
							throw new RuntimeException("Failed to find coordinates for href " + curvePropertyType.getHref());
						}

						System.out.println("curvePropertyType.getHref()" + curvePropertyType.getHref() + " coordinates.length  " + coordinates.length);

					}

					icounter = icounter + coordinates.length;

					lineStringList.add(coordinates);

				}

			}

			Coordinate lastStop = null;

			Coordinate[] coordinates = new Coordinate[icounter];

			int a = 0;

			while (lineStringList.size() > 0) {
				int x = 0;
				for (; x < lineStringList.size(); x++) {
					Coordinate[] cs = lineStringList.get(x);

					if (lastStop == null) {
						lastStop = cs[cs.length - 1];
						for (int i = 0; i < cs.length; i++) {
							Coordinate coordinate = cs[i];
							coordinates[a++] = coordinate;
						}
						break;
					} else {
						if (lastStop.equals(cs[0])) {
							lastStop = cs[cs.length - 1];
							for (int i = 0; i < cs.length; i++) {
								Coordinate coordinate = cs[i];
								coordinates[a++] = coordinate;
							}
							break;

						} else if (lastStop.equals(cs[cs.length - 1])) {
							lastStop = cs[0];
							for (int i = cs.length - 1; i >= 0; i--) {
								Coordinate coordinate = cs[i];
								coordinates[a++] = coordinate;
							}
							break;
						} else {
							if (logger.isDebugEnabled()) {
								logger.error("Failed to find start and stop for :" + lastStop + " With start coordinate " + lastStop.equals(cs[0])
										+ " and end coordinate " + lastStop.equals(cs[cs.length - 1]));
							}
						}

					}
				}
				if (logger.isDebugEnabled()) {
					logger.error("New laststop is " + lastStop + " remove linstring number " + x);
				}
				lineStringList.remove(x);

			}

			LineString g1 = geometryFactory.createLineString(coordinates);

			if (logger.isDebugEnabled()) {
				logger.error("Created linstring with length " + g1.getLength());
			}

			Polygon createPolygon = geometryFactory.createPolygon(g1.getCoordinateSequence());
			
			areaAfterWriteRead = areaAfterWriteRead + createPolygon.getArea(); 

			System.out.println("createPolygon.getArea()" + createPolygon.getArea());

			Assert.assertTrue(createPolygon.getArea() > 0.0);

		}
		
		Assert.assertEquals(orgArea, areaAfterWriteRead, 0);

	}

	private Coordinate[] getCoordinats(CurveType curve) {
		Coordinate[] coordinates = null;

		SegmentsElement segments = curve.getSegments();

		List<JAXBElement<? extends AbstractCurveSegmentType>> abstractCurveSegments = segments.getAbstractCurveSegments();
		for (JAXBElement<? extends AbstractCurveSegmentType> jaxbElement : abstractCurveSegments) {

			opengis.net.gml_3_2_1.gml.LineStringSegmentType geom = (LineStringSegmentType) jaxbElement.getValue();

			if (geom != null) {

				List<Double> values = geom.getPosList().getValues();
				coordinates = new Coordinate[values.size() / 2];
				coordinates = new Coordinate[values.size() / 2];

				int ix = 0;
				for (int i = 0; i < values.size();) {
					coordinates[ix++] = new Coordinate(new Double(values.get(i++)), new Double(values.get(i++)));
				}

			} else {
				throw new RuntimeException("Not handle conevertion from GML to JTS  Polygon " + geom.getClass().getSimpleName());

			}

		}
		return coordinates;
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
		// <sgm:REGISTRERINGSVERSJON>FKB-AR5
		// "4.5 20140101"</sgm:REGISTRERINGSVERSJON>

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
		// <sgm:REGISTRERINGSVERSJON>FKB-AR5
		// "4.5 20140101"</sgm:REGISTRERINGSVERSJON>

		// todo handle grense
		// ArealressursGrenseType ar5g = new ArealressursGrenseType();
		return ar5f;
	}

}
