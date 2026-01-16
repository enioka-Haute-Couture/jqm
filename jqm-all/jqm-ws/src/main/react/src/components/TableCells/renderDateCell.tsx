import { DatePicker } from "@mui/x-date-pickers";
import { MUIDataTableMeta } from "mui-datatables";
import React from "react";
import { format } from "date-fns";
import type { Locale } from "date-fns";

export const renderDateCell =
    (
        editingRowId: number | null,
        editingValue: Date | null,
        setEditingValue: Function,
        locale: Locale
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
                    return format(new Date(value), "PPP", { locale });
                } else return "";
            }
        };
