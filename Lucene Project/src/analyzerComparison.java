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
import org.apache.lucene.analysis.core.KeywordAnalyzer;
import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.analysis.core.StopAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;


public class analyzerComparison {

public static final String INDEX_DIRECTORY_1 = "C:\\Users\\sowmy\\OneDrive\\IU\\Fall 2017\\Search\\Assignment1\\index3_keyword";
public static final String INDEX_DIRECTORY_2 = "C:\\Users\\sowmy\\OneDrive\\IU\\Fall 2017\\Search\\Assignment1\\index3_simple";
public static final String INDEX_DIRECTORY_3 = "C:\\Users\\sowmy\\OneDrive\\IU\\Fall 2017\\Search\\Assignment1\\index3_stop";
	
	public static final String CORPUS_DIRECTORY = "C:\\Users\\sowmy\\OneDrive\\IU\\Fall 2017\\Search\\Assignment1\\corpus";


	private static IndexWriter writer;
	
	private static IndexWriter writer2;
	
	private static IndexWriter writer3;

	private static  File[] corpusFiles;
	
     
	public static void main(String[] args) throws IOException, ParseException {
		
		Directory dir1 = FSDirectory.open(Paths.get(INDEX_DIRECTORY_1));
		Directory dir2 = FSDirectory.open(Paths.get(INDEX_DIRECTORY_2));
		Directory dir3 = FSDirectory.open(Paths.get(INDEX_DIRECTORY_3));
		
		Analyzer analyzer = new KeywordAnalyzer();
		IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
		iwc.setOpenMode(OpenMode.CREATE);
		writer = new IndexWriter(dir1, iwc);
		
		Analyzer analyzer2 = new SimpleAnalyzer();
		IndexWriterConfig iwc2 = new IndexWriterConfig(analyzer2);
		iwc2.setOpenMode(OpenMode.CREATE);
		writer2 = new IndexWriter(dir2, iwc2);
		
		Analyzer analyzer3 = new StopAnalyzer();
		IndexWriterConfig iwc3 = new IndexWriterConfig(analyzer3);
		iwc3.setOpenMode(OpenMode.CREATE);
		writer3 = new IndexWriter(dir3, iwc3);
		
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
				luceneDoc.add(new TextField("DATELINE", dateline, Field.Store.NO ));
				writer.addDocument(luceneDoc);
				writer2.addDocument(luceneDoc);
				writer3.addDocument(luceneDoc);
				
			}
			
			
			buf.close();
			is.close();
			
			//
			//break;
			
		}
		 writer.commit();
		 writer.forceMerge(1);
		 writer.close();
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                     		 
		 writer2.commit();
		 writer2.forceMerge(1);
		 writer2.close();
		 
		 writer3.commit();
		 writer3.forceMerge(1);
		 writer3.close();
		

        
		}

}
