package tohfile.jdbc;

import java.util.LinkedHashMap;

public class MysqlJDBC {
    private static String[] fieldNames = {
            "remote_addr",
            "time_local",
            "time_zone",
            "http_method",
            "request",
            "http_version",
            "status",
            "http_referer",
            "body_bytes_sent",
            "remote_user"
    };
    private static String[] fieldType = {
            "String",
            "String",
            "String",
            "String",
            "String",
            "String",
            "int",
            "String",
            "int",
            "String"
    };

    private static LinkedHashMap<String,String> tableField = new LinkedHashMap<>();

    static{
        for(int i = 0 ; i < fieldNames.length ; i++){
            tableField.put(fieldNames[i],fieldType[i]);
        }
    }


    public static LinkedHashMap<String,String> getTableSchema(String tableName){
        return tableField;
    }
}
