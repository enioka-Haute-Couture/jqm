import React from "react";
import { IconButton, Tooltip } from "@material-ui/core";
import DeleteIcon from "@material-ui/icons/Delete";
import CreateIcon from "@material-ui/icons/Create";
import SaveIcon from "@material-ui/icons/Save";
import CancelIcon from "@material-ui/icons/Cancel";

export interface extraActionItem {
    title: string;
    icon: JSX.Element;
    action: Function;
}

export const renderActionsCell = (
    onCancel: Function,
    onSave: Function,
    onDelete: Function,
    editingRowId: number | null,
    onEdit: Function,
    extraActionItems: extraActionItem[] = []
) => (value: any, tableMeta: any) => {
    if (editingRowId === tableMeta.rowIndex) {
        console.log(extraActionItems);
        return (
            <>
                <Tooltip title={"Cancel changes"}>
                    <IconButton
                        color="default"
                        aria-label={"cancel"}
                        onClick={() => onCancel(tableMeta)}
                    >
                        <CancelIcon />
                    </IconButton>
                </Tooltip>
                <Tooltip title={"Save changes"}>
                    <IconButton
                        color="default"
                        aria-label={"save"}
                        onClick={() => onSave(tableMeta)}
                    >
                        <SaveIcon />
                    </IconButton>
                </Tooltip>
            </>
        );
    } else {
        return (
            <>
                {extraActionItems.map((extraActionItem) => (
                    <Tooltip
                        key={extraActionItem.title}
                        title={extraActionItem.title}
                    >
                        <IconButton
                            color="default"
                            aria-label={extraActionItem.title}
                            onClick={() => extraActionItem.action(tableMeta)}
                        >
                            {extraActionItem.icon}
                        </IconButton>
                    </Tooltip>
                ))}
                <Tooltip title={"Edit line"}>
                    <IconButton
                        color="default"
                        aria-label={"edit"}
                        onClick={() => onEdit(tableMeta)}
                    >
                        <CreateIcon />
                    </IconButton>
                </Tooltip>
                <Tooltip title={"Delete line"}>
                    <IconButton
                        color="default"
                        aria-label={"delete"}
                        onClick={() => onDelete(tableMeta)}
                    >
                        <DeleteIcon />
                    </IconButton>
                </Tooltip>
            </>
        );
    }
};
