package com.example.jobs.temps;

import java.util.LinkedHashSet;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.example.jobs.temps.entities.Temp;

@Service
public class TempHierarchyService {

    public Set<Long> getDescendantIds(Temp root) {
        Set<Long> ids = new LinkedHashSet<>();
        collectDescendants(root, ids);
        return ids;
    }

    public Set<Long> getSelfAndDescendantIds(Temp root) {
        Set<Long> ids = new LinkedHashSet<>();
        if (root.getId() != null) {
            ids.add(root.getId());
        }
        collectDescendants(root, ids);
        return ids;
    }

    public boolean isStrictDescendant(Long candidateId, Temp root) {
        return getDescendantIds(root).contains(candidateId);
    }

    public boolean isSelfOrDescendant(Long candidateId, Temp root) {
        return getSelfAndDescendantIds(root).contains(candidateId);
    }

    private void collectDescendants(Temp current, Set<Long> ids) {
        for (Temp report : current.getReports()) {
            if (report.getId() != null && ids.add(report.getId())) {
                collectDescendants(report, ids);
            }
        }
    }
}