package ehealth.wrapper;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.annotation.JsonProperty;


import ehealth.model.Person;

/**
 * Wrapper used when are listened the person
 *
 */
@XmlRootElement(name="people")
public class PeopleWrapper {

	@XmlElement(name="person")
	@JsonProperty("people")
	public List<Person> people = new ArrayList<Person>();
	
	public void setPeople(List<Person> people){
	    	this.people = people ;
	}
	
}
