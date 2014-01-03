package itest.no.skogoglanskap.provider.Arealressurs;

/**
 * This class is used to test moving data from provider to subscriber by using ArealressursFlateType and a changelog file
 * 
 * This code is adjusted to how SkogogandLandskap store their data
 *  
 */

import java.math.BigInteger;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import javax.xml.bind.JAXBElement;

import no.geonorge.skjema.changelogfile.util.ChangeLogMarshallerHelper;
import no.geonorge.skjema.changelogfile.util.IConvert2ArealressursFlateType;
import no.geonorge.skjema.sosi.produktspesifikasjon.Arealressurs_45.ArealressursFlateType;
import no.geonorge.skjema.sosi.produktspesifikasjon.Arealressurs_45.ArealressursGrenseType;
import no.geonorge.skjema.sosi.produktspesifikasjon.Arealressurs_45.IdentifikasjonPropertyType;
import no.geonorge.skjema.sosi.produktspesifikasjon.Arealressurs_45.IdentifikasjonType;
import no.geonorge.skjema.sosi.produktspesifikasjon.Arealressurs_45.PosisjonskvalitetPropertyType;
import no.geonorge.skjema.sosi.produktspesifikasjon.Arealressurs_45.PosisjonskvalitetType;
import no.geonorge.skjema.sosi.produktspesifikasjon.util.SosiProduktMarshallerHelper;
import no.geonorge.skjema.util.gml_geos.inspire.JTS2GML321;
import no.geonorge.subscriber.Arealressurs.Ar5FlateEntity;
import no.skogoglandskap.datamodel.postgres.provider.Ar5FlateProvSimpleFeatureEntity;

import opengis.net.gml_3_2_1.gml.AbstractCodeType;
import opengis.net.gml_3_2_1.gml.PolygonType;
import opengis.net.gml_3_2_1.gml.SurfacePropertyType;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.io.WKTReader;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/geosyncBaseMarshallerAppContext.xml" })
public class TestGenerateInsertChangelogFile {

	@Autowired
	private ChangeLogMarshallerHelper changelogfileJaxb2Helper;

	@Autowired
	private SosiProduktMarshallerHelper genericMarshaller;

	/**
	 * Test transfomation of Ar5FlateProvSimpleFeatureEntity ArealressursFlateType used the changelog file
	 * 
	 * @throws Exception
	 */
	@Test
	public void testMappingOfAr5FlateProvSimpleFeatureEntity() throws Exception {

		ArrayList<Ar5FlateProvSimpleFeatureEntity> providerData = new ArrayList<>();

		WKTReader reader = new WKTReader();

		Polygon borderPolygon = (Polygon) reader
				.read("POLYGON ((59.310381 4.905230,59.310370 4.905192,59.310560 4.905556,59.310572 4.905556,59.310572 4.905582,59.310381 4.905582,59.310381 4.905230, 59.310381 4.905230))");
		borderPolygon.setSRID(4258);
		borderPolygon.setUserData("NO.SK.AR5:");

		Assert.assertTrue("Boder not valid", borderPolygon.isValid());

		Ar5FlateProvSimpleFeatureEntity ar5f = new Ar5FlateProvSimpleFeatureEntity();
		ar5f.setGeo(borderPolygon);
		ar5f.setArtype(new Byte("30"));
		ar5f.setArtreslag(new Byte("33"));
		ar5f.setArskogbon(new Byte("14"));
		ar5f.setArgrunnf(new Byte("44"));
		ar5f.setArkartstd("AR5");
		java.text.SimpleDateFormat formatter = new java.text.SimpleDateFormat("yyyyMMdd");
		ar5f.setDatafangstdato(formatter.parse("20040801"));
		ar5f.setVerifiseringsdato(formatter.parse("20120502"));

//		missing valus from Fysak
		ar5f.setKartblad("k");
		ar5f.setKjoringsident(new Date(System.currentTimeMillis()));
		ar5f.setMaalemetode(new Byte("01"));
		ar5f.setNoyaktighet(1);
		ar5f.setObjtype("objtyp");
		ar5f.setSynbarhet(new Byte("01"));

		// not handle from gml
		// <sgm:KVALITET>82</sgm:KVALITET>
		// <sgm:OPPHAV>Skogoglandskap</sgm:OPPHAV>
		// <sgm:REGISTRERINGSVERSJON>FKB-AR5 "4.5 20140101"</sgm:REGISTRERINGSVERSJON>

		// todo handle grense
		ArealressursGrenseType ar5g = new ArealressursGrenseType();
		
		
		
		
		providerData.add(ar5f);

		Assert.assertTrue("To few rows created " + providerData.size(), providerData.size() > 0);

		TestConver testConver = new TestConver();

		for (Ar5FlateProvSimpleFeatureEntity aa : providerData) {

			ArealressursFlateType ar5 = testConver.convert2ArealressursFlateType(aa);
			Assert.assertNotNull("new object should not be null", ar5);

			Assert.assertEquals(aa.getArkartstd(), ar5.getKartstandard().getValue());

		}

	}

	private class TestConver implements IConvert2ArealressursFlateType {

		@Override
		public ArealressursFlateType convert2ArealressursFlateType(Object input) {
			
			Ar5FlateProvSimpleFeatureEntity a = (Ar5FlateProvSimpleFeatureEntity) input;
			
			no.geonorge.skjema.sosi.produktspesifikasjon.Arealressurs_45.ObjectFactory ofar5 = new no.geonorge.skjema.sosi.produktspesifikasjon.Arealressurs_45.ObjectFactory();

			ArealressursFlateType ar5 = ofar5.createArealressursFlateType();

			IdentifikasjonPropertyType v = new IdentifikasjonPropertyType();
			IdentifikasjonType v1 = new IdentifikasjonType();
			v1.setLokalId(System.currentTimeMillis() + "");
			v.setIdentifikasjon(v1);
			ar5.setIdentifikasjon(v);

			ar5.setArealtype(makeAbstractType(""+a.getArtype(), "ArealressursArealtype"));

			ar5.setSkogbonitet(makeAbstractType(""+a.getArskogbon(), "ArealressursSkogbonitet"));

			ar5.setTreslag(makeAbstractType(""+a.getArtreslag(), "ArealressursTreslag"));

			ar5.setGrunnforhold(makeAbstractType(""+a.getArgrunnf(), "ArealressursGrunnforhold"));

			ar5.setKartstandard(makeAbstractType(""+a.getArkartstd(), "ArealressursKartstandard"));

			{
				PosisjonskvalitetPropertyType posisjonskvalitetPropertyType = new PosisjonskvalitetPropertyType();
				PosisjonskvalitetType posisjonskvalitetType = new PosisjonskvalitetType();
				posisjonskvalitetPropertyType.setPosisjonskvalitet(posisjonskvalitetType);
				ar5.setKvalitet(posisjonskvalitetPropertyType);

				posisjonskvalitetType.setNøyaktighet(new BigInteger("10"));
				posisjonskvalitetType.setSynbarhet(makeAbstractType("10", "Synbarhet"));
				posisjonskvalitetType.setMålemetode(makeAbstractType("12","Målemetode"));
				
				

			}

			Calendar datafangstdato = Calendar.getInstance();
			ar5.setDatafangstdato(datafangstdato);
			opengis.net.gml_3_2_1.gml.ObjectFactory of = new opengis.net.gml_3_2_1.gml.ObjectFactory();
			
			
			PolygonType polygonType = (PolygonType) JTS2GML321.toGML(a.getGeo());
			JAXBElement<PolygonType> jaxbElementPolygonType = of.createPolygon(polygonType );
			SurfacePropertyType surfacePropertyType = new SurfacePropertyType();
			surfacePropertyType.setAbstractSurface(jaxbElementPolygonType);
			
			
			
			ar5.setOmråde(surfacePropertyType);

			return ar5;

		}

		private AbstractCodeType makeAbstractType(String value, String codeSpaceType) {
			AbstractCodeType abstractCodeType = new AbstractCodeType();
			
			//String codeSpaceTypeNew = codeSpaceType.substring(0, 1).toUpperCase() + codeSpaceType.substring(1).toLowerCase();
			
			String codeSpace = "http://www.geosynkronisering.no/files/skjema/Arealressurs/4.5/" + codeSpaceType +".xml";
					
			abstractCodeType.setCodeSpace(codeSpace);
			abstractCodeType.setValue(value);
			return abstractCodeType;
		}

	}

}
