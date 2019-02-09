package ca.utoronto.utm.mcs;

import java.io.IOException;

import org.json.JSONObject;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class AddMovie implements HttpHandler {

	private Driver driver;
	private String movieName;
	private String movieId;

	
	public AddMovie(Driver driver) {
		this.driver = driver;
	}
	
	@Override
	public void handle(HttpExchange request) throws IOException {
        try {
            if (request.getRequestMethod().equals("PUT")) {
                String body = Utils.convert(request.getRequestBody());
                JSONObject deserialized = new JSONObject(body);
                if (deserialized.has("name")) {
                    movieName = deserialized.getString("name");
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
       
                addMovie(request);
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
	
	public void addMovie(HttpExchange request) {
	    {
	        try (Session session = driver.session() )
	        {
	        	StatementResult dupeResult = session.run("MATCH(n:movie {movieId:\""+this.movieId+"\"}) return n");
	        	if (dupeResult.hasNext()) {
	                return;
	        	}
	        	else {
		        	session.run("Create (:movie {name:\""+this.movieName+"\", movieId:\""+this.movieId+"\"})");	
	                request.sendResponseHeaders(200,0);
	        	}
	        }
	        catch (Exception e) {
	        	e.printStackTrace();
	        }
	        	
	    }
	}

}
