package cn.com.util;

import cn.com.bo.EmployeeBO;
import cn.com.entities.*;
import org.apache.poi.hpsf.DocumentSummaryInformation;
import org.apache.poi.hpsf.SummaryInformation;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

/**
 * @author wyl
 * @create 2020-08-08 19:16
 */
public class POIUtils {
    public static ResponseEntity<byte[]> employee2Excel(List<EmployeeBO> employeeList) {
        String[] empHeaders = {"编号","姓名","工号","性别","出生日期","身份证号码","婚姻状况","民族","籍贯","政治面貌","电话号码","联系地址","所属部门","职称","职位","聘用形式","最高学历","专业","毕业院校","入职日期","在职状态","邮箱","合同期限(年)","合同起始日期","合同终止日期","转正日期"};

        // 1.创建一个工作簿
        HSSFWorkbook sheets = new HSSFWorkbook();
        // 2.创建信息属性
        sheets.createInformationProperties();
        // 设置文档的文档摘要信息
        DocumentSummaryInformation documentSummaryInformation = sheets.getDocumentSummaryInformation();
        documentSummaryInformation.setCategory("员工信息");
        documentSummaryInformation.setCompany("www.cn.com");
        documentSummaryInformation.setManager("javaBoy");
        // 设置摘要信息
        SummaryInformation summaryInformation = sheets.getSummaryInformation();
        summaryInformation.setTitle("员工信息表");
        summaryInformation.setComments("本文由 javaBoy 提供");
        summaryInformation.setAuthor("javaBoy");

        // 标题单元格样式
        HSSFCellStyle headerStyle = sheets.createCellStyle();
        headerStyle.setFillForegroundColor(HSSFColor.HSSFColorPredefined.YELLOW.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        // 日期样式
        HSSFCellStyle dateStyle = sheets.createCellStyle();
        dateStyle.setDataFormat(HSSFDataFormat.getBuiltinFormat("m/d/yy"));

        HSSFSheet sheet = sheets.createSheet("员工信息表");
        // 设置列宽
        sheet.setColumnWidth(0, 5 * 256);
        sheet.setColumnWidth(1, 12 * 256);
        sheet.setColumnWidth(2, 10 * 256);
        sheet.setColumnWidth(3, 5 * 256);
        sheet.setColumnWidth(4, 12 * 256);
        sheet.setColumnWidth(5, 20 * 256);
        sheet.setColumnWidth(6, 10 * 256);
        sheet.setColumnWidth(7, 10 * 256);
        sheet.setColumnWidth(8, 16 * 256);
        sheet.setColumnWidth(9, 12 * 256);
        sheet.setColumnWidth(10, 15 * 256);
        sheet.setColumnWidth(11, 20 * 256);
        sheet.setColumnWidth(12, 16 * 256);
        sheet.setColumnWidth(13, 14 * 256);
        sheet.setColumnWidth(14, 14 * 256);
        sheet.setColumnWidth(15, 12 * 256);
        sheet.setColumnWidth(16, 8 * 256);
        sheet.setColumnWidth(17, 20 * 256);
        sheet.setColumnWidth(18, 20 * 256);
        sheet.setColumnWidth(19, 15 * 256);
        sheet.setColumnWidth(20, 8 * 256);
        sheet.setColumnWidth(21, 25 * 256);
        sheet.setColumnWidth(22, 14 * 256);
        sheet.setColumnWidth(23, 15 * 256);
        sheet.setColumnWidth(24, 15 * 256);

        // 第一行处理
        HSSFRow r0 = sheet.createRow(0);
        for (int i = 0; i < empHeaders.length ; i++) {
            HSSFCell c0 = r0.createCell(i);
            c0.setCellStyle(headerStyle);
            c0.setCellValue(empHeaders[i]);
        }

        // 处理数据
        int size = employeeList.size() + 1;
        int actualIndex = 0;
        for (int i = 1; i < size; i++,actualIndex++) {
            EmployeeBO emp = employeeList.get(actualIndex);
            HSSFRow row = sheet.createRow(i);
            row.createCell(0).setCellValue(emp.getId());
            row.createCell(1).setCellValue(emp.getName());
            row.createCell(2).setCellValue(emp.getWorkID());
            row.createCell(3).setCellValue(emp.getGender());
            HSSFCell cell4 = row.createCell(4);
            cell4.setCellStyle(dateStyle);
            if (emp.getBirthday() != null)
                cell4.setCellValue(emp.getBirthday());
            row.createCell(5).setCellValue(emp.getIdCard());
            row.createCell(6).setCellValue(emp.getWedlock());
            if (emp.getNation() != null)
            row.createCell(7).setCellValue(emp.getNation().getName());
            row.createCell(8).setCellValue(emp.getNativePlace());
            if (emp.getPoliticsstatus() != null)
            row.createCell(9).setCellValue(emp.getPoliticsstatus().getName());
            row.createCell(10).setCellValue(emp.getPhone());
            row.createCell(11).setCellValue(emp.getAddress());
            if (emp.getDepartment() != null)
            row.createCell(12).setCellValue(emp.getDepartment().getName());
            if (emp.getJobLevel() != null)
            row.createCell(13).setCellValue(emp.getJobLevel().getName());
            if (emp.getPosition() != null)
            row.createCell(14).setCellValue(emp.getPosition().getName());
            row.createCell(15).setCellValue(emp.getEngageForm());
            row.createCell(16).setCellValue(emp.getTiptopDegree());
            row.createCell(17).setCellValue(emp.getSpecialty());
            row.createCell(18).setCellValue(emp.getSchool());
            HSSFCell cell19 = row.createCell(19);
            cell19.setCellStyle(dateStyle);
            if (emp.getBeginDate() != null)
                cell19.setCellValue(emp.getBeginDate());
            row.createCell(20).setCellValue(emp.getWorkState());
            row.createCell(21).setCellValue(emp.getEmail());
            row.createCell(22).setCellValue(emp.getContractTerm());
            HSSFCell cell23 = row.createCell(23);
            cell23.setCellStyle(dateStyle);
            if (emp.getBeginContract() != null)
                cell23.setCellValue(emp.getBeginContract());
            HSSFCell cell24 = row.createCell(24);
            cell24.setCellStyle(dateStyle);
            if (emp.getEndContract() != null)
                cell24.setCellValue(emp.getEndContract());
            HSSFCell cell25 = row.createCell(25);
            cell25.setCellStyle(dateStyle);
            if (emp.getConversionTime() != null)
                cell25.setCellValue(emp.getConversionTime());
        }

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        HttpHeaders httpHeaders = new HttpHeaders();
        try {
            sheets.write(byteArrayOutputStream);
            httpHeaders.setContentDispositionFormData("attachment",new String("员工表.xls".getBytes("UTF-8"),"ISO-8859-1"));
            httpHeaders.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new ResponseEntity<byte[]>(byteArrayOutputStream.toByteArray(),httpHeaders, HttpStatus.CREATED);
    }

    public static List<EmployeeBO> Excel2Employee(MultipartFile file, List<NationEntity> nationList, List<PoliticsstatusEntity> politicsStatusList, List<DepartmentEntity> departmentList, List<JoblevelEntity> jobLevelList, List<PositionEntity> positionList) {
        LinkedList<EmployeeBO> linkedList = new LinkedList();
        EmployeeBO employee;
        try {
            // 1.创建一个 workbook 对象
            HSSFWorkbook sheets = new HSSFWorkbook(file.getInputStream());
            // 2.获取表单数量
            int numberOfSheets = sheets.getNumberOfSheets();
            for (int i = 0; i < numberOfSheets; i++) {
                // 3.获取表单
                HSSFSheet sheetAt = sheets.getSheetAt(i);
                // 4.获取行数
                int physicalNumberOfRows = sheetAt.getPhysicalNumberOfRows();
                // 5.跳过标题行
                for (int j = 1; j < physicalNumberOfRows; j++) {
                    // 6.获取第二行开始的数据并进行解析
                    HSSFRow row = sheetAt.getRow(j);
                    // 防止中间有空行
                    if (row == null) {
                        continue;
                    }
                    employee = new EmployeeBO();
                    int physicalNumberOfCells = row.getPhysicalNumberOfCells();
                    for (int k = 0; k < physicalNumberOfCells; k++) {
                        HSSFCell cell = row.getCell(k);
                        if (cell != null)
                        switch (cell.getCellType()) {
                            case STRING:
                                String cellValue = cell.getStringCellValue();
                                switch (k) {
                                    case 1:
                                        employee.setName(cellValue);
                                        break;
                                    case 2:
                                        employee.setWorkID(cellValue);
                                        break;
                                    case 3:
                                        employee.setGender(cellValue);
                                        break;
                                    case 5:
                                        employee.setIdCard(cellValue);
                                        break;
                                    case 6:
                                        employee.setWedlock(cellValue);
                                        break;
                                    case 7:
                                        int nationIndex = nationList.indexOf(new NationEntity(cellValue));
                                        employee.setNationId(nationList.get(nationIndex).getId());
                                        break;
                                    case 8:
                                        employee.setNativePlace(cellValue);
                                        break;
                                    case 9:
                                        int politicstatusIndex = politicsStatusList.indexOf(new PoliticsstatusEntity(cellValue));
                                        employee.setPoliticId(politicsStatusList.get(politicstatusIndex).getId());
                                        break;
                                    case 10:
                                        employee.setPhone(cellValue);
                                        break;
                                    case 11:
                                        employee.setAddress(cellValue);
                                        break;
                                    case 12:
                                        int departmentIndex = departmentList.indexOf(new DepartmentEntity(cellValue));
                                        employee.setDepartmentId(departmentList.get(departmentIndex).getId());
                                        break;
                                    case 13:
                                        int jobLevelIndex = jobLevelList.indexOf(new JoblevelEntity(cellValue));
                                        employee.setJobLevelId(jobLevelList.get(jobLevelIndex).getId());
                                        break;
                                    case 14:
                                        int positionIndex = positionList.indexOf(new PositionEntity(cellValue));
                                        employee.setPosId(positionList.get(positionIndex).getId());
                                        break;
                                    case 15:
                                        employee.setEngageForm(cellValue);
                                        break;
                                    case 16:
                                        employee.setTiptopDegree(cellValue);
                                        break;
                                    case 17:
                                        employee.setSpecialty(cellValue);
                                        break;
                                    case 18:
                                        employee.setSchool(cellValue);
                                        break;
                                    case 20:
                                        employee.setWorkState(cellValue);
                                        break;
                                    case 21:
                                        employee.setEmail(cellValue);
                                        break;
                                }
                                break;
                            default: {
                                switch (k) {
                                    case 4:
                                        employee.setBirthday(cell.getLocalDateTimeCellValue().toLocalDate());
                                        break;
                                    case 19:
                                        employee.setBeginDate(cell.getLocalDateTimeCellValue().toLocalDate());
                                        break;
                                    case 23:
                                        employee.setBeginContract(cell.getLocalDateTimeCellValue().toLocalDate());
                                        break;
                                    case 24:
                                        employee.setEndContract(cell.getLocalDateTimeCellValue().toLocalDate());
                                        break;
                                    case 22:
                                        employee.setContractTerm(cell.getNumericCellValue());
                                        break;
                                    case 25:
                                        employee.setConversionTime(cell.getLocalDateTimeCellValue().toLocalDate());
                                        break;
                                    case 2:
                                        employee.setWorkID(String.format("%08d",(int)cell.getNumericCellValue()));
                                        break;
                                }
                            }
                            break;
                        }
                    }
                    linkedList.add(employee);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return linkedList;
    }
}
