import { Select, Input, FormControl, Tooltip, Typography } from "@material-ui/core";
import React from "react";

const MAX_DISPLAY_SIZE = 50

export const renderArrayCell = (
    editingRowId: number | null,
    menuItems: JSX.Element[],
    displayFunction: (element: number) => string,
    editingValue: number[] | null,
    setEditingValue: Function
) => (value: any, tableMeta: any) => {
    if (editingRowId === tableMeta.rowIndex) {
        return <FormControl fullWidth style={{ maxWidth: "300px" }} >
            <Select
                multiple
                fullWidth
                value={editingValue ? editingValue!! : []}
                onChange={(event: React.ChangeEvent<{ value: unknown }>) => {
                    setEditingValue(event.target.value as number[]);
                }}
                input={<Input />}
            >
                {menuItems}
            </Select >
        </FormControl>;
    } else {
        if (value) {
            var strValue = (value as number[]).map(displayFunction).join(", ");
            return (strValue.length > MAX_DISPLAY_SIZE) ? (
                <Tooltip title={strValue}>
                    <Typography
                        style={{ fontSize: "0.875rem" }}
                    >
                        {strValue.slice(0, MAX_DISPLAY_SIZE) + "..."}
                    </Typography>
                </Tooltip >
            ) : (
                <Typography
                    style={{ fontSize: "0.875rem" }}
                >
                    {strValue}</Typography>
            )
        } else return "";
    }

};
