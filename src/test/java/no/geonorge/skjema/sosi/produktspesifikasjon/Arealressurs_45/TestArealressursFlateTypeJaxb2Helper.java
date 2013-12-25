package no.geonorge.skjema.sosi.produktspesifikasjon.Arealressurs_45;






import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBElement;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import no.geonorge.skjema.changelogfile.util.ChangeLogMarshallerHelper;
import no.geonorge.skjema.sosi.produktspesifikasjon.Arealressurs_45.util.InspireWayDaoDummyAr5Classes;
import no.geonorge.skjema.util.gml_geos.inspire.GML321_2JTS;
import opengis.net.gml_3_2_1.gml.AbstractRingPropertyType;
import opengis.net.gml_3_2_1.gml.AbstractRingType;
import opengis.net.gml_3_2_1.gml.AbstractSurfaceType;
import opengis.net.gml_3_2_1.gml.CurvePropertyType;
import opengis.net.gml_3_2_1.gml.LineStringType;
import opengis.net.gml_3_2_1.gml.PolygonType;
import opengis.net.gml_3_2_1.gml.PosListElement;
import opengis.net.gml_3_2_1.gml.RingType;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.oxm.Marshaller;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
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
	public void testSimpleArealressursFlateType_marshal() throws ParseException, SAXException, IOException, ParserConfigurationException {


	    File temp = File.createTempFile("ArealressursFlateType", ".gml"); 
	    
		ArealressursFlateType simpleAr5 = daoDummyAr5Classes.simpleAr5Flate();
		
		OutputStream os = new FileOutputStream(temp);
		
		org.springframework.oxm.jaxb.Jaxb2Marshaller marshaller = (Jaxb2Marshaller) arealressursFlateTypeJaxb2Helper.getMarshaller();

		Map<String, Object> properties = new HashMap<String, Object>();
		properties.put(javax.xml.bind.Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
		((org.springframework.oxm.jaxb.Jaxb2Marshaller) marshaller).setMarshallerProperties(properties);

		marshaller.marshal(simpleAr5, new StreamResult(os) );
		
		
		//setProperty("jaxb.xsi.type", Boolean.FALSE);
		
		os.close();
		
		
		System.out.println(temp.getAbsolutePath());
		testSimpleAr5_unmarshalArealressursFlateType_marshal(temp.getAbsolutePath());

	}

	


	
	private void testSimpleAr5_unmarshalArealressursFlateType_marshal(String FILE_NAME) throws IOException {
		FileInputStream is = new FileInputStream(FILE_NAME);

		javax.xml.bind.JAXBElement unmarshal = (JAXBElement) arealressursFlateTypeJaxb2Helper.getUnmarshaller().unmarshal(new StreamSource(is));
		
		
		ArealressursFlateType  simpleAr5  = (ArealressursFlateType ) unmarshal.getValue();
		
		
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
			PosListElement value3 = value2.getPosList();
			List<Double> values = value3.getValues();
			
			Coordinate[] coordinates = new Coordinate[values.size()/2];

			int coordinatei = 0; 
			for (int i = 0; i < coordinates.length; i++) {
				coordinates[i] = new Coordinate(values.get(coordinatei++), new Double(values.get(coordinatei++)));
			}

			LineString g1 = new GeometryFactory().createLineString(coordinates);
			Polygon createPolygon = new GeometryFactory().createPolygon(g1.getCoordinateSequence());
			// System.out.println("gamel:"+border.toString());
			// System.out.println("ny:"+createPolygon.toString());

			Assert.assertTrue(daoDummyAr5Classes.borderPolygon.equalsExact(createPolygon));

		}

	}

	/**
	 * Test marshal
	 */
	@Test
	public void testSimpleArealressursGrenseType_marshal() throws Exception {


	    File temp = File.createTempFile("ArealressursGrenseType", ".gml"); 
	    
		ArealressursGrenseType simpleAr5 = daoDummyAr5Classes.simpleAr5Grense();
		
		OutputStream os = new FileOutputStream(temp);
		
		arealressursFlateTypeJaxb2Helper.getMarshaller().marshal(simpleAr5, new StreamResult(os) );
		
		os.close();
		
		System.out.println(temp.getAbsolutePath());

	}

}
