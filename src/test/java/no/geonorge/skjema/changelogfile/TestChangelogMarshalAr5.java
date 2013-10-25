package no.geonorge.skjema.changelogfile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import junit.framework.Assert;
import no.geonorge.skjema.changelogfile.util.ChangeLogMarshallerHelper;
import no.geonorge.skjema.changelogfile.util.ChangeLogResult;
import no.geonorge.skjema.sosi.produktspesifikasjon.Arealressurs_45.ArealressursFlateType;
import no.geonorge.skjema.sosi.produktspesifikasjon.Arealressurs_45.ArealressursGrenseType;
import no.geonorge.skjema.sosi.produktspesifikasjon.Arealressurs_45.util.InspireWayDaoDummyAr5Classes;
import no.geonorge.skjema.util.gml_geos.geoserver.GML321_2JTS;

import opengis.net.wfs_2_0.wfs.InsertType;
import opengis.net.wfs_2_0.wfs.Transaction;
import opengis.net_gml_3_2_1.AbstractRingPropertyType;
import opengis.net_gml_3_2_1.AbstractRingType;
import opengis.net_gml_3_2_1.AbstractSurfaceType;
import opengis.net_gml_3_2_1.CoordinatesType;
import opengis.net_gml_3_2_1.PolygonType;

import org.geotools.data.shapefile.shp.JTSUtilities;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opengis.annotation.UML;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.oxm.Marshaller;
import org.springframework.oxm.Unmarshaller;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.xml.sax.SAXException;

import com.sun.org.apache.xerces.internal.dom.ElementImpl;
import com.sun.org.apache.xerces.internal.dom.ElementNSImpl;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.io.ParseException;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/testSetup.xml", "/geosyncBaseMarshallerAppContext.xml" })
public class TestChangelogMarshalAr5 {

	@Autowired
	ChangeLogMarshallerHelper changelogfileJaxb2Helper;

	@Autowired
	ChangeLogMarshallerHelper genericMarshaller;

	private InspireWayDaoDummyAr5Classes daoDummyAr5Classes;

	@Before
	public void simplePolygon() throws ParseException {
		daoDummyAr5Classes = new InspireWayDaoDummyAr5Classes();
	}

	/**
	 * Test simple marshal of DAO generated TransactionCollection, send XML to standard out
	 * 
	 * @throws ParseException
	 * @throws SAXException
	 * @throws IOException
	 * @throws ParserConfigurationException
	 */
	@Test
	public void testTransactionCollection_gemreic_Marshal() throws ParseException, SAXException, IOException, ParserConfigurationException {

		no.geonorge.skjema.changelogfile.TransactionCollection simpleAr5 = new no.geonorge.skjema.changelogfile.TransactionCollection();

		List<Transaction> transactions2 = simpleAr5.getTransactions();
		Transaction e = new Transaction();
		transactions2.add(e);
		

		Marshaller marshaller = genericMarshaller.getMarshaller();

		FileOutputStream os = null;
		try {

			os = new FileOutputStream("/tmp/log.xml");
			marshaller.marshal(simpleAr5, new StreamResult(os));

		} finally {
			if (os != null) {
				os.close();
			}
		}

	}

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
		Assert.assertEquals("To few rows found for insert ", 4, changelogfileJaxb2Helper.getChangeLogResult(unmarshal, null).insertTypetList.size());

		// get ar5 rows
		Class<?>[] rowTypes = {ArealressursFlateType.class};
		ChangeLogResult result = changelogfileJaxb2Helper.getChangeLogResult(unmarshal, rowTypes );

		ArrayList<Object> insertList = result.insertTypetList;
		
		Assert.assertEquals("To few rows found for insert ", 2, insertList.size());

		for (Object object : insertList) {
				ArealressursFlateType simpleAr5FromXml = (ArealressursFlateType) object;
				System.out.println("Found area:" + simpleAr5FromXml.getOmråde());

				JAXBElement<? extends AbstractSurfaceType> abstractSurface = simpleAr5FromXml.getOmråde().getAbstractSurface();

				PolygonType value = (PolygonType) abstractSurface.getValue();
				AbstractRingPropertyType exterior = value.getExterior();
				JAXBElement<? extends AbstractRingType> abstractRing = exterior.getAbstractRing();

				opengis.net_gml_3_2_1.LinearRingType ringType = (opengis.net_gml_3_2_1.LinearRingType) abstractRing.getValue();

				Polygon createPolygon = (Polygon) GML321_2JTS.toJTS(ringType);

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

		ArrayList<Object> updateList = result.updateTypeList;
		
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

		ArrayList<Object> updateList = result.updateTypeList;
		
		Assert.assertEquals("To few rows found for insert ", 1, updateList.size());


	}



}
