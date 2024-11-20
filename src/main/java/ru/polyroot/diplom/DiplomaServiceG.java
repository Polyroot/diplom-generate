package ru.polyroot.diplom;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Qualifier("G")
public class DiplomaServiceG extends DiplomaService{

    public DiplomaServiceG(@Value("${files.diploma.pattern.zhenskiy}") String imageDiplomaPattern) {
        super(imageDiplomaPattern);
    }

}
