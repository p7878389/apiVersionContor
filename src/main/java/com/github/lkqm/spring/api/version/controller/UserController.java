package com.github.lkqm.spring.api.version.controller;


import com.github.lkqm.spring.api.version.annotations.ApiVersion;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author martin.peng
 */
@RestController
@RequestMapping("/user")
public class UserController {

    @GetMapping("/list")
    @ApiVersion(methodName = "abcdef")
    public String list() {
        return "list";
    }

    @GetMapping("/list")
    @ApiVersion(methodName = "list1")
    public String list1() {
        return "list1";
    }

    @GetMapping("/list")
    @ApiVersion(version = "1.1.0", methodName = "list2")
    public String list2() {
        return "list2";
    }

    @GetMapping("/list")
    @ApiVersion(version = "1.1.3", methodName = "list3")
    public String list3() {
        return "list3";
    }

}