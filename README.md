# Assignment 02: RESTful Services

## [Introduction to Service Design and Engineering](https://github.com/IntroSDE) | [University of Trento](http://www.unitn.it/)

This repository is the solution to the [second assignment](https://sites.google.com/a/unitn.it/introsde_2015-16/lab-sessions/assignments/assignment-2) of the course IntroSDE of the University of Trento. This assignment cover the following topics:

* [LAB05](https://github.com/IntroSDE/lab05): The REST architectural style & RESTful web services
* [LAB06](https://github.com/IntroSDE/lab06): CRUD RESTful Services
* [LAB07](https://github.com/IntroSDE/lab07): Reading and writing from Databases & JPA (Java Persistence API)

The name of the student with whom I worked: Carlo Nicolò [https://github.com/carlonicolo/introsde-2015-assignment-2](https://github.com/carlonicolo/introsde-2015-assignment-2)  
URL of my server: [https://peaceful-hamlet-5616.herokuapp.com/sdelab](https://peaceful-hamlet-5616.herokuapp.com/sdelab)  
URL of my partner server: [https://arcane-beach-6023.herokuapp.com/sdelab/](https://arcane-beach-6023.herokuapp.com/sdelab/)  

### Code

*[src/](src/)*: contains source code;  
*[src/ehealth](src/ehealth)*:;  
*[src/ehealth/dao](src/ehealth/dao)*:;  
*[src/ehealth/model](src/ehealth/model)*:;  
*[src/ehealth/resources](src/ehealth/resources)*:;  
*[src/ehealth/wrapper](src/ehealth/wrapper)*:;  
*[src/client](src/client)*:;  
*[src/ehealth/App.java](src/ehealth/App.java)*:;  

### Installation

In order to execute this project you need the following technologies (in the brackets you see the version used to develop):

* Java (jdk1.8.0)
* ANT (version 1.9.4)

Then, clone the repository. Run in your terminal:

```
git clone https://github.com/abonte/introsde-2015-assignment-2.git && cd introsde-2015-assignment-2
```

and run the following command:
```
ant execute.client
```

### Usage

This project use an [ant build script](build.xml) to automate the compilation and the execution of specific part of the Java application.
```
ant execute.client
```
This command performs the following action:

* download and install ivy (dependency manager) and resolve the dependencies. *Ivy* and *WebContent/WEB-INF/lib/* folders are generated;
* create a build directory and compile the code in the src folder. You can find the compiled code in *build* folder;
* call others target defined in the build file:
    * `execute.client.partnerServer.xml` send REST queries to the partner server with the body in XML format and accept response in XML. The output is saved into [client-server-xml.log](client-server-xml.log);
    * `execute.client.partnerServer.json`send REST queries to the partner server with the body in JSON format and accept response in JSON. The output is saved into [client-server-json.log](client-server-json.log).


You can also send queries to my server:
```
execute.client.myServer
```
This command calls the following target:
    * `execute.client.myServer.xml` send REST queries to my server with the body in XML format and accept response in XML. The output is saved into [client-myServer-xml.log](client-myServer-xml.log);
    * `execute.client.myServer.json` send REST queries to my partner server with the body in JSON format and accept response in JSON. The output is saved into [client-myServer-json.log](client-myServer-json.log).

You can also execute specific task. Before, you have to execute
```
ant install
```
and then one of the following command:

* `execute.client.partnerServer.xml`
* `execute.client.partnerServer.json`
* `execute.client.myServer.xml`
* `execute.client.myServer.json`
* `ant clean` this command deletes the folders created during the compile phase and the file created during the execution of the various targets. 

If you want to run the server locally then run:
```
ant install
ant start
```
In order to run the client on your local server modify the variable *uriServer* in the [src/client/TestClient.java](src/client/TestClient.java).

Request #1: [GET /person](#get-person)  
Request #2: [GET /person/{id}](#get-personid)   
Request #3: [PUT /person/{id}](#put-personid)   
Request #4: [POST /person](#post-person)    
Request #5: [DELETE /person/{id}](#delete-personid)  
Request #6: [GET /person/{id}/{measureType}](#get-personidmeasuretype)  
Request #7: [GET /person/{id}/{measureType}/{mid}](#get-personidmeasuretypemid)  
Request #8: [POST /person/{id}/{measureType}](#post-personidmeasuretype)  
Request #9: [GET /measureTypes](#get-measuretypes)  
Request #10: [PUT /person/{id}/{measureType}/{mid}](#put-personidmeasuretypemid)  
Request #11: [GET /person/{id}/{measureType}?before={beforeDate}&after={afterDate}](#get-personidmeasuretypebeforebeforedateafterafterdate)  
Request #12: [GET /person?measureType={measureType}&max={max}&min={min}](#get-personmeasuretypemeasuretypemaxmaxminmin)  


#### GET /person

Returns a list of all the people in the database.  
Response formats: XML, JSON.

#####Example Request

```
GET http://127.0.1.1:5700/sdelab/person
```

#####Example Result  
HTTP Status: 200 OK

**XML**
```xml

<people>
    <person>
        <idPerson>1</idPerson>
        <firstname>Pinco</firstname>
        <lastname>Palla</lastname>
        <birthdate>1978-09-02T00:00:00+02:00</birthdate>
        <healthprofile>
            <measureType>
                <measure>height</measure>
                <value>180</value>
            </measureType>
            <measureType>
                <measure>weight</measure>
                <value>86</value>
            </measureType>
        </healthprofile>
    </person>
    
    <!--more people-->
</people>

```
**JSON**
```json
{
  "people": [
    {
      "idPerson": 1,
      "firstname": "Andrea",
      "lastname": "Grant",
      "birthdate": "1978-08-31",
      "healthprofile": [
        {
          "value": "72.3",
          "measure": "weight"
        },
        {
          "value": "72",
          "measure": "height"
        }
      ]
    },
    
    MORE PEOPLE
     ]
}

```




#### GET /person/{id}
Returns all the personal information plus current measures of person identified by {id} (i.e., current measures means current health profile)  
{id} identifier of the person  
Response formats: XML, JSON.
#####Example Request

```
GET http://127.0.1.1:5700/sdelab/person/1
```

#####Example Result
HTTP Status: 200 OK

**XML**
```xml

<person>
    <idPerson>1</idPerson>
    <firstname>Pinco</firstname>
    <lastname>Palla</lastname>
    <birthdate>1978-09-02T00:00:00+02:00</birthdate>
    <healthprofile>
        <measureType>
            <measure>height</measure>
            <value>180</value>
        </measureType>
        <measureType>
            <measure>weight</measure>
            <value>86</value>
        </measureType>
    </healthprofile>
</person>

```
**JSON**
```json
{
  "idPerson": 1,
  "firstname": "Pinco",
  "lastname": "Palla,
  "birthdate": "1978-09-02",
  "healthprofile": [
    {
      "value": "86",
      "measure": "weight"
    },
    {
      "value": "180",
      "measure": "height"
    }
  ]
}

```

#### PUT /person/{id}
Update the personal information of the person identified by {id} (i.e., only the person's information, not the measures of the health profile).  
Only the information in the request are updated, the other remains as before.  
{id} identifier of the person  
Accepted formats: XML, JSON.  
Response formats: XML, JSON.
#####Example Request

```
PUT http://127.0.1.1:5700/sdelab/person/1
```

**Body XML**
```xml
<person>
    <firstname>Pinco</firstname>
    <lastname>Palla</lastname>
    <birthdate>1978-09-02</birthdate>
</person>
```

**Body JSON**
```json
{
  "firstname": "Pinco",
  "lastname": "Palla,
  "birthdate": "1978-09-02",
}

```
#####Example Result
HTTP Status: 201

#### POST /person
Create a new person and return the newly created person with its assigned id.
If a health profile is included, create also those measurements for the new person.  
Accepted formats: XML, JSON.  
Response formats: XML, JSON.
#####Example Request

```
POST http://127.0.1.1:5700/sdelab/person/
```
**Body XML**
```xml
<person>
    <firstname>Pinco</firstname>
    <lastname>Palla</lastname>
    <birthdate>1978-09-02</birthdate>
    <healthprofile>
        <measureType>
            <measure>height</measure>
            <value>180</value>
        </measureType>
        <measureType>
            <measure>weight</measure>
            <value>86</value>
        </measureType>
    </healthprofile>
</person>
```

**Body JSON**
```json
{
  "firstname": "Pinco",
  "lastname": "Palla,
  "birthdate": "1978-09-02",
  "healthprofile": [
    {
      "value": "86",
      "measure": "weight"
    },
    {
      "value": "180",
      "measure": "height"
    }
  ]
}
```
#####Example Result
HTTP Status: 200

**XML**
```xml
<person>
    <idPerson>57</idPerson>
    <firstname>Pinco</firstname>
    <lastname>Palla</lastname>
    <birthdate>1978-09-02</birthdate>
    <healthprofile>
        <measureType>
            <measure>height</measure>
            <value>180</value>
        </measureType>
        <measureType>
            <measure>weight</measure>
            <value>86</value>
        </measureType>
    </healthprofile>
</person>
```

**JSON**
```json
{
  "idPerson": 57,
  "firstname": "Pinco",
  "lastname": "Palla,
  "birthdate": "1978-09-02",
  "healthprofile": [
    {
      "value": "86",
      "measure": "weight"
    },
    {
      "value": "180",
      "measure": "height"
    }
  ]
}
```

#### DELETE /person/{id}
Delete the person identified by {id} from the system
{id} identifier of the person
#####Example Request
```
DELETE http://127.0.1.1:5700/sdelab/person/57
```
#####Example Result
HTTP Status: 204

#### GET /person/{id}/{measureType}
Return the list of values (the history) of {measureType} (e.g. weight) for person identified by {id}  
{id} identifier of the person  
{measureType} represent the name of the measure (weight, height, steps, blood pressure, heart rate, bmi) 
Response formats: XML, JSON.
#####Example Request
```
GET http://127.0.1.1:5700/sdelab/person/57/weight
```
#####Example Result
HTTP Status: 200  
**XML**
```xml
<measureHistory>
     <mesure>
          <mid>2</mid>
          <value>86</value>
          <created>2015-11-13T23:00:00Z</created>
     </mesure>
     <mesure>
         <mid>53</mid>
         <value>72</value>
         <created>2011-12-09T00:00:00Z</created>
     </mesure>
     <mesure>
         <mid>56</mid>
         <value>72</value>
         <created>2011-12-09T00:00:00Z</created>
     </mesure>
</measureHistory>
```

**JSON**
```json
[
    {
        "mid": 1,
        "value": "83",
        "created": "2012-12-27"
    },
    {
        "mid": 2,
        "value": "80",
        "created": "2013-02-26"
    }
]
```
#### GET /person/{id}/{measureType}/{mid}
Return the value of {measureType} (e.g. weight) identified by {mid} for person identified by {id}  
{id} identifier of the person  
{measureType} represent the name of the measure (weight, height, steps, blood pressure, heart rate, bmi) 
{mid} unique identifier of the element in the history  
Response formats: XML, JSON.
#####Example Request
```
GET http://127.0.1.1:5700/sdelab/person/57/weight/1
```
#####Example Result
HTTP Status: 200  
**XML**
```xml
83
```

**JSON**
```json
83
```
#### POST /person/{id}/{measureType}
Save a new value for the {measureType} (e.g. weight) of person identified by {id} and archive the old value in the history
{id} identifier of the person  
{measureType} represent the name of the measure (weight, height, steps, blood pressure, heart rate, bmi)
Accepted formats: XML, JSON.  
Response formats: XML, JSON. 
#####Example Request
```
GET http://127.0.1.1:5700/sdelab/person/57/height/
```
**XML**
```xml
<lifestatus>
     <measure>height</measure>
     <value>72</value>
</lifestatus>
```

**JSON**
```json
{
 "measure": "height",
 "value": "72"
}
```
#####Example Result
HTTP Status: 200  
**XML**
```xml
<lifestatus>
     <measure>height</measure>
     <value>72</value>
</lifestatus>
```

**JSON**
```json
{
 "measure": "height",
 "value": "72"
}
```
#### GET /measureTypes
Return the list of measures the model supports.  
Response formats: XML, JSON. 
#####Example Request
```
GET http://127.0.1.1:5700/sdelab/measureTypes
```
#####Example Result
HTTP Status: 200  
**XML**
```xml
<measureTypes>
    <measureType>weight</measureType>
    <measureType>height</measureType>
    <measureType>steps</measureType>
    <measureType>blood pressure</measureType>
    <measureType>heart rate</measureType>
    <measureType>bmi</measureType>
</measureTypes>
```

**JSON**
```json
{
  "measureTypes": [
    "weight",
    "height",
    "steps",
    "blood pressure",
    "heart rate",
    "bmi"
  ]
}
```
#### PUT /person/{id}/{measureType}/{mid} 
Update the value for the {measureType} (e.g., weight) identified by {mid}, related to the person identified by {id}
{id} identifier of the person  
{measureType} represent the name of the measure (weight, height, steps, blood pressure, heart rate, bmi)
{mid} unique identifier of the element in the history
Accepted formats: XML, JSON.  
#####Example Request
```
PUT http://127.0.1.1:5700/sdelab/1/height/12
```
**XML**
```xml
<measure>
     <value>90</value>
     <created>2011-12-09</created>
</measure>
```

**JSON**
```json
{
 "value": "72",
 "created": "2011-12-09"
}
```
#####Example Result
HTTP Status: 201

#### GET /person/{id}/{measureType}?before={beforeDate}&after={afterDate}
Return the history of {measureType} (e.g., weight) for person {id} in the specified range of date.  
{id} identifier of the person  
{measureType} represent the name of the measure (weight, height, steps, blood pressure, heart rate, bmi)  
{beforeDate} start date  
{afterDate} end date   
#####Example Request
```
GET http://127.0.0.1:5700/sdelab/person/4403/weight?before=2015-12-07&after=2011-12-08
```
#####Example Result
HTTP Status: 200
**XML**
```xml
<measureHistory>
   <mesure>
        <mid>2</mid>
        <value>186</value>
        <created>2015-11-13T23:00:00Z</created>
   </mesure>
   <mesure>
        <mid>53</mid>
        <value>72</value>
        <created>2011-12-09T00:00:00Z</created>
   </mesure>
<measureHistory>
```

**JSON**
```json
[
    {
        "mid": 1,
        "value": "186",
        "created": "2015-11-13"
    },
    {
        "mid": 53,
        "value": "72",
        "created": "2011-12-09"
    }
]
```
#### GET /person?measureType={measureType}&max={max}&min={min}
retrieves people whose {measureType} (e.g., weight) value is in the [{min},{max}] range (if only one for the query params is provided, use only that).  
{measureType} represent the name of the measure (weight, height, steps, blood pressure, heart rate, bmi)  
{max} end value  
{min} start value 
#####Example Request
```
GET http://127.0.0.1:5700/sdelab/person?measureType=weight&max=100&min=80
```
#####Example Result
HTTP Status: 200  
**XML**
```xml
<people>
    <person>
        <idPerson>1</idPerson>
        <firstname>Ellis</firstname>
        <lastname>Grant</lastname>
        <birthdate>1978-08-31T22:00:00Z</birthdate>
        <healthprofile>
            <measureType>
                <measure>weight</measure>
                <value>72.3</value>
            </measureType>
            <measureType>
                <measure>height</measure>
                <value>1.86</value>
            </measureType>
        </healthprofile>
    </person>
    <person>
        <idPerson>2</idPerson>
        <firstname>Theresia</firstname>
        <lastname>Rodriguez</lastname>
        <birthdate>1955-04-10T23:00:00Z</birthdate>
        <healthprofile>
            <measureType>
                <measure>weight</measure>
                <value>50</value>
            </measureType>
            <measureType>
                <measure>height</measure>
                <value>1.66</value>
            </measureType>
        </healthprofile>
    </person>
</people>
```

**JSON**
```json
{
  "people": [
    {
      "idPerson": 1,
      "firstname": "Ellis",
      "lastname": "Grant",
      "birthdate": "1978-08-31",
      "healthprofile": [
        {
          "value": "72.3",
          "measure": "weight"
        },
        {
          "value": "1.86",
          "measure": "height"
        }
      ]
    },
    {
      "idPerson": 2,
      "firstname": "Theresia",
      "lastname": "Rodriguez",
      "birthdate": "1955-04-10",
      "healthprofile": [
        {
          "value": "50",
          "measure": "weight"
        },
        {
          "value": "1.66",
          "measure": "height"
        }
      ]
    }
  ]
}
```