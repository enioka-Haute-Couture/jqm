import { DatePicker } from "@mui/x-date-pickers";
import { MUIDataTableMeta } from "mui-datatables";
import React from "react";

export const renderDateCell =
    (
        editingRowId: number | null,
        editingValue: Date | null,
        setEditingValue: Function
    ) =>
        (value: any, tableMeta: MUIDataTableMeta) => {
            if (editingRowId === tableMeta.rowIndex) {
                return (
                    <DatePicker
                        format="dd/MM/yyyy"
                        value={editingValue}
                        onChange={(date) => {
                            setEditingValue(date);
                        }}
                    />
                );
            } else {
                if (value) {
                    return new Date(value).toDateString();
                } else return "";
            }
        };
