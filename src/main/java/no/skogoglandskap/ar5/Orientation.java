package no.skogoglandskap.ar5;

public enum Orientation {
	
	OrientationClockWise("+"),
	OrientationAntiClockWise("-");
	
	private String orit;

	Orientation(String orit) {
		this.setOrit(orit);
		
	}

	public String getOrit() {
		return orit;
	}

	public void setOrit(String orit) {
		this.orit = orit;
	}
	
	
	 
	
	

}
