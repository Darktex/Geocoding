package edu.ucsb.testuggine.geocodingexceptions;

public class OverQueryLimitException extends GoogleGeocodingException  
{  
	private static final long serialVersionUID = 6286054962137638689L;

	public OverQueryLimitException(String message)          
    {   
        super(message);         
    }       
	public OverQueryLimitException()          
    {   
        super("Over query limit!");         
    }       
}
