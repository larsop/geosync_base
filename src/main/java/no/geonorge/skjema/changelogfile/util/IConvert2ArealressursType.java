package no.geonorge.skjema.changelogfile.util;

import java.util.UUID;

import no.geonorge.skjema.sosi.produktspesifikasjon.Arealressurs_45.ArealressursFlateType;
import no.geonorge.skjema.sosi.produktspesifikasjon.Arealressurs_45.ArealressursGrenseType;

/**
 * This interface defines a method for how to convert data from that generated by the provider to the format used in the change log file.
 * 
 * @author lop
 * 
 */

public interface IConvert2ArealressursType {

	/**
	 * Convert the input object to a ArealressursFlateType. The format of the input object is know by the supplier.
	 * The format of ArealressursFlateType is know by the system    
	 *  
	 * @param LokalId the UUID for the local id 
	 * @param input a object of type know by the data provider which contains enough info to create ArealressursFlateType   
	 * 
	 * @return the generated objected
	 */
	public ArealressursFlateType convert2FlateFromProv(UUID LokalId, Object input, boolean useXlinKHref);
	
	/**
	 * Convert the input object to a ArealressursGrenseType. The format of the input object is know by the supplier.
	 * The format of ArealressursGrenseType is know by the system    
	 *  
	 * @param LokalId the UUID for the local id 
	 * 
	 * @param input a object of type know by the data provider which contains enough info to create ArealressursGrenseType   
	 * 
	 * @return the generated objected
	 */
	public ArealressursGrenseType convert2GrenseType(UUID LokalId, Object input);
	
	
	

}
