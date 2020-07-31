package me.elyowon.springtestdemo;


import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class SampleController {
    @GetMapping("/hello")
    public EntityModel<Hello> hello(Model model) {
        Hello hello = new Hello();
        hello.setName("elyo");
        hello.setPrefix("hey");

        EntityModel<Hello> helloEntityModel = new EntityModel<>(hello);
        Link link = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(SampleController.class).hello(model)).withSelfRel();
        helloEntityModel.add(link);

        return helloEntityModel;
    }
}