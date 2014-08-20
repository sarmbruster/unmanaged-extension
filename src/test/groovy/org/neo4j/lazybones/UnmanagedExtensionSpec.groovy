package org.neo4j.lazybones

import org.junit.Rule
import org.neo4j.extension.spock.Neo4jResource
import org.neo4j.graphdb.DynamicLabel
import spock.lang.Specification

import static org.neo4j.extension.spock.Neo4jUtils.*


class UnmanagedExtensionSpec extends Specification {

    @Delegate
    @Rule
    Neo4jResource neo4j = new Neo4jResource()

    def "test unmanaged extension"() {
        setup:
        def id = createNodeWithLabel("Person").id
        def cut = new UnmanagedExtension(graphDatabaseService: graphDatabaseService)

        when:
        def label = cut.getLabel(id)

        then:
        label == "Person"
    }

    private def createNodeWithLabel(myLabel) {
        withSuccessTransaction(graphDatabaseService) {
            graphDatabaseService.createNode(DynamicLabel.label(myLabel))
        }
    }

}
