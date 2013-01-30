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

public class TAGeo {
	JSONParser parser;
	MySQLConnection db;
	boolean verbose;

	public TAGeo(boolean verbose) throws SQLException, ParseException {
		parser = new JSONParser();
		db = new MySQLConnection();
		this.verbose = verbose;

		String selectionQuery = "SELECT id, "
				+ "addressNum, addressStreet, addressCity, addressRegion, addressZip"
				+ " FROM  YelpRestaurant;";
		PreparedStatement checkStatement = db.con
				.prepareStatement(selectionQuery);
		ResultSet res = checkStatement.executeQuery();

		while (res.next()) {
			String id = res.getString("id");
			String rawData = getGoogleJSON(res);
			String[] coords = processJSON(rawData);

			String insertionQuery = "INSERT INTO `YelpCoords` (`restaurant_id`, `latitude`, "
					+ "`longitude`) " + "VALUES (?, ?, ?);";
			PreparedStatement prep = db.con.prepareStatement(insertionQuery);
			if (!alreadyThere(id)) {
				prep.setString(1, id);
				prep.setString(2, coords[0]);
				prep.setString(3, coords[1]);
				prep.execute();
			}
			prep.close();
		}
	}

	private boolean alreadyThere(String id) throws SQLException {
		String alreadyExistsCheckQuery = "SELECT * FROM  `YelpCoords` WHERE  `id` =  ?";
		PreparedStatement checkStatement = db.con
				.prepareStatement(alreadyExistsCheckQuery);
		checkStatement.setString(1, id); // the ID of this restaurant
		ResultSet alreadyExistsRes = checkStatement.executeQuery();
		if (!alreadyExistsRes.first())
			return false;
		return true;
	}

	private String[] processJSON(String rawData) throws ParseException {
		String[] result = new String[2];

		Object allDataObj = parser.parse(rawData);
		JSONObject jsonObject = (JSONObject) allDataObj;
		// The results are so structured:
		// Everything is in a 2-slots array, where
		// [1] contains metadata and [0] contains data

		JSONArray resultsArray = (JSONArray) jsonObject.get("results");
		Object metaObj = resultsArray.get(1);
		Object dataObj = resultsArray.get(0);

		JSONObject metaData = (JSONObject) metaObj;

		String status = (String) metaData.get("status");
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

		JSONObject data = (JSONObject) dataObj;

		// The data we are interested in (geocode) lies in
		// "geometry" -> "location". This one has 2 elements (not an array tho)
		// "lat" and "lng".

		JSONObject geometry = (JSONObject) data.get("geometry");
		JSONObject location = (JSONObject) geometry.get("location");

		String latitude = (String) location.get("lat");
		String longitude = (String) location.get("lng");

		result[0] = latitude;
		result[1] = longitude;

		return result;
	}

	private String getGoogleJSON(ResultSet res) throws SQLException {
		String addressNum = res.getString("addressNum").trim()
				.replaceAll("/s", "+");
		String addressStreet = res.getString("addressStreet").trim()
				.replaceAll("/s", "+");
		String addressCity = res.getString("addressCity").trim()
				.replaceAll("/s", "+");
		String addressRegion = res.getString("addressRegion").trim()
				.replaceAll("/s", "+");
		String addressZip = res.getString("addressZip").trim()
				.replaceAll("/s", "+");

		String prefix = "http://maps.googleapis.com/maps/api/geocode/json?address=";
		String postfix = "&sensor=false";

		String geoString = addressNum + "+" + addressStreet + ",+"
				+ addressCity + ",+" + addressRegion + "+" + addressZip;

		Document doc = getHTMLFromPage(geoString);
		String result = doc.select("pre").first().ownText();
		return result;
	}

	private static Document getHTMLFromPage(String url) {
		System.setProperty("http.proxyHost", "deltoro");
		System.setProperty("http.proxyPort", "3128");

		Document doc = null;
		boolean success = false;

		while (!success) {
			try {
				String ua = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_6_8) AppleWebKit/534.30 (KHTML, like Gecko) Chrome/12.0.742.122 Safari/534.30";
				doc = Jsoup.connect(url).timeout(60 * 1000).userAgent(ua).get();
				success = true;
				break;
			} catch (IOException e) {
				System.out.println("Failed to connect to " + url
						+ "\n\tRetrying...");
			}
		}
		return doc;
	}

}
