package edu.ucsb.testuggine.geocodingexceptions;
public class GoogleGeocodingException extends Exception  
{  
	private static final long serialVersionUID = 6286054962137638689L;

	public GoogleGeocodingException(String message)          
    {   
        super(message);         
    }       
}