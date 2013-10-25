package no.geonorge.skjema.changelogfile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.PropertyException;
import javax.xml.namespace.QName;
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
import net.opengis.fes._2.ComparisonOpsType;
import net.opengis.fes._2.FilterType;
import net.opengis.fes._2.LiteralType;
import net.opengis.fes._2.LogicOpsType;
import net.opengis.fes._2.PropertyIsLikeType;
import no.geonorge.skjema.changelogfile.util.ChangeLogMarshallerHelper;
import no.geonorge.skjema.changelogfile.util.ChangeLogResult;
import no.geonorge.skjema.changelogfile.util.SupportedWFSOperationType;
import no.geonorge.skjema.changelogfile.util.WSFOperation;
import no.geonorge.skjema.sosi.produktspesifikasjon.Arealressurs_45.ArealressursFlateType;
import no.geonorge.skjema.sosi.produktspesifikasjon.Arealressurs_45.ArealressursGrenseType;
import no.geonorge.skjema.sosi.produktspesifikasjon.Arealressurs_45.util.InspireWayDaoDummyAr5Classes;
import no.geonorge.skjema.util.gml_geos.geoserver.GML321_2JTS;

import opengis.net.wfs_2_0.wfs.AbstractTransactionActionType;
import opengis.net.wfs_2_0.wfs.DeleteType;
import opengis.net.wfs_2_0.wfs.InsertType;
import opengis.net.wfs_2_0.wfs.ObjectFactory;
import opengis.net.wfs_2_0.wfs.Property;
import opengis.net.wfs_2_0.wfs.Property.ValueReference;
import opengis.net.wfs_2_0.wfs.Transaction;
import opengis.net.wfs_2_0.wfs.UpdateActionType;
import opengis.net.wfs_2_0.wfs.UpdateType;
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

		ArrayList<WSFOperation> wfsOperationList = new ArrayList<>();
		ArrayList<Object> arrayList = new ArrayList<>();
		arrayList.add(daoDummyAr5Classes.simpleAr5Flate());
		// arrayList.add(daoDummyAr5Classes.simpleAr5Flate());

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


		no.geonorge.skjema.changelogfile.TransactionCollection transactionCollection = createwfsdata(wfsOperationList);

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

	private no.geonorge.skjema.changelogfile.TransactionCollection createwfsdata(ArrayList<WSFOperation> wfsOperationList) throws ParseException {
		int handle = 0;

		InsertType insertType = null;
		UpdateType updateType = null;
		DeleteType deleteType = null;
		AbstractTransactionActionType lastType = null;
		AbstractTransactionActionType newType = null;
		SupportedWFSOperationType currentWfsType = null;

		no.geonorge.skjema.changelogfile.TransactionCollection transactionCollection = new no.geonorge.skjema.changelogfile.TransactionCollection();

		List<Transaction> transactionsList = transactionCollection.getTransactions();
		Transaction transaction = new Transaction();
		transactionsList.add(transaction);

		ObjectFactory wfsObjectFactory = new opengis.net.wfs_2_0.wfs.ObjectFactory();

		net.opengis.fes._2.ObjectFactory fesObjectFactory = new net.opengis.fes._2.ObjectFactory();

		// no.geonorge.skjema.sosi.produktspesifikasjon.Arealressurs_45.ObjectFactory ar5objectFactory = new
		// no.geonorge.skjema.sosi.produktspesifikasjon.Arealressurs_45.ObjectFactory();

		for (WSFOperation wsfOperation : wfsOperationList) {
			boolean aswitchHappend = false;

			switch (wsfOperation.wfsOperationType) {
			case InsertType: {
				if (currentWfsType == null || !currentWfsType.equals(wsfOperation.wfsOperationType)) {
					insertType = new InsertType();
					insertType.setHandle("" + handle++);
					aswitchHappend = true;
					newType = insertType;
					currentWfsType = wsfOperation.wfsOperationType;
				}
				insertType.getAnies().add(wsfOperation.product);
			}
				break;

			case UpdateType: {

				// TODO fix this bad written code
				if (currentWfsType == null || !currentWfsType.equals(wsfOperation.wfsOperationType)) {
					updateType = new UpdateType();
					updateType.setHandle("" + handle++);

					// TODO remove this and make it generic
					QName qname = new QName("http://skjema.geonorge.no/SOSI/produktspesifikasjon/Arealressurs/4.5", "ArealressursFlate");
					updateType.setTypeName(qname);

					updateType.setSrsName("test");
					aswitchHappend = true;
					newType = updateType;
					currentWfsType = wsfOperation.wfsOperationType;
				}

				// set filter
				FilterType filterType = new FilterType();
				PropertyIsLikeType propertyIsLike = new PropertyIsLikeType();
				propertyIsLike.setWildCard("setWildCard");
				propertyIsLike.setSingleChar("setSingleChar");
				propertyIsLike.setEscapeChar("EscapeChar");
				JAXBElement<? extends ComparisonOpsType> comparisonOps = fesObjectFactory.createPropertyIsLike(propertyIsLike);

				LiteralType createLiteralValue = new LiteralType();
				createLiteralValue.getContent().add("whattiqeal");
				JAXBElement<LiteralType> createLiteral = fesObjectFactory.createLiteral(createLiteralValue);
				propertyIsLike.getExpressions().add(createLiteral);

				filterType.setComparisonOps(comparisonOps);

				updateType.setFilter(filterType);

				Property property = new Property();
				property.setValue("valueReferanse.value");
				ValueReference valueReferanse = new ValueReference();
				valueReferanse.setValue("valueReferanse.value");
				valueReferanse.setAction(UpdateActionType.INSERT_BEFORE);
				property.setValueReference(valueReferanse);

				// add a property
				updateType.getProperties().add(property);

			}

				break;
			case DeleleType: {
				// TODO fix this bad written code
				if (currentWfsType == null || !currentWfsType.equals(wsfOperation.wfsOperationType)) {
					deleteType = new DeleteType();
					deleteType.setHandle("" + handle++);

					// TODO remove this and make it generic
					QName qname = new QName("http://skjema.geonorge.no/SOSI/produktspesifikasjon/Arealressurs/4.5", "ArealressursFlate");
					deleteType.setTypeName(qname);
					aswitchHappend = true;
					newType = deleteType;
					currentWfsType = wsfOperation.wfsOperationType;
				}

				FilterType filterType = new FilterType();
				PropertyIsLikeType propertyIsLike = new PropertyIsLikeType();
				propertyIsLike.setWildCard("setWildCard");
				propertyIsLike.setSingleChar("setSingleChar");
				propertyIsLike.setEscapeChar("EscapeChar");
				JAXBElement<? extends ComparisonOpsType> comparisonOps = fesObjectFactory.createPropertyIsLike(propertyIsLike);

				LiteralType createLiteralValue = new LiteralType();
				createLiteralValue.getContent().add("whattiqeal");
				JAXBElement<LiteralType> createLiteral = fesObjectFactory.createLiteral(createLiteralValue);
				propertyIsLike.getExpressions().add(createLiteral);

				filterType.setComparisonOps(comparisonOps);

				deleteType.setFilter(filterType);

			}

			default:
				break;

			}

			if (aswitchHappend && lastType != null) {
				JAXBElement<? extends Serializable> abstractTransactionActions = wfsObjectFactory.createAbstractTransactionAction(lastType);
				transaction.getAbstractTransactionActions().add(abstractTransactionActions);
			}

			lastType = newType;

		}

		if (lastType != null) {
			JAXBElement<? extends Serializable> abstractTransactionActions = wfsObjectFactory.createAbstractTransactionAction(lastType);
			transaction.getAbstractTransactionActions().add(abstractTransactionActions);
		}
		return transactionCollection;
	}
}
