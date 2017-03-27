package boost_title;
//package lia.meetlucene;

/**
 * Copyright Manning Publications Co.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific lan      
*/

import org.apache.lucene.document.Document;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.Directory;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.util.Version;
import org.apache.lucene.index.*;
import org.apache.lucene.search.ScoreDoc;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Scanner;

// From chapter 1

/**
 * This code was originally written for
 * Erik's Lucene intro java.net article
 */
@SuppressWarnings("unused")
public class Searcher {
	public static int count2 = 0;
	public static int count = 0;
  public static int [][] relmat = new int[1837][3];
  public static void main(String[] args) throws IllegalArgumentException,
        IOException, ParseException {
    if (args.length != 2) {
      throw new IllegalArgumentException("Usage: java " + Searcher.class.getName()
        + " <index dir> <query>");
    }
    
    File fileIn = new File("cran/cranqrel.txt");
    Scanner scanner = new Scanner(fileIn);
    
    int k=0;
    while(scanner.hasNextInt()){
    	int q_id = scanner.nextInt();
    	relmat[k][0] = q_id;
    	int rel_doc = scanner.nextInt();
    	relmat[k][1] = rel_doc;
    	int rel_lev = scanner.nextInt();
    	relmat[k][2] = rel_lev;
    	k++;
    }
    
    String indexDir = args[0];
	  try (BufferedReader br = new BufferedReader(new FileReader("cran/cranqry.txt"))) {
		  	int qid = 0;
			String sCurrentLine;
			String question = "";
			sCurrentLine = br.readLine();
			while (sCurrentLine != null) {
				if(sCurrentLine.contains(".I")){
					String[] splited = sCurrentLine.split(" ");
					qid = Integer.parseInt(splited[1]);
				}
				if(sCurrentLine != null && sCurrentLine.contains(".W")){
					sCurrentLine = br.readLine();
					while(sCurrentLine != null && !sCurrentLine.contains(".I")){
						question = question +sCurrentLine+"\n";
						sCurrentLine = br.readLine();
					}
				    search(indexDir,"title", question,qid);
					question = "";
				}
				else{
					sCurrentLine = br.readLine();
				}
			}
		}
	  	catch (IOException e) {
	  		e.printStackTrace();
		}
  }
  
  public static void search(String indexDir,String part, String q,int qid) throws IOException, ParseException {

    File ff=new File(indexDir);
    Directory dir = FSDirectory.open(ff.toPath());
    IndexReader reader = DirectoryReader.open(dir);
    IndexSearcher is = new IndexSearcher(reader);
    
    MultiFieldQueryParser parser = new MultiFieldQueryParser(new String[]{"title", "author", "affiliation","content"}, new StandardAnalyzer());
    parser.setDefaultOperator(QueryParser.OR_OPERATOR);

    //QueryParser parser = new QueryParser( part,new StandardAnalyzer());
    Query query = parser.parse(q); 
    
    long start = System.currentTimeMillis();
    TopDocs hits = is.search(query, 1400); //5
    long end = System.currentTimeMillis();

    System.err.println("Found " + hits.totalHits +
      " document(s) (in " + (end - start) +
      " milliseconds) that matched query '" + 
      q + "':");
    count = 0;
	count2 = 0;
    for(ScoreDoc scoreDoc : hits.scoreDocs) {
      Document doc = is.doc(scoreDoc.doc);               //7
      count = check(relmat,qid,doc,count);
      /*System.out.println(doc.get("docid"));
      System.out.println(doc.get("fullpath"));  //8
      String ts = doc.get("title");
      System.out.println(ts);*/
    }
    System.out.println("Total relevant docs:"+count+" Retrieved relevant docs:"+count2);
    System.out.println("Precision:"+(float)count2/hits.totalHits);
    System.out.println("Recall:"+(float)count2/count);
   reader.close();                                //9
  }
  public static int check(int[][] relmat,int qid,Document doc,int count){
	  count=0;
	  for(int i=0;i<relmat.length;i++){
		  if(relmat[i][0]==qid){
			  count++;
			  if(relmat[i][1]==Integer.parseInt(doc.get("docid")) && relmat[i][2]>=3){
				  count2++;
			  }
			  else{
				  continue;
			  }
		  }
		  else{
			  continue;
		  }
	  }
	  return count;
  }
}

/*
#1 Parse provided index directory
#2 Parse provided query string
#3 Open index
#4 Parse query
#5 Search index
#6 Write search stats
#7 Retrieve matching document
#8 Display filename
#9 Close IndexSearcher
*/