package ca.utoronto.utm.mcs;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class GetMovie implements HttpHandler {

	private Driver driver;
	private String movieId;
	private String movieName;
	private JSONObject response = new JSONObject();
	private JSONArray JSONactorList = new JSONArray();
	
	public GetMovie(Driver driver) {
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
                getMovie(request);

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
	
	public void getMovie(HttpExchange request) {
        try (Session session = driver.session() )
        {
        	StatementResult nameResult = session.run("MATCH (a:movie {movieId:"+this.movieId+"}) return a.name");
        	this.movieName = nameResult.next().values().toString(); //replacing unnecessary characters from neo4j Record
        	this.movieName = Utils.removeChars(this.movieName);
        	
        	StatementResult result = session.run("MATCH (:movie { movieId:\""+this.movieId+"\" })<--(actor)\r\n" + 
        			"RETURN actor.actorId");
        	
        	for (Record ele:result.list()) {
            	JSONactorList.put(Utils.removeChars(ele.values().toString()));
        	}
        	response.put("movies",JSONactorList);
        	response.put("name",this.movieName);
        	response.put("actorId",this.movieId);
        	System.out.println(response);
        	
            request.sendResponseHeaders(200,response.toString().getBytes().length);
            OutputStream os = request.getResponseBody();
            os.write(response.toString().getBytes());
            os.close();
            JSONactorList = new JSONArray();
        }
        catch (Exception e) {
        	e.printStackTrace();
        }
	}

}
