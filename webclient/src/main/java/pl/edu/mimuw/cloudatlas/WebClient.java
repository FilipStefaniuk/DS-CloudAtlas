package pl.edu.mimuw.cloudatlas;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.remoting.rmi.RmiProxyFactoryBean;
import pl.edu.mimuw.cloudatlas.querySigner.QuerySignerInterface;

@SpringBootApplication
public class WebClient {

//    @Bean
//    RmiProxyFactoryBean service() {
//        RmiProxyFactoryBean rmiProxyFactory = new RmiProxyFactoryBean();
//        rmiProxyFactory.setServiceInterface(QuerySignerInterface.class);
//        rmiProxyFactory.setServiceUrl("rmi://localhost:1099/cloudatlas_query_signer");
//        return rmiProxyFactory;
//    }

    public static void main(String[] args) throws Exception {
        SpringApplication.run(WebClient.class, args);
    }
}
