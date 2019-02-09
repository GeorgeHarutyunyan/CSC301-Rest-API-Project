package ca.utoronto.utm.mcs;

import java.awt.List;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;

import org.json.JSONArray;
import org.json.JSONObject;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class GetActor implements HttpHandler {
	
	private Driver driver;
	private String actorName;
	private String actorId;
	private JSONObject response = new JSONObject();
	private JSONArray JSONmovieList = new JSONArray();
	private ArrayList<Record> movieList;

	public GetActor(Driver driver) {
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
                getActor(request);

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
	
	public void getActor(HttpExchange request) {
        try (Session session = driver.session() )
        {
        	StatementResult nameResult = session.run("MATCH (a:actor {actorId:"+this.actorId+"}) return a.name");
        	
        	if (nameResult.hasNext()){
	        	this.actorName = nameResult.next().values().toString(); //replacing unnecessary characters from neo4j Record
	        	this.actorName = Utils.removeChars(this.actorName);
        	}
        	else {
        		//ACTOR DOES NOT EXIST, error out.
        	}
        	
        	StatementResult result = session.run("MATCH (:actor { actorId:"+this.actorId+"})-->(movie)\r\n" + 
        			"RETURN movie.movieId");
        	
        	for (Record ele:result.list()) {
            	JSONmovieList.put(Utils.removeChars(ele.values().toString()));
        	}
        	response.put("movies",JSONmovieList);
        	response.put("name",this.actorName);
        	response.put("actorId",this.actorId);
        	System.out.println(response);
        	
            request.sendResponseHeaders(200,response.toString().getBytes().length);
            OutputStream os = request.getResponseBody();
            os.write(response.toString().getBytes());
            os.close();
            JSONmovieList = new JSONArray();
        }
        catch (Exception e) {
        	e.printStackTrace();
        }
	}
	
}
