package no.skogoglandskap.util;

import com.vividsolutions.jts.geom.Polygon;

/**
 * This is a interface created to make geometry methods generic.
 *   
 * @author lop
 *
 */
public interface PolygonFeature {
	/**
	 * Return the geo which may be casted t
	 * @return
	 */
	public Polygon getGeo();

}
