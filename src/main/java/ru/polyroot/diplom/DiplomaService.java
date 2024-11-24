package ru.polyroot.diplom;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.*;
import java.nio.file.Files;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static ru.polyroot.diplom.Utils.*;

@Slf4j
public class DiplomaService {

    protected final String imageDiplomaPattern;
    private final String signatureBorderPath;
    private final Font signatureFont;

    private final static String DIPLOMA_DIR = "filesDir";

    public DiplomaService(String signatureFontPath,
                          String signatureBorderPath,
                          String imageDiplomaPattern) {
        this.signatureBorderPath = signatureBorderPath;
        this.signatureFont = initializeFont(signatureFontPath);
        this.imageDiplomaPattern = imageDiplomaPattern;
    }

    private Font initializeFont(String diplomaFontPath) {
        try {
            String absolutePath = getAbsolutePath(diplomaFontPath);
            BaseFont baseFont = BaseFont.createFont(absolutePath, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
            return new Font(baseFont, 31, Font.NORMAL);
        } catch (IOException | DocumentException e) {
            log.error("Error getting diploma font", e);
        }
        return new Font(Font.FontFamily.TIMES_ROMAN, 36, Font.ITALIC);
    }

    public StreamingResponseBody getDiplomas(MultipartFile inputFile) {
        log.info("Input file with name {}", inputFile.getOriginalFilename());
        Set<String> users = parseFileToList(inputFile);
        Set<File> files = users.stream()
                .map(this::getFileDiploma)
                .collect(Collectors.toSet());

        return out -> {
            createZipWithDiplomas(files, out);
            cleanTempDir(DIPLOMA_DIR);
        };
    }

    private void createZipWithDiplomas(Set<File> files, OutputStream out) throws IOException {
        try (ZipOutputStream zipOutputStream = new ZipOutputStream(out)) {
            for (File file : files) {
                zipOutputStream.putNextEntry(new ZipEntry(file.getName()));
                Files.copy(file.toPath(), zipOutputStream);
                zipOutputStream.closeEntry();
            }
        }
    }


    private File getFileDiploma(String userName) {
        if (userName == null || userName.isEmpty()) {
            throw new IllegalArgumentException("User name cannot be null or empty");
        }

        File fileDiploma = createTempFile(DIPLOMA_DIR, userName + ".pdf");

        try (FileOutputStream fileOutputStream = new FileOutputStream(fileDiploma)) {
            Document document = new Document(PageSize.A4);
            PdfWriter writer = PdfWriter.getInstance(document, fileOutputStream);
            document.open();

            addBackground(writer);
            addBorderUserSignature(writer, userName);
            addUserSignature(writer, userName);

            document.close();
        } catch (Exception e) {
            log.error("Error creating diploma for user '{}'", userName, e);
        }

        return fileDiploma;
    }

    private void addBorderUserSignature(PdfWriter writer, String userName) throws DocumentException, IOException {
        PdfContentByte canvas = writer.getDirectContentUnder();
        Image image = Image.getInstance(signatureBorderPath);

        float widthBorderCalculated = 18.6F * userName.length();
        float widthBorder = Math.max(widthBorderCalculated, 300);

        Rectangle rectangle = new Rectangle(widthBorder, 71);
        image.scaleAbsolute(rectangle);
        image.setAbsolutePosition((PageSize.A4.getWidth() - widthBorder) / 2, 442);

        canvas.saveState();
        PdfGState state = new PdfGState();
        canvas.setGState(state);
        canvas.addImage(image);
        canvas.restoreState();
    }

    private void addUserSignature(PdfWriter writer, String userName) throws DocumentException {
        PdfContentByte canvas = writer.getDirectContent();
        ColumnText columnText = new ColumnText(canvas);
        columnText.setSimpleColumn(0, 430, PageSize.A4.getWidth(), 485);
        columnText.setAlignment(Element.ALIGN_CENTER);

        Chunk chunk = new Chunk(userName, signatureFont);
        columnText.addText(chunk);
        columnText.go();
    }

    private void addBackground(PdfWriter writer) throws IOException, DocumentException {
        PdfContentByte canvas = writer.getDirectContentUnder();
        Image image = Image.getInstance(imageDiplomaPattern);
        image.scaleAbsolute(new Rectangle(PageSize.A4.getWidth(), PageSize.A4.getHeight()));
        image.setAbsolutePosition(0, 0);
        canvas.saveState();
        PdfGState state = new PdfGState();
        canvas.setGState(state);
        canvas.addImage(image);
        canvas.restoreState();
    }


    private Set<String> parseFileToList(MultipartFile inputFile) {
        Set<String> users = new LinkedHashSet<>();
        log.info("Parsing file to list...");

        try (InputStream fileInputStream = inputFile.getInputStream();
             Workbook workbook = new XSSFWorkbook(fileInputStream)) {

            Sheet sheet = workbook.getSheetAt(0);
            for (Row row : sheet) {
                Cell cell = row.getCell(0);
                if (cell != null && cell.getRichStringCellValue().length() > 0) {
                    String username = cell.getRichStringCellValue().getString();
                    log.info("Found username: {}", username);
                    users.add(username);
                }
            }
            log.info("File parsed successfully!");
        } catch (IOException e) {
            log.error("Error parsing file", e);
        }

        return users;
    }

}
