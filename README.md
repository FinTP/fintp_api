fintp_api
=========

FinTP RESTful API. The API enables 3rd party applications to communicate with the FinTP central repository of messages.

Facts
-----
- version: 0.0.1

Building
-----
See [Build Instructions](https://github.com/FinTP/fintp_api/wiki/Build-instructions) for instructions on how to build the API.

Usage
-----
See [Usage](https://github.com/FinTP/fintp_api/wiki/Usage) for a list of usage scenarios.

Installation
-----
See [Installation](https://github.com/FinTP/fintp_api/wiki/Installation) for steps required to install and configure the API.

Contributing
-----
See [How To Contribute](http://www.fintp.org/how-to-contribute) for a list of areas where help is needed.

Requirements
------------
- Latest JDK
- Apache Tomcat 7
- Maven

Running the tests
-----
See [Running the Tests](https://github.com/FinTP/fintp_api/wiki/Running-the-tests) for steps required to run the automated tests.

Issues
-----
- Due the binary license there is no public repository that contains the Oracle Driver JAR. 
- Solutions:
	1. Comment dependency if you are using Postgres database
	2. Download ojdbc jar from Oracle site and install it to your local repository (mvn install:install-file -Dfile={Path_to_your_ojdbc6.jar} 
	-DgroupId=com.oracle -DartifactId=ojdbc6\ -Dversion=11.2.0.3.0 -Dpackaging=jar -Dfile=ojdbc.jar -DgeneratePom=true )

License
-----
- [GPLv3](http://www.gnu.org/licenses/gpl-3.0.html)

Copyright
-----
COPYRIGHT.  Copyright to the SOFTWARE is owned by ALLEVO and is protected by the copyright laws of all countries and through international treaty provisions. 
NO TRADEMARKS.  Customer agrees to remove all Allevo Trademarks from the SOFTWARE (i) before it propagates or conveys the SOFTWARE or makes it otherwise available to third parties and (ii) as soon as the SOFTWARE has been changed in any respect whatsoever. 
