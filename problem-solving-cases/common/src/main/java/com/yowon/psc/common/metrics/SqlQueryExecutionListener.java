package com.yowon.psc.common.metrics;

import java.util.List;
import net.ttddyy.dsproxy.ExecutionInfo;
import net.ttddyy.dsproxy.QueryInfo;
import net.ttddyy.dsproxy.listener.QueryExecutionListener;

public class SqlQueryExecutionListener implements QueryExecutionListener {

    @Override
    public void beforeQuery(ExecutionInfo execInfo, List<QueryInfo> queryInfoList) {
        // no-op
    }

    @Override
    public void afterQuery(ExecutionInfo execInfo, List<QueryInfo> queryInfoList) {
        long count = (queryInfoList == null || queryInfoList.isEmpty()) ? 1L : queryInfoList.size();
        SqlQueryCountHolder.addCount(count);
    }
}
