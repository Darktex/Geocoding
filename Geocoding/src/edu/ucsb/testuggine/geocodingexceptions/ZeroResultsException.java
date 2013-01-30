package edu.ucsb.testuggine.geocodingexceptions;

public class ZeroResultsException extends GoogleGeocodingException  
{  
	private static final long serialVersionUID = 6286054962137638689L;

	public ZeroResultsException(String message)          
    {   
        super(message);         
    }       
}
