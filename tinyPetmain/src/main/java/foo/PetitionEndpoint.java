package foo;

import com.google.api.server.spi.auth.common.User;
import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiMethod.HttpMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.api.server.spi.config.Named;
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

import java.util.*;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import java.security.KeyFactory;
import java.util.*;

@Api(name = "myApi", version = "v1", namespace = @ApiNamespace(ownerDomain = "helloworld.example.com", ownerName = "helloworld.example.com", packagePath = ""))
public class PetitionEndpoint {
	//Create a petition
    @ApiMethod(name = "addUser")	
    public Entity addUser(@Named("userId") String userId, @Named("name") String name) {
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        Transaction transac = datastore.beginTransaction();
        try {
            Query q = new Query("User").setFilter(new FilterPredicate("userId", FilterOperator.EQUAL, userId));
            PreparedQuery pq = datastore.prepare(q);
            List<Entity> result = pq.asList(FetchOptions.Builder.withDefaults());
            if(result != null && result.size() > 0){
                return null;
            }

            Entity e = new Entity("User");
            e.setProperty("userId", userId);
            e.setProperty("name", name);
            e.setProperty("createdPetitions", new ArrayList<String>());
            e.setProperty("signedPetitions", new ArrayList<String>());
            e.setProperty("date", new Date());
            datastore.put(e);
            transac.commit();
            return e;

        } finally {
            if (transac.isActive()) {
                transac.rollback();
            }
        }
    }

    @ApiMethod(name = "addPetition")
    public String addPetition(@Named("userId") String userId, @Named("url") String url, @Named("description") String description, @Named("title") String title, @Named("tags") List<String> tags) {
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        Transaction transac = datastore.beginTransaction();
        try {
            Query q = new Query("Petition").setFilter(new FilterPredicate("title", FilterOperator.EQUAL, title));
            PreparedQuery pq = datastore.prepare(q);
            List<Entity> result = pq.asList(FetchOptions.Builder.withDefaults());
            if (result != null && result.size() > 0) {
                return null;
            }

            Entity e = new Entity("Petition");
            String uniqueID = UUID.randomUUID().toString();
            e.setProperty("owner", userId);
            e.setProperty("uniqueID", uniqueID);
            e.setProperty("url", url);
            e.setProperty("title", title);
            e.setProperty("description", description);
            e.setProperty("tags", tags);
            e.setProperty("signatureCount", 0);
            e.setProperty("creationDate", new Date()); // Ajout de la propriété creationDate
            datastore.put(e);

            transac.commit();
            return uniqueID;

        } finally {
            if (transac.isActive()) {
                transac.rollback();
            }
        }
    }
	//Sign a petition (cannot sign two times, users are authenticated)
    @ApiMethod(name = "signPetition", httpMethod = ApiMethod.HttpMethod.POST)
    public Entity signPetition(@Named("uniqueID") String uniqueID, @Named("userId") String userId) {
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

        Query.Filter userPetitionFilter = new Query.FilterPredicate("userId", FilterOperator.EQUAL, userId);
        Query.Filter petitionFilter = new Query.FilterPredicate("uniqueID", FilterOperator.EQUAL, uniqueID);
        Query.CompositeFilter compositeFilter = CompositeFilterOperator.and(userPetitionFilter, petitionFilter);
        Query query = new Query("SignedPetitions").setFilter(compositeFilter);

        PreparedQuery preparedQuery = datastore.prepare(query);
        Entity existingSign = preparedQuery.asSingleEntity();

        if (existingSign != null) {
            return null;
        }

        Entity signedPetition = new Entity("SignedPetitions");
        signedPetition.setProperty("uniqueID", uniqueID);
        signedPetition.setProperty("userId", userId);
        signedPetition.setProperty("date", new Date());
        datastore.put(signedPetition);

        return signedPetition;
    }


	//List petitions  signed by a user sorted by date
    @ApiMethod(name = "getSignedPetitionsByUser", httpMethod = ApiMethod.HttpMethod.GET)
    public List<Entity> getSignedPetitionsByUser(@Named("userId") String userId) {
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

        Query.Filter userFilter = new Query.FilterPredicate("userId", FilterOperator.EQUAL, userId);
        Query query = new Query("SignedPetitions").setFilter(userFilter).addSort("date", SortDirection.DESCENDING);

        PreparedQuery preparedQuery = datastore.prepare(query);
        List<Entity> signedPetitions = preparedQuery.asList(FetchOptions.Builder.withDefaults());

        return signedPetitions;
    }

    @ApiMethod(name = "signPetition", httpMethod = ApiMethod.HttpMethod.POST)
    public Entity signPetition(@Named("uniqueID") String uniqueID, HttpServletRequest req) {
        String userId = req.getSession().getAttribute("userId").toString();
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

        Key petitionKey = KeyFactory.createKey("Petition", uniqueID);
        try {
            Entity petition = datastore.get(petitionKey);

            List<String> signedUsers = (List<String>) petition.getProperty("signedUsers");
            if (signedUsers.contains(userId)) {
                return null; // L'utilisateur a déjà signé cette pétition
            } else {
                signedUsers.add(userId);
                petition.setProperty("signedUsers", signedUsers);

                Long signatureCount = (Long) petition.getProperty("signatureCount");
                if (signatureCount == null) {
                    signatureCount = 0L;
                }
                petition.setProperty("signatureCount", signatureCount + 1);

                datastore.put(petition);

                // Ne pas ajouter la pétition à la liste des pétitions signées ici, mais simplement l'incrémenter

                return petition;
            }
        } catch (EntityNotFoundException e) {
            return null; // Gérer l'erreur si la pétition n'est pas trouvée
        }
    }
		//List top100 petitions triées par date.
    	@ApiMethod(name = "getTopSignedPetitions", httpMethod = ApiMethod.HttpMethod.GET)
    	public List<Entity> getTopSignedPetitions() {
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

        Query query = new Query("Petition").addSort("signatureCount", SortDirection.DESCENDING);
        PreparedQuery preparedQuery = datastore.prepare(query);
        List<Entity> topSignedPetitions = preparedQuery.asList(FetchOptions.Builder.withLimit(100));

        return topSignedPetitions;
    }

		//tag petition/find petition by tag sorted by creation date.
    	@ApiMethod(name = "findPetitionsByTag", httpMethod = ApiMethod.HttpMethod.GET)
    	public List<Entity> findPetitionsByTag(@Named("tag") String tag) {
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

        Query.Filter tagFilter = new Query.FilterPredicate("tags", FilterOperator.EQUAL, tag);
        Query query = new Query("Petition").setFilter(tagFilter).addSort("creationDate", SortDirection.DESCENDING);

        PreparedQuery preparedQuery = datastore.prepare(query);
        List<Entity> petitionsByTag = preparedQuery.asList(FetchOptions.Builder.withDefaults());

        return petitionsByTag;
    }
}


