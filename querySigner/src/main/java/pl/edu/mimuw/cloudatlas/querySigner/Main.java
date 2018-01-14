package pl.edu.mimuw.cloudatlas.querySigner;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.FileBasedConfiguration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.InputStream;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.security.KeyStore;

public class Main {

    private static final Logger LOGGER = LogManager.getLogger(Main.class);

    private static final String CONFIG_FILE = "qs.properties";
    private static final String KEYSTORE = "/keystore.jks";

    public static void main(String[] args) {
        try {
            LOGGER.info("Starting server.");

            FileBasedConfigurationBuilder<FileBasedConfiguration> builder =
                    new FileBasedConfigurationBuilder<FileBasedConfiguration>(PropertiesConfiguration.class)
                    .configure(new Parameters().fileBased().setFile(new File(CONFIG_FILE)));

            Configuration config = builder.getConfiguration();
            InputStream inputStream = QuerySigner.class.getResourceAsStream(KEYSTORE);

            KeyStore keyStore = KeyStore.getInstance("JCEKS");
            keyStore.load(inputStream, config.getString("keystore.password").toCharArray());

            KeyStore.PasswordProtection keyPassword =
                    new KeyStore.PasswordProtection(config.getString("keystore.key.password").toCharArray());

            KeyStore.PrivateKeyEntry privateKeyEntry =
                    (KeyStore.PrivateKeyEntry) keyStore.getEntry(config.getString("keystore.key.name"), keyPassword);

            QuerySigner server = new QuerySigner(privateKeyEntry.getPrivateKey());

            if (System.getSecurityManager() == null) {
                System.setSecurityManager(new SecurityManager());
            }

            QuerySignerInterface stub = (QuerySignerInterface) UnicastRemoteObject.exportObject(server, 0);
            Registry registry = LocateRegistry.getRegistry();
            registry.rebind(config.getString("rmi.service"), stub);

        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }
}
