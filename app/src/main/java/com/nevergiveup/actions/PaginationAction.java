package com.nevergiveup.actions;


import android.content.Context;
import android.util.Log;


import com.nevergiveup.model.Pagination;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public abstract class PaginationAction<Result> extends AbstractAction<Pagination<Result>> {
    private static final String tag = "TT-PaginationAction";
    //request keys
    private static final String PAGE_INDEX = "page";
    private static final String PAGE_SIZE = "num";
    //response keys
    private static final String COUNT = "Count";

    private int mPageIndex;
    private int mPageSize;
    protected int mTotalCount = -1;
    /**
     * For GET_RECOMMENDATION and GET_HOT and GET_FAVORITE actions
     * @param context
     * @param pageNum
     * @param itemsPerPage
     */
    public PaginationAction(Context context, int pageNum, int itemsPerPage){
        super(context);
        mPageIndex = pageNum;
        mPageSize = itemsPerPage;
    }

    @Override
    public void addRequestParameters(JSONObject parameters) throws JSONException {
        parameters.put(PAGE_INDEX, String.valueOf(mPageIndex));
        parameters.put(PAGE_SIZE, String.valueOf(mPageSize));
    }

    @Override
    public final Pagination<Result> createRespObject(JSONObject response) throws JSONException {
        Pagination<Result> pagination = new Pagination<Result>();
        pagination.setPageSize(mPageSize);
        if(response.has(COUNT)){
            try{
            	pagination.setTotalCounts(response.getInt(COUNT));
            }catch(Exception e){
            	Log.e(tag, "Failed to parse " + COUNT + ": " + e.getMessage());
            }
        }
        if(response.has(RESP_LIST)){
        	try{
	            JSONArray items = response.getJSONArray(RESP_LIST);
	            for(int i=0; i<items.length(); i++){
	                JSONObject item = items.getJSONObject(i);
	                pagination.getItems().add(convertJsonToResult(item));
	            }
        	}catch(Exception e){
            	Log.e(tag, "Failed to parse " + RESP_LIST + ": " + e.getMessage());
        	}
        }
        mTotalCount = pagination.getTotalCounts();
        return pagination;
    }

    protected ActionResult<Pagination<Result>> doInBackground(Void...  params){
    	if( mTotalCount < 0 || (mPageIndex -1) * mPageSize < mTotalCount){
    		return super.doInBackground(params);
    	}else{
    		mPageIndex -= 1; //-1 so that getNextPageAction will return this page index;
    		Pagination<Result> pagination = new Pagination<Result>();
            pagination.setPageSize(mPageSize);
            pagination.setCurrentPage(mPageIndex);
            pagination.setTotalCounts(mTotalCount);
    		return new ActionResult<Pagination<Result>>(pagination);
    	}
    }
    
    public boolean hasMore(){
    	return mTotalCount < 0 || mPageIndex * mPageSize < mTotalCount;
    }
    
    public <T extends PaginationAction<Result>> T getNextPageAction(){
        T action = cloneCurrentPageAction();
        action.setPageIndex(action.getPageIndex() + 1);
        action.setTotalCount(action.getTotalCount());
        return action;
    }
    
    public <T extends PaginationAction<Result>> T getPreviousPageAction(){
        T action = cloneCurrentPageAction();
        if(action.getPageIndex() > 1)
        	action.setPageIndex( action.getPageIndex() - 1);
        action.setTotalCount(action.getTotalCount());
        return action;
    }

    public final int getPageIndex(){
        return mPageIndex;
    }

    public void setPageIndex(int pageIndex){
        this.mPageIndex = pageIndex;
    }

    public final int getPageSize(){
        return mPageSize;
    }

    public final int getTotalCount(){
    	return mTotalCount;
    }
    
    public void setTotalCount(int totalCount){
    	mTotalCount = totalCount;
    }
    
    /**
     * For an AysnTask action can only be executed once, so we have to create a new one
     * for each execution. Subclasses of this class should clone all fields to the action
     * of the next page
     * @return
     */
    public abstract <T extends PaginationAction<Result>> T cloneCurrentPageAction();
    protected abstract Result convertJsonToResult(JSONObject item) throws JSONException;
}
