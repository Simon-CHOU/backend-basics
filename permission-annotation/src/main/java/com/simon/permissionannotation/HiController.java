package com.simon.permissionannotation;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/hi")
public class HiController {
    @Permission(level = PermissionEnum.KID)
    @GetMapping("/kid")
    public String hiKid() {
        return "Hi kid!";
    }

    @Permission(level = PermissionEnum.DAD)
    @GetMapping("/dad")
    public String hiDad() {
        return "Hi dad!";
    }

    @Permission(level = PermissionEnum.MOM)
    @GetMapping("/mom")
    public String hiMom() {
        return "Hi mom!";
    }
}
