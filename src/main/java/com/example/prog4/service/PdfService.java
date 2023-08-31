package com.example.prog4.service;

import static com.example.prog4.controller.params.pdfParams.BIRTHDAY;
import static com.example.prog4.controller.params.pdfParams.CUSTOM_DELAY;
import static com.example.prog4.controller.params.pdfParams.YEAR_ONLY;

import com.example.prog4.controller.params.pdfParams;
import com.example.prog4.entity.Employee.CompanyConf;
import com.example.prog4.entity.Employee.EmployeeEntity;
import com.lowagie.text.DocumentException;
import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import org.xhtmlrenderer.pdf.ITextRenderer;

@Service
@AllArgsConstructor
public class PdfService {
  private static final String EMPLOYEE_HTML_TEMPLATE = "employee-file";
  private final CompanyConfService companyConfService;
  private final EmployeeService employeeService;

  public byte[] generatePdfFromHtml(String html)
      throws DocumentException {
    ITextRenderer renderer = new ITextRenderer();
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    renderer.setDocumentFromString(html);
    renderer.layout();
    renderer.createPDF(outputStream);
    return outputStream.toByteArray();

  }

  private String parseThymeleafTemplate(EmployeeEntity employeeEntity, CompanyConf companyConf) {
    ClassLoaderTemplateResolver templateResolver = new ClassLoaderTemplateResolver();
    templateResolver.setPrefix("templates/");
    templateResolver.setSuffix(".html");
    templateResolver.setCharacterEncoding("UTF-8");
    templateResolver.setTemplateMode(TemplateMode.HTML);
    templateResolver.setOrder(1);

    TemplateEngine templateEngine = new TemplateEngine();
    templateEngine.setTemplateResolver(templateResolver);

    Context context = new Context();
    context.setVariable("employee", employeeEntity);
    context.setVariable("companyConf", companyConf);

    return templateEngine.process(PdfService.EMPLOYEE_HTML_TEMPLATE, context);
  }


  public byte[] getPdf(int id, pdfParams params) throws DocumentException {
    CompanyConf companyConf = companyConfService.getCompanyConf();
    if (params.equals(YEAR_ONLY)){
      EmployeeEntity employee = employeeService.findById(id);
      LocalDate actualDate = LocalDate.now();

      int birthdate = employee.getBirthdate().getYear();
      int actualYear = actualDate.getYear();

      employee.setAge(actualYear - birthdate);

      return generatePdfFromHtml(parseThymeleafTemplate(employee, companyConf));
   }
   if (params.equals(CUSTOM_DELAY)){
     return null;
   }
    EmployeeEntity employee = employeeService.findById(id);
    return generatePdfFromHtml(parseThymeleafTemplate(employee, companyConf));
  }
}