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
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
@Slf4j
public class DiplomaService {

    public static final String IMAGE_DIPLOMA_PATTERN = "patternDiploma/diplomaPattern.jpg";
    public static final String DIPLOMA_DIR = "diplomas";
    public static final String FONT = "diplomaFonts/arial.ttf";


    public StreamingResponseBody getDiplomas(MultipartFile inputFile) {

        log.info("input file with name {}", inputFile.getOriginalFilename());
        List<String> users = parseFileToList(inputFile);

        List<File> files = users.stream()
                .map(this::getFileDiploma)
                .collect(Collectors.toList());

        return out -> {
            createZipWithDiplomas(files, out);
            cleanDiplomasDir();
        };
    }

    private void createZipWithDiplomas(List<File> files, OutputStream out) throws IOException {
        try(ZipOutputStream zipOutputStream = new ZipOutputStream(out)) {
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


    private File getFileDiploma(String userName){

        InputStream ruleSet = ClassLoader.getSystemResourceAsStream("");

        String filePath = Objects.requireNonNull(getClass().getClassLoader().getResource("").getPath());

        File fileDiploma = new File(filePath + String.format(DIPLOMA_DIR + "/%s.pdf", "userName"));
        log.info("fileDiploma absolute path {}", filePath);

        try {
            Document document = new Document(PageSize.A4);
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(fileDiploma));
            document.open();

            addBackground(writer);
            addUserSignature(writer, userName);

            document.close();
        } catch (Exception e) {
            log.error(e.getLocalizedMessage(), e);
        }
        return fileDiploma;
    }

    private void addUserSignature(PdfWriter writer, String userName) throws DocumentException {
        PdfContentByte canvas1 = writer.getDirectContent();
        ColumnText ct = new ColumnText(canvas1);
        ct.setSimpleColumn(0, 430, 595, 485);
        ct.setAlignment(Element.ALIGN_CENTER);

        Font font = getFont();

        Chunk chunk = new Chunk(userName, font);
        ct.addText(chunk);
        ct.go();
    }

    private Font getFont() {
        try {
            BaseFont bf = BaseFont.createFont(FONT, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
            return new Font(bf, 36, Font.ITALIC);
        } catch (IOException | DocumentException e) {
            log.error(e.getLocalizedMessage(), e);
        }
        return new Font(Font.FontFamily.TIMES_ROMAN, 36, Font.ITALIC);
    }

    private void addBackground(PdfWriter writer) throws IOException, DocumentException {
        PdfContentByte canvas = writer.getDirectContentUnder();
        String filePath = Objects.requireNonNull(getClass().getClassLoader().getResource(IMAGE_DIPLOMA_PATTERN)).getPath();
        log.info("IMAGE_DIPLOMA_PATTERN absolute path {}", filePath);
        Image image = Image.getInstance(filePath);
        image.scaleAbsolute(PageSize.A4);
        image.setAbsolutePosition(0, 0);
        canvas.saveState();
        PdfGState state = new PdfGState();
        canvas.setGState(state);
        canvas.addImage(image);
        canvas.restoreState();
    }


    private List<String> parseFileToList(MultipartFile inputFile) {
        List<String> users = new ArrayList<>();

        log.info("file parsing to list:");
        try (InputStream fileInputStream = inputFile.getInputStream()){
            Workbook workbook = new XSSFWorkbook(fileInputStream);
            Sheet sheet = workbook.getSheetAt(0);

            for (Row row : sheet) {
                for (Cell cell : row) {
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

    public static void cleanDiplomasDir() {
        File diplomaDir = new File(DIPLOMA_DIR);
        File[] files = diplomaDir.listFiles();
        if (files != null) {
            Arrays.stream(files).forEach(File::delete);
        }
    }
}
