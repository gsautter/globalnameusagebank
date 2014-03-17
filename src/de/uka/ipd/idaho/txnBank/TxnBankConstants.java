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
import java.io.StringReader;

import de.uka.ipd.idaho.gamta.util.SgmlDocumentReader;
import de.uka.ipd.idaho.onn.stringPool.StringPoolClient.UploadString;
import de.uka.ipd.idaho.onn.stringPool.StringPoolConstants;
import de.uka.ipd.idaho.plugins.taxonomicNames.TaxonomicNameConstants;
import de.uka.ipd.idaho.plugins.taxonomicNames.TaxonomicNameUtils;
import de.uka.ipd.idaho.plugins.taxonomicNames.TaxonomicNameUtils.TaxonomicName;
import de.uka.ipd.idaho.plugins.taxonomicNames.TaxonomicRankSystem;
import de.uka.ipd.idaho.plugins.taxonomicNames.TaxonomicRankSystem.Rank;
import de.uka.ipd.idaho.plugins.taxonomicNames.TaxonomicRankSystem.RankGroup;

/**
 * Constant bearer for Taxon Name Bank
 * 
 * @author sautter
 */
public interface TxnBankConstants extends StringPoolConstants, TaxonomicNameConstants {
	
	public static final String TXN_XML_NAMESPACE = null;//"http://idaho.ipd.uka.de/sp/schema";
	public static final String TXN_XML_NAMESPACE_ATTRIBUTE = ((TXN_XML_NAMESPACE == null) ? "" : (" xmlns:txn=\"" + TXN_XML_NAMESPACE + "\""));
	public static final String TXN_XML_NAMESPACE_PREFIX = ((TXN_XML_NAMESPACE == null) ? "" : "txn:");
	
	public static final String NAME_SET_NODE_TYPE = (TXN_XML_NAMESPACE_PREFIX + "taxonNameSet");
	public static final String NAME_NODE_TYPE = (TXN_XML_NAMESPACE_PREFIX + "taxonName");
	public static final String NAME_PLAIN_NODE_TYPE = (TXN_XML_NAMESPACE_PREFIX + "taxonNameString");
	public static final String NAME_PARSED_NODE_TYPE = (TXN_XML_NAMESPACE_PREFIX + "taxonNameParsed");
	
	public static final String KINGDOM_RANK_GROUP_PARAMETER = KINGDOM_ATTRIBUTE;
	public static final String PHYLUM_RANK_GROUP_PARAMETER = PHYLUM_ATTRIBUTE;
	public static final String CLASS_RANK_GROUP_PARAMETER = CLASS_ATTRIBUTE;
	public static final String ORDER_RANK_GROUP_PARAMETER = ORDER_ATTRIBUTE;
	public static final String FAMILY_RANK_GROUP_PARAMETER = FAMILY_ATTRIBUTE;
	public static final String GENUS_RANK_GROUP_PARAMETER = GENUS_ATTRIBUTE;
	public static final String SPECIES_RANK_GROUP_PARAMETER = SPECIES_ATTRIBUTE;
	public static final String RANK_PARAMETER = RANK_ATTRIBUTE;
	
	public static final String DARWIN_CORE_FORMAT = "DwC";
	public static final String SIMPLE_DARWIN_CORE_FORMAT = "SimpleDwC";
	
	/**
	 * Utility class normalizing taxonomic names for uploads. If either of the
	 * <code>prepareUploadString()</code> methods returns a non-null result,
	 * that <code>UploadString</code> is guaranteed to pass the server side
	 * acceptance test of TaxonNameBank nodes.
	 * 
	 * @author sautter
	 */
	public static class TaxonomicNameChecker {
		private static TaxonomicRankSystem rankSystem;
		private static RankGroup[] rankGroups;
		private static Rank[] primaryRanks;
		private static Rank[] ranks;
		private static Rank familyRank;
		private static Rank genusRank;
		static {
			rankSystem = TaxonomicRankSystem.getRankSystem(null);
			
			//	cache rank system details
			rankGroups = rankSystem.getRankGroups();
			primaryRanks = new Rank[rankGroups.length];
			for (int g = 0; g < rankGroups.length; g++)
				primaryRanks[g] = rankGroups[g].getRank(rankGroups[g].name);
			ranks = rankSystem.getRanks();
			familyRank = rankSystem.getRank(FAMILY_ATTRIBUTE);
			genusRank = rankSystem.getRank(GENUS_ATTRIBUTE);
		}
		
		/**
		 * Synthesize an upload string (pair of plain and parsed string) from
		 * the parsed representation of a taxon name. This method does multiple
		 * normalizations:<ul>
		 * <li>eliminate intermediate ranks above genus, or above the primary
		 * rank that is the lowest direct or indirect parent to the argument
		 * taxon name; whichever is higher</li>
		 * <li>eliminate authority, as Taxon Name Bank is purely about fully
		 * qualified names</li></ul>
		 * If the argument parsed taxon name is not fit for upload, this method
		 * returns <code>null</code>.
		 * @param stringParsed the parsed taxon name in DwC
		 * @return the uploadable pair of plain and parsed string
		 */
		public static UploadString prepareUploadString(String stringParsed) {
			
			//	transform parsed string into taxon name object
			if (stringParsed != null) try {
				return doPrepareUploadString(TaxonomicNameUtils.dwcXmlToTaxonomicName(SgmlDocumentReader.readDocument(new StringReader(stringParsed)), rankSystem));
			} catch (IOException ioe) {}
			
			//	somehow we couldn't do anything about this one
			return null;
		}
		
		/**
		 * Synthesize an upload string (pair of plain and parsed string) from
		 * the object representation of a taxon name. This method does multiple
		 * normalizations:<ul>
		 * <li>eliminate intermediate ranks above genus, or above the primary
		 * rank that is the lowest direct or indirect parent to the argument
		 * taxon name; whichever is higher</li>
		 * <li>eliminate authority, as Taxon Name Bank is purely about fully
		 * qualified names</li></ul>
		 * If the argument parsed taxon name is not fit for upload, this method
		 * returns <code>null</code>.
		 * @param taxName the taxon name object
		 * @return the uploadable pair of plain and parsed string
		 */
		public static UploadString prepareUploadString(TaxonomicName taxName) {
			
			//	check data
			if (taxName == null)
				return null;
			
			//	clone taxon name to encapsulate modifications
			return doPrepareUploadString(new TaxonomicName(taxName));
		}
		
		private static UploadString doPrepareUploadString(TaxonomicName taxName) {
			
			//	obtain and check rank
			Rank taxNameRank = rankSystem.getRank(taxName.getRank());
			if (taxNameRank == null) {
				System.out.println("TaxonomicNameChecker: Could not determine taxon name rank");
				return null;
			}
			
			//	compute two levels of primary parent ranks
			Rank primaryGrandParentRank;
			Rank primaryParentRank;
			if (taxNameRank.getRelativeSignificance() <= genusRank.getRelativeSignificance()) {
				primaryGrandParentRank = null;
				primaryParentRank = null;
				for (int p = 0; p < primaryRanks.length; p++)
					if (primaryRanks[p].getRelativeSignificance() < taxNameRank.getRelativeSignificance()) {
						primaryGrandParentRank = ((p == 0) ? null : primaryRanks[p-1]);
						primaryParentRank = primaryRanks[p];
					}
			}
			else {
				primaryGrandParentRank = familyRank;
				primaryParentRank = genusRank;
			}
			
			//	check primary parent ranks
			if ((primaryGrandParentRank != null) && (taxName.getEpithet(primaryGrandParentRank.name) == null)) {
				System.out.println("TaxonomicNameChecker: " + primaryGrandParentRank.name + " (PGP) epithet lacking");
				return null;
			}
			if ((primaryParentRank != null) && (taxName.getEpithet(primaryParentRank.name) == null)) {
				System.out.println("TaxonomicNameChecker: " + primaryGrandParentRank.name + " (PP) epithet lacking");
				return null;
			}
			
			//	compile taxon name string, eliminating too high up intermediate ranks along the way
			StringBuffer taxNameString = new StringBuffer();
			for (int r = 0; r < ranks.length; r++) {
				String epithet = taxName.getEpithet(ranks[r].name);
				if (epithet == null)
					continue;
				if (ranks[r].isPrimary() || (primaryParentRank.getRelativeSignificance() <= ranks[r].getRelativeSignificance())) {
					if (taxNameString.length() != 0)
						taxNameString.append(' ');
					taxNameString.append(ranks[r].formatEpithet(epithet));
				}
				else taxName.setEpithet(ranks[r].name, null);
				if (taxNameRank == ranks[r])
					break; // lowest rank dealt with, we're done here
			}
			
			//	eliminate authority (we're not storing that)
			taxName.setAuthority(null, -1);
			
			System.out.println("TaxonomicNameChecker: Upload String created:");
			System.out.println("- " + taxNameString.toString());
			System.out.println("- " + taxName.toDwcXml());
			
			//	synthesize new pair of plain and parsed string
			return new UploadString(taxNameString.toString(), taxName.toDwcXml());
		}
	}
}