package foo;

import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import com.google.api.server.spi.auth.common.User;
import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiMethod.HttpMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.api.server.spi.config.Named;
import com.google.api.server.spi.config.Nullable;
import com.google.api.server.spi.response.CollectionResponse;
import com.google.api.server.spi.response.UnauthorizedException;
import com.google.api.server.spi.auth.EspAuthenticator;

import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.PropertyProjection;
import com.google.appengine.api.datastore.PreparedQuery.TooManyResultsException;
import com.google.appengine.api.datastore.Query.CompositeFilter;
import com.google.appengine.api.datastore.Query.CompositeFilterOperator;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.appengine.api.datastore.QueryResultList;
import com.google.appengine.api.datastore.Transaction;

@Api(name = "myApi",
		version = "v1",
		audiences = "836560140856-j0llbtlr5h6b1mubnm9jc3j2s9rfqan4.apps.googleusercontent.com",
		clientIds = {"836560140856-j0llbtlr5h6b1mubnm9jc3j2s9rfqan4.apps.googleusercontent.com",
				"836560140856-j0llbtlr5h6b1mubnm9jc3j2s9rfqan4.apps.googleusercontent.com" },
		namespace =
		@ApiNamespace(
				ownerDomain = "helloworld.example.com",
				ownerName = "helloworld.example.com",
				packagePath = "")
)

public class ScoreEndpoint {


	Random r = new Random();

	// remember: return Primitives and enums are not allowed.
	@ApiMethod(name = "getRandom", httpMethod = HttpMethod.GET)
	public RandomResult random() {
		return new RandomResult(r.nextInt(6) + 1);
	}

	@ApiMethod(name = "hello", httpMethod = HttpMethod.GET)
	public User Hello(User user) throws UnauthorizedException {
		if (user == null) {
			throw new UnauthorizedException("Invalid credentials");
		}
		System.out.println("Yeah:"+user.toString());
		return user;
	}

	@ApiMethod(name = "scores", httpMethod = HttpMethod.GET)
	public List<Entity> scores() {
		Query q = new Query("Score").addSort("score", SortDirection.DESCENDING);

		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		PreparedQuery pq = datastore.prepare(q);
		List<Entity> result = pq.asList(FetchOptions.Builder.withLimit(100));
		return result;
	}

	@ApiMethod(name = "topscores", httpMethod = HttpMethod.GET)
	public List<Entity> topscores() {
		Query q = new Query("Score").addSort("score", SortDirection.DESCENDING);

		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		PreparedQuery pq = datastore.prepare(q);
		List<Entity> result = pq.asList(FetchOptions.Builder.withLimit(10));
		return result;
	}

	@ApiMethod(name = "myscores", httpMethod = HttpMethod.GET)
	public List<Entity> myscores(@Named("name") String name) {
		Query q = new Query("Score").setFilter(new FilterPredicate("name", FilterOperator.EQUAL, name)).addSort("score",
				SortDirection.DESCENDING);
		//Query q = new Query("Score").setFilter(new FilterPredicate("name", FilterOperator.EQUAL, name));

		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		PreparedQuery pq = datastore.prepare(q);
		List<Entity> result = pq.asList(FetchOptions.Builder.withLimit(10));
		return result;
	}

	@ApiMethod(name = "addScore", httpMethod = HttpMethod.GET)
	public Entity addScore(@Named("score") int score, @Named("name") String name) {

		Entity e = new Entity("Score", "" + name + score);
		e.setProperty("name", name);
		e.setProperty("score", score);

		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		datastore.put(e);

		return e;
	}


	@ApiMethod(name = "mypost", httpMethod = HttpMethod.GET)
	public CollectionResponse<Entity> mypost(@Named("name") String name, @Nullable @Named("next") String cursorString) {

		Query q = new Query("Post").setFilter(new FilterPredicate("owner", FilterOperator.EQUAL, name));



		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		PreparedQuery pq = datastore.prepare(q);

		FetchOptions fetchOptions = FetchOptions.Builder.withLimit(2);

		if (cursorString != null) {
			fetchOptions.startCursor(Cursor.fromWebSafeString(cursorString));
		}

		QueryResultList<Entity> results = pq.asQueryResultList(fetchOptions);
		cursorString = results.getCursor().toWebSafeString();

		return CollectionResponse.<Entity>builder().setItems(results).setNextPageToken(cursorString).build();

	}

	@ApiMethod(name = "getPost",
			httpMethod = ApiMethod.HttpMethod.GET)
	public CollectionResponse<Entity> getPost(User user, @Nullable @Named("next") String cursorString)
			throws UnauthorizedException {

		if (user == null) {
			throw new UnauthorizedException("Invalid credentials");
		}

		Query q = new Query("Post").
				setFilter(new FilterPredicate("owner", FilterOperator.EQUAL, user.getEmail()));



		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		PreparedQuery pq = datastore.prepare(q);

		FetchOptions fetchOptions = FetchOptions.Builder.withLimit(2);

		if (cursorString != null) {
			fetchOptions.startCursor(Cursor.fromWebSafeString(cursorString));
		}

		QueryResultList<Entity> results = pq.asQueryResultList(fetchOptions);
		cursorString = results.getCursor().toWebSafeString();

		return CollectionResponse.<Entity>builder().setItems(results).setNextPageToken(cursorString).build();
	}



	@ApiMethod(name = "ajoutPetition", httpMethod = HttpMethod.POST)
	public Entity ajoutPetitionPetition(fPetition petition) throws UnauthorizedException {


		Entity e = new Entity("Petition");
		e.setProperty("createur", petition.createur);
		e.setProperty("creatorId", petition.creatorId);
		e.setProperty("titre", petition.titre);
		e.setProperty("description", petition.description);
		e.setProperty("tags", petition.tags);
		e.setProperty("creationDate", new Date());

		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		datastore.put(e);

		return e;
	}
	@ApiMethod(name = "gettopTenPetitions", httpMethod = HttpMethod.GET)
	public List<Entity> topTenPetitions() {
		Query query = new Query("Petition").addSort("creationDate", SortDirection.DESCENDING);

		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		PreparedQuery q = datastore.prepare(query);
		List<Entity> topPetitions = q.asList(FetchOptions.Builder.withLimit(10));
		return topPetitions;
	}

}
