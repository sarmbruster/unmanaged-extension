package org.neo4j.lazybones;

import org.neo4j.cypher.javacompat.ExecutionResult;
import org.neo4j.graphdb.*;
import org.neo4j.graphdb.traversal.Evaluators;
import org.neo4j.graphdb.traversal.Uniqueness;
import org.neo4j.helpers.collection.IteratorUtil;
import org.neo4j.server.database.CypherExecutor;

import javax.ws.rs.*;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Sample for a unmanaged extension
 * @author Stefan Armbruster
 */

@Path("/")
public class UnmanagedExtension {

    @Context
    private GraphDatabaseService graphDatabaseService;

    @Context
    private CypherExecutor cypherExecutor;

    /**
     * return first label of a node by it's internal id
     * @param id
     * @return
     */
    @GET
    public String getLabel(@QueryParam("id") long id) {

        try (Transaction tx = graphDatabaseService.beginTx()) {
            return IteratorUtil.single(graphDatabaseService.getNodeById(id).getLabels()).name();
        }
    }

    /**
     * simplistic example how to use a generic {@link javax.ws.rs.core.Response}
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/generic")
    public Response renderGenericResponse() {
        return Response.ok().entity("abc").build();
    }

    /**
     * demo usage of traversal API, see http://docs.neo4j.org/chunked/stable/tutorial-traversal.html
     */
    @GET
    @Produces("application/json")
    @Path("/traversalapi/{label}/{key}/{value}/{relType}/{depth}")
    public People findConnectedNodesUsingTraversalAPI(
            @PathParam("label") String label,
            @PathParam("key") String key,
            @PathParam("value") String value,
            @PathParam("relType") String relType,
            @PathParam("depth") int depth) {

        try (Transaction tx = graphDatabaseService.beginTx()) {
            Node startNode = IteratorUtil.single(graphDatabaseService.findNodesByLabelAndProperty(
                    DynamicLabel.label(label), key, value
            ));

            Iterable<org.neo4j.graphdb.Path> traverserResult =
                    graphDatabaseService
                            .traversalDescription()
                            .relationships(DynamicRelationshipType.withName(relType), Direction.OUTGOING)
                            .uniqueness(Uniqueness.NODE_LEVEL)
                            .evaluator(Evaluators.atDepth(depth))
//                            .evaluator(new TraversalPrinter(Evaluators.atDepth(depth)))
                            .traverse(startNode);
            return new People(new LastNodePropertyIterator(traverserResult, key));
        }
    }

    /**
     * demo usage of traversal API, see http://docs.neo4j.org/chunked/stable/tutorial-traversal.html
     */
    @GET
    @Produces("application/json")
    @Path("/cypher/{label}/{key}/{value}/{relType}/{depth}")
    public People findConnectedNodesUsingCypher(
            @PathParam("label") String label,
            @PathParam("key") String key,
            @PathParam("value") String value,
            @PathParam("relType") String relType,
            @PathParam("depth") int depth) {

        StringBuilder cypher = new StringBuilder();
        cypher.append("MATCH (:").append(label).append("{").append(key).append(":{value}})-[:").append(relType).append("*").append(depth).append("..").append(depth).append("]->(f) RETURN f.name as name");

        // System.out.println(cypher);

        // NB: executionEngine opens a transaction on its own unless it's managed explicitly
        ExecutionResult result = cypherExecutor.getExecutionEngine().execute(cypher.toString(), Collections.<String, Object>singletonMap("value", value));

        Iterator<String> names = result.columnAs("name");

        return new People(names);
    }


    /**
     * converts a iterator over a path into an iterator over the nodeId of the endNode of that path
     */
    public class LastNodePropertyIterator implements Iterator<String> {

        private Iterator<org.neo4j.graphdb.Path> iterator;
        private String propertyName;

        public LastNodePropertyIterator(Iterable<org.neo4j.graphdb.Path> iterable, String propertyName) {
            this.iterator = iterable.iterator();
            this.propertyName = propertyName;
        }

        @Override
        public boolean hasNext() {
            return iterator.hasNext();
        }

        @Override
        public String next() {
            org.neo4j.graphdb.Path path = iterator.next();
            Node node = path.endNode();
            return node.getProperty(propertyName, "<n/a>").toString();
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }



}