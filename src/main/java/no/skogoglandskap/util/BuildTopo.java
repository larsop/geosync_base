package no.skogoglandskap.util;

import java.util.ArrayList;

import no.skogoglandskap.ar5.Orientation;

import org.apache.log4j.Logger;
import org.geotools.data.shapefile.shp.JTSUtilities;
import org.geotools.geometry.GeometryBuilder;
import org.geotools.geometry.jts.JTS;
import org.geotools.geometry.text.WKTParser;
import org.geotools.referencing.CRS;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.Polygon;

public class BuildTopo {

	private static final double MAX_DISTANCE_DIF = 1;
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

			// make a collection of only line strings, remove points and other
			// geos
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

				MultiLineStringWithOrientation multiLineStringWithOrientation = getMultiLineStringWithOrientationThatIntersects(exteriorLineString,
						listOfCommonLineStrings);

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

	// use the list of common line strings to build up the new linestring
	private static MultiLineStringWithOrientation getMultiLineStringWithOrientationThatIntersects(LineString polyLineString,
			ArrayList<LineString> listOfCommonLineStringsTmp) {

		MathTransform transFilter = null;

		// hold the result
		MultiLineStringWithOrientation lineStringWithOrientation = new MultiLineStringWithOrientation();

		// this is a line string that represents the polygon , this may be exterior or a interior line
		LineString polygonLineString = (LineString) polyLineString.clone();

		// this is list of available line strings that are common for all polygons, one line string may be many coordinates
		ArrayList<LineString> listOfCommonLineStrings = (ArrayList<LineString>) listOfCommonLineStringsTmp.clone();

		try {

			CoordinateReferenceSystem sourceCRS;

			// wgs ---------------------------
			// http://spatialreference.org/ref/epsg/wgs-84/ EPSG:4326
			// sourceCRS = CRS.decode("EPSG:" + 4326);

			// http://spatialreference.org/ref/epsg/wgs-84-utm-zone-32n/ "EPSG:" + 32632
			// CoordinateReferenceSystem targetCRS = CRS.decode("EPSG:" + 32632);
			// etrs ------------------------------

			// http://spatialreference.org/ref/epsg/etrs89/ EPSG:4258
			sourceCRS = CRS.decode("EPSG:" + 4258);
			// http://spatialreference.org/ref/epsg/etrs89-utm-zone-33n/ EPSG:" + 25833
			CoordinateReferenceSystem targetCRS = CRS.decode("EPSG:" + 25833);
			transFilter = CRS.findMathTransform(sourceCRS, targetCRS);

			Coordinate[] coordinatesinputLineString = polygonLineString.getCoordinates();

			// check the list of common line strings and find matches for polylinestring

			while (listOfCommonLineStrings.size() > 0) {
				LineStringWithOrientation e = null;

				int removeX = -1;
				for (int x = 0; x < listOfCommonLineStrings.size(); x++) {
					removeX = x;
					;

					LineString commonLinestring = listOfCommonLineStrings.get(x);

					// if this common line string intersects with a length we hav a match
					Geometry intersection;
					if (commonLinestring.intersects(polygonLineString) && (intersection = commonLinestring.intersection(polygonLineString)).getLength() > 0) {
						// we have intersection with the list common lines that we can use
						e = new LineStringWithOrientation(commonLinestring);

						// we now need to find the orientation, this is done looping through all the points in the line string for the polygon

						// first get the end point
						Coordinate thisFirst = commonLinestring.getCoordinateN(0);
						Coordinate thisLast = commonLinestring.getCoordinateN(commonLinestring.getCoordinates().length - 1);

						if (commonLinestring.getNumPoints() == 2) {
							if (logger.isDebugEnabled()) {
								logger.debug("A line that contains two point both end and start shouldb be equal");
							}

							for (int i = 0; (i + 1) < coordinatesinputLineString.length; i++) {
								// only check points on this line

								// find this 2 coordinates in the input list of coordinatesS
								Coordinate inputFirst = coordinatesinputLineString[i];
								Coordinate inputLast = coordinatesinputLineString[i + 1];

								// test if exact equal
								if (inputFirst.equals2D(thisFirst) && inputLast.equals2D(thisLast)) {
									e.orientation = Orientation.OrientationAntiClockWise;
									if (logger.isDebugEnabled()) {
										logger.debug("Exact match, use " + e.orientation.name() + " oriatation for '" + thisFirst + "' = '" + thisLast + "'");
									}
								} else if (inputLast.equals2D(thisFirst) && inputFirst.equals2D(thisFirst)) {
									e.orientation = Orientation.OrientationClockWise;

									if (logger.isDebugEnabled()) {
										logger.debug("Exact match, use " + e.orientation.name() + " oriatation for '" + thisFirst + "' = '" + thisLast + "'");
									}
								}
							}
						} else {

							if (logger.isDebugEnabled()) {
								logger.debug("A line common line with many cooordniats " +  commonLinestring.getNumPoints());
							}

							for (int i = 0; (i + 2) < coordinatesinputLineString.length; i++) {
								// only check points on this line

								// find this 2 coordinates in the input list of coordinatesS
								Coordinate inputFirst = coordinatesinputLineString[i];
								Coordinate inputLast = coordinatesinputLineString[i + 1];

								// test if exact equal
								if (inputFirst.equals2D(thisFirst)) {
									e.orientation = Orientation.OrientationAntiClockWise;
									if (logger.isDebugEnabled()) {
										logger.debug("Exact match, use " + e.orientation.name() + " oriatation for '" + thisFirst + "' = '" + thisLast + "'");
									}
									break;
								} else if (inputLast.equals2D(thisFirst)) {
									e.orientation = Orientation.OrientationClockWise;

									if (logger.isDebugEnabled()) {
										logger.debug("Exact match, use " + e.orientation.name() + " oriatation for '" + thisFirst + "' = '" + thisLast + "'");
									}
									break;
								}
							}

						}
						// we have found a intersection
						break;
					}
				}

				LineString remove = listOfCommonLineStrings.remove(removeX);

				if (e != null) {
					if (e.orientation == null) {

						Geometry transforminputLineString = JTS.transform(switchXY(polygonLineString), transFilter);
						Geometry removeLine = JTS.transform(switchXY(remove), transFilter);

						logger.error("No orientation found for " + removeLine.toText());

					}
					lineStringWithOrientation.multiLineStringOrientation.add(e);
				} else {
					break;
				}
			}

		} catch (Exception e1) {
			logger.debug("Failed to find dirtance in meter ", e1);
		}

		return lineStringWithOrientation;
	}


	private static Geometry switchXY(Geometry inputLineString) throws TransformException {

		Geometry clone = (Geometry) inputLineString.clone();
		Coordinate[] coordinates = clone.getCoordinates();
		for (Coordinate coordinate : coordinates) {
			double newX = coordinate.y;
			coordinate.y = coordinate.x;
			coordinate.x = newX;
		}

		return clone;
	}

	private static Coordinate fromDegreeToMeter(MathTransform transFilter, Coordinate inputLast) throws TransformException {
		Coordinate inputLastMeter = (Coordinate) inputLast.clone();
		inputLastMeter.x = inputLast.y;
		inputLastMeter.y = inputLast.x;
		inputLastMeter = JTS.transform(inputLastMeter, null, transFilter);
		return inputLastMeter;
	}

	private static double getDistance(MathTransform transFilter, Coordinate inputLast, Coordinate thisLast) throws TransformException {
		Coordinate inputLastMeter = fromDegreeToMeter(transFilter, inputLast);

		Coordinate thisLastMeter = fromDegreeToMeter(transFilter, thisLast);

		double distanceLast = inputLastMeter.distance(thisLastMeter);
		//
		// if (logger.isDebugEnabled()) {
		// logger.debug("distance beetween  '" + inputLastMeter + "' != '" + thisLastMeter + "' in meter is " + distanceLast);
		// }
		return distanceLast;
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
