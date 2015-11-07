package ehealth.resources;
import ehealth.model.*;

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

    // Request #2
    // Return the list of people to the user in the browser
    @GET
    @Produces({MediaType.TEXT_XML,  MediaType.APPLICATION_JSON ,  MediaType.APPLICATION_XML })
    public List<Person> getPersonsBrowser() {
        System.out.println("Getting list of people...");
        List<Person> people = Person.getAll();
        return people;
    }

    // retuns the number of people
    // to get the total number of records
    @GET
    @Path("count")
    @Produces(MediaType.TEXT_PLAIN)
    public String getCount() {
        System.out.println("Getting count...");
        List<Person> people = Person.getAll();
        int count = people.size();
        return String.valueOf(count);
    }

    @POST
    @Produces({MediaType.APPLICATION_JSON ,  MediaType.APPLICATION_XML})
    @Consumes({MediaType.APPLICATION_JSON ,  MediaType.APPLICATION_XML})
    public Person newPerson(Person person) throws IOException {
    	System.out.println("Creating new person...123");
        System.out.println("Valore person in @POST prima di Person.savePerson: " + person);
        //System.out.println(person);
        System.out.println("Valore helthprofile " + person.getLifeStatus().get(0).getValue());
        System.out.println("######### " + person.getLifeStatus().get(0).getMeasureDefinition().getMeasureName());

        //se non c'è il lifestatus salva solo la persona
        if(person.getLifeStatus().isEmpty()){
        	return Person.savePerson(person);
        }else{
        	ArrayList<LifeStatus> list_lifeStatus = new ArrayList<>();
        	list_lifeStatus.addAll(person.getLifeStatus());
        	person.setLifeStatus(null);
        	Person p = Person.savePerson(person);
        	System.out.println("£££££££££££"+person.getBirthdate());
        	//int id_person =p.getIdPerson();
        	//p.setLifeStatus(list);
        	//System.out.println("Contenuto: " + p.getLifeStatus().get(0).getValue());
        	List<MeasureDefinition> md = MeasureDefinition.getAll();
        	
        	
            //history_element.setTimestamp(new Timestamp(date.getTime()));
        	
        	java.util.Date dt = new java.util.Date();

        	java.text.SimpleDateFormat sdf = 
        	     new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        	String currentTime = sdf.format(dt);
        	
        	Calendar today = Calendar.getInstance();
        	today.set(Calendar.HOUR_OF_DAY, 0);
        	System.out.println("£££££££££££"+ today.getTime());
        	for(int i=0; i<list_lifeStatus.size(); i++){
        		boolean check = false;
        		list_lifeStatus.get(i).setPerson(p);
        		HealthMeasureHistory history_element = new HealthMeasureHistory();
        			for (MeasureDefinition temp : md) {
        				if (temp.getMeasureName().equals(list_lifeStatus.get(i).getMeasureDefinition().getMeasureName())){
        					list_lifeStatus.get(i).setMeasureDefinition(temp);
        					history_element.setMeasureDefinition(temp);
        		        	
        					check=true;
        				}
        			}
        		history_element.setPerson(p);
				
				history_element.setValue(list_lifeStatus.get(i).getValue());
				history_element.setTimestamp(today.getTime());
        		if (check){
        		 //list.get(i).setMeasureDefinition(MeasureDefinition.getAll().get(0));
        		 LifeStatus.saveLifeStatus(list_lifeStatus.get(i));
        		 HealthMeasureHistory.saveHealthMeasureHistory(history_element);
        		}
        	}
        	return p;
        }
        
        //return Person.savePerson(person);
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