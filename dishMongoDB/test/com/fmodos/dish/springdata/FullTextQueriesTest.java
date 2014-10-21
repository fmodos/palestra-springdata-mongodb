package com.fmodos.dish.springdata;

import java.net.UnknownHostException;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoDbFactory;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.TextCriteria;
import org.springframework.data.mongodb.core.query.TextQuery;

import com.fmodos.dish.springdata.Blog.Notes;
import com.mongodb.MongoClient;
import com.mongodb.ServerAddress;

/**
 * 
 * @author fmodos
 * 
 */
public class FullTextQueriesTest {

	MongoOperations mongoOps;

	@Before
	public void tearUp() throws UnknownHostException {
		//cria um template que se comunica com o localhost e a base com nome 'hinosql'
		mongoOps = new MongoTemplate(new SimpleMongoDbFactory(new MongoClient(new ServerAddress("localhost")), "hinosql"));
	}

	@After
	public void tearDown() {
		//exclui a tabela
		mongoOps.dropCollection(Blog.class);
	}

	@Test
	public void testSearchByWeight() {
		Blog book = new Blog();
		book.setTitle("1 java");
		book.setContent("Acessando banco nosql em java");
		Notes n = new Notes();
		n.setText("Gostei do spring data");
		book.setNotes(n);

		mongoOps.save(book);

		book = new Blog();
		book.setTitle("2");
		book.setContent("java");
		n = new Notes();
		n.setText("Gostei do data");
		book.setNotes(n);

		mongoOps.save(book);

		book = new Blog();
		book.setTitle("3");
		book.setContent("asdfas");
		n = new Notes();
		n.setText("java");
		book.setNotes(n);

		mongoOps.save(book);

		// consulta retorna registros com o texto 'java' ordenados de acordo com
		// o weight da annotation TextIndented
		// para verificar o efeito dessa propriedade, basta alterar os weight na classe Blog
		Query query = TextQuery.queryText(new TextCriteria().matching("java")).sortByScore();
		List<Blog> page = mongoOps.find(query, Blog.class);
		for (Blog blog : page) {
			System.out.println(blog.getTitle());
		}

	}
}
