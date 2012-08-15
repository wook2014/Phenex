package org.phenoscape.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.DOMBuilder;
import org.obo.datamodel.Namespace;
import org.obo.datamodel.OBOClass;
import org.obo.datamodel.OBOProperty;
import org.obo.datamodel.OBOSession;
import org.obo.datamodel.impl.OBOClassImpl;
import org.obo.datamodel.impl.OBORestrictionImpl;
import org.xml.sax.SAXException;

public class ProvisionalTermUtil {

	private static final String SERVICE = "http://rest.bioontology.org/bioportal/provisional";
	private static final String APIKEY = "37697970-f916-40fe-bfeb-46aadbd07dba";

	public static List<OBOClass> getProvisionalTerms(OBOSession session) throws IllegalStateException, SAXException, IOException, ParserConfigurationException {
		final List<NameValuePair> values = new ArrayList<NameValuePair>();
		values.add(new BasicNameValuePair("apikey", APIKEY));
		values.add(new BasicNameValuePair("submittedby", "39814"));
		final String paramString = URLEncodedUtils.format(values, "utf-8");
		final HttpGet get = new HttpGet(SERVICE + "?" + paramString);
		final DefaultHttpClient client = new DefaultHttpClient();
		final HttpResponse response = new DefaultHttpClient().execute(get);
		client.getConnectionManager().shutdown();
		final DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
		final DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
		final Document xmlDoc = new DOMBuilder().build(docBuilder.parse(response.getEntity().getContent()));
		final List<Element> termElements = xmlDoc.getRootElement().getChild("data").getChild("page").getChild("contents").getChild("classBeanResultList").getChildren("classBean");
		final List<OBOClass> terms = new ArrayList<OBOClass>();
		for (Element element : termElements) {
			terms.add(createClassForProvisionalTerm(element, session));
		}
		return terms;
	}

	public static OBOClass createClassForProvisionalTerm(Element element, OBOSession session) {
		final String termID = element.getChild("id").getValue();
		final String label = element.getChild("label").getValue();
		final String definition = element.getChild("definitions").getValue();
		final String parentURI = findParentURI(element);
		final OBOClass newTerm = new OBOClassImpl(termID);
		newTerm.setName(label);
		newTerm.setDefinition(definition);
		if (parentURI != null) {
			newTerm.addParent(new OBORestrictionImpl(newTerm, (OBOClass)(session.getObject(toOBOID(parentURI))), (OBOProperty)(session.getObject("OBO_REL:is_a"))));
		}		
		final String permanentID = findEntry(element, "provisionalPermanentId");
		if (permanentID != null) {
			newTerm.setObsolete(true);
			//TODO add replaced_by link
		}
		newTerm.setNamespace(new Namespace("bioportal_provisional"));
		return newTerm;
	}
	
	public static String toURI(String oboID) {
		return "http://purl.obolibrary.org/obo/" + oboID.replaceAll(":", "_");
	}
	
	public static String toOBOID(String uri) {
		if (uri.contains("http://purl.obolibrary.org/obo/")) {
			final String id = uri.split("http://purl.obolibrary.org/obo/")[1];
			final int underscore = id.lastIndexOf("_");
			return id.substring(0, underscore) + ":" + id.substring(underscore + 1, id.length());
		} else {
			return uri;
		}
		
	}
	
	private static String findEntry(Element element, String key) {
		for (Object item : element.getChild("relations").getChildren("entry")) {
			final Element entry = (Element)item;
			final String entryKey = entry.getChild("string").getValue();
			if (entryKey.equals(key)) {
				if (entry.getChild("null") != null) {
					return null;
				}
			}
		}
		return "hi";
	}
	
	private static String findParentURI(Element term) {
		for (Object item : term.getChild("relations").getChildren("entry")) {
			final Element entry = (Element)item;
			final String entryKey = entry.getChild("string").getValue();
			if (entryKey.equals("provisionalSubclassOf")) {
				final Element uriElement = entry.getChild("org.openrdf.model.URI");
				if (uriElement != null) {
					return uriElement.getChild("uriString").getValue();
				} else {
					return null;
				}
			}
		}
		return null;
	}

}