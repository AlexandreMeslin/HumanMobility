package br.com.meslin.humanMobility.main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import lac.cnclib.net.NodeConnection;
import lac.cnclib.net.NodeConnectionListener;
import lac.cnclib.net.mrudp.MrUdpNodeConnection;
import lac.cnclib.sddl.message.ApplicationMessage;
import lac.cnclib.sddl.message.ClientLibProtocol.PayloadSerialization;
import lac.cnclib.sddl.message.Message;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.json.JSONObject;
import org.openstreetmap.gui.jmapviewer.Coordinate;

import br.com.meslin.humanMobility.map.GeographicMap;
import br.com.meslin.humanMobility.model.Person;
import br.com.meslin.humanMobility.model.Position;

public class HumanMobility implements NodeConnectionListener {
	private static final double EARTH_RADIUS = 6371E3;
	private static String datasetDirectory = null;

	// Parameters
	private static double timeAcceleration = 1;
	private static double latitude;
	private static double longitude;
	private static String contextNetIPAddress;
	private static int contextNetPortNumber;
	private static double scale;
	private MrUdpNodeConnection	connection;
	
	private GeographicMap map;
	private List<Person> persons;
	
	/** is connected to ContextNet? */
	private volatile boolean isConnected;


	/**
	 * Constructor
	 */
	public HumanMobility() {
		// HTTP agent to request map tiles
		String httpAgent = System.getProperty("http.agent");
		if (httpAgent == null) {
		    httpAgent = "(" + System.getProperty("os.name") + " / " + System.getProperty("os.version") + " / " + System.getProperty("os.arch") + ")";
		}
		System.setProperty("http.agent", "HumanMobility/1.0 " + httpAgent);
		
		this.isConnected = false;
		// Send information to ContextNet as a M-Hubbbb, if there was a ContextNet address configured at command line
		if(contextNetIPAddress != null) {
			InetSocketAddress address = new InetSocketAddress(contextNetIPAddress, contextNetPortNumber);
			try {
				connection = new MrUdpNodeConnection();
				connection.addNodeConnectionListener(this);
				connection.connect(address);
			} catch (IOException e) {
				e.printStackTrace();
			}			
		}
	}

	public static void main(String[] args) {
		/*
		 * get command line options
		 */
		Options options = new Options();
		Option option;
		
		option = new Option("a", "address", true, "ContextNet Gateway IP address");
		option.setRequired(false);
		options.addOption(option);

		option = new Option("d", "directory", true, "Dataset source directory");
		option.setRequired(false);
		options.addOption(option);

		option = new Option("l", "latitude", true, "Base latitute");
		option.setRequired(false);
		options.addOption(option);

		option = new Option("o", "longitude", true, "Base longitude");
		option.setRequired(false);
		options.addOption(option);
		
		option = new Option("p", "port", true, "ContextNet Gateway TCP port number");
		option.setRequired(false);
		options.addOption(option);
		
		option = new Option("s", "scale", true, "Scale multiplier");
		option.setRequired(false);
		options.addOption(option);

		option = new Option("v", "velocity", true, "Time acceleration");
		option.setRequired(false);
		options.addOption(option);
		
		CommandLineParser parser = new DefaultParser();
		HelpFormatter formatter = new HelpFormatter();
		CommandLine cmd;
		try {
			cmd = parser.parse(options, args);
		} catch (ParseException e) {
			System.err.println("Date = " +  new Date());
			formatter.printHelp("HumanMobility", options);
			e.printStackTrace();
			return;
		}

		// ContextNet IP address
		if((contextNetIPAddress = cmd.getOptionValue("address")) == null) {
			contextNetIPAddress = null;
		}
		try {
			contextNetPortNumber = Integer.parseInt(cmd.getOptionValue("port"));
		} catch(Exception e) {
			contextNetPortNumber = 5500;
		}
		// Dataset source directory
		try {
			HumanMobility.datasetDirectory = cmd.getOptionValue("directory") + "/";
		} catch(Exception e) {
			HumanMobility.datasetDirectory = "./";
		}
		System.err.println("dataset directory = " + HumanMobility.datasetDirectory);
		// Time acceleration
		try {
			HumanMobility.timeAcceleration = Double.parseDouble(cmd.getOptionValue("velocity"));
		} catch(Exception e) {
			HumanMobility.timeAcceleration = 1.;
		}
		// Scale multiplier
		try {
			HumanMobility.scale = Double.parseDouble(cmd.getOptionValue("scale"));
		} catch(Exception e) {
			HumanMobility.scale = 1.;
		}
		// Base latitude
		try {
			HumanMobility.latitude = Double.parseDouble(cmd.getOptionValue("latitude"));
		} catch(Exception e) {
			HumanMobility.latitude = -22.813249;
		}
		// Base longitude
		try {
			HumanMobility.longitude = Double.parseDouble(cmd.getOptionValue("longitude"));
		} catch(Exception e) {
			HumanMobility.longitude = -43.198811;
		}
		
		HumanMobility humanMobility = new HumanMobility();
		humanMobility.doAll();
	}
	
	/**
	 * 
	 */
	private void doAll() {
		int time;
		Boolean ended = false;
		readDataset();
		
		// draw the map
		map = new GeographicMap();
		map.setVisible(true);
		
		for(time=0; !ended; time++) {
			map.removeAll();
			ended = true;
			for(Person person : persons) {
				Position position = person.getPosition(time);
				if(position != null) {
					ended = false;	// there is still a position, so do not end yet
					double lat, lon;
					lat = HumanMobility.latitude + position.getDeltaY()/HumanMobility.EARTH_RADIUS*360 * HumanMobility.scale;
					lon = HumanMobility.longitude + position.getDeltaX()/HumanMobility.EARTH_RADIUS*360 * HumanMobility.scale;
					Coordinate coordinate = new Coordinate(lat, lon);
					map.addPerson(person.getUsername(), coordinate);
					
					// Send information to ContextNet as a M-Hub, if there was a ContextNet address configured at command line
					if(contextNetIPAddress != null) {
						while(!this.isConnected) {
							System.err.println("We are NOT connected yet!!!");
						}
						/*
						 * M-Hub message format is a JSON string coded as byte[] like this:
						 * 	{
						 * 		"uuid":"9509494b-b270-4cd7-a5a2-08cc6bb998d1",
						 * 		"source":"1-34947689C447",
						 * 		"action":"found",
						 * 		"signal":-67,
						 * 		"latitude":-22.938382,
						 * 		"longitude":-43.192847,
						 * 		"tag":"SensorData",
						 * 		"timestamp":1551904249
						 * 
						 * 		"username": "fake"
						 * }
						 */
						JSONObject jsonContent = new JSONObject();
						jsonContent.put("uuid", person.getUuid());
						jsonContent.put("source", "1-34947689C447");
						jsonContent.put("action", "found");
						jsonContent.put("signal", -67);
						jsonContent.put("latitude", lat);
						jsonContent.put("longitude", lon);
						jsonContent.put("tag", "SensorData");
						jsonContent.put("timestamp", (new Date()).getTime()/1000);
						jsonContent.put("username", person.getUsername());
						Message message = new ApplicationMessage();
						String content = jsonContent.toString();
						
						message.setPayloadType(PayloadSerialization.JSON);
						message.setContentObject(content);
						message.setTagList(new ArrayList<String>());
						message.setSenderID(person.getUuid());
						try {
							connection.sendMessage(message);
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
			}
			try {
				Thread.sleep((long) (30 * 1000/HumanMobility.timeAcceleration));
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Read the dataset located in <b>HumanMobility.datasetDirectory</b>
	 */
	private void readDataset() {
		BufferedReader br = null;

		persons = new ArrayList<Person>();

		File[] files = new File(datasetDirectory).listFiles(file -> file.isFile());
		System.err.println("Reading from " + datasetDirectory);
		for(File file : files) {
			Person person = new Person();
			person.setUsername(file.getName());
			try
			{
				br = new BufferedReader(new FileReader(file));
				String line;
				while((line = br.readLine()) != null) {
					String[] splited = line.trim().split("\\s+");
					Position position =  new Position(Double.parseDouble(splited[0]), 
							                          Double.parseDouble(splited[1]),
							                          Double.parseDouble(splited[2]));
					person.addPosition(position);
				}
			}
			catch (IOException e)
			{
				System.err.println("Date = " + new Date());
				e.printStackTrace();
			}
			finally {
				if(br != null)
				{
					try {
						br.close();
					}
					catch (IOException e) {
						System.err.println("Date = " + new Date());
						e.printStackTrace();
					}
				}
			}
			persons.add(person);
			System.err.println("A person added");
		}
	}

	@Override
	public void connected(NodeConnection arg0) {
		isConnected = true;
		System.out.println("Now we are connected to ContextNet at " + contextNetIPAddress + ":" + contextNetPortNumber);
		
/*	      ApplicationMessage message = new ApplicationMessage();
	      String serializableContent = "{"
	      		+ "\"latitude\":40.94627502260164,"
	      		+ "\"action\":\"found\","
	      		+ "\"source\":\"1-34947689C447\","
	      		+ "\"tag\":\"SensorData\","
	      		+ "\"uuid\":\"83bdc457-2491-47b0-b6ef-d959de11d6be\","
	      		+ "\"signal\":-67,"
	      		+ "\"longitude\":-73.91973960686269,"
	      		+ "\"timestamp\":1551909879129"
	      		+ "}\n";
	      message.setContentObject(serializableContent);
	 
	      try {
	          connection.sendMessage(message);
	      } catch (IOException e) {
	          e.printStackTrace();
	      }
*/	}

	@Override
	public void disconnected(NodeConnection arg0) {
		isConnected = false;
		System.err.println("Now we are DISconnected from ContextNet at " + contextNetIPAddress + ":" + contextNetPortNumber);
	}

	@Override
	public void internalException(NodeConnection arg0, Exception arg1) {
		// Auto-generated method stub
		
	}

	@Override
	public void newMessageReceived(NodeConnection remoteCon, Message message) {
	    System.out.println("Receiver Node!");
	    System.out.println("Message: " + message.getContentObject());
	}

	@Override
	public void reconnected(NodeConnection arg0, SocketAddress arg1, boolean arg2, boolean arg3) {
		isConnected = true;
		System.out.println("Now we are REconnected to ContextNet at " + contextNetIPAddress + ":" + contextNetPortNumber);
	}

	@Override
	public void unsentMessages(NodeConnection arg0, List<Message> arg1) {
		// Auto-generated method stub
		
	}
}
