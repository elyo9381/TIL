package me.elyowon.springjpa;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import javax.sql.DataSource;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;

// 아래와 같이 만들어 인티그레이션 테스트에요
// springbootapplication 의 모든빈이 다 등록된다. 따라서 postgres가 사용한다.
//@SpringBootTest
@RunWith(SpringRunner.class)
// 슬라이싱테스트는 자동으로 임베디드 데이터베이스를 쓰도록 설정된다.
@DataJpaTest
public class AccountRepositoryTest {

    @Autowired
    DataSource dataSource;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    AccountRepository accountRepository;

    @Test
    public void di() throws SQLException {
        Account account = new Account();
        account.setUsername("elyo");
        account.setPassword("pass");

        Account newAccount = accountRepository.save(account);

        assertThat(newAccount).isNotNull();

        Account existingAccount = accountRepository.findByUsername(newAccount.getUsername());
        assertThat(existingAccount).isNotNull();


        Account nonexistingAccount = accountRepository.findByUsername("whiteship");
        assertThat(nonexistingAccount).isNull();

    }
}