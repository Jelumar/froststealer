package eu.jlmb.froststealer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

import javax.swing.JTextField;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 * Stealer Class of the Frost Stealer
 * @author Jean Baumgarten
 */
public class Stealer {
	
	private String url;
	private String path;
	private JSONParser parser;
	private PrintWriter writer;
	private int max;
	private JTextField info;
	private JTextField error;

	/**
	 * Default Constructor
	 * @param url of the server to steal from
	 * @param max observations to take
	 * @param info to inform about things
	 * @param error to inform about errors
	 */
	public Stealer(String url, int max, JTextField info, JTextField error) {
		this.url = url;
		this.path = System.getProperty("user.home") + File.separator;
		this.path += "Desktop/froststealer.csv";
		this.max = max;
		this.info = info;
		this.error = error;
		this.parser = new JSONParser();
		getSensorUpData();
	}
	
	private void sendInfo(String info) {
		if (info != null && !info.isEmpty()) {
			synchronized (Window.class) {
				this.info.setText(info);
				//System.out.println(info);
			}
		}
	}
	private void sendError(String info) {
		if (info != null && !info.isEmpty()) {
			synchronized (Window.class) {
				this.error.setText(info);
				//System.out.println(info);
			}
		}
	}
	
	private void getSensorUpData() {
		try {
			this.writer = new PrintWriter(path, "UTF-8");
			
			sendInfo("Starting Getting Data.");
			int amount;
			int skip;
			
			String[] types = {
					"ObservedProperties",
					"Sensors",
					"Locations",
					"FeaturesOfInterest",
					"Things",
					"Datastreams",
					"Observations"
					};
			
			for (String type : types) {
				sendInfo("Waiting for " + type + " Informations from Server.");
				amount = getAmount(type);
				skip = 0;
				while (skip < amount && skip < this.max) {
					if (type.equals("ObservedProperties")) {
						obsPropsGetter(skip, amount);
					} else if (type.equals("Sensors")) {
						sensorGetter(skip, amount);
					} else if (type.equals("Locations")) {
						locationGetter(skip, amount);
					} else if (type.equals("FeaturesOfInterest")) {
						featureGetter(skip, amount);
					} else if (type.equals("Things")) {
						thingGetter(skip, amount);
					} else if (type.equals("Datastreams")) {
						datastreamGetter(skip, amount);
					} else if (type.equals("Observations")) {
						observationGetter(skip, amount);
					}
					skip += 1000;
				}
				sendInfo(type + " Done.");
			}
			
			writer.close();
		} catch (ParseException e) {
			sendError(e.getLocalizedMessage());
		} catch (FileNotFoundException e) {
			sendError(e.getLocalizedMessage());
		} catch (UnsupportedEncodingException e) {
			sendError(e.getLocalizedMessage());
		}
	}
	
	private void obsPropsGetter(int skip, int tot)
			throws ParseException {
		int number = skip + 1;
		JSONArray arrObj = getValues(skip, "ObservedProperties");
		for (Object obj : arrObj) {
			JSONObject value = (JSONObject) obj;
			String line = "observedProperty|" + value.get("@iot.id") + "|";
			line += value.get("name") + "|";
			line += value.get("description") + "|";
			line += value.get("definition");
			this.writer.println(line);
			sendInfo("Working on Observed Properties : " + number++ + " / " + tot);
		}
	}
	
	private void sensorGetter(int skip, int tot)
			throws ParseException {
		int number = skip + 1;
		JSONArray arrObj = getValues(skip, "Sensors");
		for (Object obj : arrObj) {
			JSONObject value = (JSONObject) obj;
			String line = "sensor|" + value.get("@iot.id") + "|";
			line += value.get("name") + "|";
			line += value.get("description") + "|";
			line += value.get("encodingType") + "|";
			line += value.get("metadata");
			this.writer.println(line);
			sendInfo("Working on Sensors : " + number++ + " / " + tot);
		}
	}
	
	private void locationGetter(int skip, int tot)
			throws ParseException {
		int number = skip + 1;
		JSONArray arrObj = getValues(skip, "Locations");
		for (Object obj : arrObj) {
			JSONObject value = (JSONObject) obj;
			String line = "location|" + value.get("@iot.id") + "|";
			line += value.get("name") + "|";
			line += value.get("description") + "|";
			line += value.get("encodingType") + "|";
			line += ((JSONObject) value.get("location")).toJSONString();
			this.writer.println(line);
			sendInfo("Working on Locations : " + number++ + " / " + tot);
		}
	}
	
	private void featureGetter(int skip, int tot)
			throws ParseException {
		int number = skip + 1;
		JSONArray arrObj = getValues(skip, "FeaturesOfInterest");
		for (Object obj : arrObj) {
			JSONObject value = (JSONObject) obj;
			String line = "featureOfInterest|" + value.get("@iot.id") + "|";
			line += value.get("name") + "|";
			line += value.get("description") + "|";
			line += value.get("encodingType") + "|";
			line += ((JSONObject) value.get("feature")).toJSONString();
			this.writer.println(line);
			sendInfo("Working on Features Of Interest : " + number++ + " / " + tot);
		}
	}
	
	private void thingGetter(int skip, int tot)
			throws ParseException {
		int number = skip + 1;
		JSONArray arrObj = getValues(skip, "Things");
		for (Object obj : arrObj) {
			JSONObject value = (JSONObject) obj;
			String line = "thing|" + value.get("@iot.id") + "|";
			line += value.get("name") + "|";
			line += value.get("description") + "|";
			line += ((JSONObject) value.get("properties")).toJSONString() + "|";
			
			String id = "" + value.get("@iot.id");
			String locs = getDataForLink(this.url + "Things(" + id + ")/Locations");
			Object locats = this.parser.parse(locs);
			JSONObject locObj = (JSONObject) locats;
			JSONArray locArr = (JSONArray) locObj.get("value");
			for (Object locObjP : locArr) {
				JSONObject val = (JSONObject) locObjP;
				line += val.get("@iot.id") + ";";
			}
			
			this.writer.println(line);
			sendInfo("Working on Things : " + number++ + " / " + tot);
		}
	}
	
	private void datastreamGetter(int skip, int tot)
			throws ParseException {
		int number = skip + 1;
		JSONArray arrObj = getValues(skip, "DataStreams");
		for (Object obj : arrObj) {
			JSONObject value = (JSONObject) obj;
			String line = "dataStream|" + value.get("@iot.id") + "|";
			line += value.get("name") + "|";
			line += value.get("description") + "|";
			line += value.get("observationType") + "|";
			line += ((JSONObject) value.get("unitOfMeasurement")).toJSONString() + "|";
			
			String id = "" + value.get("@iot.id");
			String thing = getDataForLink(this.url + "DataStreams(" + id + ")/Thing");
			String obsProp = getDataForLink(this.url + "DataStreams(" + id + ")/ObservedProperty");
			String sensor = getDataForLink(this.url + "DataStreams(" + id + ")/Sensor");
			Object oT = this.parser.parse(thing);
			Object oO = this.parser.parse(obsProp);
			Object oS = this.parser.parse(sensor);
			JSONObject jT = (JSONObject) oT;
			JSONObject jO = (JSONObject) oO;
			JSONObject jS = (JSONObject) oS;
			line += jT.get("@iot.id") + "|";
			line += jO.get("@iot.id") + "|";
			line += jS.get("@iot.id") + "|";
			
			String opt = "";
			Object optional = value.get("observedArea");
			if (optional != null) {
				opt = "" + optional;
			}
			line += opt + "|";
			opt = "";
			optional = value.get("phenomenonTime");
			if (optional != null) {
				opt = "" + optional;
			}
			line += opt + "|";
			opt = "";
			optional = value.get("resultTime");
			if (optional != null) {
				opt = "" + optional;
			}
			line += opt + "|";
			
			this.writer.println(line);
			sendInfo("Working on DataStreams : " + number++ + " / " + tot);
		}
	}
	
	private void observationGetter(int skip, int tot)
			throws ParseException {
		int number = skip + 1;
		JSONArray arrObj = getValues(skip, "Observations");
		for (Object obj : arrObj) {
			JSONObject value = (JSONObject) obj;
			String line = "observation|" + value.get("@iot.id") + "|";
			line += value.get("phenomenonTime") + "|";
			line += value.get("result") + "|";
			line += value.get("resultTime") + "|";
			
			String id = "" + value.get("@iot.id");
			String ds = getDataForLink(this.url + "Observations(" + id + ")/Datastream");
			Object oD = this.parser.parse(ds);
			JSONObject jD = (JSONObject) oD;
			line += jD.get("@iot.id") + "|";
			
			String foi = getDataForLink(this.url + "Observations(" + id + ")/FeatureOfInterest");
			Object ofoi = this.parser.parse(foi);
			JSONObject jfoi = (JSONObject) ofoi;
			line += jfoi.get("@iot.id") + "|";
			
			String opt = "";
			Object optional = value.get("resultQuality");
			if (optional != null) {
				opt = "" + optional;
			}
			line += opt + "|";
			opt = "";
			optional = value.get("validTime");
			if (optional != null) {
				opt = "" + optional;
			}
			line += opt + "|";
			opt = "";
			optional = value.get("parameters");
			if (optional != null) {
				JSONObject op = (JSONObject) optional;
				opt = "" + op.toJSONString();
			}
			line += opt + "|";
			
			this.writer.println(line);
			sendInfo("Working on Observations : " + number++ + " / " + tot);
		}
	}
	
	private JSONArray getValues(int skip, String type)
			throws ParseException {
		String currentLink = this.url + type + "?$top=1000&$skip=" + skip;
		sendInfo("Waiting for " + type + " from Server.");
		String data = getDataForLink(currentLink);
		Object o = this.parser.parse(data);
		JSONObject dataObj = (JSONObject) o;
		return (JSONArray) dataObj.get("value");
	}
	
	private int getAmount(String type)
			throws ParseException {
		String currentLink = this.url + type + "?$top=1";
		String data = getDataForLink(currentLink);
		Object o = this.parser.parse(data);
		JSONObject dataObj = (JSONObject) o;
		return Integer.parseInt("" + dataObj.get("@iot.count"));
	}
	
	private String getDataForLink(String link) {
		try {
			URL url = new URL(link);
			HttpURLConnection http = (HttpURLConnection) url.openConnection();
			http.setRequestMethod("GET");
			http.setDoInput(true);
			http.setDoOutput(true);
			http.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
			http.setRequestProperty("Content-Encoding", "charset=UTF-8");
			http.setRequestProperty("Accept", "application/json");
			http.connect();
			String allInput = "";
			try {
			    BufferedReader in = new BufferedReader(new InputStreamReader(http.getInputStream()));
			    String inputLine;

			    while ((inputLine = in.readLine()) != null) {
			    	allInput += inputLine;
			    }
			    in.close();
			} catch (IOException e) {
				sendError(e.getLocalizedMessage());
			}
	    	return allInput;
		} catch (MalformedURLException e) {
			sendError(e.getLocalizedMessage());
		} catch (ProtocolException e) {
			sendError(e.getLocalizedMessage());
		} catch (IOException e) {
			sendError(e.getLocalizedMessage());
		}
    	return "";
	}

}
