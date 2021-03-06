package ua.kh.khpi.alex_babenko;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import ua.kh.khpi.alex_babenko.art.Network;
import ua.kh.khpi.alex_babenko.exceptions.EmptyDataException;
import ua.kh.khpi.alex_babenko.services.FileService;
import ua.kh.khpi.alex_babenko.utils.Printer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
public class Application {

    private static final Logger LOG = Logger.getLogger(Application.class);

    @Value("${file.viruses}")
    private String fileVirusesName;
    @Value("${file.knowledge}")
    private String fileKnowledgeName;

    @Autowired
    private FileService fileService;
    @Autowired
    private Network network;

    public void start() {
        try {
            network.educateWithViruses(fileService.readMatrixFromFile(fileKnowledgeName));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Scheduled(fixedDelayString = "${file.reading.timeout}", initialDelayString = "${file.reading.timeout}" )
    private void execute() {
        try {
//            List<Double[]> viruses = network.findViruses(readFile());
//            Printer.printResult(viruses);
            for (double[] doubles : readFile()) {
                if (network.doesVirusDetected(doubles)) {
                    List<Double[]> objects = new ArrayList<>();
                    objects.add(ArrayUtils.toObject(doubles));
                    Printer.printResult(objects);
                }
            }
            LOG.info("Waiting for the next file.");
        } catch (IOException e) {
            LOG.error(e);
        } catch (EmptyDataException e) {
            LOG.info(e.getMessage());
        }
    }

    private double[][] readFile() throws IOException {
        double[][] result = fileService.readMatrixFromFile(fileVirusesName);
        if (ArrayUtils.isNotEmpty(result) && ArrayUtils.isNotEmpty(result[0])) {
            return result;
        }
        throw new EmptyDataException("No info in the file");
    }

}
