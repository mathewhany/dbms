package dbms.pages;

public class UpdatedRow {
    private Row oldRow;
    private Row updatedRow;

    public UpdatedRow(Row oldRow, Row updatedRow) {
        this.oldRow = oldRow;
        this.updatedRow = updatedRow;
    }

    public Row getOldRow() {
        return oldRow;
    }

    public Row getUpdatedRow() {
        return updatedRow;
    }


}
