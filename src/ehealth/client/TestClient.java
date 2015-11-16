package ehealth.client;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.annotation.Target;
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
import org.glassfish.jersey.client.ClientResponse;
import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.fasterxml.jackson.annotation.JsonRootName;


public class TestClient {
	//public static final String uriServer = "https://peaceful-hamlet-5616.herokuapp.com/sdelab"; //Andrea
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
		Response response = service.path("person").request().accept(mediaType).get(Response.class);
		if(response.getStatus() == 200){
			output = response.readEntity(String.class);
			if(mediaType == MediaType.APPLICATION_XML){
				Element rootElement = getRootElement(output);
				if (rootElement.getElementsByTagName("person").getLength() > 2 )
					result = "OK";
				first_person_id = rootElement.getFirstChild().getFirstChild().getTextContent();
				last_person_id = rootElement.getLastChild().getFirstChild().getTextContent();
			}else if (mediaType == MediaType.APPLICATION_JSON) {
				JSONArray json = new JSONArray(output);
				if (json.length() > 1 )
					result = "OK";
				// TODO finish for json
			}
			output = prettyFormat(output, mediaType);
		}
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
	 */
	public void putPerson() {
		String firstname = "John";
		String input = "<person><firstname>"+firstname+"</firstname></person>";

		Response response = service.path("person/"+first_person_id).request().accept(mediaType).put(Entity.entity(input, MediaType.APPLICATION_XML));
		//String xml = response.readEntity(String.class);
		String result = null;
		// TODO check firstname
		if (false){
			result = "OK";
		} else {
			result = "ERROR";
		}
		
		responseTemplate("3", "PUT", response, "/person/"+first_person_id, mediaType, result);
		//System.out.print(prettyFormat(xml));
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
		String input = "<person>"
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
		
		Response response = service.path("/person").request(mediaType)
	               .post(Entity.entity(input, mediaType),Response.class);
		String xml = response.readEntity(String.class);		
		Element rootElement = getRootElement(xml);
		String result = null;
		if (rootElement.getElementsByTagName("idPerson") != null && response.getStatus() >= 200 
				&& response.getStatus() <= 202){
			result = "OK";
		} else {
			result = "ERROR";
		}
		responseTemplate("4", "POST", response, "/person/", mediaType, result);
		System.out.print(prettyFormat(xml,mediaType));
		return rootElement.getFirstChild().getTextContent();
	}
	
	/**
	 * Step 3.5. Send R#5 for the person you have just created. Then send R#1 with the id of that person.
	 * If the answer is 404, your result must be OK.
	 */
	public void deletePerson(String person_id) {
		Response response = service.path("/person/"+person_id).request(mediaType)
	               .delete(Response.class);
		String result = null;
		Response responseGet = getPersonByid(person_id);
		
		if (responseGet.getStatus() == 404) {
			result = "OK";
		}else{
			result = "ERROR";
		}
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
		Response response = service.path("measureTypes").request().accept(mediaType).get(Response.class);
		String result = null;
		String xml = response.readEntity(String.class);
		Element rootElement = getRootElement(xml);
		NodeList types = rootElement.getChildNodes(); 
		for(int i = 0; i< types.getLength(); i++){
			measure_types.add(types.item(i).getTextContent());
		}
		if(types.getLength() > 2){
			result = "OK";
		}else{
			result = "ERROR";
		}	
		responseTemplate("5", "GET", response, "/measureTypes", mediaType, result);
		System.out.print(prettyFormat(xml, mediaType));
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
		ArrayList<String> xml_output = new ArrayList<String>();	
		for(String id : new String[] {first_person_id, last_person_id}){
			String xml_person = "";
			for(String temp : measure_types){
				//System.out.println("/person/"+id+"/"+temp);
				String xml = service.path("/person/"+id+"/"+temp).request(mediaType)
						.get(Response.class).readEntity(String.class);
				Element rootElement = getRootElement(xml);
				if(rootElement.getChildNodes().getLength() > 0){
					xml_person = xml_person + prettyFormat(xml, mediaType);
					measure_id = rootElement.getFirstChild().getFirstChild().getTextContent();
					measureType = temp;
					measure_id_person = id;
					result = "OK";
				}
			}
			xml_output.add(xml_person);
		}

		responseTemplate("6", "GET", Response.ok().build(), "/person/{"+first_person_id+","+last_person_id+"}/{"
				+measure_types.toString()+"}", mediaType, result);
		System.out.println("First person, id = "+first_person_id);
		System.out.println(xml_output.get(0));
		System.out.println("Second person, id = "+last_person_id);
		System.out.println(xml_output.get(1));
	}
	
	/**
	 * Step 3.8. Send R#7 (GET BASE_URL/person/{id}/{measureType}/{mid}) for the stored measure_id and measureType.
	 * If the response is 200, result is OK, else is ERROR.
	 */
	public void	getMeasureHistoryById() {
		String result = null;
		Response response = service.path("person/"+measure_id_person+"/"+measureType+"/"+measure_id).request()
				.accept(mediaType).get(Response.class);
		if (response.getStatus() == 200) {
			result = "OK";
		}else{
			result = "ERROR";
		}
		String xml = response.readEntity(String.class);
		responseTemplate("7", "GET", response, "/person/"+measure_id_person+"/"+measureType+"/"+measure_id, mediaType, result);
		System.out.println(xml);
	}
	
	/*
	 * Step 3.9. Choose a measureType from measure_types and send the request R#6 (GET BASE_URL/person/{first_person_id}/{measureType})
	 * and save count value (e.g. 5 measurements).
	 * Then send R#8 (POST BASE_URL/person/{first_person_id}/{measureTypes}) with the measurement specified below.
	 * Follow up with another R#6 as the first to check the new count value.
	 * If it is 1 measure more - print OK, else print ERROR.
	 * Remember, first with JSON and then with XML as content-types
	 */
	public void postMeasureValue() throws ParserConfigurationException, SAXException, IOException{
		String result = null;
		int count_before = countMeasureHistoryElement();
		
		String input = "<measure>"
						+ "<value>72</value>"
						+ "<created>2011-12-09</created>"
					+ "</measure>";
	
		Response response = service.path("/person/"+first_person_id+"/"+measureType).request(mediaType)
	               .post(Entity.entity(input, mediaType),Response.class);
			
		int count_after = countMeasureHistoryElement();
		if(count_after > count_before){
			result = "OK";
		}else{
			result = "ERROR";
		}

		responseTemplate("8", "POST", response, "/person/"+first_person_id+"/"+measureType, mediaType, result);
		if(response.getStatus() == 200){
			String xml = response.readEntity(String.class);	
			System.out.println(prettyFormat(xml, mediaType));
		}
	}
	
	private int countMeasureHistoryElement() throws ParserConfigurationException, SAXException, IOException {		
		String xml = service.path("/person/"+first_person_id+"/"+measureType).request(mediaType)
				.get(Response.class).readEntity(String.class);
		Element rootElement = getRootElement(xml);
		return rootElement.getChildNodes().getLength();
	}
	
	/**
	 * Step 3.10. Send R#10 using the {mid} or the measure created in the previous step and updating the value at will.
	 * Follow up with at R#6 to check that the value was updated. If it was, result is OK, else is ERROR.
	 * @throws IOException 
	 * @throws SAXException 
	 * @throws ParserConfigurationException 
	 */
	public void putHealthHistory() throws ParserConfigurationException, SAXException, IOException {
		String result = null;
		String input = "<measure>"
				+ "<value>90</value>"
				+ "<created>2011-12-09</created>"
			+ "</measure>";
		String value_before = getHealthHistoryValue();
		Response response = service.path("/person/"+first_person_id+"/"+measureType+"/"+measure_id).request(mediaType)
	               .put(Entity.entity(input, mediaType),Response.class);
		String value_after = getHealthHistoryValue();
		
		if(!value_after.equals(value_before)){
			result = "OK";
		}else{
			result = "ERROR";
		}
		responseTemplate("10", "PUT", response, "/person/"+first_person_id+"/"+measureType, MediaType.APPLICATION_XML, result);
		if(response.getStatus() == 201){
			String xml = response.readEntity(String.class);	
			System.out.println(xml);
		}
	}
	
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
		String result = null;
		Response response = service.path("/person/"+first_person_id+"/"+measureType)
				.queryParam("before", "2015-11-20").queryParam("after","2011-01-01")
				.request(mediaType).get(Response.class);
		String xml = response.readEntity(String.class);
		Element rootElement = getRootElement(xml);
		
		if(response.getStatus() == 200 && rootElement.getChildNodes().getLength() > 0){
			result = "OK";
		}else{
			result = "ERROR";
		}
		responseTemplate("11", "GET", response, "/person/"+first_person_id+"/"+measureType+"?before=2015-11-20&after=2011-01-01", mediaType, result);
		System.out.println(prettyFormat(xml,mediaType));
	}
	
	/**
	 * Step 3.12. Send R#12 using the same parameters as the preivious steps.
	 * If status is 200 and there is at least one person in the body, result is OK, else is ERROR
	 * GET /person?measureType={measureType}&max={max}&min={min}
	 * @throws IOException 
	 * @throws SAXException 
	 * @throws ParserConfigurationException 
	 */
	public void get() throws ParserConfigurationException, SAXException, IOException {
		String result = null;
		Response response = service.path("/person/")
				.queryParam("measureType", measureType).queryParam("min","0").queryParam("max", "100")
				.request(mediaType).get(Response.class);
		String xml = response.readEntity(String.class);
		Element rootElement = getRootElement(xml);
		
		if(response.getStatus() == 200 && rootElement.getChildNodes().getLength() > 0){
			result = "OK";
		}else{
			result = "ERROR";
		}
		responseTemplate("12", "GET", response, "/person?measureType="+measureType+"&max=0&min=100", mediaType, result);
		System.out.println(prettyFormat(xml, mediaType));
	}

	private static Element getRootElement(String xml) throws ParserConfigurationException, SAXException, IOException{
		DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
		domFactory.setNamespaceAware(true);
		DocumentBuilder builder = domFactory.newDocumentBuilder();
		Document doc = builder.parse(new InputSource(new StringReader(xml)));
		return doc.getDocumentElement();
	}

	//https://stackoverflow.com/questions/139076/how-to-pretty-print-xml-from-java
	private static String prettyFormatXml(String input, int indent) {
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
			throw new RuntimeException(e); // simple exception handling, please review it
		}
	}
	
	private static JSONArray getRootJson(String jsonString){
		JSONArray json = new JSONArray(jsonString);
		return json;
	}
	
	private static String prettyFormatJson(String jsonString) {
		JSONObject json = new JSONObject(jsonString); // Convert text to object
		//JSONArray json = new JSONArray(jsonString);
		return json.toString(4); // Print it with specified indentation	
	}
	
	private static String prettyFormat(String input, String media) {
		if(media == MediaType.APPLICATION_XML)
			return prettyFormatXml(input, 4);
		else if (media == MediaType.APPLICATION_JSON) 
			return prettyFormatJson(input);
		else
			return null;
	}

	private static URI getBaseURI(String uriServer) {
		return UriBuilder.fromUri(uriServer).build();
	}

	private static void responseTemplate(String req, String method, Response response, String path, String type, String result){
		/*Request #[NUMBER]: [HTTP METHOD] [URL] Accept: [TYPE] Content-type: [TYPE] 
				=> Result: [RESPONSE STATUS = OK, ERROR]
				=> HTTP Status: [HTTP STATUS CODE = 200, 404, 500 ...]
				[BODY]*/ 
		type = type.toUpperCase();
		method = method.toUpperCase();
		System.out.println("=============================================");
		System.out.println("Request #"+req+": "+method+" "+path+" Accept: "+type+" Content-type: "+type);
		System.out.println("     => Result: "+ result);
		System.out.println("     => HTTP Status: "+ response.getStatus());
		System.out.println(" ");
	}
	
	public static void main(String[] args) {
		if (args.length < 2)
			System.out.println("Error: insert {myServer, partnerServer} and {xml, json}");
			else{
				if(args[0].equals("partnerServer"))	
					uriServer = "https://arcane-beach-6023.herokuapp.com/sdelab/"; //Partner
				else		
					uriServer = "https://peaceful-hamlet-5616.herokuapp.com/sdelab"; //My server

				if(args[1].equals("JSON"))
					mediaType = MediaType.APPLICATION_JSON;
				else
					mediaType = MediaType.APPLICATION_XML;

				System.out.println("Server URL : " + uriServer);
				System.out.println("MediaType  : " + mediaType);


				try {
					TestClient jerseyClient = new TestClient();
					jerseyClient.getPeople(); //Step 3.1
					jerseyClient.getPerson(); //Step 3.2		
			/*jerseyClient.putPerson(); //Step 3.3
			String person_id = jerseyClient.postPerson(); //Step 3.4
			jerseyClient.deletePerson(person_id); //Step 3.5
			jerseyClient.getMeasureTypes(); //Step 3.6
			jerseyClient.getPersonHistoryByMeasureType(); // Step 3.7
			jerseyClient.getMeasureHistoryById(); // Step 3.8
			jerseyClient.postMeasureValue(); //Step 3.9
			jerseyClient.putHealthHistory(); //Step 3.10
			jerseyClient.getPersonHistoryByDate(); //Step 3.11
			jerseyClient.get(); //Step 3.12*/
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

		}
}