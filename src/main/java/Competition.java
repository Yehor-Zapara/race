import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Competition {
    private static final String DIRECTORY = "race_logs";
    private static final String START_READ_FILE = "tag_read_start.log";
    private static final String FINISH_READ_FILE = "tag_reads_finish.log";
    private static final String TIMESTAMP_PATTERN = "yyMMddHHmmss";
    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern(TIMESTAMP_PATTERN);
    private static final int TAG_STARTS_AT = 4;
    private static final int TAG_ENDS_AT = 16;
    private static final int TIMESTAMP_STARTS_AT = 20;
    private static final int TIMESTAMP_ENDS_AT = 32;
    private static final int NUMBER_OF_PARTICIPANTS = 10;

    public static void main(String[] args) {
        Competition competition = new Competition();
        Map<String, LocalDateTime> startData =
                competition.readFile(DIRECTORY + File.separator + START_READ_FILE);
        Map<String, LocalDateTime> finishData =
                competition.readFile(DIRECTORY + File.separator + FINISH_READ_FILE);
        competition.findWinners(startData, finishData).forEach(System.out::println);
    }

    private Map<String, LocalDateTime> readFile(String filePath) {
        try (Stream<String> lines = Files.lines(Path.of(filePath))) {
            return lines.collect(Collectors.toMap(
                    line -> line.substring(TAG_STARTS_AT, TAG_ENDS_AT),
                    line -> LocalDateTime.parse(
                            line.substring(TIMESTAMP_STARTS_AT, TIMESTAMP_ENDS_AT), FORMATTER),
                    (line, duplicateLine) ->
                            filePath.endsWith(START_READ_FILE) ? line : duplicateLine));
        } catch (IOException ex) {
            throw new RuntimeException("Can't read data by path " + filePath, ex);
        }
    }

    private List<String> findWinners(Map<String, LocalDateTime> startData,
                                     Map<String, LocalDateTime> finishData) {
        Map<String, LocalDateTime> tagsData = new HashMap<>(startData);
        return tagsData.entrySet().stream()
                .peek(e -> e.setValue(parseDateTime(e.getValue(),
                        finishData.getOrDefault(e.getKey(), LocalDateTime.MAX))))
                .sorted(Map.Entry.comparingByValue())
                .limit(NUMBER_OF_PARTICIPANTS)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    private LocalDateTime parseDateTime(LocalDateTime startDateTime,
                                        LocalDateTime finishDateTime) {
        Period period = Period.between(
                startDateTime.toLocalDate(), finishDateTime.toLocalDate());
        Duration duration = Duration.between(
                startDateTime.toLocalTime(), finishDateTime.toLocalTime());
        return LocalDateTime.MIN.plus(period).plus(duration);
    }
}
