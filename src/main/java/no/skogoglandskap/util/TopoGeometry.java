package no.skogoglandskap.util;

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
	
	// TODO remove exteriorLineStrings use exteriorLineStringsOrientation
	@Deprecated
	private MultiLineString exteriorLineStrings;
	
	
	
	// the list of linestrings that builds up the polygon holes
	public MultiLineString[] holes;


	
	public MultiLineStringWithOrientation exteriorLineStringsOrientation;

	/**
	 * 
	 * @param ar5FlateProvSimpleFeatureEntity
	 * @param exteriorLineStrings
	 * @param holes
	 */
	public TopoGeometry(PolygonFeature ar5FlateProvSimpleFeatureEntity, MultiLineString exteriorLineStrings, MultiLineString[] holes, MultiLineStringWithOrientation exteriorLineStringsOrientation) {
		this.ar5FlateProvSimpleFeatureEntity = ar5FlateProvSimpleFeatureEntity;
		this.exteriorLineStrings = exteriorLineStrings;
		this.holes = holes;
		this.exteriorLineStringsOrientation = exteriorLineStringsOrientation;
		
		
	}


}
