import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.PostingsEnum;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.similarities.ClassicSimilarity;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;


public class searchTRECtopics {

	// Import the topic file. 

	public static final String TOPIC_FILE = "topics.51-100";
	private static String line;
	
	public static void main(String[] args) throws IOException, ParseException {	
		String index = "index";
		IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths
				.get(index)));
		float N = reader.maxDoc();
		// Get the preprocessed query terms
		Analyzer analyzer = new StandardAnalyzer();
		QueryParser parser = new QueryParser("TEXT", analyzer);
		IndexSearcher searcher = new IndexSearcher(reader);
		
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
		List<LeafReaderContext> leafContexts = reader.getContext().reader()
				.leaves();
		ClassicSimilarity dSimi = new ClassicSimilarity();
		
		Pattern patternMaster = Pattern.compile("<top>(.*?)</top>",Pattern.DOTALL);
		Matcher matcherMaster = patternMaster.matcher(content);
		
		File fout = new File("MyAlgoShortQuery.txt");
		FileOutputStream fos = new FileOutputStream(fout);
	 
		OutputStreamWriter osw = new OutputStreamWriter(fos);
		
		File fout2 = new File("MyAlgoLongQuery.txt");
		FileOutputStream fos2 = new FileOutputStream(fout2);
		OutputStreamWriter osw2 = new OutputStreamWriter(fos2);
		int queryID = 1;
		while (matcherMaster.find()) {
			String doc = matcherMaster.group(1);
			///get the tile for each doc 
			Pattern patternT = Pattern.compile("<num>.*?Number:.*?(.*?)<dom>",Pattern.DOTALL );
			Matcher matcherT = patternT.matcher(doc);
			
			String topid = "";
			while (matcherT.find()) {
				topid = matcherT.group(1).trim().replace("\n", "").replace("\r", "").replace("              ","");
			}
			
			
			Pattern pattern = Pattern.compile("<title>.*?Topic:.*?(.*?)<desc>",Pattern.DOTALL );
			Matcher matcher = pattern.matcher(doc);
			
			String queryString = "";
			while (matcher.find()) {
				queryString = matcher.group(1).trim().replace("\n", "").replace("\r", "").replace("              ","");
			}
			
			// get the match for description
			Pattern pattern1 = Pattern. compile("<desc>.*?Description:.*?(.*?)<smry>",Pattern.DOTALL );
			Matcher matcher1 = pattern1.matcher(doc);
			
			Map<Integer, Float> map = new HashMap();
			Map<Integer,Float> map2 = new HashMap();
			String queryString2 = "";
			while (matcher1.find()) {
				queryString2 = matcher1.group(1).trim().replace("\n", " ").replace("\r", " ").replace("               ","");
			}
			 //System.out.println(queryString);
			 Set<Term> queryTerms = new LinkedHashSet<Term>();
			 Query query = parser.parse(QueryParser.escape(queryString));
			 searcher.createNormalizedWeight(query, false).extractTerms(queryTerms);
			 
			 Set<Term> queryTerms2 = new LinkedHashSet<Term>();
			 Query query2 = parser.parse(QueryParser.escape(queryString2));
			 searcher.createNormalizedWeight(query2, false).extractTerms(queryTerms2);
			 
			 
			 //System.out.println("The query is: "+ queryString);
			    map = new HashMap();
				for (int i = 0; i < leafContexts.size(); i++) {
					LeafReaderContext leafContext = leafContexts.get(i);
					int startDocNo = leafContext.docBase;

					
					PostingsEnum k = null;
					
					for (Term t : queryTerms) {
							String str = t.text();
							int df = reader.docFreq(t);
							float queryRelevance = (float) Math.log(1 + (N/df));
							//System.out.println(queryRelevance);
							
							//System.out.println(str);
						// get frequency of query term from postings
						PostingsEnum de = MultiFields.getTermDocsEnum(leafContext.reader(),
								"TEXT", new BytesRef(str));
						
						int doci;
						
						if (de != null) {
							while ((doci = de.nextDoc()) != PostingsEnum.NO_MORE_DOCS) {
								
								float normDocLeng = dSimi.decodeNormValue(leafContext.reader()
										.getNormValues("TEXT").get(de.docID()));
								float docLeng = 1 / (normDocLeng * normDocLeng);
								
								float term_count =  de.freq();
								
								float tf_idf = (float) ((term_count/ docLeng) * Math.log(1 + (N/df)));
								
								
								//System.out.println("The tfidf score for Q" +queryID + " docno"+ searcher.doc(de.docID() + startDocNo).get("DOCNO") +" is : "+ tf_idf );
								//System.out.println();
								map.put(de.docID() + startDocNo, tf_idf);
							}
						}
		
						
					}
					
				}

				// now sort the map to get the top 100 elements
				List<Map.Entry<Integer, Float>> list =
		                new LinkedList<Map.Entry<Integer, Float>>(map.entrySet());
				Collections.sort(list, new Comparator<Entry<Integer,Float>>() {
		            public int compare(Entry<Integer, Float> o1, Entry<Integer, Float> o2) {
		            	return (o2.getValue()).compareTo( o1.getValue() );
		            }
		       });
				
				Map<Integer, Float> sortedMap = new LinkedHashMap<Integer, Float>();
			    for (Entry<Integer, Float> entry : list) {
			      sortedMap.put(entry.getKey(), entry.getValue());
			    }
			    if (sortedMap.size() >0 &  sortedMap.size() <=1000) {
			    List<Float> topDocsValues = Collections.list(Collections.enumeration(sortedMap.values())).subList(0, sortedMap.size());
			    List<Integer> topDocsKeys = Collections.list(Collections.enumeration(sortedMap.keySet())).subList(0, sortedMap.size());
			    for (int i1=0; i1 < sortedMap.size(); i1++) {
			    	osw.write(Integer.parseInt(topid) +" "+ "0" + " " +String.valueOf(searcher.doc(topDocsKeys.get(i1)).get("DOCNO"))+"  "+ String.valueOf(i1+1)+" "+ String.valueOf(topDocsValues.get(i1))+ " run-1");
			    	osw.write("\n");
			    	//System.out.println("Doc number: "+ String.valueOf(topDocsKeys.get(i1))+" Rank: "+ String.valueOf(i1+1)+" Score: "+ String.valueOf(topDocsValues.get(i1)));
			    }
			    }
			    else if(sortedMap.size() >0 &  sortedMap.size() > 1000) {
			    	
			    	List<Float> topDocsValues = Collections.list(Collections.enumeration(sortedMap.values())).subList(0, 1000);
				    List<Integer> topDocsKeys = Collections.list(Collections.enumeration(sortedMap.keySet())).subList(0, 1000);
				    for (int i1=0; i1 < 1000; i1++) {
				    	osw.write(Integer.parseInt(topid) +" "+ "0" + " "+ String.valueOf(searcher.doc(topDocsKeys.get(i1)).get("DOCNO"))+"  "+ String.valueOf(i1+1)+" "+ String.valueOf(topDocsValues.get(i1)) + " run-1");
				    	osw.write("\n");
				    	// "Topic Id:"+topid +" Doc number: "+ String.valueOf(searcher.doc(topDocsKeys.get(i1)).get("DOCNO"))+" Rank: "+ String.valueOf(i1+1)+" Score: "+ String.valueOf(topDocsValues.get(i1))
				    }
			    	
			    }
			    
			    
			    // Long Query
			    map2 = new HashMap();
				for (int i = 0; i < leafContexts.size(); i++) {
					LeafReaderContext leafContext = leafContexts.get(i);
					int startDocNo = leafContext.docBase;

					
					PostingsEnum k = null;
					
					for (Term t : queryTerms2) {
							String str = t.text();
							int df = reader.docFreq(t);
							float queryRelevance = (float) Math.log(1 + (N/df));
							//System.out.println(queryRelevance);
							
							//System.out.println(str);
						// get frequency of query term from postings
						PostingsEnum de = MultiFields.getTermDocsEnum(leafContext.reader(),
								"TEXT", new BytesRef(str));
						
						int doci;
						
						if (de != null) {
							while ((doci = de.nextDoc()) != PostingsEnum.NO_MORE_DOCS) {
								
								float normDocLeng = dSimi.decodeNormValue(leafContext.reader()
										.getNormValues("TEXT").get(de.docID()));
								float docLeng = 1 / (normDocLeng * normDocLeng);
								
								float term_count =  de.freq();
								
								float tf_idf = (float) ((term_count/ docLeng) * Math.log(1 + (N/df)));
								
								
								//System.out.println("The tfidf score for Q" +queryID + " docno"+ searcher.doc(de.docID() + startDocNo).get("DOCNO") +" is : "+ tf_idf );
								//System.out.println();
								map2.put(de.docID() + startDocNo, tf_idf);
							}
						}
		
						
					}
					
				}
				List<Map.Entry<Integer, Float>> list2 =
		                new LinkedList<Map.Entry<Integer, Float>>(map2.entrySet());
				Collections.sort(list2, new Comparator<Entry<Integer,Float>>() {
		            public int compare(Entry<Integer, Float> o1, Entry<Integer, Float> o2) {
		            	return (o2.getValue()).compareTo( o1.getValue() );
		            }
		       });
				
				Map<Integer, Float> sortedMap2 = new LinkedHashMap<Integer, Float>();
			    for (Entry<Integer, Float> entry : list2) {
			    	sortedMap2.put(entry.getKey(), entry.getValue());
			    }
			    if (sortedMap2.size() >0 &  sortedMap2.size() <=1000) {
			    List<Float> topDocsValues = Collections.list(Collections.enumeration(sortedMap2.values())).subList(0, sortedMap2.size());
			    List<Integer> topDocsKeys = Collections.list(Collections.enumeration(sortedMap2.keySet())).subList(0, sortedMap2.size());
			    for (int i1=0; i1 < sortedMap2.size(); i1++) {
			    	osw2.write(Integer.parseInt(topid) +" "+ "0" + " "+ String.valueOf(searcher.doc(topDocsKeys.get(i1)).get("DOCNO"))+"  "+ String.valueOf(i1+1)+" "+ String.valueOf(topDocsValues.get(i1))+ " myalgo-run-1");
			    	osw2.write("\n");
			    	//System.out.println("Doc number: "+ String.valueOf(topDocsKeys.get(i1))+" Rank: "+ String.valueOf(i1+1)+" Score: "+ String.valueOf(topDocsValues.get(i1)));
			    }
			    }
			    else if(sortedMap2.size() >0 &  sortedMap2.size() > 1000) {
			    	
			    	List<Float> topDocsValues = Collections.list(Collections.enumeration(sortedMap2.values())).subList(0, 1000);
				    List<Integer> topDocsKeys = Collections.list(Collections.enumeration(sortedMap2.keySet())).subList(0, 1000);
				    for (int i1=0; i1 < 1000; i1++) {
				    	osw2.write(Integer.parseInt(topid) +" "+ "0" + " "+ String.valueOf(searcher.doc(topDocsKeys.get(i1)).get("DOCNO"))+"  "+ String.valueOf(i1+1)+" "+ String.valueOf(topDocsValues.get(i1)) + " myalgo-run-1");
				    	osw2.write("\n");
				    	// "Topic Id:"+topid +" Doc number: "+ String.valueOf(searcher.doc(topDocsKeys.get(i1)).get("DOCNO"))+" Rank: "+ String.valueOf(i1+1)+" Score: "+ String.valueOf(topDocsValues.get(i1))
				    }
			    	
			    }
				
				queryID += 1;
				
				
		}
		
		osw.close();
		osw2.close();
		
	}
	

}
