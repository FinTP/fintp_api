<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>FinTP REST API</title>
</head>
<body>
<div>This is the entry point for the FinTP API</div>
	<b>General</b>
	<br />
	<p>All API responses are JSON</p>
	<p>The number of objects in a collection is limited to a maximum page_size of 100.A custom page_size query parameter may be specified to alter the number of results being returned in one page.A custom page query parameter may be specified in order to retrieve a specific page.</p>
	<h1>
		<a href="queues">Queues</a>
	</h1>
	Provides access to queues
	<h2>POST</h2>
	Create a queue
	<h2>GET</h2>
	List queues
	<h1>
		<a href="queues/{name}">Queue</a>
	</h1>
	Provides access to a queue named {name}
	<h2>GET</h2>
	Show queue with name = {name}
	<h2>PUT</h2>
	Update queue with name = {name}
	<h2>DELETE</h2>
	Delete queue with name = {name}
	<h1>
		<a href="messages">Messages</a>
	</h1>
	Provides access to messages
	<h2>POST</h2>
	Create a message
	<h2>GET</h2>
	List messages
</body>
</html>