package no.geonorge.skjema.changelogfile;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
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
import no.geonorge.skjema.sosi.produktspesifikasjon.Arealressurs_45.ArealressursFlateType;
import no.geonorge.skjema.sosi.produktspesifikasjon.Arealressurs_45.ArealressursGrenseType;
import no.geonorge.skjema.sosi.produktspesifikasjon.Arealressurs_45.util.InspireWayDaoDummyAr5Classes;
import no.geonorge.skjema.util.GenericMarshallerJaxb2Helper;
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
@ContextConfiguration(locations = { "/testSetup.xml", "/ChangelogfileJaxb2HelperAppContext.xml", "/GenericMarshallerJaxb2HelperAppContext.xml" })
public class TestChangelogfileJaxb2Helper {

	@Autowired
	no.geonorge.skjema.util.ChangelogfileJaxb2Helper changelogfileJaxb2Helper;
	@Autowired
	GenericMarshallerJaxb2Helper genericMarshaller;

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
	 * Test unmarshaller by using changelogfileJaxb2HelperJaxb2Helper and then arealressurs_jaxb2Helper to Ar5Grense
	 * 
	 * Use file modified file TransactionCollectionOneRowTest2_ar5.xml
	 * 
	 * @throws ParseException
	 * @throws SAXException
	 * @throws IOException
	 * @throws ParserConfigurationException
	 * @throws TransformerFactoryConfigurationError
	 * @throws TransformerException
	 */
	//@Test
	public void testTransactionCollectionGrense_unMarshal1() throws ParseException, SAXException, IOException, ParserConfigurationException,
			TransformerFactoryConfigurationError, TransformerException {

		TransactionCollection unmarshal = changelogfileJaxb2Helper.unmarshal(getClass().getResource(
				"/tmp/TransactionCollection5_test_ar5_Grense.xml").getFile());

		List<Transaction> transactions = unmarshal.getTransactions();
		for (Transaction transaction : transactions) {
			List<JAXBElement<? extends Serializable>> abstractTransactionActions = transaction.getAbstractTransactionActions();
			for (JAXBElement<? extends Serializable> jaxbElement : abstractTransactionActions) {
				Serializable value = jaxbElement.getValue();

				InsertType insertType = (InsertType) value;

				List<Object> anies = insertType.getAnies();
				for (Object object : anies) {
					com.sun.org.apache.xerces.internal.dom.ElementNSImpl delement = (ElementNSImpl) object;
					ByteArrayOutputStream out = new ByteArrayOutputStream();
					Transformer transformer = TransformerFactory.newInstance().newTransformer();
					transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
					Source source = new DOMSource(delement);
					Result target = new StreamResult(out);
					transformer.transform(source, target);
					String string = out.toString();
					ByteArrayInputStream is = new ByteArrayInputStream(string.getBytes());
					ArealressursGrenseType simpleAr5FromXml = (ArealressursGrenseType) genericMarshaller.getUnmarshaller().unmarshal(new StreamSource(is));
					is.close();

					Assert.assertEquals("simpleAr5FromXml.getAvgrensingType().getValue()", simpleAr5FromXml.getAvgrensingType().getValue(), "4206");
					Assert.assertEquals("simpleAr5FromXml.getKvalitet().getPosisjonskvalitet().getMålemetode().getValue()", simpleAr5FromXml.getKvalitet()
							.getPosisjonskvalitet().getMålemetode().getValue(), "82");

					System.out.println("eeeeeeeeeeeeeeeeeeeeeeee" + simpleAr5FromXml.getAvgrensingType().getValue());
					System.out.println("eeeeeeeeeeeeeeeeeeeeeeee" + simpleAr5FromXml.getAvgrensingType().getValue());
					System.out.println("eeeeeeeeeeeeeeeeeeeeeeee" + simpleAr5FromXml.getAvgrensingType().getValue());

				}

			}
		}

	}

	/**
	 * Test unmarshaller by using changelogfileJaxb2HelperJaxb2Helper and then arealressurs_jaxb2Helper to Ar5Grense
	 * 
	 * Use file modified file TransactionCollectionOneRowTest2_ar5.xml
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
	public void test_ar5_simple_unMarshal1() throws ParseException, SAXException, IOException, ParserConfigurationException,
			TransformerFactoryConfigurationError, TransformerException, JAXBException {

		GeometryFactory geometryFactory = new GeometryFactory();

		TransactionCollection unmarshal = changelogfileJaxb2Helper.unmarshal(getClass()
				.getResource("/tmp/TransactionCollection5_test_ar5_Flate.xml").getFile());

		List<Transaction> transactions = unmarshal.getTransactions();
		for (Transaction transaction : transactions) {
			List<JAXBElement<? extends Serializable>> abstractTransactionActions = transaction.getAbstractTransactionActions();
			for (JAXBElement<? extends Serializable> jaxbElement : abstractTransactionActions) {
				Serializable valueInsert = jaxbElement.getValue();

				InsertType insertType = (InsertType) valueInsert;

				List<Object> anies = insertType.getAnies();
				for (Object object : anies) {

					com.sun.org.apache.xerces.internal.dom.ElementImpl delement =  (ElementImpl) object;

					
					Source source = new DOMSource(delement);
					Unmarshaller unmarshaller = genericMarshaller.getUnmarshaller();
					

					JAXBElement jaxblement = (JAXBElement) unmarshaller.unmarshal(source);
					 ArealressursFlateType simpleAr5FromXml = (ArealressursFlateType) jaxblement.getValue();
					 
					 

					JAXBElement<? extends AbstractSurfaceType> abstractSurface = simpleAr5FromXml.getOmråde().getAbstractSurface();

					PolygonType value = (PolygonType) abstractSurface.getValue();
					AbstractRingPropertyType exterior = value.getExterior();
					JAXBElement<? extends AbstractRingType> abstractRing = exterior.getAbstractRing();

					opengis.net_gml_3_2_1.LinearRingType ringType = (opengis.net_gml_3_2_1.LinearRingType) abstractRing
							.getValue();

					Polygon createPolygon  = (Polygon) GML321_2JTS.toJTS(ringType);

					Assert.assertTrue(createPolygon.getArea() > 0.0);

				}

			}
		}

	}


}
