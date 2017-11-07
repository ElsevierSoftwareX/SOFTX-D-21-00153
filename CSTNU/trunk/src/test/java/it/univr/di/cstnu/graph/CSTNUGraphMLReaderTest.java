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
	 */
	@SuppressWarnings({ "javadoc", "static-method" })
	@Test
	public void testCSTNU() throws IOException, ParserConfigurationException, SAXException {
		File file = new File("src/test/resources/007r.cstnu");
		CSTNUGraphMLReader reader = new CSTNUGraphMLReader(file, LabeledIntTreeMap.class);
		LabeledIntGraph g = reader.readGraph();
		Assert.assertEquals(900, g.getEdge("1S_Z").getUpperCaseValueMap().size());
	}

	@SuppressWarnings({ "javadoc", "static-method" })
	@Test
	public void testCSTN() throws IOException, ParserConfigurationException, SAXException {
		File file = new File("src/test/resources/R1.cstn");
		CSTNUGraphMLReader reader = new CSTNUGraphMLReader(file, LabeledIntTreeMap.class);
		LabeledIntGraph g = reader.readGraph();
		Assert.assertEquals(10, g.getEdgeCount());
		Assert.assertEquals(3, g.getEdge("p?_X").getLabeledValueMap().size());
	}

}
