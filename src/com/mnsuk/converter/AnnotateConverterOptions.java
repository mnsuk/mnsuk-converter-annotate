package com.mnsuk.converter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import com.ibm.dataexplorer.converter.ConverterOptions;
import com.ibm.dataexplorer.converter.FatalConverterException;

public class AnnotateConverterOptions {
	private static final String OPTION_HOSTNAME = "wca-hostname";
	private static final String OPTION_PORT = "wca-port";
	private static final String OPTION_COLLECTION_ID = "collection-id";
	private static final String OPTION_USERNAME = "username";
	private static final String OPTION_PASSWORD = "password";
	private static final String OPTION_EXCLUDE_BY_DEFAULT = "exclude-by-default";
	private static final String OPTION_SENTIMENT_ANALYSIS = "enable-sentiment-analysis";
	private static final String OPTION_NER_ANALYSIS = "enable-ner-analysis";
	private static final String OPTION_FACETS = "field";
	private static final String OPTION_CONTENT_LIST = "content-list";

	private String wcaRequestUrl;
	private String collectionId;
	private String username;
	private String password;
	private boolean excludeByDefault;
	private boolean sentimentAnalysis;
	private boolean nerAnalysis;
	private List<String> optionFacets;
	private HashSet<String> contentList = new HashSet<String>();
	
	public AnnotateConverterOptions(ConverterOptions options) {
		this.collectionId = options.getLastOptionValue(OPTION_COLLECTION_ID);
		this.username = options.getLastOptionValue(OPTION_USERNAME);
		this.password = options.getLastOptionValue(OPTION_PASSWORD);
		this.contentList.addAll(options.getOptionValues(OPTION_CONTENT_LIST));
		this.optionFacets = options.getOptionValues(OPTION_FACETS);
		this.excludeByDefault = "exclude-by-default".equals(options.getLastOptionValue(OPTION_EXCLUDE_BY_DEFAULT));
		this.sentimentAnalysis = OPTION_SENTIMENT_ANALYSIS.equals(options.getLastOptionValue(OPTION_SENTIMENT_ANALYSIS));
		this.nerAnalysis = OPTION_NER_ANALYSIS.equals(options.getLastOptionValue(OPTION_NER_ANALYSIS));
		this.wcaRequestUrl = "http://" + options.getLastOptionValue(OPTION_HOSTNAME)
				+ ":" + options.getLastOptionValue(OPTION_PORT) + "/api/v10/analysis/text"; 
		if (sentimentAnalysis) {
			optionFacets.add("sentiment-target-positive:._sentiment.target.positive");
			optionFacets.add("sentiment-target-negative:._sentiment.target.negative");
			optionFacets.add("sentiment-phrase-positive:._sentiment.phrase.positive");
			optionFacets.add("sentiment-phrase-negative:._sentiment.phrase.negative");
			optionFacets.add("sentiment-expression-positive:._sentiment.expression.positive");
			optionFacets.add("sentiment-expression-negative:._sentiment.expression.negative");
		};
		if (nerAnalysis) {
			optionFacets.add("ner-person:._ne.person");
			optionFacets.add("ner-location:._ne.location");
			optionFacets.add("ner-organization:._ne.organization");
		}
	}
	
	public String wcaRequestUrl() {
		return this.wcaRequestUrl;
	}
	
	public String collectionId() {
		return this.collectionId;
	}
	
	public String username() {
		return this.username;
	}
	
	public String password() {
		return this.password;
	}

	public List<String> optionFacets() {
		return this.optionFacets;
	}
	
	public boolean hasAuthentication() {
		return !this.username.isEmpty() && !this.password.isEmpty();
	}
	
	public void validateOptions() {
		Boolean stub = false;
		if (stub) { //stub
			throw new FatalConverterException("Required option missing");
		}
	}

	public HashSet<String> contentList() {
		return this.contentList;
	}

	public boolean excludeByDefault() {
		return this.excludeByDefault;
	}
	
	public boolean nerAnalysis() {
		return this.nerAnalysis;
	}
	
	public boolean sentimentAnalysis() {
		return this.sentimentAnalysis;
	}
}
