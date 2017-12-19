import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

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
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.search.similarities.ClassicSimilarity;

public class easySearch {

	private static String str;

	public static void main(String[] args) throws IOException, ParseException {
		
		String index = "C:\\Users\\sowmy\\OneDrive\\IU\\Fall 2017\\Search\\Assignment 2\\index";
		IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths
				.get(index)));
		float N = reader.maxDoc();
		// Get the preprocessed query terms
		Analyzer analyzer = new StandardAnalyzer();
		QueryParser parser = new QueryParser("TEXT", analyzer);
		IndexSearcher searcher = new IndexSearcher(reader);
		
		String queryString = "Indiana";
		String[] splited = queryString.split(" ");
		Query query = parser.parse(queryString);
		Set<Term> queryTerms = new LinkedHashSet<Term>();
		searcher.createNormalizedWeight(query, false).extractTerms(queryTerms);
		System.out.println("Terms in the query: ");
		for (Term t : queryTerms) {
			System.out.println(t.text());
			int df=reader.docFreq(t);
			System.out.println("Number of documents containing the term: "+df);
			System.out.println();
		}
		
		List<LeafReaderContext> leafContexts = reader.getContext().reader()
				.leaves();
		
		
		ClassicSimilarity dSimi = new ClassicSimilarity();
		// Processing each segment
		for (int i = 0; i < leafContexts.size(); i++) {
			LeafReaderContext leafContext = leafContexts.get(i);
			int startDocNo = leafContext.docBase;
			int numberOfDoc = leafContext.reader().maxDoc();
			for (int docId = 0; docId < numberOfDoc; docId++) {
				// Get normalized length (1/sqrt(numOfTokens)) of the document
				float normDocLeng = dSimi.decodeNormValue(leafContext.reader()
						.getNormValues("TEXT").get(docId));
				// Get length of the document
				float docLeng = 1 / (normDocLeng * normDocLeng);
				//System.out.println("Length of doc(" + (docId + startDocNo)
					//	+ ", " + searcher.doc(docId + startDocNo).get("DOCNO")
					//	+ ") is " + docLeng);
			}
			System.out.println();
			
			PostingsEnum k = null;
			
			for (Term t : queryTerms) {
					str = t.text();
					int df = reader.docFreq(t);
					float queryRelevance = (float) Math.log(1 + (N/df));
					//System.out.println(queryRelevance);
					
					//System.out.println(str);
				// get frequency of query term from postings
				PostingsEnum de = MultiFields.getTermDocsEnum(leafContext.reader(),
						"TEXT", new BytesRef(str));
				
				int doc;
	
				if (de != null) {
					while ((doc = de.nextDoc()) != PostingsEnum.NO_MORE_DOCS) {
						
						float normDocLeng = dSimi.decodeNormValue(leafContext.reader()
								.getNormValues("TEXT").get(de.docID()));
						float docLeng = 1 / (normDocLeng * normDocLeng);
						
						float term_count =  de.freq();
						
						float tf_idf = (float) ((term_count/ docLeng) * Math.log(1 + (N/df)));
						
						
						System.out.println("The term freq for docno"+ searcher.doc(de.docID() + startDocNo).get("DOCNO") +" is : "+ tf_idf );
						System.out.println();
					}
				}
			}
		}
		 
			

	}

}
