package ru.polyroot.diplom;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Qualifier("B")
public class DiplomaServiceB extends DiplomaService{

    @Value("${files.pattern_diploma_muzhskoy}")
    private String imageDiplomaPattern;

    @Override
    public String getImageDiplomaPattern() {
        return imageDiplomaPattern;
    }

}
