import java.awt.List;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.stream.Collectors;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.store.FSDirectory;

import edu.uci.ics.jung.algorithms.scoring.PageRank;
import edu.uci.ics.jung.graph.DirectedSparseGraph;
import edu.uci.ics.jung.graph.util.EdgeType;
import edu.uci.ics.jung.graph.util.Pair;

public class AuthorRank {


	public static void main(String[] args) throws IOException, ParseException {
		// intitialize the filename and the graph
		String fname = "C:\\Users\\sowmy\\OneDrive\\IU\\Fall 2017\\Search\\Assignment 3\\author.net";
		DirectedSparseGraph<String, String> graph = new DirectedSparseGraph<String, String>();
		
		// read the graph file 
		FileReader f = new FileReader(new File(fname));
		BufferedReader br= new BufferedReader(f);
		String line;
		int lineNum =1;
		line = br.readLine();
			
		// get the number of vertices from the first line
		String[] lineSplit= line.split("\\s+");
		String vertices_cnt = lineSplit[1];
		
		// Map each graph node to an author 
		Map<String, String> authorMap = new HashMap<String, String>();
		// add the vertices to the graph
		for(int i=0; i < Integer.parseInt(vertices_cnt); i++) {
			line = br.readLine();
			String[] vertices = line.split(" ");
			authorMap.put(vertices[0], vertices[1]);
			graph.addVertex(vertices[0]);
			
		}
		//Get the number of edges
		line = br.readLine();
		lineSplit = line.split("\\s+");
		String edge_cnt = lineSplit[1];
		
	
		// add the edges to the graph
		for(int i=0 ; i < Integer.parseInt(edge_cnt); i++) {
			line = br.readLine();
			String[] ends= line.split(" ");
			Pair<String> vertexpairs = new Pair<String>(ends[0],ends[1]);
			graph.addEdge(Integer.toString(i), vertexpairs, EdgeType.DIRECTED);
			
		}
		
		
		// initialize alpha value that gives the probability of a random jump 
		double alpha = 0.1;
		PageRank<String, String> pr = new PageRank<String,String>(graph,alpha);
		pr.evaluate();
		
		// define a hashmap to store the results
		Map<String, Double> rank_results = new HashMap<String, Double>();
		for(String v: graph.getVertices()) {
			//System.out.println(v+ " "+ pr.getVertexScore(v));
			rank_results.put(v,pr.getVertexScore(v));	
		}
		
		// sort the results 
		System.out.println("AuthorID   PageRank");
		Map<String, Double> sorted_ranks = new HashMap<String, Double>();
		rank_results.entrySet().stream()
	    .sorted(Map.Entry.<String, Double>comparingByValue().reversed()).limit(10)
	    .forEachOrdered(x -> System.out.println(authorMap.get(x.getKey()).toString() +"   " + x.getValue().toString()));
		
		
}
}