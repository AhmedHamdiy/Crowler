package com.mycompany.app;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;

import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Projections;
import com.mongodb.client.model.Sorts;
import com.mongodb.client.model.Updates;

import org.bson.BsonValue;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import com.mongodb.client.result.InsertManyResult;
import com.mongodb.client.result.InsertOneResult;

import static com.mongodb.client.model.Filters.eq;

import java.io.IOException;
//
import com.mongodb.client.*;
import com.mongodb.client.model.Field;

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Projections.*;

import java.util.*;
import java.lang.Object;

import org.bson.BsonObjectId;
import org.bson.conversions.Bson;

import java.io.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import static com.mongodb.client.model.Updates.set;
import static java.lang.Math.log;
import static java.lang.System.*;
import static com.mongodb.MongoClientSettings.getDefaultCodecRegistry;
import static com.mongodb.client.model.Filters.eq;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

import org.bson.codecs.configuration.CodecProvider;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import com.mongodb.MongoException;
import org.springframework.stereotype.Component;

@Component
public class MongoDB {
    public MongoClient mongoClient;
    public MongoDatabase database;
    public MongoCollection<Document> pageCollection;
    public MongoCollection<Document> wordCollection;
    public MongoCollection<Document> queryHistoryCollection;
    public MongoCollection<Document> visitedCollection;
    public MongoCollection<Document> toVisitCollection;

    public void initializeDatabaseConnection() {
        mongoClient = MongoClients.create();
        database = mongoClient.getDatabase("Crowler");

        pageCollection = database.getCollection("Page");
        wordCollection = database.getCollection("Word");
        queryHistoryCollection = database.getCollection("QueryHistory");
        toVisitCollection = database.getCollection("ToVisit");
        visitedCollection = database.getCollection("Visited");

        System.out.println("Connected to Database successfully");
    }

    public void PrintCollectionData(String colName) {
        MongoCollection<Document> collections = database.getCollection(colName);
        try (MongoCursor<Document> cursor = collections.find()
                .iterator()) {
            while (cursor.hasNext()) {
                System.out.println(cursor.next().toJson());
            }
        }
    }

    public void insertOne(Document doc, String collectionName) {
        InsertOneResult result;
        switch (collectionName) {
            case "Page":
                result = pageCollection.insertOne(doc);
                System.out.println("Inserted a document with the following id: "
                        + Objects.requireNonNull(result.getInsertedId()).asObjectId().getValue());
                break;
            case "Word":
                result = wordCollection.insertOne(doc);
                System.out.println("Inserted a document with the following id: "
                        + Objects.requireNonNull(result.getInsertedId()).asObjectId().getValue());
                break;
            case "QueryHistory":
                result = queryHistoryCollection.insertOne(doc);
                System.out.println("Inserted a document with the following id: "
                        + Objects.requireNonNull(result.getInsertedId()).asObjectId().getValue());
                break;
            case "Visited":
                result = visitedCollection.insertOne(doc);
                System.out.println("Inserted a document with the following id: "
                        + Objects.requireNonNull(result.getInsertedId()).asObjectId().getValue());
                break;
            case "ToVisit":
                result = toVisitCollection.insertOne(doc);
                System.out.println("Inserted a document with the following id: "
                        + Objects.requireNonNull(result.getInsertedId()).asObjectId().getValue());
                break;
        }
    }

    public void dropCollection(String collectionName) {
        switch (collectionName) {
            case "Page":
                pageCollection.drop();
                break;
            case "Word":
                wordCollection.drop();
                break;
            case "History":
                queryHistoryCollection.drop();
                break;
            case "Visited":
                visitedCollection.drop();
                break;
            case "ToVisit":
                toVisitCollection.drop();
                break;
        }
    }

    public String getFirstToVisit() throws IOException {
        Document firstToVisit = toVisitCollection.find().limit(1).first();
        if (firstToVisit != null) {
            toVisitCollection.deleteOne(firstToVisit);
            return firstToVisit.getString("URL");
        } else {
            return null;
        }
    }

    public Set<String> getVisitedPages() {
        Set<String> visited = new HashSet<String>();
        visitedCollection.find().projection(Projections.include("URL")).map(document -> document.getString("URL"))
                .into(visited);
        return visited.isEmpty() ? null : visited;
    }

    public Set<String> getCompactStrings() {
        Set<String> compactStrings = new HashSet<String>();
        pageCollection.find().projection(Projections.include("compactString")).map(document -> document.getString("compactString"))
                .into(compactStrings);
        return compactStrings;
    }

    public BlockingQueue<String> getPendingPages() {
        BlockingQueue<String> pendingPages = new LinkedBlockingQueue<>();
        toVisitCollection.find().projection(Projections.include("URL")).map(document -> document.getString("URL"))
                .into(pendingPages);
        return pendingPages;
    }

    public int checkVisitedThreshold() {
        return (int) visitedCollection.countDocuments();
    }

    public int checkTotalThreshold() {
        return (int) toVisitCollection.countDocuments() + (int) visitedCollection.countDocuments();
    }

    public void insertMany(List<Document> ls, String collectionName) {

        InsertManyResult resultmany;

        switch (collectionName) {
            case "Page":
                resultmany = pageCollection.insertMany(ls);
                for (Map.Entry<Integer, BsonValue> entry : resultmany.getInsertedIds().entrySet()) {

                    System.out.println(entry.getValue().asObjectId());
                }
                break;
            case "Word":
                resultmany = wordCollection.insertMany(ls);
                for (Map.Entry<Integer, BsonValue> entry : resultmany.getInsertedIds().entrySet()) {
                    System.out.println(entry.getValue().asObjectId());
                }
                break;
            case "History":
                resultmany = queryHistoryCollection.insertMany(ls);
                for (Map.Entry<Integer, BsonValue> entry : resultmany.getInsertedIds().entrySet()) {
                    System.out.println(entry.getValue().asObjectId());
                }
                break;
        }
    }

    public List<Document> FindWordPages(String word) {
        Document projection = new Document("Pages", 1).append("_id", 0);

        FindIterable<Document> iterable = wordCollection.find(eq("Word", word)).projection(projection);

        List<Document> result = new ArrayList<>();
        iterable.into(result);
        if (!result.isEmpty()) {
            Document firstDocument = result.get(0);
            List<Document> pages = firstDocument.getList("Pages", Document.class);
            return pages;
        }
        return null;
    }

    public void closeConnection() {
        mongoClient.close();
        System.out.println("Connection with mongoDB is closed");
    }

    public boolean containsWord(String word) {
        Document doc = wordCollection.find(eq("word", word)).first();
        if (doc == null)
            return false;

        return true;
    }

    public long getNumPagesInWord(String word) { /// work correct 👌
        return FindWordPages(word).size();
    }

    public List<Document> getWords() { /// work correct 👌
        // get all the document of words in database
        FindIterable<Document> iterable = wordCollection.find();
        List<Document> result = new ArrayList<>();
        iterable.into(result);
        return result;
    }

    public void updateIDF(double IDF, List<Document> pagesList, String word) {
        Bson updates = Updates.combine(Updates.set("IDF", IDF),
                Updates.set("Pages", pagesList));
        wordCollection.updateOne(eq("Word", word), updates);

    }

    public void updatePagesList(String word, List<Document> list) {
        Bson updates = Updates.combine(Updates.set("Pages", list));
        wordCollection.updateOne(eq("Word", word), updates);
    }

    public List<Document> getCrawlerPages() {
        FindIterable<Document> iterable = pageCollection.find();
        List<Document> result = new ArrayList<>();
        iterable.into(result);
        return result;
    }

    public void setIndexedAsTrue(ObjectId id) {
        Bson updates = Updates.combine(Updates.set("isIndexed", true));
        pageCollection.updateOne(eq("_id", id), updates);
    }

    public List<Document> getnonIndexedPages() {
        FindIterable<Document> iterable = pageCollection.find(eq("isIndexed", false));
        List<Document> result = new ArrayList<>();
        iterable.into(result);
        return result;
    }

    /* public Set<String> searchPhrase(String phrase) {
        String[] words = phrase.split("\\s+");
        Set<String> commonLinks = new HashSet<>();
        List<String> returnedLinks = new ArrayList<>();
        for (String st : words) {
            returnedLinks = getPages(st);
            if (commonLinks.isEmpty()) {
                commonLinks.addAll(returnedLinks);
            } else {
                commonLinks.retainAll(new HashSet<>(returnedLinks));
            }
        }
        return commonLinks;
    } */

    /**
     * returns a list of links to pages that contain a given word
     * 
     * @param word your search term
     * @return list of strings -> the URL of each page that contains a reference to
     *         said word
     */
    // MIGHT NEED TO EDIT THIS TO COMPLY WITH CURRENT SCHEMA!
    public List<String> getPages(String word) {
        List<String> pages = new ArrayList<>();
        FindIterable<Document> pageDocs;
        Bson filter = Filters.eq("Word", word);
        Bson projection = fields(include("Pages.Link"), excludeId());
        pageDocs = wordCollection.find(filter).projection(projection);

        List<Document> linkDocs = (List<Document>) (pageDocs.first().get("Pages"));

        for (Document doc2 : linkDocs) {
            pages.add(doc2.get("Link", String.class));
        }
        return pages;
    }

    /**
     * Updates IDF for each word in word collection according to existing metrics
     * (as IDF = total n of pages / DF)
     */
    public void updateIDF() {
        Bson projection = fields(include("Word", "No_pages"), excludeId());
        MongoCursor<Document> cursor = wordCollection.find().projection(projection).iterator();
        int totalPages = 6000;
        while (cursor.hasNext()) {
            Document doc = cursor.next();
            int docFrequency = doc.getInteger("No_pages");
            double idf = log((double) pageCollection.countDocuments() / docFrequency);
            Bson filter = Filters.eq("Word", doc.getString("Word"));
            wordCollection.updateOne(filter, set("IDF", idf));
        }
    }

    /**
     * Updates TF for each reference (in a particular page) for each word in the
     * word collection,
     * based on existing metrics (as TF = times mentioned in page 'frequency' /
     * total n of words in page).
     */
    public void updateTF() {
        Bson projection = fields(include("Word", "Pages"), excludeId());
        MongoCursor<Document> cursor = wordCollection.find().projection(projection).iterator();
        while (cursor.hasNext()) {
            Document doc = cursor.next();
            String word = doc.getString("Word");
            Object obj = doc.get("Pages");

            for (Document d : (List<Document>) obj) {
                int frequency = d.getInteger("Frequency");
                int totalWords = d.getInteger("Total_Words");
                Bson filter = Filters.and(eq("Word", word), eq("Pages.Doc_Id", d.getInteger("Doc_Id")));
                wordCollection.updateOne(filter, set("Pages.$.TF", (double) frequency / totalWords));
            }
        }
    }

    /**
     * Updates rank (TF-IDF) for each reference (in a particular page) of each word
     * based on existing TF & IDF metrics.
     */
    public void updateRank() {
        Bson projection = fields(include("Word", "IDF", "Pages.Doc_Id", "Pages.TF", "Pages.Rank"), excludeId());
        MongoCursor<Document> cursor = wordCollection.find().projection(projection).iterator();
        while (cursor.hasNext()) {
            Document doc = cursor.next();
            System.out.println(doc.toJson());
            String word = doc.getString("Word");
            double IDF = doc.getDouble("IDF");
            for (Document d1 : (List<Document>) doc.get("Pages")) {
                Bson f = Filters.and(eq("Word", word), eq("Pages.Doc_Id", d1.getInteger("Doc_Id")));
                double TF = d1.getDouble("TF");
                double rank = IDF * TF;
                wordCollection.updateOne(f, set("Pages.$.Rank", rank));
            }
        }
    }

    public MongoCursor<Document> getWordPagesCursor(String queryWord) {
        Bson filter = eq("Word", queryWord);
        Bson projection = fields(include("Pages.TF_IDF", "Pages._id", "Pages.Tag"), excludeId());
        MongoCursor<Document> cursor = wordCollection.find(filter).projection(projection).iterator();
        return cursor;
    }

    public Document findPageById(ObjectId id) {
        return pageCollection.find(eq("_id", id)).first();
    }

    public long updateQueryHistory(String query) {
        Bson filter = Filters.eq("Query", query);
        Bson update = Updates.inc("Popularity", 1); 
        return queryHistoryCollection.updateOne(filter, update).getModifiedCount();
    }

    public MongoCursor<Document> getQueryHistoryCursor() {
        return queryHistoryCollection.find().iterator();
    }
}