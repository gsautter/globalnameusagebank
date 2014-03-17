/* TnuBank, the distributed platform for taxonomic name usages.
 * Copyright (C) 2014- Plazi, by G. Sautter
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package de.uka.ipd.idaho.tnuBank;

import java.io.IOException;
import java.io.StringReader;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.Properties;

import de.uka.ipd.idaho.binoBank.BinoBankRestClient;
import de.uka.ipd.idaho.gamta.util.SgmlDocumentReader;
import de.uka.ipd.idaho.onn.stringPool.StringPoolRestClient;
import de.uka.ipd.idaho.plugins.bibRefs.BibRefUtils;
import de.uka.ipd.idaho.plugins.bibRefs.BibRefUtils.RefData;
import de.uka.ipd.idaho.plugins.taxonomicNames.TaxonomicNameUtils.TaxonomicName;
import de.uka.ipd.idaho.refBank.RefBankRestClient;
import de.uka.ipd.idaho.txnBank.TxnBankRestClient;

/**
 * TnuBank specific REST client, adding detail search for taxonomic name usages.
 * 
 * @author sautter
 */
public class TnuBankRestClient extends StringPoolRestClient implements TnuBankConstants, TnuBankClient {
	private static final String primaryRankNames = (";" + KINGDOM_ATTRIBUTE + ";" + PHYLUM_ATTRIBUTE + ";" + CLASS_ATTRIBUTE + ";" + ORDER_ATTRIBUTE + ";" + FAMILY_ATTRIBUTE + ";" + GENUS_ATTRIBUTE + ";" + SPECIES_ATTRIBUTE + ";");
	
	/* (non-Javadoc)
	 * @see de.uka.ipd.idaho.onn.stringPool.StringPoolRestClient#getNamespaceAttribute()
	 */
	public String getNamespaceAttribute() {
		return TNU_XML_NAMESPACE_ATTRIBUTE;
	}
	
	/* (non-Javadoc)
	 * @see de.uka.ipd.idaho.onn.stringPool.StringPoolRestClient#getStringNodeType()
	 */
	public String getStringNodeType() {
		return NAME_USAGE_NODE_TYPE;
	}
	
	/* (non-Javadoc)
	 * @see de.uka.ipd.idaho.onn.stringPool.StringPoolRestClient#getStringParsedNodeType()
	 */
	public String getStringParsedNodeType() {
		return NAME_USAGE_PARSED_NODE_TYPE;
	}
	
	/* (non-Javadoc)
	 * @see de.uka.ipd.idaho.onn.stringPool.StringPoolRestClient#getStringPlainNodeType()
	 */
	public String getStringPlainNodeType() {
		return NAME_USAGE_PLAIN_NODE_TYPE;
	}
	
	/* (non-Javadoc)
	 * @see de.uka.ipd.idaho.onn.stringPool.StringPoolRestClient#getStringSetNodeType()
	 */
	public String getStringSetNodeType() {
		return NAME_USAGE_SET_NODE_TYPE;
	}
	
	private RefBankRestClient rbk;
	private BinoBankRestClient bbk;
	private TxnBankRestClient txn;
	
	/**
	 * Constructor
	 * @param baseUrl the URL of the TnuBank node to connect to
	 */
	public TnuBankRestClient(String baseUrl) {
		super(baseUrl);
		baseUrl = baseUrl.substring(0, baseUrl.lastIndexOf('/'));
		this.bbk = new BinoBankRestClient(baseUrl + "/bbk");
		this.txn = new TxnBankRestClient(baseUrl + "/txn");
		this.rbk = new RefBankRestClient(baseUrl + "/rbk");
	}
	
	/**
	 * Constructor
	 * @param baseUrl the URL of the TnuBank node to connect to
	 * @param bbkBaseUrl the URL of the BinoBank node to connect to
	 * @param txnBaseUrl the URL of the TxnBank node to connect to
	 * @param rbkBaseUrl the URL of the RefBank node to connect to
	 */
	public TnuBankRestClient(String baseUrl, String bbkBaseUrl, String txnBaseUrl, String rbkBaseUrl) {
		super(baseUrl);
		this.bbk = new BinoBankRestClient(bbkBaseUrl);
		this.txn = new TxnBankRestClient(txnBaseUrl);
		this.rbk = new RefBankRestClient(rbkBaseUrl);
	}
	
	/* (non-Javadoc)
	 * @see de.uka.ipd.idaho.tnuBank.TnuBankClient#getNameUsage(java.lang.String, boolean)
	 */
	public PooledString getNameUsage(String nameUsageId, boolean expand) throws IOException {
		String[] nameUsageIds = {nameUsageId};
		PooledStringIterator nameUsageIt = this.getNameUsages(nameUsageIds, expand);
		if (nameUsageIt.hasNextString())
			return nameUsageIt.getNextString();
		IOException ioe = nameUsageIt.getException();
		if (ioe == null)
			return null;
		throw ioe;
	}
	
	/* (non-Javadoc)
	 * @see de.uka.ipd.idaho.tnuBank.TnuBankClient#getNameUsages(java.lang.String[], boolean)
	 */
	public PooledStringIterator getNameUsages(String[] nameUsageIds, boolean expand) {
		try {
			StringBuffer nameUsageIdString = new StringBuffer();
			for (int i = 0; i < nameUsageIds.length; i++)
				nameUsageIdString.append("&" + STRING_ID_ATTRIBUTE + "=" + URLEncoder.encode(nameUsageIds[i], ENCODING));
			return this.receiveStrings(ACTION_PARAMETER + "=" + GET_ACTION_NAME + (expand ? ("&" + EXPAND_PARAMETER + "=" + EXPAND_PARAMETER) : "") + nameUsageIdString.toString());
		}
		catch (IOException ioe) {
			return new ExceptionPSI(ioe);
		}
	}
	
	/* (non-Javadoc)
	 * @see de.uka.ipd.idaho.tnuBank.TnuBankClient#findNameUsages(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, int, int, boolean)
	 */
	public PooledStringIterator findNameUsages(String user, String taxNameString, String taxName, String taxNameRank, String nameUsageType, String bibRef, int pageNumber, int limit, boolean expand) {
		return this.findNameUsages(user, taxNameString, taxName, taxNameRank, nameUsageType, bibRef, pageNumber, false, limit, expand);
	}

	/* (non-Javadoc)
	 * @see de.uka.ipd.idaho.tnuBank.TnuBankClient#findNameUsages(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, int, boolean, int, boolean)
	 */
	public PooledStringIterator findNameUsages(String user, String taxNameString, String taxName, String taxNameRank, String nameUsageType, String bibRef, int pageNumber, boolean concise, int limit, boolean expand) {
		try {
			StringBuffer detailPredicates = new StringBuffer();
			if (taxNameString != null)
				detailPredicates.append("&" + TAXON_NAME_STRING_PARAMETER + "=" + URLEncoder.encode(taxNameString, ENCODING));
			if (taxName != null)
				detailPredicates.append("&" + TAXON_NAME_PARAMETER + "=" + URLEncoder.encode(taxName, ENCODING));
			if (taxNameRank != null)
				detailPredicates.append("&" + TAXON_NAME_RANK_PARAMETER + "=" + URLEncoder.encode(taxNameRank, ENCODING));
			if (nameUsageType != null)
				detailPredicates.append("&" + TAXON_NAME_USAGE_TYPE_PARAMETER + "=" + URLEncoder.encode(nameUsageType, ENCODING));
			if (bibRef != null)
				detailPredicates.append("&" + BIB_REF_PARAMETER + "=" + URLEncoder.encode(bibRef, ENCODING));
			if (pageNumber > -1)
				detailPredicates.append("&" + PAGE_NUMBER_PARAMETER + "=" + pageNumber);
			if (expand)
				detailPredicates.append("&" + EXPAND_PARAMETER + "=" + EXPAND_PARAMETER);
			return this.findStrings(null, false, null, user, concise, limit, false, detailPredicates.toString());
		}
		catch (IOException ioe) {
			return new ExceptionPSI(ioe);
		}
	}
	
	/* (non-Javadoc)
	 * @see de.uka.ipd.idaho.tnuBank.TnuBankClient#findNameUsages(java.lang.String, java.lang.String, java.util.Properties, java.lang.String, java.lang.String, int, int, int, boolean)
	 */
	public PooledStringIterator findNameUsages(String user, String taxNameString, Properties taxNameEpithets, String nameUsageType, String author, int year, int pageNumber, int limit, boolean expand) {
		return this.findNameUsages(user, taxNameString, taxNameEpithets, nameUsageType, author, year, pageNumber, false, limit, expand);
	}

	/* (non-Javadoc)
	 * @see de.uka.ipd.idaho.tnuBank.TnuBankClient#findNameUsages(java.lang.String, java.lang.String, java.util.Properties, java.lang.String, java.lang.String, int, int, boolean, int, boolean)
	 */
	public PooledStringIterator findNameUsages(String user, String taxNameString, Properties taxNameEpithets, String nameUsageType, String author, int year, int pageNumber, boolean concise, int limit, boolean expand) {
		try {
			StringBuffer detailPredicates = new StringBuffer();
			if (taxNameString != null)
				detailPredicates.append("&" + TAXON_NAME_STRING_PARAMETER + "=" + URLEncoder.encode(taxNameString, ENCODING));
			if (taxNameEpithets != null) {
				for (Iterator rnit = taxNameEpithets.keySet().iterator(); rnit.hasNext();) {
					String rankName = ((String) rnit.next());
					if (primaryRankNames.indexOf(";" + rankName + ";") != -1)
						detailPredicates.append("&" + rankName + "=" + URLEncoder.encode(taxNameEpithets.getProperty(rankName), ENCODING));
				}
			}
			if (nameUsageType != null)
				detailPredicates.append("&" + TAXON_NAME_USAGE_TYPE_PARAMETER + "=" + URLEncoder.encode(nameUsageType, ENCODING));
			if (author != null)
				detailPredicates.append("&" + BIB_REF_AUTHOR_PARAMETER + "=" + URLEncoder.encode(author, ENCODING));
			if (year > -1)
				detailPredicates.append("&" + BIB_REF_YEAR_PARAMETER + "=" + pageNumber);
			if (pageNumber > -1)
				detailPredicates.append("&" + PAGE_NUMBER_PARAMETER + "=" + pageNumber);
			if (expand)
				detailPredicates.append("&" + EXPAND_PARAMETER + "=" + EXPAND_PARAMETER);
			return this.findStrings(null, false, null, user, concise, limit, false, detailPredicates.toString());
		}
		catch (IOException ioe) {
			return new ExceptionPSI(ioe);
		}
	}
	
	/* (non-Javadoc)
	 * @see de.uka.ipd.idaho.tnuBank.TnuBankClient#updateNameUsage(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, int, java.lang.String)
	 */
	public PooledString updateNameUsage(String nameStringId, String taxonNameId, String nameUsageType, String nameUsageSubType, String bibRefId, int pageNumber, String user) throws IOException {
		TaxonNameUsage tnu = new TaxonNameUsage(nameStringId, taxonNameId, nameUsageType, nameUsageSubType, bibRefId, pageNumber);
		UploadString[] strings = {new UploadString(tnu.toPlainString(), tnu.toParsedString())};
		PooledStringIterator stringIt = super.updateStrings(strings, user);
		if (stringIt.hasNextString())
			return stringIt.getNextString();
		IOException ioe = stringIt.getException();
		if (ioe == null)
			return null;
		throw ioe;
	}
	
	/* (non-Javadoc)
	 * @see de.uka.ipd.idaho.tnuBank.TnuBankClient#updateNameUsage(java.lang.String, de.uka.ipd.idaho.plugins.taxonomicNames.TaxonomicNameUtils.TaxonomicName, java.lang.String, java.lang.String, de.uka.ipd.idaho.plugins.bibRefs.BibRefUtils.RefData, int, java.lang.String)
	 */
	public PooledString updateNameUsage(String nameString, TaxonomicName taxonName, String nameUsageType, String nameUsageSubType, RefData bibRef, int pageNumber, String user) throws IOException {
		
		//	upload name string to BinoBank
		PooledString nameStringPs = this.bbk.updateString(nameString, user);
		if (nameStringPs == null)
			throw new IOException("Could not store name string '" + nameString + "' in BinoBank");
		
		//	upload taxon name to TxnBank
		PooledString taxonNamePs = ((taxonName == null) ? null : this.txn.updateName(taxonName, user));
		
		//	upload bib ref to RefBank
		PooledString bibRefPs = this.rbk.updateReference(bibRef, user);
		if (bibRefPs == null)
			throw new IOException("Could not store bibliographic reference in RefBank");
		
		//	finally, upload name usage
		return this.updateNameUsage(nameStringPs.id, ((taxonNamePs == null) ? null : taxonNamePs.id), nameUsageType, ((taxonNamePs == null) ? null : nameUsageSubType), bibRefPs.id, pageNumber, user);
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		TnuBankRestClient tnuc = new TnuBankRestClient("http://plazi2.cs.umb.edu:8080/TnuBank/tnu");
		String ns = "Monomorium dentatum";
		TaxonomicName tn = new TaxonomicName((String) null);
		tn.setEpithet(ORDER_ATTRIBUTE, "Hymenoptera");
		tn.setEpithet(FAMILY_ATTRIBUTE, "Formicidae");
		tn.setEpithet(GENUS_ATTRIBUTE, "Monomorium");
		tn.setEpithet(SPECIES_ATTRIBUTE, "dentatum");
		RefData br = BibRefUtils.modsXmlToRefData(SgmlDocumentReader.readDocument(new StringReader("<mods:mods xmlns:mods=\"http://www.loc.gov/mods/v3\"><mods:titleInfo><mods:title>Monomorium dentatum sp. n., a new ant species from Egypt (Hymenoptera: Formicidae) related to the fossulatum group.</mods:title></mods:titleInfo><mods:name type=\"personal\"><mods:role><mods:roleTerm>Author</mods:roleTerm></mods:role><mods:namePart>Sharaf, M. R.</mods:namePart></mods:name><mods:typeOfResource>text</mods:typeOfResource><mods:relatedItem type=\"host\"><mods:titleInfo><mods:title>Zoology in the Middle East</mods:title></mods:titleInfo><mods:part><mods:detail type=\"volume\"><mods:number>41</mods:number></mods:detail><mods:date>2007</mods:date><mods:extent unit=\"page\"><mods:start>93</mods:start><mods:end>98</mods:end></mods:extent></mods:part></mods:relatedItem><mods:location><mods:url>http://antbase.org/ants/publications/21330/21330.pdf</mods:url></mods:location><mods:identifier type=\"HNS-PUB\">21330</mods:identifier><mods:classification>journal article</mods:classification></mods:mods>")));
		int pn = 94;
		PooledString ps = tnuc.updateNameUsage(ns, tn, NOMENCLATURE_NAME_USAGE_TYPE, ORIGINAL_DESCRIPTION_NAME_USAGE_TYPE, br, pn, "GuidoTester");
		System.out.println(ps.getStringPlain() + "\n" + ps.getParseChecksum() + ": " + ps.getStringParsed());
	}
}