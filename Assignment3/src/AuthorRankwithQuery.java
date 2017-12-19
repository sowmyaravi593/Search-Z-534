import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.sound.sampled.AudioFileFormat.Type;

import org.apache.commons.collections15.Transformer;
import org.apache.commons.collections15.functors.MapTransformer;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.store.FSDirectory;
import edu.uci.ics.jung.algorithms.scoring.PageRankWithPriors;
import edu.uci.ics.jung.graph.DirectedSparseGraph;
import edu.uci.ics.jung.graph.util.EdgeType;
import edu.uci.ics.jung.graph.util.Pair;

public class AuthorRankwithQuery {
	
	public static  void authorRanker(String querystr) throws IOException, ParseException {
		// load the index 
				String index = "C:\\Users\\sowmy\\OneDrive\\IU\\Fall 2017\\Search\\Assignment 3\\author_index\\";
				IndexReader reader = DirectoryReader.open(FSDirectory.open(new File(index)));
				//checker code
				//Document docu = reader.document(1);
				//System.out.println(docu.getFields());
				// New searcher
				IndexSearcher searcher = new IndexSearcher(reader);		
				searcher.setSimilarity(new BM25Similarity());
				
				//new Analyzer
				Analyzer analyzer = new StandardAnalyzer();
				
				String queryString = querystr;
				
				// retrieve all relevant data for Data Mining
				QueryParser parser = new QueryParser("content", analyzer);
				Query query = parser.parse(QueryParser.escape(queryString));
			
				TopDocs collector = searcher.search(query,300);
				
				// map for author ranks from the search
				Map<String , Double> authormap = new HashMap<String, Double>();
				ScoreDoc[] docs = collector.scoreDocs;
				for (int i = 0; i < docs.length; i++) {
					Document doci = searcher.doc(docs[i].doc);
					authormap.put(doci.get("authorid") , (double) docs[i].score);
				}
				
				
				
				// caculating the prior for each of the author
				float ranksum =0 ;
				for(Map.Entry<String , Double> entry: authormap.entrySet()) {
					ranksum += entry.getValue();
				}
				 
				Map<String, Double> authorprior = new HashMap<String, Double>();
				for(Map.Entry<String , Double> entry: authormap.entrySet()) {
					authorprior.put(entry.getKey(), entry.getValue()/ranksum);
				}
				
				
				
				// construct a graph 
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
				Map<String, String> authorMapRev = new HashMap<String, String>();
				// add the vertices to the graph
				for(int i=0; i < Integer.parseInt(vertices_cnt); i++) {
					line = br.readLine();
					String[] vertices = line.split(" ");
					authorMap.put( vertices[1].substring(1, vertices[1].length()-1),vertices[0]);
					authorMapRev.put(vertices[0], vertices[1]);
					graph.addVertex(vertices[0]);
					
				}
				
			
				
				// map the author ids that we got earlier to get vertex weights
				Map<String, Double> authorpriorNew = new HashMap<String, Double>();
				for(Map.Entry<String , String> entry: authorMap.entrySet()) {
					if (authorprior.containsKey(entry.getValue()) )
					{
						authorpriorNew.put(authorMap.get(entry.getKey()), authorprior.get(entry.getValue()));
					}
					else {
						
						authorpriorNew.put(authorMap.get(entry.getKey()), 0.0);
						
					}
					//
				} 
				
				//Get the number of edges
				line = br.readLine();
				lineSplit = line.split("\\s+");
				String edge_cnt = lineSplit[1];
				
			
				// add the edges to the graph
				Map <String, Double> edge_weight_map  = new HashMap<String,Double>();
				for(int i=0 ; i < Integer.parseInt(edge_cnt); i++) {
					line = br.readLine();
					String[] ends= line.split(" ");
					Pair<String> vertexpairs = new Pair<String>(ends[0],ends[1]);
					graph.addEdge(Integer.toString(i), vertexpairs, EdgeType.DIRECTED);
					edge_weight_map.put(Integer.toString(i),1.0);
					
				}
				br.close();
				f.close();
				// implement page rank with priors
				double alpha = 0.1;
				
				//System.out.println(graph);
				Transformer<String, Double> authorPriorT= MapTransformer.getInstance(authorpriorNew);
				Transformer<String,Double> edgeWeight = MapTransformer.getInstance(edge_weight_map);
				PageRankWithPriors<String, String> pr = new PageRankWithPriors<String, String>(graph,authorPriorT,alpha);
				pr.getVertexPriors();
				pr.setMaxIterations(30);
				pr.evaluate();
				
				// get the results and display
				Map<String, Double> rank_results = new HashMap<String, Double>();
				for(String v: graph.getVertices()) {
					//System.out.println(v+ " "+ pr.getVertexScore(v));
					rank_results.put(v,(Double) pr.getVertexScore(v));	
				}
				
				//sort the ranks
				System.out.println("AuthorID   PageRank");
				rank_results.entrySet().stream()
			    .sorted(Map.Entry.<String, Double>comparingByValue().reversed()).limit(10)
			    .forEachOrdered(x -> System.out.println(authorMapRev.get(x.getKey()).toString().substring(1, authorMapRev.get(x.getKey()).toString().length() -1) +"   " + x.getValue().toString()));
			

	}

	public static void main(String[] args) throws IOException, ParseException {
		
		System.out.println("The Query String is: Data Mining\nThe top 10 authors are");
		authorRanker("Data Mining");
		
		System.out.println("\nThe Query String is: Information Retrieval\\nThe top 10 authors are");
		authorRanker("Information Retrieval");
	}

	
}
