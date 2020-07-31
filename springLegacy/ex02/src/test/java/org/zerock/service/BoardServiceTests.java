package org.zerock.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.zerock.Service.BoardService;

import lombok.Setter;
import lombok.extern.log4j.Log4j;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("file:src/main/webapp/WEB-INF/spring/root-context.xml")
@Log4j
public class BoardServiceTests {
	
	@Setter(onMethod = @__({@Autowired}))
	private BoardService service;
	
	// confirm ServiceObject DI 
//	@Test
//	public void testExist() {
//		log.info(service);
//		assertNotNull(service);
//	}
	
	// register test
//	@Test
//	public void testRegister() {
//		
//		BoardVO board = new BoardVO();
//		board.setTitle("new title post");
//		board.setContent("new content post");
//		board.setWriter("newbie");
//		
//		service.register(board);
//		
//		log.info("생성된 계시물의 번호 = " + board.getBno());
//	}
	
	// list test
	@Test
	public void testGetList() {
		service.getList().forEach(board-> log.info(board));
	}
	
	// get Test
//	@Test
//	publisc void testGet() {
//		log.info(service.get(5L));
//	}
//	
//	@Test
//	public void testDelete() {
//		log.info("REMOVE RESULT : "+ service.remove(4L));
//	}
//	
//	@Test
//	public void testUpdate() {
//		BoardVO board = service.get(1L);
//		if(board == null) {
//			return ;
//		}
//		
//		board.setTitle("제목 수정합니다.");
//		log.info("MODIFY RESULT  : " + service.modify(board));
//	}
	
	
}
 