package itest.no.geonorge.subscriber.Arealressurs;

import java.io.IOException;
import java.io.Serializable;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;

import no.geonorge.skjema.changelogfile.TransactionCollection;
import no.geonorge.skjema.sosi.produktspesifikasjon.Arealressurs_45.ArealressursFlateType;
import no.geonorge.skjema.util.GenericMarshallerJaxb2Helper;
import no.geonorge.skjema.util.gml_geos.geoserver.GML321_2JTS;
import no.geonorge.subscriber.Arealressurs.Ar5FlateEntity;
import no.skogoglandskap.db.util.SpringHibernateTemplate;

import opengis.net.wfs_2_0.wfs.InsertType;
import opengis.net.wfs_2_0.wfs.Transaction;
import opengis.net_gml_3_2_1.AbstractRingPropertyType;
import opengis.net_gml_3_2_1.AbstractRingType;
import opengis.net_gml_3_2_1.AbstractSurfaceType;
import opengis.net_gml_3_2_1.PolygonType;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.oxm.Unmarshaller;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.xml.sax.SAXException;

import com.sun.org.apache.xerces.internal.dom.ElementImpl;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/subscriber_db_setup.xml", "/ChangelogfileJaxb2HelperAppContext.xml", "/GenericMarshallerJaxb2HelperAppContext.xml" })
public class TestAbonnentAr5FlateEntity {

	@Autowired
	private SpringHibernateTemplate abonnent_db_connection;

	@Autowired
	private no.geonorge.skjema.util.ChangelogfileJaxb2Helper changelogfileJaxb2Helper;

	@Autowired
	private GenericMarshallerJaxb2Helper genericMarshaller;

	/**
	 * Test that gpiwsEndpointHandler is set
	 */
	@Test
	public void testSessioFactory() {
		Assert.assertNotNull("Session factory should not be null", abonnent_db_connection);
	}

	/**
	 * Test add a single row the abonnent
	 * 
	 * @throws ParseException
	 */
	@Test
	public void testCreateAr5FlateEntity() throws ParseException {
		WKTReader reader = new WKTReader();

		Polygon border = (Polygon) reader
				.read("POLYGON ((11.780721752334046 60.29396719367269, 11.780746680254168 60.29404752987559, 12.621653751342507 60.54440504958914, 11.780744393519797 60.29402062502391, 11.780721752334046 60.29396719367269))");
		border.setSRID(4258);

		Ar5FlateEntity ar5 = new Ar5FlateEntity();
		ar5.setGeo(border);
		ar5.setArgrunnf(new Byte("01"));
		ar5.setArkartstd("kartstd");
		ar5.setArskogbon(new Byte("01"));
		ar5.setArtreslag(new Byte("01"));
		ar5.setArtype(new Byte("01"));
		ar5.setDatafangstdato(new Date(System.currentTimeMillis()));
		ar5.setKartblad("k");
		ar5.setKjoringsident(new Date(System.currentTimeMillis()));
		ar5.setMaalemetode(new Byte("01"));
		ar5.setNoyaktighet(1);
		ar5.setObjtype("objtyp");
		ar5.setSynbarhet(new Byte("01"));
		ar5.setVerifiseringsdato(new Date(System.currentTimeMillis()));
		abonnent_db_connection.save(ar5);

	}

	@Test
	public void test_ar5_save_one_row() throws ParseException, SAXException, IOException, ParserConfigurationException, TransformerFactoryConfigurationError,
			TransformerException, JAXBException {

		String fileName = getClass().getResource("/tmp/TransactionCollection5_test_ar5_Flate.xml").getFile();

		ArrayList<ArealressursFlateType> ar5List = getAr5List(fileName);
		for (ArealressursFlateType aa : ar5List) {
			Ar5FlateEntity ar5 = new Ar5FlateEntity();

			Polygon border = getBorderFromGml(aa);

			ar5.setGeo(border);
			ar5.setArgrunnf(new Byte(aa.getGrunnforhold().getValue()));
			// ar5.setArkartstd();
			ar5.setArskogbon(new Byte(aa.getSkogbonitet().getValue()));
			ar5.setArtreslag(new Byte(aa.getTreslag().getValue()));
			ar5.setArtype(new Byte(aa.getArealtype().getValue()));
			ar5.setDatafangstdato(aa.getDatafangstdato().getTime());
			// ar5.setKartblad();
			// ar5.setKjoringsident();
			// ar5.setMaalemetode();
			// ar5.setNoyaktighet();
			// ar5.setObjtype();
			// ar5.setSynbarhet();
			ar5.setVerifiseringsdato(aa.getVerifiseringsdato().getTime());
			abonnent_db_connection.save(ar5);

		}

	}

	private Polygon getBorderFromGml(ArealressursFlateType simpleAr5FromXml) {
		JAXBElement<? extends AbstractSurfaceType> abstractSurface = simpleAr5FromXml.getOmråde().getAbstractSurface();

		PolygonType value = (PolygonType) abstractSurface.getValue();
		AbstractRingPropertyType exterior = value.getExterior();
		JAXBElement<? extends AbstractRingType> abstractRing = exterior.getAbstractRing();

		opengis.net_gml_3_2_1.LinearRingType ringType = (opengis.net_gml_3_2_1.LinearRingType) abstractRing
				.getValue();

		Polygon border = (Polygon) GML321_2JTS.toJTS(ringType);
		// TODO remove this hack
		border.setSRID(4258);
		return border;
	}

	private ArrayList<ArealressursFlateType> getAr5List(String fileName) throws IOException {
		ArrayList<ArealressursFlateType> arrayListInsert = new ArrayList<>();

		TransactionCollection unmarshal = changelogfileJaxb2Helper.unmarshal(fileName);

		List<Transaction> transactions = unmarshal.getTransactions();
		for (Transaction transaction : transactions) {
			List<JAXBElement<? extends Serializable>> abstractTransactionActions = transaction.getAbstractTransactionActions();
			for (JAXBElement<? extends Serializable> jaxbElement : abstractTransactionActions) {
				Serializable operaTionType = jaxbElement.getValue();

				if (operaTionType instanceof InsertType) {

					InsertType insertType = (InsertType) operaTionType;

					List<Object> anies = insertType.getAnies();

					for (Object object : anies) {

						com.sun.org.apache.xerces.internal.dom.ElementImpl delement = (ElementImpl) object;

						Source source = new DOMSource(delement);
						Unmarshaller unmarshaller = genericMarshaller.getUnmarshaller();

						JAXBElement jaxblement = (JAXBElement) unmarshaller.unmarshal(source);
						ArealressursFlateType simpleAr5FromXml = (ArealressursFlateType) jaxblement.getValue();

						arrayListInsert.add(simpleAr5FromXml);

					}
				} else {

					throw new RuntimeException("Failed to handle  operation " + operaTionType.getClass().getName());

				}

			}
		}

		return arrayListInsert;
	}

}
