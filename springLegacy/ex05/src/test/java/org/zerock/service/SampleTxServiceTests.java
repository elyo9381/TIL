package org.zerock.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import lombok.Setter;
import lombok.extern.log4j.Log4j;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("file:src/main/webapp/WEB-INF/spring/root-context.xml")
@Log4j
public class SampleTxServiceTests {
	
	
	/*table을 만들어야한다. 그리고 tx에러가 난다. */
	
	@Setter(onMethod_ = {@Autowired})
	private SampleTxService sampleTxService;
	
	@Test
	public void testLong() {
		String str = "alskd"
				+ "fjlasdjfoisa"
				+ "jlkfjkasljfklajskljflkjskjfklasjls"
				+ "afkhjksahkhfkjashkjhfkjhkajshkhfkshk";
		
		log.info(str.getBytes().length);
		sampleTxService.addData(str);
	}
}
