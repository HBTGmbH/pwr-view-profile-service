package de.hbt.pwr.view.controller;

import de.hbt.pwr.view.client.files.FileUploadClient;
import de.hbt.pwr.view.client.report.ReportServiceClient;
import de.hbt.pwr.view.client.skill.SkillServiceFallback;
import de.hbt.pwr.view.exception.TemplateNotFoundException;
import de.hbt.pwr.view.model.ReportTemplate;
import de.hbt.pwr.view.model.UploadFileResponse;
import de.hbt.pwr.view.service.ReportTemplateService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

@CrossOrigin
@RequestMapping("/template")
@Controller
public class ReportTemplateController {

    private static final Logger LOG  = LogManager.getLogger(SkillServiceFallback.class);

    private final ReportTemplateService reportTemplateService;

    private FileUploadClient fileUploadClient;

    private ReportServiceClient reportServiceClient;

    @Autowired
    public ReportTemplateController(ReportTemplateService reportTemplateService,
                                    FileUploadClient fileUploadClient,
                                    ReportServiceClient reportServiceClient) {
        this.reportTemplateService = reportTemplateService;
        this.fileUploadClient = fileUploadClient;
        this.reportServiceClient = reportServiceClient;
    }


    //---------------
    // Templates
    //---------------

    @GetMapping
    public ResponseEntity<List<String>> getAllTemplates() {
        List<String> ids = reportTemplateService.getTemplateIds();
        return ResponseEntity.ok(ids);
    }

    @GetMapping("{id}")
    public ResponseEntity<ReportTemplate> getTemplate(@PathVariable String id) {
        return ResponseEntity.ok(reportTemplateService.getTemplate(id));
    }

    @PostMapping
    public ResponseEntity<ReportTemplate> uploadTemplate(@RequestParam("file") MultipartFile file, @RequestParam("templateSlice") String templateString) {

        ReportTemplate newTemplate = new ReportTemplate();
        ReportTemplate.ReportTemplateSlice templateSlice = ReportTemplate.ReportTemplateSlice.fromJSON(templateString);
        ResponseEntity<UploadFileResponse> designFileResponseEntity = fileUploadClient.uploadFile(file);

        if (designFileResponseEntity.getStatusCode() == HttpStatus.OK) {
            LOG.info(designFileResponseEntity.getBody());
            UploadFileResponse designFileResponse = designFileResponseEntity.getBody();
            ResponseEntity<UploadFileResponse> previewFileResponseEntity = reportServiceClient.generatePdf(designFileResponse.getFileId());

            if (previewFileResponseEntity.getStatusCode() == HttpStatus.OK) {
                UploadFileResponse previewFileResponse = previewFileResponseEntity.getBody();
                newTemplate.setName(templateSlice.name);
                newTemplate.setDescription(templateSlice.description);
                newTemplate.setCreatedDate(LocalDate.now());
                newTemplate.setFileId(designFileResponse.getFileId());
                newTemplate.setCreateUser(templateSlice.createUser);
                newTemplate.setPreviewId(previewFileResponse.getFileId());
                ReportTemplate template = reportTemplateService.saveTemplate(newTemplate);
                return ResponseEntity.ok(template);
            } else {
                // TODO delete design file (compensation for failed transaction)
                throw new RuntimeException("Could not store preview for template " + designFileResponse.getFileId());
            }

        } else {
            throw new RuntimeException("Could not store template.");
        }

    }


    @DeleteMapping("{id}")
    public ResponseEntity deleteTemplate(@PathVariable String id) {
        fileUploadClient.deleteFile(reportTemplateService.getTemplate(id).getPreviewId());
        fileUploadClient.deleteFile(reportTemplateService.getTemplate(id).getFileId());
        reportTemplateService.deleteTemplate(id);
        return ResponseEntity.ok("Success");
    }


    @PostMapping("{id}")
    public ResponseEntity updateTemplate(
            @PathVariable("id") String id,
            @RequestParam("templateSlice") String templateString) {


        ReportTemplate.ReportTemplateSlice templateSlice = ReportTemplate.ReportTemplateSlice.fromJSON(templateString);

        ReportTemplate newTemplate = new ReportTemplate();
        newTemplate.setName(templateSlice.name);
        newTemplate.setDescription(templateSlice.description);
        newTemplate.setFileId(reportTemplateService.getTemplate(id).getFileId());
        newTemplate.setCreateUser(reportTemplateService.getTemplate(id).getCreateUser());
        newTemplate.setCreatedDate(reportTemplateService.getTemplate(id).getCreatedDate());
        newTemplate.setPreviewId(reportTemplateService.getTemplate(id).getPreviewId());
        ReportTemplate template = reportTemplateService.updateTemplate(id, newTemplate);
        return ResponseEntity.ok(template);
    }

    //---------------
    // Preview
    //---------------

    @GetMapping("preview/{id}")
    public ResponseEntity<Resource> getPreview(@PathVariable String id) throws TemplateNotFoundException {
        ReportTemplate template = reportTemplateService.getTemplate(id);
        if (template != null) {
            String filename = template.getPreviewId();
            if (filename != null && !filename.equals("")) {

                ResponseEntity<Resource> res = fileUploadClient.serveFile(filename);
                LOG.info("Preview Received for %s is "+res,id);
                return res;
            }
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("preview")
    public ResponseEntity<List<String>> getAllPreviews() {
        return ResponseEntity.ok(reportTemplateService.getAllPreviewFilenames());
    }

    @GetMapping("preview/ids")
    public ResponseEntity<List<String>> getAllPreviewTemplateIds() {
        return ResponseEntity.ok(reportTemplateService.getAllPreviewTemplateIds());
    }
}
