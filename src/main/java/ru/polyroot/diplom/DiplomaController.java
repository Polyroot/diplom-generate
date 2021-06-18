package ru.polyroot.diplom;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.http.fileupload.IOUtils;
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
@RequiredArgsConstructor
public class DiplomaController {

    private final DiplomaService diplomaService;

    @PostMapping(value="/diplomas/get", produces="application/zip")
    public ResponseEntity<StreamingResponseBody> getDiplomas(@RequestParam("users") MultipartFile inputFile) {

        return ResponseEntity
                .ok()
                .body(diplomaService.getDiplomas(inputFile));
    }


}
