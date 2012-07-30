//Thanks to http://benjii.me/2010/08/endless-scrolling-listview-in-android/ for this code snippit.

package com.rcythr.secretsms.util;

import com.rcythr.secretsms.SMSListing;

import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;

public class EndlessScrollListener implements OnScrollListener {

    private int visibleThreshold = 5;
    private int currentPage = 0;
    private int previousTotal = 0;
    private boolean loading = true;
    private SMSListing listing;
    
    
    public EndlessScrollListener(SMSListing listing) {
    	this.listing = listing;
    }
    public EndlessScrollListener(int visibleThreshold) {
        this.visibleThreshold = visibleThreshold;
    }

    public void onScroll(AbsListView view, int firstVisibleItem,
            int visibleItemCount, int totalItemCount) {
        if (loading) {
            if (totalItemCount > previousTotal) {
                loading = false;
                previousTotal = totalItemCount;
                currentPage++;
            }
        }
        if (!loading && (totalItemCount - visibleItemCount) <= (firstVisibleItem + visibleThreshold)) {
            listing.populateSMS(previousTotal + 25);
            loading = true;
        }
    }

    public void onScrollStateChanged(AbsListView view, int scrollState) {}
}