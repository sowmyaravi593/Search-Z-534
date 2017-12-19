import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

public class generateIndex {
	public static final String INDEX_DIRECTORY = "C:\\Users\\sowmy\\OneDrive\\IU\\Fall 2017\\Search\\Assignment1\\index3";
	
	public static final String CORPUS_DIRECTORY = "C:\\Users\\sowmy\\OneDrive\\IU\\Fall 2017\\Search\\Assignment1\\corpus";

	// private static Directory dir;

	private static IndexWriter writer;

	private static File[] corpusFiles;
	
    
	public static void main(String[] args) throws IOException, ParseException {
		
		Directory dir = FSDirectory.open(Paths.get(INDEX_DIRECTORY));
		Analyzer analyzer = new StandardAnalyzer();
		IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
		iwc.setOpenMode(OpenMode.CREATE);
		writer = new IndexWriter(dir, iwc);
		corpusFiles = new File(CORPUS_DIRECTORY).listFiles();
		
		
		
		for(File f: corpusFiles)
		{
			InputStream is = new FileInputStream(f);
			BufferedReader buf = new BufferedReader(new InputStreamReader(is)); 
			String line = buf.readLine(); 
			StringBuilder sb = new StringBuilder(); 
			while(line != null){ 
				sb.append(line).append("\n"); 
				line = buf.readLine(); 
				} 
			String content = sb.toString(); 

			Pattern patternMaster = Pattern.compile("<DOC>(.*?)</DOC>",Pattern.DOTALL);
			Matcher matcherMaster = patternMaster.matcher(content);
			
			// DOCUMENT PARSER
			while (matcherMaster.find()) {
				String doc = matcherMaster.group(1);
				
				Pattern pattern = Pattern.compile("<DOCNO>(.+?)</DOCNO>");
				Matcher matcher = pattern.matcher(doc);
				
				// DOC number parser
				String doc_no = "";
				while (matcher.find()) {
					doc_no = doc_no + " " + matcher.group(1);
					//doc_list.add(doc_no);
					//document.add(new StringField("DOCNO", doc_no, Field.Store.YES));
					
				}
				
				Pattern pattern2 = Pattern.compile("<HEAD>(.*?)</HEAD>");
				Matcher matcher2 = pattern2.matcher(doc);
				
				
				// Add head
				
				String head = "";
				while (matcher2.find()) {
					head = head + " " +  matcher2.group(1);
					//head_list.add(head);
					//document.add(new TextField("HEAD", head, Field.Store.YES));
					
				}
		
				Pattern pattern3 = Pattern.compile("<TEXT>(.*?)</TEXT>",Pattern.DOTALL);
				Matcher matcher3 = pattern3.matcher(doc);
				
				
				// Text extractor
				String text = " ";
				while (matcher3.find()) {
					text = text + " " + matcher3.group(1);
					//text_list.add(text);
					//document.add(new TextField("TEXT", text, Field.Store.YES));
					
				}
				
				Pattern pattern4 = Pattern.compile("<BYLINE>(.*?)</BYLINE>",Pattern.DOTALL);
				Matcher matcher4 = pattern4.matcher(doc);
				
				
				// byline extractor
				String byline = "";
				while (matcher4.find()) {
					byline = byline + " " + matcher4.group(1);
				//	byline_list.add(byline);
					//document.add(new TextField("TEXT", text, Field.Store.YES));
					
				}
				
				Pattern pattern5 = Pattern.compile("<DATELINE>(.*?)</DATELINE>",Pattern.DOTALL);
				Matcher matcher5 = pattern5.matcher(doc);
				
				
				// byline extractor
				String dateline = "";
				while (matcher5.find()) {
					dateline = dateline + " " + matcher5.group(1);
		//			dateline_list.add(dateline);
					//document.add(new TextField("TEXT", text, Field.Store.YES));
					
				}
				
				Document luceneDoc = new Document();   
				luceneDoc.add(new StringField("DOCNO", doc_no, Field.Store.YES));
				luceneDoc.add(new TextField("HEAD", head, Field.Store.NO));
				luceneDoc.add(new TextField("TEXT", text, Field.Store.NO));
				luceneDoc.add(new TextField("BYLINE", byline, Field.Store.NO));
				luceneDoc.add(new TextField("DATELINE", dateline, Field.Store.NO));
				writer.addDocument(luceneDoc);
			
				
			}
			
			
			buf.close();
			is.close();
			
			//
			//break;
			
		}
		 writer.commit();
		 writer.forceMerge(1);
		 writer.close();
		
		IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(INDEX_DIRECTORY)));
		System.out.println("Total number of documents in the corpus: "+reader.maxDoc());  
		System.out.println("Number of documents containing the term \"Potitics\" for field \"TEXT\": "+reader.docFreq(new Term("TEXT", "new")));
		
		}
		

    }

   
