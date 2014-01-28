package no.skogoglandskap.ar5;

import no.skogoglandskap.datamodel.postgres.provider.Ar5FlateProvSimpleFeatureEntity;

import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;

public class TopoGeometry {

	public Ar5FlateProvSimpleFeatureEntity ar5FlateProvSimpleFeatureEntity;
	public TopoGeometry(Ar5FlateProvSimpleFeatureEntity ar5FlateProvSimpleFeatureEntity) {
		this.ar5FlateProvSimpleFeatureEntity = ar5FlateProvSimpleFeatureEntity;
		// TODO Auto-generated constructor stub
	}
	public MultiLineString exteriorLineStrings;
	public MultiLineString[] holes;

}
