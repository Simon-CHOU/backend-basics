package com.simon.permissionannotation;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ControllerTest {

    @Autowired
    private HiController controller;

    @Test
    void hiKid() {
        assertEquals("Hi kid!", controller.hiKid());
    }

    @Test
    void hiMom() {
        assertEquals("Hi mom!", controller.hiMom());
    }

    @Test
    void hiDad() {
        assertEquals("Hi dad!", controller.hiDad());
    }
}