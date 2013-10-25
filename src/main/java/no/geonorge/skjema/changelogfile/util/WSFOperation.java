package no.geonorge.skjema.changelogfile.util;

public class WSFOperation {

	// the DAO data to be added, this is a DAO where all objects are extracted.
	public Object product;
	
	// what ype of opeartoins
	public SupportedWFSOperationType wfsOperationType;

	// this is needed when run in a multi threaded enviroment.
	public int operationNumber;

	public WSFOperation(int operationNumber, SupportedWFSOperationType wfsOperationType, Object product) {
		this.operationNumber = operationNumber;
		this.wfsOperationType = wfsOperationType;
		this.product = product;
	}
	

}
