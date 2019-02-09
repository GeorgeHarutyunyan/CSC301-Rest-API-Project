package ca.utoronto.utm.mcs;

import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class AddActor implements HttpHandler {
	
	private Driver driver;
	private String actorName;
	private String actorId;

	public AddActor(Driver driver) {
		this.driver = driver;
	}

	@Override
	public void handle(HttpExchange request) throws IOException {
        try {
            if (request.getRequestMethod().equals("PUT")) {
                String body = Utils.convert(request.getRequestBody());
                JSONObject deserialized = new JSONObject(body);
                if (deserialized.has("name")) {
                    actorName = deserialized.getString("name");
                }
                else {
                    request.sendResponseHeaders(400,0); //error
                    return;
                }
                if (deserialized.has("actorId")) {
                    actorId = deserialized.getString("actorId");
                }
                else {
                    request.sendResponseHeaders(400,0); //error
                    return;
                }
                
                addPerson(request);
            } 
            else {
            	System.out.println("Error, not PUT");
                request.sendResponseHeaders(400,0);
                return;
            }
        } catch (Exception e) {
            request.sendResponseHeaders(500,0);
            e.printStackTrace();
        }
	}
	
    public void addPerson(HttpExchange request)
    {
        try (Session session = driver.session() )
        {
        	StatementResult dupeResult = session.run("MATCH(n:actor {actorId:\""+this.actorId+"\"}) return n");
        	if (dupeResult.hasNext()) {
                return;
        	} else {
            	session.run("Create (:actor {name:\"" +this.actorName+ "\", actorId:\""+this.actorId+"\" })");
            	request.sendResponseHeaders(200,0);	
        	}
        }
        catch (Exception e) {
        	e.printStackTrace();
        }
        	
    }

}
