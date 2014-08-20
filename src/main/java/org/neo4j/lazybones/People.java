package org.neo4j.lazybones;

import org.neo4j.helpers.collection.IteratorUtil;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Iterator;
import java.util.List;

/**
 * Created by stefan on 04.08.14.
 */
@XmlRootElement
//@XmlAccessorType(XmlAccessType.PROPERTY)
public class People {

    private List<String> names;


    public People() {};

    public People(Iterator<String> names) {
        this.names = IteratorUtil.asList(names);
    }

    public void setNames(List<String> names) {
        this.names = names;
    }

    public List<String> getNames() {
        return names;
    }
}
