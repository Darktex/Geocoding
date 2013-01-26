package edu.ucsb.testuggine;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.json.simple.parser.JSONParser;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class YelpGeo {
	JSONParser parser;
	MySQLConnection db;
	
	
	public YelpGeo() throws SQLException {
		String selectionQuery = "SELECT id, " +
				"addressNum, addressStreet, addressCity, addressRegion, addressZip" +
				" FROM  YelpRestaurant;";
		PreparedStatement checkStatement = db.con
				.prepareStatement(selectionQuery);
		ResultSet res = checkStatement.executeQuery();
		
		while (res.next()) {
			String id = res.getString("id");
			String rawData = getGoogleJSON(res);
			String[] coords = processJSON(rawData);
			
		}
		
		String insertionQuery = "INSERT INTO `YelpCoords` (`restaurant_id`, `latitude`, " +
				"`longitude`) " +
				"VALUES (?, ?, ?);";
	}
	
	private String[] processJSON(String rawData) {
		// TODO Auto-generated method stub
		return null;
	}

	private String getGoogleJSON(ResultSet res) throws SQLException {
		String addressNum = res.getString("addressNum").trim().replaceAll("/s", "+");
		String addressStreet = res.getString("addressStreet").trim().replaceAll("/s", "+");
		String addressCity = res.getString("addressCity").trim().replaceAll("/s", "+");
		String addressRegion = res.getString("addressRegion").trim().replaceAll("/s", "+");
		String addressZip = res.getString("addressZip").trim().replaceAll("/s", "+");
		
		String prefix = "http://maps.googleapis.com/maps/api/geocode/json?address=";
		String postfix = "&sensor=false";
		
		String geoString = addressNum + "+" + addressStreet + ",+" + addressCity + ",+" + 
				addressRegion + "+" + addressZip;
		
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
				System.out.println("Failed to connect to " + url + "\n\tRetrying...");
			}
		}
		return doc;
	}

}
