package org.zerock.mapper;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.zerock.domain.BoardVO;

import lombok.Setter;
import lombok.extern.log4j.Log4j;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("file:src/main/webapp/WEB-INF/spring/root-context.xml")
@Log4j
public class BoardMapperTests {
	
	@Setter(onMethod = @__({@Autowired}))
	private BoardMapper boardMapper;
	
//	
//	@Test
//	public void testGetList() {
//		List<BoardVO> boardVOs;
//		boardVOs = boardMapper.getList();
//		
////		boardMapper.getList().forEach(boardMapper->log.info(boardMapper));
//		for( BoardVO boardMapper : boardVOs) {
//			log.info("출력 가즈아 ~~"+boardMapper);
//		}
//	}
//	
//	@Test
//	public void testInsert() {
//		BoardVO board = new BoardVO();
//		board.setTitle("새로작성하는글");
//		board.setContent("new content");
//		board.setWriter("newbie");
//		
//		boardMapper.insert(board);
//		
//		log.info(board);
//		
//	}	
//	
//	@Test
//	public void testRead() {
//		BoardVO board =  boardMapper.read(5L);
//		
//		log.info(board);
//	}
//	
//	@Test
//	public void testDelete() {
//		log.info("Delete count : " + boardMapper.delete(3L));
//	}
//	
	@Test
	public void testUpdate() {
		BoardVO board = new BoardVO();
		board.setBno(5L);
		board.setTitle("수정된제목");
		board.setContent("modified content");
		board.setWriter("user00");
		
		int count = boardMapper.update(board);
		log.info("Update count : " + count);
	}
	
}
