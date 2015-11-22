package ehealth.model;

import ehealth.dao.LifeCoachDao;
import ehealth.model.MeasureDefinition;

import java.io.Serializable;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.persistence.OneToOne;

/**
 * The persistent class for the "LifeStatus" database table.
 * 
 */
@Entity
@Table(name = "LifeStatus")
@NamedQueries({
	@NamedQuery(name = "LifeStatus.findAll", query = "SELECT l FROM LifeStatus l"),
	@NamedQuery(name="LifeStatus.findByMeasureDefPerson", query="SELECT l FROM LifeStatus l WHERE l.person = ?1 AND l.measureDefinition = ?2")
})
@XmlRootElement(name="lifestatus")
public class LifeStatus implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(generator="sqlite_lifestatus")
	@TableGenerator(name="sqlite_lifestatus", table="sqlite_sequence",
	    pkColumnName="name", valueColumnName="seq",
	    pkColumnValue="LifeStatus")
	@Column(name = "idMeasure")
	private int idMeasure;

	@Column(name = "value")
	private String value;
	
	@OneToOne
	@JoinColumn(name = "idMeasureDef", referencedColumnName = "idMeasureDef", insertable = true, updatable = true)
	private MeasureDefinition measureDefinition;
	
	@ManyToOne
	@JoinColumn(name="idPerson",referencedColumnName="idPerson")
	private Person person;

	public LifeStatus() {
	}
	
	public LifeStatus(Person person, MeasureDefinition md, String value) {
		this.person = person;
		this.measureDefinition = md;
		this.value = value;
	}

	@XmlTransient
	public int getIdMeasure() {
		return this.idMeasure;
	}

	public void setIdMeasure(int idMeasure) {
		this.idMeasure = idMeasure;
	}

	public String getValue() {
		return this.value;
	}

	public void setValue(String value) {
		this.value = value;
	}
	
	@XmlElement(name = "measure")
	public MeasureDefinition getMeasureDefinition() {
		return measureDefinition;
	}
	/*
	@XmlElement(name="measure")
	public String getMeasureName(){
		return measureDefinition.getMeasureName();
	}*/
	
	public void setMeasureDefinition(MeasureDefinition param) {
		this.measureDefinition = param;
	}

	// we make this transient for JAXB to avoid and infinite loop on serialization
	@XmlTransient
	public Person getPerson() {
		return person;
	}

	public void setPerson(Person person) {
		this.person = person;
	}
	
	// Database operations
	public static LifeStatus getLifeStatusById(int lifestatusId) {
		EntityManager em = LifeCoachDao.instance.createEntityManager();
		LifeStatus p = em.find(LifeStatus.class, lifestatusId);
		LifeCoachDao.instance.closeConnections(em);
		return p;
	}
	
	/**
	 * Returns a "LifeStatus" object associated with a specific person and with a specific
	 * MeasureDefinition
	 * @param md
	 * @param p
	 * @return LifeStatus
	 */
	public static LifeStatus getLifeStatusByMeasureDefPerson(MeasureDefinition md, Person p){
		EntityManager em = LifeCoachDao.instance.createEntityManager();
		try{
			LifeStatus ls = em.createNamedQuery("LifeStatus.findByMeasureDefPerson", LifeStatus.class).setParameter(1, p).setParameter(2, md).getSingleResult();
			LifeCoachDao.instance.closeConnections(em);
			return ls;
		}
		catch(Exception e){ 	          
			return null; 
		} 
	}
	
	public static List<LifeStatus> getAll() {
		EntityManager em = LifeCoachDao.instance.createEntityManager();
	    List<LifeStatus> list = em.createNamedQuery("LifeStatus.findAll", LifeStatus.class).getResultList();
	    LifeCoachDao.instance.closeConnections(em);
	    return list;
	}
	
	public static LifeStatus saveLifeStatus(LifeStatus p) {
		EntityManager em = LifeCoachDao.instance.createEntityManager();
		EntityTransaction tx = em.getTransaction();
		tx.begin();
		em.persist(p);
		tx.commit();
	    LifeCoachDao.instance.closeConnections(em);
	    return p;
	}
	
	public static LifeStatus updateLifeStatus(LifeStatus p) {
		EntityManager em = LifeCoachDao.instance.createEntityManager();
		EntityTransaction tx = em.getTransaction();
		tx.begin();
		p=em.merge(p);
		tx.commit();
	    LifeCoachDao.instance.closeConnections(em);
	    return p;
	}
	
	public static void removeLifeStatus(LifeStatus p) {
		EntityManager em = LifeCoachDao.instance.createEntityManager();
		EntityTransaction tx = em.getTransaction();
		tx.begin();
	    p=em.merge(p);
	    em.remove(p);
	    tx.commit();
	    LifeCoachDao.instance.closeConnections(em);
	}
}
