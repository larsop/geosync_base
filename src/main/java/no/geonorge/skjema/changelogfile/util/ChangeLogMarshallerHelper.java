package no.geonorge.skjema.changelogfile.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.namespace.QName;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.stream.StreamSource;

import no.geonorge.skjema.changelogfile.TransactionCollection;
import no.geonorge.skjema.sosi.produktspesifikasjon.Arealressurs_45.FellesegenskaperType;
import no.geonorge.skjema.sosi.produktspesifikasjon.Arealressurs_45.IdentifikasjonPropertyType;
import no.geonorge.skjema.sosi.produktspesifikasjon.util.SosiProduktMarshallerHelper;
import opengis.net.filter_2_0.filter.BinaryComparisonOpType;
import opengis.net.filter_2_0.filter.FilterType;
import opengis.net.filter_2_0.filter.LiteralType;
import opengis.net.filter_2_0.filter.MatchActionType;
import opengis.net.gml_3_2_1.gml.AbstractCodeType;
import opengis.net.gml_3_2_1.gml.SurfacePropertyType;
import opengis.net.wfs_2_0.wfs.AbstractTransactionActionType;
import opengis.net.wfs_2_0.wfs.DeleteType;
import opengis.net.wfs_2_0.wfs.InsertType;
import opengis.net.wfs_2_0.wfs.ObjectFactory;
import opengis.net.wfs_2_0.wfs.Property;
import opengis.net.wfs_2_0.wfs.Property.ValueReference;
import opengis.net.wfs_2_0.wfs.Transaction;
import opengis.net.wfs_2_0.wfs.UpdateType;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.oxm.Marshaller;
import org.springframework.oxm.Unmarshaller;
import org.springframework.oxm.XmlMappingException;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

import com.sun.xml.internal.bind.marshaller.NamespacePrefixMapper;
import com.vividsolutions.jts.io.ParseException;

/**
 * This is use to read and write files TransactionCollection http://skjema.geonorge.no/standard/geosynkronisering/1.0/endringslogg
 * 
 * The code here is now using jJAX but it will be replaces by StAX
 * 
 * This parser now also know about Marshall and Unmarshall for classes in http://skjema.geonorge.no/SOSI/produktspesifikasjon/
 * 
 * 
 * @author lars
 * 
 */
public class ChangeLogMarshallerHelper {
	static Logger logger = Logger.getLogger(ChangeLogMarshallerHelper.class);

	// used for parsing the different products in the changelog file
	@Autowired
	SosiProduktMarshallerHelper sosiProduktMarshallerHelper;

	private Marshaller marshaller;
	private Unmarshaller unmarshaller;

	
	/**
	 * A Marshaller that handles
	 * 
	 * @param m
	 */
	public ChangeLogMarshallerHelper(Marshaller m) {
		if (logger.isDebugEnabled()) {
			logger.debug("enter");
		}

		this.marshaller = m;
		this.unmarshaller = (Unmarshaller) m;

		if (logger.isDebugEnabled()) {
			logger.debug("leave");
		}

	}

	/**
	 * 
	 * @return Unmarshaller that handles classes in http://skjema.geonorge.no/standard/geosynkronisering/1.0/endringslogg
	 */
	public Unmarshaller getUnmarshaller() {
		return unmarshaller;
	}

	/**
	 * 
	 * @return marsheller Unmarshaller that handles classes in http://skjema.geonorge.no/standard/geosynkronisering/1.0/endringslogg
	 */
	public Marshaller getMarshaller() {
		return marshaller;
	}

	/**
	 * A helper method for unmarshaling content in an file
	 * 
	 * @param fileName
	 * @return
	 * @throws IOException
	 * @throws XmlMappingException
	 */
	public TransactionCollection unmarshal(String fileName) throws XmlMappingException, IOException {
		if (logger.isDebugEnabled()) {
			logger.debug("enter");
		}
		FileInputStream is = new FileInputStream(fileName);
		TransactionCollection transactionCollection = null;

		try {

			StreamSource streamSource = new StreamSource(is);
			transactionCollection = (TransactionCollection) unmarshaller.unmarshal(streamSource);
		} finally {
			is.close();
		}

		if (logger.isDebugEnabled()) {
			logger.debug("leave");
		}

		return transactionCollection;
	}

	/**
	 * Get the list of insert, update and delete operations that should be executed.
	 * 
	 * @param transactionCollection
	 * @param typeOfObjectToGet
	 * @return ChangeLogResult which contains all the changes requested.
	 * @throws TransformerFactoryConfigurationError
	 * @throws TransformerException
	 * @throws XmlMappingException
	 * @throws IOException
	 */
	// TODO fix update and delete, only insert is correct
	public ChangeLogResult getChangeLogResult(TransactionCollection transactionCollection, Class<?>[] typeOfObjectToGet)
			throws TransformerFactoryConfigurationError, TransformerException, XmlMappingException, IOException {
		if (logger.isDebugEnabled()) {
			logger.debug("enter");
		}

		List<Transaction> transactions;

		ChangeLogResult changeLogResult = new ChangeLogResult();
		if (transactionCollection != null && (transactions = transactionCollection.getTransactions()) != null) {

			if (logger.isDebugEnabled()) {
				logger.debug("Handle collections with " + transactions.size() + " elements and with and typeOfObjectToGet like " + typeOfObjectToGet);
			}

			// so first row will be 0
			int operationNumber = -1;

			for (Transaction transaction : transactions) {
				List<JAXBElement<? extends Serializable>> abstractTransactionActions = transaction.getAbstractTransactionActions();
				for (JAXBElement<? extends Serializable> jaxbElement : abstractTransactionActions) {
					Serializable wfsOperation = jaxbElement.getValue();

					operationNumber++;

					if (logger.isDebugEnabled()) {
						logger.debug("found object of type " + wfsOperation.getClass() + " at row number " + operationNumber);
					}

					if (wfsOperation instanceof InsertType) {
						InsertType insertType = (InsertType) wfsOperation;
						List<Object> anies = insertType.getAnies();
						for (Object object : anies) {

							Object product = getProduct(object);

							WSFOperation wsfOperation = new WSFOperation(operationNumber, SupportedWFSOperationType.InsertType, product);

							if (typeOfObjectToGet == null) {
								changeLogResult.wfsOerationList.add(wsfOperation);
							} else {
								for (Class<?> cl1 : typeOfObjectToGet) {
									if (cl1.isInstance(product)) {
										changeLogResult.wfsOerationList.add(wsfOperation);
										break;
									}
								}
							}

						}

						// TODO fix handle update

						// TODO fix handle insert

						// } else if (wfsOperation instanceof UpdateType) {
						// UpdateType updateType = (UpdateType) wfsOperation;
						// updateType.getTypeName();
						// updateType.getFilter();
						// List<Property> properties = updateType.getProperties();
						// for (Property property : properties) {
						//
						// System.out.println(property.getValueReference().getValue() + " property: " + property.getValue() + " property: " + property);
						//
						// ElementNSImpl delement = (ElementNSImpl) property.getValue();
						// ByteArrayOutputStream out = new ByteArrayOutputStream();
						// Transformer transformer = TransformerFactory.newInstance().newTransformer();
						// transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
						// Source source = new DOMSource(delement);
						// Result target = new StreamResult(out);
						// transformer.transform(source, target);
						// String string = out.toString();
						// ByteArrayInputStream is = new ByteArrayInputStream(string.getBytes());
						// JAXBElement<? extends Serializable> jaxbAttr = (JAXBElement<? extends Serializable>)
						// sosiProduktMarshallerHelper.getUnmarshaller().unmarshal(new StreamSource(is));
						// is.close();
						// System.out.println("product.getClass():" + jaxbAttr.getValue().getClass());
						//
						//
						//
						//
						// }
						//
						// // } else if (value instanceof DeleleType) {
						// // InsertType insertType = (InsertType) value;
						// // List<Object> anies = insertType.getAnies();
						// // for (Object object : anies) {
						// // Object product = getProduct(object);
						// // changeLogResult.updateTypeList.add(product);
						// // }
					} else {
						logger.error("Failed to handle WFS operation of type " + wfsOperation.getClass().getName() + "for collections with "
								+ transactions.size() + " elements and with and typeOfObjectToGet like " + typeOfObjectToGet);
					}
				}
			}
		}
		if (logger.isDebugEnabled()) {
			logger.debug("leave");
		}

		return changeLogResult;

	}

	private Object getProduct(Object object) throws TransformerConfigurationException, TransformerFactoryConfigurationError, TransformerException, IOException {
		if (logger.isDebugEnabled()) {
			logger.debug("enter");
		}

		JAXBElement delement = (JAXBElement) object;
		Object product = delement.getValue();

		if (logger.isDebugEnabled()) {
			logger.debug("leave");
		}

		return product;
	}

	// TODO fix update and delete, only insert is correct
	// TODO and java doc
	/**
	 * This class creates a TransactionCollection list based the internal structure ArrayList<WSFOperation> wfsOperationList
	 * 
	 * @param wfsOperationList
	 * @return
	 * @throws ParseException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 */
	public TransactionCollection getTransactionCollection(ArrayList<WSFOperation> wfsOperationList) throws ParseException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException {

		if (logger.isDebugEnabled()) {
			logger.debug("enter");
		}

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

		opengis.net.filter_2_0.filter.ObjectFactory fesObjectFactory = new opengis.net.filter_2_0.filter.ObjectFactory();

		opengis.net.gml_3_2_1.gml.ObjectFactory gmlFactory = new opengis.net.gml_3_2_1.gml.ObjectFactory();

		// will get value at first iteration based on the annotation XmlRootElement
		QName qname = null;

		// will get value at first iteration based on the annotation XmlRootElement
		ArrayList<String> propOrder = new ArrayList<>();

		for (int i = 0; i < wfsOperationList.size(); i++) {
			WSFOperation wsfOperation = wfsOperationList.get(i);
			Object product = wsfOperation.product;

			if (logger.isDebugEnabled()) {
				logger.debug("handle wsfOperation.wfsOperationType " + wsfOperation.wfsOperationType + " for feature nr. " + i);
			}

			// get in first interation
			if (qname == null) {
				qname = getQName(gmlFactory, qname, propOrder, product);

			}

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
				insertType.getAnies().add(product);
				break;
			}

			case UpdateType: {

				if (currentWfsType == null || !currentWfsType.equals(wsfOperation.wfsOperationType)) {
					updateType = new UpdateType();
					updateType.setHandle("" + handle++);
					aswitchHappend = true;
					newType = updateType;
					currentWfsType = wsfOperation.wfsOperationType;
				}

				// ar5objectFactory.createIdentifikasjon(value)

				for (Method method : product.getClass().getMethods()) {
					String methodName = method.getName();
					if (methodName.startsWith("get")) {
						String valueReferanseValue = methodName.substring(3).toLowerCase();

						if (propOrder.contains(valueReferanseValue)) {
							Object invoke = method.invoke(product);
							Object value = getValue(invoke);

							if (value != null) {
								Property property = new Property();
								property.setValue(value);

								ValueReference valueReferanse = new ValueReference();
								valueReferanse.setValue(valueReferanseValue);

								property.setValueReference(valueReferanse);
								// add a property
								updateType.getProperties().add(property);
							}
						} else {
							System.out.println("lowerCase:" + valueReferanseValue);
						}
					}
				}

				// set filter
				FilterType filterType = createFilter(fesObjectFactory);
				updateType.setFilter(filterType);
				break;

			}

			case DeleleType: {
				// TODO fix this bad written code
				if (currentWfsType == null || !currentWfsType.equals(wsfOperation.wfsOperationType)) {
					deleteType = new DeleteType();
					deleteType.setHandle("" + handle++);
					deleteType.setTypeName(qname);
					aswitchHappend = true;
					newType = deleteType;
					currentWfsType = wsfOperation.wfsOperationType;
				}

				// set filter
				FilterType filterType = createFilter(fesObjectFactory);
				deleteType.setFilter(filterType);
				break;

			}

			default:
				logger.error("Not handled wsfOperation " + wsfOperation);
				break;
			}

			// save if a switch or last row
			if ((aswitchHappend && lastType != null) || i == wfsOperationList.size()) {
				if (logger.isDebugEnabled()) {
					logger.debug("aswitchHappend has happend for handle wsfOperation.wfsOperationType " + wsfOperation.wfsOperationType + " for feature nr. "
							+ i + " last type is " + lastType);
				}

				addChangelogFeature(lastType, transaction, wfsObjectFactory, wsfOperation);
			}

			lastType = newType;

		}

		if (logger.isDebugEnabled()) {
			logger.debug("leave");
		}

		return transactionCollection;
	}

	private QName getQName(opengis.net.gml_3_2_1.gml.ObjectFactory gmlFactory, QName qname, ArrayList<String> propOrder, Object product)
			throws IllegalAccessException, InvocationTargetException {
		if (logger.isDebugEnabled()) {
			logger.debug("enter");
		}

		{
			Class aClass = product.getClass();
			Annotation[] annotations = aClass.getAnnotations();
			for (Annotation annotation : annotations) {
				if (annotation instanceof XmlRootElement) {
					XmlRootElement myAnnotation = (XmlRootElement) annotation;
					qname = new QName(myAnnotation.namespace(), myAnnotation.name());
				} else if (annotation instanceof XmlType) {
					XmlType myAnnotation = (XmlType) annotation;
					for (String p : myAnnotation.propOrder()) {
						propOrder.add(p);
					}
				}
			}
		}
		{
			// get from felles komponenter

			Class aClass = FellesegenskaperType.class;
			Annotation[] annotations = aClass.getAnnotations();
			for (Annotation annotation : annotations) {
				if (annotation instanceof XmlType) {
					XmlType myAnnotation = (XmlType) annotation;
					for (String p : myAnnotation.propOrder()) {
						propOrder.add(p);
					}
				}
			}
		}

		// MZ: Find the correct method

		// WHY is thi called ????
		String name = "Skogbonitet";
		getValue(gmlFactory, product, name);
		return qname;
	}

	private void addChangelogFeature(AbstractTransactionActionType lastType, Transaction transaction, ObjectFactory wfsObjectFactory,
			WSFOperation wsfOperation) {

		JAXBElement<? extends Serializable> abstractTransactionActions = null;

		if (lastType instanceof InsertType) {
			abstractTransactionActions = wfsObjectFactory.createInsert((InsertType) lastType);
		} else if (lastType instanceof DeleteType) {
			abstractTransactionActions = wfsObjectFactory.createDelete((DeleteType) lastType);
		} else if (lastType instanceof UpdateType) {
			abstractTransactionActions = wfsObjectFactory.createUpdate((UpdateType) lastType);
		}

		if (abstractTransactionActions == null) {
			logger.error("Not handled wsfOperation " + wsfOperation);
		} else {
			transaction.getAbstractTransactionActions().add(abstractTransactionActions);
		}

	}

	private FilterType createFilter(opengis.net.filter_2_0.filter.ObjectFactory fesObjectFactory) {
		String invoke = "identifikasjon";
		String createValueReference = "identifikasjon/Identifikasjon/lokalId";

		BinaryComparisonOpType binaryComparisonOpType = new BinaryComparisonOpType();
		binaryComparisonOpType.setMatchAction(MatchActionType.ONE);

		{

			Object value = getValue(invoke);
			LiteralType createLiteralValue = new LiteralType();
			createLiteralValue.getContent().add(value);
			JAXBElement<LiteralType> createLiteral = fesObjectFactory.createLiteral(createLiteralValue);
			binaryComparisonOpType.getExpressions().add(createLiteral);
		}

		{
			JAXBElement<String> jAXBcreateValueReference = fesObjectFactory.createValueReference(createValueReference);

			binaryComparisonOpType.getExpressions().add(jAXBcreateValueReference);

		}

		JAXBElement<BinaryComparisonOpType> createPropertyIsEqualTo = fesObjectFactory.createPropertyIsEqualTo(binaryComparisonOpType);

		FilterType filterType = new FilterType();
		filterType.setComparisonOps(createPropertyIsEqualTo);
		return filterType;
	}

	private Object getValue(Object invoke) {
		Object value = null;

		if (AbstractCodeType.class.isInstance(invoke)) {
			value = ((AbstractCodeType) invoke).getValue();
			// } else if (SurfacePropertyType.class.isInstance(invoke)) {
			// SurfacePropertyType surfacePropertyType = (SurfacePropertyType) invoke;
			// JAXBElement<SurfacePropertyType> createSurfaceProperty = gmlFactory.createSurfaceProperty(surfacePropertyType);
			// value = invoke;
		} else if (java.util.ArrayList.class.isInstance(invoke)) {
			// TODO find out what to with this
			System.out.println("lowerCase + java.util.ArrayList:" + invoke.getClass().getName());
		} else if (no.geonorge.skjema.sosi.produktspesifikasjon.Arealressurs_45.IdentifikasjonPropertyType.class.isInstance(invoke)) {
			// TOD this is not correct
			no.geonorge.skjema.sosi.produktspesifikasjon.Arealressurs_45.IdentifikasjonPropertyType gv = (IdentifikasjonPropertyType) invoke;
			value = gv.getIdentifikasjon().getLokalId();
			// TODO fIX this
			// valueReferanseValue = "identifikasjon/Identifikasjon/lokalId";
		} else {
			value = invoke;
		}
		return value;
	}

	private Object getValue(opengis.net.gml_3_2_1.gml.ObjectFactory gmlFactory, Object product, String name) throws IllegalAccessException,
			InvocationTargetException {
		Object value = null;

		for (Method method : product.getClass().getMethods()) {
			if ((method.getName().startsWith("get") && method.getName().substring(3).equals(name))) {

				Object invoke = method.invoke(product);

				Class<?> class1 = method.getReturnType();

				if (AbstractCodeType.class.isInstance(invoke)) {
					value = ((AbstractCodeType) invoke).getValue();
				} else if (SurfacePropertyType.class.isInstance(invoke)) {
					SurfacePropertyType surfacePropertyType = (SurfacePropertyType) invoke;
					JAXBElement<SurfacePropertyType> createSurfaceProperty = gmlFactory.createSurfaceProperty(surfacePropertyType);
					value = createSurfaceProperty;
				} else {
					value = invoke;
				}
			}
		}

		return value;
	}

}
