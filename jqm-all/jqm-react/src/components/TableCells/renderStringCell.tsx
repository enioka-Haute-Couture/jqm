import React from "react";
import TextField from "@material-ui/core/TextField/TextField";

export const renderStringCell = (
    inputRef: React.MutableRefObject<null>,
    editingRowId: number | null
) => (value: any, tableMeta: any) => {
    const key = tableMeta.rowData[0];
    if (editingRowId === tableMeta.rowIndex) {
        const defaultDescription = tableMeta.rowData
            ? tableMeta.rowData[tableMeta.columnIndex]
            : "";
        return (
            <TextField
                key={key}
                id="standard-basic"
                defaultValue={defaultDescription}
                inputRef={inputRef}
                fullWidth
                margin="normal"
                inputProps={{
                    style: { fontSize: "0.875rem" },
                }}
            />
        );
    } else {
        return (
            <TextField
                key={key}
                defaultValue={value}
                fullWidth
                margin="normal"
                InputProps={{ disableUnderline: true }}
                inputProps={{
                    // the actual input element
                    readOnly: true,
                    style: {
                        cursor: "default",
                        fontSize: "0.875rem",
                    },
                }}
            />
        );
    }
};
