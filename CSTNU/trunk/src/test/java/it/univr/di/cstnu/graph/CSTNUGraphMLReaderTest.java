/**
 *
 */
package it.univr.di.cstnu.graph;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.junit.Assert;
import org.junit.Test;
import org.xml.sax.SAXException;

import it.univr.di.labeledvalue.LabeledIntTreeMap;

/**
 * Allows to read a graph from a file written in GraphML format.<br>
 * GraphML format allows the definition of different attributes for the graph, vertices and edges.<br>
 * All attributes are defined in the first part of a GraphML file. Examples of GraphML file that can read by this class are given in the Instances directory
 * under CstnuTool one.
 * 
 * @author posenato
 * @version $Id: $Id
 */
public class CSTNUGraphMLReaderTest {

	/**
	 * 
	 */
	File file = new File("src/test/resources/testGraphML.cstnu");
	/**
	 */
	@SuppressWarnings({ "javadoc" })
	@Test
	public void testCSTNU() throws IOException, ParserConfigurationException, SAXException {
		CSTNUGraphMLReader reader = new CSTNUGraphMLReader(this.file, LabeledIntTreeMap.class);
		LabeledIntGraph g = reader.readGraph();
		Assert.assertEquals(1, g.getEdge("YX").getUpperCaseValueMap().size());
		Assert.assertEquals(2, g.getEdge("XY").getLowerCaseValue().getValue());
	}

	@SuppressWarnings({ "javadoc" })
	@Test
	public void testCSTN() throws IOException, ParserConfigurationException, SAXException {
		CSTNUGraphMLReader reader = new CSTNUGraphMLReader(this.file, LabeledIntTreeMap.class);
		LabeledIntGraph g = reader.readGraph();
		Assert.assertEquals(8, g.getEdgeCount());
	}

}
