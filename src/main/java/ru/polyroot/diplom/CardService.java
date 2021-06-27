package ru.polyroot.diplom;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
@Slf4j
public class CardService {

    @Value("${files.card_dir}")
    private String cardDir;
    @Value("${files.card_font}")
    private String cardFont;
    @Value("${files.pattern_card}")
    private String imageCardPattern;

    public StreamingResponseBody getCards(MultipartFile inputFile) {

        log.info("input file with name {}", inputFile.getOriginalFilename());
        List<User> users = parseFileToList(inputFile);

        List<File> files = users.stream()
                .map(this::getFileDiploma)
                .collect(Collectors.toList());

        return out -> {
            createZipWithDiplomas(files, out);
            cleanCardsDir();
        };
    }

    private void createZipWithDiplomas(List<File> files, OutputStream out) throws IOException {
        try (ZipOutputStream zipOutputStream = new ZipOutputStream(out)) {
            // package files
            for (File file : files) {
                //new zip entry and copying inputstream with file to zipOutputStream, after all closing streams
                zipOutputStream.putNextEntry(new ZipEntry(file.getName()));
                try (FileInputStream fileInputStream = new FileInputStream(file)) {
                    IOUtils.copy(fileInputStream, zipOutputStream);
                }
                zipOutputStream.closeEntry();
            }
        }
    }


    private File getFileDiploma(User user) {

        File fileDiploma = new File(String.format(cardDir + "/%s.pdf", user.getName()));

        try {
            Rectangle one = new Rectangle(1052,674);

            Document document = new Document(one);
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(fileDiploma));
            document.open();

            addBackground(writer);
            addUserSignature(writer, user.getName());
            addDate(writer, user.getDate());

            document.close();
        } catch (Exception e) {
            log.error(e.getLocalizedMessage(), e);
        }
        return fileDiploma;
    }

    private void addDate(PdfWriter writer, String date) throws DocumentException{
        PdfContentByte canvas1 = writer.getDirectContent();
        ColumnText ct = new ColumnText(canvas1);
        ct.setSimpleColumn(570, 80, 952, 110);
        ct.setAlignment(Element.ALIGN_CENTER);

        Font font = getDiplomaFont();

        Chunk chunk = new Chunk(date, font);
        ct.addText(chunk);
        ct.go();
    }

    private void addUserSignature(PdfWriter writer, String userName) throws DocumentException {
        PdfContentByte canvas1 = writer.getDirectContent();
        ColumnText ct = new ColumnText(canvas1);
        ct.setSimpleColumn(150, 220, 952, 255);
        ct.setAlignment(Element.ALIGN_LEFT);

        Font font = getDiplomaFont();

        Chunk chunk = new Chunk(userName, font);
        ct.addText(chunk);
        ct.go();
    }

    private Font getDiplomaFont() {
        try {
            BaseFont bf = BaseFont.createFont(cardFont, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
            return new Font(bf, 36, Font.NORMAL);
        } catch (IOException | DocumentException e) {
            log.error(e.getLocalizedMessage(), e);
        }
        return new Font(Font.FontFamily.TIMES_ROMAN, 36, Font.ITALIC);
    }



    private void addBackground(PdfWriter writer) throws IOException, DocumentException {
        PdfContentByte canvas = writer.getDirectContentUnder();
        Image image = Image.getInstance(imageCardPattern);
        image.setAbsolutePosition(0, 0);
        canvas.saveState();
        PdfGState state = new PdfGState();
        canvas.setGState(state);
        canvas.addImage(image);
        canvas.restoreState();
    }


    private List<User> parseFileToList(MultipartFile inputFile) {
        List<User> users = new ArrayList<>();

        log.info("file parsing to list:");
        try (InputStream fileInputStream = inputFile.getInputStream()) {
            Workbook workbook = new XSSFWorkbook(fileInputStream);
            Sheet sheet = workbook.getSheetAt(0);

            for (Row row : sheet) {
                User user = null;
                int cellNumber = 0;
                for (Cell cell : row) {
                    cellNumber++;
                    if (cell.getRichStringCellValue().length() == 0 || cellNumber == 3) break;
                    String cellValue = cell.getRichStringCellValue().getString();
                    log.info(cellValue);

                    if (cellNumber == 1) {
                        user = new User();
                        user.setName(cellValue);
                        users.add(user);
                    }
                    if (cellNumber == 2) user.setDate(cellValue);
                }
            }
            log.info("file parsed successfully!");
        } catch (IOException e) {
            log.error(e.getLocalizedMessage(), e);
        }
        return users;
    }

    public void cleanCardsDir() {
        File diplomaDirectory = new File(cardDir);
        File[] files = diplomaDirectory.listFiles();
        if (files != null) {
            Arrays.stream(files).forEach(File::delete);
        }
    }
}
