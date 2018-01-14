package pl.edu.mimuw.cloudatlas.querySigner;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import pl.edu.mimuw.cloudatlas.interpreter.Interpreter;
import pl.edu.mimuw.cloudatlas.model.Attribute;
import pl.edu.mimuw.cloudatlas.model.ValueQuery;

import java.rmi.RemoteException;
import java.security.PrivateKey;

public class QuerySigner implements QuerySignerInterface {

    private static final Logger LOGGER = LogManager.getLogger(QuerySigner.class);

    private PrivateKey privateKey;

    public QuerySigner(PrivateKey privateKey) {
        this.privateKey = privateKey;
    }

    @Override
    public ValueQuery signQuery(String name, String queryString) throws RemoteException {
        try {
            LOGGER.debug("SIGN_QUERY: " + name + ": " + queryString);

            if(!Attribute.isQuery(new Attribute(name))) {
                throw new IllegalArgumentException();
            }

            if(!queryString.equals("")) {
                Interpreter.parseProgram(queryString);
            }

            ValueQuery query = new ValueQuery(name, queryString);
            query.sign(privateKey);

            return query;

        } catch (Exception e) {
            LOGGER.error("SIGN_QUERY: " + e.getMessage(), e);
            throw new RemoteException("Failed to sign query");
        }
    }
}
