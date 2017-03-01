/*
 * Copyright (c) 2016. com.biqasoft
 */

package com.biqasoft.gateway.indicators.repositories;

import com.biqasoft.gateway.leadgen.repositories.LeadGenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.biqasoft.entity.datasources.SavedLeadGenKPI;
import com.biqasoft.entity.indicators.dto.IndicatorsDTO;

@Service
public class KPIsLeadGenMethodRepository {

    private final LeadGenRepository leadRepository;

    @Autowired
    public KPIsLeadGenMethodRepository(LeadGenRepository leadRepository) {
        this.leadRepository = leadRepository;
    }

    // KPI for lead gen method
    public IndicatorsDTO getIndicatorsForLeadGenMethod(String leadGenMethodId) {

        SavedLeadGenKPI leadGenMethodHistoryDataLatest = leadRepository.findLeadGenMethodHistoryDataLatest(leadGenMethodId, null, null);

        if (leadGenMethodHistoryDataLatest == null || leadGenMethodHistoryDataLatest.getCachedKPIsData() == null) {
            return new IndicatorsDTO();
        }

        return leadGenMethodHistoryDataLatest.getCachedKPIsData();
    }

    // KPI for lead gen project
    public IndicatorsDTO getIndicatorsForLeadGenProject(String leadGenProjectId) {
        SavedLeadGenKPI leadGenMethodHistoryDataLatest = leadRepository.findLeadGenMethodHistoryDataLatest(leadGenProjectId, null, null);

        if (leadGenMethodHistoryDataLatest == null || leadGenMethodHistoryDataLatest.getCachedKPIsData() == null) {
            return new IndicatorsDTO();
        }

        return leadGenMethodHistoryDataLatest.getCachedKPIsData();
    }

}
