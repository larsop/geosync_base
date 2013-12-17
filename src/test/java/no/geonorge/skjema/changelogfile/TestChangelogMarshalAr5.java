package no.geonorge.skjema.changelogfile;

import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.stream.StreamResult;

import no.geonorge.skjema.changelogfile.util.ChangeLogMarshallerHelper;
import no.geonorge.skjema.changelogfile.util.SupportedWFSOperationType;
import no.geonorge.skjema.changelogfile.util.WSFOperation;
import no.geonorge.skjema.sosi.produktspesifikasjon.Arealressurs_45.util.InspireWayDaoDummyAr5Classes;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.oxm.Marshaller;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.xml.sax.SAXException;

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
	 * @throws InvocationTargetException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	@Test
	public void testTransactionCollection_gemreic_Marshal() throws ParseException, SAXException, IOException, ParserConfigurationException,
			IllegalAccessException, IllegalArgumentException, InvocationTargetException {

		ArrayList<WSFOperation> wfsOperationList = new ArrayList<>();
		ArrayList<Object> arrayList = new ArrayList<>();
		//arrayList.add(daoDummyAr5Classes.simpleAr5Flate());
		arrayList.add(daoDummyAr5Classes.simpleAr5Grense());

		int operationNumber = 0;
		for (Object object : arrayList) {

			WSFOperation wfs = new WSFOperation(operationNumber++, SupportedWFSOperationType.InsertType, object);
			wfsOperationList.add(wfs);

		}

		for (Object object : arrayList) {

			WSFOperation wfs = new WSFOperation(operationNumber++, SupportedWFSOperationType.UpdateType, object);
			wfsOperationList.add(wfs);

		}

		for (Object object : arrayList) {

			WSFOperation wfs = new WSFOperation(operationNumber++, SupportedWFSOperationType.DeleleType, object);
			wfsOperationList.add(wfs);

		}

		TransactionCollection transactionCollection = changelogfileJaxb2Helper.getTransactionCollection(wfsOperationList);

		Marshaller marshaller = genericMarshaller.getMarshaller();

		FileOutputStream os = null;
		try {
			Map<String, Object> properties = new HashMap<String, Object>();
			properties.put(javax.xml.bind.Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
			((org.springframework.oxm.jaxb.Jaxb2Marshaller) marshaller).setMarshallerProperties(properties);

			os = new FileOutputStream("/tmp/log.xml");
			// marshaller.marshal(simpleAr5, (Result) System.out);
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
	
	
	/**
	 * Test simple marshal of DAO generated TransactionCollection, send XML to standard out
	 * 
	 * @throws ParseException
	 * @throws SAXException
	 * @throws IOException
	 * @throws ParserConfigurationException
	 * @throws InvocationTargetException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	@Test
	public void testInsert() throws ParseException, SAXException, IOException, ParserConfigurationException,
			IllegalAccessException, IllegalArgumentException, InvocationTargetException {

		ArrayList<WSFOperation> wfsOperationList = new ArrayList<>();
		ArrayList<Object> arrayList = new ArrayList<>();
		arrayList.add(daoDummyAr5Classes.simpleAr5Flate());

		int operationNumber = 0;
		for (Object object : arrayList) {

			WSFOperation wfs = new WSFOperation(operationNumber++, SupportedWFSOperationType.InsertType, object);
			wfsOperationList.add(wfs);

		}


		TransactionCollection transactionCollection = changelogfileJaxb2Helper.getTransactionCollection(wfsOperationList);

		Marshaller marshaller = genericMarshaller.getMarshaller();

		FileOutputStream os = null;
		try {
			Map<String, Object> properties = new HashMap<String, Object>();
			properties.put(javax.xml.bind.Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
			((org.springframework.oxm.jaxb.Jaxb2Marshaller) marshaller).setMarshallerProperties(properties);

			os = new FileOutputStream("/tmp/insert.xml");
			// marshaller.marshal(simpleAr5, (Result) System.out);
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


}
