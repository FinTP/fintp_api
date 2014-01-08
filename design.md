#FinTP REST API design document
Release 1.0


*Revisions*
2013, Initial version – Horia Beschea
 
##1.	Introduction
###Purpose of this document
This document is intended to capture design constraints and decisions for the FinTP REST API project.

It is meant to create a high level view of the APIs to be provided and create guidelines for extending the APIs in a consistent manner.

###Design rationales
**What is REST ?**

REST stands for Representational State Transfer. It relies on a stateless (Not hold state between requests (meaning that all the information necessary to respond to a request is available in each individual request; no data, or state, is held by the server from request to request)), client-server, cacheable communications protocol -- and in virtually all cases, the HTTP protocol is used.

REST is an architecture style for designing networked applications. The idea is that, rather than using complex mechanisms such as CORBA, RPC or SOAP to connect between machines, simple HTTP is used to make calls between machines.

When something conforms to these REST design constraints, we commonly refer to it as “RESTful”.

RESTful applications use HTTP requests to post data (create and/or update), read data (e.g., make queries), and delete data. Thus, REST uses HTTP for all four CRUD (Create/Read/Update/Delete) operations.

**Why REST ?**

The key design constraint that sets REST apart from other distributed architectural styles is its emphasis on a uniform interface between components. REST further defines how to use the uniform interface through additional constraints around how to identify resources, how to manipulate resources through representations, and how to include metadata that make messages self-describing. This ultimately leads to a simpler overall system architecture and provides more visibility into the various interactions. 

###Notations
{id} accolades will be used to identify variables 

##2.	Resources
The fundamental concept in any RESTful API is the resource. A resource is an object with a type, associated data, relationships to other resources, and a set of methods that operate on it. It is similar to an object instance in an object-oriented programming language, with the important difference that only a few standard methods are defined for the resource (corresponding to the standard HTTP GET, POST, PUT and DELETE methods), while an object instance typically has many methods.

Resources can be grouped into collections. Each collection is homogeneous so that it contains only one type of resource, and unordered. Resources can also exist outside any collection. In this case, we refer to these resources as singleton resources. Collections are themselves resources as well.

Collections can exist globally, at the top level of an API, but can also be contained inside a single resource. In the latter case, we refer to these collections as sub-collections. Sub-collections are usually used to express some kind of "contained in" relationship.

###Resource data
Resources have data associated with them. The richness of data that can be associated with a resource is part of the resource model for an API. It defines for example the available data types and their behavior.

In JSON, just three types of data exist:
-	scalar (number, string, boolean, null). 
-	array
-	object

Scalar types have just a single value. Arrays contain an ordered list of values of arbitrary type. Objects consist of a unordered set of key:value pairs (also called attributes, not to be confused with XML attributes), where the key is a string and the value can have an arbitrary type. For more detailed information on JSON, see the JSON web site.

Times and dates will be exchanged formatted as ISO in string attributes.

Time format is : yyyy-MM-ddTHH:mm:ssZ

Date format is : yyyy-MM-dd

####Naming conventions
Resources will be named as plural ( e.g. Messages, Queues, etc )

Resources names will be concrete object, not abstract entities ( e.g. Messages vs Items, Queues vs Objects, etc ). 

Resource paths will be all-lower-case ( e.g. /queues, /messages,/routingrules )

Java classes implementing resources will be named as {ResourceName}Resource ( e.g. QueueResource, MessageResource )

Java classes implementing resource collections will be named as {ResourceName-plural}Resource ( e.g. QueuesResource, MessagesResource )
Java classes representing JPA entities will be named as {EntityName}Entity.

####Application data
We define the data that can be associated with a resource in terms of the JSON data model, using the following mapping rules:

1.	Resources are modeled as a JSON object. The type of the resource is stored under the special key:value pair "_type".
2.	Data associated with a resource is modeled as key:value pairs on the JSON object. To prevent naming conflicts with internal key:value pairs, keys must not start with "_".
3.	The values of key:value pairs use any of the native JSON data types of string, number, true, false, null, or arrays thereof. Values can also be objects, in which case they are modeling nested resources.
4.	Collections are modeled as an array of objects.
We will also refer to key:value pairs as attributes of the JSON object, and we will be sloppy and use that same term for data items associated with resources, too. This use of attributes is not to be confused with XML attributes.


#####Queue
Attribute  | Type | Description
--- | --- | ---
name | String | Name of the queue (unique, required)
description	| String | Human readable description of the purpose of the queue 
holdstatus | Boolean | Hold status (required, default=false). Values : false = not held; true = held
connector | String | Name of a defined connector
type | String | Queue type (required, default “Ordinary”). Values : “ordinary”=normal;???
link | Link | Link to messages that are in the queue


Example

	{
	"href":"api/queues/ACHOutQueue",
	"_type":"ro.allevo.fintpws.resouces.QueueResource",
	"name":"ACHOutQueue",
	"description":"ACH output queue from QPI",
	"holdstatus":0,
	"connector":1234
	"link":
		{
		href=":"api/queues/ACHOutQueue/messages",
		rel="messages"
		}
	}
	
#####Queues
Default sort will be by name ( ascending - A-Z )

Attribute |Type | Description
--- | --- | ---
total | Number| Returned only if the filter contains “t”
queues | Queue-collection	(resource included) | List of queues 

Example

	{
	"href":"api/queues",
	"_type":"ro.allevo.fintpws.resouces.QueuesResource",
	"queues":[
		{
		"href":"api/queues/BOTFDRCLInQueue",
		"_type":"ro.allevo.fintpws.resouces.QueueResource",
		"name":"BOTFDRCLInQueue",
		"holdstatus":0,
		"connector":”EventsWatcher”
		},
		{
		"href":"api/queues/qPayROIOutQueue",
		"_type":"ro.allevo.fintpws.resouces.QueueResource",
		"name":"qPayROIOutQueue",
		"holdstatus":0
		}
		]
	}
	
#####QueueAction
Attribute |Type | Description
--- | --- | ---
action | String | Name of the action
description | String | Human readable description of the purpose of the queue action
selmsg | Number | Selected message level action
groupmsg | Number | Group message level action 
optionvalues | String | Storage of the available values in the drop down lists
currmsg | Number | Current message level action

Example

	{
	"href":"api/queueactios/OutQueueAction",
	"_type":"ro.allevo.fintpws.resouces.QueueActionResource",
	"action":"OutQueueAction",
	"description":”output queue action from QPI",
	"currmsg":120,
	"groupmsg":123,
	"selmsg":1
	}
	
#####QueueActions
Default sort will be by action ( ascending - A-Z )

Attribute |Type | Description
--- | --- | ---
total | Number | Returned only if the filter contains “t”
queueactions | QueueAction-collection	(resource included) | List of queue actions 

Example

	{
	"href":"api/queueactions",
	"_type":"ro.allevo.fintpws.resouces.QueueActionsResource",
	"queueactions":[
		{
		"href":"api\/queueactions\/Authorize",
		"_type":"ro.allevo.fintpws.resources.QueueActionResource",
		"action":"Authorize",
		"currmsg":1,
		"selmsg":1,
		"groupmsg":0
		},
		{
		"href":"api\/queueactions\/Batch",
		"_type":"ro.allevo.fintpws.resources.QueueActionResource",
		"action":"Batch",
		"currmsg":0,
		"selmsg":1,
		"groupmsg":1,
		"optionvalues":"integer"
		}
	]
	}
	
#####QueueType
Attribute |Type | Description
--- | --- | ---
lev1kword1 | String	 | Level 1 – keyword 1
lev1kword2 | String	 | Level 1 – keyword 2
lev1kword3 | String	 | Level 1 – keyword 3
lev1kword4 | String	 | Level 1 – keyword 4
lev1kword5 | String	 | Level 1 – keyword 5
lev2kword1 | String	 | Level 2 – keyword 1
lev2kword2 | String	 | Level 2 – keyword 2
lev2kword3 | String	 | Level 2 – keyword 3
lev2kword4 | String	 | Level 2 – keyword 4
lev2kword5 | String | Level 2 – keyword 5

Example

	{
	"href":"api/queuetypes/OutQueueType",
	"_type":"ro.allevo.fintpws.resouces.QueueTypeResource",
	"typename":"OutQueueType",
	"lev1kword1":"lev111",
	"lev1kword2":"lev112",
	"lev1kword3":"lev113",
	"lev1kword4":"lev114",
	"lev1kword5":"lev115",
	"lev2kword1":"lev211",
	"lev2kword2":"lev212",
	"lev2kword3":"lev213",
	"lev2kword4":"lev214",
	"lev2kword5":"lev215",
	}
	
#####QueueTypes
Default sort will be by tyename ( ascending - A-Z )

Attribute |Type | Description
--- | --- | ---
total | Number | Returned only if the filter contains “t”
queuetypes | QueueType-collection	(resource included) | List of queue types 

Example

	{
	"href":"api/queuetypes",
	"_type":"ro.allevo.fintpws.resouces.QueueTypesResource",
	"queuetypes":[
		{
		"href":"api/queuetypes/BOTFDRCLInQueueType",
		"_type":"ro.allevo.fintpws.resouces.QueueTypeResource",
		"typename":"BOTFDRCLInQueueType",
		},
		{
		"href":"api/queuetypes/qPayROIOutQueueType",
		"_type":"ro.allevo.fintpws.resouces.QueueTypeResource",
		"typename":"qPayROIOutQueueType"
		}
		]
	}
	
#####ServiceMap
Attribute |Type | Description
--- | --- | ---
friendlyname | String | Service display name
status | Number | Service state
lastsessionid | Number | [not used]
heartbeatinterval | Number | [not used]
lastheartbeat | Timestamp | [not used]
version | String | [not used]
partner | String | Application name
servicetype | Number | [not used]
ioidentifier | Number | Input / Output service type
exitpoint | String | Exit point definition 
duplicatecheck | Number | Duplicate detection activation 
duplicateq | String | Name of the queue where duplicates will be sent for investigation 
duplicatemap | String | Name of the duplicate map for this service (xslt) 
duplicatenotifq | String | Name of the queue storing duplicate notifications 
delayednotifq | String | Name of the queue storing delayed notifications

Example

	{
	"href":"api\/servicemaps\/AMLEngine",
	"_type":"ro.allevo.fintpws.resources.ServiceMapResource",
	"friendlyname":"AMLEngine",
	"status":1,
	"heartbeatinterval":50,
	"lastsessionid":0,
	"lastheartbeat":"2011-05-27T14:29:40.592Z",
	"partner":" ",
	"servicetype":1,
	"ioidentifier":1,
	"sessionid":"4e5fafc6-ee005082-5f2d0001"
	}
	
#####ServiceMaps
Default sort will be by friendlyname ( ascending - A-Z )

Attribute |Type | Description
--- | --- | ---
total | Number | Returned only if the filter contains “t”
servicemaps | ServiceMap-collection	(resource included) | List of service maps 

Example

	{
	"href":"api\/servicemaps",
	"_type":"ro.allevo.fintpws.resources.ServiceMapsResource",
	"has_more":true,
	"servicemaps":[
		{
		"href":"api\/servicemaps\/AMLEngine",
		"_type":"ro.allevo.fintpws.resources.ServiceMapResource",
		"friendlyname":"AMLEngine",
		"status":1,
		"heartbeatinterval":50,
		"lastsessionid":0,
		"lastheartbeat":"2011-05-27T14:29:40.592Z",
		"partner":" ",
		"servicetype":1,
		"ioidentifier":1,
		"sessionid":"4e5fafc6-ee005082-5f2d0001"
		},
		{
		"href":"api\/servicemaps\/EventsWatcher",
		"_type":"ro.allevo.fintpws.resources.ServiceMapResource",
		"friendlyname":"EventsWatcher",
		"status":3,
		"heartbeatinterval":0,
		"lastsessionid":0,
		"lastheartbeat":"2011-05-27T14:29:40.592Z",
		"servicetype":0,
	 	"ioidentifier":0,
	 	"sessionid":"4e5f4b44-0d635082-0d6c0001"
		}
		]
	} 
	
#####Message
Attribute |Type | Description
--- | --- | ---
guid | String | Unique identifier of the message ( Required, Unique)
batchid | String | Identifies the batch this message is part of
correlationid | String | Correlation identifier (Required, relation to events)
requestorservice | String | Sender application
responderservice | String | Receiver application
requesttype | String | Request type ( request, response, singlemessage )
feedback | String | Code set by the last processing operations
sessionid | String | Identifies the session when the message left/arrived in FinTP
priority | Number | Routing field : job priority 
holdstatus | Boolean | Routing field : indicates if the message is waiting for some conditions to become available or human intervention ( true )
sequence | Number | Routing field : the sequence of the last routing rule applied
currentqueue | String | Routing field : the queue where the message can be found 
events | Events	(resource included) Events related to this message ( based on correlation id )
messagetype | String | Message type
sender | String | Business partner : sender 
receiver | String | Business partner : receiver
trn | String | Business transaction reference
payload | String | Actual message payload ( business data ) – only returned if the filter contains ”b”

Example

	{
	"href":"/api/messages/123456-789012-345678",
	"type":"…",
	"guid":"123456-789012-345678",
	"batchid":"123456-789012-345678",
	"correlationid":"123456-789012-345678",
	"requestorservice":"BOACHOut",
	"responderservice":"ACHOut",
	"requestortype":"SingleMessage",
	"feedback":"W|123|02",
	"sessionid":"1",
	"priority":"99",
	"holdstatus":"1",
	"sequence":"1",
	"currentqueue":"BOACHOutQueue",
	"events": [
		{
		"href":"api/events/30",
		"_type":"ro.allevo.fintpws.resources.EventResource",
		"guid":"30",
		"correlationid":"51363376-3301a556-b7920009",
		"service":3,
		"type":"Error",
		"machine":"fintp-build",
		"eventdate":"2013-01-25 15:44:59.54206",
		"insertdate":"2013-01-25 15:44:59.54206",
		"message":"an error occured",
		"additionalinfo":"the database connection was lost",
		"innerexception":"more details"
		}
	],
	"messagetype":"MT103",
	"sender":"PTSAROAAXXX",
	"receiver":"PTSAROAAXXX",
	"trn":"reference01",
	"payload":"……"
	}
	
#####Messages
Default sort will be by insertdate ( ascending – newest first )
Attribute |Type | Description
--- | --- | ---
total | Number | Returned only if the filter contains “t”
messages | Message-collection	(resource included) | List of messages

Example :
	{
	"href":"api/messages",
	"_type":"ro.allevo.fintpws.resouces.MessagesResource",
	"total":5000,
	"messages":[
		{
		"href":"/api/messages/123456-789012-345678",
		"type":"…",
		"guid":"123456-789012-345678",
		"batchid":"123456-789012-345678",
		…
		},
		{
		"href":"/api/messages/223456-789012-345678",
		"type":"…",
		"guid":"223456-789012-345678",
		"batchid":"223456-789012-345678",
		…
		}
		]
	}

#####Event
Attribute |Type | Description
--- | --- | ---
guid | String | Unique identifier of the event ( Required, Unique)
correlationid | String | Correlation identifier (Required, relation to messages)
service | String | Service reporting the event
type | String | Type of event (Required, one of : info, error, warning, management)
machine | String | Machine where the event originated
eventdate | String | Date/time when the event was generated (local time at origin)
insertdate | String | Date/time when the message arrived (local time at server)
message | String | Message text
additionalinfo | String | More details
innerexception | String | Even more details

Example :

	{
	"href":"api/events/30",
	"_type":"ro.allevo.fintpws.resources.EventResource",
	"guid":"30",
	"correlationid":"51363376-3301a556-b7920009",
	"service":3,
	"type":"Error",
	"machine":"fintp-build",
	"eventdate":"2013-04-19T00:51:12.000Z",
	"insertdate":"2013-04-19T00:51:12.000Z",
	"message":"an error occured",
	"additionalinfo":"the database connection was lost",
	"innerexception":"more details"
	}

#####Events 	
Default sort will be by insertdate ( ascending – newest first )
Attribute |Type | Description
--- | --- | ---
total | Number | Returned only if the filter contains “t”
events | Event-collection	(resource included) | List of events

Example :

	{
	"href":"api/events",
	"_type":"ro.allevo.fintpws.resources.EventsResource",
	"total":5000,
	"events":[
		{
		"href":"api/events/30",
		"_type":"ro.allevo.fintpws.resources.EventResource",
		"guid":"30",
		"correlationid":"51363376-3301a556-b7920009",
		"service":3,
		"type":"Error",
		"machine":"fintp-build",
		"eventdate":"2013-04-19T00:51:12.000Z",
		"insertdate: "2013-04-19T00:51:12.000Z",
		"message":"an error occured",
		"additionalinfo":"the database connection was lost",
		"innerexception":"more details"
		},
		{
		"href":"api/events/40",
		"_type":"ro.allevo.fintpws.resources.EventResource",
		"guid":"40",
		"correlationid":"51363376-3301a556-b7920009",
		"service":3,
		"type":"Info",
		"machine":"fintp-build",
		"eventdate":"2013-01-25T15:44:59.000Z",
		"insertdate":"2013-01-25T15:44:59.000Z",
		"message":"something occured",
		"additionalinfo":"more info",
		"innerexception":"more details"
		}
	}

#####Users

#####UserGroups

#####Connectors

#####RoutingSchema
Attribute |Type | Description
--- | --- | ---
name | String | Name of the routingschema(unique, required)
description | String | Human readable description of the purpose of the routingschema 
sessioncode | String | Payment session code
startlimit | Number | Id of a defined timelimit 
stoplimit | Number | Id of a defined timelimit
isvisivle | String | RoutingSchema visibility 
active | Number | 1 - active ; 0 - inactive

Example

	{
	"href":"api\/routingschemas\/RS1",
	"_type":"ro.allevo.fintpws.resources.RoutingSchemaResource",
	"name":"RS1",
	"active":1,
	"startlimit":"Stoap qPay s23 ",
	"stoplimit":"Start qPay123 ",
	"sessioncode":"0",
	"isvisible":"0",
	"link":{
		"href":"api\/routingschemas\/test\/timelimits",
		"rel":"timelimits"
		}
	}
	
#####RoutingSchemas
Default sort will be by guid ( ascending - A-Z )

Attribute |Type | Description
--- | --- | ---
total | Number | Returned only if the filter contains “t”
routingschemas | RoutingSchema-collection	(resource included) | List of routingschemas

Example


	{
	"href":"api\/routingschemas",
	"_type":"ro.allevo.fintpws.resources.RoutingSchemasResource",
	"routingschemas":[
		{
		"href":"api\/routingschemas\/RS1",
		"_type":"ro.allevo.fintpws.resources.RoutingSchemaResource",
		"name":"RS1",
		"active":234, 
		"startlimit":”Start qPay”,
		"stoplimit":”Stop qPay”, "sessioncode":"0",
		"isvisible":"0",
		"link":{
			"href":"api\/routingschemas\/test\/timelimits",
			"rel":"timelimits"
			}
		},
		{
		"href":"api\/routingschemas\/RS2",
		"_type":"ro.allevo.fintpws.resources.RoutingSchemaResource",
		"name":"RS2",
		"active":234, 
		"startlimit":”Start qPay”,
		"stoplimit":”Stop qPay”, 
		"isvisible":"0",
		"link":{
			"href":"api\/routingschemas\/test\/timelimits",
			"rel":"timelimits"
			}
		}
		]
	}

#####RoutingRule

Attribute |Type | Description
--- | --- | ---
guid | Number | Unique identifier of the routingrule (unique, required)
schema | String | Name of a defined routingschema
description | String | Human readable description of the purpose of the routing rule
queue | String | Name of a defined queue 
sequence | Number | Rule executing order 
msgcond | String | Message type condition
ruletype | Number | 0 – normal; 1 – ran only after schema activation; 2 – rand only after schema deactivation
funccond | String | Function result condition 
metacond | String | Metadata condition 
action | String | Rule action

Example

	{
	"href":"api\/routingrules\/1261",
	"_type":"ro.allevo.fintpws.resources.RoutingRuleResource",
	"guid":1261,
	"queue":"ACHOutQueue",
	"action":"act1",
	"schema":"schema1",
	"sequence":100,
	"ruletype":0
	}
	
#####RoutingRules
Default sort will be by guid ( ascending - A-Z )

Attribute |Type | Description
--- | --- | ---
total | Number | Returned only if the filter contains “t”
routingrules | Routingrule-collection	(resource included) | List of routingrules

Example

	{
	"href":"api\/routingrules",
	"_type":"ro.allevo.fintpws.resources.RoutingRulesResource",
	"routingrules":[
		{
	 	"href":"api\/routingrules\/1261",
	 	"_type":"ro.allevo.fintpws.resources.RoutingRuleResource",
	 	"guid":1262,
	 	"queue":"ACHOutQueue",
	 	"action":"act1",
	 	"schema":"schema1",
	 	"sequence":100,
	 	"ruletype":0
	 	},
		{
	 	"href":"api\/routingrules\/1263",
	 	"_type":"ro.allevo.fintpws.resources.RoutingRuleResource",
	 	"guid":1261,
	 	"queue":"ACHOutQueue",
	 	"action":"act1",
	 	"schemaguid":"schema1",
	 	"sequence":100,
	 	"ruletype":0
	 	}
		]
	}

#####TimeLimit
Attribute |Type | Description
--- | --- | ---
limitname | String | Name of the timelimit (unique, required)
limittime | Date | Time stamp of the timelimit 

Example

	{
	"href":"api\/timelimits\/timelimit",
	"_type":"ro.allevo.fintpws.resources.TimeLimitResource",
	"limitname":"timelimit",
	"limittime":"11:37:04"
	}
	
#####Timelimits
Default sort will be by guid ( ascending - A-Z )

Attribute |Type | Description
--- | --- | ---
total | Number | Returned only if the filter contains “t”
timelimits | Timelimit-collection	(resource included) | List of timelimits

Example

	{
	"href":"api\/timelimits",
	"_type":"ro.allevo.fintpws.resources.TimeLimitsResource",
	"timelimits":[
		{
		"href":"api\/timelimits\/ timelimit1",
		"_type":"ro.allevo.fintpws.resources.TimeLimitResource",
		"limitname":"timelimit1",
		"limittime":"11:37:04"
		},
		{
		"href":"api\/timelimits\/timelimit2",
		"_type":"ro.allevo.fintpws.resources.TimeLimitResource",
		"limitname":"timelimit2",
		"limittime":"11:37:04"
		}
		]
	}

#####RoutingKeyWord
Attribute |Type | Description
--- | --- | ---
guid | Number | Unique identifier of the routing key word (unique, required) 
keyword | Number | Name of the routing keyword 
description | String | Human readable description of the purpose of the routing key word
comparer | String | Data type
selector | String | Regular expression
selectoriso | String | Regular expression (ISO version)

Example

	{
	"href":"api\/routingkeywords\/Amount",
	"_type":"ro.allevo.fintpws.resources.RoutingKeyWordResource",
	"keyword":"Amount",
	"comparer":"string",
	"selector":"(?<value>[\\d,.]{1,})$",
	"description":"Transaction amount",
	"selectoriso":"(?<value>[\\d,.]{1,})"
	}

#####RoutingKeyWords
Default sort will be by keyword ( ascending - A-Z )

Attribute |Type | Description
--- | --- | ---
total | Number | Returned only if the filter contains “t”
routingkeywords | RoutingKeyWord-collection	(resource included) | List of routing key words 

Example

	{
	"href":"api\/routingkeywords",
	"_type":"ro.allevo.fintpws.resources.RoutingKeyWordsResource",
	"has_more":true,
	"routingkeywords":[
		{
		"href":"api\/routingkeywords\/Amount",
		"_type":"ro.allevo.fintpws.resources.RoutingKeyWordResource",
		"keyword":"Amount",
		"comparer":"string",
		"selector":"(?<value>[\\d,.]{1,})$",
		"description":"Transaction amount",
		"selectoriso":"(?<value>[\\d,.]{1,})"
		},
		{
		"href":"api\/routingkeywords\/BenBank",
		"_type":"ro.allevo.fintpws.resources.RoutingKeyWordResource",
		"keyword":"BenBank",
		"comparer":"string",
		"selector":"(?<value>[A-Z 0-9]{8})",
		"description":"Beneficiary bank",
		"selectoriso":"(?<value>[A-Z 0-9]{8})"
		}
	]
	}
	
#####RoutingKeyWordMap
Attribute |Type | Description
--- | --- | ---
mapid | Number | Unique identifier of the routing key word map (unique, required) 
keyword | Number | Routing keyword 
tag | String | Message keyword xpath
mt | String | Message type
selector | String | Selector/seectoriso

Example
	{
	"href":"api\/routingkeywordmaps\/1",
	"_type":"ro.allevo.fintpws.resources.RoutingKeyWordMapResource",
	"mapid":1,
	"keyword":"Sender",
	"tag":"\/\/sg:BasicHeader\/@SenderLT",
	"mt":"103",
	"selector":"selector"
	}
	
#####RoutingKeyWordMaps
Default sort will be by mapid ( ascending )

Attribute |Type | Description
--- | --- | ---
total | Number | Returned only if the filter contains “t”
routingkeywordmaps | RoutingKeyWordMap-collection	(resource included) | List of routing key word maps 

Example

	{
	"href":"api\/routingkeywordmaps",
	"_type":"ro.allevo.fintpws.resources.RoutingKeyWordMapsResource",
	"has_more":true,
	"routingkeywordmaps":[
		{
		"href":"api\/routingkeywordmaps\/1",
		"_type":"ro.allevo.fintpws.resources.RoutingKeyWordMapResource",
		"mapid":1,
		"keyword":"Sender",
		"tag":"\/\/sg:BasicHeader\/@SenderLT",
		"mt":"103",
		"selector":"selector"
		},
		{
		"href":"api\/routingkeywordmaps\/2",
		"_type":"ro.allevo.fintpws.resources.RoutingKeyWordMapResource",
		"mapid":2,
		"keyword":"Receiver",
		"tag":"\/\/sg:ApplicationHeaderInput\/@ReceiverLT|\/\/sg:ApplicationHeaderOutput\/@ReceiverLT",
		"mt":"103",
		"selector":"selector"
	}
	]
	}
	
#####RoutingJob
Attribute |Type | Description
--- | --- | ---
guid | String | Unique identifier of the routing key word map (unique, required) 
status | Number | Job processing state
backout | Number | Number of times this job was tried
priority | Number | Priority of the job
routingpoint | String | Current queue name
function | String | Job action description
userid | Number | User identifier (generating the job)

Example
	{
	"href":"api\/routingjobs\/job",
	"_type":"ro.allevo.fintpws.resources.RoutingJobResource",
	"guid":"job",
	"status":1,
	"backout":0,
	"priority":1,
	"routingpoint":"AchQueue",
	"userid":"1"
	}
	
#####RoutingJobs
Default sort will be by guid ( ascending )
Attribute |Type | Description
--- | --- | ---
total | Number | Returned only if the filter contains “t”
routingjobs | RoutingJob-collection	(resource included) | List of routing jobs 

Example

	{
	"href":"api\/routingjobs",
	"_type":"ro.allevo.fintpws.resources.RoutingJobsResource",
	"routingjobs":[
		{
		"href":"api\/routingjobs\/job",
		"_type":"ro.allevo.fintpws.resources.RoutingJobResource",
		"guid":"job",
		"status":1,
		"backout":0,
		"priority":1,
		"routingpoint":"AchQueue",
		"userid":"1"
		}
	]}
	
#####ServicePerformance
Attribute |Type | Description
--- | --- | ---
serviceid | Number | Unique identifier of the service performance (unique, required)
insertdate | Timestamp | Date of creation 
mintransactiontime | Number | Shortest interval (time) it took to process a message (in ms)
maxtransactiontime | Number | Longest interval (time) it took to process a message (in ms)
meantransactiontime | Number | Average transaction time 
sequenceno | Number | Used when building the batch identifier
ioidentifier | Number | Input / Output service type
sessionid | Number | Session identifier 
commitedtrns | Number | Number of successful transactions performed by the service
abortedtrns | Number | Number of failed transactions performed by the service

Example

	{
	"href":"api\/serviceperformances\/244 ",
	"_type":"ro.allevo.fintpws.resources.ServicePerformanceResource",
	"serviceid":244,
	"insertdate":"2013-07-02T13:46:44.000Z",
	"mintransactiontime":12434,
	"maxtransactiontime":23231,
	"meantransactiontime":31314,
	"sequenceno":231,
	"ioidentifier":232,
	"sessionid":232,
	"commitedtrns":32314,
	"abortedtrns":1234
	}

#####ServicePerformances
Default sort will be by serviceid ( ascending)

Attribute |Type | Description
--- | --- | ---
total | Number | Returned only if the filter contains “t”
serviceperformances | ServicePerformance-collection	(resource included) | List of service performances 

Example

	{
	"href":"api\/serviceperformances",
	"_type":"ro.allevo.fintpws.resources.ServicePerformancesResource",
	"serviceperformances":[
		{
		"href":"api\/serviceperformances\/244 ",
		"_type":"ro.allevo.fintpws.resources.ServicePerformanceResource",
		"serviceid":244,
		"insertdate":"2013-07-02T13:46:44.000Z",
		"mintransactiontime":12434,
		"maxtransactiontime":23231,
		"meantransactiontime":31314,
		"sequenceno":231,
		"ioidentifier":232,
		"sessionid":232,
		"commitedtrns":32314,
		"abortedtrns":1234
		}
	]
	}

#####Alerts

#####Versions

#####Logs


####REST metadata
In addition to exposing application data, resources also include other information that is specific to the RESTful API. Such information includes URLs and relationships.

The following table lists generic attributes that are defined and have a specific meaning on all resources. They should not be used for mapping application model attributes.

Attribute |Type | Description
--- | --- | ---
_type | String | resourcetype ( usually name of the resource ) or resourcetype-collection ( 
href | String | Identifies the URL of the current resource. (required)
link | Object | Identifies a relationship for a resource. This attribute is itself an object and has "rel" "href" attributes.
has_more | Boolean | Set for collections if the total number of items exceeds maximum page_size ( see Ranges/Pagination )
total | Number | Will hold the total number of items for a requested resource. For performance reasons it will not be returned unless requested in a fiter ( see Filters )

###Representations
We have defined resources, and defined the data associated with them in terms of the JSON data model. However, these resources are still abstract entities. Before they can be communicated to a client over an HTTP connection, they need to be serialized to a textual representation. This representation can then be included as an entity in an HTTP message body.

The following representations are common for resources. The table also lists the appropriate content-type to use:

Type	Content-Type
JSON	application/ json
XML	application/xml
HTML	text/html

####JSON
JSON (which stands for JavaScript Object Notation) is an alternative to XML. It gets its name from the fact that its data format resembles JavaScript objects, and it is often more succinct than the equivalent XML.
Formatting a resource to JSON is trivial because the data model of a resource is defined in terms of the JSON model. Below we give an example of a JSON serialization of a virtual machine:

	{
	  "_type": "vm",
	  "name": "A virtual machine",
	  "memory": 1024,
	  "cpu": {
	    "cores": 4,
	    "speed": 3600
	  },
	  "boot": {
	    "devices": ["cdrom", "harddisk"]
	  }
	}

####XML
XML is the most complex representation format due to both its complexity as well as its limitations. I recommend the following rules:

-	Resources are mapped to XML elements with a tag name equal to the resource type.
-	Attributes of resources are mapped to XML child elements with the tag name equal to the attribute name.
-	Scalar values are stored as text nodes. A special "type" attribute on the containing element should be used to refer to an XML Schema Part 2 type definition.
-	Lists should be stored as a single container element with child elements for each list item. The tag of the container element should be the English plural of the attribute name. The item tag should be the English singular of the attribute name. Lists should have the "xd:list" type annotation.
The same VM again, now in XML:

	<vm xmlns:xs="http://www.w3.org/2001/XMLSchema">
	  <name type="xs:string">My VM</name>
	  <memory type="xs:int">1024</memory>
	  <cpu>
	    <cores type="xs:int">4</cores>
	    <speed type="xs:int">3600</speed>
	  </cpu>
	  <boot>
	    <devices type="xs:list">
	      <device type="xs:string">cdrom</device>
	      <device type="xs:string">harddisk</device>
	    </devices>
	  </boot>
	</vm>

###Content types
Generic content types will follow the format "application/format".
Clients can express their preference for a certain representation format using the HTTP "Accept" header. The HTTP RFC defines an elaborate set of rules in which multiple formats can be requested, each with its own priority. In the following example, the client tells the API that it accepts only JSON input:

GET /api/collection
Accept: application/json
 
##3.	URLs
Entry point
A RESTful API needs to have one and exactly one entry point. The URL of the entry point needs to be communicated to API clients so that they can find the API.

Technically speaking, the entry point can be seen as a singleton resource that exists outside any collection. It is common for the entry point to contain some or all of the following information:
-	Information on API version, supported features, etc.
-	A list of top-level collections.
-	A list of singleton resources.
-	Any other information that the API designer deemed useful, for example a small summary of operating status, statistics, etc.

Request:

	GET / fintp/api HTTP/1.1
	Accept: application/xml


Server response:

	200 OK
	Content-Type: application/xml
	
	<api collection='fintp' version='0.1.0'>
	  <link href = ’/messages' rel='messages'></link>
	  <link href=’/events' rel='events'></link>
	  <link href=’/queues' rel='queues'></link>
	  <link href=’/users' rel='users'></link>
	  <link href=’/queues' rel='queues'></link>
	  <link href=’/users' rel='users'></link>
	  <link href=’/usergroups' rel='usergroups'></link>
	  <link href=’/connectors' rel='connectors'></link>
	  <link href=’/routingschemas' rel='routingschemas'></link>
	  <link href=’/routingrules' rel='routingrules'></link>
	  <link href=’/timelimits' rel='timelimits'></link>
	  <link href=’/alerts' rel='alerts'></link>
	  <link href=’/versions' rel='versions'></link>
	  <link href=’/logs' rel='logs'></link>
	
	  <link href=’/images' rel='images'>
	    <feature name='owner_id'></feature>
	  </link>
	  <link href='http://localhost:3001/api/instances' rel='instances'>
	    <feature name='user_data'>
	    </feature>
	    <feature name='authentication_key'>
	    </feature>
	  </link>
	  <link href='http://localhost:3001/api/keys' rel='keys'></link>
	</api>


###Versioning

###URL structure
Each collection and resource in the API has its own URL. URLs should never be constructed by an API client. Instead, the client should only follow links that are generated by the API itself.

The recommended convention for URLs is to use alternate collection / resource path segments, relative to the API entry point. This is best described by example. The table below uses the ":name" URL variable style from Rail's "Routes" implementation.

###URL	Description
/api	The API entry point
/api/:coll	A top-level collection named "coll"
/api/:coll/:id	The resource "id" inside collection "coll"
/api/:coll/:id/:subcoll	Sub-collection "subcoll" under resource "id2"
/api/:coll/:id/:subcoll/:subid	The resource "id2" inside "subcoll"

Even though sub-collections may be arbitrarily nested, you want to keep the depth limited to 2, if possible. Longer URLs are more difficult to work with when using simple command-line tools like curl.

It is strongly recommended that the URLs generated by the API should be absolute URLs.

###URLs for resources
/api entry point excluded for clarity

URL | Description
--- | ---
/messages | A collection of all messages
/messages/{id} | A message with guid = {id} inside the messages collection
/events | A collection of all events
/events/{id} | An event with guid = {id} inside the events collection
/queues | A collection of all queues
/queues/{name} | A queue with name={name} inside the queues collection
/users | A collection of all users
/users/{name} | A user with name = {name} inside the users collection
/events | A collection of all events
/events/{id} | An event with id = {id} inside events collection
/histories | A collection of all history payments (??)
/histories/{id} | A history payment with id = {id}
/roles | A collection of all roles
/roles/{name} | A role with name = {name}
/routingjobs | A collection of routing jobs
/routingjobs/{id} | A routing job with id = {id}
/routingkeywordmaps | A collection of routing keyword maps
/routingkeywordmaps/{id} | A routing keyword map with id = {id}
/routingkeywords | A collection of routing keywords
/routingkeywords/{name} | A routing keyword with name = {name}
/routingschemas | A collection of routing schemas
/routingschemas/{id} | A routing schema with id = {id}
/routingrules | A collection of routing rules
/routingrules/{id} | A routing rule with id = {id}
/timelimits | A collection of time limits
/timelimits/{name} | A time limit with name = {name}
/queuetypes | A collection of queue types
/queuetypes/{name} | A queue type with name = {name}
/queueactions | A collection of queue actions
/queueactions/{name} | A queue action with name = {name}
/servicemaps | A collection of service maps
/servicemaps/{name} | A service map with friendly name = {name}
/serviceperformances | A collection of service performances
/serviceperformances/{id} | A service performance with id={id} 
 
##4.	Methods
###Standard methods
Methods can be executed on resources via their URL.
The table below lists the standard methods that have a well-defined meaning for all resources and collections.

Method | Scope | Semantics
--- | --- | ---
GET | collection | Retrieve all resources in a collection
GET | resource | Retrieve a single resource (retrieve information from the specified source)
HEAD | collection | Retrieve all resources in a collection (header only)
HEAD | resource | Retrieve a single resource (header only)
POST | collection | Create a new resource in a collection (sends new information to the specified source.)
PUT | resource | Update a resource (updates existing information of the specified source)
PATCH | resource | Update a resource
DELETE | resource | Delete a resource (removes existing information from the specified source)
OPTIONS	any	Return available HTTP methods and other options

Normally, not all resources and collections implement all methods. There are two ways to find out which methods are accepted by a resource or collection.

Use the OPTIONS method on the URL, and look at the "Allow" header that is returned. This header contains a comma-separated list of methods are are supported for the resource or collection.

Just issue the method you want to issue, but be prepared for a "405 Method Not Allowed" response that indicates the method is not accepted for this resource or collection.

Some common HTTP status codes
Status Range | Description | Examples
--- | --- | ---
100 | Informational | 100 Continue
200 | Successful | 200 OK
201 | Created | 
202 | Accepted | 
300 | Redirection | 301 Moved Permanently
304 | Not Modified | 
400 | Client error | 401 Unauthorized
402 | Payment Required | 
404 | Not Found | 
405 | Method Not Allowed | 
500 | Server error | 500 Internal Server Error
501 | Not Implemented	

###Asynchronous Requests
The response entity of a 202 Accepted response should be a regular resource with only the information filled in that was available at the time the request was accepted. The resource should contain a "link" attribute that points to a status monitor that can be polled to get updated status information.

When polling the status monitor, it should return a "response" object with information on the current status of the asynchronous request. If the request is still in progress, such a response could look like this (in JSON):

	{
	code: 202,
	progress: “50%”
	}

If the call has finished, the response should include the same headers and response body had the request been fulfilled synchronously:

	{
	code: “201”,
	message:”created”
	}


###Ranges/Pagination
The number of objects in a collection is limited to a maximum page_size of 100.
When collections contain many resources, it is quite a common requirement for a client to retrieve only a subset of the available resources. This will be implemented using a pair of query string params:

Filter | Meaning
--- | ---
?page=n | Requests the nth page of results. Default 1=first page
?page_size=n | Requests n results on page. Default 100. Limits: 1-100.

An example that would return resources 100 through 199 (inclusive): 

	GET /api/collection?page=2 
	
or 

	GET /api/collection?page=2&page_size=100

Note that it is the responsibility of the API implementer to ensure a proper and preferably meaningful ordering can be guaranteed for the resources.

When the number of results exceeds page_size, the has_more metadata will be present in the response and it will have a true value.

###Filters
Filters are appended to the query string in the form of ?filter={filter combination separated by comma}
For now, filters will be defined below and used as clear text. It may be possible to require shortening in the future

Filter | Meaning
--- | ---
?filter=t | Returns the total metadata
?filter=b | Returns the body 

###Error handling
API responses will always return a consistent HTTP return code as well as a code and response message within the actual response body.
API responses will have the following format ( JSON) :

	{
	“code”:{http status},
	“message”:”{error message}”,
	“id”:”{id}”
	}
	
Error message will be verbose and use plain language descriptions. Add as many hints as you can think of about what's causing an error.

{Id } will be filled with an id generated by the API, that can be used to track the request ( usually successful create requests ).

Code 200 ( OK) will be returned for successful requests.
Code 201 (Created) will be returned for POST methods when the creation was successful.
Error code 400 (Bad request) will be returned for methods that throw errors due to parameters received being incorrectly formatted.
Error code 404 (Not found) will be returned for methods using identifiers {id}/{name} that do not exist
Error code 405 (Method not allowed) will be returned for calling a method that doesn’t exist
Error code 409 (Conflict) will be returned for PUT methods that have conflicting ids ( pathparam, entity id ) and POST methods when a key is violated
Error code 500 (Internal server error) will be returned for API errors related to the server

See more on [wikipedia](http://en.wikipedia.org/wiki/List_of_HTTP_status_codes)

###Methods for URLs
/api entry point removed for clarity 


URL | POST | GET | PUT | DELETE
--- | --- | --- | --- | ---
/messages | Create a message | List messages | Bulk update messages | Error 405 ( no method )
/messages/{id} | Error 405 ( no method ) | Show message with guid = {id} | Update message with guid = {id} | Error 405 ( no method )
/events | Create an event | List events | Error 405 ( no method ) | Error 405 ( no method )
/events/{id} | Error 405 ( no method ) | Show event with guid={id} | Error 405 ( no method ) | Error 405 ( no method )
/queues | Create a queue | List queues | Error 405 ( no method ) | Error 405 ( no method )
/queues/{name} | Error 405 ( no method ) | Show queue with name = {name} | Update queue with name = {name} | Delete queue with name = {name}
/users | Create a user | List users | Error 405 ( no method ) | Error 405 ( no method )
/users/{name} | Error 405 ( no method ) | Show user with name = {name} | Update user with name = {name} | Delete user with name = {name}
/roles | Create a role | List roles | Error 405 ( no method ) | Error 405 ( no method )
/roles/{name} | Error 405 ( no method ) |  Show role with name={name} | Update role with name={name} | Delete role with name={name}
/users/{username}/roles | Map a new role to user with name  {username} | List roles that user with name = {username} has | Error 405 ( no method ) | Error 405 ( no method )
/users/{username}/roles/{rolename} | Error 405 ( no method ) | Show role with name={rolename} | Error 405 ( no method ) | Delete mapping ({username},{rolename})
/roles/{rolename}/mappings | Create a new queue-role mapping | List mappings | Error 405 ( no method ) | Error 405 ( no method )
/roles/{rolename}/mappings/{queuename} | Error 405 ( no method ) | Show mapping with rolename= {rolename} and queuename={queuename} | Update the  mapping with rolename= {rolename} and queuename={queuename} | Delete mapping ({rolename}{queuename}) 

##5.	Relationships
Resources do not exist in isolation, but have relationships to other other resources. Sometimes these relationships exist between the mapped objects in the application data model as well, sometimes they are specific to the RESTful resources.
One of the principles of the RESTful architecture style is that these relationships are expressed by hyperlinks to the representation of a resource.

For the sake of simplicity, resource relations identified in the application data model will be modeled as URLs following this model : {resource}/{identifier}/{resource}.

URLs can go as deep as necessary, but above two levels, the complexity should be moved behind the ‘’?’ query string. ( e.g. events?source=RoutingEngine )

###URLs for relationships
Relationship | Semantics
--- | ---
/messages/{id}/events | Link to a related collection of events specific to the message with guid={id}
/queues/{name}/messages | Link to a related collection of messages located in a queue with name={name}
/users/{username}/roles | Link to a related collection of roles belonging to user with username={username}
/roles/{rolename}/mappings | Link to a related collection of queue-role mappings having rolename = {rolename}
/routingkeywordmaps/{map}/
routingkeywords | Link to a related collection of routing key words mapped to {map}
/routingschemas/{schema}/
timelimits | Link to a related collection of time limits that define the start and stop limit for the routing schema having name = {schema} 
/routingschemas/{schema}/routingrules | Link to a related collection of routing rules belonging to the routing schema having name = {schema}
 
##6.	Security

###Authentication

An API key grants you access to a particular API and identifies you to the API, which helps the API provider keep track of how their service is used and prevent unauthorized or malicious activity.
Basic authentication is currently used. 
The following scenario takes place when an unauthenticated client tries to access a resource that requires authentication (e.g. api/queues). Also, we will asume that the first credentials sent are valid(admin/admin).


Authentication scenario
Request:

	GET /api/queues

Response:

	201(Unauthorized)
	WWW-Authenticate: Basic realm=”ro.allevo.fintpws”

Request:

	GET /api/queues
	Authorization: Basic YWRtaW46YWRtaW4=

Request:

	GET /api/queues/{queueid}
	Authorization: Basic YWRtaW46YWRtaW4=


In the previous example YWRtaW46YWRtaW4= is the string “admin:admin” encoded using Base64
Thus, the username and password are not encrypted or hashed in any way. Therefore the authentication will be used over HTTPS.
The Basic Authentication header is sent with each HTTP request, therefore the browsers cache the credentials for a period to avoid prompting the user for the credentials at each request.

###Authorization

On most resources (all except messages and their relationships containing messages) the authorization rules are following:

•	GET method is allowed to be called by any authenticated user
•	POST, PUT, DELETE are allowed to be called only by users having “Administrator” role

On messages resource and other relations including messages (e.g. /messages/{id}/events), the following rules take place:

•	On routed messages (that don’t belong to any queue) the user must have “Reports” role in order to access messages
•	On other messages (belonging to a specific queue) access is granted this way (considering the message belongs is in {queue}):
o	GET method is allowed if the current user has a role which is mapped to {queue} with “READ”  action type
o	POST, PUT, DELETE methods are allowed if the current user has a role which is mapped to {queue} with “WRITE”  action type

##7.	API usage scenarios

Queues screen

Get a list of queues
Request:

	GET /api/queues

Response:

	200(OK)
	Content-Type:application/json
	{
		“_type”:”queues-collection”,
		“href”:”/queues”,
		“queues”:[
		{“name”:”RTGSOutQueue”},
		{“name”:”ACHOutQueue”},
		{“name”:”RTGSInQueue”}
		]
	}

Get the RTGSOutQueue queue details ( if needed )

Request:

	GET /api/queues/RTGSOutQueue

Response:

	200(OK)
	Content-Type:application/json
	{
		“_type”:”queues”,
		“href”:”/ queues/RTGSOutQueue”,
		“name”:”RTGSOutQueue”,
		“type”:”normal”,
		“messages-rel”:”/queue/RTGSOutQueue/messages”
	}

Get the total number of messages

Request:

	GET /api/queues/RTGSOutQueue/messages?filter=t
	Range: resources=0-0

Response:

	Content-Type:application/json
	{
		“_type”:”messages”,
		“href”:”/ queues/RTGSOutQueue/messages”,
		“total”:”43”,
		“messages”:[
		]
	}
	
##8.	Coding guidelines 
###Exception handling
Exceptions will be intercepted by custom exceptionmappers in order to return the above define status codes and messages.

Exceptions related to data format ( NumberFormatException/JSONException/… ) for data received from the client will send a response with code INTERNAL_SERVER_ERROR

Exceptions related to data format ( NumberFormatException/JSONException/… ) for data sent from the server will send a response with code BAD_REQUEST

- prinde doar exceptiile checked ( declarate cu throws in metodele folosite ) si doar pentru a le impacheta in exceptii care au sens pt. nivelul superior sau daca pot fi tratate ( ex : reincercat operatia, returnat un cod specificat in doc. design ).
- nu prinde exceptiile generice gen PersistenceException, Exception, etc. ( nu o poti trata intr-un mod care sa fie tot timpul corect pt. ca o exceptie generica poate semnifica o multitudine de exceptii posibile ).
	ex : trebuie sa returnam 404 pt. exceptia specifica "not found", nu 500 pt. orice persistenceexception.
- nu arunca exceptii generice ( nau ofera nici o informatie despre ce a mers gresit ).
- nu folosi printStackTrace(). system.out si system.err nu sunt monitorizate in productie si sunt oricum pline de alte erori. 
- nu ascunde exceptiile ( catch fara throw ). Desi pot exista cazuri cand este ok de folosit, in general e gresit. Returneaza null in cazuri normale ce pot fi verificate ( de ex. param. de intrare e null, rezultatul e null ), altfel arunca exceptie si lasa codul de la nivelul superior sa o trateze.
- codurile de eroare care trebuie returnate sunt specificate in doc. design.
- anumite exceptii returneaza coduri diferite in functie de metoda folosita ( ex. GET + jsonexception -> INTERNAL_SERVER_ERROR, PUT + jsonexception -> BAD_REQUEST ). Am actualizat doc. design cu ele.
- mesajele din exceptiile prinse ocnsecutiv trebuie sa fie "asemanatoare" : operatie cu exceptie + informatie suplimentara ( ex, failed to update q : json, failed to update q : number format, failed to update q : database errror ... )

###Coding conventions

- comenteaza codul si recomenteaza-l daca ai schimbat semnatura metodei ( de ex, numarul de param ) pt. a nu desincroniza documentatia de cod.
- foloseste un stil de scris codul si foloseste-l peste tot. Daca ti se pare prea migalos, foloseste optiunea Source->Format din eclipse inainte de a comite codul.
- daca faci copy paste la cod, uita-te atent la mesajele folosite si vezi daca sunt relevante ( ex. mesaj exceptie la update in operatia de create ). 

##9.	References
https://github.com/geertj/restful-api-design
http://piwik.org/blog/2008/01/how-to-design-an-api-best-practises-concepts-technical-aspects/
http://info.apigee.com/Portals/62317/docs/web%20api.pdf
http://www.websanova.com/tutorials/web-services/how-to-design-a-rest-api-and-why-you-should#.URtPDqV8nng
http://msdn.microsoft.com/en-us/library/dd203052.aspx


