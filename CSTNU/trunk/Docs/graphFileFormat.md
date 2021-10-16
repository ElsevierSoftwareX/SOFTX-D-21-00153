# Representation of temporal constraint network in GraphML format

GraphML is an XML application for representing graphs of different types in a very flexible way.
In consists of two parts: a language core to describe the structural properties of a graph, and a flexible extension mechanism to add application-specific data.

In the language core, there are the elements `graph, node`, and `edge` by which it is possible to describe the topology of a graph.

The `GraphML-Attributes` extension allows the definition of node/edge attributes in the same XML document where the graph is defined.

In the CSTNU Tool library, it is assumed that a GraphML document describing a temporal constraint network uses the following attributes (we report here the most significant):
```xml
<key id="Obs" for="node">
	<desc>Proposition Observed. Value specification: [a-zA-F]</desc>
</key>
<key id="Label" for="node">
	<desc>Label. Format: [§¬§[a-zA-F]|[a-zA-F]]+|§$\emptylabel$§</desc>
	<default>§$\emptylabel$§</default>
</key>
<key id="Potential" for="node">
	<desc>Labeled Potential Values. Format: {[('node name', 'integer', 'label') ]+}|{}</desc>
</key>
<key id="Type" for="edge">
	<desc>Type: Possible values: contingent|requirement|derived|internal.</desc>
	<default>normal</default>
</key>
<key id="LowerCaseLabeledValues" for="edge">
	<desc>Labeled Lower-Case Values. Format: {[('node name', 'integer', 'label') ]+}|{}</desc>
</key>
<key id="UpperCaseLabeledValues" for="edge">
	<desc>Labeled Upper-Case Values. Format: {[('node name', 'integer', 'label') ]+}|{}</desc>
</key>
<key id="Value" for="edge">
	<desc>Value for STN edge. Format: 'integer'</desc>
</key>
<key id="LabeledValues" for="edge">
	<desc>Labeled Values. Format: {[('integer', 'label') ]+}|{}</desc>
</key>
```

Therefore, for example, to assign the labeled values $\langle 8, p\rangle$ and $\langle 6, q\rangle$ to the edge from node $C0$ to node $X$, it is sufficient to add the attribute "LabeledValues" as shown in the following listing:

```xml
<edge id="c0x" source="C0" target="X">
	<data key="LabeledValues">{(8, p), (6, q) }</data>
</edge>
```

