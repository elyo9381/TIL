package org.zero.persistence;

import static org.junit.Assert.fail;

import java.sql.Connection;

import javax.sql.DataSource;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.zero.config.RootConfig;

import lombok.Setter;
import lombok.extern.log4j.Log4j;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {RootConfig.class})
@Log4j
public class DataSourceTests {
	
	
	@Setter(onMethod = @__({@Autowired}))
	private DataSource dataSource;

	
	@Setter(onMethod = @__({@Autowired}))
	private SqlSessionFactory sessionFactory;

	
	@Test
	public void testMybitis() {
		try(SqlSession session = sessionFactory.openSession();
				Connection con = dataSource.getConnection() ) {
			log.info(session);
			log.info(con);
		} catch (Exception e) {
			// TODO: handle exception
			fail(e.getMessage());
		}
	}
	
	

}
