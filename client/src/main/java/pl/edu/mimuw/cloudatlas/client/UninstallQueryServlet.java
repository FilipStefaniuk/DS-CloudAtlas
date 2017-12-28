package pl.edu.mimuw.cloudatlas.client;

import pl.edu.mimuw.cloudatlas.agent.AgentRMIInterface;
import pl.edu.mimuw.cloudatlas.model.Attribute;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

@WebServlet("/ajax/uninstallquery")
public class UninstallQueryServlet extends HttpServlet{

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        String name = request.getParameter("name");

        if (name == null)
            throw new ServletException();

        try {
            Registry registry = LocateRegistry.getRegistry("localhost", 1324);
            AgentRMIInterface stub = (AgentRMIInterface) registry.lookup("Agent");
            stub.uninstallQuery(new Attribute('&' + name));

        } catch (NotBoundException e) {
            throw new ServletException();
        }
    }
}
