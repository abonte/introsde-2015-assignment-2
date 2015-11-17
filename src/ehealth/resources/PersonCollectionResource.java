package ehealth.resources;
import ehealth.model.*;
import ehealth.wrapper.PeopleWrapper;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import javax.ejb.*;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceContextType;
import javax.persistence.PersistenceUnit;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.UriInfo;

@Stateless // will work only inside a Java EE application
@LocalBean // will work only inside a Java EE application
@Path("/person")
public class PersonCollectionResource {

    // Allows to insert contextual objects into the class,
    // e.g. ServletContext, Request, Response, UriInfo
    @Context
    UriInfo uriInfo;
    @Context
    Request request;

    // will work only inside a Java EE application
    @PersistenceUnit(unitName="introsde-jpa")
    EntityManager entityManager;

    // will work only inside a Java EE application
    @PersistenceContext(unitName = "introsde-jpa",type=PersistenceContextType.TRANSACTION)
    private EntityManagerFactory entityManagerFactory;

    /**
     * Request #1: GET /person should list all the people in the database
     * Returns the list in JSON or XML format
     * 
     * Request #12: GET /person?measureType={measureType}&max={max}&min={min}
     * retrieves people whose {measureType} (e.g., weight) value is in the [{min},{max}]
     * range (if only one for the query params is provided, use only that)
     * 
     * @return list of person
     */    
    @GET
    @Produces({MediaType.TEXT_XML,  MediaType.APPLICATION_JSON ,  MediaType.APPLICATION_XML })
    public PeopleWrapper getPersonsBrowser(@QueryParam("measureType") String measureName, 
    		@QueryParam("max") Double max, @QueryParam("min") Double min) {
        System.out.println("Getting list of people...");
        
        List<Person> people = new ArrayList<Person>();
        if(measureName != null && (min != null || max != null)){
        	MeasureDefinition md = new MeasureDefinition();
        	md = MeasureDefinition.getMeasureDefinitionByName(measureName);
        	min = (min == null) ? 0 : min;
        	max = (max == null) ? 300: max;
        	people = Person.getByMeasureNameMinMax(md, min, max);
        }else{
        	people = Person.getAll();
        }
        PeopleWrapper pw = new PeopleWrapper();
        pw.setPeople(people);
        return pw;
    }
    
    /**
     * returns the number of people to get the total number of records
     * @return a string representing the number of people
     */
    @GET
    @Path("count")
    @Produces(MediaType.TEXT_PLAIN)
    public String getCount() {
        System.out.println("Getting count...");
        List<Person> people = Person.getAll();
        int count = people.size();
        return String.valueOf(count);
    }
    
    /**
     * Request #4: POST /person should create a new person and return the newly created person
     * with its assigned id (if a health profile is included, create also those measurements for the new person).
     * The method accepts a person and store it in the database.
     * The tag 'healthprofile' in the data sent by the client includes a list of 'lifestatus'.
     * The object 'lifestatus' represents a measure of the person with the value (e.g. weight).
     * The method distinguishes if a person includes a 'healthprofile'.
     * The person without a 'healthprofile' is saved in the database.
     * The person with a 'healthprofile' is saved performing two steps. In the first step the person
     * is saved in the database in order to retrieve the id. In the second step each 'lifestatus', which
     * compose the 'healthprofile', is associated with a MeasureDefinition and stored in the database.
     * If the association fails, then 'lifestatus' will be not saved. In any case, if the other 'lifestatus'
     * are correct, they will be saved.
     * Accepts data in JSON or XML formats.
     * Returns the person saved in JSON or XML.
     * @param person
     * @return the person saved in the database
     * @throws IOException
     */
    @POST
    @Produces({MediaType.APPLICATION_JSON ,  MediaType.APPLICATION_XML})
    @Consumes({MediaType.APPLICATION_JSON ,  MediaType.APPLICATION_XML})
    public Person newPerson(Person person) throws IOException {
    	System.out.println("Creating new person...");
    	// checks if person includes life statuses, in other words a 'healthprofile'
    	System.out.println(person.getLastname());
    	System.out.println(person.getLifeStatus().isEmpty());
    	if(person.getLifeStatus().isEmpty()){
    		return Person.savePerson(person);
    	}else{
    		//removes the life statuses in the persons and puts them in another variable
    		ArrayList<LifeStatus> list_lifeStatus = new ArrayList<>();
    		list_lifeStatus.addAll(person.getLifeStatus());
    		person.setLifeStatus(null);
    		
    		//saves the person in the database and retrieve the id
    		Person p = Person.savePerson(person);
    		int id_person = p.getIdPerson();
    		
    		//creates the today date
    		Calendar today = Calendar.getInstance();
    		
    		//the use of this list avoid the insertion of the same measure multiple time
    		//the list stores progressively the measure already inserted 
    		ArrayList<Integer> control = new ArrayList<>();
    		
    		//iterates on all 'lifestatus' the client wants to insert
    		for(int i=0; i<list_lifeStatus.size(); i++){
    			//associates the 'lifestatus' with the person
    			list_lifeStatus.get(i).setPerson(p);
    			HealthMeasureHistory history_element = new HealthMeasureHistory();
    			
    			//retrieves the name of the measures inserted by the client (e.g. weight)
    			String measureName = list_lifeStatus.get(i).getMeasureDefinition().getMeasureName();
    			
    			//searches the measure definition associated with the name of the measure
    			MeasureDefinition temp = new MeasureDefinition();
    			temp = MeasureDefinition.getMeasureDefinitionByName(measureName);

    			if (temp != null && !control.contains(temp.getIdMeasureDef())){
    				control.add(temp.getIdMeasureDef());
    				//associates the lifestatus with the corresponding measureDefinition
    				list_lifeStatus.get(i).setMeasureDefinition(temp); 
    				history_element.setMeasureDefinition(temp);
    				history_element.setPerson(p);
    				history_element.setValue(list_lifeStatus.get(i).getValue());
    				history_element.setTimestamp(today.getTime());
    				LifeStatus.saveLifeStatus(list_lifeStatus.get(i));  //saves lifestatus in the db
    				HealthMeasureHistory.saveHealthMeasureHistory(history_element);
    			}
    		}
    		return Person.getPersonById(id_person);
    	}
    }
    
    
    

    // Defines that the next path parameter after the base url is
    // treated as a parameter and passed to the PersonResources
    // Allows to type http://localhost:599/base_url/1
    // 1 will be treaded as parameter todo and passed to PersonResource
    @Path("{personId}")
    public PersonResource getPerson(@PathParam("personId") int id) {
        return new PersonResource(uriInfo, request, id);
    }
}