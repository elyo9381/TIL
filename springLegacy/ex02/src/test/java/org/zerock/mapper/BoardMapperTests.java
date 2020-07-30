package org.zerock.mapper;

import java.util.List;

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
	
	@Test
	public void testGetList() {
		List<BoardVO> boardVOs;
		boardVOs = boardMapper.getList();
		
//		boardMapper.getList().forEach(boardMapper->log.info(boardMapper));
		for( BoardVO boardMapper : boardVOs) {
			log.info("출력 가즈아 ~~"+boardMapper);
		}
	}
	
}
