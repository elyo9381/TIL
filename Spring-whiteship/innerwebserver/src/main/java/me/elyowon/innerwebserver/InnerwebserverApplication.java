package me.elyowon.innerwebserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
public class InnerwebserverApplication {

    @GetMapping("/hello")
    public String hello(){
        return "hello Spring";
    }

    public static void main(String[] args) {
        SpringApplication.run(InnerwebserverApplication.class, args);
    }
}

//    @Bean
//    public ServletWebServerFactory serverFactory(){
//        TomcatServletWebServerFactory tomcat = new TomcatServletWebServerFactory();
//        tomcat.addAdditionalTomcatConnectors(createStandardConnetor());
//        return tomcat;
//    }
//
//    private Connector createStandardConnetor() {
//        Connector connector = new Connector("org.apache.coyote.http11.Http11NioProtocol");
//        connector.setPort(8080);
//        return connector;
//    }
