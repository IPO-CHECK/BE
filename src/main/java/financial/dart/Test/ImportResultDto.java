package financial.dart.Test;


public class ImportResultDto {
    public int parsedRows;
    public int insertedRows;
    public int skippedRows;

    public ImportResultDto(int parsedRows, int insertedRows, int skippedRows) {
        this.parsedRows = parsedRows;
        this.insertedRows = insertedRows;
        this.skippedRows = skippedRows;
    }
}