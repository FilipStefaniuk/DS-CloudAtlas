package pl.edu.mimuw.cloudatlas.agent.modules;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cfg4j.provider.ConfigurationProvider;
import pl.edu.mimuw.cloudatlas.agent.framework.*;
import pl.edu.mimuw.cloudatlas.model.Attribute;
import pl.edu.mimuw.cloudatlas.model.AttributesMap;
import pl.edu.mimuw.cloudatlas.model.Value;
import pl.edu.mimuw.cloudatlas.model.ValueQuery;

import java.io.InputStream;
import java.security.KeyStore;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.util.Map;

@Module(value = "VerifierModule", dependencies = {"ZMIModule"})
public class VerifierModule extends ModuleBase{

    private final static Logger LOGGER = LogManager.getLogger(VerifierModule.class);

    public static final int VERIFY_QUERIES = 1;

    private PublicKey publicKey;

    @Handler(VERIFY_QUERIES)
    private final MessageHandler<?> h1 = new MessageHandler<GenericMessage<AttributesMap>>() {
        @Override
        protected void handle(GenericMessage<AttributesMap> message) {
            try {
                LOGGER.debug("VERIFY_QUERIES: IN: " + message.toString());

                AttributesMap verifiedQueries = new AttributesMap();

                for (Map.Entry<Attribute, Value> entry : message.getData()) {
                    if (Attribute.isQuery(entry.getKey())) {
                        ValueQuery query = (ValueQuery) entry.getValue();
                        if (query.getName().equals(entry.getKey().getName())
                                && !query.getName().equals(ZMIModule.Q_CONTACTS)
                                && !query.getName().equals(ZMIModule.Q_NMEMBERS)
                                && query.verify(publicKey)) {
                            verifiedQueries.addOrChange(entry);
                        }
                    }
                }

                if (!verifiedQueries.isEmpty()) {
                    Address address = new Address(ZMIModule.class, ZMIModule.ADD_OR_CHANGE_ATTRIBUTES);
                    Message msg = new GenericMessage<>(verifiedQueries);
                    sendMessage(address, msg);
                    LOGGER.debug("VERIFY_QUERIES: OUT: " + msg.toString());
                }

            } catch (Exception e) {
                LOGGER.error("VERIFY_QUERIES: " + e.getMessage(), e);
            }
        }
    };

    @Override
    public void initialize(ConfigurationProvider configurationProvider) {
        try {
            InputStream inputStream = VerifierModule.class.getResourceAsStream("/keystore.jks");
            KeyStore keyStore = KeyStore.getInstance("JCEKS");
            keyStore.load(inputStream, "cloudatlas".toCharArray());


            Certificate cert =  keyStore.getCertificate("querySigner");
            publicKey = cert.getPublicKey();

        } catch (Exception e) {
            LOGGER.error("INITIALIZE: " + e.getMessage(), e);
        }
    }
}
