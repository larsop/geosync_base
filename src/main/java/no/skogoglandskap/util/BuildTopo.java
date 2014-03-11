package no.skogoglandskap.util;

import java.util.ArrayList;

import no.skogoglandskap.ar5.Orientation;

import org.apache.log4j.Logger;
import org.geotools.data.shapefile.shp.JTSUtilities;
import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.Polygon;

public class BuildTopo {

	private static Logger logger = Logger.getLogger(BuildTopo.class);
	private static GeometryFactory gf = new GeometryFactory();

	/**
	 * A helper method to find all common line strings from list polygons.
	 * 
	 * @param providerData
	 *            The list of input polygons to work on
	 * 
	 * @return the list of common line strings where duplicates are removed.
	 * 
	 * 
	 */
	public static ArrayList<LineString> findAllCommonLinestrings(ArrayList<PolygonFeature> providerData) {

		// the result where common line strings are shared
		ArrayList<LineString> lineStringsNew = new ArrayList<LineString>();

		if (providerData == null || providerData.size() == 0) {
			if (logger.isDebugEnabled()) {
				logger.debug("enter, no data in for providerData");
			}
		} else {
			if (logger.isDebugEnabled()) {
				logger.debug("enter, handle data with " + providerData.size() + " geos ");
			}

			// holds the list of all line strings found in input geos
			ArrayList<LineString> lineStringsOrg = new ArrayList<LineString>();

			// the srid used
			Integer srid = providerData.get(0).getGeo().getSRID();

			// find all line strings in all geometries both Exterior and
			// Interior

			for (int i = 0; i < providerData.size(); i++) {
				Polygon geo = providerData.get(i).getGeo();
				LineString lineString = geo.getExteriorRing();
				lineStringsOrg.add(lineString);

				for (int x = 0; x < geo.getNumInteriorRing(); x++) {
					LineString ls = geo.getInteriorRingN(x);
					lineStringsOrg.add(ls);
				}

			}

			if (logger.isDebugEnabled()) {
				logger.debug("Total number of linstrings before union " + lineStringsOrg.size());
			}

			// Create a collection of org linestrings
			MultiLineString geometryCollectionOrgMultiLineString = gf.createMultiLineString(lineStringsOrg.toArray(new LineString[lineStringsOrg.size()]));

			// // Get a union of all of the line strings
			Geometry newLinstringGeo = geometryCollectionOrgMultiLineString.union();

			// make a collection of only line strings, remove points and other geos
			for (int i = 0; i < newLinstringGeo.getNumGeometries(); i++) {
				Geometry g = newLinstringGeo.getGeometryN(i);
				if (g instanceof LineString) {
					lineStringsNew.add((LineString) g);
					g.setSRID(srid);

				}
			}

			if (logger.isDebugEnabled()) {
				logger.debug("Total number of linstrings after union " + lineStringsNew.size());
			}

		}

		if (logger.isDebugEnabled()) {
			logger.debug("leave");
		}

		return lineStringsNew;

	}

	/**
	 * Find all linestrings that belongs to each polygon and based on the list og listOfCommonLineStrings
	 * 
	 * @param providerData
	 * @param listOfCommonLineStrings
	 */

	public static ArrayList<TopoGeometry> getGeoWithCommonLinestrings(ArrayList<PolygonFeature> providerData, ArrayList<LineString> listOfCommonLineStrings) {

		ArrayList<TopoGeometry> lr = new ArrayList<>();

		if (providerData == null || providerData.size() == 0) {
			if (logger.isDebugEnabled()) {
				logger.debug("enter, no data in for providerData");
			}
		} else {
			if (logger.isDebugEnabled()) {
				logger.debug("enter, handle data with " + providerData.size() + " geos ");
			}

			// find the linestrings that belongs to each polygon
			for (PolygonFeature polygonFeature : providerData) {

				Polygon p = (Polygon) polygonFeature.getGeo();

				LineString exteriorLineString = p.getExteriorRing();

				ArrayList<LineString> exteriorLineStringtmp = getLineStringsThatIntersects(exteriorLineString, listOfCommonLineStrings);

				MultiLineStringWithOrientation multiLineStringWithOrientation = getMultiLineStringWithOrientation(exteriorLineString, listOfCommonLineStrings);

				MultiLineString exteriorLineStrings = gf.createMultiLineString(exteriorLineStringtmp.toArray(new LineString[exteriorLineStringtmp.size()]));

				MultiLineString[] holes = new MultiLineString[p.getNumGeometries()];

				for (int i = 0; i < p.getNumInteriorRing(); i++) {
					LineString holeLineString = p.getInteriorRingN(i);

					ArrayList<LineString> holeLineStringTmp = getLineStringsThatIntersects(holeLineString, listOfCommonLineStrings);
					MultiLineString newLis = gf.createMultiLineString(holeLineStringTmp.toArray(new LineString[holeLineStringTmp.size()]));

					holes[i] = newLis;
				}

				TopoGeometry tg = new TopoGeometry(polygonFeature, exteriorLineStrings, holes, multiLineStringWithOrientation);

				lr.add(tg);

			}

			if (logger.isDebugEnabled()) {
				logger.debug("leave with new Topolist " + providerData.size() + " geos ");
			}

		}

		if (logger.isDebugEnabled()) {
			logger.debug("leave");
		}

		return lr;
	}

	private static MultiLineStringWithOrientation getMultiLineStringWithOrientation(LineString inputLineString, ArrayList<LineString> listOfCommonLineStrings) {
		// hold the list linstrings that is of interest for this polygon

		MultiLineStringWithOrientation lineStringWithOrientation = new MultiLineStringWithOrientation();

		// get all that touch touches this linestring
		for (LineString ls : listOfCommonLineStrings) {
			Geometry intersection;

			// TODO Maybe we need a better way to this
			if (inputLineString.intersects(ls) && (intersection = inputLineString.intersection(ls)).getLength() > 0.00000) {

				LineStringWithOrientation e = new LineStringWithOrientation();
				e.lineString = ls;

				Coordinate inputFirst = inputLineString.getCoordinateN(0);
				Coordinate thisFirst = ls.getCoordinateN(0);
				Coordinate inputLast = inputLineString.getCoordinateN(inputLineString.getCoordinates().length - 1);
				Coordinate thisLast = ls.getCoordinateN(ls.getCoordinates().length - 1);
				if (inputFirst.equals2D(thisFirst)) {
					//e.orientation = Orientation.OrientationAntiClockWise;
					if (logger.isDebugEnabled()) {
						logger.debug("Use clockwise oriatation for '" + thisFirst + "' != '" + thisLast + "'");
					}

					// TDOD find out way this is different
					if (!inputLast.equals2D(thisLast)) {
						if (logger.isDebugEnabled()) {
							logger.debug("distance beetween  '" + inputLast + "' != '" + thisLast + "' in degree is " + inputLast.distance(thisLast));
							CoordinateReferenceSystem sourceCRS;
							try {
								// this is hack for debug test
								sourceCRS = CRS.decode("EPSG:" + 4258);
								CoordinateReferenceSystem targetCRS = CRS.decode("EPSG:" + 25833);
								MathTransform transFilter = CRS.findMathTransform(sourceCRS, targetCRS);

								Coordinate inputLastMeter = (Coordinate) inputLast.clone();
								inputLastMeter.x = inputLast.y;
								inputLastMeter.y = inputLast.x;
								inputLastMeter = JTS.transform(inputLastMeter, null, transFilter);

								Coordinate thisLastMeter = (Coordinate) thisLast.clone();
								thisLastMeter.x = thisLast.y;
								thisLastMeter.y = thisLast.x;
								thisLastMeter = JTS.transform(thisLastMeter, null, transFilter);


								logger.debug("distance beetween  '" + inputLastMeter + "' != '" + thisLastMeter + "' in meter is " + inputLastMeter.distance(thisLastMeter));

							} catch (Exception e1) {
								logger.debug("Failed to find dirtance in meter ", e1);
							}
 
							
							
						}
					}

				} else {
					e.orientation = Orientation.OrientationAntiClockWise;
					if (logger.isDebugEnabled()) {
						logger.debug("Use non clockwise oriatation for '" + thisFirst + "' != '" + thisLast + "'");
					}

				}

				lineStringWithOrientation.multiLineStringOrientation.add(e);

			}

		}

		return lineStringWithOrientation;
	}

	/**
	 * Build up a linestring list based based on the list listOfCommonLineStrings that overlaps with the inputLineString
	 * 
	 * @param inputLineString
	 * @param listOfCommonLineStrings
	 * @return the new list of linestrings
	 */
	private static ArrayList<LineString> getLineStringsThatIntersects(LineString inputLineString, ArrayList<LineString> listOfCommonLineStrings) {
		// hold the list linstrings that is of interest for this polygon
		ArrayList<LineString> lineStringstmp = new ArrayList<LineString>();

		// get all that touch touches this linestring
		for (LineString ls : listOfCommonLineStrings) {
			// TODO Maybe we need a better way to this
			if (inputLineString.intersects(ls) && inputLineString.intersection(ls).getLength() > 0.00000) {
				lineStringstmp.add(ls);
			}
		}
		return lineStringstmp;
	}

}
