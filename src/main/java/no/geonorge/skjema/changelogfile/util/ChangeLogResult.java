package no.geonorge.skjema.changelogfile.util;

import java.util.ArrayList;
/**
 * @author lars
 * 
 * Hold the content as is it read from the the changelog. 
 * 
 * This is also used as input for creating a changelog that that can be sent to client. 
 * 
 * The operations we can get from the changelog is insert, update and delete.
 *
 */
public class ChangeLogResult {

	// As it looks now we may not this class but I keep so we one common places to keep info for one class
	
	/**
	 * Holds operations that from and to wfs 
	 */
	public ArrayList<WSFOperation > wfsOerationList = new ArrayList<>();
	
	

}
