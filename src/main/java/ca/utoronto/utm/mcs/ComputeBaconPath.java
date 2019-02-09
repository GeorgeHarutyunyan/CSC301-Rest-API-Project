package ca.utoronto.utm.mcs;

import java.io.IOException;
import java.io.OutputStream;

import org.json.JSONArray;
import org.json.JSONObject;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.types.Node;
import org.neo4j.driver.v1.types.Path;
import org.neo4j.driver.v1.types.Path.Segment;
import org.neo4j.driver.v1.types.Relationship;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class ComputeBaconPath implements HttpHandler {

	Driver driver;
	private String actorId;
	private String id;
	private String idStatementValue;
	JSONObject response = new JSONObject();
	private JSONArray JSONpathList = new JSONArray();
	
	public ComputeBaconPath(Driver driver) {
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
                computeBaconPath(request);

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

	private void computeBaconPath(HttpExchange request) {
		try (Session session = driver.session())
		{
			StatementResult pathstatement = session.run("MATCH (a:actor {actorId:\""+this.actorId+"\"}),(b:actor {actorId:\"nm0000102\"}), "
					+ "p = shortestPath((a)-[*]-(b)) RETURN p");
			Path path = pathstatement.single().get(0).asPath(); //Gets the path from the cypher query
			String baconNumber = Integer.toString(path.length()/2);
			response.put("baconNumber",baconNumber);
			
			System.out.println(path.toString());
			StatementResult idResult;
			JSONObject formatObject = new JSONObject();
			String statementActorId;
			String statementMovieId;
			
			for (Segment segment:path) {
				idResult = session.run("MATCH (s) WHERE ID(s) = "+segment.start().id()+" RETURN s");
				statementActorId = segment.start().get("actorId").toString();
				statementMovieId = segment.end().get("movieId").toString();
				System.out.println(statementActorId);
				System.out.println(statementMovieId);
				
				if (!(statementActorId.equals("NULL")|| statementMovieId.equals("NULL"))) {
					formatObject.put("actorId",Utils.removeChars(statementActorId));
					formatObject.put("movieId",Utils.removeChars(statementMovieId));
					JSONpathList.put(formatObject);
					formatObject = new JSONObject();
				}
			}
			response.put("baconNumber",baconNumber);
			response.put("baconPath",JSONpathList);
			
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
