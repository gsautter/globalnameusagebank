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
import java.util.Properties;

import de.uka.ipd.idaho.onn.stringPool.StringPoolClient;
import de.uka.ipd.idaho.plugins.bibRefs.BibRefUtils.RefData;
import de.uka.ipd.idaho.plugins.taxonomicNames.TaxonomicNameUtils.TaxonomicName;

/**
 * TnuBank specific client object, adding detail search for taxonomic name
 * usages.
 * 
 * @author sautter
 */
public interface TnuBankClient extends StringPoolClient {
	
	/**
	 * Retrieve a name usage by its ID. This method is also good for resolving
	 * IDs.
	 * @param nameUsageId the ID to resolve
	 * @param expand expand parsed name usages to include human readable data?
	 * @return the name usage with the specified ID
	 * @throws IOException
	 */
	public abstract PooledString getNameUsage(String nameUsageId, boolean expand) throws IOException;
	
	/**
	 * Retrieve strings by their IDs. This method is also good for resolving
	 * IDs.
	 * @param nameUsageIds an array holding the IDs to resolve
	 * @param expand expand parsed name usages to include human readable data?
	 * @return an iterator over the name usages with the specified IDs
	 */
	public abstract PooledStringIterator getNameUsages(String[] nameUsageIds, boolean expand);
	
	/**
	 * Search for taxonomic name usages, using both full text and detail
	 * predicates.
	 * @param user the name of the user to contribute or last update the name
	 *            usages
	 * @param taxNameString query against verbatim taxon name
	 * @param taxName query against fully qualified taxon name
	 * @param taxNameRank query against the rank of taxon names
	 * @param nameUsageType type of the name usage (also queries sub type)
	 * @param bibRef query against the bibliographic reference
	 * @param pageNumber page number of the name usage
	 * @param limit the maximum number of names to include in the result (0 means no limit)
	 * @param expand expand parsed name usages to include human readable data?
	 * @return an iterator over the references matching the query
	 */
	public abstract PooledStringIterator findNameUsages(String user, String taxNameString, String taxName, String taxNameRank, String nameUsageType, String bibRef, int pageNumber, int limit, boolean expand);
	
	/**
	 * Search for taxonomic name usages, using both full text and detail
	 * predicates.
	 * @param user the name of the user to contribute or last update the name
	 *            usages
	 * @param taxNameString query against verbatim taxon name
	 * @param taxName query against fully qualified taxon name
	 * @param taxNameRank query against the rank of taxon names
	 * @param nameUsageType type of the name usage (also queries sub type)
	 * @param bibRef query against the bibliographic reference
	 * @param pageNumber page number of the name usage
	 * @param concise obtain a concise result, i.e., without parses?
	 * @param limit the maximum number of names to include in the result (0 means no limit)
	 * @param expand expand parsed name usages to include human readable data?
	 * @return an iterator over the references matching the query
	 */
	public abstract PooledStringIterator findNameUsages(String user, String taxNameString, String taxName, String taxNameRank, String nameUsageType, String bibRef, int pageNumber, boolean concise, int limit, boolean expand);
	
	/**
	 * Search for taxonomic name usages, using both full text and detail
	 * predicates.
	 * @param user the name of the user to contribute or last update the name
	 *            usages
	 * @param taxNameString query against verbatim taxon name
	 * @param taxNameEpithets a mapping of primary rank names to epithet specific queries 
	 * @param nameUsageType type of the name usage (also queries sub type)
	 * @param author query against the author of the bibliographic reference
	 * @param year query against the year of the bibliographic reference
	 * @param pageNumber page number of the name usage
	 * @param limit the maximum number of names to include in the result (0 means no limit)
	 * @param expand expand parsed name usages to include human readable data?
	 * @return an iterator over the references matching the query
	 */
	public abstract PooledStringIterator findNameUsages(String user, String taxNameString, Properties taxNameEpithets, String nameUsageType, String author, int year, int pageNumber, int limit, boolean expand);
	
	/**
	 * Search for taxonomic name usages, using both full text and detail
	 * predicates.
	 * @param user the name of the user to contribute or last update the name
	 *            usages
	 * @param taxNameString query against verbatim taxon name
	 * @param taxNameEpithets a mapping of primary rank names to epithet specific queries 
	 * @param nameUsageType type of the name usage (also queries sub type)
	 * @param author query against the author of the bibliographic reference
	 * @param year query against the year of the bibliographic reference
	 * @param pageNumber page number of the name usage
	 * @param concise obtain a concise result, i.e., without parses?
	 * @param limit the maximum number of names to include in the result (0 means no limit)
	 * @param expand expand parsed name usages to include human readable data?
	 * @return an iterator over the references matching the query
	 */
	public abstract PooledStringIterator findNameUsages(String user, String taxNameString, Properties taxNameEpithets, String nameUsageType, String author, int year, int pageNumber, boolean concise, int limit, boolean expand);
	
	/**
	 * Upload a taxon name usage. It is the responsibility of client code to
	 * ensure that the argument identifiers (a) match and (b) resolve properly
	 * at the respective services. If the fully qualified taxon name is not
	 * available, the respective ID may be null. The name usage type then
	 * defaults to 'name string usage'.
	 * @param nameStringId the BinoBank ID of the verbatim name string
	 * @param taxonNameId the TxnBank ID of the fully qualified taxon name
	 * @param nameUsageType the name usage type
	 * @param nameUsageSubType the name usage sub type, providing further detail
	 * @param bibRefId the RefBank ID of the bibliographic reference
	 * @param pageNumber the page number of the usage
	 * @param user the name of the user contributing the name usage
	 * @return the uploaded taxon name usage
	 * @throws IOException
	 */
	public abstract PooledString updateNameUsage(String nameStringId, String taxonNameId, String nameUsageType, String nameUsageSubType, String bibRefId, int pageNumber, String user) throws IOException;
	
	/**
	 * Upload a taxon name usage.
	 * @param nameString the verbatim name string
	 * @param taxonName the fully qualified taxon name
	 * @param nameUsageType the name usage type
	 * @param nameUsageSubType the name usage sub type, providing further detail
	 * @param bibRef the bibliographic reference
	 * @param pageNumber the page number of the usage
	 * @param user the name of the user contributing the name usage
	 * @return the uploaded taxon name usage
	 * @throws IOException
	 */
	public abstract PooledString updateNameUsage(String nameString, TaxonomicName taxonName, String nameUsageType, String nameUsageSubType, RefData bibRef, int pageNumber, String user) throws IOException;
}