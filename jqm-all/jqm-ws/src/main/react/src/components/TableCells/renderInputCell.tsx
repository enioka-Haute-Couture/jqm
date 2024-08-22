import React from "react";
import TextField from "@mui/material/TextField/TextField";
import { Tooltip } from "@mui/material";
import { MUIDataTableMeta } from "mui-datatables";

export const renderInputCell =
    (
        inputRef: React.MutableRefObject<null>,
        editingRowId: number | null,
        toolTip: boolean = false,
        inputType: string = "text"
    ) =>
        (value: any, tableMeta: MUIDataTableMeta) => {
            const key = tableMeta.rowData[0];
            if (editingRowId === tableMeta.rowIndex) {
                const defaultDescription = tableMeta.rowData
                    ? tableMeta.rowData[tableMeta.columnIndex]
                    : "";
                return (
                    <TextField
                        key={`${key}-edit`}
                        defaultValue={defaultDescription}
                        inputRef={inputRef}
                        fullWidth
                        margin="normal"
                        inputProps={{
                            style: { fontSize: "0.875rem" },
                        }}
                        type={inputType}
                        variant="standard"
                    />
                );
            } else {
                const textField = getTextField(`${key}-display`, value, inputType);
                return toolTip && value != null ? (
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
        variant="standard"
    />
);
