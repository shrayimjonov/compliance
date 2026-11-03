package uz.davrbank.compliance.report

import org.apache.poi.ss.usermodel.BorderStyle
import org.apache.poi.ss.usermodel.FillPatternType
import org.apache.poi.ss.usermodel.HorizontalAlignment
import org.apache.poi.ss.usermodel.IndexedColors
import org.apache.poi.ss.usermodel.VerticalAlignment
import org.apache.poi.ss.util.CellRangeAddress
import org.apache.poi.xssf.streaming.SXSSFWorkbook
import org.apache.poi.xssf.usermodel.XSSFCellStyle
import org.apache.poi.xssf.usermodel.XSSFColor
import org.springframework.stereotype.Component
import uz.davrbank.compliance.dto.response.transfer.TransferComplianceCheckResponse
import uz.davrbank.compliance.enums.GoodsType
import uz.davrbank.compliance.enums.TransferComplianceStatus
import uz.davrbank.compliance.util.DictionaryMemoryStore
import java.io.ByteArrayOutputStream
import java.time.format.DateTimeFormatter

@Component
class TransferComplianceCheckReportGenerator(
    private val dictionaryMemoryStore: DictionaryMemoryStore,
) {

    fun generate(rows: List<TransferComplianceCheckResponse>): ByteArray {
        val workbook = SXSSFWorkbook(ROW_ACCESS_WINDOW)
        try {
            val sheet = workbook.createSheet(SHEET_NAME)

            val headerFont = workbook.createFont().apply {
                bold = true
                fontHeightInPoints = HEADER_FONT_SIZE
                color = IndexedColors.WHITE.index
                fontName = FONT_NAME
            }
            val bodyFont = workbook.createFont().apply {
                fontHeightInPoints = BODY_FONT_SIZE
                fontName = FONT_NAME
            }

            val headerStyle = filledStyle(workbook, HEADER_FILL_RGB).apply {
                setFont(headerFont)
                alignment = HorizontalAlignment.CENTER
                verticalAlignment = VerticalAlignment.CENTER
                wrapText = true
            }
            val bodyStyle = bodyStyle(workbook, bodyFont, HorizontalAlignment.LEFT)
            val centeredBodyStyle = bodyStyle(workbook, bodyFont, HorizontalAlignment.CENTER)
            val decisionApproved = filledBodyStyle(workbook, bodyFont, APPROVED_FILL_RGB)
            val decisionRejected = filledBodyStyle(workbook, bodyFont, REJECTED_FILL_RGB)
            val decisionOther = filledBodyStyle(workbook, bodyFont, OTHER_FILL_RGB)

            fun decisionStyle(status: TransferComplianceStatus): XSSFCellStyle = when (status) {
                TransferComplianceStatus.APPROVED -> decisionApproved
                TransferComplianceStatus.REJECTED -> decisionRejected
                else -> decisionOther
            }

            COLUMN_WIDTHS.forEachIndexed { index, chars -> sheet.setColumnWidth(index, (chars * 256).toInt()) }

            val headerRow = sheet.createRow(0).apply { heightInPoints = HEADER_ROW_HEIGHT }
            HEADERS.forEachIndexed { index, title ->
                headerRow.createCell(index).apply {
                    setCellValue(title)
                    cellStyle = headerStyle
                }
            }

            rows.forEachIndexed { rowIndex, item ->
                val row = sheet.createRow(rowIndex + 1).apply { heightInPoints = BODY_ROW_HEIGHT }
                cellValues(rowIndex + 1, item).forEachIndexed { colIndex, value ->
                    row.createCell(colIndex).apply {
                        setCellValue(value.truncateForCell())
                        cellStyle = when (colIndex) {
                            DECISION_COLUMN -> decisionStyle(item.status)
                            in CENTERED_COLUMNS -> centeredBodyStyle
                            else -> bodyStyle
                        }
                    }
                }
            }

            sheet.createFreezePane(0, 1)
            if (rows.isNotEmpty()) {
                sheet.setAutoFilter(CellRangeAddress(0, rows.size, 0, HEADERS.size - 1))
            }

            ByteArrayOutputStream(INITIAL_BUFFER_SIZE).use { output ->
                workbook.write(output)
                return output.toByteArray()
            }
        } finally {
            workbook.dispose()
        }
    }

    private fun cellValues(rowNumber: Int, item: TransferComplianceCheckResponse): List<String> = listOf(
        rowNumber.toString(),                                                   // №
        item.createdDate?.format(DATE_TIME_FORMATTER).orEmpty(),                // Дата запроса
        dictionaryMemoryStore.branchName(item.branchCode),                      // Филиал
        item.clientName,                                                        // Наименование клиента
        item.clientTin.orEmpty(),                                               // ИНН клиента
        item.enterpriseRegistrationDate?.format(DATE_FORMATTER).orEmpty(),      // Дата регист. предпр.
        item.activityType.orEmpty(),                                            // Вид деятельности
        dictionaryMemoryStore.contractTypeName(item.contractTypeCode),          // Тип контракта
        item.contractNumber,                                                    // Номер контракта
        item.contractDate?.format(DATE_FORMATTER).orEmpty(),                    // Дата контракта
        item.contractIdn.orEmpty(),                                             // ИДН контракта
        item.foreignPartnerName.orEmpty(),                                      // Наименование инопартнера
        item.foreignPartnerTin.orEmpty(),                                       // ИНН инопартнера
        item.directorAndRelatedParties.orEmpty(),                               // Директор компании и связанные лица
        dictionaryMemoryStore.countryName(item.foreignPartnerCountryCode),      // Страна инопартнера
        item.foreignPartnerBank.orEmpty(),                                      // Банк инопартнера
        item.bicSwift.orEmpty(),                                                // BIC/SWIFT
        dictionaryMemoryStore.countryName(item.foreignPartnerBankCountryCode),  // Страна банка инопартнера
        item.goodsType.label(),                                                 // Тип (товар/услуга)
        item.goodsName.orEmpty(),                                               // Наименование товара
        item.goodsTnvedCode.orEmpty(),                                          // Код ТНВЭД товара
        dictionaryMemoryStore.countryName(item.goodsOriginCountryCode),         // Происхождение товара
        item.consignor.orEmpty(),                                               // Грузо-отправитель
        item.consignee.orEmpty(),                                               // Грузо-получатель
        dictionaryMemoryStore.currencyName(item.currencyCode),                  // Валюта контракта
        item.operationAmount?.toPlainString().orEmpty(),                        // Сумма операции для проверки
        item.lastOperationDate?.format(DATE_FORMATTER).orEmpty(),               // Дата последней операции
        item.feaOfficerFullName.orEmpty(),                                      // Ф.И.О. сотрд ВЭД
        item.status.label(),                                                    // Решение
        item.checkedDate?.format(DATE_TIME_FORMATTER).orEmpty(),                // Дата ответа
        item.decisionNote.orEmpty(),                                            // Доп к решению
        item.checkedBy.orEmpty(),                                               // Ф.И.О. сотрд СК
        item.description.orEmpty(),                                             // Описание
    )

    private fun TransferComplianceStatus.label(): String = when (this) {
        TransferComplianceStatus.NEW -> "Новый"
        TransferComplianceStatus.SENT -> "Отправлено"
        TransferComplianceStatus.CHECKED -> "Проверено"
        TransferComplianceStatus.APPROVED -> "Одобрено"
        TransferComplianceStatus.REJECTED -> "Отклонено"
        TransferComplianceStatus.CHECK_ERROR -> "Ошибка проверки"
    }

    private fun GoodsType?.label(): String = when (this) {
        GoodsType.PRODUCT -> "Товар"
        GoodsType.SERVICE -> "Услуга"
        null -> ""
    }

    private fun String.truncateForCell(): String =
        if (length <= MAX_CELL_LENGTH) this else substring(0, MAX_CELL_LENGTH)

    private fun bodyStyle(
        workbook: SXSSFWorkbook,
        font: org.apache.poi.ss.usermodel.Font,
        horizontal: HorizontalAlignment,
    ): XSSFCellStyle = (workbook.createCellStyle() as XSSFCellStyle).apply {
        setFont(font)
        verticalAlignment = VerticalAlignment.CENTER
        alignment = horizontal
        wrapText = true
        applyBorder()
    }

    private fun filledBodyStyle(
        workbook: SXSSFWorkbook,
        font: org.apache.poi.ss.usermodel.Font,
        rgb: ByteArray,
    ): XSSFCellStyle = filledStyle(workbook, rgb).apply {
        setFont(font)
        verticalAlignment = VerticalAlignment.CENTER
        alignment = HorizontalAlignment.LEFT
        wrapText = true
    }

    private fun filledStyle(workbook: SXSSFWorkbook, rgb: ByteArray): XSSFCellStyle =
        (workbook.createCellStyle() as XSSFCellStyle).apply {
            setFillForegroundColor(XSSFColor(rgb, null))
            fillPattern = FillPatternType.SOLID_FOREGROUND
            applyBorder()
        }

    private fun XSSFCellStyle.applyBorder() {
        borderTop = BorderStyle.THIN
        borderBottom = BorderStyle.THIN
        borderLeft = BorderStyle.THIN
        borderRight = BorderStyle.THIN
    }

    companion object {
        private const val SHEET_NAME = "ТИФ проверка"
        private const val ROW_ACCESS_WINDOW = 200
        private const val INITIAL_BUFFER_SIZE = 64 * 1024
        private const val MAX_CELL_LENGTH = 32_767

        private const val FONT_NAME = "Calibri"
        private const val HEADER_FONT_SIZE: Short = 15
        private const val BODY_FONT_SIZE: Short = 14
        private const val HEADER_ROW_HEIGHT = 72.75f
        private const val BODY_ROW_HEIGHT = 23.25f

        private const val DECISION_COLUMN = 28

        private val HEADER_FILL_RGB = byteArrayOf(0x5B, 0x9B.toByte(), 0xD5.toByte())
        private val APPROVED_FILL_RGB = byteArrayOf(0x92.toByte(), 0xD0.toByte(), 0x50)
        private val REJECTED_FILL_RGB = byteArrayOf(0xFF.toByte(), 0x00, 0x00)
        private val OTHER_FILL_RGB = byteArrayOf(0xFF.toByte(), 0xFF.toByte(), 0x00)

        private val CENTERED_COLUMNS = setOf(0, 1, 5, 9, 18, 24, 26, 29)

        private val DATE_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
        private val DATE_TIME_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")

        private val HEADERS = listOf(
            "№",
            "Дата запроса",
            "Филиал",
            "Наименование клиента",
            "ИНН клиента",
            "Дата регист. предпр.",
            "Вид деятельности",
            "Тип контракта",
            "Номер контракта",
            "Дата контракта",
            "ИДН контракта",
            "Наименование инопартнера",
            "ИНН инопартнера",
            "Директор компании и связонные лица",
            "Страна инопартнера",
            "Банк инопартнера",
            "BIC/SWIFT",
            "Страна банка инопартнера",
            "Тип (товар/услуга)",
            "Наименование товара",
            "Код ТНВЭД товара",
            "Происхождение товара",
            "Грузо-отправитель",
            "Грузо-получатель",
            "Валюта контракта",
            "Сумма операции для проверки",
            "Дата последной операции",
            "Ф.И.О. сотрд ВЭД",
            "Решение",
            "Дата ответа",
            "Доп к решению",
            "Ф.И.О. сотрд СК",
            "Описание",
        )

        private val COLUMN_WIDTHS = listOf(
            9.14, 18.14, 26.86, 40.29, 22.57, 18.71, 18.43, 24.14, 28.14, 25.43,
            40.86, 42.86, 29.0, 56.14, 32.29, 29.43, 18.43, 40.71, 20.0, 34.71,
            32.57, 36.29, 41.71, 32.57, 29.0, 46.43, 40.0, 27.86, 17.0, 20.71,
            25.86, 25.86, 18.29,
        )
    }
}
