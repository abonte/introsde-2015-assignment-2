package client;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URI;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import org.glassfish.jersey.client.ClientConfig;
import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;


public class TestClient {

	public static String uriServer = null;
	public static String mediaType = null;

	private Client client = null;
	private WebTarget service = null;
	private ClientConfig clientConfig = null;

	private String first_person_id = null;
	private String last_person_id = null;
	private ArrayList<String> measure_types=new ArrayList<>();
	private String measure_id = null;
	private String measureType = null;

	private String measure_id_person;

	public TestClient(){
		clientConfig = new ClientConfig();
		client = ClientBuilder.newClient(clientConfig);
		service = client.target(getBaseURI(uriServer));
	}

	public void reloadUri(){
		service = null;
		service = client.target(getBaseURI(uriServer));
	}

	/**
	 * Step 3.1. Send R#1 (GET BASE_URL/person). Calculate how many people are in the response.
	 * If more than 2, result is OK, else is ERROR (less than 3 persons).
	 * Save into a variable id of the first person (first_person_id) and of the last person (last_person_id)
	 * @throws IOException 
	 * @throws SAXException 
	 * @throws ParserConfigurationException 
	 */
	public void getPeople() throws ParserConfigurationException, SAXException, IOException{
		String result = "ERROR";
		String output = null;
		//get request
		Response response = service.path("person").request().accept(mediaType).get(Response.class);
		//checks whether the request end well
		if(response.getStatus() == 200){
			//retrieves the response
			output = response.readEntity(String.class);
			if(mediaType == MediaType.APPLICATION_XML){
				//converts string in a XML document
				Element rootElement = getRootElement(output);
				//checks the number of person in the database
				if (rootElement.getElementsByTagName("person").getLength() > 2 )
					result = "OK";
				first_person_id = rootElement.getFirstChild().getFirstChild().getTextContent();
				last_person_id = rootElement.getLastChild().getFirstChild().getTextContent();
			}else if (mediaType == MediaType.APPLICATION_JSON) {
				//generates a JSON object from the output
				JSONObject jsonObj = new JSONObject(output);
				JSONArray array = jsonObj.getJSONArray("people");
				if (array.length() > 2 )
					result = "OK";
				first_person_id = String.valueOf(array.getJSONObject(0).getInt("idPerson"));
				last_person_id = String.valueOf(array.getJSONObject(array.length()-1).getInt("idPerson"));
			}
			output = prettyFormat(output, mediaType);
		}
		//prints the response in the console
		responseTemplate("1", "GET", response, "/person", mediaType, result);
		System.out.println(output);
	}

	/**
	 * Step 3.2. Send R#2 for first_person_id. If the responses for this is 200 or 202, the result is OK.
	 */
	public void getPerson() {
		String result = "ERROR";
		String output = null;
		Response response = getPersonByid(first_person_id);
		if (response.getStatus() == 200 || response.getStatus() == 202) {
			output = prettyFormat(response.readEntity(String.class), mediaType);		
			result = "OK";
		}
		responseTemplate("2", "GET", response, "/person/"+first_person_id, mediaType, result);
		System.out.print(output);
	}	

	private Response getPersonByid(String person_id) {
		return service.path("person/"+person_id).request().accept(mediaType).get(Response.class);
	}

	/**
	 * Step 3.3. Send R#3 for first_person_id changing the firstname.
	 * If the responses has the name changed, the result is OK
	 * @throws IOException 
	 * @throws SAXException 
	 * @throws ParserConfigurationException 
	 */
	public void putPerson() throws ParserConfigurationException, SAXException, IOException {
		String firstname = "Andrea";
		String result = "ERROR";
		String input = null;
		if(mediaType == MediaType.APPLICATION_XML){
			//creates a new person in XML
			input = "<person><firstname>"+firstname+"</firstname></person>";
		}else{
			//creates a new person in JSON
			JSONObject person = new JSONObject();
			person.put("firstname", firstname);
			input = person.toString();
		}
		//put the person
		Response response = service.path("person/"+first_person_id).request()
				.accept(mediaType).put(Entity.entity(input, mediaType));		
		
		//get the person
		String output = service.path("person/"+first_person_id).request()
				.accept(MediaType.APPLICATION_XML).get(Response.class).readEntity(String.class);
		Element rootElement = getRootElement(output);
		//checks if the name is changed
		if (rootElement.getElementsByTagName("firstname").item(0).getTextContent().equals(firstname))
			result = "OK";

		responseTemplate("3", "PUT", response, "/person/"+first_person_id, mediaType, result);
	}

	/**
	 * Step 3.4. Send R#4 to create the following person. Store the id of the new person.
	 * If the answer is 201 (200 or 202 are also applicable) with a person in the body who has an ID,
	 * the result is OK.
	 * @return id of the new person
	 * @throws IOException 
	 * @throws SAXException 
	 * @throws ParserConfigurationException 
	 */
	public String postPerson() throws ParserConfigurationException, SAXException, IOException {
		String input = null;
		String result = "ERROR";
		String newid = null;
		if(mediaType == MediaType.APPLICATION_XML){
			//creates a person in XML with a healthprofile
			input = "<person>"
					+ "<firstname>Chuck</firstname>"
					+ "<lastname>Norris</lastname>"
					+ "<birthdate>1945-01-01</birthdate>"
					+ "<healthprofile>"
					+ "    <measureType>"
					+ "        <measure>weight</measure>"
					+ "        <value>72.3</value>"
					+ "   </measureType>"
					+ "   <measureType>"
					+ "        <measure>height</measure> "
					+ "        <value>1.86</value>"
					+ "   </measureType>"
					+ "</healthprofile>"
					+ "</person>";
		}else{
			//creates a person in JSON
			//the post of a new person with healthprofile in json doesn't work
			//the post of a new person without healthprofile works correctly
			JSONObject person = new JSONObject();
			person.put("firstname", "Chuck");
			person.put("lastname", "Norris");
			person.put("birthdate", "1945-01-01");
			/*
			JSONObject mt1 = new JSONObject();
			mt1.put("measure", "weight");
			mt1.put("value", "72.3");

			JSONObject mt2 = new JSONObject();
			mt2.put("measure", "weight");
			mt2.put("value", "72.3");

			JSONArray list = new JSONArray();
			list.put(mt1);
			list.put(mt2);

			person.put("healthprofile", list);
			 */
			input = person.toString();
		}
		
		//post the new person
		Response response = service.path("/person").request(mediaType)
				.post(Entity.entity(input, mediaType),Response.class);
		//read response
		String output = response.readEntity(String.class);
		if(response.getStatus() >= 200 && response.getStatus() <= 202){
			if(mediaType == MediaType.APPLICATION_XML){
				//the response is in XML
				Element rootElement = getRootElement(output);
				//the person has an id, so it is saved in the database
				if (rootElement.getElementsByTagName("idPerson") != null ){
					result = "OK";
					//saves the id of the new person
					newid = rootElement.getElementsByTagName("idPerson").item(0).getTextContent();
				}
			}else{
				//the response is in JSON
				JSONObject jsonObj = new JSONObject(output);
				if (jsonObj.get("idPerson") != null ){
					result = "OK";
					newid = String.valueOf(jsonObj.getInt("idPerson"));
				}
			}
		}

		responseTemplate("4", "POST", response, "/person", mediaType, result);
		System.out.print(prettyFormat(output,mediaType));
		return newid; 
	}

	/**
	 * Step 3.5. Send R#5 for the person you have just created. Then send R#1 with the id of that person.
	 * If the answer is 404, your result must be OK.
	 */
	public void deletePerson(String person_id) {
		//delete person with id=person_id
		Response response = service.path("/person/"+person_id).request(mediaType).delete(Response.class);
		String result = "ERROR";

		Response responseGet = getPersonByid(person_id);
		//if status is 404, then the person with id=person_id is not present
		if (responseGet.getStatus() == 404) 
			result = "OK";

		responseTemplate("5", "DELETE", response, "/person/"+person_id, mediaType, result);
	}

	/**
	 * Step 3.6. Follow now with the R#9 (GET BASE_URL/measureTypes).
	 * If response contains more than 2 measureTypes
	 * result is OK, else is ERROR (less than 3 measureTypes).
	 * Save all measureTypes into array (measure_types
	 * @throws IOException 
	 * @throws SAXException 
	 * @throws ParserConfigurationException )
	 */
	public void getMeasureTypes() throws ParserConfigurationException, SAXException, IOException {
		String result = "ERROR";
		Response response = service.path("measureTypes").request().accept(mediaType).get(Response.class);		
		String output = response.readEntity(String.class);
		if(response.getStatus() == 200){
			if(mediaType == MediaType.APPLICATION_XML){
				Element rootElement = getRootElement(output);
				NodeList types = rootElement.getChildNodes();
				//stores each measureType into array
				for(int i = 0; i< types.getLength(); i++){
					measure_types.add(types.item(i).getTextContent());
				}
				if(types.getLength() > 2)
					result = "OK";
			}else{
				JSONObject jsonObj = new JSONObject(output);
				JSONArray jsonTypes = jsonObj.getJSONArray("measureTypes");
				for(int i = 0; i< jsonTypes.length(); i++){
					measure_types.add(jsonTypes.getString(i));
				}
				if(jsonTypes.length() > 2)
					result = "OK";
			}
			output = prettyFormat(output,mediaType); 
		}
		responseTemplate("9", "GET", response, "/measureTypes", mediaType, result);
		System.out.print(output);
	}

	/**
	 * Step 3.7. Send R#6 (GET BASE_URL/person/{id}/{measureType}) for the first person you obtained at the beginning
	 * and the last person, and for each measure types from measure_types.
	 * If no response has at least one measure
	 * result is ERROR (no data at all) else result is OK. Store one measure_id and one measureType.
	 * @throws IOException 
	 * @throws SAXException 
	 * @throws ParserConfigurationException 
	 */
	public void getPersonHistoryByMeasureType() throws ParserConfigurationException, SAXException, IOException {
		String result = "ERROR";		
		ArrayList<String> output_people = new ArrayList<String>();
		//iterates on person
		for(String id : new String[] {first_person_id, last_person_id}){
			String output_person = "";
			//iterates on measureTypes
			for(String temp : measure_types){
				String output = service.path("/person/"+id+"/"+temp).request(mediaType)
						.get(Response.class).readEntity(String.class);
				if(mediaType == MediaType.APPLICATION_XML){
					Element rootElement = getRootElement(output);
					if(rootElement.getChildNodes().getLength() > 0){
						//stores the output to print in the console
						output_person = output_person + " \n "+temp.toUpperCase()+ " \n " + prettyFormat(output, mediaType);
						//stores information about the current HealthMeasureHisotory element
						measure_id = rootElement.getFirstChild().getFirstChild().getTextContent();
						measureType = temp;
						measure_id_person = id;
						result = "OK";
					}
				}else{
					JSONArray jsonHistory = new JSONArray(output);
					if(jsonHistory.length() > 0){
						output_person = output_person + " \n "+temp.toUpperCase()+ " \n " + jsonHistory.toString(4);
						measure_id = String.valueOf(jsonHistory.getJSONObject(0).getInt("mid"));
						measureType = temp;
						measure_id_person = id;
						result = "OK";
					}
				}
			}
			output_people.add(output_person);
		}

		responseTemplate("6", "GET", Response.ok().build(), "/person/{"+first_person_id+","+last_person_id+"}/{"
				+measure_types.toString()+"}", mediaType, result);
		System.out.println("First person, id = "+first_person_id);
		System.out.println(output_people.get(0));
		System.out.println("Second person, id = "+last_person_id);
		System.out.println(output_people.get(1));
	}

	/**
	 * Step 3.8. Send R#7 (GET BASE_URL/person/{id}/{measureType}/{mid}) for the stored measure_id and measureType.
	 * If the response is 200, result is OK, else is ERROR.
	 */
	public void	getMeasureHistoryById() {
		String result = "ERROR";
		Response response = service.path("person/"+measure_id_person+"/"+measureType+"/"+measure_id).request()
				.accept(mediaType).get(Response.class);
		if (response.getStatus() == 200)
			result = "OK";
		String output = response.readEntity(String.class);
		responseTemplate("7", "GET", response, "/person/"+measure_id_person+"/"+measureType+"/"+measure_id, mediaType, result);
		System.out.println(output);
	}

	/**
	 * Step 3.9. Choose a measureType from measure_types and send the request R#6 (GET BASE_URL/person/{first_person_id}/{measureType})
	 * and save count value (e.g. 5 measurements).
	 * Then send R#8 (POST BASE_URL/person/{first_person_id}/{measureTypes}) with the measurement specified below.
	 * Follow up with another R#6 as the first to check the new count value.
	 * If it is 1 measure more - print OK, else print ERROR.
	 */
	public void postMeasureValue() throws ParserConfigurationException, SAXException, IOException{
		String result = "ERROR";
		String input = null;

		int count_before = countMeasureHistoryElement();

		if(mediaType == MediaType.APPLICATION_XML){
			input = "<measure>"
					+ "<value>72</value>"
					+ "<created>2011-12-09</created>"
					+ "</measure>";
		}else{
			JSONObject measureObj = new JSONObject();
			measureObj.put("value", 72);
			measureObj.put("created", "2011-12-09");
			input = measureObj.toString();
		}
		Response response = service.path("/person/"+first_person_id+"/"+measureType).request(mediaType)
				.post(Entity.entity(input, mediaType),Response.class);

		int count_after = countMeasureHistoryElement();
		//checks the number of measurements before and after the post
		if(count_after > count_before)
			result = "OK";

		responseTemplate("8", "POST", response, "/person/"+first_person_id+"/"+measureType, mediaType, result);
		if(response.getStatus() == 200){
			String output = response.readEntity(String.class);	
			System.out.println(prettyFormat(output, mediaType));
		}
	}
	
	/**
	 * Returns the number of elements in the history for a specific person and measureType
	 * @return number
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 */
	private int countMeasureHistoryElement() throws ParserConfigurationException, SAXException, IOException {		
		String output = service.path("/person/"+first_person_id+"/"+measureType).request(mediaType)
				.get(Response.class).readEntity(String.class);
		if(mediaType == MediaType.APPLICATION_XML){
			Element rootElement = getRootElement(output);
			return rootElement.getChildNodes().getLength();
		}else{
			JSONArray jsonArr = new JSONArray(output);
			return jsonArr.length();
		}
	}

	/**
	 * Step 3.10. Send R#10 using the {mid} or the measure created in the previous step and updating the value at will.
	 * Follow up with at R#6 to check that the value was updated. If it was, result is OK, else is ERROR.
	 * @throws IOException 
	 * @throws SAXException 
	 * @throws ParserConfigurationException 
	 */
	public void putHealthHistory() throws ParserConfigurationException, SAXException, IOException {
		String result = "ERROR";
		String input = null;
		if(mediaType == MediaType.APPLICATION_XML){
			input = "<measure>"
					+ "<value>90</value>"
					+ "<created>2011-12-09</created>"
					+ "</measure>";
		}else{
			JSONObject measureObj = new JSONObject();
			measureObj.put("value", 90);
			measureObj.put("created", "2011-12-09");
			input = measureObj.toString();
		}
		String value_before = getHealthHistoryValue();
		Response response = service.path("/person/"+first_person_id+"/"+measureType+"/"+measure_id).request(mediaType)
				.put(Entity.entity(input, mediaType),Response.class);
		if(response.getStatus() == 201){
			String value_after = getHealthHistoryValue();
			//checks if the value is changed
			if(!value_after.equals(value_before))
				result = "OK";
		}
		responseTemplate("10", "PUT", response, "/person/"+first_person_id+"/"+measureType+"/"+measure_id, mediaType, result);
	}

	/**
	 * Returns the value of a HealthmeasureHistory element with a specific id
	 */
	private String getHealthHistoryValue() {
		return service.path("person/"+measure_id_person+"/"+measureType+"/"+measure_id).request()
				.accept(mediaType).get(Response.class).readEntity(String.class);
	}

	/**
	 * Step 3.11. Send R#11 for a measureType, before and after dates given by your fellow student (who implemented the server).
	 * If status is 200 and there is at least one measure in the body, result is OK, else is ERROR
	 * @throws IOException 
	 * @throws SAXException 
	 * @throws ParserConfigurationException 
	 * 
	 */
	public void getPersonHistoryByDate() throws ParserConfigurationException, SAXException, IOException {
		String result = "ERROR";
		String before = "2015-11-20";
		String after = "2011-01-01";
		Response response = service.path("/person/"+first_person_id+"/"+measureType)
				.queryParam("before", before).queryParam("after", after)
				.request(mediaType).get(Response.class);
		String output = response.readEntity(String.class);
		if(response.getStatus() == 200){
			int length = 0;
			if(mediaType == MediaType.APPLICATION_XML){
				Element rootElement = getRootElement(output);
				length = rootElement.getChildNodes().getLength();
				output = prettyFormat(output, mediaType);
			}else{
				JSONArray jsonArr = new JSONArray(output);
				length = jsonArr.length();
				output = jsonArr.toString(4);
			}
			if(length > 0)
				result = "OK";		
		}
		responseTemplate("11", "GET", response, "/person/"+first_person_id+"/"+measureType+"?before=2015-11-20&after=2011-01-01", mediaType, result);
		System.out.println(output);
	}

	/**
	 * Step 3.12. Send R#12 using the same parameters as the preivious steps.
	 * If status is 200 and there is at least one person in the body, result is OK, else is ERROR
	 * GET /person?measureType={measureType}&max={max}&min={min}
	 * @throws IOException 
	 * @throws SAXException 
	 * @throws ParserConfigurationException 
	 */
	public void getPersonHistoryByValue() throws ParserConfigurationException, SAXException, IOException {
		String result = "ERROR";
		String min = "70";
		String max = "80";
		Response response = service.path("/person")
				.queryParam("measureType", measureType).queryParam("min",min).queryParam("max", max)
				.request(mediaType).get(Response.class);
		String output = response.readEntity(String.class);
		if(response.getStatus() == 200 ){
			int length = 0;
			if(mediaType == MediaType.APPLICATION_XML){
				Element rootElement = getRootElement(output);
				length = rootElement.getChildNodes().getLength();
				output = prettyFormat(output, mediaType);
			}else{
				JSONObject jsonObj = new JSONObject(output);
				JSONArray array = jsonObj.getJSONArray("people");
				length = array.length();
				output = array.toString(4);
			}
			if(length > 0)
				result = "OK";
		}
		responseTemplate("12", "GET", response, "/person?measureType="+measureType+"&max="+max+"&min="+min, mediaType, result);
		System.out.println(output);
	}
	
	/**
	 * Generates a document containing xml
	 * @param xml
	 * @return the root element of the document
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 */
	private Element getRootElement(String xml) throws ParserConfigurationException, SAXException, IOException{
		DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
		domFactory.setNamespaceAware(true);
		DocumentBuilder builder = domFactory.newDocumentBuilder();
		Document doc = builder.parse(new InputSource(new StringReader(xml)));
		return doc.getDocumentElement();
	}

	//https://stackoverflow.com/questions/139076/how-to-pretty-print-xml-from-java
	/**
	 * Pretty Format for Xml
	 * @param input
	 * @param indent
	 * @return a string containing the indent xml
	 */
	private String prettyFormatXml(String input, int indent) {
		try {
			Source xmlInput = new StreamSource(new StringReader(input));
			StringWriter stringWriter = new StringWriter();
			StreamResult xmlOutput = new StreamResult(stringWriter);
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			transformerFactory.setAttribute("indent-number", indent);
			Transformer transformer = transformerFactory.newTransformer(); 
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
			transformer.transform(xmlInput, xmlOutput);
			return xmlOutput.getWriter().toString();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Pretty Format for JSON
	 * @param jsonString
	 * @param indent
	 * @return a string containing the indent json
	 */
	private String prettyFormatJson(String jsonString, int indent) {
		JSONObject json = new JSONObject(jsonString); // Convert text to object
		return json.toString(indent); // Print it with specified indentation	
	}

	private String prettyFormat(String input, String media) {
		if(media == MediaType.APPLICATION_XML)
			return prettyFormatXml(input, 4);
		else if (media == MediaType.APPLICATION_JSON) 
			return prettyFormatJson(input, 4);
		else
			return null;
	}

	private static URI getBaseURI(String uriServer) {
		return UriBuilder.fromUri(uriServer).build();
	}

	/**
	 * Template for the printing to the console
	 * Request #[NUMBER]: [HTTP METHOD] [URL] Accept: [TYPE] Content-type: [TYPE] 
	 *			=> Result: [RESPONSE STATUS = OK, ERROR]
	 *			=> HTTP Status: [HTTP STATUS CODE = 200, 404, 500 ...]
	 *			[BODY]
	 * @param req
	 * @param method
	 * @param response
	 * @param path
	 * @param type
	 * @param result
	 */
	private void responseTemplate(String req, String method, Response response,
			String path, String type, String result){ 
		type = type.toUpperCase();
		method = method.toUpperCase();
		System.out.println("===========================================================================");
		System.out.println("Request #"+req+": "+method+" "+path+" Accept: "+type+" Content-type: "+type);
		System.out.println("     => Result: "+ result);
		System.out.println("     => HTTP Status: "+ response.getStatus());
		System.out.println(" ");
	}

	public static void main(String[] args) {
		if (args.length < 2)
			System.out.println("Error: insert {myServer, partnerServer} and {xml, json}");
		else{
			//sets the server
			if(args[0].equals("partnerServer"))	
				uriServer = "https://introsde-server-carlo.herokuapp.com/sdelab/"; //Partner
			else		
				uriServer = "https://peaceful-hamlet-5616.herokuapp.com/sdelab"; //My server

			//sets the media type (XML or JSON)
			if(args[1].equals("JSON"))
				mediaType = MediaType.APPLICATION_JSON;
			else
				mediaType = MediaType.APPLICATION_XML;

			System.out.println("Server URL : " + uriServer);
			System.out.println("MediaType  : " + mediaType);

			try {
				//starts the client
				TestClient jerseyClient = new TestClient();
				jerseyClient.getPeople(); //Step 3.1
				jerseyClient.getPerson(); //Step 3.2		
				jerseyClient.putPerson(); //Step 3.3
				//post person returns the id of the new person
				String person_id = jerseyClient.postPerson(); //Step 3.4
				jerseyClient.deletePerson(person_id); //Step 3.5
				jerseyClient.getMeasureTypes(); //Step 3.6
				jerseyClient.getPersonHistoryByMeasureType(); // Step 3.7
				jerseyClient.getMeasureHistoryById(); // Step 3.8
				jerseyClient.postMeasureValue(); //Step 3.9
				jerseyClient.putHealthHistory(); //Step 3.10
				jerseyClient.getPersonHistoryByDate(); //Step 3.11
				jerseyClient.getPersonHistoryByValue(); //Step 3.12
				
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}
}