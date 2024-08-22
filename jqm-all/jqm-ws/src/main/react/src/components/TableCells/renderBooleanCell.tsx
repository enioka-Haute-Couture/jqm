import React from "react";
import { Switch } from "@mui/material";
import DoneIcon from "@mui/icons-material/Done";
import BlockIcon from "@mui/icons-material/Block";
import { MUIDataTableMeta } from "mui-datatables";

export const renderBooleanCell =
    (
        editingRowId: number | null,
        isChecked: boolean | null,
        setBoolean: Function
    ) =>
        (value: any, tableMeta: MUIDataTableMeta) => {
            if (editingRowId === tableMeta.rowIndex) {
                return (
                    <Switch
                        checked={isChecked!!}
                        onChange={(event: React.ChangeEvent<HTMLInputElement>) =>
                            setBoolean(event.target.checked)
                        }
                    />
                );
            } else {
                return value ? <DoneIcon /> : <BlockIcon />;
            }
        };
