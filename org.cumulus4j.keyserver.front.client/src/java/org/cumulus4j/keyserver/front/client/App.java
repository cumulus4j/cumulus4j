package org.cumulus4j.keyserver.front.client;

import javax.ws.rs.core.MediaType;

import org.cumulus4j.keyserver.front.shared.PutUserRequest;

import com.sun.jersey.api.client.Client;

/**
 * Hello world!
 *
 */
public class App
{
    public static void main( String[] args )
    {
        Client c = new Client();
        PutUserRequest putUserRequest = new PutUserRequest();
        putUserRequest.setUserName("marco");
        putUserRequest.setPassword("test".toCharArray());
        c.resource("http://localhost:8080/user").accept(MediaType.APPLICATION_XML_TYPE).put(putUserRequest);
    }
}
