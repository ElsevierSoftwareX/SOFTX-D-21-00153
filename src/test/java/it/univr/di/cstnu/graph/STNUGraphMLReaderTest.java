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

import it.univr.di.labeledvalue.Constants;

/**
 * Allows to read a TNGraph from a file written in GraphML format.<br>
 * GraphML format allows the definition of different attributes for the tNGraph, vertices and edges.<br>
 * All attributes are defined in the first part of a GraphML file. Examples of GraphML file that can read by this class are given in the Instances directory
 * under CstnuTool one.
 * 
 * @author posenato
 * @version $Id: $Id
 */
public class STNUGraphMLReaderTest {

	/**
	 * 
	 */
	static File fileSTNU = new File("src/test/resources/testGraphML.stnu");
	/**
	 * 
	 */
	TNGraphMLReader<STNUEdge> readerSTNU;

	TNGraph<STNUEdge> stnu;

	@Test
	public void testSTNU() throws IOException, ParserConfigurationException, SAXException {
		this.readerSTNU = new TNGraphMLReader<>(fileSTNU, STNUEdgeInt.class);
		this.stnu = this.readerSTNU.readGraph();
		Assert.assertEquals(-5, this.stnu.getEdge("YX").getLabeledValue());
		Assert.assertEquals(2, this.stnu.getEdge("XY").getLabeledValue());
	}

	@Test
	public void testSTNU1() throws IOException, ParserConfigurationException, SAXException {
		this.readerSTNU = new TNGraphMLReader<>(fileSTNU, STNUEdgeInt.class);
		this.stnu = this.readerSTNU.readGraph();
		Assert.assertEquals(2, this.stnu.getEdgeCount());
	}

	@Test
	public void testSTNU33() throws IOException, ParserConfigurationException, SAXException {
		this.readerSTNU = new TNGraphMLReader<>(fileSTNU, STNUEdgeInt.class);
		this.stnu = this.readerSTNU.readGraph();
		Assert.assertEquals(Constants.INT_NULL, this.stnu.getEdge("YX").getValue());
		Assert.assertEquals(Constants.INT_NULL, this.stnu.getEdge("XY").getValue());
	}

}
