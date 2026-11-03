package uz.davrbank.compliance.report

import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uz.davrbank.compliance.dto.response.transfer.TransferComplianceCheckResponse
import uz.davrbank.compliance.enums.GoodsType
import uz.davrbank.compliance.enums.TransferComplianceStatus
import uz.davrbank.compliance.util.DictionaryMemoryStore
import java.io.ByteArrayInputStream
import java.util.UUID

class TransferComplianceCheckReportGeneratorTest {

    private val generator = TransferComplianceCheckReportGenerator(DictionaryMemoryStore())

    @Test
    fun `goods type column is rendered and keeps later columns aligned`() {
        val rows = listOf(
            response(GoodsType.PRODUCT, TransferComplianceStatus.APPROVED),
            response(GoodsType.SERVICE, TransferComplianceStatus.REJECTED),
            response(null, TransferComplianceStatus.NEW),
        )

        XSSFWorkbook(ByteArrayInputStream(generator.generate(rows))).use { workbook ->
            val sheet = workbook.getSheetAt(0)
            val header = sheet.getRow(0)

            assertThat(header.getCell(GOODS_TYPE_COLUMN).stringCellValue).isEqualTo("Тип (товар/услуга)")
            assertThat(header.getCell(GOODS_NAME_COLUMN).stringCellValue).isEqualTo("Наименование товара")
            assertThat(header.getCell(DECISION_COLUMN).stringCellValue).isEqualTo("Решение")

            assertThat(sheet.getRow(1).getCell(GOODS_TYPE_COLUMN).stringCellValue).isEqualTo("Товар")
            assertThat(sheet.getRow(2).getCell(GOODS_TYPE_COLUMN).stringCellValue).isEqualTo("Услуга")
            assertThat(sheet.getRow(3).getCell(GOODS_TYPE_COLUMN).stringCellValue).isEmpty()

            assertThat(sheet.getRow(1).getCell(DECISION_COLUMN).stringCellValue).isEqualTo("Одобрено")
            assertThat(sheet.getRow(2).getCell(DECISION_COLUMN).stringCellValue).isEqualTo("Отклонено")

            (1..rows.size).forEach { rowIndex ->
                assertThat(sheet.getRow(rowIndex).physicalNumberOfCells)
                    .isEqualTo(header.physicalNumberOfCells)
            }
        }
    }

    private fun response(
        goodsType: GoodsType?,
        status: TransferComplianceStatus,
    ): TransferComplianceCheckResponse = TransferComplianceCheckResponse(
        id = UUID.randomUUID().toString(),
        checkNumber = "TR_COM-1",
        externalId = UUID.randomUUID().toString(),
        branchCode = "00001",
        clientName = "Client",
        contractTypeCode = "01",
        contractNumber = "C-1",
        goodsType = goodsType,
        goodsName = "Goods",
        status = status,
    )

    companion object {
        private const val GOODS_TYPE_COLUMN = 18
        private const val GOODS_NAME_COLUMN = 19
        private const val DECISION_COLUMN = 28
    }
}
