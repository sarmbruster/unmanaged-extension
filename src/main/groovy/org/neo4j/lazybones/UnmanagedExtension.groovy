package org.neo4j.lazybones

import groovy.transform.CompileStatic
import org.neo4j.graphdb.GraphDatabaseService
import org.neo4j.helpers.collection.IteratorUtil
import org.neo4j.tooling.GlobalGraphOperations

import javax.ws.rs.GET
import javax.ws.rs.Path
import javax.ws.rs.QueryParam
import javax.ws.rs.core.Context

/**
 * Sample for a unmanaged extension
 * @author Stefan Armbruster
 */

@CompileStatic
@Path("/")
class UnmanagedExtension {

    @Context
    GraphDatabaseService graphDatabaseService;

    @GET
    def getLabel(@QueryParam("id") long id) {
        withTransaction {
            IteratorUtil.single(graphDatabaseService.getNodeById(id).labels).name()
        }
    }

    private withTransaction(Closure closure) {
        def tx = graphDatabaseService.beginTx()
        try {
            def retVal = closure.call()
            tx.success()
            return retVal
        } finally {
            tx.close()
        }
    }

}