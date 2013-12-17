package no.geonorge.skjema.changelogfile;

import java.io.IOException;
import java.util.ArrayList;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;

import junit.framework.Assert;
import no.geonorge.skjema.changelogfile.util.ChangeLogMarshallerHelper;
import no.geonorge.skjema.changelogfile.util.ChangeLogResult;
import no.geonorge.skjema.changelogfile.util.WSFOperation;
import no.geonorge.skjema.sosi.produktspesifikasjon.Arealressurs_45.ArealressursFlateType;
import no.geonorge.skjema.util.gml_geos.geoserver.GML321_2JTS;
import opengis.net.gml_3_2_1.gml.AbstractRingPropertyType;
import opengis.net.gml_3_2_1.gml.AbstractRingType;
import opengis.net.gml_3_2_1.gml.AbstractSurfaceType;
import opengis.net.gml_3_2_1.gml.PolygonType;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.xml.sax.SAXException;

import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.io.ParseException;

/**
 * A class for testing of unmarshaling of Changelog files with ArealressursFlateType.
 *   
 * @author lars
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/testSetup.xml", "/geosyncBaseMarshallerAppContext.xml" })
public class TestChangelogUnmarshalAr5 {

	@Autowired
	ChangeLogMarshallerHelper changelogfileJaxb2Helper;


	/**
	 * Test reading off a file with only inserts
	 * 
	 * @throws ParseException
	 * @throws SAXException
	 * @throws IOException
	 * @throws ParserConfigurationException
	 * @throws TransformerFactoryConfigurationError
	 * @throws TransformerException
	 * @throws JAXBException
	 */
	@Test
	public void test_read_file_Cxxx_6_ar5() throws ParseException, SAXException, IOException, ParserConfigurationException,
			TransformerFactoryConfigurationError, TransformerException, JAXBException {

		TransactionCollection unmarshal = changelogfileJaxb2Helper.unmarshal(getClass().getResource("/tmp/file_Cxxx_6_ar5.xml").getFile());

		// get all rows
		Assert.assertEquals("To few rows found for insert ", 4, changelogfileJaxb2Helper.getChangeLogResult(unmarshal, null).wfsOerationList.size());

		// get ar5 rows
		Class<?>[] rowTypes = {ArealressursFlateType.class};
		ChangeLogResult result = changelogfileJaxb2Helper.getChangeLogResult(unmarshal, rowTypes );

		ArrayList<WSFOperation> insertList = result.wfsOerationList;
		
		Assert.assertEquals("To few rows found for insert ", 2, insertList.size());

		for (WSFOperation object : insertList) {
				ArealressursFlateType simpleAr5FromXml = (ArealressursFlateType) object.product;
				System.out.println("Found area:" + simpleAr5FromXml.getOmråde());

				JAXBElement<? extends AbstractSurfaceType> abstractSurface = simpleAr5FromXml.getOmråde().getAbstractSurface();

				PolygonType value = (PolygonType) abstractSurface.getValue();
				AbstractRingPropertyType exterior = value.getExterior();
				JAXBElement<? extends AbstractRingType> abstractRing = exterior.getAbstractRing();

				opengis.net.gml_3_2_1.gml.LinearRingType ringType = (opengis.net.gml_3_2_1.gml.LinearRingType) abstractRing.getValue();

				Polygon createPolygon = (Polygon) GML321_2JTS.toJTS(ringType);
				
				// System.out.println(createPolygon.toText());

				Assert.assertTrue(createPolygon.getArea() > 0.0);

		}

	}
	
	
	
	/**
	 * Test reading off a file with only updates
	 * 
	 * @throws ParseException
	 * @throws SAXException
	 * @throws IOException
	 * @throws ParserConfigurationException
	 * @throws TransformerFactoryConfigurationError
	 * @throws TransformerException
	 * @throws JAXBException
	 */
//	@Test
	public void test_read_file_xxUx_6_ar5() throws ParseException, SAXException, IOException, ParserConfigurationException,
			TransformerFactoryConfigurationError, TransformerException, JAXBException {

		TransactionCollection unmarshal = changelogfileJaxb2Helper.unmarshal(getClass().getResource("/tmp/file_xxUx_6_ar5.xml").getFile());

		ChangeLogResult result = changelogfileJaxb2Helper.getChangeLogResult(unmarshal, null);

		ArrayList<WSFOperation> updateList = result.wfsOerationList;
		
		Assert.assertEquals("To few rows found for insert ", 1, updateList.size());


	}
	
	/**
	 * Test reading off a file with only deletes
	 * 
	 * @throws ParseException
	 * @throws SAXException
	 * @throws IOException
	 * @throws ParserConfigurationException
	 * @throws TransformerFactoryConfigurationError
	 * @throws TransformerException
	 * @throws JAXBException
	 */
//	@Test
	public void test_read_file_xxxD_6_ar5() throws ParseException, SAXException, IOException, ParserConfigurationException,
			TransformerFactoryConfigurationError, TransformerException, JAXBException {

		TransactionCollection unmarshal = changelogfileJaxb2Helper.unmarshal(getClass().getResource("/tmp/file_xxxD_6_ar5.xml").getFile());

		ChangeLogResult result = changelogfileJaxb2Helper.getChangeLogResult(unmarshal, null);

		ArrayList<WSFOperation> updateList = result.wfsOerationList;
		
		Assert.assertEquals("To few rows found for insert ", 1, updateList.size());


	}



}
