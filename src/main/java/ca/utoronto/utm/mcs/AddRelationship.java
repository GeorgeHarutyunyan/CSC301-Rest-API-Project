package ca.utoronto.utm.mcs;

import java.io.IOException;

import org.json.JSONObject;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class AddRelationship implements HttpHandler {

	private Driver driver;
	private String actorId;
	private String movieId;
	
	public AddRelationship(Driver driver) {
		this.driver = driver;
	}
	
	@Override
	public void handle(HttpExchange request) throws IOException {
        try {
            if (request.getRequestMethod().equals("PUT")) {
                String body = Utils.convert(request.getRequestBody());
                JSONObject deserialized = new JSONObject(body);
                if (deserialized.has("actorId")) {
                    actorId = deserialized.getString("actorId");
                }
                else {
                    request.sendResponseHeaders(400,0); //error
                    return;
                }
                if (deserialized.has("movieId")) {
                    movieId = deserialized.getString("movieId");
                }
                else {
                    request.sendResponseHeaders(400,0); //error
                    return;
                }
                
                addRelationship(request);
                request.sendResponseHeaders(200,0);
            } 
            else {
            	System.out.println("Error, not PUT");
                request.sendResponseHeaders(400,0);
            }
        } catch (Exception e) {
            request.sendResponseHeaders(500,0);
            e.printStackTrace();
        }
	}
	
	
	public void addRelationship(HttpExchange request) {
        try (Session session = driver.session())
        {
	        session.run("MATCH (a:actor {actorId:\""+this.actorId+"\"}) MATCH (b:movie {movieId:\""+this.movieId+"\"}) Create ((a)-[:ACTED_IN]->(b))");
	        request.sendResponseHeaders(200,0);	
        }
        catch (Exception e) {
        	e.printStackTrace();
        }

	}

}
