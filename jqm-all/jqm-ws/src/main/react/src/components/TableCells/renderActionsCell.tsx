import React from "react";
import { IconButton, Link, Tooltip } from "@mui/material";
import DeleteIcon from "@mui/icons-material/Delete";
import CreateIcon from "@mui/icons-material/Create";
import SaveIcon from "@mui/icons-material/Save";
import CancelIcon from "@mui/icons-material/Cancel";
import { MUIDataTableMeta } from "mui-datatables";
import { TFunction } from "i18next";

export interface extraActionItem {
    title: string;
    addIcon: Function;
    action?: Function;
    getLinkURL?: Function;
}

export const renderActionsCell =
    (
        onCancel: Function,
        onSave: Function,
        onDelete: Function | null,
        editingRowId: number | null,
        onEdit: Function,
        canEdit: boolean,
        canDelete: boolean,
        extraActionItems: extraActionItem[] = [],
        t: TFunction
    ) =>
        (value: any, tableMeta: MUIDataTableMeta) => {
            if (editingRowId === tableMeta.rowIndex) {
                return <>
                    <Tooltip title={t("common.cancelChanges")}>
                        <IconButton
                            color="default"
                            aria-label={"cancel"}
                            onClick={() => onCancel(tableMeta)}
                            size="large">
                            <CancelIcon />
                        </IconButton>
                    </Tooltip>
                    <Tooltip title={t("common.save")}>
                        <IconButton
                            color="default"
                            aria-label={"save"}
                            onClick={() => onSave(tableMeta)}
                            size="large">
                            <SaveIcon />
                        </IconButton>
                    </Tooltip>
                </>;
            } else {
                return <> {extraActionItems.map((extraActionItem) => (
                    <Tooltip
                        key={extraActionItem.title}
                        title={extraActionItem.title}
                    >
                        {extraActionItem.getLinkURL ?
                            (
                                <IconButton
                                    component={Link}
                                    href={extraActionItem.getLinkURL!(tableMeta)}
                                    aria-label={extraActionItem.title}
                                    size="large">
                                    {extraActionItem.addIcon(tableMeta)}
                                </IconButton>)
                            :
                            (
                                <IconButton
                                    color="default"
                                    aria-label={extraActionItem.title}
                                    onClick={() => {
                                        if (extraActionItem.action) {
                                            extraActionItem.action!(tableMeta)
                                        }
                                    }
                                    }
                                    size="large">
                                    {extraActionItem.addIcon(tableMeta)}
                                </IconButton>
                            )

                        }

                    </Tooltip>
                ))
                }

                    {canEdit &&
                        <Tooltip title={t("common.edit")}>
                            <IconButton
                                color="default"
                                aria-label={"edit"}
                                onClick={() => onEdit(tableMeta)}
                                size="large">
                                <CreateIcon />
                            </IconButton>
                        </Tooltip>
                    }
                    {onDelete && canDelete && (
                        <Tooltip title={t("common.delete")}>
                            <IconButton
                                color="default"
                                aria-label={"delete"}
                                onClick={() => onDelete(tableMeta)}
                                size="large">
                                <DeleteIcon />
                            </IconButton>
                        </Tooltip>
                    )}
                </>;
            }
        };
