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

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.Writer;
import java.sql.SQLException;
import java.util.Date;
import java.util.LinkedList;
import java.util.Properties;
import java.util.UUID;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.Transformer;

import com.fasterxml.uuid.Generators;
import com.fasterxml.uuid.impl.NameBasedGenerator;

import de.uka.ipd.idaho.binoBank.BinoBankServlet;
import de.uka.ipd.idaho.easyIO.EasyIO;
import de.uka.ipd.idaho.easyIO.IoProvider;
import de.uka.ipd.idaho.easyIO.SqlQueryResult;
import de.uka.ipd.idaho.easyIO.sql.TableDefinition;
import de.uka.ipd.idaho.easyIO.web.WebAppHost;
import de.uka.ipd.idaho.gamta.AnnotationUtils;
import de.uka.ipd.idaho.gamta.Gamta;
import de.uka.ipd.idaho.gamta.MutableAnnotation;
import de.uka.ipd.idaho.gamta.util.SgmlDocumentReader;
import de.uka.ipd.idaho.htmlXmlUtil.accessories.XsltUtils;
import de.uka.ipd.idaho.onn.stringPool.StringPoolRestClient;
import de.uka.ipd.idaho.onn.stringPool.StringPoolServlet;
import de.uka.ipd.idaho.plugins.bibRefs.BibRefUtils.RefData;
import de.uka.ipd.idaho.plugins.taxonomicNames.TaxonomicNameUtils.TaxonomicName;
import de.uka.ipd.idaho.plugins.taxonomicNames.TaxonomicRankSystem;
import de.uka.ipd.idaho.plugins.taxonomicNames.TaxonomicRankSystem.RankGroup;
import de.uka.ipd.idaho.refBank.RefBankServlet;
import de.uka.ipd.idaho.txnBank.TxnBankServlet;

/**
 * @author sautter
 */
public class TnuBankServlet extends StringPoolServlet implements TnuBankClient, TnuBankConstants {
	
	/* (non-Javadoc)
	 * @see de.uka.ipd.idaho.onn.stringPool.StringPoolServlet#getExternalDataName()
	 */
	protected String getExternalDataName() {
		return "TaxonNameUsage";
	}
	
	/* (non-Javadoc)
	 * @see de.uka.ipd.idaho.onn.stringPool.StringPoolServlet#getNamespaceAttribute()
	 */
	public String getNamespaceAttribute() {
		return TNU_XML_NAMESPACE_ATTRIBUTE;
	}
	
	/* (non-Javadoc)
	 * @see de.uka.ipd.idaho.onn.stringPool.StringPoolServlet#getStringNodeType()
	 */
	public String getStringNodeType() {
		return NAME_USAGE_NODE_TYPE;
	}
	
	/* (non-Javadoc)
	 * @see de.uka.ipd.idaho.onn.stringPool.StringPoolServlet#getStringParsedNodeType()
	 */
	public String getStringParsedNodeType() {
		return NAME_USAGE_PARSED_NODE_TYPE;
	}
	
	/* (non-Javadoc)
	 * @see de.uka.ipd.idaho.onn.stringPool.StringPoolServlet#getStringPlainNodeType()
	 */
	public String getStringPlainNodeType() {
		return NAME_USAGE_PLAIN_NODE_TYPE;
	}
	
	/* (non-Javadoc)
	 * @see de.uka.ipd.idaho.onn.stringPool.StringPoolServlet#getStringSetNodeType()
	 */
	public String getStringSetNodeType() {
		return NAME_USAGE_SET_NODE_TYPE;
	}
	
	private static final String NAME_STRING_ID_COLUMN_NAME = "nameStringId";
	private static final String NAME_STRING_ID_HASH_COLUMN_NAME = "nameStringIdHash";
	private static final String TAXON_NAME_ID_COLUMN_NAME = "taxonNameId";
	private static final String TAXON_NAME_ID_HASH_COLUMN_NAME = "taxonNameIdHash";
	private static final String NAME_USAGE_TYPE_COLUMN_NAME = "nameUsageType";
	private static final String NAME_USAGE_SUB_TYPE_COLUMN_NAME = "nameUsageSubType";
	private static final String BIB_REF_ID_COLUMN_NAME = "bibRefId";
	private static final String BIB_REF_ID_HASH_COLUMN_NAME = "bibRefIdHash";
	private static final String PAGE_NUMBER_COLUMN_NAME = "pageNumber";
	private static final int ID_COLUMN_LENGTH = 32;
	
	/* (non-Javadoc)
	 * @see de.uka.ipd.idaho.onn.stringPool.StringPoolServlet#extendIndexTableDefinition(de.uka.ipd.idaho.easyIO.sql.TableDefinition)
	 */
	protected boolean extendIndexTableDefinition(TableDefinition itd) {
		itd.addColumn(NAME_STRING_ID_COLUMN_NAME, TableDefinition.VARCHAR_DATATYPE, ID_COLUMN_LENGTH);
		itd.addColumn(NAME_STRING_ID_HASH_COLUMN_NAME, TableDefinition.INT_DATATYPE, 0);
		itd.addColumn(TAXON_NAME_ID_COLUMN_NAME, TableDefinition.VARCHAR_DATATYPE, ID_COLUMN_LENGTH);
		itd.addColumn(TAXON_NAME_ID_HASH_COLUMN_NAME, TableDefinition.INT_DATATYPE, 0);
		itd.addColumn(NAME_USAGE_TYPE_COLUMN_NAME, TableDefinition.CHAR_DATATYPE, 2);
		itd.addColumn(NAME_USAGE_SUB_TYPE_COLUMN_NAME, TableDefinition.CHAR_DATATYPE, 2);
		itd.addColumn(BIB_REF_ID_COLUMN_NAME, TableDefinition.VARCHAR_DATATYPE, ID_COLUMN_LENGTH);
		itd.addColumn(BIB_REF_ID_HASH_COLUMN_NAME, TableDefinition.INT_DATATYPE, 0);
		itd.addColumn(PAGE_NUMBER_COLUMN_NAME, TableDefinition.INT_DATATYPE, 0);
		return true;
	}
	
	private RefBankServlet rbk;
	private BinoBankServlet bbk;
	private TxnBankServlet txn;
	
	private IoProvider io;
	
	private String[] primaryRankNames;
	
	/* (non-Javadoc)
	 * @see de.uka.ipd.idaho.onn.stringPool.StringPoolServlet#doInit()
	 */
	protected void doInit() throws ServletException {
		super.doInit();
		
		//	connect to RefBank, BinoBank, and TxnBank
		this.rbk = ((RefBankServlet) this.getServlet(this.getSetting("refBankNodeName"), this.getSetting("refBankNodeUrl")));
		if (this.rbk == null)
			throw new ServletException("TaxonNameUsageBank requires a RefBank servlet to be present.");
		this.bbk = ((BinoBankServlet) this.getServlet(this.getSetting("binoBankNodeName"), this.getSetting("binoBankNodeUrl")));
		if (this.bbk == null)
			throw new ServletException("TaxonNameUsageBank requires a BionomialBank servlet to be present.");
		this.txn = ((TxnBankServlet) this.getServlet(this.getSetting("txnBankNodeName"), this.getSetting("txnBankNodeUrl")));
		if (this.txn == null)
			throw new ServletException("TaxonNameUsageBank requires a TaxonNameBank servlet to be present.");
		
		// get and check database connection
		this.io = WebAppHost.getInstance(this.getServletContext()).getIoProvider();
		if (!this.io.isJdbcAvailable())
			throw new ServletException("ParsedStringPool: Cannot work without database access.");
		
		//	get primary ranks
		TaxonomicRankSystem rankSystem = TaxonomicRankSystem.getRankSystem(null);
		RankGroup[] rankGroups = rankSystem.getRankGroups();
		this.primaryRankNames = new String[rankGroups.length];
		for (int g = 0; g < rankGroups.length; g++)
			this.primaryRankNames[g] = rankGroups[g].name;
	}
	
	private Servlet getServlet(String name, String url) {
		
		//	check servlet registry
		Servlet s = this.webAppHost.getServlet(name);
		
		//	servlet already there
		if (s != null)
			return s;
		
		//	send bogus resolver query to force servlet to be loaded
		StringPoolRestClient sprc = new StringPoolRestClient(url);
		String[] nonExistingIds = {Gamta.getAnnotationID()};
		sprc.getStrings(nonExistingIds);
		
		//	get servlet from registry
		return this.webAppHost.getServlet(name);
	}
	
	/* (non-Javadoc)
	 * @see de.uka.ipd.idaho.onn.stringPool.StringPoolServlet#getXmlNamespaceUriBindings()
	 */
	protected String getXmlNamespaceUriBindings() {
		return "xmlns:mods=\"http://www.loc.gov/mods/v3\" xmlns:dwc=\"http://digir.net/schema/conceptual/darwin/2003/1.0\" xmlns:dwcranks=\"http://rs.tdwg.org/UBIF/2006/Schema/1.1\"";
	}
	
	/* (non-Javadoc)
	 * @see de.uka.ipd.idaho.onn.stringPool.StringPoolServlet#doGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String action = request.getPathInfo();
		if (action == null)
			action = request.getParameter(ACTION_PARAMETER);
		else {
			while (action.startsWith("/"))
				action = action.substring(1);
			if (action.indexOf('/') != -1)
				action = action.substring(0, action.indexOf('/'));
		}
		
		//	ID-based request for name usages, with expansion required
		if (GET_ACTION_NAME.equals(action) && (request.getParameter(EXPAND_PARAMETER) != null))
			this.doGetNameUsages(request, response);
		
		//	search for name usages
		else if (FIND_ACTION_NAME.equals(action))
			this.doFindNameUsages(request, response);
		
		//	let super class handle anything else
		else super.doGet(request, response);
	}
	
	private void doGetNameUsages(HttpServletRequest request, HttpServletResponse response) throws IOException {
		String[] ids = request.getParameterValues(ID_PARAMETER);
		if ((ids == null) || (ids.length == 0)) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Taxon Name Usage ID missing.");
			return;
		}
		
		PooledStringIterator tnuIt = this.getStrings(ids);
		
		String format = request.getParameter(FORMAT_PARAMETER);
		Transformer formatter = null;
		if (format != null) try {
			formatter = XsltUtils.getTransformer(new File(this.dataFolder, format), !"force".equals(request.getParameter("formatCache")));
		}
		catch (IOException ioe) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, ("Invalid format: " + format));
			return;
		}
		response.setCharacterEncoding(ENCODING);
		response.setContentType("text/xml");
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(response.getOutputStream(), ENCODING));
		if (formatter != null)
			bw = new BufferedWriter(XsltUtils.wrap(bw, formatter));
		this.sendNameUsages(tnuIt, bw, true);
		bw.flush();
		bw.close();
	}
	
	private void doFindNameUsages(HttpServletRequest request, HttpServletResponse response) throws IOException {
		String[] fullTextQueryPredicates = request.getParameterValues(QUERY_PARAMETER);
		boolean disjunctive = OR_COMBINE.equals(request.getParameter(COMBINE_PARAMETER));
		String taxNameString = request.getParameter(TAXON_NAME_STRING_PARAMETER);
		String taxName = request.getParameter(TAXON_NAME_PARAMETER);
		Properties taxNameEpithets = null;
		for (int r = 0; r < this.primaryRankNames.length; r++) {
			String rankEpithet = request.getParameter(this.primaryRankNames[r]);
			if (rankEpithet != null) {
				if (taxNameEpithets == null)
					taxNameEpithets = new Properties();
				taxNameEpithets.setProperty(this.primaryRankNames[r], rankEpithet);
			}
		}
		String taxNameRank = request.getParameter(TAXON_NAME_RANK_PARAMETER);
		String nameUsageType = request.getParameter(TAXON_NAME_USAGE_TYPE_PARAMETER);
		String bibRef = request.getParameter(BIB_REF_PARAMETER);
		String author = request.getParameter(BIB_REF_AUTHOR_PARAMETER);
		String yearString = request.getParameter(BIB_REF_YEAR_PARAMETER);
		int year = -1;
		if (yearString != null) try {
			year = Integer.parseInt(yearString);
		} catch (NumberFormatException nfe) {}
		String pageNumberString = request.getParameter(PAGE_NUMBER_PARAMETER);
		int pageNumber = -1;
		if (pageNumberString != null) try {
			pageNumber = Integer.parseInt(pageNumberString);
		} catch (NumberFormatException nfe) {}
		int limit = 0;
		String limitString = request.getParameter(LIMIT_PARAMETER);
		if (limitString != null) try {
			limit = Integer.parseInt(limitString);
		} catch (NumberFormatException nfe) {}
		
		if (((fullTextQueryPredicates == null) || (fullTextQueryPredicates.length == 0)) && (taxNameString == null) && (taxName == null) && (taxNameEpithets == null) && (taxNameRank == null) && (bibRef == null) && (author == null)) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Empty query.");
			return;
		}
		
		PooledStringIterator tnuIt = this.findNameUsagesInternal(fullTextQueryPredicates, disjunctive, request.getParameter(USER_PARAMETER), CONCISE_FORMAT.equals(request.getParameter(FORMAT_PARAMETER)), limit, SELF_CANONICAL_ONLY_PARAMETER.equals(request.getParameter(SELF_CANONICAL_ONLY_PARAMETER)), taxNameString, taxName, taxNameEpithets, taxNameRank, nameUsageType, bibRef, author, year, pageNumber);
		if ((request.getParameter(EXPAND_PARAMETER) != null) && (tnuIt.getException() == null))
			tnuIt = new ExpandedParsePooledStringIterator(tnuIt);
		
		String format = request.getParameter(FORMAT_PARAMETER);
		Transformer formatter = null;
		if ((format != null) && !CONCISE_FORMAT.equals(format)) try {
			formatter = XsltUtils.getTransformer(new File(this.dataFolder, format), !"force".equals(request.getParameter("formatCache")));
		}
		catch (IOException ioe) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, ("Invalid format: " + format));
			return;
		}
		response.setCharacterEncoding(ENCODING);
		response.setContentType("text/xml");
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(response.getOutputStream(), ENCODING));
		if (formatter != null)
			bw = new BufferedWriter(XsltUtils.wrap(bw, formatter));
		this.sendNameUsages(tnuIt, bw, !CONCISE_FORMAT.equals(format));
		bw.flush();
		bw.close();
	}
	
	private void sendNameUsages(PooledStringIterator strings, BufferedWriter bw, boolean full) throws IOException {
		if (!strings.hasNextString()) {
			bw.write("<" + NAME_USAGE_SET_NODE_TYPE);
			bw.write(TNU_XML_NAMESPACE_ATTRIBUTE);
			bw.write("/>");
			return;
		}
		
		bw.write("<" + NAME_USAGE_SET_NODE_TYPE);
		bw.write(TNU_XML_NAMESPACE_ATTRIBUTE);
		bw.write(" " + this.getXmlNamespaceUriBindings());
		bw.write(">");
		bw.newLine();
		while (strings.hasNextString())
			this.writeNameUsage(strings.getNextString(), bw, full);
		bw.write("</" + NAME_USAGE_SET_NODE_TYPE + ">");
		bw.newLine();
	}
	
	private void writeNameUsage(PooledString string, BufferedWriter bw, boolean full) throws IOException {
		bw.write("<" + NAME_USAGE_NODE_TYPE);
		bw.write(" " + STRING_ID_ATTRIBUTE + "=\"" + string.id + "\"");
		if ((string.getCanonicalStringID() != null) && (string.getCanonicalStringID().length() != 0))
			bw.write(" " + CANONICAL_STRING_ID_ATTRIBUTE + "=\"" + string.getCanonicalStringID() + "\"");
		bw.write(" " + CREATE_TIME_ATTRIBUTE + "=\"" + TIMESTAMP_DATE_FORMAT.format(new Date(string.getCreateTime())) + "\"");
		bw.write(" " + CREATE_DOMAIN_ATTRIBUTE + "=\"" + AnnotationUtils.escapeForXml(string.getCreateDomain()) + "\"");
		bw.write(" " + CREATE_USER_ATTRIBUTE + "=\"" + AnnotationUtils.escapeForXml(string.getCreateUser()) + "\"");
		bw.write(" " + UPDATE_TIME_ATTRIBUTE + "=\"" + TIMESTAMP_DATE_FORMAT.format(new Date(string.getUpdateTime())) + "\"");
		bw.write(" " + UPDATE_DOMAIN_ATTRIBUTE + "=\"" + AnnotationUtils.escapeForXml(string.getUpdateDomain(), true) + "\"");
		bw.write(" " + UPDATE_USER_ATTRIBUTE + "=\"" + AnnotationUtils.escapeForXml(string.getUpdateUser(), true) + "\"");
		bw.write(" " + DELETED_ATTRIBUTE + "=\"" + (string.isDeleted() ? "true" : "false") + "\"");
		
		if (!full && (string.getParseChecksum() != null) && (string.getParseChecksum().length() != 0))
			bw.write(" " + PARSE_CHECKSUM_ATTRIBUTE + "=\"" + string.getParseChecksum() + "\"");
		bw.write(">");
		bw.newLine();
		bw.write("<" + NAME_USAGE_PLAIN_NODE_TYPE + ">" + AnnotationUtils.escapeForXml(string.getStringPlain()) + "</" + NAME_USAGE_PLAIN_NODE_TYPE + ">");
		bw.newLine();
		if (full) {
			MutableAnnotation parsedString = SgmlDocumentReader.readDocument(new StringReader(string.getStringParsed()));
			if (parsedString != null) {
				bw.write("<" + NAME_USAGE_PARSED_NODE_TYPE + ">");
				bw.newLine();
				AnnotationUtils.writeXML(parsedString, bw);
				bw.newLine();
				bw.write("</" + NAME_USAGE_PARSED_NODE_TYPE + ">");
				bw.newLine();
			}
		}
		bw.write("</" + NAME_USAGE_NODE_TYPE + ">");
	}
	
	/* (non-Javadoc)
	 * @see de.uka.ipd.idaho.onn.stringPool.StringPoolServlet#extendIndexData(de.uka.ipd.idaho.onn.stringPool.StringPoolServlet.ParsedStringIndexData, de.uka.ipd.idaho.gamta.MutableAnnotation)
	 */
	protected void extendIndexData(ParsedStringIndexData indexData, MutableAnnotation stringParsed) {
		
		//	get index data
		TaxonNameUsage tnu = TaxonNameUsage.parseTaxonNameUsage(stringParsed);
		
		//	add attributes
		indexData.addIndexAttribute(NAME_STRING_ID_COLUMN_NAME, tnu.nameStringId);
		indexData.addIndexAttribute(NAME_STRING_ID_HASH_COLUMN_NAME, ("" + tnu.nameStringId.hashCode()));
		indexData.addIndexAttribute(TAXON_NAME_ID_COLUMN_NAME, ((tnu.taxonNameId == null) ? "" : tnu.taxonNameId));
		indexData.addIndexAttribute(TAXON_NAME_ID_HASH_COLUMN_NAME, ("" + ((tnu.taxonNameId == null) ? "" : tnu.taxonNameId).hashCode()));
		indexData.addIndexAttribute(NAME_USAGE_TYPE_COLUMN_NAME, tnu.nameUsageTypeCode);
		indexData.addIndexAttribute(NAME_USAGE_SUB_TYPE_COLUMN_NAME, ((tnu.nameUsageSubTypeCode == null) ? "" : tnu.nameUsageSubTypeCode));
		indexData.addIndexAttribute(BIB_REF_ID_COLUMN_NAME, tnu.bibRefId);
		indexData.addIndexAttribute(BIB_REF_ID_HASH_COLUMN_NAME, ("" + tnu.bibRefId.hashCode()));
		indexData.addIndexAttribute(PAGE_NUMBER_COLUMN_NAME, ("" + tnu.pageNumber));
	}
	
	/* (non-Javadoc)
	 * @see de.uka.ipd.idaho.onn.stringPool.StringPoolServlet#indexCaseSensitive()
	 */
	protected boolean indexCaseSensitive() {
		return true; // we don't want our ID strings converted to lower case ...
	}
	
	/* (non-Javadoc)
	 * @see de.uka.ipd.idaho.onn.stringPool.StringPoolServlet#checkPlainString(java.lang.String, java.lang.String, de.uka.ipd.idaho.gamta.MutableAnnotation)
	 */
	protected String checkPlainString(String stringId, String stringPlain, MutableAnnotation stringParsed) {
		
		//	test if string matches pattern
		return (TaxonNameUsage.isValid(stringPlain) ? null : "TaxonNameUsageBank accepts identifier strings only.");
	}
	
	/* (non-Javadoc)
	 * @see de.uka.ipd.idaho.onn.stringPool.StringPoolServlet#checkParsedString(java.lang.String, java.lang.String, de.uka.ipd.idaho.gamta.MutableAnnotation)
	 */
	protected String checkParsedString(String stringId, String stringPlain, MutableAnnotation stringParsed) {
		
		//	check tokens
		for (int t = 0; t < stringParsed.size(); t++) {
			String value = stringParsed.valueAt(t);
			if (stringPlain.indexOf(value) == -1)
				return ("Parsed string is inconsistent with plain string: '" + value + "' is not a part of '" + stringPlain + "'.");
		}
		
		//	no errors found
		return null;
	}
	
	/* (non-Javadoc)
	 * @see de.uka.ipd.idaho.onn.stringPool.StringPoolServlet#getStringType(de.uka.ipd.idaho.gamta.MutableAnnotation)
	 */
	protected String getStringType(MutableAnnotation stringParsed) {
		return "TaxonNameUsage";
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
	
	private PooledStringIterator findNameUsagesInternal(String[] fullTextQueryPredicates, boolean disjunctive, String user, boolean concise, int limit, boolean selfCanonicalOnly, String taxNameString, String taxName, Properties taxNameEpithets, String taxNameRank, String nameUsageType, String bibRef, String author, int year, int pageNumber) {
		String fullTextPredicate = (((fullTextQueryPredicates != null) && (fullTextQueryPredicates.length != 0)) ? fullTextQueryPredicates[0] : null);
		boolean useBbk = ((fullTextPredicate != null) || (taxNameString != null) || (taxNameRank != null));
		boolean useTxn = ((fullTextPredicate != null) || (taxName != null));
		boolean useTxnIdx = false;
		boolean useRbk = ((fullTextPredicate != null) || (bibRef != null));
		boolean useRbkIdx = ((author != null) || (year > -1));
		
		String nameUsageTypeCode = ((nameUsageType == null) ? null : ((String) nameUsageTypeCodes.get(nameUsageType)));
		if (nameUsageTypeCode == null)
			nameUsageTypeCode = ((nameUsageType == null) ? null : ((String) nameUsageSubTypeCodes.get(nameUsageType)));
		
		StringBuffer where = new StringBuffer("(1=0");
		if (fullTextPredicate != null) {
			where.append(" " + "OR" + " lower(bbk." + STRING_TEXT_COLUMN_NAME + ") LIKE '%" + EasyIO.prepareForLIKE(fullTextPredicate.toLowerCase()) + "%'");
			where.append(" " + "OR" + " lower(txn." + STRING_TEXT_COLUMN_NAME + ") LIKE '%" + EasyIO.prepareForLIKE(fullTextPredicate.toLowerCase()) + "%'");
			where.append(" " + "OR" + " lower(rbk." + STRING_TEXT_COLUMN_NAME + ") LIKE '%" + EasyIO.prepareForLIKE(fullTextPredicate.toLowerCase()) + "%'");
			where.append(" " + "OR" + " lower(data." + STRING_TEXT_COLUMN_NAME + ") LIKE '%" + EasyIO.prepareForLIKE(fullTextPredicate.toLowerCase()) + "%'");
		}
		where.append(")");
		if (where.length() < 6)
			where = new StringBuffer("(1=1)");
		
		if (taxNameString != null)
			where.append(" " + "AND" + " lower(bbk." + STRING_TEXT_COLUMN_NAME + ") LIKE '%" + EasyIO.prepareForLIKE(taxNameString.toLowerCase()) + "%'");
		if (taxName != null)
			where.append(" " + "AND" + " lower(txn." + STRING_TEXT_COLUMN_NAME + ") LIKE '%" + EasyIO.prepareForLIKE(taxName.toLowerCase()) + "%'");
		if (taxNameEpithets != null)
			for (int r = 0; r < this.primaryRankNames.length; r++) {
				String rankEpithet = taxNameEpithets.getProperty(this.primaryRankNames[r]);
				if (rankEpithet != null) {
					useTxnIdx = true;
					where.append(" " + "AND" + " txnidx.tx" + this.primaryRankNames[r].substring(0, 1).toUpperCase() + this.primaryRankNames[r].substring(1) + " LIKE '%" + EasyIO.prepareForLIKE(rankEpithet.toLowerCase()) + "%'");
				}
			}
		if (taxNameRank != null)
			where.append(" " + "AND" + " lower(bbk." + STRING_TYPE_COLUMN_NAME + ") LIKE '%" + EasyIO.prepareForLIKE(taxNameRank.toLowerCase()) + "%'");
		if (bibRef != null)
			where.append(" " + "AND" + " lower(rbk." + STRING_TEXT_COLUMN_NAME + ") LIKE '%" + EasyIO.prepareForLIKE(bibRef.toLowerCase()) + "%'");
		if (author != null)
			where.append(" " + "AND" + " rbkidx." + "DocAuthor" + " LIKE '%" + EasyIO.prepareForLIKE(author.toLowerCase()) + "%'");
		if (year > -1)
			where.append(" " + "AND" + " rbkidx." + "DocDate" + " LIKE '" + year + "%'");
		if (pageNumber > -1)
			where.append(" " + "AND" + " idx." + PAGE_NUMBER_COLUMN_NAME + " = " + pageNumber + "");
		
		if (nameUsageTypeCode != null)
			where.append(" AND ((idx." + NAME_USAGE_TYPE_COLUMN_NAME + " = '" + EasyIO.prepareForLIKE(nameUsageTypeCode) + "') OR (idx." + NAME_USAGE_SUB_TYPE_COLUMN_NAME + " = '" + EasyIO.prepareForLIKE(nameUsageTypeCode) + "'))");
		if (user != null)
			where.append(" AND ((data." + CREATE_USER_COLUMN_NAME + " LIKE '%" + EasyIO.prepareForLIKE(user) + "%') OR (data." + UPDATE_USER_COLUMN_NAME + " LIKE '%" + EasyIO.prepareForLIKE(user) + "%'))");
		
		//	catch empty predicates
		if (where.length() < 6)
			return new ExceptionPSI(new IOException("Invalid query"));
		
		//	filter out strings that are not self-canonical
		if (selfCanonicalOnly)
			where.append(" AND (data." + STRING_ID_HASH_COLUMN_NAME + " = data." + CANONICAL_STRING_ID_HASH_COLUMN_NAME + " OR data." + CANONICAL_STRING_ID_COLUMN_NAME + " = '')");
		
		//	assemble fields
		String fields = (
				"data." + STRING_ID_COLUMN_NAME + 
				", " +
				"data." + CANONICAL_STRING_ID_COLUMN_NAME + 
				", " + 
				"data." + PARSE_CHECKSUM_COLUMN_NAME + 
				", " +
				"data." + CREATE_TIME_COLUMN_NAME + 
				", " +
				"data." + CREATE_DOMAIN_COLUMN_NAME + 
				", " +
				"data." + CREATE_USER_COLUMN_NAME + 
				", " +
				"data." + UPDATE_TIME_COLUMN_NAME + 
				", " +
				"data." + UPDATE_DOMAIN_COLUMN_NAME + 
				", " +
				"data." + UPDATE_USER_COLUMN_NAME + 
				", " +
				"data." + LOCAL_UPDATE_TIME_COLUMN_NAME + 
				", " +
				"data." + DELETED_COLUMN_NAME + 
				", " +
				"data." + STRING_TEXT_COLUMN_NAME
				);
		
		//	assemble query
		String query = "SELECT " + fields +
				" FROM " + this.getStringDataTableName() + " data" + ", " + this.getStringIndexTableName() + " idx" + (useBbk ? (", " + this.bbk.getStringDataTableName() + " bbk") : "") + (useTxn ? (", " + this.txn.getStringDataTableName() + " txn") : "") + (useTxnIdx ? (", " + this.txn.getStringIndexTableName() + " txnidx") : "") + (useRbk ? (", " + this.rbk.getStringDataTableName() + " rbk") : "") + (useRbkIdx ? (", " + this.rbk.getStringIndexTableName() + " rbkidx") : "") +
				" WHERE 1=1" +
				" AND (data." + STRING_ID_HASH_COLUMN_NAME + " = idx." + STRING_ID_HASH_COLUMN_NAME + ")" +
				" AND (data." + STRING_ID_COLUMN_NAME + " = idx." + STRING_ID_COLUMN_NAME + ")" +
				(useBbk ? (
						" AND (idx." + NAME_STRING_ID_HASH_COLUMN_NAME + " = bbk." + STRING_ID_HASH_COLUMN_NAME + ")" +
						" AND (idx." + NAME_STRING_ID_COLUMN_NAME + " = bbk." + STRING_ID_COLUMN_NAME + ")"
					) : "") +
				(useTxn ? (
						" AND (idx." + TAXON_NAME_ID_HASH_COLUMN_NAME + " = txn." + STRING_ID_HASH_COLUMN_NAME + ")" +
						" AND (idx." + TAXON_NAME_ID_COLUMN_NAME + " = txn." + STRING_ID_COLUMN_NAME + ")"
				) : "") +
				(useTxnIdx ? (
						" AND (idx." + TAXON_NAME_ID_HASH_COLUMN_NAME + " = txnidx." + STRING_ID_HASH_COLUMN_NAME + ")" +
						" AND (idx." + TAXON_NAME_ID_COLUMN_NAME + " = txnidx." + STRING_ID_COLUMN_NAME + ")"
				) : "") +
				(useRbk ? (
						" AND (idx." + BIB_REF_ID_HASH_COLUMN_NAME + " = rbk." + STRING_ID_HASH_COLUMN_NAME + ")" +
						" AND (idx." + BIB_REF_ID_COLUMN_NAME + " = rbk." + STRING_ID_COLUMN_NAME + ")"
				) : "") +
				(useRbkIdx ? (
						" AND (idx." + BIB_REF_ID_HASH_COLUMN_NAME + " = rbkidx." + STRING_ID_HASH_COLUMN_NAME + ")" +
						" AND (idx." + BIB_REF_ID_COLUMN_NAME + " = rbkidx." + STRING_ID_COLUMN_NAME + ")"
				) : "") +
				" AND " + where + 
				((limit > 0) ? (" LIMIT " + limit) : "") + 
				";";
		
		System.out.println("Query is " + query);
		SqlQueryResult sqr = null;
		try {
			System.out.println("TaxonNameUsageBank: searching ...");
			sqr = this.io.executeSelectQuery(query);
			System.out.println("TaxonNameUsageBank: search complete");
		}
		catch (SQLException sqle) {
			System.out.println("TaxonNameUsageBank: " + sqle.getClass().getName() + " (" + sqle.getMessage() + ") while searching name usages.");
			System.out.println("  query was " + query);
		}
		System.out.println("TaxonNameUsageBank: search result wrapped");
		return new SqlParsedStringIterator(sqr);
	}
	
	private class SqlParsedStringIterator implements PooledStringIterator {
		private SqlQueryResult sqr;
		private PooledString next;
		SqlParsedStringIterator(SqlQueryResult sqr) {
			this.sqr = sqr;
		}
		public boolean hasNextString() {
			if (this.next != null)
				return true;
			else if (this.sqr == null)
				return false;
			else if (this.sqr.next()) {
				this.next = new SqlPooledString(this.sqr);
				return true;
			}
			else {
				this.sqr.close();
				this.sqr = null;
				return false;
			}
		}
		public PooledString getNextString() {
			if (this.hasNextString()) {
				PooledString next = this.next;
				this.next = null;
				return next;
			}
			else return null;
		}
		public IOException getException() {
			return null;
		}
		private class SqlPooledString extends PooledString {
			private String canonicalId;
			private long createTime;
			private String createDomain;
			private String createUser;
			private long updateTime;
			private String updateDomain;
			private String updateUser;
			private long localUpdateTime;
			private boolean deleted;
			private String stringPlain;
			private String parseChecksum;
			private String parseError;
			SqlPooledString(SqlQueryResult sqr) {
				super(sqr.getString(0));
				this.canonicalId = sqr.getString(1);
				this.parseChecksum = sqr.getString(2);
				this.createTime = Long.parseLong(sqr.getString(3));
				this.createDomain = sqr.getString(4);
				this.createUser = sqr.getString(5);
				this.updateTime = Long.parseLong(sqr.getString(6));
				this.updateDomain = sqr.getString(7);
				this.updateUser = sqr.getString(8);
				this.localUpdateTime = Long.parseLong(sqr.getString(9));
				this.deleted = "D".equals(sqr.getString(10));
				this.stringPlain = sqr.getString(11);
				this.parseError = null;
			}
			public String getStringPlain() {
				return this.stringPlain;
			}
			public String getStringParsed() {
				TaxonNameUsage tnu = TaxonNameUsage.parseTaxonNameUsage(this.stringPlain);
				return tnu.toParsedString();
			}
			public String getParseChecksum() {
				return this.parseChecksum;
			}
			public String getCanonicalStringID() {
				return this.canonicalId;
			}
			public String getParseError() {
				return this.parseError;
			}
			public long getCreateTime() {
				return this.createTime;
			}
			public String getCreateDomain() {
				return this.createDomain;
			}
			public String getCreateUser() {
				return this.createUser;
			}
			public long getUpdateTime() {
				return this.updateTime;
			}
			public String getUpdateDomain() {
				return this.updateDomain;
			}
			public String getUpdateUser() {
				return this.updateUser;
			}
			public long getNodeUpdateTime() {
				return this.localUpdateTime;
			}
			public boolean wasCreated() {
				return false;
			}
			public boolean wasUpdated() {
				return false;
			}
			public boolean isDeleted() {
				return this.deleted;
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see de.uka.ipd.idaho.tnuBank.TnuBankClient#getNameUsage(java.lang.String, boolean)
	 */
	public PooledString getNameUsage(String nameUsageId, boolean expand) throws IOException {
		PooledString ps = this.getString(nameUsageId);
		return ((expand && (ps != null)) ? new ExpandedParsePooledString(ps) : ps);
	}
	
	/* (non-Javadoc)
	 * @see de.uka.ipd.idaho.tnuBank.TnuBankClient#getNameUsages(java.lang.String[], boolean)
	 */
	public PooledStringIterator getNameUsages(String[] nameUsageIds, boolean expand) {
		PooledStringIterator psi = this.getStrings(nameUsageIds);
		return ((expand && (psi.getException() == null)) ? new ExpandedParsePooledStringIterator(psi) : psi);
	}
	
	/* (non-Javadoc)
	 * @see de.uka.ipd.idaho.onn.stringPool.StringPoolServlet#findStrings(java.lang.String[], boolean, java.lang.String, java.lang.String, int, boolean)
	 */
	public PooledStringIterator findStrings(String[] textPredicates, boolean disjunctive, String type, String user, int limit, boolean selfCanonicalOnly) {
		return this.findStrings(textPredicates, disjunctive, type, user, false, limit, selfCanonicalOnly);
	}
	
	/* (non-Javadoc)
	 * @see de.uka.ipd.idaho.onn.stringPool.StringPoolServlet#findStrings(java.lang.String[], boolean, java.lang.String, java.lang.String, boolean, int, boolean)
	 */
	public PooledStringIterator findStrings(String[] textPredicates, boolean disjunctive, String type, String user, boolean concise, int limit, boolean selfCanonicalOnly) {
		return this.findNameUsagesInternal(textPredicates, disjunctive, user, concise, limit, selfCanonicalOnly, null, null, null, null, null, null, null, -1, -1);
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
		PooledStringIterator psi = this.findNameUsagesInternal(null, false, user, concise, limit, false, taxNameString, taxName, null, taxNameRank, nameUsageType, bibRef, null, -1, pageNumber);
		return ((expand && (psi.getException() == null)) ? new ExpandedParsePooledStringIterator(psi) : psi);
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
		PooledStringIterator psi = this.findNameUsagesInternal(null, false, user, concise, limit, false, taxNameString, null, taxNameEpithets, null, nameUsageType, null, author, year, pageNumber);
		return ((expand && (psi.getException() == null)) ? new ExpandedParsePooledStringIterator(psi) : psi);
	}

	private class ExpandedParsePooledString extends PooledString {
		PooledString ps;
		public ExpandedParsePooledString(PooledString ps) {
			super(ps.id);
			this.ps = ps;
		}
		public String getStringPlain() {
			return this.ps.getStringPlain();
		}
		public String getStringParsed() {
			TaxonNameUsage tnu = TaxonNameUsage.parseTaxonNameUsage(ps.getStringPlain());
			final StringBuffer parsedString = new StringBuffer("<taxonNameUsage>");
			
			try {
				PooledString nameStringPs = bbk.getString(tnu.nameStringId);
				if (nameStringPs != null)
					parsedString.append("<dwc:scientificName>" + AnnotationUtils.escapeForXml(nameStringPs.getStringPlain()) + "</dwc:scientificName>");
			} catch (IOException ioe) {}
			parsedString.append("<dwc:scientificNameID>" + AnnotationUtils.escapeForXml(tnu.nameStringId) + "</dwc:scientificNameID>");
			
			if (tnu.taxonNameId != null) {
				MutableAnnotation taxonNameParsed = txn.getStringParsed(tnu.taxonNameId);
				if (taxonNameParsed != null) try {
					AnnotationUtils.writeXML(taxonNameParsed, new Writer() {
						public void write(char[] cbuf, int off, int len) throws IOException {
							parsedString.append(cbuf, off, len);
						}
						public void flush() throws IOException {}
						public void close() throws IOException {}
					});
				} catch (IOException ioe) {}
				parsedString.append("<dwc:taxonID>" + AnnotationUtils.escapeForXml(tnu.taxonNameId) + "</dwc:taxonID>");
			}
			
			parsedString.append("<nameUsageType>" + ((String) nameUsageTypes.get(tnu.nameUsageTypeCode)) + "</nameUsageType>");
			if (tnu.nameUsageSubTypeCode != null)
				parsedString.append("<nameUsageSubType>" + ((String) nameUsageSubTypes.get(tnu.nameUsageSubTypeCode)) + "</nameUsageSubType>");
			
			MutableAnnotation bibRefParsed = rbk.getStringParsed(tnu.bibRefId);
			if (bibRefParsed != null) try {
				AnnotationUtils.writeXML(bibRefParsed, new Writer() {
					public void write(char[] cbuf, int off, int len) throws IOException {
						parsedString.append(cbuf, off, len);
					}
					public void flush() throws IOException {}
					public void close() throws IOException {}
				});
			} catch (IOException ioe) {}
			parsedString.append("<bibRefId>" + AnnotationUtils.escapeForXml(tnu.bibRefId) + "</bibRefId>");
			
			parsedString.append("<pageNumber>" + tnu.pageNumber + "</pageNumber>");
			
			parsedString.append("</taxonNameUsage>");
			return parsedString.toString();
		}
		public String getParseChecksum() {
			return this.ps.getParseChecksum();
		}
		public String getCanonicalStringID() {
			return this.ps.getCanonicalStringID();
		}
		public String getParseError() {
			return this.ps.getParseError();
		}
		public long getCreateTime() {
			return this.ps.getCreateTime();
		}
		public String getCreateDomain() {
			return this.ps.getCreateDomain();
		}
		public String getCreateUser() {
			return this.ps.getCreateUser();
		}
		public long getUpdateTime() {
			return this.ps.getUpdateTime();
		}
		public String getUpdateDomain() {
			return this.ps.getUpdateDomain();
		}
		public String getUpdateUser() {
			return this.ps.getUpdateUser();
		}
		public long getNodeUpdateTime() {
			return this.ps.getNodeUpdateTime();
		}
		public boolean wasCreated() {
			return this.ps.wasCreated();
		}
		public boolean wasUpdated() {
			return this.ps.wasUpdated();
		}
		public boolean isDeleted() {
			return this.ps.isDeleted();
		}
	}
	
	private class ExpandedParsePooledStringIterator implements PooledStringIterator {
		PooledStringIterator psi;
		ExpandedParsePooledStringIterator(PooledStringIterator psi) {
			this.psi = psi;
		}
		public boolean hasNextString() {
			return this.psi.hasNextString();
		}
		public PooledString getNextString() {
			return new ExpandedParsePooledString(this.psi.getNextString());
		}
		public IOException getException() {
			return this.psi.getException();
		}
	}
	
	/* (non-Javadoc)
	 * @see de.uka.ipd.idaho.onn.stringPool.StringPoolServlet#updateString(de.uka.ipd.idaho.onn.stringPool.StringPoolClient.UploadString, java.lang.String)
	 */
	public PooledString updateString(UploadString string, String user) throws IOException {
		UploadString[] strings = {string};
		PooledStringIterator stringIt = this.updateStrings(strings, user);
		if (stringIt.hasNextString())
			return stringIt.getNextString();
		IOException ioe = stringIt.getException();
		if (ioe == null)
			return null;
		throw ioe;
	}
	
	/* (non-Javadoc)
	 * @see de.uka.ipd.idaho.onn.stringPool.StringPoolServlet#updateStrings(de.uka.ipd.idaho.onn.stringPool.StringPoolClient.UploadString[], java.lang.String)
	 */
	public PooledStringIterator updateStrings(UploadString[] strings, String user) {
		LinkedList uploadStrings = new LinkedList();
		for (int s = 0; s < strings.length; s++) try {
			String error = this.checkPlainString(this.getStringId(strings[s].stringPlain), strings[s].stringPlain, null);
			if (error != null)
				continue;
			TaxonNameUsage tnu = TaxonNameUsage.parseTaxonNameUsage(strings[s].stringPlain);
			uploadStrings.add(new UploadString(tnu.toPlainString(), tnu.toParsedString()));
		} catch (IOException ioe) {}
		return super.updateStrings(((UploadString[]) uploadStrings.toArray(new UploadString[uploadStrings.size()])), user);
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
}