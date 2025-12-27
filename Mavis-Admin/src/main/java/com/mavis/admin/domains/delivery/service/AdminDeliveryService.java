package com.mavis.admin.domains.delivery.service;

import com.mavis.admin.domains.order.dto.GetAdminOrderExcelResponse;
import com.mavis.admin.domains.order.dto.UpdateAdminOrderConfirmRequest;
import com.mavis.common.annotation.ExcelColumn;
import com.mavis.domain.domains.delivery.domain.Delivery;
import com.mavis.domain.domains.delivery.repository.DeliveryRepository;
import com.mavis.domain.domains.order.domain.Order;
import com.mavis.domain.domains.order.domain.OrderStatus;
import com.mavis.domain.domains.order.exception.OrderNotToBeConfirmedException;
import com.mavis.domain.domains.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@RequiredArgsConstructor
public class AdminDeliveryService {

    private final DeliveryRepository deliveryRepository;
    private final OrderRepository orderRepository;

    public byte[] getOrderByExcel(Pageable pageable) {
        try (
                SXSSFWorkbook workbook = new SXSSFWorkbook();
                ByteArrayOutputStream out = new ByteArrayOutputStream()
        ) {
            Sheet sheet = workbook.createSheet("배송목록");
            CellStyle style = workbook.createCellStyle();
            style.setBorderTop(BorderStyle.THIN);
            style.setBorderBottom(BorderStyle.THIN);
            style.setBorderLeft(BorderStyle.THIN);
            style.setBorderRight(BorderStyle.THIN);

            style.setTopBorderColor(IndexedColors.BLACK.getIndex());
            style.setBottomBorderColor(IndexedColors.BLACK.getIndex());
            style.setLeftBorderColor(IndexedColors.BLACK.getIndex());
            style.setRightBorderColor(IndexedColors.BLACK.getIndex());
            createHeader(sheet, workbook);
            //createBody(sheet, orderPages.getContent());
            workbook.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException("엑셀 생성 실패", e);
        }
    }

    private void createHeader(Sheet sheet, Workbook workbook) {
        Row header = sheet.createRow(0);
        CellStyle headerStyle = createHeaderStyle(workbook);

        AtomicInteger index = new AtomicInteger(0);
        Arrays.stream(GetAdminOrderExcelResponse.class.getDeclaredFields())
                .forEach(field -> {
                    String headerName = field.getAnnotation(ExcelColumn.class).header();
                    Cell cell = header.createCell(index.getAndIncrement());
                    cell.setCellValue(headerName);
                    cell.setCellStyle(headerStyle);
                });
    }

    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();

        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.BLACK.getIndex());
        style.setFont(font);

        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);

        style.setTopBorderColor(IndexedColors.BLACK.getIndex());
        style.setBottomBorderColor(IndexedColors.BLACK.getIndex());
        style.setLeftBorderColor(IndexedColors.BLACK.getIndex());
        style.setRightBorderColor(IndexedColors.BLACK.getIndex());

        return style;
    }

    private void createBody(Sheet sheet, List<GetAdminOrderExcelResponse> orders) {
        int rowIndex = 1;

        for (GetAdminOrderExcelResponse data : orders) {
            Row row = sheet.createRow(rowIndex++);
            int colIndex = 0;

            for (Field field : GetAdminOrderExcelResponse.class.getDeclaredFields()) {
                field.setAccessible(true);

                Object value;
                try {
                    value = field.get(data);
                } catch (Exception e) {
                    throw new IllegalStateException("액셀 필드 실패");
                }

                Cell cell = row.createCell(colIndex++);
                cell.setCellValue(value == null ? "" : value.toString());
            }
        }
    }

    @Transactional
    public void confirmOrderDelivery(UpdateAdminOrderConfirmRequest request) {
        List<Long> orderIds = request.orderIds();
        List<Order> orders = orderRepository.findByIdInAndIsDeletedFalse(orderIds);
        orders.forEach(order -> {
            if (order.getOrderStatus() != OrderStatus.ORDERED) {
                throw OrderNotToBeConfirmedException.EXCEPTION;
            }
            order.confirm();
            Delivery delivery = Delivery.builder()
                    .order(order)
                    .build();
            deliveryRepository.save(delivery);
        });
    }
}
