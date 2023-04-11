package foo;

import java.util.ArrayList;
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
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Projection;
import com.google.appengine.api.datastore.PropertyProjection;
import com.google.appengine.api.datastore.PreparedQuery.TooManyResultsException;
import com.google.appengine.api.datastore.Query.CompositeFilter;
import com.google.appengine.api.datastore.Query.CompositeFilterOperator;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.appengine.repackaged.com.google.datastore.v1.*;
import com.google.appengine.repackaged.com.google.datastore.v1.Value.Builder;
import com.google.appengine.repackaged.com.google.protobuf.StringValue;
import com.google.appengine.api.datastore.QueryResultList;
import com.google.appengine.api.datastore.Transaction;
import com.google.appengine.api.datastore.AdminDatastoreService.EntityBuilder;

@Api(name = "myApi", version = "v1", audiences = "59984564463-p06vp32rova2ubjkdnhdu0f7d4rbb7j7.apps.googleusercontent.com", clientIds = {
        "59984564463-p06vp32rova2ubjkdnhdu0f7d4rbb7j7.apps.googleusercontent.com" }, namespace = @ApiNamespace(ownerDomain = "helloworld.example.com", ownerName = "helloworld.example.com", packagePath = ""))

public class tinypetEndpoint {

    Random r = new Random();

    // remember: return Primitives and enums are not allowed.

    @ApiMethod(name = "getRandom", httpMethod = HttpMethod.GET)
    public RandomResult random(User user) throws UnauthorizedException {

        if (user == null) {
            throw new UnauthorizedException("Invalid credentials");
        }
        return new RandomResult(r.nextInt(6) + 1);

    }

    // Avoir toutes les pétitions
    @ApiMethod(name = "gotAllPetitions", httpMethod = HttpMethod.GET)
    public CollectionResponse<Entity> gotAllPetitions(@Nullable @Named("next") String cursorString) {

        Query q = new Query("PetitionTP").addSort("Date", SortDirection.DESCENDING);

        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        PreparedQuery pq = datastore.prepare(q);

        FetchOptions fetchOptions = FetchOptions.Builder.withLimit(5);

        if (cursorString != null) {
            fetchOptions.startCursor(Cursor.fromWebSafeString(cursorString));
        }

        QueryResultList<Entity> results = pq.asQueryResultList(fetchOptions);
        cursorString = results.getCursor().toWebSafeString();

        return CollectionResponse.<Entity>builder().setItems(results).setNextPageToken(cursorString).build();

    }

    // Avoir toutes mes petitions crées
    @ApiMethod(name = "gotMyPetitions", httpMethod = HttpMethod.GET)
    public CollectionResponse<Entity> gotMyPetitions(User user, @Nullable @Named("next") String cursorString)
            throws UnauthorizedException {

        if (user == null) {
            throw new UnauthorizedException("Invalid credentials");
        }

        Query q = new Query("PetitionTP")
                .setFilter(new FilterPredicate("Proprietaire", FilterOperator.EQUAL, user.getId())).addSort("Date",
                        SortDirection.DESCENDING);
        ;

        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        PreparedQuery pq = datastore.prepare(q);

        FetchOptions fetchOptions = FetchOptions.Builder.withLimit(5);

        if (cursorString != null) {
            fetchOptions.startCursor(Cursor.fromWebSafeString(cursorString));
        }

        QueryResultList<Entity> results = pq.asQueryResultList(fetchOptions);
        cursorString = results.getCursor().toWebSafeString();

        return CollectionResponse.<Entity>builder().setItems(results).setNextPageToken(cursorString).build();

    }

    // Avoir toutes mes petitions signées
    @ApiMethod(name = "gotSignedPetitions", httpMethod = HttpMethod.GET)
    public CollectionResponse<Entity> gotSignedPetitions(User user, @Nullable @Named("next") String cursorString)
            throws UnauthorizedException {

        if (user == null) {
            throw new UnauthorizedException("Invalid credentials");
        }

        Query q = new Query("PetitionTP").setFilter(new FilterPredicate("Votants", FilterOperator.EQUAL, user.getId()))
                .addSort("Date",
                        SortDirection.DESCENDING);

        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        PreparedQuery pq = datastore.prepare(q);

        FetchOptions fetchOptions = FetchOptions.Builder.withLimit(5);

        if (cursorString != null) {
            fetchOptions.startCursor(Cursor.fromWebSafeString(cursorString));
        }

        QueryResultList<Entity> results = pq.asQueryResultList(fetchOptions);
        cursorString = results.getCursor().toWebSafeString();

        return CollectionResponse.<Entity>builder().setItems(results).setNextPageToken(cursorString).build();

    }

    // Avoir une pétition
    @ApiMethod(name = "gotOnePetitionById", httpMethod = HttpMethod.GET)
    public Entity gotOnePetitionById(@Named("idpetition") String idPetition) {

        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

        Query query = new Query("PetitionTP");

        FilterPredicate filter = new FilterPredicate(Entity.KEY_RESERVED_PROPERTY, FilterOperator.EQUAL,
                KeyFactory.createKey("PetitionTP", idPetition));

        query.setFilter(filter);
        PreparedQuery pq = datastore.prepare(query);
        Entity entity = pq.asSingleEntity();

        return entity;

    }

    // Crée une pétition
    @ApiMethod(name = "addPetition", httpMethod = HttpMethod.GET)
    public Entity addPetition(User user, @Named("title") String title, @Named("description") String body)
            throws UnauthorizedException {

        if (user == null) {
            throw new UnauthorizedException("Invalid credentials");
        }

        String finalKey = "P1";
        Key lastkey = getLastEntityKey();
        
        if(lastkey != null){
            long lastkeyNum = Long.parseLong(lastkey.getName().substring(1));
            lastkeyNum++;
            String newKeyNum = Long.toString(lastkeyNum);
    
            Key newPetitionKey = KeyFactory.createKey(lastkey.getParent(), lastkey.getKind(), "P" + newKeyNum);
            
             finalKey = newPetitionKey.getName();
        }
       
        Entity e = new Entity("PetitionTP", finalKey);
        e.setProperty("Date", new Date());
        e.setProperty("Proprietaire", user.getId());
        e.setProperty("Texte", body);
        e.setProperty("Titre", title);

        HashSet<String> list = new HashSet<String>();
        list.add(user.getId());

        e.setProperty("Votants", list);
        e.setProperty("Nbvotants", list.size());

        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        datastore.put(e);

        return e;
    }

    public Key getLastEntityKey() {

        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

        // Créez une requête pour récupérer la dernière entité créée
        Query query = new Query("PetitionTP").addSort("Date", SortDirection.DESCENDING)
                .setKeysOnly();

        // Exécutez la requête et récupérez les résultats
        PreparedQuery pq = datastore.prepare(query);
        Iterator<Entity> results = pq.asIterable().iterator();

        // Récupérez la clé de la première (et unique) entité dans les résultats
        Entity entity = results.next();
        Key lastEntityKey = entity.getKey();

        return lastEntityKey;

    }

    // Signer une petition
    @ApiMethod(name = "signPetition", httpMethod = HttpMethod.GET)
    public Entity signPetition(User user, @Named("petitionId") String id) throws UnauthorizedException {

        if (user == null) {
            throw new UnauthorizedException("Invalid credentials");
        }

        Entity e = gotOnePetitionById(id);

        ArrayList<String> list = (ArrayList<String>) e.getProperty("Votants"); 


        if(!list.contains(user.getId())){
            list.add(user.getId());
            e.setProperty("Votants", list);
            e.setProperty("Nbvotants", list.size());    
        }
        
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        datastore.put(e);

        return e;

    }


}
