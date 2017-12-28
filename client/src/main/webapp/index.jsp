<%@ page import="java.rmi.registry.Registry" %>
<%@ page import="java.rmi.registry.LocateRegistry" %>
<%@ page import="pl.edu.mimuw.cloudatlas.agent.AgentRMIInterface" %>
<%@ page import="pl.edu.mimuw.cloudatlas.model.PathName" %>
<%@ page import="java.util.Set" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <title>CloudAtlas</title>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap.min.css">
    <script src="https://ajax.googleapis.com/ajax/libs/jquery/3.2.1/jquery.min.js"></script>
    <script src="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/js/bootstrap.min.js"></script>
    <script type="text/javascript" src="static/js/smoothie.js"></script>
    <script src="static/js/functions.js"></script>
</head>
<body>

<%--Header--%>
<div class="jumbotron text-center">
    <h1>CloudAtlas</h1>
</div>
<div class="container">
    <div class="row">
        <div class="col-sm-3">

            <%--Select Zone--%>
            <%
                Registry registry = LocateRegistry.getRegistry("localhost");
                AgentRMIInterface stub = (AgentRMIInterface) registry.lookup("Agent");
                Set<PathName> names = stub.getAgentZones();
            %> <select id="zones" class="form-control selectWidth"> <%
                for(PathName name : names) {
                    %> <option value="<%=name.getName()%>"><%=name.getName()%></option><%
                }
            %> </select>
        </div>
    </div>

    <%--Attributes --%>
    <div class="row">
        <div class="col-sm-4">
            <h3 class="text-center">Attributes</h3>
            <table id="attributes-table" class="table table-hover"></table>
        </div>
        <div class="col-sm-8" id="chart-div">
            <h3 id="chart-title" class="text-center">Choose Attribute</h3>
            <div>
                <canvas id="chart-canvas" style="width:100%; height:100%"></canvas>
            </div>
        </div>
    </div>

    <%--Queries--%>
    <div class="row">
        <h3 class="text-center"> Queries </h3>
        <table class="table table-striped table-hover" id="queries-table"></table>
        <h4>Install Query</h4>
        <form action="javascript:installQuery()" id="install_query" class="form-inline">
            <div class="form-group">
                <label>Name:</label>
                <input name="name" id="query-name">
            </div>
            <div class="form-group">
                <label>Query:</label>
                <input class="form-control" id="query-value" name="value">
            </div>
            <button class="btn btn-default">Install</button>
        </form>
    </div>

    <%--Footer--%>
    <div class="row">
        <hr>
        <div class="col-lg-12">
            <div class="col-md-8">
            </div>
            <div class="col-md-4">
                <p class="muted pull-right">by Filip Stefaniuk</p>
            </div>
        </div>
    </div>
</div>
</body>
</html>
