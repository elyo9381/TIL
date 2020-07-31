package org.zerock.controller;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import lombok.Setter;
import lombok.extern.log4j.Log4j;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration({"file:src/main/webapp/WEB-INF/spring/root-context.xml",
	"file:src/main/webapp/WEB-INF/spring/appServlet/servlet-context.xml"})
@Log4j
public class BoardControllerTests {
	
	@Setter(onMethod = @__(@Autowired))
	private WebApplicationContext ctx;
	
	private MockMvc mockMvc;
	
	@org.junit.Before
	public void setup() {
		this.mockMvc = MockMvcBuilders.webAppContextSetup(ctx).build();
		
	}
	
	
	
	/* MockMvc는 말그대로 가짜mvc라는 것이다. 
	 * MockMvcRequestBuilders.get("/board/list") 
	 * 를 통해서 get방식의 호출을하며
	 * .getModelAndView().getModelMap() 
	 * 를 통해서 위의 반한된 결과를 model에 어떤데이터가 들어있으지 확인합니다.
	 * */
	
	
//	@Test
//	public void testList() throws  Exception { 
//		log.info(
//				mockMvc.perform(MockMvcRequestBuilders.get("/board/list"))
//				.andReturn()
//				.getModelAndView()
//				.getModelMap()
//				);
//		
//	}
	
	
//	@Test
//	public void testRegister() throws Exception{
//		String resultPage = mockMvc.perform(MockMvcRequestBuilders.post("/board/register")
//				.param("title", "test new title post")
//				.param("content","test new content post")
//				.param("writer","user00")
//				).andReturn().getModelAndView().getViewName();
//		
//		log.info(resultPage);
//				
//	}
	
//	@Test
//	public void testGet() throws Exception {
//		log.info(mockMvc.perform(MockMvcRequestBuilders
//				.get("/board/get")
//				.param("bno","14"))
//				.andReturn()
//				.getModelAndView().getModelMap());
//	}
	
//	@Test
//	public void testModify() throws Exception {
//		String resultPage = mockMvc.perform(MockMvcRequestBuilders.post("/board/modify")
//				.param("bno","1")
//				.param("title", "modified test title")
//				.param("content", "modified test content")
//				.param("writer", "user00"))
//				.andReturn().getModelAndView().getViewName();
//		
//		log.info(resultPage);			
//	}
//	
	@Test
	public void testRemove() throws Exception {
		String resultPage = mockMvc.perform(MockMvcRequestBuilders.post("/board/remove")
				.param("bno","13"))
				.andReturn().getModelAndView().getViewName();
		
		log.info(resultPage);
	}
}
