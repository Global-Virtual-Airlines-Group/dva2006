package org.deltava.search;

import java.io.*;
import java.util.*;

import org.apache.lucene.analysis.*;
import org.apache.lucene.document.DateTools;
import org.apache.lucene.index.*;
import org.apache.lucene.search.*;
import org.apache.lucene.store.*;

import org.deltava.util.*;

import junit.framework.TestCase;

@Deprecated
public class TestSearchIndex extends TestCase {
	
	private IndexReader _ir;
	private IndexSearcher _is;

	protected void setUp() throws Exception {
		super.setUp();
		
		// Get the index
		File idxPath = new File("c:\\temp\\coolerIdx");
		assertTrue(idxPath.isDirectory());
		Directory d = FSDirectory.getDirectory(idxPath);
		assertTrue(IndexReader.indexExists(d));
		_ir = IndexReader.open(d, true);
		assertNotNull(_ir);
		_is = new IndexSearcher(_ir);
	}

	protected void tearDown() throws Exception {
		_is.close();
		_ir.close();
		super.tearDown();
	}

	public void testSimpleSearch() throws IOException {
		
		TermQuery tq = new TermQuery(new Term("body", "acars"));
		TopDocs docs = _is.search(tq, 100);
		assertNotNull(docs);
		ScoreDoc[] sdocs = docs.scoreDocs;;
		assertNotNull(sdocs);
		assertTrue(sdocs.length > 0);
	}
	
	public void testMultiWordSearch() throws IOException {
	
		Analyzer a = SearchUtils.getAnaylyzer();
		assertNotNull(a);
		TokenStream ts = a.tokenStream("body", new StringReader("framework exception"));
		assertNotNull(ts);
		
		// Build the query
		BooleanQuery bq = new BooleanQuery();
		Token t = ts.next(new Token());
		while (t != null) {
			TermQuery tq = new TermQuery(new Term("body", t.term()));
			bq.add(tq, BooleanClause.Occur.MUST);
			t = ts.next(t);
		}
		
		TopDocs docs = _is.search(bq, 100);
		assertNotNull(docs);
		ScoreDoc[] sdocs = docs.scoreDocs;;
		assertNotNull(sdocs);
		assertTrue(sdocs.length > 0);
	}
	
	public void testMultiFieldSearch() throws IOException {

		Analyzer a = SearchUtils.getAnaylyzer();
		assertNotNull(a);
		TokenStream ts = a.tokenStream("body", new StringReader("framework exception"));
		assertNotNull(ts);

		// Build the query
		BooleanQuery bq = new BooleanQuery();
		Token t = ts.next(new Token());
		while (t != null) {
			TermQuery tq = new TermQuery(new Term("body", t.term()));
			bq.add(tq, BooleanClause.Occur.MUST);
			t = ts.next(t);
		}

		// Add another term
		TermQuery tq = new TermQuery(new Term("channel", "acars development"));
		bq.add(tq, BooleanClause.Occur.SHOULD);
		
		TopDocs docs = _is.search(bq, 100);
		assertNotNull(docs);
		ScoreDoc[] sdocs = docs.scoreDocs;;
		assertNotNull(sdocs);
		assertTrue(sdocs.length > 0);
	}
	
	public void testDateRangeSearch() throws IOException {

		BooleanQuery bq = new BooleanQuery();
		bq.add(new TermQuery(new Term("body", "acars")), BooleanClause.Occur.MUST);
		
		// Build the minimum date
		Date dt = StringUtils.parseDate("20090301", "yyyyMMdd");
		assertNotNull(dt);
		ConstantScoreRangeQuery mdq = new ConstantScoreRangeQuery("created",
				DateTools.dateToString(dt, DateTools.Resolution.MINUTE), 
				DateTools.dateToString(new Date(), DateTools.Resolution.SECOND), true, true);
		bq.add(mdq, BooleanClause.Occur.MUST);
		
		TopDocs docs = _is.search(bq, 100);
		assertNotNull(docs);
		ScoreDoc[] sdocs = docs.scoreDocs;;
		assertNotNull(sdocs);
		assertTrue(sdocs.length > 0);
	}
}