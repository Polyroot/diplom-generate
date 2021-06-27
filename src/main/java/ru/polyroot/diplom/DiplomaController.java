package ru.polyroot.diplom;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;


@RestController
@Slf4j
public class DiplomaController {

    @Qualifier("G")
    @Autowired
    private DiplomaService diplomaServiceG;
    @Qualifier("B")
    @Autowired
    private DiplomaService diplomaServiceB;

    @Autowired
    private CardService cardService;

    @PostMapping(value="/diplomas/get/g", produces="application/zip")
    public ResponseEntity<StreamingResponseBody> getDiplomasG(@RequestParam("users") MultipartFile inputFile) {

        return ResponseEntity
                .ok()
                .body(diplomaServiceG.getDiplomas(inputFile));
    }

    @PostMapping(value="/diplomas/get/b", produces="application/zip")
    public ResponseEntity<StreamingResponseBody> getDiplomasB(@RequestParam("users") MultipartFile inputFile) {

        return ResponseEntity
                .ok()
                .body(diplomaServiceB.getDiplomas(inputFile));
    }

    @PostMapping(value="/cards/get", produces="application/zip")
    public ResponseEntity<StreamingResponseBody> getCards(@RequestParam("users") MultipartFile inputFile) {

        return ResponseEntity
                .ok()
                .body(cardService.getCards(inputFile));
    }

}
