package no.skogoglandskap.ar5;

import no.skogoglandskap.datamodel.PolygonFeature;

import com.vividsolutions.jts.geom.MultiLineString;

/**
 * This a class used to hold linestrings that belongs to this feature.
 * 
 * These are needed when we build the polygons 
 *   
 * @author lop
 *
 */
//TOTO make attributes private 
public class TopoGeometry {

	// a ref to the the object that hold the original feature
	public PolygonFeature ar5FlateProvSimpleFeatureEntity;

	// the list of linestrings that builds up the polygon exterior 
	public MultiLineString exteriorLineStrings;
	
	// the list of linestrings that builds up the polygon holes
	public MultiLineString[] holes;

	/**
	 * 
	 * @param ar5FlateProvSimpleFeatureEntity
	 * @param exteriorLineStrings
	 * @param holes
	 */
	public TopoGeometry(PolygonFeature ar5FlateProvSimpleFeatureEntity, MultiLineString exteriorLineStrings, MultiLineString[] holes) {
		this.ar5FlateProvSimpleFeatureEntity = ar5FlateProvSimpleFeatureEntity;
		this.exteriorLineStrings = exteriorLineStrings;
		this.holes = holes;
	}


}
