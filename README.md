# Assignment 02: RESTful Services

## [Introduction to Service Design and Engineering](https://github.com/IntroSDE) | [University of Trento](http://www.unitn.it/)

This repository is the solution to the [second assignment](https://sites.google.com/a/unitn.it/introsde_2015-16/lab-sessions/assignments/assignment-2) of the course IntroSDE of the University of Trento. This assignment cover the following topics:

* [LAB05](https://github.com/IntroSDE/lab05): The REST architectural style & RESTful web services
* [LAB06](https://github.com/IntroSDE/lab06): CRUD RESTful Services
* [LAB07](https://github.com/IntroSDE/lab07): Reading and writing from Databases & JPA (Java Persistence API)

### Task of the code

### Code

##### Folders

##### File

### Installation

### Usage

TODO Requires authentcation?
		license

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
Should update the personal information of the person identified by {id} (i.e., only the person's information, not the measures of the health profile).  
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
Parameters
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
**XML**
```xml
```

**JSON**
```json
```
#### DELETE /person/{id}
Parameters
#####Example Request
#####Example Result
#### GET /person/{id}/{measureType}
Parameters
#####Example Request
#####Example Result
**XML**
```xml
```

**JSON**
```json
```
#### GET /person/{id}/{measureType}/{mid}
Parameters
#####Example Request
#####Example Result
**XML**
```xml
```

**JSON**
```json
```
#### POST /person/{id}/{measureType}
Parameters
#####Example Request
**XML**
```xml
```

**JSON**
```json
```
#####Example Result
**XML**
```xml
```

**JSON**
```json
```
#### GET /measureTypes
Parameters
#####Example Request
#####Example Result
**XML**
```xml
```

**JSON**
```json
```
#### PUT /person/{id}/{measureType}/{mid} 
Parameters
#####Example Request
**XML**
```xml
```

**JSON**
```json
```
#####Example Result
**XML**
```xml
```

**JSON**
```json
```
#### GET /person/{id}/{measureType}?before={beforeDate}&after={afterDate}
Parameters
#####Example Request
#####Example Result
**XML**
```xml
```

**JSON**
```json
```
#### GET /person?measureType={measureType}&max={max}&min={min}
Parameters
#####Example Request
#####Example Result
**XML**
```xml
```

**JSON**
```json
```