import java.io.IOException;
import java.nio.file.Paths;

import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.Terms;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.store.FSDirectory;

public class searchIndexComparison {
	
	public static final String INDEX_DIRECTORY_1 = "C:\\Users\\sowmy\\OneDrive\\IU\\Fall 2017\\Search\\Assignment1\\index3_keyword";
	public static final String INDEX_DIRECTORY_2 = "C:\\Users\\sowmy\\OneDrive\\IU\\Fall 2017\\Search\\Assignment1\\index3_simple";
	public static final String INDEX_DIRECTORY_3 = "C:\\Users\\sowmy\\OneDrive\\IU\\Fall 2017\\Search\\Assignment1\\index3_stop";

	public static void main(String[] args) throws IOException, ParseException {	
		IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(INDEX_DIRECTORY_1)));
		IndexReader reader2 = DirectoryReader.open(FSDirectory.open(Paths.get(INDEX_DIRECTORY_2)));
		IndexReader reader3 = DirectoryReader.open(FSDirectory.open(Paths.get(INDEX_DIRECTORY_3)));
	
		Terms vocabulary = MultiFields.getTerms(reader, "TEXT");
		Terms vocabulary2 = MultiFields.getTerms(reader2, "TEXT");
		Terms vocabulary3 = MultiFields.getTerms(reader3, "TEXT");
		
		
		System.out.println("Keyword Analyzer");
        System.out.println("Number of tokens for this field: "+vocabulary.getSumTotalTermFreq());
        System.out.println("Size of the vocabulary for this field: "+vocabulary.size());
        
		System.out.println("Simple Analyzer");
        System.out.println("Number of tokens for this field: "+vocabulary2.getSumTotalTermFreq());
        System.out.println("Size of the vocabulary for this field: "+vocabulary2.size());
        
		System.out.println("Stop Analyzer");
        System.out.println("Number of tokens for this field: "+vocabulary3.getSumTotalTermFreq());
        System.out.println("Size of the vocabulary for this field: "+vocabulary3.size());
        

        /*
        TermsEnum iterator = vocabulary3.iterator();
        BytesRef byteRef = null;
        System.out.println("\n*******Vocabulary-Start**********");

        while((byteRef = iterator.next()) != null) {

            String term = byteRef.utf8ToString();

            System.out.print(term+"\t");

        }
        System.out.println("\n*******Vocabulary-End**********");  
        */
        
       
	}

}
