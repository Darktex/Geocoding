package edu.ucsb.testuggine.geocodingexceptions;

public class InvalidRequestException extends GoogleGeocodingException  
{  
	private static final long serialVersionUID = 6286054962137638689L;

	public InvalidRequestException(String message)          
    {   
        super(message);         
    }       
}
