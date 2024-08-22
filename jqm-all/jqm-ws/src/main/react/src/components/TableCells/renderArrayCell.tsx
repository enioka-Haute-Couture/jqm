import {
    FormControl,
    Input,
    Select,
    SelectChangeEvent,
    Tooltip,
    Typography,
} from "@mui/material";
import { MUIDataTableMeta } from "mui-datatables";
import React, { ReactNode } from "react";

const MAX_DISPLAY_SIZE = 50;

export const renderArrayCell =
    (
        editingRowId: number | null,
        menuItems: JSX.Element[],
        displayFunction: (element: number) => string,
        editingValue: number[] | number | null,
        setEditingValue: Function,
        multiple: boolean = true
    ) =>
        (value: any, tableMeta: MUIDataTableMeta) => {
            if (editingRowId === tableMeta.rowIndex) {
                return (
                    <FormControl
                        fullWidth
                        style={{ maxWidth: "300px", paddingTop: "5px" }}
                    >
                        <Select
                            multiple={multiple}
                            fullWidth
                            value={editingValue ? editingValue!! : []}
                            onChange={(event: SelectChangeEvent<number[] | number>, child: ReactNode) => {
                                if (multiple) {
                                    setEditingValue(event.target.value as number[]);
                                } else {
                                    setEditingValue(event.target.value as number);
                                }
                            }}
                            input={<Input />}
                        >
                            {menuItems}
                        </Select>
                    </FormControl>
                );
            } else {
                if (value) {
                    var strValue = multiple
                        ? (value as number[]).map(displayFunction).join(", ")
                        : displayFunction(value as number);

                    return strValue.length > MAX_DISPLAY_SIZE ? (
                        <Tooltip title={strValue}>
                            <Typography
                                style={{ fontSize: "0.875rem", paddingTop: "5px" }}
                            >
                                {strValue.slice(0, MAX_DISPLAY_SIZE) + "..."}
                            </Typography>
                        </Tooltip>
                    ) : (
                        <Typography
                            style={{ fontSize: "0.875rem", paddingTop: "5px" }}
                        >
                            {strValue}
                        </Typography>
                    );
                } else return "";
            }
        };
