//package pl.edu.mimuw.cloudatlas.client;
//
//import com.google.gson.Gson;
//import pl.edu.mimuw.cloudatlas.agent.AgentInterface;
//import pl.edu.mimuw.cloudatlas.model.PathName;
//
//import javax.servlet.ServletException;
//import javax.servlet.annotation.WebServlet;
//import javax.servlet.http.HttpServlet;
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;
//import java.io.IOException;
//import java.rmi.NotBoundException;
//import java.rmi.registry.LocateRegistry;
//import java.rmi.registry.Registry;
//import java.util.ArrayList;
//import java.util.Collections;
//import java.util.List;
//import java.util.Set;
//
//@WebServlet("/ajax/agent_zones")
//public class ListAgentZonesServlet extends HttpServlet {
//
//    @Override
//    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
//        try {
//            Registry registry = LocateRegistry.getRegistry("localhost");
//
//            AgentInterface stub = (AgentInterface) registry.lookup("Agent");
//            Set<PathName> names = stub.getAgentZones();
//            List<String> result = new ArrayList<>();
//            for(PathName name : names) {
//                result.add(name.getName());
//            }

////            Collections.sort(result);
//            response.setContentType("text/plain");
//            response.setCharacterEncoding("UTF-8");
//            response.getWriter().write(new Gson().toJson(names));
//
//        } catch (NotBoundException e) {
//            throw new ServletException();
//        }
//    }
//}
