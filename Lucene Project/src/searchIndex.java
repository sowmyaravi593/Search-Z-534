import java.io.IOException;
import java.nio.file.Paths;

import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;

public class searchIndex {

	public static void main(String[] args) throws IOException, ParseException {	
		String index = "C:\\Users\\sowmy\\OneDrive\\IU\\Fall 2017\\Search\\Assignment1\\index3";
		IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths
				.get(index)));
		
	
		Terms vocabulary = MultiFields.getTerms(reader, "TEXT");
		
		System.out.println("Total number of documents in the corpus: "+reader.maxDoc()); 
        System.out.println("Number of tokens for this field: "+vocabulary.getSumTotalTermFreq());
        System.out.println("Size of the vocabulary for this field: "+vocabulary.size());
        
        System.out.println(vocabulary);
        
       
        TermsEnum iterator = vocabulary.iterator();
        BytesRef byteRef = null;
        System.out.println("\n*******Vocabulary-Start**********");

        while((byteRef = iterator.next()) != null) {

            String term = byteRef.utf8ToString();

            System.out.print(term+"\t");

        }
        System.out.println("\n*******Vocabulary-End**********");        

        
        reader.close();

    }

    		
}
