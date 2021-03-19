import { Select, Input, FormControl } from "@material-ui/core";
import React from "react";


export const renderArrayCell = (
    editingRowId: number | null,
    menuItems: JSX.Element[],
    displayFunction: (element: number) => string,
    editingValue: number[] | null,
    setEditingValue: Function
) => (value: any, tableMeta: any) => {
    if (editingRowId === tableMeta.rowIndex) {
        return <FormControl fullWidth style={{ maxWidth: "150px" }} >
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
            return (value as number[]).map(displayFunction).join(","); // TODO: ellipsis if too long
        } else return "";
    }

};
