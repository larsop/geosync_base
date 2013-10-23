package no.geonorge.skjema.sosi.produktspesifikasjon.Arealressurs_45;





import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import javax.xml.bind.JAXBElement;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import no.geonorge.skjema.changelogfile.util.ChangeLogMarshallerHelper;
import no.geonorge.skjema.sosi.produktspesifikasjon.Arealressurs_45.util.InspireWayDaoDummyAr5Classes;
import no.geonorge.skjema.util.gml_geos.inspire.GML321_2JTS;

import opengis.net_gml_3_2_1.AbstractRingPropertyType;
import opengis.net_gml_3_2_1.AbstractRingType;
import opengis.net_gml_3_2_1.AbstractSurfaceType;
import opengis.net_gml_3_2_1.CoordinatesType;
import opengis.net_gml_3_2_1.CurvePropertyType;
import opengis.net_gml_3_2_1.LineStringType;
import opengis.net_gml_3_2_1.PolygonType;
import opengis.net_gml_3_2_1.RingType;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.xml.sax.SAXException;


import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.io.ParseException;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/testSetup.xml", "/geosyncBaseMarshallerAppContext.xml" })
public class TestArealressursFlateTypeJaxb2Helper {


	@Autowired
	ChangeLogMarshallerHelper arealressursFlateTypeJaxb2Helper;

	private InspireWayDaoDummyAr5Classes daoDummyAr5Classes;

	@Before
	public void simplePolygon() throws ParseException {
		daoDummyAr5Classes = new InspireWayDaoDummyAr5Classes(); 
	}


	/**
	 * Test the polygon is equal after reading it from the GMl structure
	 */
	@Test
	public void testSimpleAr5() throws ParseException, SAXException, IOException, ParserConfigurationException {

		ArealressursFlateType simpleAr5 = daoDummyAr5Classes.simpleAr5Flate();

		Assert.assertNotNull("Object should not be null", simpleAr5);
		Assert.assertNotNull("Areal type should not be null", simpleAr5.getArealtype());
		Assert.assertNotNull("Område should not be null", simpleAr5.getOmråde());

		PolygonType value = (PolygonType) simpleAr5.getOmråde().getAbstractSurface().getValue();
		AbstractRingPropertyType exterior = value.getExterior();
		JAXBElement<? extends AbstractRingType> abstractRing = exterior.getAbstractRing();

		RingType ringType = (RingType) abstractRing.getValue();

		// GMLReader gmlReader = new GMLReader();
		// gmlReader.read(ringType.toString(), null);

		Polygon createPolygon = (Polygon) GML321_2JTS.toJTS(ringType);
			// System.out.println("gamel:"+border.toString());
			// System.out.println("ny:"+createPolygon.toString());

			Assert.assertTrue(daoDummyAr5Classes.borderPolygon.equalsExact(createPolygon));

		

	}

	/**
	 * Test marshal
	 */
	@Test
	public void testSimpleAr5_marshal() throws ParseException, SAXException, IOException, ParserConfigurationException {


	    File temp = File.createTempFile("InspireWayDaoDummyAr5Classes", ".tmp"); 
	    
		ArealressursFlateType simpleAr5 = daoDummyAr5Classes.simpleAr5Flate();
		OutputStream os = new FileOutputStream(temp);
		
		arealressursFlateTypeJaxb2Helper.getMarshaller().marshal(simpleAr5, new StreamResult(os) );
		
		os.close();
		
		
		System.out.println(temp.getAbsolutePath());
		testSimpleAr5_unmarshal(temp.getAbsolutePath());

	}

	
	private void testSimpleAr5_unmarshal(String FILE_NAME) throws IOException {
		FileInputStream is = new FileInputStream(FILE_NAME);

		ArealressursFlateType  simpleAr5  = (ArealressursFlateType ) arealressursFlateTypeJaxb2Helper.getUnmarshaller().unmarshal(new StreamSource(is));
		
		
		Assert.assertNotNull("Object should not be null", simpleAr5);
		Assert.assertNotNull("Areal type should not be null", simpleAr5.getArealtype());
		Assert.assertNotNull("Område should not be null", simpleAr5.getOmråde());

		JAXBElement<? extends AbstractSurfaceType> abstractSurface = simpleAr5.getOmråde().getAbstractSurface();
		
		PolygonType value = (PolygonType) abstractSurface.getValue();
		AbstractRingPropertyType exterior = value.getExterior();
		JAXBElement<? extends AbstractRingType> abstractRing = exterior.getAbstractRing();

		RingType ringType = (RingType) abstractRing.getValue();

		// GMLReader gmlReader = new GMLReader();
		// gmlReader.read(ringType.toString(), null);

		List<CurvePropertyType> curveMembers = ringType.getCurveMembers();
		for (CurvePropertyType ct : curveMembers) {
			LineStringType value2 = (LineStringType) ct.getAbstractCurve().getValue();
			CoordinatesType value3 = value2.getCoordinates();
			String value4 = value3.getValue();
			String[] split = value4.split(" ");
			Coordinate[] coordinates = new Coordinate[split.length];

			for (int i = 0; i < coordinates.length; i++) {
				String string = split[i];
				String[] split2 = string.split(",");
				coordinates[i] = new Coordinate(new Double(split2[0]), new Double(split2[1]));

			}

			LineString g1 = new GeometryFactory().createLineString(coordinates);
			Polygon createPolygon = new GeometryFactory().createPolygon(g1.getCoordinateSequence());
			// System.out.println("gamel:"+border.toString());
			// System.out.println("ny:"+createPolygon.toString());

			Assert.assertTrue(daoDummyAr5Classes.borderPolygon.equalsExact(createPolygon));

		}

	}

}
