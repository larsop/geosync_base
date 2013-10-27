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

import net.opengis.fes._2.BinaryComparisonOpType;
import net.opengis.fes._2.FilterType;
import net.opengis.fes._2.LiteralType;
import net.opengis.fes._2.MatchActionType;
import no.geonorge.skjema.changelogfile.TransactionCollection;
import no.geonorge.skjema.sosi.produktspesifikasjon.Arealressurs_45.ArealressursFlateType;
import no.geonorge.skjema.sosi.produktspesifikasjon.Arealressurs_45.FellesegenskaperType;
import no.geonorge.skjema.sosi.produktspesifikasjon.Arealressurs_45.IdentifikasjonPropertyType;
import no.geonorge.skjema.sosi.produktspesifikasjon.util.SosiProduktMarshallerHelper;
import opengis.net.wfs_2_0.wfs.AbstractTransactionActionType;
import opengis.net.wfs_2_0.wfs.DeleteType;
import opengis.net.wfs_2_0.wfs.InsertType;
import opengis.net.wfs_2_0.wfs.ObjectFactory;
import opengis.net.wfs_2_0.wfs.Property;
import opengis.net.wfs_2_0.wfs.Property.ValueReference;
import opengis.net.wfs_2_0.wfs.Transaction;
import opengis.net.wfs_2_0.wfs.UpdateType;
import opengis.net_gml_3_2_1.AbstractCodeType;
import opengis.net_gml_3_2_1.SurfacePropertyType;

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
	 * A helper methid to set name spaces
	 * 
	 * @param marshaller
	 */
	private void setMarshaller(Marshaller marshaller) {
		this.marshaller = marshaller;

		final NamespacePrefixMapper mapper = new NamespacePrefixMapper() {
			@Override
			public String getPreferredPrefix(String s, String s2, boolean b) {
				if ("http://www.opengis.net/gml/3.2".equals(s)) {
					return "gml";
				} else if ("http://skjema.geonorge.no/standard/geosynkronisering/1.0/produkt".equals(s)) {
					return "chlogf";
				} else if ("http://www.opengis.net/fes/2.0".equals(s)) {
					return "fes";
				} else if ("http://www.opengis.net/wfs/2.0".equals(s)) {
					return "wfs";
				} else if ("http://skjema.geonorge.no/SOSI/produktspesifikasjon/Arealressurs/4.5".equals(s)) {
					return "ar5";
				} else {
					return s2;
				}
			}
		};
		((Jaxb2Marshaller) marshaller).setMarshallerProperties(new HashMap<String, Object>() {
			{
				put("com.sun.xml.internal.bind.namespacePrefixMapper", mapper);
			}
		});
	}

	/**
	 * A Marshaller that handles
	 * 
	 * @param m
	 */
	public ChangeLogMarshallerHelper(Marshaller m) {
		if (logger.isDebugEnabled()) {
			logger.debug("enter");
		}

		setMarshaller(m);
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
	 * This class creates a TransactionCollection  list based the internal structure ArrayList<WSFOperation> wfsOperationList  
	 * @param wfsOperationList
	 * @return
	 * @throws ParseException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 */
	public TransactionCollection getTransactionCollection(ArrayList<WSFOperation> wfsOperationList) throws ParseException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException {
		int handle = 0;

		InsertType insertType = null;
		UpdateType updateType = null;
		DeleteType deleteType = null;
		AbstractTransactionActionType lastType = null;
		AbstractTransactionActionType newType = null;
		AbstractTransactionActionType currentType = null;
		SupportedWFSOperationType currentWfsType = null;

		no.geonorge.skjema.changelogfile.TransactionCollection transactionCollection = new no.geonorge.skjema.changelogfile.TransactionCollection();

		List<Transaction> transactionsList = transactionCollection.getTransactions();
		Transaction transaction = new Transaction();
		transactionsList.add(transaction);

		ObjectFactory wfsObjectFactory = new opengis.net.wfs_2_0.wfs.ObjectFactory();

		net.opengis.fes._2.ObjectFactory fesObjectFactory = new net.opengis.fes._2.ObjectFactory();

		no.geonorge.skjema.sosi.produktspesifikasjon.Arealressurs_45.ObjectFactory ar5objectFactory = new no.geonorge.skjema.sosi.produktspesifikasjon.Arealressurs_45.ObjectFactory();

		opengis.net_gml_3_2_1.ObjectFactory gmlFactory = new opengis.net_gml_3_2_1.ObjectFactory();

		// will get value at first iteration based on the annotation XmlRootElement
		QName qname = null;

		// will get value at first iteration based on the annotation XmlRootElement
		ArrayList<String> propOrder = new ArrayList<>();

		for (WSFOperation wsfOperation : wfsOperationList) {
			Object product = (ArealressursFlateType) wsfOperation.product;

			if (qname == null) {
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

				String name = "Skogbonitet";

				getValue(gmlFactory, product, name);

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
			}
				break;

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

			}

				break;
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

				FilterType filterType = createFilter(fesObjectFactory);
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

	private FilterType createFilter(net.opengis.fes._2.ObjectFactory fesObjectFactory) {
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

	private Object getValue(opengis.net_gml_3_2_1.ObjectFactory gmlFactory, Object product, String name) throws IllegalAccessException,
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
