package ehealth.resources;
import ehealth.model.*;
import ehealth.wrapper.MeasureDefinitionWrapper;

import java.util.List;
import javax.ejb.*;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceContextType;
import javax.persistence.PersistenceUnit;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.UriInfo;

@Stateless // will work only inside a Java EE application
@LocalBean // will work only inside a Java EE application
@Path("/measureTypes")
public class MesureDefinitionResourceCollection {

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
     * Request #9: GET /measureTypes should return the list of measures your model supports
     * Returns data in JSON and XML
     * @return list of Measure Definition
     */
    @GET
    @Produces({MediaType.TEXT_XML,  MediaType.APPLICATION_JSON ,  MediaType.APPLICATION_XML })
    public MeasureDefinitionWrapper getMeasureTypes() {
        List<MeasureDefinition> definitions = MeasureDefinition.getAll();
        MeasureDefinitionWrapper mdw = new MeasureDefinitionWrapper();
        mdw.setMeasureDefinition(definitions);
        return mdw;
    }

}
