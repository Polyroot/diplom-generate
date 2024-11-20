package ru.polyroot.diplom;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Qualifier("B")
public class DiplomaServiceB extends DiplomaService{

    public DiplomaServiceB(@Value("${files.diploma.pattern.muzhskoy}") String imageDiplomaPattern) {
        super(imageDiplomaPattern);
    }
}
