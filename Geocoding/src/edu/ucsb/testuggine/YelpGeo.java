package edu.ucsb.testuggine;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import edu.princeton.cs.introcs.StdOut;

public class YelpGeo {
	JSONParser parser;
	MySQLConnection db;
	boolean verbose;

	public YelpGeo(boolean verbose) throws SQLException, ParseException {
		parser = new JSONParser();
		db = new MySQLConnection();
		this.verbose = verbose;

		String selectionQuery = "SELECT id, "
				+ "addressNum, addressStreet, addressCity, addressRegion, addressZip"
				+ " FROM YelpRestaurant;";
		PreparedStatement checkStatement = db.con
				.prepareStatement(selectionQuery);
		ResultSet res = checkStatement.executeQuery();

		while (res.next()) {
			String id = res.getString("id");
			
			String addressNum = processField(res, "addressNum");
			String addressStreet = processField(res, "addressStreet");
			String addressCity = processField(res, "addressCity");
			String addressRegion = processField(res, "addressRegion");
			String addressZip = processField(res, "addressZip");
			
			if (addressStreet.isEmpty() || addressCity.isEmpty() || addressRegion.isEmpty()) continue;
			
			String rawData = getGoogleJSON(addressNum, addressStreet, addressCity, addressRegion, addressZip);
			String[] coords = processJSON(rawData);

			String insertionQuery = "INSERT INTO `YelpCoords` (`restaurant_id`, `latitude`, "
					+ "`longitude`, `isApprox`) " + "VALUES (?, ?, ?, ?);";
			PreparedStatement prep = db.con
					.prepareStatement(insertionQuery);
			if (!alreadyThere(id)) {	
				prep.setString(1, id);
				prep.setString(2, coords[0]);
				prep.setString(3, coords[1]);
				boolean approx;
				if (coords[2].equals("true")) approx = true;
				else if (coords[2].equals("false")) approx = false;
				else throw new IllegalStateException("'Approx' didn't return a legal value");
				prep.setBoolean(4, approx);
				if (verbose)
					StdOut.println("----\n" + prep + "\n--------");
				prep.execute();
			}
			else {
				if (verbose)
					StdOut.println("Restaurant " + id
							+ " already in the DB. Skipping...");
			}
			prep.close();
		}
	}


	private boolean alreadyThere(String id) throws SQLException {
		String alreadyExistsCheckQuery = "SELECT * FROM  `YelpCoords` WHERE  `restaurant_id` =  ?";
		PreparedStatement checkStatement = db.con
				.prepareStatement(alreadyExistsCheckQuery);
		checkStatement.setString(1, id); // the ID of this restaurant
		ResultSet alreadyExistsRes = checkStatement.executeQuery();
		if (!alreadyExistsRes.first() ) return false;
		return true;
	}

	private String[] processJSON(String rawData) throws ParseException {
		String[] retVal = new String[3]; // [0] contains lat, [1] contains lng, [2] contains "true" if partial match, 
		// "false" otherwise

		Object allDataObj = parser.parse(rawData);
		JSONObject jsonObject = (JSONObject) allDataObj;

		String status = (String) jsonObject.get("status");
		// Status Codes

		// The "status" field within the Geocoding response object contains the
		// status of the request, and may contain debugging information to help
		// you track down why Geocoding is not working. The "status" field may
		// contain the following values:

		// "OK" indicates that no errors occurred; the address was successfully
		// parsed and at least one geocode was returned.
		// "ZERO_RESULTS" indicates that the geocode was successful but returned
		// no results. This may occur if the geocode was passed a non-existent
		// address or a latlng in a remote location.
		// "OVER_QUERY_LIMIT" indicates that you are over your quota.
		// "REQUEST_DENIED" indicates that your request was denied, generally
		// because of lack of a sensor parameter.
		// "INVALID_REQUEST" generally indicates that the query (address or
		// latlng) is missing.

		JSONArray results = (JSONArray) jsonObject.get("results");

		// The data we are interested in (geocode) lies in
		// "geometry" -> "location". This one has 2 elements (not an array tho)
		// "lat" and "lng".

		JSONObject resultsObj = (JSONObject) results.get(0);
		JSONObject geometry = (JSONObject) resultsObj.get("geometry"); // .get("geometry");
		JSONObject location = (JSONObject) geometry.get("location");

		String latitude = String.valueOf((Double) location.get("lat")).trim();
		String longitude = String.valueOf((Double) location.get("lng")).trim();
		String isPartialMatch = String.valueOf(((Boolean) resultsObj.get("partial_match"))).trim();
		if (isPartialMatch.equals("null")) isPartialMatch = "false";

		retVal[0] = latitude;
		retVal[1] = longitude;
		retVal[2] = isPartialMatch;

		return retVal;
	}

	private String getGoogleJSON(String addressNum, String addressStreet,
			String addressCity, String addressRegion, String addressZip) {
		
		String prefix = "http://maps.googleapis.com/maps/api/geocode/json?address=";
		String postfix = "&sensor=false";

		String geoString = addressNum + "+" + addressStreet + ",+"
				+ addressCity + ",+" + addressRegion + "+" + addressZip;

		String result = makeRequest(prefix + geoString + postfix);
		return result;
	}
	
	private String processField(ResultSet res, String fieldName) throws SQLException {
		String result = res.getString(fieldName);
		if (result == null || result.isEmpty()) return "";
		else return result.trim().replaceAll(" ", "+");
	}

	private static String makeRequest(String url) {
		System.setProperty("http.proxyHost", "deltoro");
		System.setProperty("http.proxyPort", "3128");

		String doc = null;
		boolean success = false;

		while (!success) {
			try {				
				String ua = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_7_5) AppleWebKit/537.17 (KHTML, like Gecko) Chrome/24.0.1312.56 Safari/537.17";
				doc = Jsoup.connect(url).ignoreContentType(true).userAgent(ua).execute().body();
				success = true;
				break;
			} catch (IOException e) {
				// System.out.println("Failed to connect to " + url
						// + "\n\tRetrying...");
				e.printStackTrace();
			}
		}
		return doc;
	}

}
