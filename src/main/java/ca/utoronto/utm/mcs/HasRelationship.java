package ca.utoronto.utm.mcs;

import java.io.IOException;
import java.io.OutputStream;

import org.json.JSONObject;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class HasRelationship implements HttpHandler {

	private String actorId;
	private String movieId;
	private Driver driver;
	private String relationshipValue;
	private JSONObject response = new JSONObject();
	
	
	public HasRelationship(Driver driver) {
		this.driver = driver;
	}
	
	@Override
	public void handle(HttpExchange request) throws IOException {
        try {
            if (request.getRequestMethod().equals("GET")) {
                String body = Utils.convert(request.getRequestBody());
                JSONObject deserialized = new JSONObject(body);
                if (deserialized.has("movieId")) {
                    movieId = deserialized.getString("movieId");
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
                hasRelationship(request);

            } 
            else {
            	System.out.println("Error, not GET");
                request.sendResponseHeaders(400,0);
            }
        } catch (Exception e) {
            request.sendResponseHeaders(500,0);
            e.printStackTrace();
        }
	}
	
	public void hasRelationship(HttpExchange request) {
        try (Session session = driver.session()){
    		StatementResult relationshipResult = session.run("MATCH (a:actor {actorId:"+this.actorId+" }) MATCH (b:movie { movieId:"+this.movieId+" }) RETURN EXISTS( (a)-[:ACTED_IN]-(b))");
    		relationshipValue = Utils.removeChars(relationshipResult.next().values().toString());
    		relationshipValue = relationshipValue.toLowerCase();
    		System.out.println(relationshipValue);
    		response.put("actorId",this.actorId);
    		response.put("movieId",this.movieId);
    		response.put("hasRelationship",relationshipValue);
    		
            request.sendResponseHeaders(200,response.toString().getBytes().length);
            OutputStream os = request.getResponseBody();
            os.write(response.toString().getBytes());
            os.close();
        }
        catch(Exception e) {
        	System.out.println(e);
            // What happens if it fails? Also need to check for non-existing ID's.
        }
	}

}
