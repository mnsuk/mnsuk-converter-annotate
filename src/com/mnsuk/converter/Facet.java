package com.mnsuk.converter;

import java.util.HashSet;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class Facet {
    private static final String PATH = "Path";
    private static final String KEYWORD = "Keyword";
    private static final String SENTIMENT_ATTRIBUTE_NAME = "_sentiment";
    static final String POSITIVE_SENTIMENT = "positive";
    static final String NEGATIVE_SENTIMENT = "negative";
    private static final String NAMED_ENTITY_ATTRIBUTE_NAME = "_ne";
    static final String PERSON_ENTITY = "person";
    static final String ORGANIZATION_ENTITY = "organization";
    static final String LOCATION_ENTITY = "location";
    private String category = null;
    private String value = null;

    public Facet(Element facet) {
        NodeList children = facet.getChildNodes();
        HashSet<String> pathSet = new HashSet<String>();
        for (int num = 0; num < children.getLength(); ++num) {
            Node currentChild = children.item(num);
            if (!(currentChild instanceof Element)) continue;
            if ("Path".equals(((Element)currentChild).getTagName())) {
                pathSet.add(currentChild.getTextContent());
                continue;
            }
            if (!"Keyword".equals(((Element)currentChild).getTagName())) continue;
            this.value = currentChild.getTextContent();
        }
        if (pathSet.contains("_sentiment")) {
            if (pathSet.contains("positive")) {
                this.category = "positive";
            } else if (pathSet.contains("negative")) {
                this.category = "negative";
            }
        } else if (pathSet.contains("_ne")) {
            if (pathSet.contains("person")) {
                this.category = "person";
            } else if (pathSet.contains("organization")) {
                this.category = "organization";
            } else if (pathSet.contains("location")) {
                this.category = "location";
            }
        }
    }

    public boolean hasAnnotation() {
        return this.category != null && this.value != null;
    }

    public String getCategory() {
        return this.category;
    }

    public String getValue() {
        return this.value;
    }
}