package com.webank.wedatasphere.dss.apiservice.core.vo;

import java.util.Arrays;

public class FileContentVo {

    private InputTable[] input_tables;
    private Object[] input_partitions;

    // getter 和 setter 方法
    public InputTable[] getInput_tables() {
        return input_tables;
    }

    public void setInput_tables(InputTable[] input_tables) {
        this.input_tables = input_tables;
    }

    public Object[] getInput_partitions() {
        return input_partitions;
    }

    public void setInput_partitions(Object[] input_partitions) {
        this.input_partitions = input_partitions;
    }

    @Override
    public String toString() {
        return "FileContentVo{" +
                "input_tables=" + Arrays.toString(input_tables) +
                ", input_partitions=" + Arrays.toString(input_partitions) +
                '}';
    }

    public class InputTable {
        private String tablename;
        private String tabletype;

        // getter 和 setter 方法
        public String getTablename() {
            return tablename;
        }

        public void setTablename(String tablename) {
            this.tablename = tablename;
        }

        public String getTabletype() {
            return tabletype;
        }

        public void setTabletype(String tabletype) {
            this.tabletype = tabletype;
        }

        @Override
        public String toString() {
            return "InputTable{" +
                    "tablename='" + tablename + '\'' +
                    ", tabletype='" + tabletype + '\'' +
                    '}';
        }
    }
}
