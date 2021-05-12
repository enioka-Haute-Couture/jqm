import { KeyboardDatePicker } from "@material-ui/pickers";
import React from "react";

export const renderDateCell = (
    editingRowId: number | null,
    editingValue: Date | null,
    setEditingValue: Function
) => (value: any, tableMeta: any) => {
    if (editingRowId === tableMeta.rowIndex) {
        return <KeyboardDatePicker
            disableToolbar
            variant="inline"
            format="dd/MM/yyyy"
            margin="normal"
            fullWidth
            id="date-picker-inline"
            value={editingValue}
            onChange={(date) => {
                setEditingValue(date)
            }}
            KeyboardButtonProps={{
                'aria-label': 'change date',
            }}
        />
    } else {
        if (value) {
            return new Date(value).toDateString();
        } else return "";
    }
};
