package net.ducksmanager.whattheduck;



public class PublicationFullName {
	private String countryName;
	private String publicationName;

	public PublicationFullName(String countryName, String publicationName) {
		super();
		this.countryName = countryName;
		this.publicationName = publicationName;
	}
	
	public PublicationFullName() {
		
	}
	
	public String getCountryName() {
		return countryName;
	}

	public void setCountryName(String countryName) {
		this.countryName = countryName;
	}

	public String getPublicationName() {
		return publicationName;
	}

	public void setPublicationName(String publicationName) {
		this.publicationName = publicationName;
	}

	public String toString() {
		return this.countryName+" ("+this.publicationName+")";
	}
	
}
