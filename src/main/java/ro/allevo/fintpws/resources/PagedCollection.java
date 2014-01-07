/*
* FinTP - Financial Transactions Processing Application
* Copyright (C) 2013 Business Information Systems (Allevo) S.R.L.
*
* This program is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this program. If not, see <http://www.gnu.org/licenses/>
* or contact Allevo at : 031281 Bucuresti, 23C Calea Vitan, Romania,
* phone +40212554577, office@allevo.ro <mailto:office@allevo.ro>, www.allevo.ro.
*/

package ro.allevo.fintpws.resources;

import java.util.List;

import javax.persistence.Query;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

/**
 * @author horia
 * @version $Revision: 1.0 $
 */
public class PagedCollection {
	/**
	 * Field logger.
	 */
	private static Logger logger = LogManager.getLogger(PagedCollection.class
			.getName());

	/**
	 * Field PARAM_FILTER. (value is ""filter"")
	 */
	static final String PARAM_FILTER = "filter";
	/**
	 * Field PARAM_PAGE. (value is ""page"")
	 */
	static final String PARAM_PAGE = "page";
	/**
	 * Field PARAM_PAGE_SIZE. (value is ""filter"")
	 */
	static final String PARAM_PAGE_SIZE = "page_size";
	/**
	 * Field FILTER_TOTAL. (value is ""t"")
	 */
	static final String FILTER_TOTAL = "t";

	/**
	 * Field DEFAULT_PAGE. (value is 1)
	 */
	private static final int DEFAULT_PAGE = 1;
	/**
	 * Field DEFAULT_PAGE_SIZE. (value is 100)
	 */
	private static final int DEFAULT_PAGE_SIZE = 100;
	/**
	 * Field MAX_PAGE_SIZE. (value is 100)
	 */
	private static final int MAX_PAGE_SIZE = 100;

	private int page;
	private int pageSize;
	private List<?> items;
	private Integer total;

	// actual uri info provided by parent resource
	private UriInfo uriInfo;
	private Query itemsQuery;
	private Query totalQuery;

	private boolean needsTotal;
	private boolean hasMore;

	/**
	 * Constructor for PagedCollection.
	 * 
	 * @param uriInfo
	 *            UriInfo
	 * @param query
	 *            Query
	 */
	public PagedCollection(UriInfo uriInfo, Query itemsQuery, Query totalQuery) {
		this.uriInfo = uriInfo;
		this.itemsQuery = itemsQuery;
		this.totalQuery = totalQuery;
		this.total = 0;
		this.page = DEFAULT_PAGE;
		this.pageSize = DEFAULT_PAGE_SIZE;
		this.hasMore = false;
		this.needsTotal = false;
	}

	/**
	 * Method getPage. Sanitizes the input query string parameters [page] and
	 * [page_size] and retrieves the requested page of items from the database
	 * page_size is limited to 100 If page is invalid ( not a number, <1 ), the
	 * first page is returned If page_size is invalid ( not a number, <0, >100
	 * ), the page size is set to 100
	 */
	protected void getPage() {
		MultivaluedMap<String, String> params = uriInfo.getQueryParameters();

		// get page
		page = DEFAULT_PAGE;
		if (params.containsKey(PARAM_PAGE)) {
			try {
				page = Integer.parseInt(params.getFirst(PARAM_PAGE));
			} catch (NumberFormatException nfe) {
				// just ignore garbage, return default page
			}
		}
		// check boundaries
		if (page < 1) {
			page = DEFAULT_PAGE;
		}

		// get page size
		pageSize = DEFAULT_PAGE_SIZE;
		if (params.containsKey(PARAM_PAGE_SIZE)) {
			try {
				pageSize = Integer.parseInt(params.getFirst(PARAM_PAGE_SIZE));
			} catch (NumberFormatException nfe) {
				// just ignore garbage, return default page
			}
		}
		// check boundaries
		if ((pageSize < 0) || (pageSize > MAX_PAGE_SIZE)) {
			pageSize = DEFAULT_PAGE_SIZE;
		}

		// request +1 item and set has_more metadata if it is returned
		itemsQuery.setFirstResult((page - 1) * pageSize);
		itemsQuery.setMaxResults(pageSize + 1);
		hasMore = false;

		// execute the query
		items = itemsQuery.getResultList();
		if (items.size() == pageSize + 1) {
			// remove the extra item
			items.remove(pageSize);
			hasMore = true;
		}

		// look for a fiter to request total number of items
		needsTotal = false;
		if ((params.containsKey(PARAM_FILTER))
				&& params.getFirst(PARAM_FILTER).contains(FILTER_TOTAL)) {
			needsTotal = true;

			// optimization ( if we're on the first page and has_more is false,
			// return the count from the selection )
			if ((page == DEFAULT_PAGE) && (!hasMore)) {
				total = items.size();
			} else {
				total = ((Long) totalQuery.getSingleResult()).intValue();
			}
		}
	}

	/**
	 * Method asJson.
	 * 
	 * @return JSONObject
	 * @throws JSONException
	 */
	protected JSONObject asJson() throws JSONException {
		JSONObject jsonItem = ApiResource.getMetaResource(uriInfo.getPath(),
				this.getClass());
		if (needsTotal)
			jsonItem.put("total", total);
		if (hasMore) {
			jsonItem.put("has_more", hasMore);
		}

		return jsonItem;
	}

	/**
	 * Method getItems.
	 * 
	 * @return List<?>
	 */
	public List<?> getItems() {
		return items;
	}

	/**
	 * Method getItemsQuery.
	 * 
	 * @return Query
	 */
	public Query getItemsQuery() {
		return itemsQuery;
	}

	/**
	 * Method getTotalQuery.
	 * 
	 * @return Query
	 */
	public Query getTotalQuery() {
		return totalQuery;
	}

	/**
	 * Method setItemsQuery.
	 * 
	 * @param itemsQuery
	 *            Query
	 */
	public void setItemsQuery(Query itemsQuery) {
		this.itemsQuery = itemsQuery;
	}

	/**
	 * Method setTotalQuery.
	 * 
	 * @param totalQuery
	 *            Query
	 */
	public void setTotalQuery(Query totalQuery) {
		this.totalQuery = totalQuery;
	}

	/**
	 * Method has_more
	 * 
	 * @return boolean
	 * */
	public boolean hasMore() {
		return hasMore;
	}

	/**
	 * Method getUriInfo.
	 * 
	 * @return UriInfo
	 */
	public UriInfo getUriInfo() {
		return uriInfo;
	}
}
