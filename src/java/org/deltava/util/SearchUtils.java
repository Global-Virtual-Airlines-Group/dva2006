// Copyright 2009 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.util;

import java.io.*;
import java.util.*;

import org.apache.lucene.analysis.*;
import org.apache.lucene.document.*;
import org.apache.lucene.index.*;
import org.apache.lucene.search.*;
import org.apache.lucene.store.*;

import org.deltava.beans.cooler.*;
import org.deltava.util.system.SystemData;

/**
 * A utility class for Water Cooler Lucene searches.
 * @author Luke
 * @version 2.5
 * @since 2.5
 */

public class SearchUtils {
	
	public static final String[] STOPWORDS = new String[] {"i", "you", "have", "my", "like", "do",
			"one", "your", "so", "up", "out", "me", "go", "i'm", "fs", "lol", "s", "t", "am", "can", "get"};
	
	// private
	private SearchUtils() {
		super();
	}
	
	/**
	 * Returns the default index Analyzer.
	 * @return a Lucene Analyzer
	 */
	public static final Analyzer getAnaylyzer() {
		Set<String> stopWords = new HashSet<String>(Arrays.asList(StopAnalyzer.ENGLISH_STOP_WORDS));
		stopWords.addAll(Arrays.asList(SearchUtils.STOPWORDS));
		return new StopAnalyzer(stopWords);
	}

	/**
	 * Searches for a particular search term.
	 * @param sc the SearchCriteria to use
	 * @param maxResults the maximum number of results
	 * @return a Collection of SearchResult beans
	 * @throws IOException if an I/O error occurs
	 */
	public static Collection<SearchResult> search(SearchCriteria sc, int maxResults) throws IOException {

		// Build the Query
		BooleanQuery q = new BooleanQuery();

		// Add the search term
		PhraseQuery pq = new PhraseQuery();
		pq .add(new Term("body", sc.getSearchTerm().toLowerCase()));
		q.add(pq, BooleanClause.Occur.MUST);
		if (sc.getSearchSubject())
			q.add(new TermQuery(new Term("subject", sc.getSearchTerm().toLowerCase())), BooleanClause.Occur.SHOULD);

		// Add the channel
		if (!StringUtils.isEmpty(sc.getChannel()) && !Channel.ALL.equals(sc.getChannel()))
			q.add(new TermQuery(new Term("channel", sc.getChannel().toLowerCase())), BooleanClause.Occur.MUST);
		
		// Add the pilot name
		if (!StringUtils.isEmpty(sc.getAuthorName())) {
			String name = sc.getAuthorName();
			if (sc.getSearchNameFragment())
				name += "*";
			
			q.add(new TermQuery(new Term("author", name)), BooleanClause.Occur.SHOULD);
		}

		// Add minimum date
		if (sc.getMinimumDate() != null) {
			ConstantScoreRangeQuery mdq = new ConstantScoreRangeQuery("created",
				DateTools.dateToString(sc.getMinimumDate(), DateTools.Resolution.MINUTE), DateTools.dateToString(new Date(),
				DateTools.Resolution.SECOND), true, true);
			q.add(mdq, BooleanClause.Occur.MUST);
		}
		
		// Get the index
		File idxPath = new File(SystemData.get("cooler.search.idx"));
		Directory d = FSDirectory.getDirectory(idxPath);
		IndexReader idxR = IndexReader.open(d, true);
		IndexSearcher idxS = new IndexSearcher(idxR);

		Map<Integer, SearchResult> results = new HashMap<Integer, SearchResult>();
		TopDocs docs = idxS.search(q, Math.min(100, maxResults));
		ScoreDoc[] sdocs = docs.scoreDocs;
		for (int x = 0; x < sdocs.length; x++) {
			ScoreDoc sdoc = sdocs[x];
			Document doc = idxS.doc(sdoc.doc);
			Integer id = new Integer((int) NumberTools.stringToLong(doc.getField("id").stringValue()));
			if (id.intValue() != 0) {
				SearchResult sr = results.get(id);
				if (sr == null) {
					sr = new SearchResult(id.intValue(), sdoc.score);
					results.put(id, sr);
				} else
					sr.addHit(sdoc.score);
			}
		}

		idxS.close();
		idxR.close();
		
		// Sort the results
		List<SearchResult> sortedResults = new ArrayList<SearchResult>(results.values());
		Collections.sort(sortedResults, Collections.reverseOrder());
		return sortedResults;
	}

	/**
	 * Adds or updates a message in the index.
	 * @param msg the Message
	 * @throws IOException if an I/O error occurs
	 * @see SearchUtils#update(int, Collection)
	 */
	public static synchronized void add(IndexableMessage msg) throws IOException {
		if (StringUtils.isEmpty(msg.getBody()))
			return;
		
		// Get the index
		File idxPath = new File(SystemData.get("cooler.search.idx"));
		Directory d = FSDirectory.getDirectory(idxPath);
		IndexWriter.unlock(d);
		IndexWriter idxW = new IndexWriter(d, getAnaylyzer(), false, IndexWriter.MaxFieldLength.UNLIMITED);

		Document doc = new Document();
		doc.add(new Field("id", NumberTools.longToString(msg.getThreadID()), Field.Store.YES, Field.Index.NOT_ANALYZED));
		doc.add(new Field("created", DateTools.dateToString(msg.getCreatedOn(), DateTools.Resolution.MINUTE),
				Field.Store.NO, Field.Index.NOT_ANALYZED));
		doc.add(new Field("subject", msg.getSubject(), Field.Store.NO, Field.Index.ANALYZED));
		doc.add(new Field("channel", msg.getChannel(), Field.Store.NO, Field.Index.NOT_ANALYZED));
		doc.add(new Field("body", msg.getBody(), Field.Store.NO, Field.Index.ANALYZED));
		if (msg.getAuthor() != null)
			doc.add(new Field("author", msg.getAuthor().getName(), Field.Store.NO, Field.Index.NOT_ANALYZED));

		idxW.addDocument(doc);
		idxW.commit();
		idxW.close();
	}

	/**
	 * Adds or updates a message thread in the index.
	 * @param threadID the Message Thread database ID
	 * @param msgs the Messages from the Thread to update
	 * @throws IOException if an I/O error occurs
	 */
	public static synchronized void update(int threadID, Collection<IndexableMessage> msgs) throws IOException {
		
		// Get the index
		File idxPath = new File(SystemData.get("cooler.search.idx"));
		Directory d = FSDirectory.getDirectory(idxPath);
		IndexWriter.unlock(d);
		IndexWriter idxW = new IndexWriter(d, getAnaylyzer(), false, IndexWriter.MaxFieldLength.UNLIMITED);

		// Delete the thread
		Term t = new Term("id", NumberTools.longToString(threadID));
		idxW.deleteDocuments(t);
		for (Iterator<IndexableMessage> i = msgs.iterator(); i.hasNext();) {
			IndexableMessage msg = i.next();
			if (!StringUtils.isEmpty(msg.getBody())) {
				Document doc = new Document();
				doc.add(new Field("id", NumberTools.longToString(threadID), Field.Store.YES, Field.Index.NOT_ANALYZED));
				doc.add(new Field("created", DateTools.dateToString(msg.getCreatedOn(), DateTools.Resolution.MINUTE),
						Field.Store.NO, Field.Index.NOT_ANALYZED));
				doc.add(new Field("subject", msg.getSubject(), Field.Store.NO, Field.Index.ANALYZED));
				doc.add(new Field("channel", msg.getChannel(), Field.Store.NO, Field.Index.NOT_ANALYZED));
				doc.add(new Field("body", msg.getBody(), Field.Store.NO, Field.Index.ANALYZED));
				if (msg.getAuthor() != null)
					doc.add(new Field("author", msg.getAuthor().getName(), Field.Store.NO, Field.Index.NOT_ANALYZED));

				idxW.addDocument(doc);
			}
		}

		idxW.commit();
		idxW.close();
	}

	/**
	 * Convenience method to delete a thread from the index. 
	 * @param threadID the Message Thread database ID
	 * @throws IOException if an I/O error occurs
	 * @see SearchUtils#update(int, Collection)
	 */
	public static void delete(int threadID) throws IOException {
		Collection<IndexableMessage> EMPTY = Collections.emptyList();
		update(threadID, EMPTY);
	}
}