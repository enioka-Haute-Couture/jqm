import React from "react";
import TextField from "@material-ui/core/TextField/TextField";

export const renderStringCell = (
    inputRef: React.MutableRefObject<null>,
    editingRowId: number | null
) => (value: any, tableMeta: any) => {
    const id = `${tableMeta.rowIndex}-${tableMeta.columnIndex}`
    if (editingRowId === tableMeta.rowIndex) {
        const defaultDescription = tableMeta.rowData
            ? tableMeta.rowData[tableMeta.columnIndex]
            : "";
        return (
            <TextField
                key={`${id}-edit`}
                id={id}
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
            // TODO: hides everything if too long
            <TextField
                key={`${id}-display`}
                id={id}
                value={value}
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
