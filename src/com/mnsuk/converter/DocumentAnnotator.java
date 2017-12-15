package com.mnsuk.converter;

import static com.ibm.dataexplorer.converter.LoggingConstants.PUBLIC_ENTRY;

import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.vivisimo.parser.input.VXMLDocument;

public class DocumentAnnotator {
	private static final String FACETS_TAG = "Facets";
	private static final String METADATA_TAG = "Metadata";
	private static final Logger LOGGER = LoggerFactory.getLogger(Annotate.class);

	public static void addFacetAnnotations(Element inputElement, Node annotationNode, List<String> list) throws XPathExpressionException {
		XPathExpression xpathErr = null;
		XPathExpression xpath0 = null;
		XPathExpression xpath1 = null;
		XPathExpression xpathPath = null;
		XPathExpression xpathKeyword = null;
		XPathExpression xpath4 = null;
		XPathExpression xpath5 = null;

		DocumentBuilder builder = null;
		Document doc = null;

		DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
		domFactory.setNamespaceAware(true);
		try
		{
			builder = domFactory.newDocumentBuilder();
			XPathFactory factory = XPathFactory.newInstance();
			XPath xpath = factory.newXPath();

			xpathErr = xpath.compile("/error/message/text()");
			xpath0 = xpath.compile("/Document/Content/text()");
			xpath1 = xpath
					.compile("/Document/Metadata/Facets/Facet[@Begin]");
			xpathPath = xpath.compile("Path/text()");
			xpathKeyword = xpath.compile("Keyword/text()");

			xpath4 = xpath.compile("@Begin");
			xpath5 = xpath.compile("@End");
		}
		catch (ParserConfigurationException localParserConfigurationException) {}catch (XPathExpressionException localXPathExpressionException1) {}

		for (String f : list){
			LOGGER.info("Extracting facet: " + f);
			String[] t = f.split(":");
			String fcElementName = (t.length >= 1 ) ? t[0] : null; 
			String acFacetName = (t.length >= 2 ) ? t[1] : null; 
			// ica facet names should begin with a ".", add it in if the user forgot.
			if ((fcElementName != null && !fcElementName.isEmpty()) && (acFacetName != null && !acFacetName.isEmpty())) {
				if (!acFacetName.startsWith("."))
					acFacetName = "." + acFacetName;
				//NodeList facets = DocumentAnnotator.getFacetNodes(annotationNode);
				NodeList facets = (NodeList)xpath1.evaluate(annotationNode, 
				        XPathConstants.NODESET);
				int facetLength = facets.getLength();
				for (int n = 0; n < facetLength; n++) {
					NodeList paths = (NodeList)xpathPath.evaluate(facets.item(n), 
							XPathConstants.NODESET);
					StringBuffer sb = new StringBuffer();
					for (int m = 0; m < paths.getLength(); m++) {
						sb.append("." + paths.item(m).getTextContent());
					}
					String path = sb.toString();
					if (path.equals(acFacetName)) {
						Element newContent = inputElement.getOwnerDocument().createElement("content");
						newContent.setAttribute("name", fcElementName);
						String val = (String)xpathKeyword.evaluate(facets.item(n), 
								XPathConstants.STRING);
						newContent.appendChild(inputElement.getOwnerDocument().createTextNode(val));
						inputElement.appendChild(newContent);
					}
				}
			}
		}

	}

	public static void annotateDocument(Element documentElement, Node annotationNode) throws ParserConfigurationException {
		NodeList facets = DocumentAnnotator.getFacetNodes(annotationNode);
		if (facets != null) {
			DocumentAnnotator.addSentimentToDocument(new VXMLDocument(documentElement), facets);
		}
	}

	private static void addSentimentToDocument(VXMLDocument document, NodeList facets) {
		for (int fieldNum = 0; fieldNum < facets.getLength(); ++fieldNum) {
			Facet facet;
			Node currentNode = facets.item(fieldNum);
			if (!(currentNode instanceof Element) || !(facet = new Facet((Element)currentNode)).hasAnnotation()) continue;
			document.addContentNode(facet.getCategory(), facet.getValue());
		}
	}

	private static NodeList getFacetNodes(Node annotations) {
		Node metadataNode = DocumentAnnotator.getFirstElement(annotations, "Metadata");
		if (metadataNode == null) {
			return null;
		}
		Node fieldsNode = DocumentAnnotator.getFirstElement(metadataNode, "Facets");
		if (fieldsNode == null) {
			return null;
		}
		return fieldsNode.getChildNodes();
	}

	private static Node getFirstElement(Node parent, String name) {
		NodeList children;
		if (parent instanceof Element && (children = ((Element)parent).getElementsByTagName(name)).getLength() > 0) {
			return children.item(0);
		}
		return null;
	}
}