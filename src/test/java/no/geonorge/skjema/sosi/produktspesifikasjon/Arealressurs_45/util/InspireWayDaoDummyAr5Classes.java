package no.geonorge.skjema.sosi.produktspesifikasjon.Arealressurs_45.util;



import java.util.Calendar;

import javax.xml.bind.JAXBElement;

import org.junit.Before;

import no.geonorge.skjema.sosi.produktspesifikasjon.Arealressurs_45.AbstractCodeType;
import no.geonorge.skjema.sosi.produktspesifikasjon.Arealressurs_45.AbstractCurveType;
import no.geonorge.skjema.sosi.produktspesifikasjon.Arealressurs_45.AbstractGeometryType;
import no.geonorge.skjema.sosi.produktspesifikasjon.Arealressurs_45.AbstractSurfaceType;
import no.geonorge.skjema.sosi.produktspesifikasjon.Arealressurs_45.ArealressursFlateType;
import no.geonorge.skjema.sosi.produktspesifikasjon.Arealressurs_45.ArealressursGrenseType;
import no.geonorge.skjema.sosi.produktspesifikasjon.Arealressurs_45.CurvePropertyType;
import no.geonorge.skjema.sosi.produktspesifikasjon.Arealressurs_45.ObjectFactory;
import no.geonorge.skjema.sosi.produktspesifikasjon.Arealressurs_45.SurfacePropertyType;
import no.geonorge.skjema.util.gml_geos.inspire.JTS2GML321;

import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;


public class InspireWayDaoDummyAr5Classes {


	public Polygon borderPolygon;
	public LineString borderLineString;

	public InspireWayDaoDummyAr5Classes() throws ParseException {
		setUp();
	}
	
	public void setUp() throws ParseException {
		WKTReader reader = new WKTReader();
		borderPolygon = (Polygon) reader
				.read("POLYGON ((12.021918457142856 60.627196057142854, 12.021914528571429 60.6272032, 12.021881285714286 60.62726214285714, 12.021862314285714 60.627330185714285, 12.021849214285714 60.627387514285715, 12.0218417 60.6274422, 12.021829814285715 60.627467228571426, 12.021816257142858 60.62748775714286, 12.021786171428571 60.62751081428571, 12.021743457142858 60.62752928571429, 12.021717557142857 60.62753802857143, 12.021673314285714 60.6275484, 12.021548157142858 60.62757331428571, 12.021541285714285 60.62756158571429, 12.021476114285715 60.627447000000004, 12.021433971428571 60.627303014285715, 12.021419442857143 60.627202342857146, 12.021678457142857 60.62721364285714, 12.021865314285714 60.627200071428575, 12.021918457142856 60.627196057142854))");
		borderPolygon.setSRID(4258);

		borderLineString = (LineString) reader
				.read("LINESTRING (12.021914528571429 60.6272032, 12.021881285714286 60.62726214285714, 12.021862314285714 60.627330185714285, 12.021849214285714 60.627387514285715, 12.0218417 60.6274422, 12.021829814285715 60.627467228571426, 12.021816257142858 60.62748775714286, 12.021786171428571 60.62751081428571, 12.021743457142858 60.62752928571429, 12.021717557142857 60.62753802857143, 12.021673314285714 60.6275484, 12.021548157142858 60.62757331428571, 12.021541285714285 60.62756158571429, 12.021476114285715 60.627447000000004, 12.021433971428571 60.627303014285715, 12.021419442857143 60.627202342857146, 12.021678457142857 60.62721364285714, 12.021865314285714 60.627200071428575, 12.021918457142856 60.627196057142854)");
		borderLineString.setSRID(4258);
	}

	public ArealressursFlateType simpleAr5Flate() throws ParseException {
		ObjectFactory of = new ObjectFactory();

		// DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();

		ArealressursFlateType ar5 = of.createArealressursFlateType();

		AbstractCodeType arealtype = new AbstractCodeType();
		arealtype.setCodeSpace("codespace");
		arealtype.setValue("valueforcodespace");
		ar5.setArealtype(arealtype);

		Calendar datafangstdato = Calendar.getInstance();
		ar5.setDatafangstdato(datafangstdato);

		SurfacePropertyType omrade = new SurfacePropertyType();
		AbstractSurfaceType abstractSurfaceType = (AbstractSurfaceType) JTS2GML321.toGML(borderPolygon);
		JAXBElement<AbstractSurfaceType> createAbstractSurface = of.createAbstractSurface(abstractSurfaceType);
		omrade.setAbstractSurface(createAbstractSurface);
		ar5.setOmråde(omrade);

		return ar5;
	}
	

	public ArealressursGrenseType simpleAr5Grense() throws ParseException {
		no.geonorge.skjema.sosi.produktspesifikasjon.Arealressurs_45.ObjectFactory of = new no.geonorge.skjema.sosi.produktspesifikasjon.Arealressurs_45.ObjectFactory();

		ArealressursGrenseType ar5 = of.createArealressursGrenseType();

		//ar5.setArealtype(arealtype);

		Calendar datafangstdato = Calendar.getInstance();
		ar5.setDatafangstdato(datafangstdato);

		
		CurvePropertyType omrade = new CurvePropertyType();
		AbstractGeometryType abstractSurfaceType =  JTS2GML321.toGML(borderLineString);
		AbstractCurveType sddd = (AbstractCurveType) abstractSurfaceType;
		JAXBElement<? extends AbstractCurveType> value = of.createAbstractCurve(sddd );
		omrade.setAbstractCurve(value);
		ar5.setGrense(omrade);

		
AbstractCodeType avgrensingarealtype = new AbstractCodeType();
		avgrensingarealtype.setCodeSpace("codespace");
		avgrensingarealtype.setValue("valueforcodespace");
		ar5.setAvgrensingType(avgrensingarealtype);

		return ar5;
	}

}
