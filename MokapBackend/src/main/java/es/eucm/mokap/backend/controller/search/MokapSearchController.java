package es.eucm.mokap.backend.controller.search;

import com.google.appengine.api.search.Results;
import com.google.appengine.api.search.ScoredDocument;

import es.eucm.ead.schemax.repo.RepoElementFields;
import es.eucm.mokap.backend.controller.BackendController;
import es.eucm.mokap.backend.model.response.SearchResponse;

import java.io.*;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Specific controller class for the resource search service.
 */
public class MokapSearchController extends BackendController implements SearchController {

    /**
     * Performs a Datastore simple search given a string with the search terms. The search returns up to 20 results for that search.
     * If there are more results to be retrieved, it also returns a cursor string (https://cloud.google.com/appengine/docs/java/datastore/queries?hl=Java_Query_cursors#Java_Query_cursors)
     * to allow the client to continue iterating the search results. If searchCursor is empty, the method will assume it's a first query.
     * The results and cursor string are embedded into a JSON String of the type es.eucm.mokap.model.response.SearchResponse.
     * @param searchString Terms to search for
     * @param searchCursor WebSafeString for resuming a search
     * @throws java.io.IOException When the search can't access Datastore or the Search Index for some reason
     * @return JSON String of the type es.eucm.mokap.model.response.SearchResponse
     */
    @Override
    public String searchByString(String searchString, String searchCursor) throws IOException {
        SearchResponse gr = new SearchResponse();
        Results<ScoredDocument> results = db.searchByString(searchString, searchCursor);
        if(results.getCursor()!=null)
            gr.setSearchCursor(results.getCursor().toWebSafeString());
        gr.setTotal(results.getNumberFound());

        // Iterate the results and find the corresponding entities
        fillResults(gr, results);
        String str = gr.toJsonString();
        return str;
    }




    /**
     * Fills a SearchResponse with a set of search results
     * In case of error in the addition of one entity, we just skip it and substract one from the total
     * so it's coherent.
     * @param gr SearchResponse object we need to fill
     * @param results List of results we're processing
     */
    private void fillResults(SearchResponse gr, Results<ScoredDocument> results) {
        for(ScoredDocument sd : results){
            try{
                long keyId = Long.parseLong(sd.getOnlyField(RepoElementFields.ENTITYREF).getText());
                Map<String, Object> ent = db.getEntityByIdAsMap(keyId);
                prepareResponseEntity(keyId, ent);

                gr.addResult(ent);
            }catch(Exception e){
                gr.setTotal(gr.getTotal() - 1);
                e.printStackTrace();
            }
        }
    }

    /**
     * Calculates and adds the missing fields to the entity we're sending to the user
     * See https://cloud.google.com/appengine/docs/java/javadoc/com/google/appengine/api/datastore/Entity
     * See https://cloud.google.com/appengine/docs/java/javadoc/com/google/appengine/api/datastore/Key
     * @param keyId Key id of the entity we're modifying
     * @param ent Entity we're modifying already converted to hashmap
     * @throws IOException
     */
    private void prepareResponseEntity(long keyId,
                                       Map<String, Object> ent) throws IOException {
    	ent.put(RepoElementFields.ENTITYREF, keyId+"");
        ent.put(RepoElementFields.CONTENTSURL, DOWNLOAD_URL+keyId+".zip");
        List<String> tnsUrls = st.getTnsUrls(DOWNLOAD_URL, keyId);
        List<Integer> tnsWidths = new LinkedList<Integer>();
        List<Integer> tnsHeights = new LinkedList<Integer>();
        for(String tn : tnsUrls){
            int lastSeparator = tn.lastIndexOf("/")+1;
            String end = tn.substring(lastSeparator);
            String res = end.split("\\.")[0];
            String[] resolutionParams = res.split("x");
            tnsWidths.add(Integer.parseInt(resolutionParams[0]));
            tnsHeights.add(Integer.parseInt(resolutionParams[1]));
        }

        ent.put(RepoElementFields.THUMBNAILURLLIST, tnsUrls);
        ent.put(RepoElementFields.THUMBNAILWIDTHLIST, tnsWidths);
        ent.put(RepoElementFields.THUMBNAILHEIGHTLIST, tnsHeights);
    }
}
