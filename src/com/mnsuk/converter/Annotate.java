package com.mnsuk.converter;

import static com.ibm.dataexplorer.converter.LoggingConstants.PUBLIC_ENTRY;
import static com.ibm.dataexplorer.converter.LoggingConstants.PUBLIC_EXIT;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ibm.dataexplorer.converter.ByteArrayConverter;
import com.ibm.dataexplorer.converter.ConversionException;
import com.ibm.dataexplorer.converter.ConverterOptions;
import com.ibm.dataexplorer.converter.FatalConverterException;
import com.vivisimo.parser.input.ConverterInput;
import com.vivisimo.parser.input.InputFilter;
import com.vivisimo.parser.input.InputFilterFactory;
import com.vivisimo.parser.input.VXMLInputBuilder;

public class Annotate implements ByteArrayConverter {
	private static final Logger LOGGER = LoggerFactory.getLogger(Annotate.class);
	private AnnotateConverterOptions opts;
	private boolean isAlive;

	public Annotate(ConverterOptions options) throws FatalConverterException {
		this.opts = new AnnotateConverterOptions(options);
		isAlive = true;
	}

	@Override
	public byte[] convert(byte[] data) throws ConversionException, FatalConverterException {
		LOGGER.trace(PUBLIC_ENTRY);
		checkIsAlive();
		opts.validateOptions();
		try {
			String stringData = convertToString(data);
			CloseableHttpClient client = setupClient();
			Throwable throwable = null;
			try {
				VXMLInputBuilder inputBuilder = new VXMLInputBuilder(stringData);
				for (ConverterInput inputDocument : inputBuilder.documents()) {
					InputFilter filter = InputFilterFactory.createInputFilter(inputDocument, opts.contentList());
					String filteredContents = filter.filterInput(opts.excludeByDefault());
					if (filteredContents == null || filteredContents.isEmpty()) {
						continue;
					}
					WCARequest request = new WCARequest(opts.wcaRequestUrl(), (HttpClient) client, opts.collectionId());
					WCAResponse response = WCAResponse.fromHttpResponse(request.analyseText(filteredContents));
					LOGGER.debug("Annotate response: " + response.toString());
					if (response.hasError()) {
						throw new ConversionException(response.getErrorMessage());
					}
					DocumentAnnotator.addFacetAnnotations(inputDocument.getDocumentElement(), response.getAnnotationNode(), opts.optionFacets());
				}
				return convertToBytes(inputBuilder.newOutputBuilder(opts.excludeByDefault()).toString());
			} catch (Throwable inputBuilder) {
				throwable = inputBuilder;
				throw inputBuilder;
			} finally {
				if (client != null) {
					if (throwable != null) {
						try {
							client.close();
						} catch (Throwable x2) {
							throwable.addSuppressed(x2);
						}
					} else {
						client.close();
					}
				}
			}

		} catch (Exception e) {
			throw new ConversionException("Error annotating content with wca server: " + e.getMessage(), (Throwable) e);
		} 
		finally {
			LOGGER.trace(PUBLIC_EXIT);
		}
	}

	private byte[] convertToBytes(String data) throws UnsupportedEncodingException {
		return data == null ? null : data.getBytes("UTF-8");
	}

	private String convertToString(byte[] data) throws UnsupportedEncodingException {
		return data == null ? "" : new String(data, "UTF-8");
	}

	@Override
	public void terminate() {
		checkIsAlive();

		LOGGER.debug("Terminating");
		isAlive = false;

		// Nothing to terminate
	}

	private void checkIsAlive() {
		if (!isAlive()) {
			LOGGER.error("I've already been terminated");
			throw new IllegalStateException("The object has already been terminated");
		}
	}

	boolean isAlive() {
		return isAlive;
	}

	private CloseableHttpClient setupClient() throws MalformedURLException {
		HttpClientBuilder builder = HttpClientBuilder.create();
		if (this.opts.hasAuthentication()) {
			BasicCredentialsProvider credsProvider = new BasicCredentialsProvider();
			credsProvider.setCredentials(new AuthScope(new URL(this.opts.wcaRequestUrl()).getHost(), -1), (Credentials) new UsernamePasswordCredentials(this.opts.username(), this.opts.password()));
			builder.setDefaultCredentialsProvider((CredentialsProvider)credsProvider);
		}
		return builder.build();
	}

}

