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

import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

import de.uka.ipd.idaho.gamta.Annotation;
import de.uka.ipd.idaho.gamta.AnnotationUtils;
import de.uka.ipd.idaho.gamta.MutableAnnotation;
import de.uka.ipd.idaho.onn.stringPool.StringPoolConstants;
import de.uka.ipd.idaho.plugins.bibRefs.BibRefConstants;
import de.uka.ipd.idaho.plugins.taxonomicNames.TaxonomicNameConstants;

/**
 * Constant bearer for Taxonomic Name Usage Bank
 * 
 * @author sautter
 */
public interface TnuBankConstants extends StringPoolConstants, TaxonomicNameConstants, BibRefConstants {
	
	public static final String TNU_XML_NAMESPACE = null;//"http://idaho.ipd.uka.de/sp/schema";
	public static final String TNU_XML_NAMESPACE_ATTRIBUTE = ((TNU_XML_NAMESPACE == null) ? "" : (" xmlns:tnu=\"" + TNU_XML_NAMESPACE + "\""));
	public static final String TNU_XML_NAMESPACE_PREFIX = ((TNU_XML_NAMESPACE == null) ? "" : "tnu:");
	
	public static final String NAME_USAGE_SET_NODE_TYPE = (TNU_XML_NAMESPACE_PREFIX + "taxonNameUsageSet");
	public static final String NAME_USAGE_NODE_TYPE = (TNU_XML_NAMESPACE_PREFIX + "taxonNameUsage");
	public static final String NAME_USAGE_PLAIN_NODE_TYPE = (TNU_XML_NAMESPACE_PREFIX + "taxonNameUsageString");
	public static final String NAME_USAGE_PARSED_NODE_TYPE = (TNU_XML_NAMESPACE_PREFIX + "taxonNameUsageParsed");
	
	public static final String TAXON_NAME_STRING_PARAMETER = "taxNameString";
	public static final String TAXON_NAME_PARAMETER = "taxName";
	public static final String TAXON_NAME_RANK_PARAMETER = "taxNameRank";
	public static final String TAXON_NAME_USAGE_TYPE_PARAMETER = "taxNameUsageType";
	public static final String BIB_REF_PARAMETER = "bibRef";
	public static final String BIB_REF_AUTHOR_PARAMETER = "bibRefAuthor";
	public static final String BIB_REF_YEAR_PARAMETER = "bibRefYear";
	public static final String PAGE_NUMBER_PARAMETER = "pageNumber";
	public static final String EXPAND_PARAMETER = "expand";
	
	public static final String GENERIC_NAME_USAGE_TYPE = "generic usage";
	public static final String NOMENCLATURE_NAME_USAGE_TYPE = "nomenclature usage";
	public static final String OTHER_NAME_USAGE_TYPE = "other usage";
	
	public static final String NAME_STRING_NAME_USAGE_TYPE = "name string";
	public static final String ORIGINAL_DESCRIPTION_NAME_USAGE_TYPE = "original description";
	public static final String REDESCRIPTION_USAGE_TYPE = "redescription";
	public static final String DATA_DESCRIPTION_NAME_USAGE_TYPE = "data description";
	public static final String COMPILED_DESCRIPTION_NAME_USAGE_TYPE = "compiled description";
	public static final String CITATION_NAME_USAGE_TYPE = "citation";
	public static final String KEY_ROOT_NAME_USAGE_TYPE = "key root";
//	public static final String KEY_LEAF_NAME_USAGE_TYPE = "key leaf";
	public static final String SENIOR_SYNONYMIZATION_NAME_USAGE_TYPE = "senior synonymization";
	public static final String JUNIOR_SYNONYMIZATION_USAGE_TYPE = "junior synonymization";
	public static final String NEW_COMBINATION_USAGE_TYPE = "new combination";
	
	public static final Map nameUsageTypes = Collections.unmodifiableMap(new TreeMap(String.CASE_INSENSITIVE_ORDER) {
		{
			this.put("GU", GENERIC_NAME_USAGE_TYPE);
			this.put("NU", NOMENCLATURE_NAME_USAGE_TYPE);
			this.put("OU", OTHER_NAME_USAGE_TYPE);
			this.put(GENERIC_NAME_USAGE_TYPE, GENERIC_NAME_USAGE_TYPE);
			this.put(NOMENCLATURE_NAME_USAGE_TYPE, NOMENCLATURE_NAME_USAGE_TYPE);
			this.put(OTHER_NAME_USAGE_TYPE, OTHER_NAME_USAGE_TYPE);
		}
	});
	public static final Map nameUsageSubTypes = Collections.unmodifiableMap(new TreeMap(String.CASE_INSENSITIVE_ORDER) {
		{
			this.put("NS", NAME_STRING_NAME_USAGE_TYPE);
			this.put("OD", ORIGINAL_DESCRIPTION_NAME_USAGE_TYPE);
			this.put("RD", REDESCRIPTION_USAGE_TYPE);
			this.put("DD", DATA_DESCRIPTION_NAME_USAGE_TYPE);
			this.put("CD", COMPILED_DESCRIPTION_NAME_USAGE_TYPE);
			this.put("CI", CITATION_NAME_USAGE_TYPE);
			this.put("KR", KEY_ROOT_NAME_USAGE_TYPE);
//			this.put("KL", KEY_LEAF_NAME_USAGE_TYPE);
			this.put("SS", SENIOR_SYNONYMIZATION_NAME_USAGE_TYPE);
			this.put("JS", JUNIOR_SYNONYMIZATION_USAGE_TYPE);
			this.put("NC", NEW_COMBINATION_USAGE_TYPE);
			this.put(NAME_STRING_NAME_USAGE_TYPE, NAME_STRING_NAME_USAGE_TYPE);
			this.put(ORIGINAL_DESCRIPTION_NAME_USAGE_TYPE, ORIGINAL_DESCRIPTION_NAME_USAGE_TYPE);
			this.put(REDESCRIPTION_USAGE_TYPE, REDESCRIPTION_USAGE_TYPE);
			this.put(DATA_DESCRIPTION_NAME_USAGE_TYPE, DATA_DESCRIPTION_NAME_USAGE_TYPE);
			this.put(COMPILED_DESCRIPTION_NAME_USAGE_TYPE, COMPILED_DESCRIPTION_NAME_USAGE_TYPE);
			this.put(CITATION_NAME_USAGE_TYPE, CITATION_NAME_USAGE_TYPE);
			this.put(KEY_ROOT_NAME_USAGE_TYPE, KEY_ROOT_NAME_USAGE_TYPE);
//			this.put(KEY_LEAF_NAME_USAGE_TYPE, KEY_LEAF_NAME_USAGE_TYPE);
			this.put(SENIOR_SYNONYMIZATION_NAME_USAGE_TYPE, SENIOR_SYNONYMIZATION_NAME_USAGE_TYPE);
			this.put(JUNIOR_SYNONYMIZATION_USAGE_TYPE, JUNIOR_SYNONYMIZATION_USAGE_TYPE);
			this.put(NEW_COMBINATION_USAGE_TYPE, NEW_COMBINATION_USAGE_TYPE);
		}
	});
	public static final Map nameUsageTypeCodes = Collections.unmodifiableMap(new TreeMap(String.CASE_INSENSITIVE_ORDER) {
		{
			this.put("GU", "GU");
			this.put("NU", "NU");
			this.put("OU", "OU");
			this.put(GENERIC_NAME_USAGE_TYPE, "GU");
			this.put(NOMENCLATURE_NAME_USAGE_TYPE, "NU");
			this.put(OTHER_NAME_USAGE_TYPE, "OU");
		}
	});
	public static final Map nameUsageSubTypeCodes = Collections.unmodifiableMap(new TreeMap(String.CASE_INSENSITIVE_ORDER) {
		{
			this.put("NS", "NS");
			this.put("OD", "OD");
			this.put("RD", "RD");
			this.put("DD", "DD");
			this.put("CD", "CD");
			this.put("CI", "CI");
			this.put("KR", "KR");
//			this.put("KL", "KL");
			this.put("SS", "SS");
			this.put("JS", "JS");
			this.put("NC", "NC");
			this.put(NAME_STRING_NAME_USAGE_TYPE, "NS");
			this.put(ORIGINAL_DESCRIPTION_NAME_USAGE_TYPE, "OD");
			this.put(REDESCRIPTION_USAGE_TYPE, "RD");
			this.put(DATA_DESCRIPTION_NAME_USAGE_TYPE, "DD");
			this.put(COMPILED_DESCRIPTION_NAME_USAGE_TYPE, "CD");
			this.put(CITATION_NAME_USAGE_TYPE, "CI");
			this.put(KEY_ROOT_NAME_USAGE_TYPE, "KR");
//			this.put(KEY_LEAF_NAME_USAGE_TYPE, "KL");
			this.put(SENIOR_SYNONYMIZATION_NAME_USAGE_TYPE, "SS");
			this.put(JUNIOR_SYNONYMIZATION_USAGE_TYPE, "JS");
			this.put(NEW_COMBINATION_USAGE_TYPE, "NC");
		}
	});
	
	/**
	 * Object representation of a taxon name usage
	 * 
	 * @author sautter
	 */
	public static class TaxonNameUsage {
		public final String nameStringId;
		public final String taxonNameId;
		public final String nameUsageType;
		public final String nameUsageTypeCode;
		public final String nameUsageSubType;
		public final String nameUsageSubTypeCode;
		public final String bibRefId;
		public final int pageNumber;
		TaxonNameUsage(String nameStringId, String taxonNameId, String nameUsageType, String nameUsageSubType, String bibRefId, int pageNumber) {
			this.nameStringId = nameStringId;
			this.taxonNameId = taxonNameId;
			this.nameUsageType = ((String) nameUsageTypes.get(nameUsageType));
			if (this.nameUsageType == null)
				throw new IllegalArgumentException("'" + nameUsageType + "' is not a valid taxon name usage type");
			this.nameUsageTypeCode = ((String) nameUsageTypeCodes.get(this.nameUsageType));
			if (this.nameUsageTypeCode == null)
				throw new IllegalArgumentException("'" + nameUsageType + "' is not a valid taxon name usage type");
			if (NOMENCLATURE_NAME_USAGE_TYPE.equals(this.nameUsageType) && (nameUsageSubType != null)) {
				this.nameUsageSubType = ((this.taxonNameId == null) ? NAME_STRING_NAME_USAGE_TYPE : nameUsageSubType);
				this.nameUsageSubTypeCode = ((String) nameUsageSubTypeCodes.get(this.nameUsageSubType));
				if (this.nameUsageSubTypeCode == null)
					throw new IllegalArgumentException("'" + nameUsageSubType + "' is not a valid taxon name usage sub type");
			}
			else {
				this.nameUsageSubType = null;
				this.nameUsageSubTypeCode = null;
			}
			this.bibRefId = bibRefId;
			this.pageNumber = pageNumber;
			if (pageNumber < 1)
				throw new IllegalArgumentException("'" + pageNumber + "' is not a valid page number");
		}
		
		public String toPlainString() {
			StringBuffer plainString = new StringBuffer();
			plainString.append(this.nameStringId);
			if (this.taxonNameId != null)
				plainString.append("(" + this.taxonNameId + ")");
			plainString.append("-" + this.nameUsageTypeCode);
			if (this.nameUsageSubTypeCode != null)
				plainString.append("/" + this.nameUsageSubTypeCode);
			plainString.append("@" + this.bibRefId);
			plainString.append(":" + this.pageNumber);
			return plainString.toString();
		}
		
		public String toParsedString() {
			StringBuffer parsedString = new StringBuffer("<taxonNameUsage xmlns:dwc=\"http://digir.net/schema/conceptual/darwin/2003/1.0\">");
			parsedString.append("<dwc:scientificNameID>" + AnnotationUtils.escapeForXml(this.nameStringId) + "</dwc:scientificNameID>");
			if (this.taxonNameId != null)
				parsedString.append("<dwc:taxonID>" + AnnotationUtils.escapeForXml(this.taxonNameId) + "</dwc:taxonID>");
			parsedString.append("<nameUsageType>" + this.nameUsageTypeCode + "</nameUsageType>");
			if (this.nameUsageSubTypeCode != null)
				parsedString.append("<nameUsageSubType>" + this.nameUsageSubTypeCode + "</nameUsageSubType>");
			parsedString.append("<bibRefId>" + AnnotationUtils.escapeForXml(this.bibRefId) + "</bibRefId>");
			parsedString.append("<pageNumber>" + this.pageNumber + "</pageNumber>");
			parsedString.append("</taxonNameUsage>");
			return parsedString.toString();
		}
		
		public static TaxonNameUsage parseTaxonNameUsage(String tnuString) {
			String nameStringId = tnuString;
			if (nameStringId.indexOf('(') == -1)
				nameStringId = nameStringId.substring(0, nameStringId.indexOf('-'));
			else nameStringId = nameStringId.substring(0, nameStringId.indexOf('('));
			tnuString = tnuString.substring(nameStringId.length() + "(".length());
			String taxonNameId = null;
			if (tnuString.indexOf(")-") != -1) {
				taxonNameId = tnuString;
				taxonNameId = taxonNameId.substring(0, taxonNameId.indexOf(")-"));
				tnuString = tnuString.substring(taxonNameId.length() + ")-".length());
			}
			String nameUsageType = tnuString;
			String nameUsageSubType;
			if (nameUsageType.indexOf('/') == -1) {
				nameUsageType = nameUsageType.substring(0, nameUsageType.indexOf('@'));
				nameUsageSubType = null;
				tnuString = tnuString.substring(nameUsageType.length() + "@".length());
			}
			else {
				nameUsageType = nameUsageType.substring(0, nameUsageType.indexOf('/'));
				tnuString = tnuString.substring(nameUsageType.length() + "/".length());
				nameUsageSubType = tnuString;
				nameUsageSubType = nameUsageSubType.substring(0, nameUsageSubType.indexOf('@'));
				tnuString = tnuString.substring(nameUsageSubType.length() + "@".length());
			}
			String bibRefId = tnuString;
			bibRefId = bibRefId.substring(0, bibRefId.indexOf(':'));
			tnuString = tnuString.substring(bibRefId.length() + ":".length());
			String pageNumber = tnuString;
			return new TaxonNameUsage(nameStringId, taxonNameId, nameUsageType, nameUsageSubType, bibRefId, Integer.parseInt(pageNumber));
		}
		
		public static TaxonNameUsage parseTaxonNameUsage(MutableAnnotation tnu) {
			Annotation[] nameStringIds = tnu.getAnnotations("dwc:scientificNameID");
			if (nameStringIds.length == 0)
				return null;
			Annotation[] taxonNameIds = tnu.getAnnotations("dwc:taxonID");
			Annotation[] nameUsageTypes = tnu.getAnnotations("nameUsageType");
			if (nameUsageTypes.length == 0)
				return null;
			Annotation[] nameUsageSubTypes = tnu.getAnnotations("nameUsageSubType");
			Annotation[] bibRefIds = tnu.getAnnotations("bibRefId");
			if (bibRefIds.length == 0)
				return null;
			Annotation[] pageNumbers = tnu.getAnnotations("pageNumber");
			if (pageNumbers.length == 0)
				return null;
			return new TaxonNameUsage(nameStringIds[0].getValue(), ((taxonNameIds.length == 0) ? null : taxonNameIds[0].getValue()), nameUsageTypes[0].getValue(), ((nameUsageSubTypes.length == 0) ? null : nameUsageSubTypes[0].getValue()), bibRefIds[0].getValue(), Integer.parseInt(pageNumbers[0].getValue()));
		}
		
		public static boolean isValid(String tnuString) {
			return (tnuString.matches(
						"[0-9a-fA-F]{32}" +
						"\\([0-9a-fA-F]{32}\\)" +
						"\\-(GU|NU|OU)" +
						"(\\/(OD|RD|DD|CD|CI|KR|KL|SS|JS|NC))?" +
						"\\@[0-9a-fA-F]{32}" +
						"\\:[1-9][0-9]{0,4}"
					) || tnuString.matches(
						"[0-9a-fA-F]{32}" +
						"\\-NU" +
						"(\\/NS)?" +
						"\\@[0-9a-fA-F]{32}" +
						"\\:[1-9][0-9]{0,4}"
					));
		}
	}
	
	/* Usage types:
	 * - NU: generic name usage, e.g. in a discussion
	 * 
	 * - OD: original description
	 * - RD: redescription
	 * - DD: data description, e.g. in a checklist or inventory
	 * - CD: compiled description, e.g. in a revision or flora
	 * - CI: citation, i.e., use as a non-taxon in the header of an SD
	 * 
	 * - KR: use as root node of a key, i.e., key to children
	 * - KL: leaf node in a key, i.e., key outcome
	 * 
	 * - SS: part of synonymization as senior synonym
	 * - SJ: part of synonymization as junior synonym
	 * 
	 * - NC: new combination (used for original combination, with new combination being an OD)
	 */
	
	/* String style options:
	 * - <nameStringId>(<taxonNameId>).<usageType>@<bibRefId>:<pageNumber>
	 *   + concise, no redundancy
	 *   - hard (virtually impossible) to debug for lack of human readable parts
	 *   - requires joins for even most basic search
	 * - <nameString> {<nameStringId>} (<taxonName> {<taxonNameId>}), <usageType> @ <author> (<year>) <title> {<bibRefId>}:<pageNumber>
	 *   - redundant data
	 *   + basic search works without additional XyzBanks
	 *   + able to resolve most treatment citations right away
	 *   - IDs intermixed with plain text
	 * - <nameString> {<nameStringId>} (<taxonName> {<taxonNameId>}), <usageType> @ <bibRefString> {<bibRefId>}:<pageNumber>
	 *   - lots of redundant data
	 *   + any search works without additional XyzBanks
	 *   + able to resolve treatment citations right away
	 *   - many possible representations of same usage due to different representations of same reference
	 * - <nameStringId>(<taxonNameId>).<usageType>@<bibRefId>:<pageNumber> == <nameString> (<taxonName>), <usageType> in <author> (<year>) <title>:<pageNumber>
	 *   - redundant data
	 *   + basic search works without additional XyzBanks
	 *   + able to resolve most treatment citations right away
	 */
}