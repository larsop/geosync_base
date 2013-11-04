/**
 * 
 */
package no.geonorge.skjema.util.gml_geos.inspire;


import javax.xml.bind.JAXBElement;

import opengis.net.gml_3_2_1.gml.AbstractGeometricAggregateType;
import opengis.net.gml_3_2_1.gml.AbstractGeometryType;
import opengis.net.gml_3_2_1.gml.AbstractRingPropertyType;
import opengis.net.gml_3_2_1.gml.CoordinatesType;
import opengis.net.gml_3_2_1.gml.CurvePropertyType;
import opengis.net.gml_3_2_1.gml.DirectPositionType;
import opengis.net.gml_3_2_1.gml.EnvelopeType;
import opengis.net.gml_3_2_1.gml.LineStringType;
import opengis.net.gml_3_2_1.gml.MultiCurveType;
import opengis.net.gml_3_2_1.gml.MultiGeometryType;
import opengis.net.gml_3_2_1.gml.MultiPointType;
import opengis.net.gml_3_2_1.gml.MultiSurfaceType;
import opengis.net.gml_3_2_1.gml.ObjectFactory;
import opengis.net.gml_3_2_1.gml.PointPropertyType;
import opengis.net.gml_3_2_1.gml.PointType;
import opengis.net.gml_3_2_1.gml.PolygonType;
import opengis.net.gml_3_2_1.gml.RingType;
import opengis.net.gml_3_2_1.gml.SurfacePropertyType;


import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

/**
 * Conversion methods from JTS geometries to GML 3.2.1 geometries.
 * 
 * @author julien Gaffuri
 * 
 *         modfied by Lars Opsahl to new Shema
 * 
 */
public class JTS2GML321 {
	private static final ObjectFactory ofGML = new ObjectFactory();

	public static AbstractGeometryType toGML(Geometry geom) {
		return toGML(geom, null);
	}

	public static AbstractGeometryType toGML(Geometry geom, String srsName) {
		if (geom instanceof Point)
			return toGML((Point) geom, srsName);
		else if (geom instanceof LinearRing)
			return toGML((LineString) geom, srsName);
		else if (geom instanceof LineString)
			return toGML((LineString) geom, srsName);
		else if (geom instanceof Polygon)
			return toGML((Polygon) geom, srsName);
		else if (geom instanceof MultiPoint)
			return toGML((MultiPoint) geom, srsName);
		else if (geom instanceof MultiLineString)
			return toGML((MultiLineString) geom, srsName);
		else if (geom instanceof MultiPolygon)
			return toGML((MultiPolygon) geom, srsName);
		else if (geom instanceof GeometryCollection)
			return toGML((GeometryCollection) geom, srsName);
		throw new RuntimeException("Failed to to convert from GML to JTS + " + geom.getClass().getSimpleName());
	}

	private static DirectPositionType toGML(Coordinate c, String srsName) {
		DirectPositionType dp = ofGML.createDirectPositionType();
		dp.getValues().add(new Double(c.x));
		dp.getValues().add(new Double(c.y));
		if (srsName != null)
			dp.setSrsName(srsName);
		if (!Double.isNaN(c.z))
			dp.getValues().add(new Double(c.z));
		return dp;
	}

	private static PointType toGML(Point pt, String srsName) {
		PointType ptOut = ofGML.createPointType();
		ptOut.setPos(toGML(pt.getCoordinate(), null));
		if (srsName != null)
			ptOut.setSrsName(srsName);
		return ptOut;
	}

	private static LineStringType toGML(LineString line, String srsName) {
		LineStringType ls = ofGML.createLineStringType();
		CoordinatesType cst = ofGML.createCoordinatesType();
		StringBuffer st = new StringBuffer();
		for (int i = 0; i < line.getCoordinateSequence().size(); i++) {
			Coordinate c = line.getCoordinateSequence().getCoordinate(i);
			if (i != 0)
				st.append(" ");
			st.append(c.x).append(",").append(c.y);
			if (!Double.isNaN(c.z))
				st.append(",").append(c.z);
		}
		// <gml:coordinates>-71.16028,42.258729 -71.160837,42.259112 -71.161143,42.25932</gml:coordinates>
		cst.setValue(st.toString());
		ls.setCoordinates(cst);
		if (srsName != null)
			ls.setSrsName(srsName);
		return ls;
	}

	private static RingType toGML(LinearRing line) {
		RingType rt = ofGML.createRingType();
		CurvePropertyType cpt = ofGML.createCurvePropertyType();
		LineStringType curve = toGML(line, null);
		JAXBElement<LineStringType> act = ofGML.createLineString(curve);
		cpt.setAbstractCurve(act);
		rt.getCurveMembers().add(cpt);
		return rt;
	}

	private static PolygonType toGML(Polygon poly, String srsName) {
		PolygonType pt = ofGML.createPolygonType();

		// exterior
		LineString ls = poly.getExteriorRing();
		LinearRing lr = ls.getFactory().createLinearRing(ls.getCoordinateSequence());
		RingType rt = toGML(lr);
		JAXBElement<RingType> art = ofGML.createRing(rt);
		AbstractRingPropertyType arp = ofGML.createAbstractRingPropertyType();
		arp.setAbstractRing(art);
		pt.setExterior(arp);

		// interiors
		for (int i = 0; i < poly.getNumInteriorRing(); i++) {
			LineString ls_ = poly.getInteriorRingN(i);
			LinearRing lr_ = ls_.getFactory().createLinearRing(ls_.getCoordinateSequence());
			RingType rt_ = toGML(lr_);
			JAXBElement<RingType> art_ = ofGML.createRing(rt_);
			AbstractRingPropertyType arp_ = ofGML.createAbstractRingPropertyType();
			arp_.setAbstractRing(art_);
			pt.getInteriors().add(arp_);
		}
		if (srsName != null)
			pt.setSrsName(srsName);
		return pt;
	}

	private static MultiPointType toGML(MultiPoint mpt, String srsName) {
		MultiPointType mp = ofGML.createMultiPointType();
		for (int i = 0; i < mpt.getNumGeometries(); i++) {
			Point pt = (Point) mpt.getGeometryN(i);
			PointPropertyType pp = ofGML.createPointPropertyType();
			pp.setPoint(toGML(pt, null));
			mp.getPointMembers().add(pp);
		}
		if (srsName != null)
			mp.setSrsName(srsName);
		return mp;
	}

	private static MultiCurveType toGML(MultiLineString mls, String srsName) {
		System.out.println("JTS geometry type not treated yet: " + mls.getClass().getSimpleName() + " " + srsName);
		return null;
	}

	private static MultiSurfaceType toGML(MultiPolygon mp, String srsName) {
		MultiSurfaceType ms = ofGML.createMultiSurfaceType();
		for (int i = 0; i < mp.getNumGeometries(); i++) {
			Polygon poly = (Polygon) mp.getGeometryN(i);
			PolygonType ast = toGML(poly, null);
			JAXBElement<PolygonType> saf = ofGML.createPolygon(ast);
			SurfacePropertyType spt = ofGML.createSurfacePropertyType();
			spt.setAbstractSurface(saf);
			ms.getSurfaceMembers().add(spt);
		}
		if (srsName != null)
			ms.setSrsName(srsName);
		return ms;
	}

	private static AbstractGeometricAggregateType toGML(GeometryCollection gc, String srsName) {
		if (gc instanceof MultiPoint)
			return toGML((MultiPoint) gc, srsName);
		else if (gc instanceof MultiLineString)
			return toGML((MultiLineString) gc, srsName);
		else if (gc instanceof MultiPolygon)
			return toGML((MultiPolygon) gc, srsName);
		else
			System.err.println("JTS geometry type not treated: " + gc.getClass().getSimpleName());
		return null;
	}

	public static EnvelopeType toGML(Envelope env) {
		return toGML(env, null);
	}

	public static EnvelopeType toGML(Envelope env, String srsName) {
		EnvelopeType envt = ofGML.createEnvelopeType();
		envt.setLowerCorner(toGML(new Coordinate(env.getMinX(), env.getMinY()), null));
		envt.setUpperCorner(toGML(new Coordinate(env.getMaxX(), env.getMaxY()), null));
		envt.setSrsName(srsName);
		return envt;
	}

	public static JAXBElement<? extends AbstractGeometryType> toJAXBGML(Geometry geom) {
		return toJAXBGML(geom, null);
	}

	public static JAXBElement<? extends AbstractGeometryType> toJAXBGML(Geometry geom, String srsName) {
		if (geom instanceof Point) {
			return ofGML.createPoint((PointType) JTS2GML321.toGML(geom, srsName));
		} else if (geom instanceof LineString) {
			return ofGML.createLineString((LineStringType) JTS2GML321.toGML(geom, srsName));
		} else if (geom instanceof Polygon) {
			return ofGML.createPolygon((PolygonType) JTS2GML321.toGML(geom, srsName));
		} else if (geom instanceof MultiPoint) {
			return ofGML.createMultiPoint((MultiPointType) JTS2GML321.toGML(geom, srsName));
		} else if (geom instanceof MultiLineString) {
			return ofGML.createMultiCurve((MultiCurveType) JTS2GML321.toGML(geom, srsName));
		} else if (geom instanceof MultiPolygon) {
			return ofGML.createMultiSurface((MultiSurfaceType) JTS2GML321.toGML(geom, srsName));
		} else if (geom instanceof GeometryCollection) {
			return ofGML.createMultiGeometry((MultiGeometryType) JTS2GML321.toGML(geom, srsName));
		} else
			System.out.println("JTS geometry type not treated: " + geom.getClass().getSimpleName());
		return null;
	}

}
