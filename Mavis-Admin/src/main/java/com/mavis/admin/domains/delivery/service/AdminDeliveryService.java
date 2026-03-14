package com.mavis.admin.domains.delivery.service;

import com.mavis.admin.common.page.PageResponse;
import com.mavis.admin.domains.delivery.dto.AdminCompleteDeliveryRequest;
import com.mavis.admin.domains.delivery.dto.GetAdminDeliveryResponse;
import com.mavis.admin.domains.order.dto.AdminDeliveryStartRequest;
import com.mavis.admin.domains.order.dto.GetAdminOrderExcelResponse;
import com.mavis.admin.domains.order.dto.OrderItemExcelInfo;
import com.mavis.admin.domains.order.dto.OrderItemInfo;
import com.mavis.common.annotation.ExcelColumn;
import com.mavis.domain.domains.delivery.domain.Delivery;
import com.mavis.domain.domains.delivery.domain.DeliveryStatus;
import com.mavis.domain.domains.delivery.exception.DeliveryCannotBeCompleteException;
import com.mavis.domain.domains.delivery.exception.DeliveryNotFoundException;
import com.mavis.domain.domains.delivery.repository.DeliveryRepository;
import com.mavis.domain.domains.order.domain.Order;
import com.mavis.domain.domains.order.domain.OrderAddress;
import com.mavis.domain.domains.order.domain.OrderItem;
import com.mavis.domain.domains.order.domain.OrderStatus;
import com.mavis.domain.domains.order.exception.OrderNotToBeConfirmedException;
import com.mavis.domain.domains.order.implement.OrderReader;
import com.mavis.domain.domains.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminDeliveryService {

    private final DeliveryRepository deliveryRepository;
    private final OrderReader orderReader;
    private static final DateTimeFormatter EXCEL_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd(E)", Locale.KOREAN);

    @Transactional(readOnly = true)
    public PageResponse<GetAdminDeliveryResponse> getAdminDeliveryLists(Pageable pageable, DeliveryStatus deliveryStatus) {
        Page<Delivery> deliveryPages = deliveryRepository.findDeliveryPagesByDeliveryStatus(pageable, deliveryStatus);
        Page<GetAdminDeliveryResponse> deliveryResponses = deliveryPages.map(delivery -> {
                    Order order = delivery.getOrder();
                    OrderAddress orderAddress = order.getOrderAddress();
                    User user = order.getUser();
                    List<OrderItem> orderItems = order.getOrderItems();
                    List<OrderItemInfo> orderItemInfoList = orderItems.stream().map(
                            orderItem -> OrderItemInfo.builder()
                                    .productName(orderItem.getProduct().getName())
                                    .color(orderItem.getColor())
                                    .quantity(orderItem.getQuantity())
                                    .build()
                    ).toList();
                    return GetAdminDeliveryResponse.from(delivery, order, orderAddress, orderItemInfoList, user);
                }
        );
        return PageResponse.of(deliveryResponses);
    }

    public byte[] getOrderByExcel(Pageable pageable) {
        try (
                SXSSFWorkbook workbook = new SXSSFWorkbook();
                ByteArrayOutputStream out = new ByteArrayOutputStream()
        ) {
            Page<Delivery> deliveryPages = deliveryRepository.findDeliveryPagesByDeliveryStatus(pageable, DeliveryStatus.READY);
            Page<GetAdminOrderExcelResponse> deliveryExcelResponses = deliveryPages.map(delivery -> {
                        Order order = delivery.getOrder();
                        OrderAddress orderAddress = order.getOrderAddress();
                        User user = order.getUser();
                        List<OrderItem> orderItems = order.getOrderItems();
                        List<OrderItemExcelInfo> orderItemInfoList = orderItems.stream()
                                .map(orderItem -> OrderItemExcelInfo.from(orderItem.getProduct().getName(), orderItem.getColor(), orderItem.getQuantity()))
                                .toList();

                        String orderedAt = order.getCreatedAt().format(EXCEL_DATE_FORMATTER);
                        return GetAdminOrderExcelResponse.from(order, orderAddress, orderItemInfoList, user, orderedAt);
                    }
            );

            Sheet sheet = workbook.createSheet("배송목록");

            createHeader(sheet, workbook);
            createBody(sheet, deliveryExcelResponses.getContent());

            workbook.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException("엑셀 생성 실패", e);
        }
    }

    private void createHeader(Sheet sheet, Workbook workbook) {
        Row header = sheet.createRow(0);
        header.setHeightInPoints(header.getHeightInPoints() * 1.2f);
        CellStyle headerStyle = createHeaderStyle(workbook);

        AtomicInteger index = new AtomicInteger(0);

        Arrays.stream(GetAdminOrderExcelResponse.class.getDeclaredFields())
                .forEach(field -> {
                    if (List.class.isAssignableFrom(field.getType())) {
                        Arrays.stream(OrderItemExcelInfo.class.getDeclaredFields())
                                .filter(f -> f.isAnnotationPresent(ExcelColumn.class))
                                .forEach(f -> {
                                    String headerName = f.getAnnotation(ExcelColumn.class).header();
                                    Cell cell = header.createCell(index.getAndIncrement());
                                    cell.setCellValue(headerName);
                                    cell.setCellStyle(headerStyle);
                                });
                        return;
                    }

                    if (!field.isAnnotationPresent(ExcelColumn.class)) {
                        return;
                    }

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

        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);

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
        CellStyle bodyStyle = createBodyStyle(sheet.getWorkbook());

        for (GetAdminOrderExcelResponse data : orders) {
            Row row = sheet.createRow(rowIndex++);
            row.setHeightInPoints(row.getHeightInPoints() * 1.1f);

            int colIndex = 0;
            for (Field field : GetAdminOrderExcelResponse.class.getDeclaredFields()) {
                field.setAccessible(true);
                try {
                    Object value = field.get(data);

                    if (value instanceof List<?> list) {
                        List<OrderItemExcelInfo> items = (List<OrderItemExcelInfo>) list;

                        String productNames = items.stream()
                                .map(OrderItemExcelInfo::productName)
                                .collect(Collectors.joining("\n"));
                        String optionQuantities = items.stream()
                                .map(OrderItemExcelInfo::optionQuantity)
                                .collect(Collectors.joining("\n"));

                        writeCell(row, colIndex++, productNames, bodyStyle);
                        writeCell(row, colIndex++, optionQuantities, bodyStyle);
                        continue;
                    }

                    writeCell(row, colIndex++, value == null ? "" : value.toString(), bodyStyle);
                } catch (Exception e) {
                    throw new IllegalStateException("Failed to generate excel body", e);
                }
            }
        }

        int totalCols = 0;
        for (Field field : GetAdminOrderExcelResponse.class.getDeclaredFields()) {
            if (List.class.isAssignableFrom(field.getType())) totalCols += 2;
            else if (field.isAnnotationPresent(ExcelColumn.class)) totalCols += 1;
        }

        finalizeSheetLayout(sheet, totalCols);
    }

    private CellStyle createBodyStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setWrapText(true);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);

        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);

        Font font = workbook.createFont();
        font.setFontName("Arial");
        font.setFontHeightInPoints((short) 10);
        style.setFont(font);

        return style;
    }

    private void writeCell(Row row, int colIndex, String value, CellStyle style) {
        Cell cell = row.createCell(colIndex);
        cell.setCellValue(value);
        cell.setCellStyle(style);
    }

    private void finalizeSheetLayout(Sheet sheet, int columnCount) {
        for (int i = 0; i < columnCount; i++) {
            sheet.setColumnWidth(i, 6000);
        }
        sheet.createFreezePane(0, 1);
    }

    @Transactional
    public void confirmOrderDelivery(Long orderId, AdminDeliveryStartRequest request) {
        Order order = orderReader.findOrderById(orderId);
        if (order.getOrderStatus() != OrderStatus.ORDERED) {
            throw OrderNotToBeConfirmedException.EXCEPTION;
        }

        Delivery delivery = deliveryRepository.findByOrderAndIsDeletedFalse(order)
                .orElseThrow(() -> DeliveryNotFoundException.EXCEPTION);
        delivery.startDelivery(request.carrier(), request.trackingNumber());
    }

    @Transactional
    public void completeDelivery(AdminCompleteDeliveryRequest request) {
        List<Long> deliverIds = request.deliverIds();
        List<Delivery> deliveries = deliveryRepository.findByIdInAndIsDeletedFalse(deliverIds);
        deliveries.forEach(delivery -> {
            if (delivery.getDeliveryStatus() != DeliveryStatus.SHIPPED) {
                throw DeliveryCannotBeCompleteException.EXCEPTION;
            }
            delivery.complete();
        });
    }
}
