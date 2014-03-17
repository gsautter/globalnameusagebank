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
import java.util.LinkedList;
import java.util.Properties;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import com.fasterxml.uuid.Generators;
import com.fasterxml.uuid.impl.NameBasedGenerator;

import de.uka.ipd.idaho.easyIO.sql.TableDefinition;
import de.uka.ipd.idaho.gamta.Annotation;
import de.uka.ipd.idaho.gamta.MutableAnnotation;
import de.uka.ipd.idaho.onn.stringPool.StringPoolServlet;
import de.uka.ipd.idaho.plugins.taxonomicNames.TaxonomicNameUtils;
import de.uka.ipd.idaho.plugins.taxonomicNames.TaxonomicNameUtils.TaxonomicName;
import de.uka.ipd.idaho.plugins.taxonomicNames.TaxonomicRankSystem;
import de.uka.ipd.idaho.plugins.taxonomicNames.TaxonomicRankSystem.Rank;
import de.uka.ipd.idaho.plugins.taxonomicNames.TaxonomicRankSystem.RankGroup;
import de.uka.ipd.idaho.stringUtils.StringVector;

/**
 * @author sautter
 */
public class TxnBankServlet extends StringPoolServlet implements TxnBankClient, TxnBankConstants {
	
	/* (non-Javadoc)
	 * @see de.uka.ipd.idaho.onn.stringPool.StringPoolServlet#getExternalDataName()
	 */
	protected String getExternalDataName() {
		return "TaxonName";
	}
	
	/* (non-Javadoc)
	 * @see de.uka.ipd.idaho.onn.stringPool.StringPoolServlet#getNamespaceAttribute()
	 */
	public String getNamespaceAttribute() {
		return TXN_XML_NAMESPACE_ATTRIBUTE;
	}
	
	/* (non-Javadoc)
	 * @see de.uka.ipd.idaho.onn.stringPool.StringPoolServlet#getStringNodeType()
	 */
	public String getStringNodeType() {
		return NAME_NODE_TYPE;
	}
	
	/* (non-Javadoc)
	 * @see de.uka.ipd.idaho.onn.stringPool.StringPoolServlet#getStringParsedNodeType()
	 */
	public String getStringParsedNodeType() {
		return NAME_PARSED_NODE_TYPE;
	}
	
	/* (non-Javadoc)
	 * @see de.uka.ipd.idaho.onn.stringPool.StringPoolServlet#getStringPlainNodeType()
	 */
	public String getStringPlainNodeType() {
		return NAME_PLAIN_NODE_TYPE;
	}
	
	/* (non-Javadoc)
	 * @see de.uka.ipd.idaho.onn.stringPool.StringPoolServlet#getStringSetNodeType()
	 */
	public String getStringSetNodeType() {
		return NAME_SET_NODE_TYPE;
	}
	
	private static final String KINGDOM_RANK_GROUP_COLUMN_NAME = "txKingdom";
	private static final String PHYLUM_RANK_GROUP_COLUMN_NAME = "txPhylum";
	private static final String CLASS_RANK_GROUP_COLUMN_NAME = "txClass";
	private static final String ORDER_RANK_GROUP_COLUMN_NAME = "txOrder";
	private static final String FAMILY_RANK_GROUP_COLUMN_NAME = "txFamily";
	private static final int HIGHER_FAMILY_COLUMN_LENGTH = 32;
	private static final String GENUS_RANK_GROUP_COLUMN_NAME = "txGenus";
	private static final String SPECIES_RANK_GROUP_COLUMN_NAME = "txSpecies";
	private static final int GENUS_SPECIES_COLUMN_LENGTH = 80;
	
	/* (non-Javadoc)
	 * @see de.uka.ipd.idaho.onn.stringPool.StringPoolServlet#extendIndexTableDefinition(de.uka.ipd.idaho.easyIO.sql.TableDefinition)
	 */
	protected boolean extendIndexTableDefinition(TableDefinition itd) {
		itd.addColumn(KINGDOM_RANK_GROUP_COLUMN_NAME, TableDefinition.VARCHAR_DATATYPE, HIGHER_FAMILY_COLUMN_LENGTH);
		itd.addColumn(PHYLUM_RANK_GROUP_COLUMN_NAME, TableDefinition.VARCHAR_DATATYPE, HIGHER_FAMILY_COLUMN_LENGTH);
		itd.addColumn(CLASS_RANK_GROUP_COLUMN_NAME, TableDefinition.VARCHAR_DATATYPE, HIGHER_FAMILY_COLUMN_LENGTH);
		itd.addColumn(ORDER_RANK_GROUP_COLUMN_NAME, TableDefinition.VARCHAR_DATATYPE, HIGHER_FAMILY_COLUMN_LENGTH);
		itd.addColumn(FAMILY_RANK_GROUP_COLUMN_NAME, TableDefinition.VARCHAR_DATATYPE, HIGHER_FAMILY_COLUMN_LENGTH);
		itd.addColumn(GENUS_RANK_GROUP_COLUMN_NAME, TableDefinition.VARCHAR_DATATYPE, GENUS_SPECIES_COLUMN_LENGTH);
		itd.addColumn(SPECIES_RANK_GROUP_COLUMN_NAME, TableDefinition.VARCHAR_DATATYPE, GENUS_SPECIES_COLUMN_LENGTH);
		return true;
	}
	
	private TaxonomicRankSystem rankSystem;
	private RankGroup[] rankGroups;
	private Rank[] primaryRanks;
	private Rank familyRank;
	private Rank genusRank;
	
	/* (non-Javadoc)
	 * @see de.uka.ipd.idaho.onn.stringPool.StringPoolServlet#doInit()
	 */
	protected void doInit() throws ServletException {
		super.doInit();
		
		//	get generic rank system (we'll be handling names from all domains)
		this.rankSystem = TaxonomicRankSystem.getRankSystem(null);
		
		//	cache rank system details
		this.rankGroups = this.rankSystem.getRankGroups();
		this.primaryRanks = new Rank[this.rankGroups.length];
		for (int g = 0; g < this.rankGroups.length; g++)
			this.primaryRanks[g] = this.rankGroups[g].getRank(this.rankGroups[g].name);
		this.familyRank = this.rankSystem.getRank(FAMILY_ATTRIBUTE);
		this.genusRank = this.rankSystem.getRank(GENUS_ATTRIBUTE);
	}
	
	/* (non-Javadoc)
	 * @see de.uka.ipd.idaho.onn.stringPool.StringPoolServlet#getXmlNamespaceUriBindings()
	 */
	protected String getXmlNamespaceUriBindings() {
		return "xmlns:dwc=\"http://digir.net/schema/conceptual/darwin/2003/1.0\" xmlns:dwcranks=\"http://rs.tdwg.org/UBIF/2006/Schema/1.1\"";
	}
	
	/* (non-Javadoc)
	 * @see de.uka.ipd.idaho.onn.stringPool.StringPoolServlet#addIndexPredicates(javax.servlet.http.HttpServletRequest, java.util.Properties)
	 */
	protected void addIndexPredicates(HttpServletRequest request, Properties detailPredicates) {
		String txKingdom = request.getParameter(KINGDOM_RANK_GROUP_PARAMETER);
		if (txKingdom != null)
			detailPredicates.setProperty(KINGDOM_RANK_GROUP_COLUMN_NAME, txKingdom);
		String txPhylum = request.getParameter(PHYLUM_RANK_GROUP_PARAMETER);
		if (txPhylum != null)
			detailPredicates.setProperty(PHYLUM_RANK_GROUP_COLUMN_NAME, txPhylum);
		String txClass = request.getParameter(CLASS_RANK_GROUP_PARAMETER);
		if (txClass != null)
			detailPredicates.setProperty(CLASS_RANK_GROUP_COLUMN_NAME, txClass);
		String txOrder = request.getParameter(ORDER_RANK_GROUP_PARAMETER);
		if (txOrder != null)
			detailPredicates.setProperty(ORDER_RANK_GROUP_COLUMN_NAME, txOrder);
		String txFamily = request.getParameter(FAMILY_RANK_GROUP_PARAMETER);
		if (txFamily != null)
			detailPredicates.setProperty(FAMILY_RANK_GROUP_COLUMN_NAME, txFamily);
		String txGenus = request.getParameter(GENUS_RANK_GROUP_PARAMETER);
		if (txGenus != null)
			detailPredicates.setProperty(GENUS_RANK_GROUP_COLUMN_NAME, txGenus);
		String txSpecies = request.getParameter(SPECIES_RANK_GROUP_PARAMETER);
		if (txSpecies != null)
			detailPredicates.setProperty(SPECIES_RANK_GROUP_COLUMN_NAME, txSpecies);
	}
	
	/* (non-Javadoc)
	 * @see de.uka.ipd.idaho.onn.stringPool.StringPoolServlet#extendIndexData(de.uka.ipd.idaho.onn.stringPool.StringPoolServlet.ParsedStringIndexData, de.uka.ipd.idaho.gamta.MutableAnnotation)
	 */
	protected void extendIndexData(ParsedStringIndexData indexData, MutableAnnotation stringParsed) {
		
		//	get index data
		TaxonNameIndexData taxonNameIndexData = this.getIndexData(stringParsed);
		
		//	add attributes
		indexData.addIndexAttribute(KINGDOM_RANK_GROUP_COLUMN_NAME, taxonNameIndexData.txKingdom.toLowerCase());
		indexData.addIndexAttribute(PHYLUM_RANK_GROUP_COLUMN_NAME, taxonNameIndexData.txPhylum.toLowerCase());
		indexData.addIndexAttribute(CLASS_RANK_GROUP_COLUMN_NAME, taxonNameIndexData.txClass.toLowerCase());
		indexData.addIndexAttribute(ORDER_RANK_GROUP_COLUMN_NAME, taxonNameIndexData.txOrder.toLowerCase());
		indexData.addIndexAttribute(FAMILY_RANK_GROUP_COLUMN_NAME, taxonNameIndexData.txFamily.toLowerCase());
		indexData.addIndexAttribute(GENUS_RANK_GROUP_COLUMN_NAME, taxonNameIndexData.txGenus.toLowerCase());
		indexData.addIndexAttribute(SPECIES_RANK_GROUP_COLUMN_NAME, taxonNameIndexData.txSpecies.toLowerCase());
	}
	
	/*
<dwc:Taxon>
  <dwc:taxonID>urn:lsid:catalogueoflife.org:taxon:df0a797c-29c1-102b-9a4a-00304854f820:col20120721</dwc:taxonID>
  <dwc:parentNameUsageID>urn:lsid:catalogueoflife.org:taxon:d79c11aa-29c1-102b-9a4a-00304854f820:col20120721</dwc:parentNameUsageID>
  <dwc:scientificName>Ctenomys sociabilis</dwc:scientificName>
  <dwc:scientificNameAuthorship>Pearson and Christie, 1985</dwc:scientificNameAuthorship>
  <dwc:taxonRank>species</dwc:taxonRank>
  <dwc:nomenclaturalCode>ICZN</dwc:nomenclaturalCode>
  <dwc:higherClassification>Animalia; Chordata; Vertebrata; Mammalia; Theria; Eutheria; Rodentia; Hystricognatha; Hystricognathi; Ctenomyidae; Ctenomyini; Ctenomys</dwc:higherClassification>
  <dwc:kingdom>Animalia</dwc:kingdom>
  <dwc:phylum>Chordata</dwc:phylum>
  <dwc:class>Mammalia</dwc:class>
  <dwc:order>Rodentia</dwc:order>
  <dwc:family>Ctenomyidae</dwc:family>
  <dwc:genus>Ctenomys</dwc:genus>
  <dwc:specificEpithet>sociabilis</dwc:specificEpithet>
</dwc:Taxon>
	 */
	
	private static class TaxonNameIndexData {
		final String txKingdom;
		final String txPhylum;
		final String txClass;
		final String txOrder;
		final String txFamily;
		final String txGenus;
		final String txSpecies;
		TaxonNameIndexData(String txKingdom, String txPhylum, String txClass, String txOrder, String txFamily, String txGenus, String txSpecies) {
			this.txKingdom = txKingdom;
			this.txPhylum = txPhylum;
			this.txClass = txClass;
			this.txOrder = txOrder;
			this.txFamily = txFamily;
			this.txGenus = txGenus;
			this.txSpecies = txSpecies;
		}
	}
	
	private TaxonNameIndexData getIndexData(MutableAnnotation stringParsed) {
		
		//	get attributes
		TaxonomicName taxName = TaxonomicNameUtils.dwcXmlToTaxonomicName(stringParsed);
		
		//	what do we want to index?
		StringBuffer txKingdom = new StringBuffer();
		StringBuffer txPhylum = new StringBuffer();
		StringBuffer txClass = new StringBuffer();
		StringBuffer txOrder = new StringBuffer();
		StringBuffer txFamily = new StringBuffer();
		StringBuffer txGenus = new StringBuffer();
		StringBuffer txSpecies = new StringBuffer();
		
		//	collect epithets
		for (int g = 0; g < this.rankGroups.length; g++) {
			StringBuffer target;
			if (SPECIES_ATTRIBUTE.equals(this.rankGroups[g].name))
				target = txSpecies;
			else if (GENUS_ATTRIBUTE.equals(this.rankGroups[g].name))
				target = txGenus;
			else if (FAMILY_ATTRIBUTE.equals(this.rankGroups[g].name))
				target = txFamily;
			else if (ORDER_ATTRIBUTE.equals(this.rankGroups[g].name))
				target = txOrder;
			else if (CLASS_ATTRIBUTE.equals(this.rankGroups[g].name))
				target = txClass;
			else if (PHYLUM_ATTRIBUTE.equals(this.rankGroups[g].name))
				target = txPhylum;
			else if (KINGDOM_ATTRIBUTE.equals(this.rankGroups[g].name))
				target = txKingdom;
			else continue;
			Rank[] ranks = this.rankGroups[g].getRanks();
			for (int r = 0; r < ranks.length; r++) {
				target.append('|');
				String epithet = taxName.getEpithet(ranks[r].name);
				if (epithet != null)
					target.append(epithet);
			}
		}
		
		//	trim data
		if (txKingdom.length() > HIGHER_FAMILY_COLUMN_LENGTH)
			txKingdom.delete(HIGHER_FAMILY_COLUMN_LENGTH, txKingdom.length());
		if (txPhylum.length() > HIGHER_FAMILY_COLUMN_LENGTH)
			txPhylum.delete(HIGHER_FAMILY_COLUMN_LENGTH, txPhylum.length());
		if (txClass.length() > HIGHER_FAMILY_COLUMN_LENGTH)
			txClass.delete(HIGHER_FAMILY_COLUMN_LENGTH, txClass.length());
		if (txOrder.length() > HIGHER_FAMILY_COLUMN_LENGTH)
			txOrder.delete(HIGHER_FAMILY_COLUMN_LENGTH, txOrder.length());
		if (txFamily.length() > HIGHER_FAMILY_COLUMN_LENGTH)
			txFamily.delete(HIGHER_FAMILY_COLUMN_LENGTH, txFamily.length());
		if (txGenus.length() > GENUS_SPECIES_COLUMN_LENGTH)
			txGenus.delete(GENUS_SPECIES_COLUMN_LENGTH, txGenus.length());
		if (txSpecies.length() > GENUS_SPECIES_COLUMN_LENGTH)
			txSpecies.delete(GENUS_SPECIES_COLUMN_LENGTH, txSpecies.length());
		
		//	finally ...
		return new TaxonNameIndexData(txKingdom.toString(), txPhylum.toString(), txClass.toString(), txOrder.toString(), txFamily.toString(), txGenus.toString(), txSpecies.toString());
	}
	
	/* (non-Javadoc)
	 * @see de.uka.ipd.idaho.onn.stringPool.StringPoolServlet#checkPlainString(java.lang.String, java.lang.String, de.uka.ipd.idaho.gamta.MutableAnnotation)
	 */
	protected String checkPlainString(String stringId, String stringPlain, MutableAnnotation stringParsed) {
		
		//	we require a parse ...
		if (stringParsed == null) {
			System.out.println("TxnBank: rejecting '" + stringPlain + "' for lacking parsed version.");
			return ("TaxonNameBank accepts parsed taxon names only.");
		}
		
		//	... and a valid one
		String parseError = this.checkParsedString(stringId, stringPlain, stringParsed);
		if (parseError != null)
			System.out.println("TxnBank: rejecting '" + stringPlain + "' for invalid parsed version (" + parseError + ")");
		
		return parseError;
	}
	
	/* (non-Javadoc)
	 * @see de.uka.ipd.idaho.onn.stringPool.StringPoolServlet#checkParsedString(java.lang.String, java.lang.String, de.uka.ipd.idaho.gamta.MutableAnnotation)
	 */
	protected String checkParsedString(String stringId, String stringPlain, MutableAnnotation stringParsed) {
		StringVector extraTokens = new StringVector();
		
		//	collect tokens that may exist outside plain string
		Annotation[] rankAnnots = stringParsed.getAnnotations("dwc:taxonRank");
		for (int r = 0; r < rankAnnots.length; r++) {
			if (this.rankSystem.getRank(rankAnnots[r].getValue()) != null)
				addTokens(extraTokens, rankAnnots[r]);
			else {
				System.out.println("TxnBank: rejecting parsed version of '" + stringPlain + "' for invalid rank '" + rankAnnots[r].getValue() + "'.");
				return ("Parsed string is inconsistent: '" + rankAnnots[r].getValue() + "' is not a valid rank.");
			}
		}
		
		//	check authority (we don't want one here)
		Annotation[] authorityAnnots = stringParsed.getAnnotations("dwc:scientificNameAuthorship");
		if (authorityAnnots.length != 0) {
			System.out.println("TxnBank: rejecting parsed version of '" + stringPlain + "' for authority '" + authorityAnnots[0].getValue() + "'.");
			return "Parsed string is invalid, authorities are not part of TaxonNameBank.";
		}
		
		//	check tokens
		for (int t = 0; t < stringParsed.size(); t++) {
			String value = stringParsed.valueAt(t);
			if ((stringPlain.indexOf(value) == -1) && !extraTokens.contains(value)) {
				System.out.println("TxnBank: rejecting parsed version of '" + stringPlain + "' for invalid token '" + value + "'.");
				return ("Parsed string is inconsistent with plain string: '" + value + "' is not a part of '" + stringPlain + "'.");
			}
		}
		
		//	parse taxon name into object for further validation
		TaxonomicName taxName = TaxonomicNameUtils.dwcXmlToTaxonomicName(stringParsed, this.rankSystem);
		
		//	obtain and check rank
		Rank taxNameRank = this.rankSystem.getRank(taxName.getRank());
		if (taxNameRank == null) {
			System.out.println("TxnBank: rejecting parsed version of '" + stringPlain + "' for undeterminable rank.");
			return "Parsed string is invalid, rank could not be determined.";
		}
		
		//	compute two levels of primary parent ranks
		Rank primaryGrandParentRank;
		Rank primaryParentRank;
		if (taxNameRank.getRelativeSignificance() <= this.genusRank.getRelativeSignificance()) {
			primaryGrandParentRank = null;
			primaryParentRank = null;
			for (int p = 0; p < this.primaryRanks.length; p++)
				if (this.primaryRanks[p].getRelativeSignificance() < taxNameRank.getRelativeSignificance()) {
					primaryGrandParentRank = ((p == 0) ? null : this.primaryRanks[p-1]);
					primaryParentRank = this.primaryRanks[p];
				}
		}
		else {
			primaryGrandParentRank = this.familyRank;
			primaryParentRank = this.genusRank;
		}
		
		//	check primary parent ranks
		if ((primaryGrandParentRank != null) && (taxName.getEpithet(primaryGrandParentRank.name) == null)) {
			System.out.println("TxnBank: rejecting parsed version of '" + stringPlain + "' for lacking " + primaryGrandParentRank.name + " epithet.");
			return ("Parsed string is invalid: taxon names of rank " + taxNameRank.name + " require a " + primaryGrandParentRank.name + " epithet to be present.");
		}
		if ((primaryParentRank != null) && (taxName.getEpithet(primaryParentRank.name) == null)) {
			System.out.println("TxnBank: rejecting parsed version of '" + stringPlain + "' for lacking " + primaryParentRank.name + " epithet.");
			return ("Parsed string is invalid: taxon names of rank " + taxNameRank.name + " require a " + primaryParentRank.name + " epithet to be present.");
		}
		
		//	no errors found
		return null;
	}
	private static void addTokens(StringVector tokens, Annotation annot) {
		for (int t = 0; t < annot.size(); t++)
			tokens.addElement(annot.valueAt(t));
	}
	
	/* (non-Javadoc)
	 * @see de.uka.ipd.idaho.onn.stringPool.StringPoolServlet#getStringType(de.uka.ipd.idaho.gamta.MutableAnnotation)
	 */
	protected String getStringType(MutableAnnotation stringParsed) {
		Annotation[] rankAnnots = stringParsed.getAnnotations("dwc:taxonRank");
		return (((rankAnnots != null) && (rankAnnots.length != 0)) ? rankAnnots[0].getValue() : null);
	}
	
	/**
	 * Overwrites ID generation to use UUID version 5 with 'globalnames.org' in
	 * the DNS namespace, also using an instance pool for increased performance.
	 * However, the UUIDs are converted to plain 32 character HEX strings to
	 * comply with the contract of this method.
	 * @see de.uka.ipd.idaho.onn.stringPool.StringPoolServlet#getStringId(java.lang.String)
	 */
	protected String getStringId(String string) throws IOException {
		NameBasedGenerator nbg = null;
		try {
			nbg = getUuidGenerator();
			UUID id = nbg.generate(string.getBytes("UTF-8"));
			String ids = id.toString().replaceAll("\\-", "").toUpperCase();
			return ids;
		}
		finally {
			returnUuidGenerator(nbg);
		}
	}
	private static UUID globalNamesInDns;
	private static LinkedList uuidGenerators = new LinkedList();
	private static NameBasedGenerator getUuidGenerator() throws IOException {
		synchronized (uuidGenerators) {
			if (uuidGenerators.size() != 0)
				return ((NameBasedGenerator) uuidGenerators.removeFirst());
			if (globalNamesInDns == null) {
				NameBasedGenerator nbg = Generators.nameBasedGenerator(NameBasedGenerator.NAMESPACE_DNS);
				globalNamesInDns = nbg.generate("globalnames.org");
			}
			return Generators.nameBasedGenerator(globalNamesInDns);
		}
	}
	private static void returnUuidGenerator(NameBasedGenerator nbg) {
		if (nbg == null)
			return;
		synchronized (uuidGenerators) {
			uuidGenerators.addLast(nbg);
		}
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
		Properties detailPredicates = new Properties();
		if (txKingdom != null)
			detailPredicates.setProperty(KINGDOM_RANK_GROUP_COLUMN_NAME, txKingdom.toLowerCase());
		if (txPhylum != null)
			detailPredicates.setProperty(PHYLUM_RANK_GROUP_COLUMN_NAME, txPhylum.toLowerCase());
		if (txClass != null)
			detailPredicates.setProperty(CLASS_RANK_GROUP_COLUMN_NAME, txClass.toLowerCase());
		if (txOrder != null)
			detailPredicates.setProperty(ORDER_RANK_GROUP_COLUMN_NAME, txOrder.toLowerCase());
		if (txFamily != null)
			detailPredicates.setProperty(FAMILY_RANK_GROUP_COLUMN_NAME, txFamily.toLowerCase());
		if (txGenus != null)
			detailPredicates.setProperty(GENUS_RANK_GROUP_COLUMN_NAME, txGenus.toLowerCase());
		if (txSpecies != null)
			detailPredicates.setProperty(SPECIES_RANK_GROUP_COLUMN_NAME, txSpecies.toLowerCase());
		return this.findStrings(textPredicates, disjunctive, txRank, user, concise, limit, false, detailPredicates);
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
	 * TaxonNameBank accepts only parsed taxon names.
	 * @see de.uka.ipd.idaho.onn.stringPool.StringPoolServlet#updateString(java.lang.String, java.lang.String)
	 */
	public PooledString updateString(String stringPlain, String user) throws IOException {
		throw new UnsupportedOperationException("TaxonNameBank accepts parsed taxon names only."); // we're not accepting strings without a parsed version
	}
	
	/**
	 * This implementation overwrites the original one from the super class to
	 * simply throw an <code>UnsupportedOperationException</code> because
	 * TaxonNameBank accepts only parsed taxon names.
	 * @see de.uka.ipd.idaho.onn.stringPool.StringPoolServlet#updateStrings(java.lang.String[], java.lang.String)
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
	 * @see de.uka.ipd.idaho.onn.stringPool.StringPoolServlet#updateStrings(de.uka.ipd.idaho.onn.stringPool.StringPoolClient.UploadString[], java.lang.String)
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