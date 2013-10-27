package no.geonorge.skjema.changelogfile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.PropertyException;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
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
import net.opengis.fes._2.BinaryComparisonOpType;
import net.opengis.fes._2.ComparisonOpsType;
import net.opengis.fes._2.FilterType;
import net.opengis.fes._2.FunctionType;
import net.opengis.fes._2.LiteralType;
import net.opengis.fes._2.LogicOpsType;
import net.opengis.fes._2.MatchActionType;
import net.opengis.fes._2.PropertyIsLikeType;
import no.geonorge.skjema.changelogfile.util.ChangeLogMarshallerHelper;
import no.geonorge.skjema.changelogfile.util.ChangeLogResult;
import no.geonorge.skjema.changelogfile.util.SupportedWFSOperationType;
import no.geonorge.skjema.changelogfile.util.WSFOperation;
import no.geonorge.skjema.sosi.produktspesifikasjon.Arealressurs_45.ArealressursFlateType;
import no.geonorge.skjema.sosi.produktspesifikasjon.Arealressurs_45.ArealressursGrenseType;
import no.geonorge.skjema.sosi.produktspesifikasjon.Arealressurs_45.FellesegenskaperType;
import no.geonorge.skjema.sosi.produktspesifikasjon.Arealressurs_45.IdentifikasjonPropertyType;
import no.geonorge.skjema.sosi.produktspesifikasjon.Arealressurs_45.util.InspireWayDaoDummyAr5Classes;
import no.geonorge.skjema.util.gml_geos.geoserver.GML321_2JTS;

import opengis.net.wfs_2_0.wfs.AbstractTransactionActionType;
import opengis.net.wfs_2_0.wfs.DeleteType;
import opengis.net.wfs_2_0.wfs.InsertType;
import opengis.net.wfs_2_0.wfs.ObjectFactory;
import opengis.net.wfs_2_0.wfs.Property;
import opengis.net.wfs_2_0.wfs.Property.ValueReference;
import opengis.net.wfs_2_0.wfs.PropertyName;
import opengis.net.wfs_2_0.wfs.Transaction;
import opengis.net.wfs_2_0.wfs.UpdateActionType;
import opengis.net.wfs_2_0.wfs.UpdateType;
import opengis.net_gml_3_2_1.AbstractCodeType;
import opengis.net_gml_3_2_1.AbstractRingPropertyType;
import opengis.net_gml_3_2_1.AbstractRingType;
import opengis.net_gml_3_2_1.AbstractSurfaceType;
import opengis.net_gml_3_2_1.CoordinatesType;
import opengis.net_gml_3_2_1.PolygonType;
import opengis.net_gml_3_2_1.SurfacePropertyType;

import org.geotools.data.shapefile.shp.JTSUtilities;
import org.geotools.filter.FilterFactoryImpl;
import org.geotools.filter.IsEqualsToImpl;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opengis.annotation.UML;
import org.opengis.filter.FilterFactory;
import org.opengis.filter.PropertyIsEqualTo;
import org.opengis.filter.PropertyIsLike;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.oxm.Marshaller;
import org.springframework.oxm.Unmarshaller;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.interceptor.MatchAlwaysTransactionAttributeSource;
import org.xml.sax.SAXException;

import sun.util.logging.resources.logging;

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
	 * @throws InvocationTargetException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	@Test
	public void testTransactionCollection_gemreic_Marshal() throws ParseException, SAXException, IOException, ParserConfigurationException,
			IllegalAccessException, IllegalArgumentException, InvocationTargetException {

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

}
