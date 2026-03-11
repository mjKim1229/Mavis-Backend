package com.mavis.admin.domains.delivery.service;

import com.mavis.admin.common.page.PageResponse;
import com.mavis.admin.domains.delivery.dto.AdminCompleteDeliveryRequest;
import com.mavis.admin.domains.delivery.dto.GetAdminDeliveryResponse;
import com.mavis.admin.domains.order.dto.AdminDeliveryStartRequest;
import com.mavis.admin.domains.order.dto.GetAdminOrderExcelResponse;
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
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@RequiredArgsConstructor
public class AdminDeliveryService {

    private final DeliveryRepository deliveryRepository;
    private final OrderReader orderReader;

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
