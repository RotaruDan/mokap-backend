package es.eucm.mokap.backend.data.db;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.search.Results;
import com.google.appengine.api.search.ScoredDocument;
import es.eucm.mokap.backend.model.response.SearchResponse;

import java.io.IOException;
import java.util.Map;

/**
 * Created by mario on 05/12/2014.
 */
public interface DatabaseInterface {

    /**
     * Performs a simple search with no associated cursor. Actually, it calls searchByString(String searchString, String cursorString)
     * with null in the cursor parameter.
     * @param searchString String to search for
     * @return Results object, similar to a list: https://cloud.google.com/appengine/docs/java/search/results
     * @throws IOException
     */
    Results<ScoredDocument> searchByString(String searchString) throws IOException;

    /**
     *
     * @param searchString String to search for
     * @param cursorString Search Cursor as WebSafeString: https://cloud.google.com/appengine/docs/java/javadoc/com/google/appengine/api/datastore/Cursor
     * @return Results object, similar to a list: https://cloud.google.com/appengine/docs/java/search/results
     * @throws IOException
     */
    Results<ScoredDocument> searchByString(String searchString, String cursorString) throws IOException;

    /**
     * Stores an entity in Google Datastore.
     * See https://cloud.google.com/appengine/docs/java/javadoc/com/google/appengine/api/datastore/Entity
     * See https://cloud.google.com/appengine/docs/java/javadoc/com/google/appengine/api/datastore/Key
     * @param ent The entity to store
     * @return The Key generated by Google Datastore for the entity
     */
    Key storeEntity(Entity ent);

    /**
     * Deletes an entity from Google Datastore
     * @param keyId Key id of the entity to delete
     */
    void deleteEntity(long keyId);

    /**
     * Looks for the entity with the Key id supplied in Datastore, converts it into a HashMap<String, Object> and returns it
     * @param keyId Key id of the entity we're looking for
     * @return A HashMap with all the data in the entity
     */
    Map<String, Object> getEntityByIdAsMap(long keyId);

    /**
     * Adds an entity to The search Index.
     * @param ent The entity to add
     * @param k The key of the entity in Datastore
     */
    void addToSearchIndex(Entity ent, Key k);
}
