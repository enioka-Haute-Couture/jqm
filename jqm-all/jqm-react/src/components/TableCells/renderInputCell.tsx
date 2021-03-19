import React from "react";
import TextField from "@material-ui/core/TextField/TextField";
import { Tooltip } from "@material-ui/core";

export const renderInputCell = (
    inputRef: React.MutableRefObject<null>,
    editingRowId: number | null,
    toolTip: boolean = false,
    inputType: string = "text"
) => (value: any, tableMeta: any) => {
    const key = tableMeta.rowData[0];
    if (editingRowId === tableMeta.rowIndex) {
        const defaultDescription = tableMeta.rowData
            ? tableMeta.rowData[tableMeta.columnIndex]
            : "";
        return (
            <TextField
                key={`${key}-edit`}
                id="standard-basic"
                defaultValue={defaultDescription}
                inputRef={inputRef}
                fullWidth
                margin="normal"
                inputProps={{
                    style: { fontSize: "0.875rem" },
                }}
                type={inputType}
            />
        );
    } else {
        const textField = getTextField(`${key}-display`, value, inputType);
        return toolTip ? (
            <Tooltip title={value}>{textField}</Tooltip>
        ) : (
            textField
        );
    }
};

const getTextField = (key: string, value: string, inputType: string) => (
    <TextField
        key={key}
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
        type={inputType}
    />
);
