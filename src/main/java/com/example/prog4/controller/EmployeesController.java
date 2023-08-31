package com.example.prog4.controller;

import com.example.prog4.controller.Data.InputData.EmployeeInput;
import com.example.prog4.controller.Mapper.EmployeeMapper;
import com.example.prog4.controller.params.pdfParams;
import com.example.prog4.entity.Employee.EmployeeEntity;
import com.example.prog4.service.CsvService;
import com.example.prog4.service.EmployeeService;
import com.example.prog4.service.PdfService;
import com.lowagie.text.DocumentException;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import lombok.AllArgsConstructor;
import net.kaczmarzyk.spring.data.jpa.domain.GreaterThanOrEqual;
import net.kaczmarzyk.spring.data.jpa.domain.Like;
import net.kaczmarzyk.spring.data.jpa.domain.LikeIgnoreCase;
import net.kaczmarzyk.spring.data.jpa.web.annotation.And;
import net.kaczmarzyk.spring.data.jpa.web.annotation.Join;
import net.kaczmarzyk.spring.data.jpa.web.annotation.Spec;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;


@Controller
@AllArgsConstructor
@RequestMapping("/employees")
public class EmployeesController {
  private final EmployeeService employeeService;
  private final CsvService csvService;
  private final PdfService pdfService;
  private final EmployeeMapper employeeMapper;

  @GetMapping
  public String getAllEmployees(
      @Join(path = "phoneNumbers", alias = "p")
      @And({
          @Spec(path = "firstname", params = "firstname", spec = LikeIgnoreCase.class),
          @Spec(path = "lastname", params = "lastname", spec = LikeIgnoreCase.class),
          @Spec(path = "sex", params = "sex", spec = LikeIgnoreCase.class),
          @Spec(path = "function", params = "function", spec = LikeIgnoreCase.class),
          @Spec(path = "hireDate", params = "hire", spec = GreaterThanOrEqual.class),
          @Spec(path = "resignationDate", params = "resignation", spec = GreaterThanOrEqual.class),
          @Spec(path = "p.phoneNumber", params = "phone", spec = LikeIgnoreCase.class),
          @Spec(path = "p.countryCode", params = "code", spec = Like.class)
      })
      Specification<EmployeeEntity> entitySpec, Model model) {
    Iterable<EmployeeEntity> employees = employeeService.findAllWithoutCnaps(entitySpec);
    model.addAttribute("employees", employees);
    return "employee-list";
  }

  @GetMapping("/csv")
  public void downloadEmployees(
      @Join(path = "phoneNumbers", alias = "p")
      @And({
          @Spec(path = "firstname", params = "firstname", spec = LikeIgnoreCase.class),
          @Spec(path = "lastname", params = "lastname", spec = LikeIgnoreCase.class),
          @Spec(path = "sex", params = "sex", spec = LikeIgnoreCase.class),
          @Spec(path = "function", params = "function", spec = LikeIgnoreCase.class),
          @Spec(path = "hireDate", params = "hire", spec = GreaterThanOrEqual.class),
          @Spec(path = "resignationDate", params = "resignation", spec = GreaterThanOrEqual.class),
          @Spec(path = "p.phoneNumber", params = "phone", spec = LikeIgnoreCase.class),
          @Spec(path = "p.countryCode", params = "code", spec = Like.class)
      })
      Specification<EmployeeEntity> entitySpec,
      HttpServletResponse response) {
    Iterable<EmployeeEntity> employees;
    employees = employeeService.findAll(entitySpec);
    csvService.writeEmployeesToCsv((List<EmployeeEntity>) employees, response);
  }

  @GetMapping("/{id}")
  public String getEmployeeById(Model model, @PathVariable int id) {
    EmployeeEntity employeeEntity = employeeService.findById(id);
    model.addAttribute("employee", employeeEntity);
    return "employee-card";
  }

  @GetMapping(value = "/{id}/pdf/{params}", produces = MediaType.APPLICATION_PDF_VALUE)
  public ResponseEntity<byte[]> getEmployeePdfAndRedirect(
      @PathVariable int id,
      @PathVariable pdfParams params
  )
      throws DocumentException {
    byte[] pdfCardAsBytes = pdfService.getPdf(id,params);

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_PDF);
    headers.setContentDispositionFormData("attachment", "employee.pdf");
    headers.setContentLength(pdfCardAsBytes.length);

    return new ResponseEntity<>(pdfCardAsBytes, headers, HttpStatus.OK);
  }


  @GetMapping("/add")
  public String showAddEmployeeForm(Model model) {
    model.addAttribute("employee", new EmployeeEntity());
    return "employee-add";
  }

  @PostMapping("/save")
  public String saveEmployee(@ModelAttribute("employee") EmployeeInput employee,
                             @RequestParam("image") MultipartFile imageFile,
                             @RequestParam("codes") String[] codes,
                             @RequestParam("phones") String[] phones) throws IOException {
    List<String> codesList = Arrays.asList(codes);
    List<String> phonesList = Arrays.asList(phones);
    employeeService.save(employeeMapper.toDomain(employee, codesList, phonesList, imageFile));
    return "redirect:/employees";
  }
}
