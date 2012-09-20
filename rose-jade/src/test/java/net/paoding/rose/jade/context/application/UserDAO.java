package net.paoding.rose.jade.context.application;

import net.paoding.rose.jade.annotation.DAO;
import net.paoding.rose.jade.annotation.SQL;

@DAO
public interface UserDAO {

    @SQL("select name from user where id=:1")
    String getName(int id);

    @SQL("select name from user order by id asc")
    String[] getNames();
}
