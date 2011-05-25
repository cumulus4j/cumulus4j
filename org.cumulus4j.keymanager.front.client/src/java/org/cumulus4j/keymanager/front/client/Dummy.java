package org.cumulus4j.keymanager.front.client;

import javax.ws.rs.core.MediaType;

import org.cumulus4j.keymanager.front.shared.UserWithPassword;

import com.sun.jersey.api.client.Client;

/**
 * Hello world!
 *
 */
public class Dummy
{
    public static void main( String[] args )
    {
        Client c = new Client();
        UserWithPassword userWithPassword = new UserWithPassword();
        userWithPassword.setUserName("gaga");
        userWithPassword.setPassword("test".toCharArray());
        c.resource("http://localhost:8080/org.cumulus4j.keyserver.front.webapp/user").accept(MediaType.APPLICATION_XML_TYPE).put(userWithPassword);
    }
}
