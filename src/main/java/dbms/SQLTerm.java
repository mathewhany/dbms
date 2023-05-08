package dbms;

public class SQLTerm {
    public String _strTableName;
    public String _strColumnName;
    public String _strOperator;
    public Object _objValue;

    public SQLTerm() {
    }

    public SQLTerm(String _strTableName, String _strColumnName, String _strOperator, Object _objValue) {
        this._strTableName = _strTableName;
        this._strColumnName = _strColumnName;
        this._strOperator = _strOperator;
        this._objValue = _objValue;
    }
}
