package com.mnsuk.converter;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

import org.apache.http.HttpResponse;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import com.ibm.dataexplorer.converter.ConversionException;
import com.vivisimo.gelato.utils.XmlUtil;

public class WCAResponse {
	private final String responseBody;
	private final Element annotationNode;
	private final int statusCode;
	private final String reasonPhrase;
	
	public WCAResponse(int statusCode, String reasonPhrase, String responseBody, Element annotationNode) {
		this.statusCode = statusCode;
		this.reasonPhrase = reasonPhrase;
		this.responseBody = responseBody;
		this.annotationNode = annotationNode;
	}
	
	public static WCAResponse fromHttpResponse(HttpResponse httpResponse) {
		String responseBody = null;
		Element annotationNode;
		int statusCode;
		String reasonPhrase;
		
		if (httpResponse != null) {
			statusCode = httpResponse.getStatusLine().getStatusCode();
			reasonPhrase = httpResponse.getStatusLine().getReasonPhrase();
			try {
				responseBody = WCAResponse.inputStreamToString(httpResponse.getEntity().getContent());
				if(!WCAResponse.statusIsError(statusCode)) {
					annotationNode = XmlUtil.createElementFromString((String) responseBody);
				} else {
					annotationNode = null;
				}
			} catch (IOException | SAXException e) {
				String err = String.format("Unable to parse response [%s] as XML", responseBody);
				throw new ConversionException(err, (Throwable)e);
			}
		} else {
			statusCode = 500;
			reasonPhrase = "Annotation response was null";
			responseBody = "";
			annotationNode = null;
		}
		return new WCAResponse(statusCode, reasonPhrase, responseBody, annotationNode);
	}
	
	private static boolean statusIsError(int statusCode) {
		return statusCode != 200;
	}
	
	public Node getAnnotationNode() {
		return this.annotationNode;
	}
	
	public boolean hasError() {
		return WCAResponse.statusIsError(this.statusCode);
	}
	
	public String getErrorMessage() {
		return String.format("%d %s: %s %s", this.statusCode, this.reasonPhrase, System.getProperty("line.separator"), this.responseBody);
	}
	
	public String toString() {
		if (this.hasError()) {
			return this.getErrorMessage();
		}
		return this.responseBody;
	}
	
	private static String inputStreamToString(InputStream inputStream) {
		Scanner scanner = new Scanner(inputStream, StandardCharsets.UTF_8.toString());
		String beginningOfInputBoundary = "\\A";
		scanner.useDelimiter(beginningOfInputBoundary);
		String inputString =scanner.hasNext() ? scanner.next() : "";
		scanner.close();
		return inputString;
	}
	

}
