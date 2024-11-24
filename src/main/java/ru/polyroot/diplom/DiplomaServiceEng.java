package ru.polyroot.diplom;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Qualifier("Eng")
public class DiplomaServiceEng extends DiplomaService{

    public DiplomaServiceEng(@Value("${files.diploma.font}") String signatureFontPath,
                           @Value("${files.diploma.border}") String signatureBorderPath,
                           @Value("${files.diploma.pattern.eng}") String imageDiplomaPattern) {
        super(signatureFontPath, signatureBorderPath, imageDiplomaPattern);
    }

}
