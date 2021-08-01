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

/**
 * Allows to read a tNGraph from a file written in GraphML format.<br>
 * GraphML format allows the definition of different attributes for the tNGraph, vertices and edges.<br>
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
	static File fileCSTNU = new File("src/test/resources/testGraphML.cstnu");
	/**
	 * 
	 */
	static File fileSTN = new File("src/test/resources/testGraphML.stn");
	/**
	 * 
	 */
	TNGraphMLReader<CSTNUEdge> readerCSTNU;
	/**
	 * 
	 */
	TNGraphMLReader<STNEdge> readerSTN;

	/**
	 * 
	 */
	TNGraph<CSTNUEdge> cstnu;

	/**
	 * 
	 */
	TNGraph<STNEdge> stn;

	/**
	 * @throws IOException nope
	 * @throws ParserConfigurationException nope
	 * @throws SAXException nope
	 */
	@Test
	public void testCSTNU() throws IOException, ParserConfigurationException, SAXException {
		this.readerCSTNU = new TNGraphMLReader<>();
		this.cstnu = this.readerCSTNU.readGraph(fileCSTNU, CSTNUEdgePluggable.class);
		Assert.assertEquals(1, this.cstnu.getEdge("YX").getUpperCaseValueMap().size());
		Assert.assertEquals(2, this.cstnu.getEdge("XY").getLowerCaseValue().getValue());
		Assert.assertEquals(2, this.cstnu.getEdgeCount());
	}

	/**
	 * @throws IOException  nope
	 * @throws ParserConfigurationException nope
	 * @throws SAXException nope
	 */
	@Test
	public void testSTN() throws IOException, ParserConfigurationException, SAXException {
		this.readerSTN = new TNGraphMLReader<>();
		this.stn = this.readerSTN.readGraph(fileSTN, STNEdgeInt.class);
		Assert.assertEquals(18, this.stn.getEdgeCount());
		Assert.assertEquals(-1, this.stn.getEdge("e7").getValue());
		Assert.assertEquals(0, this.stn.findEdge("Z", "n5").getValue());
	}
}
