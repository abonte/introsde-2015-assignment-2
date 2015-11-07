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

> Request #1: [GET /person](#get-person)  
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

Response formats: XML,JSON

Example Request

```
http://127.0.1.1:5700/sdelab/person
```

Example Result

```xml

<people>
    <person>
        <idPerson>1</idPerson>
        <lastname>BB</lastname>
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

#### GET /person/{id}
Parameters
Example Request
Example Result
#### PUT /person/{id}
Parameters
Example Request
Example Result
#### POST /person
Parameters
Example Request
Example Result
#### DELETE /person/{id}
Parameters
Example Request
Example Result
#### GET /person/{id}/{measureType}
Parameters
Example Request
Example Result
#### GET /person/{id}/{measureType}/{mid}
Parameters
Example Request
Example Result
#### POST /person/{id}/{measureType}
Parameters
Example Request
Example Result
#### GET /measureTypes
Parameters
Example Request
Example Result
#### PUT /person/{id}/{measureType}/{mid} 
Parameters
Example Request
Example Result
#### GET /person/{id}/{measureType}?before={beforeDate}&after={afterDate}
Parameters
Example Request
Example Result
#### GET /person?measureType={measureType}&max={max}&min={min}
Parameters
Example Request
Example Result