package io.salad109.medicalofficemanager.visits.internal

import io.salad109.medicalofficemanager.visits.VisitResponse
import org.openpdf.text.*
import org.openpdf.text.pdf.PdfPCell
import org.openpdf.text.pdf.PdfPTable
import org.openpdf.text.pdf.PdfWriter
import org.springframework.stereotype.Component
import java.awt.Color
import java.io.ByteArrayOutputStream
import java.time.format.DateTimeFormatter

@Component
class VisitPdfGenerator {

    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    private val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")

    fun generatePatientVisitReport(visits: List<VisitResponse>): ByteArray {
        if (visits.isEmpty()) {
            throw IllegalArgumentException("Cannot generate report with no visits")
        }

        val outputStream = ByteArrayOutputStream()
        val document = Document(PageSize.A4)
        PdfWriter.getInstance(document, outputStream)

        document.open()

        val firstVisit = visits.first()
        addHeader(document, firstVisit)

        addVisitHistoryTable(document, visits)

        addFooter(document, visits.size)

        document.close()

        return outputStream.toByteArray()
    }

    private fun addHeader(document: Document, visit: VisitResponse) {
        val titleFont = Font(Font.HELVETICA, 18f, Font.BOLD)
        val headerFont = Font(Font.HELVETICA, 12f, Font.BOLD)
        val normalFont = Font(Font.HELVETICA, 11f)

        val title = Paragraph("Medical Office Manager", titleFont)
        title.alignment = Element.ALIGN_CENTER
        document.add(title)

        val subtitle = Paragraph("Patient Visit History Report", headerFont)
        subtitle.alignment = Element.ALIGN_CENTER
        subtitle.spacingAfter = 20f
        document.add(subtitle)

        // Patient information
        val patientInfo = Paragraph()
        patientInfo.add(Chunk("Patient: ", headerFont))
        patientInfo.add(
            Chunk(
                "${visit.patientFirstName} ${visit.patientLastName} (ID: ${visit.patientId})",
                normalFont
            )
        )
        patientInfo.spacingAfter = 10f
        document.add(patientInfo)

        val generatedDate = Paragraph()
        generatedDate.add(Chunk("Report Generated: ", headerFont))
        generatedDate.add(Chunk(java.time.LocalDateTime.now().format(dateTimeFormatter), normalFont))
        generatedDate.spacingAfter = 20f
        document.add(generatedDate)
    }

    private fun addVisitHistoryTable(document: Document, visits: List<VisitResponse>) {
        val headerFont = Font(Font.HELVETICA, 12f, Font.BOLD)
        val cellFont = Font(Font.HELVETICA, 10f)

        val sectionTitle = Paragraph("Visit History", headerFont)
        sectionTitle.spacingAfter = 10f
        document.add(sectionTitle)

        val table = PdfPTable(5)
        table.widthPercentage = 100f
        table.setWidths(floatArrayOf(1.2f, 1f, 2f, 1.8f, 3f))

        addTableHeader(table, "Date", headerFont)
        addTableHeader(table, "Time", headerFont)
        addTableHeader(table, "Doctor", headerFont)
        addTableHeader(table, "Completed", headerFont)
        addTableHeader(table, "Notes", headerFont)

        visits.sortedByDescending { it.appointmentDate }
            .forEach { visit ->
                addTableCell(table, visit.appointmentDate.format(dateFormatter), cellFont)
                addTableCell(table, visit.appointmentTime.format(timeFormatter), cellFont)
                addTableCell(table, "Dr. ${visit.doctorFirstName} ${visit.doctorLastName}", cellFont)
                addTableCell(table, visit.completedAt?.format(dateTimeFormatter) ?: "N/A", cellFont)
                addTableCell(table, visit.notes ?: "N/A", cellFont)
            }

        document.add(table)
    }

    private fun addTableHeader(table: PdfPTable, text: String, font: Font) {
        val cell = PdfPCell(Phrase(text, font))
        cell.backgroundColor = Color(200, 200, 200)
        cell.horizontalAlignment = Element.ALIGN_CENTER
        cell.verticalAlignment = Element.ALIGN_MIDDLE
        cell.setPadding(8f)
        table.addCell(cell)
    }

    private fun addTableCell(table: PdfPTable, text: String, font: Font) {
        val cell = PdfPCell(Phrase(text, font))
        cell.setPadding(6f)
        cell.verticalAlignment = Element.ALIGN_TOP
        table.addCell(cell)
    }

    private fun addFooter(document: Document, visitCount: Int) {
        val footerFont = Font(Font.HELVETICA, 9f, Font.ITALIC)
        val footer = Paragraph()
        footer.spacingBefore = 30f
        footer.add(Chunk("Total Visits: $visitCount", footerFont))
        footer.alignment = Element.ALIGN_CENTER

        val disclaimer = Paragraph(
            "This document contains confidential medical information. " +
                    "Unauthorized disclosure is prohibited.",
            footerFont
        )
        disclaimer.alignment = Element.ALIGN_CENTER
        disclaimer.spacingBefore = 10f

        document.add(footer)
        document.add(disclaimer)
    }
}