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
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.*;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Slf4j
public abstract class DiplomaService {

    @Value("${files.diploma_dir}")
    private String diplomaDir;
    @Value("${files.diploma_font}")
    private String diplomaFont;
    @Value("${files.diploma_border}")
    private String diplomaBorderSignature;

    public abstract String getImageDiplomaPattern();

    public StreamingResponseBody getDiplomas(MultipartFile inputFile) {

        log.info("input file with name {}", inputFile.getOriginalFilename());
        Set<String> users = parseFileToList(inputFile);

        Set<File> files = users.stream()
                .map(this::getFileDiploma)
                .collect(Collectors.toSet());

        return out -> {
            createZipWithDiplomas(files, out);
            cleanDiplomasDir();
        };
    }

    private void createZipWithDiplomas(Set<File> files, OutputStream out) throws IOException {
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


    private File getFileDiploma(String userName) {

        File fileDiploma = new File(String.format(diplomaDir + "/%s.pdf", userName));

        try {
            Document document = new Document(PageSize.A4);
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(fileDiploma));
            document.open();

            addBackground(writer);
            addBorderUserSignature(writer, userName);
            addUserSignature(writer, userName);

            document.close();
        } catch (Exception e) {
            log.error(e.getLocalizedMessage(), e);
        }
        return fileDiploma;
    }

    private void addBorderUserSignature(PdfWriter writer, String userName) throws DocumentException, IOException {

        PdfContentByte canvas = writer.getDirectContentUnder();
        Image image = Image.getInstance(diplomaBorderSignature);

        float widthBorderCalculated = 18.6F * userName.length();
        float widthBorder = widthBorderCalculated > 300 ? widthBorderCalculated : 300;

        Rectangle one = new Rectangle(widthBorder,71);
        image.scaleAbsolute(one);
        image.setAbsolutePosition((PageSize.A4.getWidth() - widthBorder)/2, 442);

        canvas.saveState();
        PdfGState state = new PdfGState();
        canvas.setGState(state);
        canvas.addImage(image);
        canvas.restoreState();
    }

    private void addUserSignature(PdfWriter writer, String userName) throws DocumentException {
        PdfContentByte canvas1 = writer.getDirectContent();
        ColumnText ct = new ColumnText(canvas1);
        ct.setSimpleColumn(0, 430, 595, 485);
        ct.setAlignment(Element.ALIGN_CENTER);

        Font font = getDiplomaFont();

        Chunk chunk = new Chunk(userName, font);
        ct.addText(chunk);
        ct.go();
    }

    private Font getDiplomaFont() {
        try {
            BaseFont bf = BaseFont.createFont(diplomaFont, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
            return new Font(bf, 31, Font.NORMAL);
        } catch (IOException | DocumentException e) {
            log.error(e.getLocalizedMessage(), e);
        }
        return new Font(Font.FontFamily.TIMES_ROMAN, 36, Font.ITALIC);
    }



    private void addBackground(PdfWriter writer) throws IOException, DocumentException {
        PdfContentByte canvas = writer.getDirectContentUnder();
        Image image = Image.getInstance(getImageDiplomaPattern());
        image.scaleAbsolute(PageSize.A4);
        image.setAbsolutePosition(0, 0);
        canvas.saveState();
        PdfGState state = new PdfGState();
        canvas.setGState(state);
        canvas.addImage(image);
        canvas.restoreState();
    }


    private Set<String> parseFileToList(MultipartFile inputFile) {
        Set<String> users = new HashSet<>();

        log.info("file parsing to list:");
        try (InputStream fileInputStream = inputFile.getInputStream()) {
            Workbook workbook = new XSSFWorkbook(fileInputStream);
            Sheet sheet = workbook.getSheetAt(0);

            for (Row row : sheet) {
                for (Cell cell : row) {
                    if (cell.getRichStringCellValue().length() == 0) break;
                    String username = cell.getRichStringCellValue().getString();
                    log.info(username);
                    users.add(username);
                    break;
                }
            }
            log.info("file parsed successfully!");
        } catch (IOException e) {
            log.error(e.getLocalizedMessage(), e);
        }
        return users;
    }

    public void cleanDiplomasDir() {
        File diplomaDirectory = new File(diplomaDir);
        File[] files = diplomaDirectory.listFiles();
        if (files != null) {
            Arrays.stream(files).forEach(File::delete);
        }
    }
}
