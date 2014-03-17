/* TxnBank, the distributed platform for fully qualified taxonomic names.
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
package de.uka.ipd.idaho.txnBank;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.LinkedList;

import de.uka.ipd.idaho.onn.stringPool.StringPoolRestClient;
import de.uka.ipd.idaho.plugins.taxonomicNames.TaxonomicNameUtils.TaxonomicName;

/**
 * TxnBank specific REST client, adding detail search for taxonomic names.
 * 
 * @author sautter
 */
public class TxnBankRestClient extends StringPoolRestClient implements TxnBankConstants, TxnBankClient {
	
	/* (non-Javadoc)
	 * @see de.uka.ipd.idaho.onn.stringPool.StringPoolRestClient#getNamespaceAttribute()
	 */
	public String getNamespaceAttribute() {
		return TXN_XML_NAMESPACE_ATTRIBUTE;
	}
	
	/* (non-Javadoc)
	 * @see de.uka.ipd.idaho.onn.stringPool.StringPoolRestClient#getStringNodeType()
	 */
	public String getStringNodeType() {
		return NAME_NODE_TYPE;
	}
	
	/* (non-Javadoc)
	 * @see de.uka.ipd.idaho.onn.stringPool.StringPoolRestClient#getStringParsedNodeType()
	 */
	public String getStringParsedNodeType() {
		return NAME_PARSED_NODE_TYPE;
	}
	
	/* (non-Javadoc)
	 * @see de.uka.ipd.idaho.onn.stringPool.StringPoolRestClient#getStringPlainNodeType()
	 */
	public String getStringPlainNodeType() {
		return NAME_PLAIN_NODE_TYPE;
	}
	
	/* (non-Javadoc)
	 * @see de.uka.ipd.idaho.onn.stringPool.StringPoolRestClient#getStringSetNodeType()
	 */
	public String getStringSetNodeType() {
		return NAME_SET_NODE_TYPE;
	}
	
	/**
	 * Constructor
	 * @param baseUrl the URL of the TxnBank node to connect to
	 */
	public TxnBankRestClient(String baseUrl) {
		super(baseUrl);
	}
	
	/* (non-Javadoc)
	 * @see de.uka.ipd.idaho.txnBank.TxnBankClient#findNames(java.lang.String[], boolean, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, int)
	 */
	public PooledStringIterator findNames(String[] textPredicates, boolean disjunctive, String user, String txKingdom, String txPhylum, String txClass, String txOrder, String txFamily, String txGenus, String txSpecies, String txRank, int limit) {
		return this.findNames(textPredicates, disjunctive, user, txKingdom, txPhylum, txClass, txOrder, txFamily, txGenus, txSpecies, txRank, false, limit);
	}

	/* (non-Javadoc)
	 * @see de.uka.ipd.idaho.txnBank.TxnBankClient#findNames(java.lang.String[], boolean, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, boolean, int)
	 */
	public PooledStringIterator findNames(String[] textPredicates, boolean disjunctive, String user, String txKingdom, String txPhylum, String txClass, String txOrder, String txFamily, String txGenus, String txSpecies, String txRank, boolean concise, int limit) {
		try {
			StringBuffer detailPredicates = new StringBuffer();
			if (txKingdom != null)
				detailPredicates.append("&" + KINGDOM_RANK_GROUP_PARAMETER + "=" + URLEncoder.encode(txKingdom, ENCODING));
			if (txPhylum != null)
				detailPredicates.append("&" + PHYLUM_RANK_GROUP_PARAMETER + "=" + URLEncoder.encode(txPhylum, ENCODING));
			if (txClass != null)
				detailPredicates.append("&" + CLASS_RANK_GROUP_PARAMETER + "=" + URLEncoder.encode(txClass, ENCODING));
			if (txOrder != null)
				detailPredicates.append("&" + ORDER_RANK_GROUP_PARAMETER + "=" + URLEncoder.encode(txOrder, ENCODING));
			if (txFamily != null)
				detailPredicates.append("&" + FAMILY_RANK_GROUP_PARAMETER + "=" + URLEncoder.encode(txFamily, ENCODING));
			if (txGenus != null)
				detailPredicates.append("&" + GENUS_RANK_GROUP_PARAMETER + "=" + URLEncoder.encode(txGenus, ENCODING));
			if (txSpecies != null)
				detailPredicates.append("&" + SPECIES_RANK_GROUP_PARAMETER + "=" + URLEncoder.encode(txSpecies, ENCODING));
			return this.findStrings(textPredicates, disjunctive, txRank, user, concise, limit, false, detailPredicates.toString());
		}
		catch (IOException ioe) {
			return new ExceptionPSI(ioe);
		}
	}
	
	/* (non-Javadoc)
	 * @see de.uka.ipd.idaho.txnBank.TxnBankClient#updateName(de.uka.ipd.idaho.plugins.taxonomicNames.TaxonomicNameUtils.TaxonomicName, java.lang.String)
	 */
	public PooledString updateName(TaxonomicName taxName, String user) throws IOException {
		TaxonomicName[] taxNames = {taxName};
		PooledStringIterator psi = this.updateNames(taxNames, user);
		if (psi.hasNextString())
			return psi.getNextString();
		IOException ioe = psi.getException();
		if (ioe == null)
			return null;
		throw ioe;
	}
	
	/* (non-Javadoc)
	 * @see de.uka.ipd.idaho.txnBank.TxnBankClient#updateNames(de.uka.ipd.idaho.plugins.taxonomicNames.TaxonomicNameUtils.TaxonomicName[], java.lang.String)
	 */
	public PooledStringIterator updateNames(TaxonomicName[] taxNames, String user) {
		LinkedList taxNameList = new LinkedList();
		for (int t = 0; t < taxNames.length; t++) {
			UploadString taxNameString = TaxonomicNameChecker.prepareUploadString(taxNames[t]);
			if (taxNameString != null)
				taxNameList.add(taxNameString);
		}
		return super.updateStrings(((UploadString[]) taxNameList.toArray(new UploadString[taxNameList.size()])), user);
	}
	
	/**
	 * This implementation overwrites the original one from the super class to
	 * simply throw an <code>UnsupportedOperationException</code> because
	 * Taxon Name Bank accepts only parsed taxon names.
	 * @see de.uka.ipd.idaho.onn.stringPool.StringPoolRestClient#updateString(java.lang.String, java.lang.String)
	 */
	public PooledString updateString(String stringPlain, String user) throws IOException {
		throw new UnsupportedOperationException("TaxonNameBank accepts parsed taxon names only."); // we're not accepting strings without a parsed version
	}
	
	/**
	 * This implementation overwrites the original one from the super class to
	 * simply throw an <code>UnsupportedOperationException</code> because
	 * Taxon Name Bank accepts only parsed taxon names.
	 * @see de.uka.ipd.idaho.onn.stringPool.StringPoolRestClient#updateStrings(java.lang.String[], java.lang.String)
	 */
	public PooledStringIterator updateStrings(String[] stringsPlain, String user) {
		throw new UnsupportedOperationException("TaxonNameBank accepts parsed taxon names only."); // we're not accepting strings without a parsed version
	}
	
	/**
	 * This implementation overwrites the original one from the super class to
	 * check and normalize the parsed versions of the argument strings via the
	 * <code>TaxonomicNameChecker.prepareUploadString()</code> method. If this
	 * latter method finds an argument string unfit for upload, this method
	 * simply ignores it. The check works through the argument array one by
	 * one, so one unfit string does not prevent any fit ones from being
	 * uploaded.
	 * @see de.uka.ipd.idaho.onn.stringPool.StringPoolRestClient#updateStrings(de.uka.ipd.idaho.onn.stringPool.StringPoolClient.UploadString[], java.lang.String)
	 */
	public PooledStringIterator updateStrings(UploadString[] strings, String user) {
		LinkedList stringList = new LinkedList();
		for (int s = 0; s < strings.length; s++) {
			UploadString string = TaxonomicNameChecker.prepareUploadString(strings[s].stringParsed);
			if (string != null)
				stringList.add(string);
		}
		return super.updateStrings(((UploadString[]) stringList.toArray(new UploadString[stringList.size()])), user);
	}
}