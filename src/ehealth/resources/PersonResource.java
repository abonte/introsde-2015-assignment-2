package ehealth.resources;

//import ehealth.model.MeasureDefinition;
import ehealth.model.*;
import ehealth.wrapper.MeasureHistoryWrapper;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

@Stateless // only used if the the application is deployed in a Java EE container
@LocalBean // only used if the the application is deployed in a Java EE container
public class PersonResource {
    @Context
    UriInfo uriInfo;
    @Context
    Request request;
    int id;

    EntityManager entityManager; // only used if the application is deployed in a Java EE container

    public PersonResource(UriInfo uriInfo, Request request,int id, EntityManager em) {
        this.uriInfo = uriInfo;
        this.request = request;
        this.id = id;
        this.entityManager = em;
    }

    public PersonResource(UriInfo uriInfo, Request request,int id) {
        this.uriInfo = uriInfo;
        this.request = request;
        this.id = id;
    }

    // Application integration
    /**
     * Request #2: GET /person/{id} should give all the personal information plus current measures
     * of person identified by {id}, current measures means current health profile.
     * @return the person corresponding to the {id}
     */    
    @GET
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response getPerson() {
        Person person = this.getPersonById(id);
        if (person == null)
           return Response.status(Response.Status.NOT_FOUND)
        		   .entity("Get: Person with " + id + " not found").build();
        else
           return Response.ok(person).build();
    }

    // for the browser
    @GET
    @Produces(MediaType.TEXT_XML)
    public Response getPersonHTML() {
        Person person = this.getPersonById(id);
        if (person == null)
            return Response.status(Response.Status.NOT_FOUND)
         		   .entity("Get: Person with " + id + " not found").build();
         else
            return Response.ok(person).build();
    }
    
    /**
     * Request #3: PUT /person/{id} should update the personal information of the person identified by {id}
     * (e.i., only the person's information, not the measures of the health profile)
     * @param person
     * @return the response to this operation: the content is not present
     */
    @PUT
    @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response putPerson(Person person) {
        System.out.println("--> Updating Person... " +this.id);
        System.out.println("--> "+person.toString());
        //Person.updatePerson(person);
        Response res;
        Person existing = getPersonById(this.id);

        if (existing == null) {
            res = Response.noContent().build();
        } else {
            res = Response.created(uriInfo.getAbsolutePath()).build();
            person.setIdPerson(this.id);
            //checks if the client sent a name in order to update the person
            //if there is no name, remain the previous name
            if (person.getName() == null){
            	person.setName(existing.getName());
            }
            if (person.getLastname() == null){
            	person.setLastname(existing.getLastname());
            }
            if (person.getBirthdate() == null){
            	person.setBirthdate(existing.getBirthdate());
            }
            person.setLifeStatus(existing.getLifeStatus());
            Person.updatePerson(person);
        }
        return res;
    } 
    
    /**
     * Request #5: DELETE /person/{id} should delete the person identified by {id} from the system
     */
    @DELETE
    public void deletePerson() {
        Person c = getPersonById(id);
        if (c == null)
            throw new RuntimeException("Delete: Person with " + id
                    + " not found");
        Person.removePerson(c);
    }

    public Person getPersonById(int personId) {
        System.out.println("Reading person from DB with id: "+personId);

        // this will work within a Java EE container, where not DAO will be needed
        //Person person = entityManager.find(Person.class, personId); 

        Person person = Person.getPersonById(personId);
        //System.out.println("Person: "+person.toString());
        return person;
    }
    
    /**
     * Request #6: GET /person/{id}/{measureType} should return
     * the list of values (the history) of {measureType} (e.g. weight) for person identified by {id}
     * 
     * Request #11: GET /person/{id}/{measureType}?before={beforeDate}&after={afterDate}
     * should return the history of {measureType} (e.g., weight) for person {id}
     * in the specified range of date
     * @param measureName
     * @return list of HealthMeasureHistory objects
     * @throws ParseException 
     */
    @GET
    @Path("{measureType}")
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public MeasureHistoryWrapper getPersonHistory(@PathParam("measureType") String measureName, @QueryParam("before") String before_s,
    		@QueryParam("after") String after_s) throws ParseException {
    	
    	// TODO midificare root element HealthmeasureHISTORIES
    	//searches the measure definition associated with the name of the measure
    	MeasureDefinition md = new MeasureDefinition();
    	md = MeasureDefinition.getMeasureDefinitionByName(measureName);

    	Person person = this.getPersonById(id);
    	List<HealthMeasureHistory> list_MH = new ArrayList<HealthMeasureHistory>();
    	// TODO invertire if else
    	if(before_s == null || after_s == null){
    		list_MH = HealthMeasureHistory.getByPersonMeasure(person, md);
    	}else{
    		Calendar before = Calendar.getInstance();
        	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        	before.setTime(sdf.parse(before_s));// all done
        	Calendar after = Calendar.getInstance();
        	after.setTime(sdf.parse(after_s));// all done
    		list_MH = HealthMeasureHistory.getByPersonMeasureDate(person, md, before, after);
    	}
    	if (list_MH == null)
    		throw new RuntimeException("Get: History for person " + id + " not found");
    	MeasureHistoryWrapper mhw = new MeasureHistoryWrapper();
    	mhw.setHealthMeasureHistory(list_MH);
    	return mhw;
    }
    
    /**
     * Request #7: GET /person/{id}/{measureType}/{mid} should return the value of {measureType} (e.g. weight)
     * identified by {mid} for person identified by {id}
     * @param measureName
     * @param mid
     * @return a string representing the value of the HealthMeasureHistory with id = {mid}
     */
    @GET
    @Path("{measureType}/{mid}")
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public String getMeasureHistoryId(@PathParam("measureType") String measureName, @PathParam("mid") int mid) {
        return HealthMeasureHistory.getHealthMeasureHistoryById(mid).getValue();
    }
    
    /** 
     * Request #8: POST /person/{id}/{measureType} should save a new value for the {measureType}
     * (e.g. weight) of person identified by {id} and archive the old value in the history
     * @param mesureName
     * @return the new 'lifestatus' object
     */
    @POST
    @Path("{measureType}")
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public LifeStatus newMeasureValue(HealthMeasureHistory hmh, @PathParam("measureType") String measureName){
    	Person person = this.getPersonById(id);
    	
    	//searches the measure definition associated with the name of the measure
		MeasureDefinition md = new MeasureDefinition();
		md = MeasureDefinition.getMeasureDefinitionByName(measureName);
		
		//remove actual 'lifestatus' for measureName
		LifeStatus lf = LifeStatus.getLifeStatusByMeasureDefPerson(md,person);
		if(lf != null)
			LifeStatus.removeLifeStatus(lf);
		
		//save new 'lifestatus' for measureName
		LifeStatus newlf = new LifeStatus(person, md, hmh.getValue());
		newlf = LifeStatus.saveLifeStatus(newlf);
		
		//insert the new measure value in the history
		hmh.setPerson(person);
		hmh.setMeasureDefinition(md);
		HealthMeasureHistory.saveHealthMeasureHistory(hmh);
		
    	return LifeStatus.getLifeStatusById(newlf.getIdMeasure());
    }
    
    /**
     * Request #10: PUT /person/{id}/{measureType}/{mid} should update the value for the {measureType}
     * (e.g., weight) identified by {mid}, related to the person identified by {id}
     */    
    @PUT
    @Path("{measureType}/{mid}")
    @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response putHealthHistory(HealthMeasureHistory hmh,@PathParam("mid") int mid){
    	 Response res;
         HealthMeasureHistory existing = HealthMeasureHistory.getHealthMeasureHistoryById(mid);
         
         if (existing == null) {
             res = Response.noContent().build();
         } else {
        	 res = Response.created(uriInfo.getAbsolutePath()).build();
        	 existing.setValue(hmh.getValue()); //change the value
        	 HealthMeasureHistory.updateHealthMeasureHistory(existing);
         }
         return res;
     }
    
}