package com.fmodos.dish.springdata;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoDbFactory;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import com.mongodb.MongoClient;
import com.mongodb.ServerAddress;

/**
 * 
 * @author fmodos
 * 
 */
public class CrudTest {

	MongoOperations template;

	@Before
	public void tearUp() throws UnknownHostException {
		// cria um template que se comunica com o localhost e a base com nome
		// 'hinosql'
		template = new MongoTemplate(new SimpleMongoDbFactory(new MongoClient(new ServerAddress("localhost")), "hinosql"));
	}

	@After
	public void tearDown() {
		template.dropCollection(Prato.class);
	}

	@Test
	public void testInsertSave() {

		Set<String> ings = new HashSet<>();
		ings.add("arroz");
		ings.add("carne");
		ings.add("creme de leite");
		Prato prato = new Prato("strogonoff", ings);

		// insere um prato
		template.insert(prato);
		Assert.assertNotNull(prato.getId());
		Assert.assertEquals("strogonoff", prato.getNome());
		Assert.assertEquals(3, prato.getIngredients().size());

		// busca o prato pelo id
		prato = template.findById(prato.getId(), Prato.class);
		prato.setNome("strognoff carne");
		try {
			// tenta inserir um prato com id já existente
			template.insert(prato);
			Assert.fail("Chave duplicada");
		} catch (org.springframework.dao.DuplicateKeyException e) {

		}

		prato = template.findById(prato.getId(), Prato.class);
		Assert.assertEquals("strogonoff", prato.getNome());

		prato.setNome("strognoff carne");
		prato.getIngredients().add("mostarda");
		// salva prato com propriedades alteradas
		template.save(prato);

		// carrega o prato e verifica se as alterações foram persistidas
		prato = template.findById(prato.getId(), Prato.class);
		Assert.assertEquals("strognoff carne", prato.getNome());
		Assert.assertEquals(4, prato.getIngredients().size());
	}

	@Test
	public void testInsertWithAssociation() {
		Chef chef = new Chef();
		chef.setNome("João");
		// insere um chef
		template.insert(chef);

		Prato prato = new Prato("churrasco");
		prato.setChef(chef);
		// insere um prato com chef
		template.insert(prato);

		prato = template.findById(prato.getId(), Prato.class);

		// garanto que o prato tem o chef associado
		Assert.assertEquals("João", prato.getChef().getNome());

	}

	@Test
	public void testUpdate() {
		ArrayList<Prato> pratos = new ArrayList<>(3);
		for (int i = 0; i < 3; i++) {
			Set<String> ings = new HashSet<>();
			ings.add("arroz");
			ings.add("carne");
			pratos.add(new Prato("prato " + i, ings));
		}
		// insere uma lista de pratos
		template.insertAll(pratos);

		// verifica se tem 3 pratos inseridos
		Assert.assertEquals(3, template.findAll(Prato.class).size());

		// aumenta a qtde de views para 1 do primeiro prato que encontrar que
		// tenha views 0
		template.updateFirst(new Query(Criteria.where("views").is(0)), new Update().inc("views", 1), Prato.class);

		// verifica que tem 1 prato com views 1 e 2 pratos com views 0
		Assert.assertEquals(1, template.find(new Query(Criteria.where("views").is(1)), Prato.class).size());
		Assert.assertEquals(2, template.find(new Query(Criteria.where("views").is(0)), Prato.class).size());

		// adiciona feijão para todos os pratos que tenha o ingrediente arroz
		template.updateMulti(new Query(Criteria.where("ingredients").in("arroz")), new Update().addToSet("ingredients", "feijão"), Prato.class);

		// verifica se tem 3 pratos com feijão
		Assert.assertEquals(3, template.find(new Query(Criteria.where("ingredients").is("feijão")), Prato.class).size());
	}

	@Test
	public void testUpsert() {
		// verifica que não tem nenhum prato com views 5
		Assert.assertEquals(0, template.find(new Query(Criteria.where("views").is(5)), Prato.class).size());
		// insere um prato com o nome macarrão e views 5
		template.upsert(new Query(Criteria.where("nome").is("macarrão")), Update.update("views", 5), Prato.class);
		// verifica se pra foi inserido
		Assert.assertEquals(1, template.find(new Query(Criteria.where("views").is(5).and("nome").is("macarrão")), Prato.class).size());
		// atualiza pra para 10 views (nesse caso atualiza, porque já existe um
		// prato com o nome macarrão)
		template.upsert(new Query(Criteria.where("nome").is("macarrão")), Update.update("views", 10), Prato.class);

		// verifica que só tem 1 prato com o nome macarrão
		Assert.assertEquals(1, template.find(new Query(Criteria.where("nome").is("macarrão")), Prato.class).size());
		Prato prato = template.find(new Query(Criteria.where("nome").is("macarrão")), Prato.class).get(0);
		// verifica que este prato tem 10 views
		Assert.assertEquals(10, prato.getViews());
	}

	@Test
	public void testFindAndModify() {
		// insere um prato risoto
		template.insert(new Prato("risoto"));

		// encontra o prato risoto e altera o nome para risoto 2
		Prato prato = template.findAndModify(new Query(Criteria.where("nome").is("risoto")), Update.update("nome", "risoto 2"), Prato.class);

		// verifica se o objeto retorno é o anterior ao registro alterado
		Assert.assertEquals(prato.getNome(), "risoto");

		// consulta novamente o BD e garante que só tem o regisitro com nome
		// risoto 2
		Assert.assertEquals(0, template.find(new Query(Criteria.where("nome").is("risoto")), Prato.class).size());
		Assert.assertEquals(1, template.find(new Query(Criteria.where("nome").is("risoto 2")), Prato.class).size());
	}

	@Test
	public void testRemove() {
		Prato prato = new Prato("risoto");
		// insere prato risoto
		template.insert(prato);

		Assert.assertEquals(1, template.find(new Query(Criteria.where("nome").is("risoto")), Prato.class).size());
		// remove o prato
		template.remove(prato);
		// garante que ele foi removido
		Assert.assertEquals(0, template.find(new Query(Criteria.where("nome").is("risoto")), Prato.class).size());

		// insere o prato novamente
		template.insert(prato);
		Assert.assertEquals(1, template.find(new Query(Criteria.where("nome").is("risoto")), Prato.class).size());
		// remove o prato com uma query pelo nome dele
		template.remove(new Query(Criteria.where("nome").is("risoto")), Prato.class);
		Assert.assertEquals(0, template.find(new Query(Criteria.where("nome").is("risoto")), Prato.class).size());

	}

	@Test
	public void testSearchByViewsAndNotEqualsName() {
		// insere 3 pratos
		Prato p5 = new Prato("prato 5");
		p5.setViews(5);
		template.insert(p5);
		Prato p10 = new Prato("prato 10");
		p10.setViews(10);
		template.insert(p10);
		Prato p15 = new Prato("prato 15");
		p15.setViews(15);
		template.insert(p15);

		// busca por pratos com view>=10 e <20 e que não tenha o nome 'prato 15'
		Query query = new Query(Criteria.where("views").gte(10).lt(20).not().and("nome").is("prato 15"));
		Assert.assertEquals(1, template.find(query, Prato.class).size());
	}

	@Test
	public void testSearchByMultipleIn() {
		Set<String> ings = new HashSet<>();
		ings.add("arroz");
		ings.add("frango");
		ings.add("pimenta");
		Prato pfrango = new Prato("prato frango", ings);
		template.insert(pfrango);

		ings = new HashSet<>();
		ings.add("arroz");
		ings.add("carne");
		ings.add("pimenta");
		Prato pcarne = new Prato("prato carne", ings);
		template.insert(pcarne);

		// buscar pelos pratos que tenha os ingrediente arroz e pimenta
		Set<String> findIn = new HashSet<>();
		findIn.add("arroz");
		findIn.add("pimenta");
		Query query = new Query(Criteria.where("ingredients").in(findIn));
		Assert.assertEquals(2, template.find(query, Prato.class).size());
	}

	@Test
	public void testSearchByExistsAndSize() {
		Set<String> set = new HashSet<>();
		set.add("picanha");
		Prato prato = new Prato("churrasco", set);

		template.insert(prato);

		// procura prato com o nome churrasco, que nao tenha chef e que tenha
		// 1 ingrediente
		Query query = new Query(Criteria.where("nome").is("churrasco").and("chef").exists(false).and("ingredients").size(1));
		Assert.assertEquals(1, template.find(query, Prato.class).size());
	}

}
