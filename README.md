fintp_api
=========

FinTP RESTful api

Facts
-----
- version: 0.0.1

Requirements
------------
- Latest JDK
- Apache Tomcat 7
- Maven

Issues
------
- Due the binary license there is no public repository that contains the Oracle Driver JAR. 
- Solutions:
	1. Comment dependency if you are using Postgres database
	2. Download ojdbc jar from Oracle site and install it to your local repository (mvn install:install-file -Dfile={Path_to_your_ojdbc6.jar} 
	-DgroupId=com.oracle -DartifactId=ojdbc6\ -Dversion=11.2.0.3.0 -Dpackaging=jar -Dfile=ojdbc.jar -DgeneratePom=true )

License
-------
- [GPLv3](http://www.gnu.org/licenses/gpl-3.0.html)

Copyright
---------
FinTP - Financial Transactions Processing Application
Copyright (C) 2013 Business Information Systems (Allevo) S.R.L.

