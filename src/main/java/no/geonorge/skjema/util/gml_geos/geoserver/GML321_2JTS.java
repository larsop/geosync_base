/**
 * 
 */
package no.geonorge.skjema.util.gml_geos.geoserver;


import java.util.List;

import opengis.net.gml_3_2_1.gml.AbstractRingType;


import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Polygon;

/**
 * Conversion methods from GML geometries created by Geoserver to JTS geometries
 * 
 * @author Lars Opsahl
 * 
 *         This is only test cose
 * 
 */
public class GML321_2JTS {

	static GeometryFactory geometryFactory = new GeometryFactory();

	/**
	 * Generic method to convert from GML to JTS
	 * 
	 * @param geom
	 * @param srsName
	 * @return
	 */
	public static Geometry toJTS(AbstractRingType geom) {
		if (geom instanceof opengis.net.gml_3_2_1.gml.LinearRingType)
			return toJTSImpl((opengis.net.gml_3_2_1.gml.LinearRingType) geom);
		else
			throw new RuntimeException("Not handle conevertion from GML to JTS  " + geom.getClass().getSimpleName());
	}

	/**
	 * Bad code only used for testing
	 * 
	 * @param ringType
	 * @return
	 */
	// TOTO fix this code
	// TODO handle srid
	private static Geometry toJTSImpl(opengis.net.gml_3_2_1.gml.LinearRingType geom) {

		if (geom != null) {

			List<Double> values = geom.getPosList().getValues();
			Coordinate[] coordinates = new Coordinate[values.size() / 2];

			int ix = 0;
			for (int i = 0; i < values.size();) {
				coordinates[ix++] = new Coordinate(new Double(values.get(i++)), new Double(values.get(i++)));

			}

			LineString g1 = geometryFactory.createLineString(coordinates);
			Polygon createPolygon = geometryFactory.createPolygon(g1.getCoordinateSequence());
			return createPolygon;
		} else {
			throw new RuntimeException("Not handle conevertion from GML to JTS  Polygon " + geom.getClass().getSimpleName());

		}
	}

}
