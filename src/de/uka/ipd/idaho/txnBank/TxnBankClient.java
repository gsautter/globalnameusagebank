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

import de.uka.ipd.idaho.onn.stringPool.StringPoolClient;
import de.uka.ipd.idaho.plugins.taxonomicNames.TaxonomicNameUtils.TaxonomicName;

/**
 * TxnBank specific client object, adding detail search for taxonomic names.
 * 
 * @author sautter
 */
public interface TxnBankClient extends StringPoolClient {
	
	/**
	 * Search for taxonomic names, using both full text and detail predicates.
	 * @param textPredicates the full text predicates
	 * @param disjunctive combine the predicates with 'or'?
	 * @param user the name of the user to contribute or last update the
	 *            names
	 * @param higher taxonomic epithet above family level to search for
	 * @param txKingdom taxonomic epithet between kingdom (inclusive) and phylum (exclusive)
	 * @param txPhylum taxonomic epithet between phylum (inclusive) and class (exclusive)
	 * @param txClass taxonomic epithet between class (inclusive) and order (exclusive)
	 * @param txOrder taxonomic epithet between order (inclusive) and family (exclusive)
	 * @param txFamily taxonomic epithet between family (inclusive) and genus (exclusive)
	 * @param txGenus taxonomic epithet between genus (inclusive) and species (exclusive)
	 * @param txSpecies taxonomic epithet at species level or below
	 * @param txRank rank of taxonomic names to find
	 * @param limit the maximum number of names to include in the result (0 means no limit)
	 * @return an iterator over the references matching the query
	 */
	public abstract PooledStringIterator findNames(String[] textPredicates, boolean disjunctive, String user, String txKingdom, String txPhylum, String txClass, String txOrder, String txFamily, String txGenus, String txSpecies, String txRank, int limit);
	
	/**
	 * Search for taxonomic names, using both full text and detail predicates.
	 * @param textPredicates the full text predicates
	 * @param disjunctive combine the predicates with 'or'?
	 * @param user the name of the user to contribute or last update the
	 *            names
	 * @param txKingdom taxonomic epithet between kingdom (inclusive) and phylum (exclusive)
	 * @param txPhylum taxonomic epithet between phylum (inclusive) and class (exclusive)
	 * @param txClass taxonomic epithet between class (inclusive) and order (exclusive)
	 * @param txOrder taxonomic epithet between order (inclusive) and family (exclusive)
	 * @param txFamily taxonomic epithet between family (inclusive) and genus (exclusive)
	 * @param txGenus taxonomic epithet between genus (inclusive) and species (exclusive)
	 * @param txSpecies taxonomic epithet at species level or below
	 * @param txRank rank of taxonomic names to find
	 * @param concise obtain a concise result, i.e., without parses?
	 * @param limit the maximum number of names to include in the result (0 means no limit)
	 * @return an iterator over the references matching the query
	 */
	public abstract PooledStringIterator findNames(String[] textPredicates, boolean disjunctive, String user, String txKingdom, String txPhylum, String txClass, String txOrder, String txFamily, String txGenus, String txSpecies, String txRank, boolean concise, int limit);
	
	/**
	 * Upload a taxon name.
	 * @param taxName the taxon name to upload
	 * @param user the name of the user contributing the taxon name
	 * @return the uploaded taxon name
	 * @throws IOException
	 */
	public abstract PooledString updateName(TaxonomicName taxName, String user) throws IOException;
	
	/**
	 * Upload a series of taxon names.
	 * @param taxNames an array holding the taxon names to upload
	 * @param user the name of the user contributing the taxon names
	 * @return an iterator over the uploaded taxon names
	 */
	public abstract PooledStringIterator updateNames(TaxonomicName[] taxNames, String user);
}