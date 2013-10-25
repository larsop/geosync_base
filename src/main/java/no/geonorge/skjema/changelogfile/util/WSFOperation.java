package no.geonorge.skjema.changelogfile.util;

public class WSFOperation {

	// the data to be added
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
