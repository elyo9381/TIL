package me.elyowon.springinit.springboot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Application {
    public static void main(String[] args) {
//        Tomcat tomcat =new Tomcat();
//        tomcat.setPort(8080);
//
//        Context context = tomcat.addContext("","/");
//
//        HttpServlet servlet = new HttpServlet() {
//            @Override
//            protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
//                PrintWriter writer = resp.getWriter();
//                writer.println("<html><head><title>");
//                writer.println("Hey, Tomcat");
//                writer.println("</title></head>");
//                writer.println("<body><h1>Hello TomCat</h1></body>");
//                writer.println("</html>");
//            }
//        };
//
//        String servletName = "helloServlet";
//        tomcat.addServlet("",servletName,servlet);
//        context.addServletMappingDecoded("/hello",servletName);
//
//        tomcat.getConnector();
//        tomcat.start();
        SpringApplication.run(Application.class, args);
    }
}
