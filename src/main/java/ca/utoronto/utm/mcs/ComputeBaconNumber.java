package ca.utoronto.utm.mcs;

import java.io.IOException;
import java.io.OutputStream;

import org.json.JSONObject;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.types.Path;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class ComputeBaconNumber implements HttpHandler {

	private Driver driver;
	private String actorId;
	JSONObject response = new JSONObject();
	
	public ComputeBaconNumber(Driver driver) {
		this.driver = driver;
	}

	@Override
	public void handle(HttpExchange request) throws IOException {
        try {
            if (request.getRequestMethod().equals("GET")) {
                String body = Utils.convert(request.getRequestBody());
                JSONObject deserialized = new JSONObject(body);
                if (deserialized.has("actorId")) {
                    actorId = deserialized.getString("actorId");
                }
                else {
                    request.sendResponseHeaders(400,0); //error
                    return;
                }                
                computeBaconNumber(request);

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

	private void computeBaconNumber(HttpExchange request) {
		try (Session session = driver.session())
		{
			StatementResult pathstatement = session.run("MATCH (a:actor {actorId:\""+this.actorId+"\"}),(b:actor {actorId:\"nm0000102\"}), "
					+ "p = shortestPath((a)-[*]-(b)) RETURN p");
			Path path = pathstatement.single().get(0).asPath(); //Gets the path from the cypher query
			String baconNumber = Integer.toString(path.length()/2);
			response.put("baconNumber",baconNumber);
			
            request.sendResponseHeaders(200,response.toString().getBytes().length);
            OutputStream os = request.getResponseBody();
            os.write(response.toString().getBytes());
            os.close();
		}
		catch (Exception e) {
        	e.printStackTrace();
		}
	}

}
