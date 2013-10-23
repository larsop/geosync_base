package no.geonorge.skjema.util;



import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import no.geonorge.skjema.changelogfile.TransactionCollection;

import org.springframework.oxm.Marshaller;
import org.springframework.oxm.Unmarshaller;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

import com.sun.xml.internal.bind.marshaller.NamespacePrefixMapper;

public class ChangelogfileJaxb2Helper {
	private Marshaller marshaller;
	private Unmarshaller unmarshaller;

	public void setMarshaller(Marshaller marshaller) {
		this.marshaller = marshaller;

//		final NamespacePrefixMapper mapper = new NamespacePrefixMapper() {
//			@Override
//			public String getPreferredPrefix(String s, String s2, boolean b) {
//				if ("http://www.opengis.net/gml/3.2".equals(s)) {
//					return "gml";
//				} else if ("http://skjema.geonorge.no/standard/geosynkronisering/1.0/endringslogg".equals(s)) {
//					return "chlogf";
//				} else if ("http://www.opengis.net/fes/2.0".equals(s)) {
//					return "fes";
//				} else if ("http://www.opengis.net/wfs/2.0".equals(s)) {
//					return "wfs";
//				} else if ("http://skjema.geonorge.no/SOSI/produktspesifikasjon/Arealressurs/4.5".equals(s)) {
//					return "app";
//				} else {
//					return s2;
//				}
//
//			}
//		};
//		((Jaxb2Marshaller) marshaller).setMarshallerProperties(new HashMap<String, Object>() {
//			{
//				put("com.sun.xml.internal.bind.namespacePrefixMapper", mapper);
//			}
//		});
	}

	public void setUnmarshaller(Unmarshaller unmarshaller) {
		this.unmarshaller = unmarshaller;
	}

	public void marshal(TransactionCollection settings, String file_name) throws IOException {

		FileOutputStream os = null;
		try {

			os = new FileOutputStream(file_name);
			this.marshaller.marshal(settings, new StreamResult(os));

		} finally {
			if (os != null) {
				os.close();
			}
		}

	}

	public TransactionCollection unmarshal(String file_name) throws IOException {

		TransactionCollection transactionCollection;
		FileInputStream is = null;
		try {

			is = new FileInputStream(file_name);
			transactionCollection = unmarshal(is);

		} finally {
			if (is != null) {
				is.close();
			}
		}

		return transactionCollection;
	}

	public TransactionCollection unmarshal(FileInputStream is) throws IOException {
		TransactionCollection transactionCollection;
		StreamSource source = new StreamSource(is);
		transactionCollection = (TransactionCollection) this.unmarshaller.unmarshal(source);
		return transactionCollection;
	}

	public TransactionCollection unmarshal(InputStream is) {
		// TODO Auto-generated method stub
		return null;
	}

	// public Object unmarshalString(com.sun.org.apache.xerces.internal.dom.ElementNSImpl str) throws IOException {
	//
	// Object transactionCollection;
	// InputStream is = new ByteArrayInputStream(str.getBytes());
	//
	// try {
	//
	// // convert String into InputStream
	//
	// StreamSource source = new StreamSource(is);
	// transactionCollection = (TransactionCollection) this.unmarshaller.unmarshal(str.get);
	//
	// } finally {
	// if (is != null) {
	// is.close();
	// }
	// }
	//
	// return transactionCollection;
	// }

}