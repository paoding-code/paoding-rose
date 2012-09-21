package net.paoding.rose.samples.hellorose.controllers;

import net.paoding.rose.web.Invocation;
import net.paoding.rose.web.annotation.Param;
import net.paoding.rose.web.annotation.Path;
import net.paoding.rose.web.annotation.rest.Get;

@Path("world")
public class WorldController {

    private String worlds = "heaven,earth,hell";

    @Get("")
    public String index() {
        return "@Every Thing is OK. <p><a href=\"/HelloRose/world/list\">list worlds</a>";
    }

    @Get("list")
    public String list(Invocation inv) {
        inv.addModel("worlds", worlds.split(","));
        return "world-list"; // refer: views/world-list.vm
    }

    @Get("prefix-{name}")
    public String show(Invocation inv, @Param("name") String name) {
        int index = worlds.indexOf(name);
        inv.addModel("index", index);
        inv.addModel("name", name);
        return "world-show";// refer: views/world-show.vm
    }
}
