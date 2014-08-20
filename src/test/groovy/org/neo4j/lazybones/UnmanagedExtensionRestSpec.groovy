package org.neo4j.lazybones

import org.junit.ClassRule
import org.neo4j.extension.spock.Neo4jServerResource
import org.neo4j.graphdb.DynamicLabel
import spock.lang.Shared
import spock.lang.Specification

/**
 * Created by stefan on 27.07.14.
 */
class UnmanagedExtensionRestSpec extends Specification {

    @ClassRule
    @Shared
    Neo4jServerResource neo4j = new Neo4jServerResource(
            thirdPartyJaxRsPackages: [ "org.neo4j.lazybones":"/mountpoint" ]
    )

    def "unmanaged extension is responding"() {
        setup:
        def id = createNodeWithLabel("Person").id

        when:
        def response = neo4j.http.GET("mountpoint?id=$id")

        then:
        response.status() == 200
        response.rawContent() == "Person"
    }

    private def createNodeWithLabel(myLabel) {
        neo4j.withTransaction {
            neo4j.graphDatabaseService.createNode(DynamicLabel.label(myLabel))
        }
    }
}
