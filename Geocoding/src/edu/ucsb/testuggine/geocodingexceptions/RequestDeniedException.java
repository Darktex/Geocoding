package edu.ucsb.testuggine.geocodingexceptions;

public class RequestDeniedException extends GoogleGeocodingException  
{  
	private static final long serialVersionUID = 6286054962137638689L;

	public RequestDeniedException(String message)          
    {   
        super(message);         
    }      
	
	public RequestDeniedException()          
    {   
        super("Request denied!");         
    } 
}
