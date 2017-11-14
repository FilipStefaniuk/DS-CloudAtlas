<%@ page import="java.rmi.registry.Registry" %>
<%@ page import="java.rmi.registry.LocateRegistry" %>
<%@ page import="pl.edu.mimuw.cloudatlas.agent.AgentInterface" %>
<%@ page import="pl.edu.mimuw.cloudatlas.model.PathName" %>
<%@ page import="java.util.Set" %>
<html>
<head>
    <script src="https://ajax.googleapis.com/ajax/libs/jquery/3.2.1/jquery.min.js"></script>
    <script src="static/js/test.js"></script>
</head>
<body>
<h1>CloudAtlas</h1>
<%
    Registry registry = LocateRegistry.getRegistry("localhost");
    AgentInterface stub = (AgentInterface) registry.lookup("Agent");
    Set<PathName> names = stub.getAgentZones();
%> <select id="zones"> <%
    for(PathName name : names) {
        %> <option value="<%=name.getName()%>"><%=name.getName()%></option><%
    }
%> </select>
<h3> Attributes </h3>
<div id="attributes"></div>
<h3> Queries </h3>
<div id=""queries"></div>
</body>
</html>
