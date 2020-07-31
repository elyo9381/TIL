//package me.elyowon.springtestdemo;
//
//
//import org.springframework.stereotype.Controller;
//import org.springframework.web.bind.annotation.ExceptionHandler;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.ResponseBody;
//
//@Controller
//public class HandleController {
//
//    @GetMapping("/hello")
//    public String hello(){
//        throw new HandleException();
//    }
//
//    @ExceptionHandler(HandleException.class)
//    public @ResponseBody AppError sampleError(HandleException e){
//        AppError appError = new AppError();
//        appError.setMessage("error.app.key");
//        appError.setReason("IDK IDK IDK");
//        return appError;
//    }
//
//}