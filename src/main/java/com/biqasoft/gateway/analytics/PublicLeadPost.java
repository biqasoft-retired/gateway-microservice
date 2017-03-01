/*
* Copyright (c) 2016 biqasoft.com




 */

package com.biqasoft.gateway.analytics;

import com.biqasoft.entity.analytics.AnalyticRecord;
import com.biqasoft.entity.analytics.MarketingScore;
import org.javers.core.metamodel.annotation.Value;

import java.io.Serializable;

@Value
public class PublicLeadPost implements Serializable {

    private MarketingScore marketingLeadCreatedScore;
    private WebSDKSendCustomer lead;
    private AnalyticRecord analyticRecord;

    public MarketingScore getMarketingLeadCreatedScore() {
        return marketingLeadCreatedScore;
    }

    public void setMarketingLeadCreatedScore(MarketingScore marketingLeadCreatedScore) {
        this.marketingLeadCreatedScore = marketingLeadCreatedScore;
    }

    public WebSDKSendCustomer getLead() {
        return lead;
    }

    public void setLead(WebSDKSendCustomer lead) {
        this.lead = lead;
    }

    public AnalyticRecord getAnalyticRecord() {
        return analyticRecord;
    }

    public void setAnalyticRecord(AnalyticRecord analyticRecord) {
        this.analyticRecord = analyticRecord;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PublicLeadPost that = (PublicLeadPost) o;

        if (marketingLeadCreatedScore != null ? !marketingLeadCreatedScore.equals(that.marketingLeadCreatedScore) : that.marketingLeadCreatedScore != null)
            return false;
        if (lead != null ? !lead.equals(that.lead) : that.lead != null) return false;
        return analyticRecord != null ? analyticRecord.equals(that.analyticRecord) : that.analyticRecord == null;

    }

    @Override
    public int hashCode() {
        int result = marketingLeadCreatedScore != null ? marketingLeadCreatedScore.hashCode() : 0;
        result = 31 * result + (lead != null ? lead.hashCode() : 0);
        result = 31 * result + (analyticRecord != null ? analyticRecord.hashCode() : 0);
        return result;
    }

}
