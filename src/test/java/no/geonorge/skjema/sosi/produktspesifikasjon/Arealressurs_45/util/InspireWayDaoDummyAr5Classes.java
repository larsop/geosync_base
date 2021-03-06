package no.geonorge.skjema.sosi.produktspesifikasjon.Arealressurs_45.util;

import java.math.BigInteger;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.xml.bind.JAXBElement;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.log4j.Logger;

import no.geonorge.skjema.sosi.produktspesifikasjon.Arealressurs_45.ArealressursFlateType;
import no.geonorge.skjema.sosi.produktspesifikasjon.Arealressurs_45.ArealressursGrenseType;
import no.geonorge.skjema.sosi.produktspesifikasjon.Arealressurs_45.IdentifikasjonPropertyType;
import no.geonorge.skjema.sosi.produktspesifikasjon.Arealressurs_45.IdentifikasjonType;
import no.geonorge.skjema.sosi.produktspesifikasjon.Arealressurs_45.PosisjonskvalitetPropertyType;
import no.geonorge.skjema.sosi.produktspesifikasjon.Arealressurs_45.PosisjonskvalitetType;
import no.geonorge.skjema.util.gml_geos.inspire.JTS2GML321;
import no.skogoglandskap.ar5.SimpleAr5Transformerer1;
import opengis.net.gml_3_2_1.gml.AbstractCodeType;
import opengis.net.gml_3_2_1.gml.AbstractCurveType;
import opengis.net.gml_3_2_1.gml.CompositeCurveType;
import opengis.net.gml_3_2_1.gml.CurvePropertyType;
import opengis.net.gml_3_2_1.gml.CurveType;
import opengis.net.gml_3_2_1.gml.LineStringSegmentType;
import opengis.net.gml_3_2_1.gml.LineStringType;
import opengis.net.gml_3_2_1.gml.PolygonType;
import opengis.net.gml_3_2_1.gml.SegmentsElement;
import opengis.net.gml_3_2_1.gml.SurfacePropertyType;

import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

public class InspireWayDaoDummyAr5Classes {

	private Logger logger = Logger.getLogger(InspireWayDaoDummyAr5Classes.class);

	public Polygon borderPolygon;
	public LineString borderLineString;

	public InspireWayDaoDummyAr5Classes() throws ParseException {
		setUp();
	}

	public void setUp() throws ParseException {
		WKTReader reader = new WKTReader();
		//borderPolygon = (Polygon) reader.read("POLYGON ((12.021918457142856 60.627196057142854, 12.021914528571429 60.6272032, 12.021881285714286 60.62726214285714, 12.021862314285714 60.627330185714285, 12.021849214285714 60.627387514285715, 12.0218417 60.6274422, 12.021829814285715 60.627467228571426, 12.021816257142858 60.62748775714286, 12.021786171428571 60.62751081428571, 12.021743457142858 60.62752928571429, 12.021717557142857 60.62753802857143, 12.021673314285714 60.6275484, 12.021548157142858 60.62757331428571, 12.021541285714285 60.62756158571429, 12.021476114285715 60.627447000000004, 12.021433971428571 60.627303014285715, 12.021419442857143 60.627202342857146, 12.021678457142857 60.62721364285714, 12.021865314285714 60.627200071428575, 12.021918457142856 60.627196057142854))");
		//borderPolygon.setSRID(4258);
		
		String p1 = "59.766215682960826 10.098056120304616";
		String bs = "59.76625375488964 10.098131971091492, 59.76630356371131 10.098232599639427, 59.766337205120905 10.098312044287807, 59.76638506620991 10.098366317526061, 59.7664572094809 10.098448629456222, 59.766522242283806 10.09853284474922, 59.76658079243491 10.098576074734506, 59.766661839990796 10.098624495067469, 59.76671134567463 10.098653896680894, 59.766767336888556 10.098723928261965, 59.766825961418775 10.09879048563564, 59.766882774940036 10.098848081326427, 59.76694587396814 10.09889502277652, 59.76700428657674 10.098911720141858, 59.76707787682707 10.098906838167535, 59.76714068909698 10.098902136178856, 59.76720341108061 10.098886748206835, 59.76725989115017 10.098855308608623, 59.76731514102997 10.0987883964172, 59.767368819029485 10.098737634995803, 59.767425056422 10.098670754836665, 59.767478629193235 10.098611087032628, 59.7675366944108 10.098561893610103, 59.76760191410677 10.098494773705823, 59.76764836205556 10.098449471589696, 59.767715604182364 10.098430491903262, 59.76778727821115 10.098407740536418, 59.76787245427636 10.098370476030578, 59.76795116065547 10.098312166544652, 59.76801820759508 10.098252049458003, 59.768087810886016 10.098176169210287, 59.76813410868362 10.098095072030244, 59.76816755766213 10.097961026505283, 59.76817599285565 10.09783274762123, 59.76818704787362 10.097702596060083, 59.768165890167616 10.097601478515353, 59.76815462594148 10.09749854911927, 59.768149589097426 10.09739190693184, 59.7680953180701 10.097298605951492, 59.768035018619265 10.09726048240672, 59.768035018619265 10.09726048240672, 59.768079472085674 10.097174338060535, 59.76810794226936 10.097120253176804, 59.76814808736934 10.097076878380527, 59.768200272750995 10.097097286118217, 59.76824524245135 10.097110691092658, 59.76824524245135 10.097110691092658, 59.768407044076106 10.097726910592966, 59.768407044076106 10.097726910592966, 59.768424700573874 10.097860142236234, 59.76845041064261 10.097975298611345, 59.76848243752366 10.098108290204662, 59.768498237449734 10.098227216837959, 59.768503394234386 10.098351669908746, 59.768532667321786 10.098481010485573, 59.76854937904815 10.098587503433157, 59.76856685355119 10.098688679831401, 59.76858536218206 10.098795053978634, 59.76862448425031 10.098906378985298, 59.76863392053313 10.099012989465521, 59.768660693025204 10.099151329900208, 59.768672883986376 10.099261414216745, 59.76870940001105 10.099394377479264, 59.76873147140724 10.099514936215442, 59.76877087930387 10.099677908893181, 59.76878491051288 10.099793396047833, 59.768814182173976 10.099922738962276, 59.76883022122989 10.100077287276692, 59.768857877378224 10.100227944534629, 59.76890322427833 10.100335737731893, 59.76890322427833 10.100335737731893, 59.76891478767069 10.100499220399598, 59.76891373037548 10.100646974710857, 59.7689133450277 10.10075735870993, 59.76891667286675 10.100896176354476, 59.76891012153344 10.101024341334732, 59.76889289657331 10.101193820631632, 59.76886661440264 10.101318842853095, 59.76885127591839 10.101488205863607, 59.76882484345773 10.101598978056206, 59.76878219788679 10.101694080975394, 59.7687287876957 10.101787760647909, 59.76868427101985 10.101859653868704, 59.76862172931798 10.101917776697329, 59.768569710384554 10.101941880982993, 59.76851578864334 10.101925147421994, 59.76847792939286 10.101888466629953, 59.76847792939286 10.101888466629953, 59.76845325738801 10.101799871731775, 59.76840342002075 10.101702969551665, 59.768367220637224 10.101628951997505, 59.76833979334786 10.101526200399041, 59.76832139192015 10.1014287318073, 59.768292213932305 10.10133126453978, 59.76827831952742 10.101242491288984, 59.768225877298704 10.101167049406678, 59.76815391696186 10.101138149741079, 59.76815391696186 10.101138149741079, 59.7681785683551 10.10102554021838, 59.76818687169646 10.100891916000748, 59.768204353167505 10.100799012391748, 59.76822880872746 10.100656126572359, 59.768217501078176 10.100526486154395, 59.768196330268715 10.100416463485502, 59.76816430501735 10.100305015500325, 59.76812618888683 10.100213131453568, 59.768076470719336 10.100134042007221, 59.76799368707522 10.100099981202723, 59.76791930408671 10.100113737278704, 59.76783851169741 10.100110011271832, 59.767773904218 10.100125335206995, 59.76769775474946 10.100157016352204, 59.76763085899668 10.100241883316096, 59.767588169320526 10.10033164155225, 59.76751221579869 10.10040428039443, 59.76745442112345 10.10050689491645, 59.76739839400525 10.100623633292589, 59.76732888016669 10.100731559003995, 59.767273659817796 10.100816274049441, 59.76722909746035 10.10089368349819, 59.7671802095934 10.100983591892513, 59.767119480582245 10.101050497877969, 59.767074948230444 10.101135029626505, 59.76701429401179 10.101203718083555, 59.76696326340001 10.101249220745402, 59.76690970435343 10.101307103405881, 59.766853450413414 10.101365075184194, 59.766806924808826 10.101398084341021, 59.766741432806995 10.101411773782662, 59.766674235298375 10.101436089840659, 59.76661407277582 10.101435354826027, 59.76655503353865 10.101472046600747, 59.766501292166694 10.101487369125786, 59.76644120509873 10.10150978249456, 59.76636219460717 10.101497031680262, 59.766292929188424 10.10146804366753, 59.766231731962804 10.10144074584371, 59.766231731962804 10.10144074584371";
		borderPolygon = (Polygon) reader.read("POLYGON ((" + p1 + ","+ bs + "," + p1 + "))");
		borderPolygon.setSRID(4258);
		borderPolygon.setUserData("NO.SK.AR5:");

		if (!borderPolygon.isValid() ) {
			throw new RuntimeException("not valid " + borderPolygon);
			
		}

				
//		Coordinate[] coordinates = borderPolygon.getCoordinates();
//		for (Coordinate coordinate : coordinates) {
//			coordinate.x = coordinate.y;
//			coordinate.y = coordinate.x;			
//		}

		borderLineString = (LineString) reader
				.read("LINESTRING ("  + p1 + ","+  bs + ")");
		borderLineString.setSRID(4258);
		borderLineString.setUserData("NO.SK.AR5:");
		
		if (!borderLineString.isValid() ) {
			throw new RuntimeException("not valid " + borderLineString);
			
		}

//		
//		coordinates = borderPolygon.getCoordinates();
//		for (Coordinate coordinate : coordinates) {
//			coordinate.x = coordinate.y;
//			coordinate.y = coordinate.x;			
//		}

	}

	public ArealressursFlateType simpleAr5Flate() throws ParseException {
		no.geonorge.skjema.sosi.produktspesifikasjon.Arealressurs_45.ObjectFactory ofar5 = new no.geonorge.skjema.sosi.produktspesifikasjon.Arealressurs_45.ObjectFactory();

		// DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();

		ArealressursFlateType ar5 = ofar5.createArealressursFlateType();

		IdentifikasjonPropertyType v = new IdentifikasjonPropertyType();
		IdentifikasjonType v1 = new IdentifikasjonType();
		v1.setLokalId(System.currentTimeMillis() + "");
		v.setIdentifikasjon(v1);
		ar5.setIdentifikasjon(v);

		ar5.setArealtype(makeAbstractType("12", "ArealressursArealtype"));

		ar5.setSkogbonitet(makeAbstractType("98", "ArealressursSkogbonitet"));

		ar5.setTreslag(makeAbstractType("98", "ArealressursTreslag"));

		ar5.setGrunnforhold(makeAbstractType("98", "ArealressursGrunnforhold"));

		ar5.setKartstandard(makeAbstractType("AR5", "ArealressursKartstandard"));

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
		ar5.setDatafangstdato(getXmlCalender(datafangstdato.getTime()));
		opengis.net.gml_3_2_1.gml.ObjectFactory of = new opengis.net.gml_3_2_1.gml.ObjectFactory();

//		// with SurfacePropertyType
//		SurfacePropertyType omrade = new SurfacePropertyType();
//		AbstractSurfaceType abstractSurfaceType = (AbstractSurfaceType) JTS2GML321.toGML(borderPolygon);
//		JAXBElement<AbstractSurfaceType> createAbstractSurface = of.createAbstractSurface(abstractSurfaceType);
//		omrade.setAbstractSurface(createAbstractSurface);

		
		
		PolygonType polygonType = (PolygonType) JTS2GML321.toGML(borderPolygon);
		JAXBElement<PolygonType> jaxbElementPolygonType = of.createPolygon(polygonType );
		SurfacePropertyType surfacePropertyType = new SurfacePropertyType();
		surfacePropertyType.setAbstractSurface(jaxbElementPolygonType);
		
		
		
		ar5.setOmråde(surfacePropertyType);

		return ar5;
	}
	
	// move to common classes
	private XMLGregorianCalendar getXmlCalender(Date dato) {
		XMLGregorianCalendar now = null; 
		        
		GregorianCalendar gregorianCalendar = new GregorianCalendar();
		gregorianCalendar.setTime(dato);
        DatatypeFactory datatypeFactory;
		try {
			datatypeFactory = DatatypeFactory.newInstance();
	        now = 
	                datatypeFactory.newXMLGregorianCalendar(gregorianCalendar);

		} catch (DatatypeConfigurationException e) {
			logger.error("Failed to convert " + dato + " to XMLGregorianCalendar"); 
			
		}
		
		return now;
	}


	/*
	    <ar5:arealtype codeSpace="http://www.geosynkronisering.no/files/skjema/Arealressurs/4.5/ArealressursArealtype.xml">12</ar5:arealtype>
        <ar5:skogbonitet codeSpace="http://www.geosynkronisering.no/files/skjema/Arealressurs/4.5/ArealressursSkogbonitet.xml">98</ar5:skogbonitet>
        <ar5:treslag codeSpace="http://www.geosynkronisering.no/files/skjema/Arealressurs/4.5/ArealressursTreslag.xml">98</ar5:treslag>
        <ar5:grunnforhold codeSpace="http://www.geosynkronisering.no/files/skjema/Arealressurs/4.5/ArealressursGrunnforhold.xml">98</ar5:grunnforhold>
        <ar5:kartstandard codeSpace="http://www.geosynkronisering.no/files/skjema/Arealressurs/4.5/ArealressursKartstandard.xml">AR5</ar5:kartstandard>

	 */
	private AbstractCodeType makeAbstractType(String value, String codeSpaceType) {
		AbstractCodeType abstractCodeType = new AbstractCodeType();
		
		//String codeSpaceTypeNew = codeSpaceType.substring(0, 1).toUpperCase() + codeSpaceType.substring(1).toLowerCase();
		
		String codeSpace = "http://www.geosynkronisering.no/files/skjema/Arealressurs/4.5/" + codeSpaceType +".xml";
				
		abstractCodeType.setCodeSpace(codeSpace);
		abstractCodeType.setValue(value);
		return abstractCodeType;
	}

	public ArealressursGrenseType simpleAr5Grense() throws ParseException {
		no.geonorge.skjema.sosi.produktspesifikasjon.Arealressurs_45.ObjectFactory ofar5 = new no.geonorge.skjema.sosi.produktspesifikasjon.Arealressurs_45.ObjectFactory();

		ArealressursGrenseType ar5 = ofar5.createArealressursGrenseType();

		// ar5.setArealtype(arealtype);

		Calendar datafangstdato = Calendar.getInstance();
		ar5.setDatafangstdato(getXmlCalender(datafangstdato.getTime()));

		opengis.net.gml_3_2_1.gml.ObjectFactory of = new opengis.net.gml_3_2_1.gml.ObjectFactory();

		

//		// does not work in snowflake, qgis
//		// <ar5:grense><gml:LineString srsName="urn:ogc:def:crs:EPSG::4258" gml:id="NO.SK.AR5:-488458468"><gml:posList>5...
//		LineStringType lineStringType = (LineStringType) JTS2GML321.toGML(borderLineString);
//		CurvePropertyType curvePropertyType = of.createCurvePropertyType();
//		JAXBElement<LineStringType> abstractCurve = of.createLineString(lineStringType);
//		curvePropertyType.setAbstractCurve(abstractCurve);
//		ar5.setGrense(curvePropertyType );
		

// use curve snowflake, qgis
		//CurvePropertyType curvePropertyType2 = createCompositeCurveType(of,borderLineString);
		CurvePropertyType curvePropertyType2 = creatCurveType(of,borderLineString);
		

			ar5.setGrense(curvePropertyType2);
		
		
		
		
		/*
		<gml:CompositeCurveType xmlns:gml="http://www.opengis.net/gml/3.2" srsName="EPSG:4269" gml:id="l185">
		   <gml:curveMember>
		      <gml:Curve xmlns:gml="http://www.opengis.net/gml/3.2" gml:id="l185_1">
		         <gml:segments>
		            <gml:LineStringSegment>
		               <gml:posList srsDimension="2">-91.1716151689462 46.8441439939094 -91.1709825689471 46.8442585272425 -91.1703163689482 46.8442359272426 -91.1680853689517 46.8445793939087</gml:posList>
		            </gml:LineStringSegment>
		         </gml:segments>
		      </gml:Curve>
		   </gml:curveMember>
		   <gml:curveMember>
		      <gml:Curve xmlns:gml="http://www.opengis.net/gml/3.2" gml:id="l185_2">
		         <gml:segments>
		           <gml:LineStringSegment>
		              <gml:posList srsDimension="2">-91.1727139689444 46.843937927243 -91.1716151689462 46.8441439939094</gml:posList>
		           </gml:LineStringSegment>
		         </gml:segments>
		      </gml:Curve>
		   </gml:curveMember>
		</gml:CompositeCurveType>

*/
		ar5.setAvgrensingType(makeAbstractType("12","ArealressursAvgrensingType"));
		
		{
			PosisjonskvalitetPropertyType posisjonskvalitetPropertyType = new PosisjonskvalitetPropertyType();
			PosisjonskvalitetType posisjonskvalitetType = new PosisjonskvalitetType();
			posisjonskvalitetPropertyType.setPosisjonskvalitet(posisjonskvalitetType);
			ar5.setKvalitet(posisjonskvalitetPropertyType);

			posisjonskvalitetType.setNøyaktighet(new BigInteger("10"));
			posisjonskvalitetType.setSynbarhet(makeAbstractType("10", "Synbarhet"));
			posisjonskvalitetType.setMålemetode(makeAbstractType("12","Målemetode"));

		}



		return ar5;
	}

/*	
	<gml:Curve srsName="urn:ogc:def:crs:EPSG::4258" gml:id="NO.SK.PKYST.KURVEMEDLEM333333">
    <gml:segments>
       <gml:LineStringSegment interpolation="linear">
          <gml:posList>60.5 10.5 60.2 10.2 60.8 10.2 60.8 10.5</gml:posList>
       </gml:LineStringSegment>
    </gml:segments>
 </gml:Curve>
*/

	private CurvePropertyType creatCurveType(opengis.net.gml_3_2_1.gml.ObjectFactory of, LineString borderLineString) {

		// TODO set id
		CurvePropertyType e = creatCurveTypeTmp(of, borderLineString);
		
		CompositeCurveType 	compositeCurveType = of.createCompositeCurveType();
		compositeCurveType.setId("ss");
	
		compositeCurveType.getCurveMembers().add(e);
		CurvePropertyType gg = new CurvePropertyType();
		JAXBElement<CompositeCurveType> abstractCurve = of.createCompositeCurve(compositeCurveType);
		gg.setAbstractCurve(abstractCurve);
		


		return gg;
	}


	private static CurvePropertyType creatCurveTypeTmp(opengis.net.gml_3_2_1.gml.ObjectFactory of, LineString borderLineString) {
		
		LineStringType lineStringType = (LineStringType) JTS2GML321.toGML(borderLineString);
		LineStringSegmentType lineStringSegmentType = of.createLineStringSegmentType();
		lineStringSegmentType.setPosList(lineStringType.getPosList());
		JAXBElement<LineStringSegmentType> e = of.createLineStringSegment(lineStringSegmentType);
		SegmentsElement segments = of.createSegmentsElement();
		segments.getAbstractCurveSegments().add(e);
		CurveType createCurveType = of.createCurveType();
		createCurveType.setSegments(segments );
		
		// a hack to set id
		createCurveType.setId(""+ borderLineString.getUserData() + borderLineString.hashCode());
		String srsName = "urn:ogc:def:crs:EPSG::" + borderLineString.getSRID();
		createCurveType.setSrsName(srsName);
		JAXBElement<CurveType> abstractCurve = of.createCurve(createCurveType);
		CurvePropertyType curvePropertyType = of.createCurvePropertyType();
		curvePropertyType.setAbstractCurve(abstractCurve);

		return curvePropertyType;
	}

 
	/*
	<ar5:grense>
    <gml:CompositeCurve>
        <gml:curveMember>
            <gml:Curve srsName="urn:ogc:def:crs:EPSG::4258" gml:id="NO.SK.AR5:-488458468">
                <gml:segments>
                    <gml:LineStringSegment>
                        <gml:posList>59.766215682960826 

                        */
	private static CurvePropertyType createCompositeCurveType(opengis.net.gml_3_2_1.gml.ObjectFactory of, LineString borderLineString) {
		
		LineStringType lineStringType = (LineStringType) JTS2GML321.toGML(borderLineString);
		LineStringSegmentType lineStringSegmentType = of.createLineStringSegmentType();
		lineStringSegmentType.setPosList(lineStringType.getPosList());
		JAXBElement<LineStringSegmentType> e = of.createLineStringSegment(lineStringSegmentType);
		SegmentsElement segments = of.createSegmentsElement();
		segments.getAbstractCurveSegments().add(e);
		CurveType createCurveType = of.createCurveType();
		createCurveType.setSegments(segments );
		
		// a hack to set id
		createCurveType.setId(""+ borderLineString.getUserData() + borderLineString.hashCode());
		String srsName = "urn:ogc:def:crs:EPSG::" + borderLineString.getSRID();
		createCurveType.setSrsName(srsName);
		JAXBElement<CurveType> abstractCurve = of.createCurve(createCurveType);
		CurvePropertyType curvePropertyType = of.createCurvePropertyType();
		curvePropertyType.setAbstractCurve(abstractCurve);



			// work with qis snowflake
			CompositeCurveType compositeCurveType = of.createCompositeCurveType();
			compositeCurveType.getCurveMembers().add(curvePropertyType);

			CurvePropertyType curvePropertyType2 = of.createCurvePropertyType();
			
			AbstractCurveType act = compositeCurveType;
			JAXBElement<CompositeCurveType> createCompositeCurve = of.createCompositeCurve(compositeCurveType);
			curvePropertyType2.setAbstractCurve(createCompositeCurve );
		return curvePropertyType2;
	}

}
