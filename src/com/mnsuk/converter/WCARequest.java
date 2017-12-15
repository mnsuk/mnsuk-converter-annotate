package com.mnsuk.converter;

import com.ibm.dataexplorer.converter.ConversionException;
import com.mnsuk.converter.Annotate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.message.BasicNameValuePair;

public class WCARequest {
	private String wcaServerUrl;
	private HttpClient client;
	private String collectionId;
	private static final Logger LOGGER = LoggerFactory.getLogger(Annotate.class);

	public WCARequest(String serverUrl, HttpClient client2, String collectionId) {
		this.wcaServerUrl = serverUrl;
		this.client = client2;
		this.collectionId = collectionId;
	}
	
	public HttpResponse analyseText(String data) {
		HttpPost request = buildRequest(data);
		try {
			return client.execute((HttpUriRequest)request);
		} catch (IOException e) {
			throw new ConversionException("WCA Server Annotation API error: " + e.getMessage(), (Throwable)e);
		}
	}

	private HttpPost buildRequest(String data) {
		HttpPost request = new HttpPost(this.wcaServerUrl);
		ArrayList<BasicNameValuePair> nvPairs = new ArrayList<BasicNameValuePair>(3);
		nvPairs.add(new BasicNameValuePair("collection", this.collectionId));
		nvPairs.add(new BasicNameValuePair("text", data));
		nvPairs.add(new BasicNameValuePair("output", "integration"));
		UrlEncodedFormEntity entity = new UrlEncodedFormEntity(nvPairs, StandardCharsets.UTF_8);
		request.setEntity(entity);
		return request;
	}
	

}
