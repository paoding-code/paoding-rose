package net.paoding.rose.samples.hellorose.controllers;

import net.paoding.rose.web.annotation.Path;
import net.paoding.rose.web.annotation.rest.Get;

@Path("")
public class MainController {

    @Get("")
    public String main() {
        return "redirect:/HelloRose/world";
    }
}
