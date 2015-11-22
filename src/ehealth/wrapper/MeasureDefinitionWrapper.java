package ehealth.wrapper;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.annotation.JsonProperty;

import ehealth.model.MeasureDefinition;
/**
 * Wrapper used when are listened the measureName of all the MeasureDefinition
 *
 */
@XmlRootElement(name="measureTypes")
public class MeasureDefinitionWrapper {
	
	@XmlElement(name="measureType")
	@JsonProperty("measureTypes")
	public List<MeasureDefinition> measureDefinition = new ArrayList<MeasureDefinition>();
	
	public void setMeasureDefinition(List<MeasureDefinition> definitions) {
		this.measureDefinition = definitions;
	}
	
	
}
