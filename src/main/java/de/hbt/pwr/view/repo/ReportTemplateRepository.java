package de.hbt.pwr.view.repo;

import de.hbt.pwr.view.model.ReportTemplate;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReportTemplateRepository extends CrudRepository<ReportTemplate, String> {
    void deleteById(String id);
    ReportTemplate findReportTemplateById(String id);
}
