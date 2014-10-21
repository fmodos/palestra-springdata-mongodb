package com.fmodos.mongodb;

import java.net.UnknownHostException;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;

/**
 * 
 * @author fmodos
 * 
 */
public class MongoAPITest {

	MongoClient mongoClient;

	@Before
	public void tearUp() throws UnknownHostException {
		mongoClient = new MongoClient("localhost");
	}

	@After
	public void tearDown() {
		DB db = mongoClient.getDB("testapi");
		db.getCollection("users").drop();
	}

	@Test
	public void testInsertQuery() {
		DB db = mongoClient.getDB("testapi");
		DBCollection collection = db.getCollection("users");

		//insere um objeto
		BasicDBObject object = new BasicDBObject("nome", "Joel").append("sobrenome", "Santana");
		collection.insert(object);

		//busca por objetos com nome Joel
		DBCursor cursor = collection.find(new BasicDBObject("nome", "Joel"));
		Assert.assertEquals(1, cursor.count());
		while (cursor.hasNext()) {
			DBObject dbobject = cursor.next();
			Assert.assertEquals("Joel", dbobject.get("nome"));
			Assert.assertEquals("Santana", dbobject.get("sobrenome"));
		}
	}
}
