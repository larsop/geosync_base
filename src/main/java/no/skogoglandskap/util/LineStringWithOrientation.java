package no.skogoglandskap.util;

import no.skogoglandskap.ar5.Orientation;

import com.vividsolutions.jts.geom.LineString;

public class LineStringWithOrientation {

	public LineStringWithOrientation(LineString commonLinestring) {
		lineString = commonLinestring;
	}
	public LineString lineString;
	public Orientation orientation = null;
	 
}
