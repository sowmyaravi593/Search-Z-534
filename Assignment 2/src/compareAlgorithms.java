import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.file.Paths;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.search.similarities.ClassicSimilarity;
import org.apache.lucene.search.similarities.LMDirichletSimilarity;
import org.apache.lucene.search.similarities.LMJelinekMercerSimilarity;
import org.apache.lucene.search.similarities.TFIDFSimilarity;
import org.apache.lucene.store.FSDirectory;

public class compareAlgorithms {
	
	public static final String TOPIC_FILE = "topics.51-100";

	public static final String index = "index";
	public static void main(String[] args) throws IOException, ParseException {
		// compare different search algorithms in this class
		
		IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths
				.get(index)));
		float N = reader.maxDoc();
		System.out.println("Size of the document: "+ N);
		List<LeafReaderContext> leafContexts = reader.getContext().reader()
				.leaves();
		
		// Read the input file , initialize the similarity and analyzer
		IndexSearcher searcher = new IndexSearcher(reader);
		searcher.setSimilarity(new BM25Similarity()); //You need to explicitly specify the ranking algorithm using the respective Similarity class	
		Analyzer analyzer = new StandardAnalyzer();
		QueryParser parser = new QueryParser("TEXT", analyzer);
		
		// Read the topic file and store it in a variable 
		FileReader fr = new FileReader(TOPIC_FILE);
		BufferedReader br = new BufferedReader(fr);
		String line = br.readLine(); 
		StringBuilder sb = new StringBuilder(); 
		while(line != null){ 
			sb.append(line).append("\n"); 
			line = br.readLine(); 
			} 
		String content = sb.toString(); 	
		//System.out.println(content);
		br.close();
		
		Pattern patternMaster = Pattern.compile("<top>(.*?)</top>",Pattern.DOTALL);
		Matcher matcherMaster = patternMaster.matcher(content);
		
		File fout = new File("BM25shortQuery.txt");
		FileOutputStream fos = new FileOutputStream(fout);
		OutputStreamWriter osw = new OutputStreamWriter(fos);
		
		File fout1 = new File("BM25longQuery.txt");
		FileOutputStream fos1 = new FileOutputStream(fout1);
		OutputStreamWriter osw1 = new OutputStreamWriter(fos1);
		
		File fout2 = new File("LMDirichletshortQuery.txt");
		FileOutputStream fos2 = new FileOutputStream(fout2);
		OutputStreamWriter osw2 = new OutputStreamWriter(fos2);
		
		File fout3 = new File("LMDirichletlongQuery.txt");
		FileOutputStream fos3 = new FileOutputStream(fout3);
		OutputStreamWriter osw3 = new OutputStreamWriter(fos3);
		
		File fout4 = new File("LMJelinekMercershortQuery.txt");
		FileOutputStream fos4 = new FileOutputStream(fout4);
		OutputStreamWriter osw4 = new OutputStreamWriter(fos4);
		
		File fout5 = new File("LMJelinekMercerlongQuery.txt");
		FileOutputStream fos5 = new FileOutputStream(fout5);
		OutputStreamWriter osw5 = new OutputStreamWriter(fos5);
		
		File fout6 = new File("ClassicSimilarityshortQuery.txt");
		FileOutputStream fos6 = new FileOutputStream(fout6);
		OutputStreamWriter osw6 = new OutputStreamWriter(fos6);
		
		File fout7 = new File("ClassicSimilaritylongQuery.txt");
		FileOutputStream fos7 = new FileOutputStream(fout7);
		OutputStreamWriter osw7 = new OutputStreamWriter(fos7);
		
		while (matcherMaster.find()) {
			String doc = matcherMaster.group(1);

			Pattern patternT = Pattern.compile("<num>.*?Number:.*?(.*?)<dom>",Pattern.DOTALL );
			Matcher matcherT = patternT.matcher(doc);
			
			String topid = "";
			while (matcherT.find()) {
				topid = matcherT.group(1).trim().replace("\n", "").replace("\r", "").replace("              ","");
			}
			
			// Create pattern matchers for short query
			Pattern patternsq = Pattern.compile("<title>.*?Topic:.*?(.*?)<desc>",Pattern.DOTALL );
			Matcher matchersq = patternsq.matcher(doc);
			
			String shortQuery = "";
			while (matchersq.find()) {
				shortQuery = matchersq.group(1).trim().replace("\n", "").replace("\r", "").replace("              ","");
			}
			
			// Create pattern matchers for long query
			Pattern patternlq = Pattern.compile("<desc>.*?Description:.*?(.*?)<smry>",Pattern.DOTALL );
			Matcher matcherlq = patternlq.matcher(doc);
			
			String longQuery = "";
			while (matcherlq.find()) {
				longQuery = matcherlq.group(1).trim().replace("\n", "").replace("\r", "").replace("              ","");
			}
			
			// for short query
			// for BM25 model
			searcher.setSimilarity(new BM25Similarity());
			Query query = parser.parse(QueryParser.escape(shortQuery).replace("NOT", ""));
			TopScoreDocCollector collector = TopScoreDocCollector.create(1000);
			searcher.search(query, collector);
			ScoreDoc[] docs = collector.topDocs().scoreDocs;
			for (int i = 0; i < docs.length; i++) {
				Document doci = searcher.doc(docs[i].doc);
		    	osw.write(Integer.parseInt(topid) +" "+ "0" + " "+ doci.get("DOCNO") +"  "+ String.valueOf(i+1)+" "+ docs[i].score + " bm25-run-1");
		    	osw.write("\n");
				//System.out.println(doci.get("DOCNO")+" "+docs[i].score);
			} 
			
			
			// for long query 
			Query query2 = parser.parse(QueryParser.escape(longQuery).replace("NOT", ""));
			TopScoreDocCollector collector2 = TopScoreDocCollector.create(1000);
			searcher.search(query2, collector2);
			ScoreDoc[] docs2 = collector2.topDocs().scoreDocs;
			for (int i = 0; i < docs2.length; i++) {
				Document doci = searcher.doc(docs2[i].doc);
		    	osw1.write(Integer.parseInt(topid) +" "+ "0" + " "+ doci.get("DOCNO") +"  "+ String.valueOf(i+1)+" "+ docs2[i].score + " bm25-run-1");
		    	osw1.write("\n");
				//System.out.println(doci.get("DOCNO")+" "+docs[i].score);
			}
			
			
			// LMDirichlet Model
			searcher.setSimilarity(new LMDirichletSimilarity());
			collector = TopScoreDocCollector.create(1000);
			searcher.search(query, collector);
			docs = collector.topDocs().scoreDocs;
			for (int i = 0; i < docs.length; i++) {
				Document doci = searcher.doc(docs[i].doc);
		    	osw2.write(Integer.parseInt(topid) +" "+ "0" + " "+ doci.get("DOCNO") +"  "+ String.valueOf(i+1)+" "+ docs[i].score + " lmd-run-1");
		    	osw2.write("\n");
			}
			
			collector = TopScoreDocCollector.create(1000);
			searcher.search(query2, collector);
			docs = collector.topDocs().scoreDocs;
			for (int i = 0; i < docs.length; i++) {
				Document doci = searcher.doc(docs[i].doc);
		    	osw3.write(Integer.parseInt(topid) +" "+ "0" + " "+ doci.get("DOCNO") +"  "+ String.valueOf(i+1)+" "+ docs[i].score + " lmd-run-1");
		    	osw3.write("\n");
			}
			
			// LMDirichlet Model
			searcher.setSimilarity(new LMJelinekMercerSimilarity(0.7f));
			collector = TopScoreDocCollector.create(1000);
			searcher.search(query, collector);
			docs = collector.topDocs().scoreDocs;
			for (int i = 0; i < docs.length; i++) {
				Document doci = searcher.doc(docs[i].doc);
		    	osw4.write(Integer.parseInt(topid) +" "+ "0" + " "+ doci.get("DOCNO") +"  "+ String.valueOf(i+1)+" "+ docs[i].score + " lmj-run-1");
		    	osw4.write("\n");
			}
			
			collector = TopScoreDocCollector.create(1000);
			searcher.search(query2, collector);
			docs = collector.topDocs().scoreDocs;
			for (int i = 0; i < docs.length; i++) {
				Document doci = searcher.doc(docs[i].doc);
		    	osw5.write(Integer.parseInt(topid) +" "+ "0" + " "+ doci.get("DOCNO") +"  "+ String.valueOf(i+1)+" "+ docs[i].score + " lmj-run-1");
		    	osw5.write("\n");
			}
			
			// Classic similarity Vector space model
			searcher.setSimilarity(new ClassicSimilarity());
			collector = TopScoreDocCollector.create(1000);
			searcher.search(query, collector);
			docs = collector.topDocs().scoreDocs;
			for (int i = 0; i < docs.length; i++) {
				Document doci = searcher.doc(docs[i].doc);
		    	osw6.write(Integer.parseInt(topid) +" "+ "0" + " "+ doci.get("DOCNO") +"  "+ String.valueOf(i+1)+" "+ docs[i].score + " classic-run-1");
		    	osw6.write("\n");
			}
			
			collector = TopScoreDocCollector.create(1000);
			searcher.search(query2, collector);
			docs = collector.topDocs().scoreDocs;
			for (int i = 0; i < docs.length; i++) {
				Document doci = searcher.doc(docs[i].doc);
		    	osw7.write(Integer.parseInt(topid) +" "+ "0" + " "+ doci.get("DOCNO") +"  "+ String.valueOf(i+1)+" "+ docs[i].score + " classic-run-1");
		    	osw7.write("\n");
			}
			

		}
		
		
		analyzer.close();
		osw.close();
		osw1.close();
		osw2.close();
		osw3.close();
		osw4.close();
		osw5.close();
		osw6.close();
		osw7.close();
	}
}
