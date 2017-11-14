package pl.edu.mimuw.cloudatlas.client;

import com.google.gson.Gson;
import pl.edu.mimuw.cloudatlas.agent.AgentInterface;
import pl.edu.mimuw.cloudatlas.model.Attribute;
import pl.edu.mimuw.cloudatlas.model.AttributesMap;
import pl.edu.mimuw.cloudatlas.model.PathName;
import pl.edu.mimuw.cloudatlas.model.Value;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.HashMap;
import java.util.Map;

@WebServlet("/ajax/attributes")
public class ListAttributesServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        String name = request.getParameter("name");

        if (name == null)
            throw new ServletException();

        try {
            Registry registry = LocateRegistry.getRegistry("localhost");
            AgentInterface stub = (AgentInterface) registry.lookup("Agent");
            AttributesMap attributes = stub.getAttributes(new PathName(name));

            Map<String, String> data = new HashMap<>();
            for(Map.Entry<Attribute, Value> entry : attributes) {
                data.put(entry.getKey().getName(), entry.getValue().toString());
            }

            response.setContentType("text/plain");
            response.setCharacterEncoding("UTF-8");
            String json = new Gson().toJson(data);
            response.getWriter().write(json);

        } catch (NotBoundException e) {
            throw new ServletException();
        }
    }
}
