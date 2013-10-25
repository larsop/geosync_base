package no.geonorge.skjema.changelogfile.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.List;

import javax.xml.bind.JAXBElement;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.stream.StreamSource;

import no.geonorge.skjema.changelogfile.TransactionCollection;
import no.geonorge.skjema.sosi.produktspesifikasjon.util.SosiProduktMarshallerHelper;
import opengis.net.wfs_2_0.wfs.InsertType;
import opengis.net.wfs_2_0.wfs.Transaction;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.oxm.Marshaller;
import org.springframework.oxm.Unmarshaller;
import org.springframework.oxm.XmlMappingException;

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

	private Marshaller uarshaller;
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

		this.uarshaller = m;
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
		return uarshaller;
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
								changeLogResult.wfsOerationList.add(wsfOperation );
							} else {
								for (Class<?> cl1 : typeOfObjectToGet) {
									if (cl1.isInstance(product)) {
										changeLogResult.wfsOerationList.add(wsfOperation );
										break;
									}
								}
							}

						}
//					} else if (wfsOperation instanceof UpdateType) {
//						UpdateType updateType = (UpdateType) wfsOperation;
//						updateType.getTypeName();
//						updateType.getFilter();
//						List<Property> properties = updateType.getProperties();
//						for (Property property : properties) {
//							
//							System.out.println(property.getValueReference().getValue() + " property: " + property.getValue() + " property: " + property);
//							
//							ElementNSImpl delement = (ElementNSImpl) property.getValue();
//							 ByteArrayOutputStream out = new ByteArrayOutputStream();
//							 Transformer transformer = TransformerFactory.newInstance().newTransformer();
//							 transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
//							 Source source = new DOMSource(delement);
//							 Result target = new StreamResult(out);
//							 transformer.transform(source, target);
//							 String string = out.toString();
//							 ByteArrayInputStream is = new ByteArrayInputStream(string.getBytes());
//							 JAXBElement<? extends Serializable> jaxbAttr = (JAXBElement<? extends Serializable>) sosiProduktMarshallerHelper.getUnmarshaller().unmarshal(new StreamSource(is));
//							 is.close();
//							 System.out.println("product.getClass():" + jaxbAttr.getValue().getClass());
//							
//							
//							
//
//						}
//
//						// } else if (value instanceof DeleleType) {
//						// InsertType insertType = (InsertType) value;
//						// List<Object> anies = insertType.getAnies();
//						// for (Object object : anies) {
//						// Object product = getProduct(object);
//						// changeLogResult.updateTypeList.add(product);
//						// }
					} else {
						logger.error("Failed to handle WFS operation of type " + wfsOperation.getClass().getName() + "for collections with " + transactions.size() + " elements and with and typeOfObjectToGet like " + typeOfObjectToGet);
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

		javax.xml.bind.JAXBElement delement = (JAXBElement) object;
		Object product = delement.getValue();

		// com.sun.org.apache.xerces.internal.dom.ElementNSImpl delement = (ElementNSImpl) object;
		// ByteArrayOutputStream out = new ByteArrayOutputStream();
		// Transformer transformer = TransformerFactory.newInstance().newTransformer();
		// transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
		// Source source = new DOMSource(delement);
		// Result target = new StreamResult(out);
		// transformer.transform(source, target);
		// String string = out.toString();
		// ByteArrayInputStream is = new ByteArrayInputStream(string.getBytes());
		// Object product = sosiProduktMarshallerHelper.getUnmarshaller().unmarshal(new StreamSource(is));
		// is.close();
		//

		if (logger.isDebugEnabled()) {
			logger.debug("leave");
		}

		return product;
	}

}
