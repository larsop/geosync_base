package itest.no.skogoglanskap.provider.Arealressurs;

/**
 * This class is used to test moving data from provider to subscriber by using ArealressursFlateType and a changelog file

 * 
 * This code is adjusted to how SkogogandLandskap store their data
 *  
 */

// 
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.stream.StreamResult;

import no.geonorge.skjema.changelogfile.TransactionCollection;
import no.geonorge.skjema.changelogfile.util.ChangeLogMarshallerHelper;
import no.geonorge.skjema.changelogfile.util.ChangeLogResult;
import no.geonorge.skjema.changelogfile.util.SupportedWFSOperationType;
import no.geonorge.skjema.changelogfile.util.WSFOperation;
import no.geonorge.skjema.sosi.produktspesifikasjon.Arealressurs_45.ArealressursFlateType;
import no.geonorge.skjema.sosi.produktspesifikasjon.Arealressurs_45.ArealressursGrenseType;
import no.geonorge.subscriber.Arealressurs.Ar5FlateEntity;
import no.skogoglandskap.ar5.SimpleAr5Transformerer1;
import no.skogoglandskap.datamodel.postgres.provider.Ar5FlateProvSimpleFeatureEntity;
import no.skogoglandskap.util.BuildTopo;
import no.skogoglandskap.util.PolygonFeature;
import no.skogoglandskap.util.TopoGeometry;
import opengis.net.gml_3_2_1.gml.AbstractCurveSegmentType;
import opengis.net.gml_3_2_1.gml.AbstractCurveType;
import opengis.net.gml_3_2_1.gml.AbstractFeatureCollectionType;
import opengis.net.gml_3_2_1.gml.AbstractFeatureCollectionType2;
import opengis.net.gml_3_2_1.gml.AbstractFeatureType;
import opengis.net.gml_3_2_1.gml.AbstractRingPropertyType;
import opengis.net.gml_3_2_1.gml.AbstractSurfacePatchType;
import opengis.net.gml_3_2_1.gml.AbstractSurfaceType;
import opengis.net.gml_3_2_1.gml.CompositeCurveType;
import opengis.net.gml_3_2_1.gml.CompositeSurfaceType;
import opengis.net.gml_3_2_1.gml.CurvePropertyType;
import opengis.net.gml_3_2_1.gml.CurveType;
import opengis.net.gml_3_2_1.gml.FeaturePropertyType;
import opengis.net.gml_3_2_1.gml.LineStringSegmentType;
import opengis.net.gml_3_2_1.gml.OrientableCurveType;
import opengis.net.gml_3_2_1.gml.PolygonPatchType;
import opengis.net.gml_3_2_1.gml.RingType;
import opengis.net.gml_3_2_1.gml.SegmentsElement;
import opengis.net.gml_3_2_1.gml.SurfacePatchArrayPropertyType;
import opengis.net.gml_3_2_1.gml.SurfacePropertyType;
import opengis.net.gml_3_2_1.gml.SurfaceType;
import opengis.net.wfs_2_0.wfs.FeatureCollectionType;
import opengis.net.wfs_2_0.wfs.Member;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opengis.referencing.operation.TransformException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.oxm.Marshaller;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/testSetup.xml", "/geosyncBaseMarshallerAppContext.xml" })
public class TestGenerateInsertChangelogFile {
	private Logger logger = Logger.getLogger(TestGenerateInsertChangelogFile.class);

	private java.text.SimpleDateFormat formatter = new java.text.SimpleDateFormat("yyyyMMdd");
	Locale nLocale = new Locale.Builder().setLanguage("nb").setRegion("NO").build();

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
		int gmlFlateId = 0;
		for (TopoGeometry aa : geoWithCommonLinestrings) {

			// convert the surface object
			String name = "" + aa.exteriorLineStrings.toText();
			ArealressursFlateType ar5Surface = testConver.convert2FlateFromProv(UUID.nameUUIDFromBytes(name.getBytes()), aa, useXlinKHref,
					"no.skogoglandskap.ar5.ArealressursFlate." + gmlFlateId++, getCalendarObject(getDatofangstDato(formatter)),
					getCalendarObject(getVerifiseringsDato2(formatter)));

			subscriberSurfcaeData.add(ar5Surface);

		}

		// this is the list of common border objects
		ArrayList<ArealressursGrenseType> subscriberBorderData = new ArrayList<>();

		// create the grense objects
		int gmlGrenseId = 0;

		for (LineString ls : lineStringsNew) {
			// convert the border object
			String text = ls.toText();

			getDatofangstDato(formatter);

			ArealressursGrenseType ar5Border = testConver.convert2GrenseType(UUID.nameUUIDFromBytes(text.getBytes()), ls,
					"no.skogoglandskap.ar5.ArealressursGrense." + gmlGrenseId++, getCalendarObject(getDatofangstDato(formatter)),
					getCalendarObject(getVerifiseringsDato2(formatter)));
			subscriberBorderData.add(ar5Border);

		}

		boolean testOneSingleFile = true;

		opengis.net.gml_3_2_1.gml.ObjectFactory of = new opengis.net.gml_3_2_1.gml.ObjectFactory();

		AbstractFeatureCollectionType2 fc2 = new AbstractFeatureCollectionType2();
		JAXBElement<AbstractFeatureCollectionType2> createAbstractFeatureCollection = of.createTypeFeatureCollection(fc2);

		if (testOneSingleFile) {

			ArrayList<WSFOperation> wfsOperationList1 = new ArrayList<>();

			int operationNumber1 = 0;

			for (int i = 0; i < subscriberBorderData.size();) {

				ArealressursGrenseType product = subscriberBorderData.get(i++);
				WSFOperation wfs = new WSFOperation(operationNumber1++, SupportedWFSOperationType.InsertType, product);
				wfsOperationList1.add(wfs);

				QName _AbstractFeature_QNAME = new QName("http://skjema.geonorge.no/SOSI/produktspesifikasjon/Arealressurs/4.5", "ArealressursGrense");
				JAXBElement<AbstractFeatureType> g = new JAXBElement<AbstractFeatureType>(_AbstractFeature_QNAME, AbstractFeatureType.class, null, product);
				FeaturePropertyType e = new FeaturePropertyType();
				e.setAbstractFeature(g);
				fc2.getFeatureMembers().add(e);

			}

			for (int i = 0; i < subscriberSurfcaeData.size();) {
				ArealressursFlateType product = subscriberSurfcaeData.get(i++);
				WSFOperation wfs = new WSFOperation(operationNumber1++, SupportedWFSOperationType.InsertType, product);
				wfsOperationList1.add(wfs);

				QName _AbstractFeature_QNAME = new QName("http://skjema.geonorge.no/SOSI/produktspesifikasjon/Arealressurs/4.5", "ArealressursFlate");
				JAXBElement<AbstractFeatureType> g = new JAXBElement<AbstractFeatureType>(_AbstractFeature_QNAME, AbstractFeatureType.class, null, product);
				FeaturePropertyType e = new FeaturePropertyType();
				e.setAbstractFeature(g);
				fc2.getFeatureMembers().add(e);

			}

			String name;

			if (!useXlinKHref) {
				name = "/tmp/fil1_implicit_topo_OrientableCurve_f1.xml";
			} else {
				name = "/tmp/fil2_topo_xlink_OrientableCurve_f2.xml";
			}

			{
				Marshaller marshaller = changelogfileJaxb2Helper.getMarshaller();

				FileOutputStream os = null;
				try {
					String tname = name.replace("/tmp/", "/tmp/gml_");
					tname = tname.replace(".xml", ".gml");

					os = new FileOutputStream(tname);

					marshaller.marshal(createAbstractFeatureCollection, new StreamResult(os));

				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} finally {
					if (os != null) {
						os.close();
					}
				}

			}

			marshallList(wfsOperationList1, name);

			umarshalFileAndTest(orgArea, areaAfterWriteRead, name);

		} else {

			ArrayList<WSFOperation> wfsOperationList1 = new ArrayList<>();
			ArrayList<WSFOperation> wfsOperationList2 = new ArrayList<>();

			int operationNumber1 = 0;

			for (int i = 0; i < subscriberBorderData.size();) {

				ArealressursGrenseType product = subscriberBorderData.get(i++);
				System.out.println(i + " product.getId():" + product.getId());
				wfsOperationList1.add(new WSFOperation(operationNumber1++, SupportedWFSOperationType.InsertType, product));
				if ("no.skogoglandskap.ar5.ArealressursGrense.1".equals(product.getId())) {
					wfsOperationList2.add(new WSFOperation(operationNumber1++, SupportedWFSOperationType.UpdateType, product));

					System.out.println("test");

				}

				if (i < subscriberBorderData.size()) {
					product = subscriberBorderData.get(i++);
					System.out.println(i + " product.getId():" + product.getId());

					wfsOperationList1.add(new WSFOperation(operationNumber1++, SupportedWFSOperationType.InsertType, product));
					if ("no.skogoglandskap.ar5.ArealressursGrense.1".equals(product.getId())) {
						wfsOperationList2.add(new WSFOperation(operationNumber1++, SupportedWFSOperationType.UpdateType, product));
						System.out.println("test");

					}
				}

			}

			for (int i = 0; i < subscriberSurfcaeData.size();) {
				ArealressursFlateType product = subscriberSurfcaeData.get(i++);
				wfsOperationList1.add(new WSFOperation(operationNumber1++, SupportedWFSOperationType.InsertType, product));
				if (i < subscriberSurfcaeData.size()) {
					product = subscriberSurfcaeData.get(i++);
					wfsOperationList2.add(new WSFOperation(operationNumber1++, SupportedWFSOperationType.InsertType, product));
				}
			}

			if (!useXlinKHref) {
				marshallList(wfsOperationList1, "/tmp/leveranse1_implicit_topo_OrientableCurve.xml");
				marshallList(wfsOperationList2, "/tmp/leveranse2_implicit_topo_OrientableCurve.xml");

			} else {
				marshallList(wfsOperationList1, "/tmp/leveranse1_topo_xlink_OrientableCurve.xml");
				marshallList(wfsOperationList2, "/tmp/leveranse2_topo_xlink_OrientableCurve.xml");

			}

		}

	}

	private Calendar getCalendarObject(Date datofangstDato) throws java.text.ParseException {
		Calendar datafangstdato = Calendar.getInstance(nLocale);

		datafangstdato.setTime(datofangstDato);
		return datafangstdato;
	}

	private void marshallList(ArrayList<WSFOperation> wfsOperationList1, String name) throws ParseException, IllegalAccessException, InvocationTargetException,
			IOException {
		Locale nLocale = new Locale.Builder().setLanguage("nb").setRegion("NO").build();
		Calendar timestamp = Calendar.getInstance(nLocale);

		TransactionCollection transactionCollection = changelogfileJaxb2Helper.getTransactionCollection(wfsOperationList1, timestamp);

		Marshaller marshaller = changelogfileJaxb2Helper.getMarshaller();

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
	}

	private void umarshalFileAndTest(double orgArea, double areaAfterWriteRead, String name) throws IOException, TransformerFactoryConfigurationError,
			TransformerException {
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

				CompositeCurveType compositeCurveType = (CompositeCurveType) curvePropertyType.getAbstractCurve().getValue();

				// HACK to test
				CurveType curve = (CurveType) compositeCurveType.getCurveMembers().get(0).getAbstractCurve().getValue();

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

			if (abstractSurface.getValue() instanceof opengis.net.gml_3_2_1.gml.SurfaceType) {
				opengis.net.gml_3_2_1.gml.SurfaceType value = (SurfaceType) abstractSurface.getValue();
				JAXBElement<SurfacePatchArrayPropertyType> surfacePropertyType = value.getPatches();
				SurfacePatchArrayPropertyType value2 = surfacePropertyType.getValue();

				areaAfterWriteRead = resolvePatches(areaAfterWriteRead, geometryFactory, hrefLinkList, value2);

			} else {
				opengis.net.gml_3_2_1.gml.CompositeSurfaceType value = (CompositeSurfaceType) abstractSurface.getValue();
				SurfacePropertyType surfacePropertyType = value.getSurfaceMembers().get(0);

				JAXBElement<? extends AbstractSurfaceType> patches = surfacePropertyType.getAbstractSurface();

				SurfaceType value4 = (SurfaceType) patches.getValue();

				SurfacePatchArrayPropertyType patches3 = value4.getPatches().getValue();

				areaAfterWriteRead = resolvePatches(areaAfterWriteRead, geometryFactory, hrefLinkList, patches3);
			}

		}

		Assert.assertEquals(orgArea, areaAfterWriteRead, 0);
	}

	private double resolvePatches(double areaAfterWriteRead, GeometryFactory geometryFactory, Hashtable<String, Coordinate[]> hrefLinkList,
			SurfacePatchArrayPropertyType patches3) {
		List<JAXBElement<? extends AbstractSurfacePatchType>> abstractSurfacePatches = (List<JAXBElement<? extends AbstractSurfacePatchType>>) patches3
				.getAbstractSurfacePatches();

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
					AbstractCurveType curv = curvePropertyType.getAbstractCurve().getValue();
					if (curv instanceof CurveType) {
						CurveType curve = (CurveType) curv;
						coordinates = getCoordinats(curve);
					} else if (curv instanceof opengis.net.gml_3_2_1.gml.CompositeCurveType) {
						CompositeCurveType curve = (CompositeCurveType) curv;
						List<CurvePropertyType> curvePropertyType2 = curve.getCurveMembers();
						for (CurvePropertyType curvePropertyType4 : curvePropertyType2) {

							if (curvePropertyType4.getAbstractCurve() == null) {
								coordinates = hrefLinkList.get(curvePropertyType4.getHref());
							} else {
								CurveType curve2 = (CurveType) curvePropertyType4.getAbstractCurve().getValue();
								coordinates = getCoordinats(curve2);
							}

							icounter = icounter + coordinates.length;

							lineStringList.add(coordinates);

						}

						coordinates = new Coordinate[0];

					} else {
						OrientableCurveType curve = (OrientableCurveType) curv;
						CurvePropertyType curvePropertyType2 = curve.getBaseCurve();
						if (curvePropertyType2.getAbstractCurve() == null) {
							coordinates = hrefLinkList.get(curvePropertyType2.getHref());

							if (curve.getOrientation() != null && curve.getOrientation().equals("-")) {
								if (logger.isDebugEnabled()) {
									logger.error("Switch coordinate oriatatior for " + coordinates[0] + "......" + coordinates[coordinates.length - 1]);
								}

								Coordinate[] reversedArray = new Coordinate[coordinates.length];
								int j = 0;
								for (int i = coordinates.length - 1; i >= 0; i--) {
									reversedArray[j++] = coordinates[i];
								}
								coordinates = reversedArray;
							}

						} else {
							CurveType curve2 = (CurveType) curvePropertyType2.getAbstractCurve().getValue();
							coordinates = getCoordinats(curve2);
						}
					}

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
					// TODO find how to handle this
					// This test should not be needed because all the line strings should be sorted already
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
		return areaAfterWriteRead;
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

		borderPolygon = (Polygon) switchXY(borderPolygon);

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
		ar5f.setDatafangstdato(getDatofangstDato(formatter));
		ar5f.setVerifiseringsdato(getVerifiseringsDato(formatter));

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

	private Date getVerifiseringsDato(java.text.SimpleDateFormat formatter) throws java.text.ParseException {
		return formatter.parse("20120502");
	}

	private Date getDatofangstDato(java.text.SimpleDateFormat formatter) throws java.text.ParseException {
		return formatter.parse("20140312");
	}

	private Date getVerifiseringsDato2(java.text.SimpleDateFormat formatter) throws java.text.ParseException {
		return formatter.parse("20140312");
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

		borderPolygon = (Polygon) switchXY(borderPolygon);

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
		ar5f.setDatafangstdato(getDatofangstDato(formatter));
		ar5f.setVerifiseringsdato(getVerifiseringsDato(formatter));

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

	private static Geometry switchXY(Polygon inputLineString) throws RuntimeException {

		Geometry clone = (Geometry) inputLineString.clone();
		// Coordinate[] coordinates = clone.getCoordinates();
		// for (Coordinate coordinate : coordinates) {
		// double newX = coordinate.y;
		// coordinate.y = coordinate.x;
		// coordinate.x = newX;
		// }

		return clone;
	}

}
