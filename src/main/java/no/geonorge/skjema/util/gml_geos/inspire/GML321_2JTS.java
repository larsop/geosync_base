/**
 * 
 */
package no.geonorge.skjema.util.gml_geos.inspire;


import java.util.List;

import opengis.net.gml_3_2_1.gml.AbstractRingType;
import opengis.net.gml_3_2_1.gml.CoordinatesType;
import opengis.net.gml_3_2_1.gml.CurvePropertyType;
import opengis.net.gml_3_2_1.gml.LineStringType;
import opengis.net.gml_3_2_1.gml.PosListElement;
import opengis.net.gml_3_2_1.gml.RingType;


import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Polygon;

/**
 * Conversion methods from GML geometries created by Inspire to JTS geometries.
 * 
 * @author Lars Opsahl
 * 
 *         This is only test cose
 * 
 */
public class GML321_2JTS {

	/**
	 * Generic method to convert from GML to JTS 
	 * 
	 * @param geom
	 * @param srsName
	 * @return
	 */
	public static Geometry toJTS(AbstractRingType geom) {
		if (geom instanceof RingType)
			return toJTSImpl((RingType) geom);
		else
			throw new RuntimeException("Not handle conevertion from GML to JTS  " + geom.getClass().getSimpleName());
	}

	
//	/**
//	 * Bad code only used for testing
//	 * 
//	 * @param ringType
//	 * @return
//	 */
//	// TOTO fix this code
//	// TODO handle srid
//	private static Geometry toJTSImpl(RingType geom) {
//		if (geom.getCurveMembers().size() == 1) {
//			
//			List<CurvePropertyType> curveMembers = geom.getCurveMembers();
//			CurvePropertyType ct = curveMembers.get(0);
//			LineStringType value2 = (LineStringType) ct.getAbstractCurve().getValue();
//			CoordinatesType value3 = value2.getCoordinates();
//			String value4 = value3.getValue();
//			String[] split = value4.split(" ");
//			Coordinate[] coordinates = new Coordinate[split.length];
//
//			for (int i = 0; i < coordinates.length; i++) {
//				String string = split[i];
//				String[] split2 = string.split(",");
//				coordinates[i] = new Coordinate(new Double(split2[0]), new Double(split2[1]));
//
//			}
//
//			LineString g1 = new GeometryFactory().createLineString(coordinates);
//			Polygon createPolygon = new GeometryFactory().createPolygon(g1.getCoordinateSequence());
//			return createPolygon;
//		} else {
//			throw new RuntimeException("Not handle conevertion from GML to JTS  Polygon " + geom.getClass().getSimpleName());
//
//		}
//	}

	/**
	 * Bad code only used for testing
	 * 
	 * @param ringType
	 * @return
	 */
	// TOTO fix this code
	// TODO handle srid
	private static Geometry toJTSImpl(RingType geom) {
		if (geom.getCurveMembers().size() == 1) {
			
			List<CurvePropertyType> curveMembers = geom.getCurveMembers();
			CurvePropertyType ct = curveMembers.get(0);
			LineStringType value2 = (LineStringType) ct.getAbstractCurve().getValue();
			PosListElement value3 = value2.getPosList();
			List<Double> values = value3.getValues();
			
			Coordinate[] coordinates = new Coordinate[values.size()/2];

			int coordinatei = 0; 
			for (int i = 0; i < coordinates.length; i++) {
				coordinates[i] = new Coordinate(values.get(coordinatei++), new Double(values.get(coordinatei++)));
			}

			LineString g1 = new GeometryFactory().createLineString(coordinates);
			Polygon createPolygon = new GeometryFactory().createPolygon(g1.getCoordinateSequence());
			return createPolygon;
		} else {
			throw new RuntimeException("Not handle conevertion from GML to JTS  Polygon " + geom.getClass().getSimpleName());

		}
	}

}
