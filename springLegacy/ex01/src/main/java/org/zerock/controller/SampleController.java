package org.zerock.controller;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.zerock.domain.SampleDTO;
import org.zerock.domain.SampleDTOList;
import org.zerock.domain.TodoDTO;

import lombok.extern.log4j.Log4j;

@Controller
@RequestMapping("/sample/*")
@Log4j
public class SampleController{
	
		@InitBinder
		public void initBinder(WebDataBinder binder) {
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
			binder.registerCustomEditor(java.util.Date.class, new CustomDateEditor(dateFormat,false));
		}
		
		
	    @RequestMapping(value="/basic", method= {RequestMethod.GET,RequestMethod.POST})
	    public void basicGet1() {
	        log.info("get basic ..........");
	    }
	    @GetMapping("/basicOnlyGet")
	    public void basicGet2() {
	        log.info("basic get only get ..........");
	    }
	    
	    
	    @GetMapping("/ex01")
	    public String ex01(SampleDTO dto) {
	    	log.info("" + dto);
	    	return "ex01";
	    }
	    
	    @GetMapping("/ex02")
	    public String ex02(@RequestParam("name") String name, 
	    		@RequestParam("age") int age) {
	    	log.info("name : " + name);
	    	log.info("age : " + age);
	    	
	    	return "ex02";
	    }
	    
	    @GetMapping("/ex02List")
	    public String ex02List(@RequestParam("ids")ArrayList<String> ids) {
	    	log.info("ids : " + ids);
	    	
	    	return "ex02List";
	    }
	    
	    @GetMapping("/ex02Bean")
	    public String ex02Bean(SampleDTOList list) {
	    	log.info("list dtos : " + list);	    	
	    	return "ex02Bean";
	    }
	    
	    @GetMapping("/ex03")
	    public String ex03(TodoDTO todoDTO){
	    	log.info("todo : " + todoDTO);
	    	return "ex03";
	    	
	    }
	    
	    //@ModelAttribute("page") << 애노테이션을 통해서 page를 입력받을때 자동 바인딩을 형성해준다. 
	    //@ModelAttribute 는 파라미터를 Model에 담아서 강제로 전달하는 애노테이션 입니다.  
	    @GetMapping("/ex04")
	    public String ex04(SampleDTO dto, @ModelAttribute("page") int page) {
	    	log.info("dto : " + dto);
	    	log.info("page : " + page);
	    	
	    	return "/sample/ex04";
	    }
	    
	    

}

