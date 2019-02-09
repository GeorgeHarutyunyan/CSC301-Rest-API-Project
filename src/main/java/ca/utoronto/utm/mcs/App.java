package ca.utoronto.utm.mcs;

import java.io.IOException;
import java.net.InetSocketAddress;

import org.neo4j.driver.v1.AuthTokens;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.GraphDatabase;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.Value;

import com.sun.net.httpserver.HttpServer;

public class App 
{
    static int PORT = 8080;
	static Driver driver = GraphDatabase.driver("bolt://localhost:7687", AuthTokens.basic("neo4j","neo4j"));

    public static void main(String[] args) throws IOException
    {
        HttpServer server = HttpServer.create(new InetSocketAddress("0.0.0.0", PORT), 0);
        
        server.createContext("/api/v1/addActor", new AddActor(driver));
        server.createContext("/api/v1/addMovie", new AddMovie(driver));
        server.createContext("/api/v1/addRelationship", new AddRelationship(driver));
        server.createContext("/api/v1/getActor", new GetActor(driver));
        server.createContext("/api/v1/getMovie", new GetMovie(driver));
        server.createContext("/api/v1/hasRelationship", new HasRelationship(driver));
        server.createContext("/api/v1/computeBaconNumber", new ComputeBaconNumber(driver));
        server.createContext("/api/v1/computeBaconPath", new ComputeBaconPath(driver));
        
        server.start();
        System.out.printf("Server started on port %d...\n", PORT);
         

    }


    }

