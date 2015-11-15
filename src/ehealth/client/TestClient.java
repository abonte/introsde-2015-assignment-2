package ehealth.client;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.annotation.Target;
import java.net.URI;

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
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;


public class TestClient {
	public static final String uriServer = "https://arcane-beach-6023.herokuapp.com/sdelab/"; //Carlo	
	//public static final String uriServer = "https://peaceful-hamlet-5616.herokuapp.com/sdelab"; //Andrea
	
	private Client client = null;
	private WebTarget service = null;
	private ClientConfig clientConfig = null;
	
	private String first_person_id = null;
	private String last_person_id = null;
	
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
	 * Step 3.1. Send R#1 (GET BASE_URL/person)
	 * @throws IOException 
	 * @throws SAXException 
	 * @throws ParserConfigurationException 
	 */
	public void getPeople() throws ParserConfigurationException, SAXException, IOException{
		Response response = service.path("person").request().accept(MediaType.APPLICATION_XML).get(Response.class);
		if(response.getStatus() == 200){
			String xml = response.readEntity(String.class);		
			Element rootElement = getRootElement(xml);

			// Calculate how many people are in the response.
			//If more than 2, result is OK, else is ERROR (less than 3 persons)
			String result = null;
			if (rootElement.getElementsByTagName("person").getLength() > 2 ){
				result = "OK";
			} else {
				result = "ERROR";
			}

			//Save into a variable id of the first person (first_person_id) and of the last person (last_person_id)
			first_person_id = rootElement.getFirstChild().getFirstChild().getTextContent();
			last_person_id = rootElement.getLastChild().getFirstChild().getTextContent();

			responseTemplate("1", "GET", response, "/person", MediaType.APPLICATION_XML, result);
			System.out.print(prettyFormat(xml));
		}
	}
	/**
	 * Step 3.2. Send R#2 for first_person_id. If the responses for this is 200 or 202, the result is OK.
	 */
	public void getPerson() {
		Response response = getPersonByid(first_person_id);
		String xml = response.readEntity(String.class);
		String result = null;
		if (response.getStatus() == 200 || response.getStatus() == 202) {
			result = "OK";
		}else{
			result = "ERROR";
		}
		
		responseTemplate("2", "GET", response, "person/"+first_person_id, MediaType.APPLICATION_XML, result);
		System.out.print(prettyFormat(xml));
	}
	
	private Response getPersonByid(String person_id) {
		return service.path("person/"+person_id).request().accept(MediaType.APPLICATION_XML).get(Response.class);
	}
	
	/**
	 * Step 3.3. Send R#3 for first_person_id changing the firstname.
	 * If the responses has the name changed, the result is OK
	 */
	public void putPerson() {
		String firstname = "John";
		String input = "<person><firstname>"+firstname+"</firstname></person>";

		Response response = service.path("person/"+first_person_id).request().accept(MediaType.APPLICATION_XML).put(Entity.entity(input, MediaType.APPLICATION_XML));
		//String xml = response.readEntity(String.class);
		String result = null;
		// TODO check firstname
		if (false){
			result = "OK";
		} else {
			result = "ERROR";
		}
		
		responseTemplate("3", "PUT", response, "person/"+first_person_id, MediaType.APPLICATION_XML, result);
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
		
		Response response = service.path("/person").request(MediaType.APPLICATION_XML)
	               .post(Entity.entity(input, MediaType.APPLICATION_XML),Response.class);
		String xml = response.readEntity(String.class);		
		Element rootElement = getRootElement(xml);
		String result = null;
		if (rootElement.getElementsByTagName("idPerson") != null && response.getStatus() >= 200 
				&& response.getStatus() <= 202){
			result = "OK";
		} else {
			result = "ERROR";
		}
		responseTemplate("4", "POST", response, "person/", MediaType.APPLICATION_XML, result);
		System.out.print(prettyFormat(xml));
		return rootElement.getFirstChild().getTextContent();
	}
	
	/**
	 * Step 3.5. Send R#5 for the person you have just created. Then send R#1 with the id of that person.
	 * If the answer is 404, your result must be OK.
	 */
	public void deletePerson(String person_id) {
		Response response = service.path("/person/"+person_id).request(MediaType.APPLICATION_XML)
	               .delete(Response.class);
		String result = null;
		Response responseGet = getPersonByid(person_id);
		System.out.println(responseGet.getStatus());
		if (responseGet.getStatus() == 404) {
			result = "OK";
		}else{
			result = "ERROR";
		}
		responseTemplate("5", "DELETE", response, "person/"+person_id, MediaType.APPLICATION_XML, result);

	}
	
	
	public static void main(String[] args) {

		/*ClientConfig clientConfig = new ClientConfig();
		Client client = ClientBuilder.newClient(clientConfig);
		WebTarget service = client.target(getBaseURI(uriServer));*/
		System.out.println("Server URL : " + uriServer);
		try {
			TestClient jerseyClient = new TestClient();
			jerseyClient.getPeople(); //Step 3.1
			jerseyClient.getPerson(); //Step 3.2		
			jerseyClient.putPerson(); //Step 3.3
			String person_id = jerseyClient.postPerson(); //Step 4.4
			jerseyClient.deletePerson(person_id); //Step 4.5
			
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private static Element getRootElement(String xml) throws ParserConfigurationException, SAXException, IOException{
		DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
		domFactory.setNamespaceAware(true);
		DocumentBuilder builder = domFactory.newDocumentBuilder();
		Document doc = builder.parse(new InputSource(new StringReader(xml)));
		return doc.getDocumentElement();
	}

	//https://stackoverflow.com/questions/139076/how-to-pretty-print-xml-from-java
	private static String prettyFormat(String input, int indent) {
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

	private static String prettyFormat(String input) {
		return prettyFormat(input, 4);
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
}