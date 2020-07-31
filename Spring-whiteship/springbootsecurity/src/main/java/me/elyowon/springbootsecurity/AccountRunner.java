package me.elyowon.springbootsecurity;

import me.elyowon.springbootsecurity.account.Account;
import me.elyowon.springbootsecurity.account.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class AccountRunner implements ApplicationRunner {

    @Autowired
    AccountService accountService;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        Account elyo = accountService.createAccount("elyo","1234");
        System.out.println(elyo.getUsername() + " password : " + elyo.getPassword());
    }
}